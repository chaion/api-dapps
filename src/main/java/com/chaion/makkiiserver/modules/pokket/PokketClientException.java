package com.chaion.makkiiserver.modules.pokket;

public class PokketClientException extends Exception {

    private Integer statusCode;

    private String message;

    public PokketClientException(String message) {
        super(message);
    }

    public PokketClientException(String message, Integer code) {
        this.statusCode = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        if (this.statusCode != null) {
            return this.statusCode + ":" + this.message;
        }
        return this.message;
    }
}
