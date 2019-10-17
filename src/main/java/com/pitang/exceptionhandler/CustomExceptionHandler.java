package com.pitang.exceptionhandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @Autowired
    public CustomExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        super.handleHttpMessageNotReadable(ex, headers, status, request);
        String customMessageUser = messageSource
                .getMessage("handleInvalidFields", null, LocaleContextHolder.getLocale());
        List<Error> errors = Collections.singletonList(new Error(customMessageUser, "4"));
        return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }



    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {

        List<Error> errors = createErrorList(ex.getBindingResult());
        return handleExceptionInternal(ex,
                errors,
                headers,
                HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler({EmptyResultDataAccessException.class})
    public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException tx,
                                                                       WebRequest request) {
        String customMessageUser = messageSource
                .getMessage("resource.not-found", null, LocaleContextHolder.getLocale());
        String developerMessage = tx.toString();
        List<Error> errors = Collections.singletonList(new Error(customMessageUser, developerMessage));
        return handleExceptionInternal(tx, errors, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }



    private List<Error> createErrorList(BindingResult result) {
        List<Error> errors = new ArrayList<>();
        for (FieldError error : result.getFieldErrors()) {
            String customMessageUser = messageSource.getMessage(error, LocaleContextHolder.getLocale());
            String developerMessage = error.toString();
            errors.add(new Error(customMessageUser, developerMessage));
        }
        return errors;
    }

    public static class Error {
        private String message;
        private String errorCode;

        public Error(String message, String errorCode) {
            this.message = message;
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }
    }

}