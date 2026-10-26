package io.github.gracegrc.controlapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import io.github.gracegrc.controlapi.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.entity.ControlEntity;
import io.github.gracegrc.controlapi.domain.model.exception.ControlAlreadyExistsException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupNotFoundException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlNotFoundException;
import io.github.gracegrc.controlapi.domain.repository.ControlGroupRepository;
import io.github.gracegrc.controlapi.domain.repository.ControlRepository;
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
class ControlServiceImplTest {

	private static final UUID ID = UUID.fromString("7c3d2e1f-0a9b-4c8d-8e7f-6a5b4c3d2e1f");

	private static final UUID GROUP_ID = UUID.fromString("5b1e8c2a-3d4f-4a6b-8c9d-0e1f2a3b4c5d");

	private static final UUID PARENT_ID = UUID.fromString("2d4f6a8c-1b3d-4e5f-9a7b-8c9d0e1f2a3b");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final ControlEntity EXISTING = new ControlEntity(ID, GROUP_ID, null, "AC-2", "Account Management",
			null, CREATED_AT, "admin-console");

	private static final ControlEntity ENHANCEMENT = new ControlEntity(UUID.randomUUID(), GROUP_ID, ID, "AC-2(1)",
			"Automated Account Management", null, CREATED_AT, "admin-console");

	@Mock
	private ControlRepository controlRepository;

	@Mock
	private ControlGroupRepository controlGroupRepository;

	@InjectMocks
	private ControlServiceImpl controlService;

	@Captor
	private ArgumentCaptor<ControlEntity> saved;

	@Test
	void createSavesTheControlWithTheCurrentTime() {
		given(controlRepository.save(any())).willAnswer(invocation -> {
			ControlEntity entity = invocation.getArgument(0);
			return new ControlEntity(ID, entity.groupId(), entity.parentControlId(), entity.controlRef(),
					entity.title(), entity.statement(), entity.createdAt(), entity.createdBy());
		});
		Instant before = Instant.now();

		UUID id = controlService
			.create(new ControlRequestDto(null, null, "AC-2", "Account Management", "Manage accounts.", "admin-console"));

		then(controlRepository).should().save(saved.capture());
		assertThat(saved.getValue().id()).isNull();
		assertThat(saved.getValue().controlRef()).isEqualTo("AC-2");
		assertThat(saved.getValue().statement()).isEqualTo("Manage accounts.");
		assertThat(saved.getValue().createdAt()).isBetween(before, Instant.now());
		assertThat(saved.getValue().createdBy()).isEqualTo("admin-console");
		assertThat(id).isEqualTo(ID);
		then(controlGroupRepository).shouldHaveNoInteractions();
		then(controlRepository).should(never()).existsById(any());
	}

