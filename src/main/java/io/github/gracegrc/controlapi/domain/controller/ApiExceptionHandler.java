package io.github.gracegrc.controlapi.domain.controller;

import io.github.gracegrc.controlapi.domain.model.exception.ControlAlreadyExistsException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupAlreadyExistsException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupNotFoundException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlNotFoundException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ ControlGroupNotFoundException.class, ControlNotFoundException.class })
	public ProblemDetail handleNotFound(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({ ControlGroupAlreadyExistsException.class, ControlAlreadyExistsException.class })
	public ProblemDetail handleAlreadyExists(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler({ ControlGroupInUseException.class, ControlInUseException.class })
	public ProblemDetail handleInUse(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ex.getBody();
		Map<String, String> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.collect(Collectors.toMap(FieldError::getField, error -> String.valueOf(error.getDefaultMessage()),
					(first, second) -> first));
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

}
