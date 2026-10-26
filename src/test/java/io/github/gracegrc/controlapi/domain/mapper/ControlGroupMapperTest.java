package io.github.gracegrc.controlapi.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.entity.ControlGroupEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlGroupMapperTest {

	private static final UUID ID = UUID.fromString("5b1e8c2a-3d4f-4a6b-8c9d-0e1f2a3b4c5d");

	private static final UUID PARENT_ID = UUID.fromString("9a8b7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Test
	void toEntityFromRequestHasNoIdAndUsesTheGivenCreatedAt() {
		ControlGroupRequestDto request = new ControlGroupRequestDto(PARENT_ID, "AC", "Access Control", "admin-console");

		ControlGroupEntity entity = ControlGroupMapper.toEntity(request, CREATED_AT);

		assertThat(entity)
			.isEqualTo(new ControlGroupEntity(null, PARENT_ID, "AC", "Access Control", CREATED_AT, "admin-console"));
	}

	@Test
	void toEntityFromUpdateKeepsIdCreatedAtAndCreatedBy() {
		ControlGroupEntity existing = new ControlGroupEntity(ID, null, "AC", "Access", CREATED_AT, "admin-console");
		ControlGroupUpdateRequestDto request = new ControlGroupUpdateRequestDto(PARENT_ID, "AC-X", "Access Control");

		ControlGroupEntity entity = ControlGroupMapper.toEntity(existing, request);

		assertThat(entity)
			.isEqualTo(new ControlGroupEntity(ID, PARENT_ID, "AC-X", "Access Control", CREATED_AT, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		ControlGroupEntity entity = new ControlGroupEntity(ID, PARENT_ID, "AC", "Access Control", CREATED_AT,
				"admin-console");

		ControlGroupResponseDto response = ControlGroupMapper.toResponseDto(entity);

		assertThat(response).isEqualTo(
				new ControlGroupResponseDto(ID, PARENT_ID, "AC", "Access Control", CREATED_AT, "admin-console"));
	}

}
