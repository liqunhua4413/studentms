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
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Operator, UserType, DepartmentId");

        // 处理 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return false;
        }

        // 获取操作者信息
        String operator = request.getHeader("Operator");
        if (operator != null) {
            try {
                operator = java.net.URLDecoder.decode(operator, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String userType = request.getHeader("UserType");
        String departmentId = request.getHeader("DepartmentId");
        
        if (operator == null || operator.isEmpty()) {
            operator = request.getParameter("operator");
        }
        if (userType == null || userType.isEmpty()) {
            userType = request.getParameter("userType");
        }
        if (departmentId == null || departmentId.isEmpty()) {
            departmentId = request.getParameter("departmentId");
        }
        
        if (operator == null || operator.isEmpty()) {
            operator = "unknown";
        }
        
        // 将操作者信息存储到 request 属性中
        request.setAttribute("operator", operator);
        request.setAttribute("userType", userType);
        if (departmentId != null && !departmentId.isEmpty()) {
            try {
                request.setAttribute("departmentId", Integer.parseInt(departmentId));
            } catch (NumberFormatException e) {
                // 忽略格式错误
            }
        }

        // 检查权限
        String requestURI = request.getRequestURI();
        
        // 仅管理员（admin）可操作的路径：写操作（增删改、导入、模板），不拦截只读接口（如 findById/findAll）
        String[] adminOnlyPaths = {
            "/department/add", "/department/deleteById", "/department/update", "/department/import", "/department/template",
            "/major/add", "/major/deleteById", "/major/update", "/major/import", "/major/template",
            "/class/add", "/class/deleteById", "/class/update", "/class/import", "/class/template",
            "/grade/reexamination/export",
            "/paper/deleteById",
            "/student/import", "/teacher/import",
            "/course/import", "/courseTeacher/import",
            "/admin/clearAllData", "/admin/generateTestData",
            "/init/clearTestData", "/init/importBaseData"
        };

        // 检查是否是仅管理员可操作的路径
        boolean isAdminOnly = false;
        for (String path : adminOnlyPaths) {
            if (requestURI.contains(path)) {
                isAdminOnly = true;
                break;
            }
        }

        if (isAdminOnly) {
            // 权限检查：仅检查 userType 是否为 "admin"（不检查 operator，operator 是中文名称如"系统管理员"）
            // 调试日志：输出 userType 和 operator 以便排查问题
            System.out.println("[AdminInterceptor] 权限检查 - URI: " + requestURI + ", userType: " + userType + ", operator: " + operator);
            if (!"admin".equals(userType)) {
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "权限不足：仅系统管理员可操作（当前 userType: " + (userType != null ? userType : "null") + "）");
                ObjectMapper mapper = new ObjectMapper();
                response.getWriter().write(mapper.writeValueAsString(result));
                return false;
            }
        }

        return true;
    }
}
