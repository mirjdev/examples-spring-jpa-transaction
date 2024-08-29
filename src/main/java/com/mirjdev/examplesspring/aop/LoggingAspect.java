package com.mirjdev.examplesspring.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Астекты для логирования времени выполнения и дэбага
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(public * *(..)) && within(com.mirjdev.examplesspring..*)")
    public void adsServicePointcut() {
    }

    @Pointcut("execution(* com.mirjdev.examplesspring.controller.*.*(..))")
    public void adsExceptionPointcut() {
    }

    @Around("adsServicePointcut()")
    public Object logMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        final StopWatch stopWatch = new StopWatch();
        //calculate method execution time
        stopWatch.start();
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();
        //Log method execution time
        log.info("Execution time of " + methodSignature.getDeclaringType().getName() // Class Name
                + "." + methodSignature.getName() + " " // Method Name
                + ":: " + stopWatch.getTotalTimeMillis() + " ms");
        return result;
    }

    @Around("adsServicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Method name = {}.{}() :: input  params = {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
        Object result = joinPoint.proceed();
        log.debug("Method output data::{}.{}() with result = {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                result);
        return result;
    }

    @AfterThrowing(value = "adsExceptionPointcut()", throwing = "ex")
    public void logExceptions(JoinPoint joinPoint, Exception ex) {

        log.error("Error message :: {}:: {} in {}.{}()",
                ex.getMessage(),
                Arrays.toString(joinPoint.getArgs()),
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
    }

    @Around(value = "@annotation(com.mirjdev.examplesspring.aop.TransactionMonitoring)")
    public Object transactionMonitoring(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        TransactionMonitoring transactionMonitoring = method.getAnnotation(TransactionMonitoring.class);
        Level level = transactionMonitoring.level();
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        boolean newTransaction = TransactionInterceptor.currentTransactionStatus().isNewTransaction();
        String path = joinPoint.getSignature().getDeclaringType() + "." + joinPoint.getSignature().getName();
        String logString = String.format("===== Transaction currentTransactionName: %s, isNewTransaction: %s, method path: %s =====", currentTransactionName, newTransaction, path);
        switch (level) {
            case TRACE:
                log.trace(logString);
                break;
            case DEBUG:
                log.debug(logString);
                break;
            case INFO:
                log.info(logString);
                break;
            case WARN:
                log.warn(logString);
                break;
            case ERROR:
                log.error(logString);
                break;
        }

        return joinPoint.proceed();
    }
}
