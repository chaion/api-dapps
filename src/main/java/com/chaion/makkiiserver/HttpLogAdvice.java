package com.chaion.makkiiserver;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class HttpLogAdvice {
    private static final Logger logger = LoggerFactory.getLogger("HttpRequest");

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)||"
            + "@annotation(org.springframework.web.bind.annotation.GetMapping)||"
            + "@annotation(org.springframework.web.bind.annotation.PostMapping)||"
            + "@annotation(org.springframework.web.bind.annotation.PutMapping)||"
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object args[] = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        StringBuilder argsString = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            argsString.append(args[i]);
            if (i != args.length) argsString.append(";");
        }
        logger.info(method.getDeclaringClass().getName() + "." + method.getName() + " : " + argsString);
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            logger.error("method:" + method.getDeclaringClass().getName() + "." + method.getName()
                    + "\r\n args:" + argsString.toString()
                    + "\r\n throw: " + throwable.getMessage());
            logger.error("Exception occurs in controller: ", throwable);
            throw throwable;
        }
    }
}
