package io.github.gracegrc.controlapi.domain.mapper;

import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.entity.ControlGroupEntity;
import java.time.Instant;

public final class ControlGroupMapper {

	private ControlGroupMapper() {
	}

	public static ControlGroupEntity toEntity(ControlGroupRequestDto request, Instant createdAt) {
		return new ControlGroupEntity(null, request.parentGroupId(), request.groupRef(), request.title(), createdAt,
				request.createdBy());
	}

	public static ControlGroupEntity toEntity(ControlGroupEntity existing, ControlGroupUpdateRequestDto request) {
		return new ControlGroupEntity(existing.id(), request.parentGroupId(), request.groupRef(), request.title(),
				existing.createdAt(), existing.createdBy());
	}

	public static ControlGroupResponseDto toResponseDto(ControlGroupEntity entity) {
		return new ControlGroupResponseDto(entity.id(), entity.parentGroupId(), entity.groupRef(), entity.title(),
				entity.createdAt(), entity.createdBy());
	}

}
