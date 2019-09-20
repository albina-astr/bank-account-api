package com.gimaletdinova.rest.exception;

/**
 * Created by agimaletdinova on 14.09.2019.
 */
public class ApiException extends RuntimeException {

    private String message;

    public ApiException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
