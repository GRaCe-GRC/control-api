package io.github.gracegrc.controlapi.domain.mapper;

import io.github.gracegrc.controlapi.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.entity.ControlEntity;
import java.time.Instant;

public final class ControlMapper {

	private ControlMapper() {
	}

	public static ControlEntity toEntity(ControlRequestDto request, Instant createdAt) {
		return new ControlEntity(null, request.groupId(), request.parentControlId(), request.controlRef(),
				request.title(), request.statement(), createdAt, request.createdBy());
	}

	public static ControlEntity toEntity(ControlEntity existing, ControlUpdateRequestDto request) {
		return new ControlEntity(existing.id(), request.groupId(), request.parentControlId(), request.controlRef(),
				request.title(), request.statement(), existing.createdAt(), existing.createdBy());
	}

	public static ControlResponseDto toResponseDto(ControlEntity entity) {
		return new ControlResponseDto(entity.id(), entity.groupId(), entity.parentControlId(), entity.controlRef(),
				entity.title(), entity.statement(), entity.createdAt(), entity.createdBy());
	}

}