	@Test
	void createWithAnExistingGroupAndParentSavesTheControl() {
		given(controlGroupRepository.existsById(GROUP_ID)).willReturn(true);
		given(controlRepository.existsById(PARENT_ID)).willReturn(true);
		given(controlRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		controlService.create(new ControlRequestDto(GROUP_ID, PARENT_ID, "AC-2(1)", "Automated", null, "admin-console"));

		then(controlRepository).should().save(saved.capture());
		assertThat(saved.getValue().groupId()).isEqualTo(GROUP_ID);
		assertThat(saved.getValue().parentControlId()).isEqualTo(PARENT_ID);
	}

	@Test
	void createWithAnUnknownGroupThrowsGroupNotFoundAndSavesNothing() {
		given(controlGroupRepository.existsById(GROUP_ID)).willReturn(false);

		assertThatThrownBy(() -> controlService
			.create(new ControlRequestDto(GROUP_ID, null, "AC-2", "Account Management", null, "admin-console")))
			.isInstanceOf(ControlGroupNotFoundException.class)
			.hasMessage("Control group " + GROUP_ID + " not found");
		then(controlRepository).should(never()).save(any());
	}

	@Test
	void createWithAnUnknownParentThrowsNotFoundAndSavesNothing() {
		given(controlRepository.existsById(PARENT_ID)).willReturn(false);

		assertThatThrownBy(() -> controlService
			.create(new ControlRequestDto(null, PARENT_ID, "AC-2(1)", "Automated", null, "admin-console")))
			.isInstanceOf(ControlNotFoundException.class)
			.hasMessage("Control " + PARENT_ID + " not found");
		then(controlRepository).should(never()).save(any());
	}

	@Test
	void createWithADuplicateRefThrowsAlreadyExists() {
		given(controlRepository.save(any())).willThrow(new DuplicateKeyException("controls_control_ref_key"));

		assertThatThrownBy(() -> controlService
			.create(new ControlRequestDto(null, null, "AC-2", "Account Management", null, "admin-console")))
			.isInstanceOf(ControlAlreadyExistsException.class)
			.hasMessage("A control with ref 'AC-2' already exists");
	}

	@Test
	void findAllWithoutFiltersReturnsEveryControl() {
		given(controlRepository.findAll()).willReturn(List.of(EXISTING, ENHANCEMENT));

		List<ControlResponseDto> response = controlService.findAll(null, null);

		assertThat(response).extracting(ControlResponseDto::controlRef).containsExactly("AC-2", "AC-2(1)");
	}

	@Test
	void findAllWithGroupIdReturnsTheGroupsControls() {
		given(controlRepository.findByGroupId(GROUP_ID)).willReturn(List.of(EXISTING));

		assertThat(controlService.findAll(GROUP_ID, null)).extracting(ControlResponseDto::controlRef)
			.containsExactly("AC-2");
		then(controlRepository).should(never()).findAll();
	}

	@Test
	void findAllWithParentIdReturnsTheEnhancements() {
		given(controlRepository.findByParentControlId(ID)).willReturn(List.of(ENHANCEMENT));

		assertThat(controlService.findAll(null, ID)).extracting(ControlResponseDto::controlRef)
			.containsExactly("AC-2(1)");
		then(controlRepository).should(never()).findAll();
	}

	@Test
	void findAllWithGroupIdAndParentIdUsesBothFilters() {
		given(controlRepository.findByGroupIdAndParentControlId(GROUP_ID, ID)).willReturn(List.of(ENHANCEMENT));

		assertThat(controlService.findAll(GROUP_ID, ID)).extracting(ControlResponseDto::controlRef)
			.containsExactly("AC-2(1)");
		then(controlRepository).should(never()).findByGroupId(any());
		then(controlRepository).should(never()).findByParentControlId(any());
	}

	@Test
	void findByIdReturnsTheControl() {
		given(controlRepository.findById(ID)).willReturn(Optional.of(EXISTING));

		assertThat(controlService.findById(ID)).isEqualTo(new ControlResponseDto(ID, GROUP_ID, null, "AC-2",
				"Account Management", null, CREATED_AT, "admin-console"));
	}

	@Test
	void findByIdWithAnUnknownIdThrowsNotFound() {
		given(controlRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> controlService.findById(ID)).isInstanceOf(ControlNotFoundException.class)
			.hasMessage("Control " + ID + " not found");
	}

	@Test
	void updateChangesEverythingButIdCreatedAtAndCreatedBy() {
		given(controlRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(controlRepository.existsById(PARENT_ID)).willReturn(true);
		given(controlRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		ControlResponseDto response = controlService.update(ID,
				new ControlUpdateRequestDto(null, PARENT_ID, "AC-2(2)", "Removal", "Remove accounts."));

		assertThat(response).isEqualTo(new ControlResponseDto(ID, null, PARENT_ID, "AC-2(2)", "Removal",
				"Remove accounts.", CREATED_AT, "admin-console"));
	}

	@Test
	void updateWithAnUnknownIdThrowsNotFoundAndSavesNothing() {
		given(controlRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> controlService.update(ID,
				new ControlUpdateRequestDto(null, null, "AC-2", "Account Management", null)))
			.isInstanceOf(ControlNotFoundException.class);
		then(controlRepository).should(never()).save(any());
	}

	@Test
	void updateToADuplicateRefThrowsAlreadyExists() {
		given(controlRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(controlGroupRepository.existsById(GROUP_ID)).willReturn(true);
		given(controlRepository.save(any())).willThrow(new DuplicateKeyException("controls_control_ref_key"));

		assertThatThrownBy(() -> controlService.update(ID,
				new ControlUpdateRequestDto(GROUP_ID, null, "AC-3", "Access Enforcement", null)))
			.isInstanceOf(ControlAlreadyExistsException.class)
			.hasMessage("A control with ref 'AC-3' already exists");
	}

	@Test
	void deleteRemovesAnExistingControl() {
		given(controlRepository.existsById(ID)).willReturn(true);

		controlService.delete(ID);

		then(controlRepository).should().deleteById(ID);
	}

	@Test
	void deleteWithAnUnknownIdThrowsNotFoundAndDeletesNothing() {
		given(controlRepository.existsById(ID)).willReturn(false);

		assertThatThrownBy(() -> controlService.delete(ID)).isInstanceOf(ControlNotFoundException.class);
		then(controlRepository).should(never()).deleteById(any());
	}

	@Test
	void deleteOfAControlWithEnhancementsThrowsInUse() {
		given(controlRepository.existsById(ID)).willReturn(true);
		willThrow(new DataIntegrityViolationException("controls_parent_control_id_fkey")).given(controlRepository)
			.deleteById(ID);

		assertThatThrownBy(() -> controlService.delete(ID)).isInstanceOf(ControlInUseException.class)
			.hasMessage("Control " + ID + " still has enhancements");
	}

}
