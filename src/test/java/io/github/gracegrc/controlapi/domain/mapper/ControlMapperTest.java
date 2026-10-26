package io.github.gracegrc.controlapi.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.controlapi.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.entity.ControlEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlMapperTest {

	private static final UUID ID = UUID.fromString("7c3d2e1f-0a9b-4c8d-8e7f-6a5b4c3d2e1f");

	private static final UUID GROUP_ID = UUID.fromString("5b1e8c2a-3d4f-4a6b-8c9d-0e1f2a3b4c5d");

	private static final UUID PARENT_ID = UUID.fromString("2d4f6a8c-1b3d-4e5f-9a7b-8c9d0e1f2a3b");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Test
	void toEntityFromRequestHasNoIdAndUsesTheGivenCreatedAt() {
		ControlRequestDto request = new ControlRequestDto(GROUP_ID, PARENT_ID, "AC-2(1)", "Automated Account Management",
				"Support the management of accounts.", "admin-console");

		ControlEntity entity = ControlMapper.toEntity(request, CREATED_AT);

		assertThat(entity).isEqualTo(new ControlEntity(null, GROUP_ID, PARENT_ID, "AC-2(1)",
				"Automated Account Management", "Support the management of accounts.", CREATED_AT, "admin-console"));
	}

	@Test
	void toEntityFromUpdateKeepsIdCreatedAtAndCreatedBy() {
		ControlEntity existing = new ControlEntity(ID, null, null, "AC-2", "Accounts", null, CREATED_AT,
				"admin-console");
		ControlUpdateRequestDto request = new ControlUpdateRequestDto(GROUP_ID, PARENT_ID, "AC-2(1)",
				"Account Management", "Manage accounts.");

		ControlEntity entity = ControlMapper.toEntity(existing, request);

		assertThat(entity).isEqualTo(new ControlEntity(ID, GROUP_ID, PARENT_ID, "AC-2(1)", "Account Management",
				"Manage accounts.", CREATED_AT, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		ControlEntity entity = new ControlEntity(ID, GROUP_ID, PARENT_ID, "AC-2", "Account Management",
				"Manage accounts.", CREATED_AT, "admin-console");

		ControlResponseDto response = ControlMapper.toResponseDto(entity);

		assertThat(response).isEqualTo(new ControlResponseDto(ID, GROUP_ID, PARENT_ID, "AC-2", "Account Management",
				"Manage accounts.", CREATED_AT, "admin-console"));
	}

}
