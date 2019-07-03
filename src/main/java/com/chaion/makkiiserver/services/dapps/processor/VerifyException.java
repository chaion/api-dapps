package com.chaion.makkiiserver.services.dapps.processor;

import com.chaion.makkiiserver.exception.CodedErrorEnum;
import com.chaion.makkiiserver.exception.CodedException;

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
