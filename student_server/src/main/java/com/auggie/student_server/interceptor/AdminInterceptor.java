package com.auggie.student_server.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: AdminInterceptor - Admin 权限拦截器
 * @Version 1.0.0
 */

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Operator");

        // 处理 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return false;
        }

        // 获取操作者信息（从请求头获取）
        String operator = request.getHeader("Operator");
        if (operator == null || operator.isEmpty()) {
            operator = request.getParameter("operator");
        }
        if (operator == null || operator.isEmpty()) {
            operator = "unknown";
        }
        // 将操作者信息存储到 request 属性中，供后续使用
        request.setAttribute("operator", operator);

        // 检查是否是 admin 操作
        String requestURI = request.getRequestURI();
        
        // 需要 admin 权限的路径
        String[] adminPaths = {
            "/department", "/major", "/class",
            "/grade/upload", "/grade/reexamination/export",
            "/paper/deleteById",
            "/operationLog",
            "/student/import", "/teacher/import",
            "/department/import", "/major/import", "/class/import", "/course/import",
            "/init/clearTestData", "/init/importBaseData"
        };

        boolean needAdmin = false;
        for (String path : adminPaths) {
            if (requestURI.contains(path)) {
                needAdmin = true;
                break;
            }
        }

        if (needAdmin) {
            // 检查操作者是否是 admin
            // 这里简化处理，实际应该从 Session 或 Token 中获取用户角色
            // 当前通过请求头或参数传递，前端需要在请求时设置
            if (!"admin".equals(operator)) {
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "需要管理员权限");
                ObjectMapper mapper = new ObjectMapper();
                response.getWriter().write(mapper.writeValueAsString(result));
                return false;
            }
        }

        return true;
    }
}
