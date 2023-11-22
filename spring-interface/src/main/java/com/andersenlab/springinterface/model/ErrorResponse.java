package com.andersenlab.springinterface.model;

import org.springframework.http.HttpStatus;

public record ErrorResponse(HttpStatus status, String exception) {
}
