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
                    // 如果是admin账户，返回"系统管理员"
                    if ("admin".equals(operator) || "系统管理员".equals(operator)) {
                        return "系统管理员";
                    }
                    // 尝试URL解码
                    try {
                        operator = java.net.URLDecoder.decode(operator, "UTF-8");
                    } catch (Exception e) {
                        // 忽略解码错误
                    }
                    return operator;
                }
                operator = request.getHeader("Operator");
                if (operator != null && !operator.isEmpty()) {
                    try {
                        operator = java.net.URLDecoder.decode(operator, "UTF-8");
                    } catch (Exception e) {
                        // 忽略解码错误
                    }
                    // 如果是admin账户，返回"系统管理员"
                    if ("admin".equals(operator) || "系统管理员".equals(operator)) {
                        return "系统管理员";
                    }
                    return operator;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "system";
    }

    /** admin / 系统管理员 统一显示为「系统管理员」 */
    private String normalizeOperator(String operator) {
        if (operator == null || operator.isEmpty()) {
            return operator;
        }
        if ("system".equals(operator) || "unknown".equals(operator)) {
            return operator;
        }
        if ("admin".equals(operator) || "系统管理员".equals(operator)) {
            return "系统管理员";
        }
        return operator;
    }

    // 拦截所有重要的 Service 层方法
    @Pointcut("execution(* com.auggie.student_server.service.*.save(..)) || " +
              "execution(* com.auggie.student_server.service.*.update*(..)) || " +
              "execution(* com.auggie.student_server.service.*.delete*(..)) || " +
              "execution(* com.auggie.student_server.service.*.insert*(..)) || " +
              "execution(* com.auggie.student_server.service.*.batchInsert(..)) || " +
              "execution(* com.auggie.student_server.service.*.upload*(..)) || " +
              "execution(* com.auggie.student_server.service.*.importFromExcel(..)) || " +
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

            // 确定操作类型（必须明确，不能是"未知"）
            String operationType;
            if (methodName.contains("importFromExcel") || methodName.contains("import")) {
                operationType = "批量导入";
            } else if (methodName.contains("save") || methodName.contains("insert")) {
                operationType = "新增";
            } else if (methodName.contains("update")) {
                operationType = "编辑";
            } else if (methodName.contains("delete") || methodName.contains("clear")) {
                operationType = "删除";
            } else if (methodName.contains("upload")) {
                operationType = "上传";
            } else {
                operationType = "其他";
            }

            // 确定目标表和模块（必须明确，不能是"未知模块"）
            String targetTable = className.replace("Service", "").toLowerCase();
            String targetModuleName;
            // 优先匹配更具体的类名
            if (className.equals("CourseTeacherService")) {
                targetModuleName = "开课管理";
                targetTable = "course_open";
            } else if (targetTable.contains("student")) {
                targetModuleName = "学生管理";
                targetTable = "student";
            } else if (targetTable.contains("courseteacher")) {
                targetModuleName = "开课管理";
                targetTable = "course_open";
            } else if (targetTable.contains("course") && !targetTable.contains("teacher")) {
                targetModuleName = "课程管理";
                targetTable = "course";
            } else if (targetTable.contains("teacher") && !targetTable.contains("course")) {
                targetModuleName = "教师管理";
                targetTable = "teacher";
            } else if (targetTable.contains("wordpaper") || targetTable.contains("paper")) {
                targetModuleName = "试卷分析";
                targetTable = "exam_paper_analysis";
            } else if (targetTable.contains("init")) {
                targetModuleName = "系统维护";
                targetTable = "all_tables";
            } else if (targetTable.contains("gradechangelog")) {
                targetModuleName = "成绩管理";
                targetTable = "grade_change_log";
            } else if (targetTable.contains("gradechangerequest")) {
                targetModuleName = "成绩管理";
                targetTable = "grade_change_request";
            } else if (targetTable.contains("grade") || targetTable.contains("sct")) {
                targetModuleName = "成绩管理";
                targetTable = "score";
            } else if (targetTable.contains("gradelevel")) {
                targetModuleName = "年级管理";
                targetTable = "grade_level";
            } else if (targetTable.contains("department")) {
                targetModuleName = "学院管理";
                targetTable = "department";
            } else if (targetTable.contains("major")) {
                targetModuleName = "专业管理";
                targetTable = "major";
            } else if (targetTable.contains("class")) {
                targetModuleName = "班级管理";
                targetTable = "class";
            } else {
                targetModuleName = "其他模块";
            }

            // 获取目标ID
            Long targetId = null;
            if (args.length > 0) {
                targetId = extractId(args[0]);
            }
            if (targetId == null && result != null) {
                targetId = extractId(result);
            }

            // 构建内容（必须包含操作类型和模块）
            String content = String.format("执行 %s 操作，模块：%s", operationType, targetModuleName);
            if (methodName.contains("importFromExcel") || methodName.contains("import")) {
                // 批量导入：显示导入结果摘要
                if (result != null && result instanceof String) {
                    String resultStr = (String) result;
                    // 提取成功/失败数量
                    if (resultStr.contains("成功") || resultStr.contains("失败")) {
                        content += "，" + resultStr;
                    } else {
                        content += "，结果: " + (resultStr.length() > 150 ? resultStr.substring(0, 150) + "..." : resultStr);
                    }
                } else {
                    content += "，批量导入操作";
                }
            } else if (methodName.contains("upload")) {
                content += "，结果: " + (result != null ? result.toString() : "未知");
            } else if (args.length > 0) {
                // 只显示关键信息，避免内容过长
                String argStr = args[0].toString();
                if (argStr.length() > 200) {
                    argStr = argStr.substring(0, 200) + "...";
                }
                content += "，详情: " + argStr;
            }

            // 记录日志：优先用 request 中的 operator，否则对试卷分析上传用 uploadBy 兜底
            String operator = getCurrentOperator();
            if ("system".equals(operator) && "WordPaperService".equals(className) && "upload".equals(methodName)
                    && args != null && args.length >= 2 && args[1] instanceof String) {
                String uploadBy = (String) args[1];
                if (uploadBy != null && !uploadBy.isEmpty()) {
                    operator = uploadBy;
                }
            }
            operator = normalizeOperator(operator);
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
