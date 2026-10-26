package io.github.gracegrc.controlapi.domain.service;

import io.github.gracegrc.controlapi.domain.mapper.ControlGroupMapper;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.entity.ControlGroupEntity;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupAlreadyExistsException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupNotFoundException;
import io.github.gracegrc.controlapi.domain.repository.ControlGroupRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ControlGroupServiceImpl implements ControlGroupService {

	private final ControlGroupRepository controlGroupRepository;

	public ControlGroupServiceImpl(ControlGroupRepository controlGroupRepository) {
		this.controlGroupRepository = controlGroupRepository;
	}

	@Override
	public UUID create(ControlGroupRequestDto request) {
		ControlGroupEntity group = ControlGroupMapper.toEntity(request, Instant.now());
		return save(group).id();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ControlGroupResponseDto> findAll(UUID parentId) {
		List<ControlGroupEntity> groups = parentId == null ? controlGroupRepository.findAll()
				: controlGroupRepository.findByParentGroupId(parentId);
		return groups.stream().map(ControlGroupMapper::toResponseDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ControlGroupResponseDto findById(UUID id) {
		return ControlGroupMapper.toResponseDto(findEntity(id));
	}

	@Override
	public ControlGroupResponseDto update(UUID id, ControlGroupUpdateRequestDto request) {
		ControlGroupEntity group = ControlGroupMapper.toEntity(findEntity(id), request);
		return ControlGroupMapper.toResponseDto(save(group));
	}

	@Override
	public void delete(UUID id) {
		if (!controlGroupRepository.existsById(id)) {
			throw new ControlGroupNotFoundException(id);
		}
		try {
			controlGroupRepository.deleteById(id);
		}
		catch (DataIntegrityViolationException ex) {
			throw new ControlGroupInUseException(id);
		}
	}

	private ControlGroupEntity findEntity(UUID id) {
		return controlGroupRepository.findById(id).orElseThrow(() -> new ControlGroupNotFoundException(id));
	}

	private ControlGroupEntity save(ControlGroupEntity group) {
		UUID parentGroupId = group.parentGroupId();
		if (parentGroupId != null && !controlGroupRepository.existsById(parentGroupId)) {
			throw new ControlGroupNotFoundException(parentGroupId);
		}
		try {
			return controlGroupRepository.save(group);
		}
		catch (DuplicateKeyException ex) {
			throw new ControlGroupAlreadyExistsException(group.groupRef());
		}
	}

}
