package com.chaion.makkiserver.exception;

import com.chaion.makkiserver.dapps.DAppsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ExceptionResponse handleException(HttpServletRequest request, Exception exception) {
        if (exception instanceof CodedException) {
            logger.warn(exception.getMessage(), exception);
            CodedException codedException = (CodedException) exception;
            return new ExceptionResponse(codedException.getCode(), codedException.getMessage());
        } else {
            logger.error(exception.getMessage(), exception);
            return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
        }
    }
}
