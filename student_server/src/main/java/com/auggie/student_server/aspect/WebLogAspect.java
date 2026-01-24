package com.auggie.student_server.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Web 请求日志切面
 */
@Aspect
@Component
public class WebLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(public * com.auggie.student_server.controller..*.*(..))")
    public void webLog() {}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logger.info("========================================== Start ==========================================");
                logger.info("URL            : {}", request.getRequestURL().toString());
                logger.info("HTTP Method    : {}", request.getMethod());
                logger.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
                logger.info("IP             : {}", request.getRemoteAddr());
                logger.info("Request Args   : {}", Arrays.toString(joinPoint.getArgs()));
            }
        } catch (Exception e) {
            logger.error("Log Before Error: ", e);
        }
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) {
        try {
            logger.info("Response Result : {}", ret != null ? ret.toString() : "null");
            logger.info("=========================================== End ===========================================");
        } catch (Exception e) {
            logger.error("Log After Error: ", e);
        }
    }
}
