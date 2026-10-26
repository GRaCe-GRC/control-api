package io.github.gracegrc.controlapi.domain.model.exception;

public class ControlGroupAlreadyExistsException extends RuntimeException {

	public ControlGroupAlreadyExistsException(String groupRef) {
		super("A control group with ref '" + groupRef + "' already exists");
	}

}
