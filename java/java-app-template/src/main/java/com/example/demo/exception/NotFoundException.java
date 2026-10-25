package com.example.demo.exception;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = -3026499731507132837L;

	public NotFoundException() {
		super("Resource not found");
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}
}
