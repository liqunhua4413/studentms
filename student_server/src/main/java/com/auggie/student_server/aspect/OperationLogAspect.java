package com.auggie.student_server.aspect;

import com.auggie.student_server.service.OperationLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: OperationLogAspect - 操作日志切面
 * @Version 1.0.0
 */

@Aspect
@Component
public class OperationLogAspect {
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 获取当前操作者
     */
    private String getCurrentOperator() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String operator = (String) request.getAttribute("operator");
                if (operator != null && !operator.isEmpty() && !"unknown".equals(operator)) {
                    return operator;
                }
                operator = request.getHeader("Operator");
                if (operator != null && !operator.isEmpty()) {
                    return operator;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "system";
    }

    // 拦截所有重要的 Service 层方法
    @Pointcut("execution(* com.auggie.student_server.service.*.save(..)) || " +
              "execution(* com.auggie.student_server.service.*.update*(..)) || " +
              "execution(* com.auggie.student_server.service.*.delete*(..)) || " +
              "execution(* com.auggie.student_server.service.*.insert*(..)) || " +
              "execution(* com.auggie.student_server.service.*.batchInsert(..)) || " +
              "execution(* com.auggie.student_server.service.*.upload*(..)) || " +
              "execution(* com.auggie.student_server.service.*.clearTestData(..)) || " +
              "execution(* com.auggie.student_server.service.*.insertInitialData(..))")
    public void operationPointcut() {
    }

    @AfterReturning(pointcut = "operationPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            Object[] args = joinPoint.getArgs();

            // 确定操作类型
            String operationType = "UNKNOWN";
            if (methodName.contains("save") || methodName.contains("insert") || methodName.contains("upload")) {
                operationType = "INSERT";
            } else if (methodName.contains("update")) {
                operationType = "UPDATE";
            } else if (methodName.contains("delete") || methodName.contains("clear")) {
                operationType = "DELETE";
            }

            // 确定目标表
            String targetTable = className.replace("Service", "").toLowerCase();
            if (targetTable.contains("student")) {
                targetTable = "student";
            } else if (targetTable.contains("course")) {
                targetTable = "course";
            } else if (targetTable.contains("teacher")) {
                targetTable = "teacher";
            } else if (targetTable.contains("wordpaper") || targetTable.contains("paper")) {
                targetTable = "exam_paper_analysis";
            } else if (targetTable.contains("init")) {
                targetTable = "all_tables";
            }

            // 获取目标ID
            Long targetId = null;
            if (args.length > 0) {
                targetId = extractId(args[0]);
            }
            if (targetId == null && result != null) {
                targetId = extractId(result);
            }

            // 构建内容
            String content = String.format("执行 %s 操作，方法：%s.%s", operationType, className, methodName);
            if (methodName.contains("upload")) {
                content += "，文件名: " + (result != null ? result.toString() : "未知");
            }

            // 记录日志
            String operator = getCurrentOperator();
            operationLogService.recordOperation(operator, operationType, targetTable, targetId, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long extractId(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Long) return (Long) obj;
        
        try {
            // 尝试多个可能的 ID 方法名
            String[] methodNames = {"getId", "getSid", "getTid", "getCid"};
            for (String mName : methodNames) {
                try {
                    Method method = obj.getClass().getMethod(mName);
                    Object idObj = method.invoke(obj);
                    if (idObj instanceof Long) return (Long) idObj;
                    if (idObj instanceof Integer) return ((Integer) idObj).longValue();
                } catch (NoSuchMethodException e) {
                    // 忽略，尝试下一个
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return null;
    }
}
