package com.personal.lld.domain.exception;

public class InvalidATMOperationException extends RuntimeException {

    public InvalidATMOperationException(String message) {
        super(message);
    }
}
