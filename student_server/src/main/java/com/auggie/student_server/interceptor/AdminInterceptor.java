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
 * @Description: AdminInterceptor - 权限拦截器
 * @Version 1.0.0
 */

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Operator, UserType");

        // 处理 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return false;
        }

        // 获取操作者信息
        String operator = request.getHeader("Operator");
        String userType = request.getHeader("UserType");
        
        if (operator == null || operator.isEmpty()) {
            operator = request.getParameter("operator");
        }
        if (userType == null || userType.isEmpty()) {
            userType = request.getParameter("userType");
        }
        
        if (operator == null || operator.isEmpty()) {
            operator = "unknown";
        }
        
        // 将操作者信息存储到 request 属性中
        request.setAttribute("operator", operator);
        request.setAttribute("userType", userType);

        // 检查是否是 admin/dean 操作
        String requestURI = request.getRequestURI();
        
        // 需要管理员或院长权限的路径
        String[] restrictedPaths = {
            "/department", "/major", "/class",
            "/grade/upload", "/grade/reexamination/export",
            "/paper/deleteById",
            "/operationLog",
            "/student/import", "/teacher/import",
            "/department/import", "/major/import", "/class/import", "/course/import",
            "/admin/clearAllData", "/admin/generateTestData"
        };

        boolean isRestricted = false;
        for (String path : restrictedPaths) {
            if (requestURI.contains(path)) {
                isRestricted = true;
                break;
            }
        }

        if (isRestricted) {
            boolean isAuthorized = "admin".equals(operator) || "admin".equals(userType) || "dean".equals(userType);
            
            // 如果不是 admin 或 dean，则拦截
            if (!isAuthorized) {
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "权限不足");
                ObjectMapper mapper = new ObjectMapper();
                response.getWriter().write(mapper.writeValueAsString(result));
                return false;
            }
        }

        return true;
    }
}
