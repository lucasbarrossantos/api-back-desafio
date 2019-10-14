package com.pitang.controller.errors;

import com.pitang.exceptionhandler.CustomExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

public class ErrorsGeneric {

    public ResponseEntity<?> errorFieldsNull(BindingResult result) {
        List<CustomExceptionHandler.Error> errors = new ArrayList<>();
        result.getAllErrors().forEach(error -> errors.add(new CustomExceptionHandler
                .Error(error.getDefaultMessage(), "5")));
        return ResponseEntity.badRequest().body(errors);
    }

}
