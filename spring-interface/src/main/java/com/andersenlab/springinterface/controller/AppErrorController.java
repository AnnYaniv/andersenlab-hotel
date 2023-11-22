package com.andersenlab.springinterface.controller;

import com.andersenlab.springinterface.model.ErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleCustomParameterConstraintExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(
                new ErrorResponse(status, e.getMessage()),
                status
        );
    }
}
