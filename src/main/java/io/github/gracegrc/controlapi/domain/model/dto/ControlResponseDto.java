package io.github.gracegrc.controlapi.domain.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ControlResponseDto(
		UUID id,
		UUID groupId,
		UUID parentControlId,
		String controlRef,
		String title,
		String statement,
		Instant createdAt,
		String createdBy) {
}
