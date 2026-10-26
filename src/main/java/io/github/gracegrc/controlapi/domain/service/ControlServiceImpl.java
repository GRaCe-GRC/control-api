package io.github.gracegrc.controlapi.domain.service;

import io.github.gracegrc.controlapi.domain.mapper.ControlMapper;
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
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ControlServiceImpl implements ControlService {

	private final ControlRepository controlRepository;

	private final ControlGroupRepository controlGroupRepository;

	public ControlServiceImpl(ControlRepository controlRepository, ControlGroupRepository controlGroupRepository) {
		this.controlRepository = controlRepository;
		this.controlGroupRepository = controlGroupRepository;
	}

	@Override
	public UUID create(ControlRequestDto request) {
		ControlEntity control = ControlMapper.toEntity(request, Instant.now());
		return save(control).id();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ControlResponseDto> findAll(UUID groupId, UUID parentId) {
		List<ControlEntity> controls;
		if (groupId != null && parentId != null) {
			controls = controlRepository.findByGroupIdAndParentControlId(groupId, parentId);
		}
		else if (groupId != null) {
			controls = controlRepository.findByGroupId(groupId);
		}
		else if (parentId != null) {
			controls = controlRepository.findByParentControlId(parentId);
		}
		else {
			controls = controlRepository.findAll();
		}
		return controls.stream().map(ControlMapper::toResponseDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ControlResponseDto findById(UUID id) {
		return ControlMapper.toResponseDto(findEntity(id));
	}

	@Override
	public ControlResponseDto update(UUID id, ControlUpdateRequestDto request) {
		ControlEntity control = ControlMapper.toEntity(findEntity(id), request);
		return ControlMapper.toResponseDto(save(control));
	}

	@Override
	public void delete(UUID id) {
		if (!controlRepository.existsById(id)) {
			throw new ControlNotFoundException(id);
		}
		try {
			controlRepository.deleteById(id);
		}
		catch (DataIntegrityViolationException ex) {
			throw new ControlInUseException(id);
		}
	}

	private ControlEntity findEntity(UUID id) {
		return controlRepository.findById(id).orElseThrow(() -> new ControlNotFoundException(id));
	}

	private ControlEntity save(ControlEntity control) {
		UUID groupId = control.groupId();
		if (groupId != null && !controlGroupRepository.existsById(groupId)) {
			throw new ControlGroupNotFoundException(groupId);
		}
		UUID parentControlId = control.parentControlId();
		if (parentControlId != null && !controlRepository.existsById(parentControlId)) {
			throw new ControlNotFoundException(parentControlId);
		}
		try {
			return controlRepository.save(control);
		}
		catch (DuplicateKeyException ex) {
			throw new ControlAlreadyExistsException(control.controlRef());
		}
	}

}
