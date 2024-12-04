package com.santander.chk_int.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspectUtils {
  String xTraceId = "x-traceId";
  
  @Before("execution(* com.santander.chk_int.processor..*.*(..)) && args(exchange)")
  public void logBeforeMethodExecution(JoinPoint joinPoint, Exchange exchange) {
    
    String logToShow = """
        
        ################
        # Process started from class: {}
        # Started at: {}
        # trace-id: {}
        # headers: {}
        # body: {}
        # query params: {}
        # properties: {}
        ################""";
    
    log.info(logToShow,
             joinPoint.getSignature().getDeclaringTypeName(),
             new LocalDateTime(),
             exchange.getIn().getHeader(xTraceId),
             exchange.getIn().getHeaders(),
             exchange.getIn().getBody(Object.class),
             exchange.getIn().getHeader(Exchange.HTTP_QUERY),
             exchange.getProperties());
  }

  @AfterReturning(pointcut = "execution(* com.santander.chk_int.processor..*.*(..)) && args(exchange)")
  public void logAfter(JoinPoint joinPoint, Exchange exchange) {

    String logToShow = """
        
        ################
        # Process finished successfully from class: {}
        # Finished at: {}
        # trace-id: {}
        # headers: {}
        # body: {}
        # query params: {}
        # properties: {}
        ################""";
    
    log.info(logToShow,
             joinPoint.getSignature().getDeclaringTypeName(),
             new LocalDateTime(),
             exchange.getIn().getHeader(xTraceId),
             exchange.getIn().getHeaders(),
             exchange.getIn().getBody(Object.class),
             exchange.getIn().getHeader(Exchange.HTTP_QUERY),
             exchange.getProperties());
  }


  @AfterThrowing(pointcut = "execution(* com.santander.chk_int.processor..*.*(..)) && args(exchange)", throwing = "exception")
  public void logThrow(JoinPoint joinPoint, Exchange exchange, Throwable exception) {

    String logToShow = """
        
        ################
        # Process throw exception from class: {}
        # Finished at: {}
        # trace-id: {}
        # headers: {}
        # body: {}
        # query params: {}
        # properties: {}
        # EXCEPTION name: {}
        # EXCEPTION message: {}
        # EXCEPTION line number: {}
        ################""";
    
    log.error(logToShow,
              joinPoint.getSignature().getDeclaringTypeName(),
              new LocalDateTime(),
              exchange.getIn().getHeader(xTraceId),
              exchange.getIn().getHeaders(),
              exchange.getIn().getBody(Object.class),
              exchange.getIn().getHeader(Exchange.HTTP_QUERY),
              exchange.getProperties(),
              exception.getClass().getName(),
              exception.getMessage(),
              exception.getStackTrace()[0].getLineNumber());
  }
}
