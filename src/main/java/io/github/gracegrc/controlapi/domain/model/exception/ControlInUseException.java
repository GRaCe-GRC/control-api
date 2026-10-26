package io.github.gracegrc.controlapi.domain.model.exception;

import java.util.UUID;

public class ControlInUseException extends RuntimeException {

	public ControlInUseException(UUID id) {
		super("Control " + id + " still has enhancements");
	}

}
