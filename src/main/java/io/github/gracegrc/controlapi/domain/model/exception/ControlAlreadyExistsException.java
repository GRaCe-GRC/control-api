package io.github.gracegrc.controlapi.domain.model.exception;

public class ControlAlreadyExistsException extends RuntimeException {

	public ControlAlreadyExistsException(String controlRef) {
		super("A control with ref '" + controlRef + "' already exists");
	}

}
