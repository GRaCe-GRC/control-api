package io.github.gracegrc.controlapi.domain.model.exception;

import java.util.UUID;

public class ControlParentLoopException extends RuntimeException {

	public ControlParentLoopException(UUID id) {
		super("Control " + id + " cannot be an enhancement of itself or of one of its enhancements");
	}

}
