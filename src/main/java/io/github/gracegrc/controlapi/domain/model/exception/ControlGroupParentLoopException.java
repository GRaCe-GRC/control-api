package io.github.gracegrc.controlapi.domain.model.exception;

import java.util.UUID;

public class ControlGroupParentLoopException extends RuntimeException {

	public ControlGroupParentLoopException(UUID id) {
		super("Control group " + id + " cannot be moved under itself or one of its child groups");
	}

}
