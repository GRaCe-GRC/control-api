package io.github.gracegrc.controlapi.domain.service;

import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface ControlGroupService {

	UUID create(ControlGroupRequestDto request);

	List<ControlGroupResponseDto> findAll(UUID parentId);

	ControlGroupResponseDto findById(UUID id);

	ControlGroupResponseDto update(UUID id, ControlGroupUpdateRequestDto request);

	void delete(UUID id);

}
