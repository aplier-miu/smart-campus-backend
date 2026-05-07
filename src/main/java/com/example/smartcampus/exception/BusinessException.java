package com.example.smartcampus.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.BAD_REQUEST;
    }

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code == null ? ErrorCode.BAD_REQUEST : code;
    }
}