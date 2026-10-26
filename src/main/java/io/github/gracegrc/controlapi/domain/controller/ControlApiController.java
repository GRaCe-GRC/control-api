package io.github.gracegrc.controlapi.domain.controller;

import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.service.ControlGroupService;
import io.github.gracegrc.controlapi.domain.service.ControlService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
public class ControlApiController {

	private final ControlGroupService controlGroupService;

	private final ControlService controlService;

	public ControlApiController(ControlGroupService controlGroupService, ControlService controlService) {
		this.controlGroupService = controlGroupService;
		this.controlService = controlService;
	}

	@PostMapping("/control-groups")
	public ResponseEntity<Void> createGroup(@Valid @RequestBody ControlGroupRequestDto request) {
		UUID id = controlGroupService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@GetMapping("/control-groups")
	public List<ControlGroupResponseDto> findGroups(@RequestParam(required = false) UUID parentId) {
		return controlGroupService.findAll(parentId);
	}

	@GetMapping("/control-groups/{id}")
	public ControlGroupResponseDto findGroupById(@PathVariable UUID id) {
		return controlGroupService.findById(id);
	}

	@PutMapping("/control-groups/{id}")
	public ControlGroupResponseDto updateGroup(@PathVariable UUID id,
			@Valid @RequestBody ControlGroupUpdateRequestDto request) {
		return controlGroupService.update(id, request);
	}

	@DeleteMapping("/control-groups/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteGroup(@PathVariable UUID id) {
		controlGroupService.delete(id);
	}

	@PostMapping("/controls")
	public ResponseEntity<Void> createControl(@Valid @RequestBody ControlRequestDto request) {
		UUID id = controlService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@GetMapping("/controls")
	public List<ControlResponseDto> findControls(@RequestParam(required = false) UUID groupId,
			@RequestParam(required = false) UUID parentId) {
		return controlService.findAll(groupId, parentId);
	}

	@GetMapping("/controls/{id}")
	public ControlResponseDto findControlById(@PathVariable UUID id) {
		return controlService.findById(id);
	}

	@PutMapping("/controls/{id}")
	public ControlResponseDto updateControl(@PathVariable UUID id,
			@Valid @RequestBody ControlUpdateRequestDto request) {
		return controlService.update(id, request);
	}

	@DeleteMapping("/controls/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteControl(@PathVariable UUID id) {
		controlService.delete(id);
	}

}
