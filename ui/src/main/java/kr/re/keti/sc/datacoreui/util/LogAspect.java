package kr.re.keti.sc.datacoreui.util;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Utility for log aspect
 * @FileName LogAspect.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@Component
@Aspect
public class LogAspect {

    Logger logger = LoggerFactory.getLogger(LogAspect.class);

    /**
     * Get log with execute time.
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(kr.re.keti.sc.datacoreui.util.LogExecutionTime)")
    public Object logExecuceTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object proceed = proceedingJoinPoint.proceed();

        stopWatch.stop();

        logger.info("Running time : " + stopWatch.getTotalTimeSeconds() + " s");

        return proceed;
    }


}
