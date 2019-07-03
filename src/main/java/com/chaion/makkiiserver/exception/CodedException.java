package com.chaion.makkiiserver.exception;

public class CodedException extends RuntimeException {
    private int code;
    public CodedException(int code, String message) {
        super(message);
        this.code = code;
    }
    public CodedException(CodedErrorEnum codedError) {
        super(codedError.getMsg());
        this.code = codedError.getCode();
    }
    public CodedException(CodedErrorEnum codedError, String message) {
        super(message);
        this.code = codedError.getCode();
    }

    public int getCode() {
        return code;
    }
}
