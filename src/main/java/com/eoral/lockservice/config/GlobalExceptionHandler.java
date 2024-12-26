package com.eoral.lockservice.config;

import com.eoral.lockservice.exception.*;
import com.eoral.lockservice.model.ErrorResponse;
import com.eoral.lockservice.service.UniqueIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final UniqueIdGenerator uniqueIdGenerator;

    public GlobalExceptionHandler(UniqueIdGenerator uniqueIdGenerator) {
        this.uniqueIdGenerator = uniqueIdGenerator;
    }

    @ExceptionHandler(CannotReleaseLockAcquiredByAnotherProcessException.class)
    protected ResponseEntity<Object> handle(CannotReleaseLockAcquiredByAnotherProcessException ex) {
        return generateResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    protected ResponseEntity<Object> handle(TokenExpiredException ex) {
        return generateResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(TokenRequiredException.class)
    protected ResponseEntity<Object> handle(TokenRequiredException ex) {
        return generateResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<Object> handle(InvalidTokenException ex) {
        return generateResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(LockAcquiredByAnotherProcessException.class)
    protected ResponseEntity<Object> handle(LockAcquiredByAnotherProcessException ex) {
        return generateResponseEntity(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LockNameRequiredException.class)
    protected ResponseEntity<Object> handle(LockNameRequiredException ex) {
        return generateResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handle(Exception ex) {
        String logId = uniqueIdGenerator.generate();
        String message = "Unknown exception: " + logId;
        logger.error(message, ex);
        return generateResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private ResponseEntity<Object> generateResponseEntity(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(status.value(), message);
        return ResponseEntity.status(status).body(body);
    }
}
