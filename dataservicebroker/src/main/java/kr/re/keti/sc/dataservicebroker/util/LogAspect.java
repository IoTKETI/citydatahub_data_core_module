package kr.re.keti.sc.dataservicebroker.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @Around("@annotation(kr.re.keti.sc.dataservicebroker.util.LogExecutionTime)")
    public Object logExecuceTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object proceed = proceedingJoinPoint.proceed();

        stopWatch.stop();

        log.info("Running time : " + stopWatch.getTotalTimeSeconds() + " s");

        return proceed;
    }


}
