package ar.edu.um.limitadorum.exception;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex,
			HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNAUTHORIZED,
				"Error de usuario y/o de password."
		);
		problem.setTitle("Unauthorized");
		problem.setType(URI.create("https://limitador.edu.ar/problems/invalid-credentials"));
		if (request != null && request.getRequestURI() != null) {
			problem.setInstance(URI.create(request.getRequestURI()));
		}
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problem);
	}

	@ExceptionHandler(DuplicateUserException.class)
	public ProblemDetail handleDuplicateUser(DuplicateUserException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Conflict");
		problem.setType(URI.create("https://limitador.edu.ar/problems/duplicate-user"));
		problem.setProperty("field", ex.getField());
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Validation failed for one or more fields");
		problem.setTitle("Bad Request");
		problem.setType(URI.create("https://limitador.edu.ar/problems/validation-error"));
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		problem.setProperty("errors", errors);
		return problem;
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"The request conflicts with the current state of the resource");
		problem.setTitle("Conflict");
		problem.setType(URI.create("https://limitador.edu.ar/problems/data-integrity"));
		return problem;
	}
}
