package io.github.gracegrc.controlapi.domain.model.exception;

import java.util.UUID;

public class ControlGroupNotFoundException extends RuntimeException {

	public ControlGroupNotFoundException(UUID id) {
		super("Control group " + id + " not found");
	}

}
