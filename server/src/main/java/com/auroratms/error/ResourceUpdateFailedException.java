package com.auroratms.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
public class ResourceUpdateFailedException extends RuntimeException {
    public ResourceUpdateFailedException() {
        super();
    }

    public ResourceUpdateFailedException(String message) {
        super(message);
    }

    public ResourceUpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
