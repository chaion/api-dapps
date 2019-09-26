package com.chaion.makkiiserver.modules.pokket;

public class PokketServiceException extends Exception {

    /**
     * this status code represents http response status code from pokket server.
     */
    private Integer statusCode;

    private String message;

    public PokketServiceException(String message) {
        super(message);
    }

    public PokketServiceException(String message, Integer code) {
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
