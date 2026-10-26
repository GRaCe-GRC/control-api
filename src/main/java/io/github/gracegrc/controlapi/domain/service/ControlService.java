package io.github.gracegrc.controlapi.domain.service;

import io.github.gracegrc.controlapi.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface ControlService {

	UUID create(ControlRequestDto request);

	List<ControlResponseDto> findAll(UUID groupId, UUID parentId);

	ControlResponseDto findById(UUID id);

	ControlResponseDto update(UUID id, ControlUpdateRequestDto request);

	void delete(UUID id);

}
