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
                // 优先从 request 属性中获取（由拦截器设置）
                String operator = (String) request.getAttribute("operator");
                if (operator != null && !operator.isEmpty() && !"unknown".equals(operator)) {
                    return operator;
                }
                // 从请求头获取
                operator = request.getHeader("Operator");
                if (operator != null && !operator.isEmpty()) {
                    return operator;
                }
                // 从请求参数获取
                operator = request.getParameter("operator");
                if (operator != null && !operator.isEmpty()) {
                    return operator;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "system";
    }

    // 拦截所有 Service 层的 save、updateById、deleteById 方法
    @Pointcut("execution(* com.auggie.student_server.service.*.save(..)) || " +
              "execution(* com.auggie.student_server.service.*.updateById(..)) || " +
              "execution(* com.auggie.student_server.service.*.deleteById(..)) || " +
              "execution(* com.auggie.student_server.service.*.insert(..)) || " +
              "execution(* com.auggie.student_server.service.*.batchInsert(..))")
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
            if (methodName.contains("save") || methodName.contains("insert")) {
                operationType = "INSERT";
            } else if (methodName.contains("update")) {
                operationType = "UPDATE";
            } else if (methodName.contains("delete")) {
                operationType = "DELETE";
            }

            // 确定目标表（从 Service 类名推断）
            String targetTable = className.replace("Service", "").toLowerCase();
            if (targetTable.contains("student")) {
                targetTable = "s";
            } else if (targetTable.contains("course")) {
                targetTable = "c";
            } else if (targetTable.contains("teacher")) {
                targetTable = "t";
            } else if (targetTable.contains("department")) {
                targetTable = "department";
            } else if (targetTable.contains("major")) {
                targetTable = "major";
            } else if (targetTable.contains("class")) {
                targetTable = "class";
            } else if (targetTable.contains("wordpaper")) {
                targetTable = "word_papers";
            }

            // 获取目标ID
            Long targetId = null;
            if (args.length > 0) {
                Object arg = args[0];
                if (arg != null) {
                    // 尝试从对象中获取ID
                    try {
                        Method getIdMethod = arg.getClass().getMethod("getSid");
                        targetId = ((Integer) getIdMethod.invoke(arg)).longValue();
                    } catch (Exception e) {
                        try {
                            Method getIdMethod = arg.getClass().getMethod("getCid");
                            targetId = ((Integer) getIdMethod.invoke(arg)).longValue();
                        } catch (Exception e2) {
                            try {
                                Method getIdMethod = arg.getClass().getMethod("getTid");
                                targetId = ((Integer) getIdMethod.invoke(arg)).longValue();
                            } catch (Exception e3) {
                                try {
                                    Method getIdMethod = arg.getClass().getMethod("getId");
                                    Object idObj = getIdMethod.invoke(arg);
                                    if (idObj instanceof Long) {
                                        targetId = (Long) idObj;
                                    } else if (idObj instanceof Integer) {
                                        targetId = ((Integer) idObj).longValue();
                                    }
                                } catch (Exception e4) {
                                    // 如果第一个参数是 Integer（deleteById 的情况）
                                    if (arg instanceof Integer) {
                                        targetId = ((Integer) arg).longValue();
                                    } else if (arg instanceof Long) {
                                        targetId = (Long) arg;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 构建内容
            String content = String.format("执行 %s 操作，方法：%s.%s，参数：%s", 
                    operationType, className, methodName, Arrays.toString(args));

            // 记录日志（从请求中获取操作者）
            String operator = getCurrentOperator();
            operationLogService.recordOperation(operator, operationType, targetTable, targetId, content);
        } catch (Exception e) {
            // 记录日志失败不应该影响业务逻辑
            e.printStackTrace();
        }
    }
}
