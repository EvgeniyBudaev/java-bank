package com.accountsservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAdvice {

    @Around("@annotation(com.aggregation.aspect.LogMethodExecutionTime)")
    public Object logMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Method [{}] requested with arguments:\n{}", joinPoint.getSignature().getName(),
                joinPoint.getArgs());
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        log.info("Method [{}] execution time: {}ms", joinPoint.getSignature().getName(),
                endTime - startTime);
        return result;
    }

}