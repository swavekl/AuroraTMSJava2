package com.auroratms.server.errorhandling;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralized place to handle errors in API calls and sending response
 */
@RestControllerAdvice
public class AppControllerAdvice extends ResponseEntityExceptionHandler {

}
