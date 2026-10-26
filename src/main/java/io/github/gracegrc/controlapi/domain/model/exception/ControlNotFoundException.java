package io.github.gracegrc.controlapi.domain.model.exception;

import java.util.UUID;

public class ControlNotFoundException extends RuntimeException {

	public ControlNotFoundException(UUID id) {
		super("Control " + id + " not found");
	}

}
