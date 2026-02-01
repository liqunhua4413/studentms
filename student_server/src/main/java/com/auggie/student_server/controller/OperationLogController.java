package com.auggie.student_server.controller;

import com.auggie.student_server.entity.OperationLog;
import com.auggie.student_server.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        // 院长只能查看本学院教师的日志（通过 department_id 过滤）
        if ("dean".equals(userType)) {
            if (departmentId != null) {
                return operationLogService.findByDepartmentId(departmentId);
            }
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
        
        // 院长只能查看本学院教师的日志（通过 department_id 过滤），支持其他查询条件
        if ("dean".equals(userType)) {
            if (departmentId != null) {
                String operationType = map.get("operationType");
                String targetTable = map.get("targetTable");
                String startTime = map.get("startTime");
                String endTime = map.get("endTime");
                
                List<OperationLog> deptLogs = operationLogService.findByDepartmentId(departmentId);
                
                // 应用其他过滤条件
                if (operationType != null && !operationType.isEmpty()) {
                    deptLogs = deptLogs.stream()
                        .filter(log -> operationType.equals(log.getOperationType()))
                        .collect(java.util.stream.Collectors.toList());
                }
                if (targetTable != null && !targetTable.isEmpty()) {
                    deptLogs = deptLogs.stream()
                        .filter(log -> targetTable.equals(log.getTargetTable()))
                        .collect(java.util.stream.Collectors.toList());
                }
                if (startTime != null && !startTime.isEmpty()) {
                    LocalDateTime st = parseTime(startTime);
                    if (st != null) {
                        final LocalDateTime st0 = st;
                        deptLogs = deptLogs.stream()
                            .filter(log -> log.getCreateTime() != null && !log.getCreateTime().isBefore(st0))
                            .collect(java.util.stream.Collectors.toList());
                    }
                }
                if (endTime != null && !endTime.isEmpty()) {
                    LocalDateTime et = parseTime(endTime);
                    if (et != null) {
                        final LocalDateTime et0 = et;
                        deptLogs = deptLogs.stream()
                            .filter(log -> log.getCreateTime() != null && !log.getCreateTime().isAfter(et0))
                            .collect(java.util.stream.Collectors.toList());
                    }
                }
                return deptLogs;
            }
        }
        
        // 其他用户只能查看自己的日志
        String operator = currentOperator;
        String operationType = map.get("operationType");
        String targetTable = map.get("targetTable");
        String startTime = map.get("startTime");
        String endTime = map.get("endTime");
        return operationLogService.findBySearch(operator, operationType, targetTable, startTime, endTime);
    }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static LocalDateTime parseTime(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s, TIME_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
