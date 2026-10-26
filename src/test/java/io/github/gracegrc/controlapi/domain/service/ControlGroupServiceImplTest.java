package io.github.gracegrc.controlapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.entity.ControlGroupEntity;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupAlreadyExistsException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupNotFoundException;
import io.github.gracegrc.controlapi.domain.repository.ControlGroupRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class ControlGroupServiceImplTest {

	private static final UUID ID = UUID.fromString("5b1e8c2a-3d4f-4a6b-8c9d-0e1f2a3b4c5d");

	private static final UUID PARENT_ID = UUID.fromString("9a8b7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final ControlGroupEntity EXISTING = new ControlGroupEntity(ID, null, "AC", "Access Control",
			CREATED_AT, "admin-console");

	@Mock
	private ControlGroupRepository controlGroupRepository;

	@InjectMocks
	private ControlGroupServiceImpl controlGroupService;

	@Captor
	private ArgumentCaptor<ControlGroupEntity> saved;

	@Test
	void createSavesTheGroupWithTheCurrentTime() {
		given(controlGroupRepository.save(any())).willAnswer(invocation -> {
			ControlGroupEntity entity = invocation.getArgument(0);
			return new ControlGroupEntity(ID, entity.parentGroupId(), entity.groupRef(), entity.title(),
					entity.createdAt(), entity.createdBy());
		});
		Instant before = Instant.now();

		UUID id = controlGroupService.create(new ControlGroupRequestDto(null, "AC", "Access Control", "admin-console"));

		then(controlGroupRepository).should().save(saved.capture());
		assertThat(saved.getValue().id()).isNull();
		assertThat(saved.getValue().parentGroupId()).isNull();
		assertThat(saved.getValue().groupRef()).isEqualTo("AC");
		assertThat(saved.getValue().createdAt()).isBetween(before, Instant.now());
		assertThat(saved.getValue().createdBy()).isEqualTo("admin-console");
		assertThat(id).isEqualTo(ID);
		then(controlGroupRepository).should(never()).existsById(any());
	}

	@Test
	void createWithAnExistingParentSavesTheGroup() {
		given(controlGroupRepository.existsById(PARENT_ID)).willReturn(true);
		given(controlGroupRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		controlGroupService.create(new ControlGroupRequestDto(PARENT_ID, "AC-SUB", "Sub", "admin-console"));

		then(controlGroupRepository).should().save(saved.capture());
		assertThat(saved.getValue().parentGroupId()).isEqualTo(PARENT_ID);
	}

	@Test
	void createWithAnUnknownParentThrowsNotFoundAndSavesNothing() {
		given(controlGroupRepository.existsById(PARENT_ID)).willReturn(false);

		assertThatThrownBy(() -> controlGroupService
			.create(new ControlGroupRequestDto(PARENT_ID, "AC-SUB", "Sub", "admin-console")))
			.isInstanceOf(ControlGroupNotFoundException.class)
			.hasMessage("Control group " + PARENT_ID + " not found");
		then(controlGroupRepository).should(never()).save(any());
	}

	@Test
	void createWithADuplicateRefThrowsAlreadyExists() {
		given(controlGroupRepository.save(any())).willThrow(new DuplicateKeyException("control_groups_group_ref_key"));

		assertThatThrownBy(() -> controlGroupService
			.create(new ControlGroupRequestDto(null, "AC", "Access Control", "admin-console")))
			.isInstanceOf(ControlGroupAlreadyExistsException.class)
			.hasMessage("A control group with ref 'AC' already exists");
	}

	@Test
	void findAllWithoutParentReturnsEveryGroup() {
		ControlGroupEntity other = new ControlGroupEntity(PARENT_ID, null, "AU", "Audit", CREATED_AT, "admin-console");
		given(controlGroupRepository.findAll()).willReturn(List.of(EXISTING, other));

		List<ControlGroupResponseDto> response = controlGroupService.findAll(null);

		assertThat(response).extracting(ControlGroupResponseDto::groupRef).containsExactly("AC", "AU");
		then(controlGroupRepository).should(never()).findByParentGroupId(any());
	}

	@Test
	void findAllWithParentReturnsItsChildren() {
		ControlGroupEntity child = new ControlGroupEntity(UUID.randomUUID(), PARENT_ID, "AC-SUB", "Sub", CREATED_AT,
				"admin-console");
		given(controlGroupRepository.findByParentGroupId(PARENT_ID)).willReturn(List.of(child));

		List<ControlGroupResponseDto> response = controlGroupService.findAll(PARENT_ID);

		assertThat(response).extracting(ControlGroupResponseDto::groupRef).containsExactly("AC-SUB");
		then(controlGroupRepository).should(never()).findAll();
	}

	@Test
	void findByIdReturnsTheGroup() {
		given(controlGroupRepository.findById(ID)).willReturn(Optional.of(EXISTING));

		assertThat(controlGroupService.findById(ID))
			.isEqualTo(new ControlGroupResponseDto(ID, null, "AC", "Access Control", CREATED_AT, "admin-console"));
	}

	@Test
	void findByIdWithAnUnknownIdThrowsNotFound() {
		given(controlGroupRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> controlGroupService.findById(ID)).isInstanceOf(ControlGroupNotFoundException.class)
			.hasMessage("Control group " + ID + " not found");
	}

	@Test
	void updateChangesParentRefAndTitleOnly() {
		given(controlGroupRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(controlGroupRepository.existsById(PARENT_ID)).willReturn(true);
		given(controlGroupRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		ControlGroupResponseDto response = controlGroupService.update(ID,
				new ControlGroupUpdateRequestDto(PARENT_ID, "AC-1", "Access"));

		assertThat(response)
			.isEqualTo(new ControlGroupResponseDto(ID, PARENT_ID, "AC-1", "Access", CREATED_AT, "admin-console"));
	}

	@Test
	void updateWithAnUnknownIdThrowsNotFoundAndSavesNothing() {
		given(controlGroupRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(
				() -> controlGroupService.update(ID, new ControlGroupUpdateRequestDto(null, "AC", "Access Control")))
			.isInstanceOf(ControlGroupNotFoundException.class);
		then(controlGroupRepository).should(never()).save(any());
	}

	@Test
	void updateToADuplicateRefThrowsAlreadyExists() {
		given(controlGroupRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(controlGroupRepository.save(any())).willThrow(new DuplicateKeyException("control_groups_group_ref_key"));

		assertThatThrownBy(() -> controlGroupService.update(ID, new ControlGroupUpdateRequestDto(null, "AU", "Audit")))
			.isInstanceOf(ControlGroupAlreadyExistsException.class)
			.hasMessage("A control group with ref 'AU' already exists");
	}

	@Test
	void deleteRemovesAnExistingGroup() {
		given(controlGroupRepository.existsById(ID)).willReturn(true);

		controlGroupService.delete(ID);

		then(controlGroupRepository).should().deleteById(ID);
	}

	@Test
	void deleteWithAnUnknownIdThrowsNotFoundAndDeletesNothing() {
		given(controlGroupRepository.existsById(ID)).willReturn(false);

		assertThatThrownBy(() -> controlGroupService.delete(ID)).isInstanceOf(ControlGroupNotFoundException.class);
		then(controlGroupRepository).should(never()).deleteById(any());
	}

	@Test
	void deleteOfAGroupWithChildrenThrowsInUse() {
		given(controlGroupRepository.existsById(ID)).willReturn(true);
		willThrow(new DataIntegrityViolationException("control_groups_parent_group_id_fkey"))
			.given(controlGroupRepository)
			.deleteById(ID);

		assertThatThrownBy(() -> controlGroupService.delete(ID)).isInstanceOf(ControlGroupInUseException.class)
			.hasMessage("Control group " + ID + " still has child groups or controls");
	}

}
