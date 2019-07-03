package com.chaion.makkiiserver;

public class ServiceException extends Exception {
    private int code;
    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
