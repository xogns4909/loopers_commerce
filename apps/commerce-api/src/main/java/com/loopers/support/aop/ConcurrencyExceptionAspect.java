package com.loopers.support.aop;


import com.loopers.support.annotation.HandleConcurrency;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.LockTimeoutException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ConcurrencyExceptionAspect {

    @Around("@annotation(handleConcurrency)")
    public Object handleConcurrency(ProceedingJoinPoint joinPoint, HandleConcurrency handleConcurrency) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (OptimisticLockingFailureException |
                 PessimisticLockingFailureException |
                 LockAcquisitionException |
                 LockTimeoutException e) {
            throw new CoreException(ErrorType.CONFLICT,handleConcurrency.message());
        }
    }
}

