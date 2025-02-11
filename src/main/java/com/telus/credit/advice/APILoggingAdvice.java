package com.telus.credit.advice;

import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

//comment out opencensus.trace.Tracing as it frequently fails
//import io.opencensus.trace.Tracer;
//import io.opencensus.trace.Tracing;

@Aspect
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class APILoggingAdvice {
    private static Logger logger = LoggerFactory.getLogger(APILoggingAdvice.class);
    private static final String LOG_MESSAGE_FORMAT = "%s.%s execution time: %dms";
    private static final String LOG_TRACE_FORMAT = ".%s.%s call";
    
    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    
   // private static final Tracer tracer = Tracing.getTracer();

    @Before("execution(* com.telus.credit.controllers.*.*(..))")
    public void beforeAdviceForControllerLayer(JoinPoint jp) {
        logEntry(jp);
    }

    @After("execution(* com.telus.credit.controllers.*.*(..))")
    public void afterAdviceForControllerLayer(JoinPoint jp) {
        logExit(jp);
    }

    @Pointcut("execution(* com.telus.credit.controllers.*.*(..))")
    public void restPointCut() {
        logger.debug("Rest Layer pointcut for elapsed time");
    }

    @Around("restPointCut()")
    public Object timeProfileRestLayer(ProceedingJoinPoint pjp) throws Throwable {
        return calculateElapsedTime(pjp);
    }

    
    
    @Pointcut("execution(* com.telus.credit.firestore.*.*(..))")
    public void fireStorePointCut() {
        logger.debug("Firestore pointcut for elapsed time");
    }

    @Around("fireStorePointCut()")
    public Object timeProfileFireStoreLayer(ProceedingJoinPoint pjp) throws Throwable {
        return calculateElapsedTime(pjp);
    }
    
   @Pointcut("execution(* com.telus.credit.xconv.*.*.*(..))")
    public void xConvPointCut() {
        logger.debug("Xconv pointcut for elapsed time");
    }

    @Around("xConvPointCut()")
    public Object xConvMigLayer(ProceedingJoinPoint pjp) throws Throwable {
        return calculateElapsedTime(pjp);
    }
    

    @Pointcut("execution(* com.telus.credit.pubsub.service.*.*(..))")
    public void migStorePointCut() {
        logger.debug("Pubsub pointcut for elapsed time");
    }

    @Around("migStorePointCut()")
    public Object timeProfileMigLayer(ProceedingJoinPoint pjp) throws Throwable {
        return calculateElapsedTime(pjp);
    }

    
    @Before("execution(* com.telus.credit.firestore.*.*(..))")
    public void beforeAdviceForFirestoreLayer(JoinPoint jp) {
        logEntry(jp);
    }
    @After("execution(* com.telus.credit.firestore.*.*(..))")
    public void afterAdviceForFirestoreLayer(JoinPoint jp) {
        logExit(jp);
    }

    
    @Before("execution(* com.telus.credit.service.impl.*.*(..))")
    public void beforeAdviceForServiceLayer(JoinPoint jp) {
        logEntry(jp);
    }
    @After("execution(* com.telus.credit.service.impl.*.*(..))")
    public void afterAdviceForServiceLayer(JoinPoint jp) {
        logExit(jp);
    }
    @Pointcut("execution(* com.telus.credit.service.impl.*.*(..))")
    public void serviceImplPointCut() {
        logger.debug("serviceImpl pointcut for elapsed time");
    }

    @Around("serviceImplPointCut()")
    public Object timeServiceImplLayer(ProceedingJoinPoint pjp) throws Throwable {
        return calculateElapsedTime(pjp);
    }
    
    
    
    @Before("execution(* com.telus.credit.dao.*.*(..))")
    public void beforeAdviceForDAOLayer(JoinPoint jp) {
        logEntry(jp);
    }
    @After("execution(* com.telus.credit.dao.*.*(..))")
    public void afterAdviceForDAOLayer(JoinPoint jp) {
        logExit(jp);
    }
    

    @Pointcut("execution(* com.telus.credit.dao.*.*(..))")
    public void daoPointCut() {
        logger.debug("dao pointcut for elapsed time");
    }

    @Around("daoPointCut()")
    public Object timeDaoLayer(ProceedingJoinPoint pjp) throws Throwable {
        return calculateElapsedTime(pjp);
    }
    
    /**
     * @param pjp
     * @return
     * @throws Throwable
     */
    private Object calculateElapsedTime(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object retVal = null;

        retVal = pjp.proceed();

        stopWatch.stop();
        logExecutionTime(pjp, stopWatch);
        return retVal;
    }

    private void logExecutionTime(ProceedingJoinPoint joinPoint, StopWatch stopWatch) {
        String logMessage = String.format(LOG_MESSAGE_FORMAT, joinPoint.getTarget().getClass().getSimpleName(),
            joinPoint.getSignature().getName(), stopWatch.getTime());
        logger.debug(logMessage);
    }

    private void logEntry(JoinPoint joinPoint) {
        String logMessage = String.format(
					        		LOG_TRACE_FORMAT
					        		, joinPoint.getTarget().getClass().getSimpleName()
					        		,joinPoint.getSignature().getName()
					        		);
        try {
			//tracer.spanBuilder(activeProfile + logMessage).setSampler(Samplers.alwaysSample()).startScopedSpan();
			//tracer.spanBuilder(activeProfile + logMessage).startScopedSpan();
		} catch (Throwable e) {
			logger.warn("tracer.spanBuilder failed." + logMessage, e);
		}
        logger.trace("trace start:{} {}",activeProfile, logMessage);
        logger.info("start:{}", logMessage);
    }

    private void logExit(JoinPoint joinPoint) {
        String logMessage = String.format(LOG_TRACE_FORMAT, joinPoint.getTarget().getClass().getSimpleName(),
            joinPoint.getSignature().getName());
        //tracer.getCurrentSpan().end();
        logger.trace("trace end:{} {}",activeProfile, logMessage);
        logger.info("end:{}", logMessage);
    }
}
