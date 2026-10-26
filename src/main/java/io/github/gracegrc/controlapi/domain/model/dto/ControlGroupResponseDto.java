package io.github.gracegrc.controlapi.domain.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ControlGroupResponseDto(
		UUID id,
		UUID parentGroupId,
		String groupRef,
		String title,
		Instant createdAt,
		String createdBy) {
}
