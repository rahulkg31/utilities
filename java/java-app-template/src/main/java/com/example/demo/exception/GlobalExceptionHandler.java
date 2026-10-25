package com.example.demo.exception;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    private static final String ERROR_KEY = "error";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	    HttpHeaders headers, HttpStatusCode status, WebRequest request) {

	String message = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getDefaultMessage())
		.collect(Collectors.joining(", "));

	LOG.error("Validation failed: {}", message);
	return ResponseEntity.status(status).body(Collections.singletonMap(ERROR_KEY, message));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
	    HttpStatusCode statusCode, WebRequest request) {

	LOG.error("{} occurred: {}", ex.getClass().getSimpleName(), ex.getMessage());
	String reason = HttpStatus.valueOf(statusCode.value()).getReasonPhrase();
	return ResponseEntity.status(statusCode).headers(headers).body(Collections.singletonMap(ERROR_KEY, reason));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
	LOG.error("IllegalArgumentException occurred:", ex);

	return new ResponseEntity<>(
		Collections.singletonMap(ERROR_KEY, "Illegal argument exception: " + ex.getMessage()),
		HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ NotFoundException.class })
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
	LOG.error("{} occurred:", ex.getClass().getSimpleName(), ex);

	return new ResponseEntity<>(
		Collections.singletonMap(ERROR_KEY, "Resource not found exception: " + ex.getMessage()),
		HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException ex) {
	LOG.error("IOException occurred:", ex);

	return new ResponseEntity<>(Collections.singletonMap(ERROR_KEY, "I/O error occurred"),
		HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
	LOG.error("RuntimeException occurred:", ex);

	return new ResponseEntity<>(
		Collections.singletonMap(ERROR_KEY, "Unexpected runtime exception: " + ex.getMessage()),
		HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
	LOG.error("Exception occurred:", ex);

	return new ResponseEntity<>(Collections.singletonMap(ERROR_KEY, "Internal server error"),
		HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
