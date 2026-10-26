package io.github.gracegrc.controlapi.domain.model.exception;

import java.util.UUID;

public class ControlGroupInUseException extends RuntimeException {

	public ControlGroupInUseException(UUID id) {
		super("Control group " + id + " still has child groups or controls");
	}

}
