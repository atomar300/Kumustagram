package com.ashish.kumustagram.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;



@Slf4j
@ControllerAdvice
public class APIExceptionHandler {




    // 404
    @ExceptionHandler
    public ResponseEntity<APIError> usernameNotFoundExceptionHandler(UserNotFoundException ex) {

        return new ResponseEntity<>(
                new APIError(ex.getLocalizedMessage()), HttpStatus.NOT_FOUND);
    }



    // 404
    @ExceptionHandler
    public ResponseEntity<APIError> productNotFoundExceptionHandler(PostNotFoundException ex) {

        return new ResponseEntity<>(
                new APIError(ex.getLocalizedMessage()), HttpStatus.NOT_FOUND);
    }



    // 403
    // Access token doesn't include or imply any permission or scope that allows the client to perform the desired action.
    @ExceptionHandler
    public ResponseEntity<APIError> accessDeniedExceptionHandler(AccessDeniedException ex) {

        return new ResponseEntity<>(
                new APIError(ex.getLocalizedMessage()), HttpStatus.UNAUTHORIZED);
    }

    // 401 for bad credentials
    // An access token is missing, access token is expired, revoked, malformed, or invalid for other reasons.
    @ExceptionHandler
    public ResponseEntity<APIError> badCredentialsExceptionHandler(BadCredentialsException ex) {

        return new ResponseEntity<>(
                new APIError(ex.getLocalizedMessage()), HttpStatus.UNAUTHORIZED);
    }



    @ExceptionHandler
    public ResponseEntity<APIError> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException  ex) {

        FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);

        return new ResponseEntity<>(
                new APIError(fieldError.getField() + ": " + fieldError.getDefaultMessage()), HttpStatus.BAD_REQUEST);
    }



    // 500
    // Problem in the server-side
    @ExceptionHandler
    public ResponseEntity<APIError> internalServerErrorHandler(InternalServerError ex) {

        return new ResponseEntity<>(
                new APIError(ex.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler
    public ResponseEntity<APIError> missingRequestCookieExceptionHandler(MissingRequestCookieException ex){
        return new ResponseEntity<>(
                new APIError("Login to access this resource"), HttpStatus.FORBIDDEN);
    }



    // 400
    // Also handles validation failed exceptions
    // Problem cause by the client-side
    // The HyperText Transfer Protocol (HTTP) 400 Bad Request response status code indicates that the server cannot or will not process the request due to something that is perceived to be a client error
    @ExceptionHandler
    public ResponseEntity<APIError> exceptionHandler(Exception ex) {

        return new ResponseEntity<>(
                new APIError(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

}