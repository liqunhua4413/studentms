package com.auggie.student_server.controller;

import com.auggie.student_server.entity.OperationLog;
import com.auggie.student_server.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: OperationLogController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/operationLog")
public class OperationLogController {
    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/findAll")
    public List<OperationLog> findAll(@RequestAttribute(value = "operator", required = false) String currentOperator,
                                      @RequestAttribute(value = "userType", required = false) String userType,
                                      @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        // 权限控制：admin 可以查看所有日志
        if ("admin".equals(userType)) {
            return operationLogService.findAll();
        }
        // 院长只能查看自己学院的日志
        if ("dean".equals(userType) && departmentId != null) {
            return operationLogService.findByDepartmentId(departmentId);
        }
        // 其他用户只能查看自己的日志
        return operationLogService.findBySearch(currentOperator, null, null, null, null);
    }

    @GetMapping("/findById/{id}")
    public OperationLog findById(@PathVariable("id") Long id) {
        return operationLogService.findById(id);
    }

    @PostMapping("/findBySearch")
    public List<OperationLog> findBySearch(@RequestBody Map<String, String> map,
                                           @RequestAttribute(value = "operator", required = false) String currentOperator,
                                           @RequestAttribute(value = "userType", required = false) String userType,
                                           @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        // 权限控制：admin 可以查看所有日志
        if ("admin".equals(userType)) {
            String operator = map.get("operator");
            String operationType = map.get("operationType");
            String targetTable = map.get("targetTable");
            String startTime = map.get("startTime");
            String endTime = map.get("endTime");
            return operationLogService.findBySearch(operator, operationType, targetTable, startTime, endTime);
        }
        
        // 院长只能查看自己学院的日志
        if ("dean".equals(userType) && departmentId != null) {
            // 如果指定了操作者，需要验证是否属于该学院
            String operator = map.get("operator");
            if (operator != null && !operator.isEmpty()) {
                // 验证操作者是否属于该学院
                List<OperationLog> allDeptLogs = operationLogService.findByDepartmentId(departmentId);
                return allDeptLogs.stream()
                    .filter(log -> operator == null || log.getOperator().contains(operator))
                    .collect(java.util.stream.Collectors.toList());
            }
            return operationLogService.findByDepartmentId(departmentId);
        }
        
        // 其他用户只能查看自己的日志
        String operator = currentOperator;
        String operationType = map.get("operationType");
        String targetTable = map.get("targetTable");
        String startTime = map.get("startTime");
        String endTime = map.get("endTime");
        return operationLogService.findBySearch(operator, operationType, targetTable, startTime, endTime);
    }
}
