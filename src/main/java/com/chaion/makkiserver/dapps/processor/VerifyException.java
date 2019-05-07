package com.chaion.makkiserver.dapps.processor;

import com.chaion.makkiserver.exception.CodedErrorEnum;
import com.chaion.makkiserver.exception.CodedException;

public class VerifyException extends CodedException {
    public VerifyException(int code, String message) {
        super(code, message);
    }

    public VerifyException(CodedErrorEnum codedError) {
        super(codedError);
    }

    public VerifyException(CodedErrorEnum codedError, String message) {
        super(codedError, message);
    }
}
