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
        // 院长可以查看自己学院的日志，优先通过operator匹配，如果匹配不到再通过departmentId
        if ("dean".equals(userType)) {
            if (currentOperator != null && !currentOperator.isEmpty() && !"unknown".equals(currentOperator)) {
                // 先通过operator查询，这样可以匹配到所有以该operator为操作者的日志
                List<OperationLog> operatorLogs = operationLogService.findBySearch(currentOperator, null, null, null, null);
                // 如果departmentId存在，也尝试通过departmentId查询，然后合并去重
                if (departmentId != null) {
                    List<OperationLog> deptLogs = operationLogService.findByDepartmentId(departmentId);
                    java.util.Map<Long, OperationLog> logMap = new java.util.HashMap<>();
                    for (OperationLog log : operatorLogs) logMap.put(log.getId(), log);
                    for (OperationLog log : deptLogs) logMap.put(log.getId(), log);
                    java.util.List<OperationLog> merged = new java.util.ArrayList<>(logMap.values());
                    merged.sort((a, b) -> (b.getCreateTime() == null ? LocalDateTime.MIN : b.getCreateTime())
                            .compareTo(a.getCreateTime() == null ? LocalDateTime.MIN : a.getCreateTime()));
                    return merged;
                }
                return operatorLogs;
            } else if (departmentId != null) {
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
        
        // 院长可以查看自己学院的日志，优先通过operator匹配
        if ("dean".equals(userType)) {
            String operator = map.get("operator");
            String operationType = map.get("operationType");
            String targetTable = map.get("targetTable");
            String startTime = map.get("startTime");
            String endTime = map.get("endTime");
            
            // 优先使用当前操作者（院长本人）来查询
            String queryOperator = (operator != null && !operator.isEmpty()) ? operator : currentOperator;
            
            if (queryOperator != null && !queryOperator.isEmpty() && !"unknown".equals(queryOperator)) {
                // 通过operator查询
                List<OperationLog> operatorLogs = operationLogService.findBySearch(queryOperator, operationType, targetTable, startTime, endTime);
                // 如果departmentId存在，也尝试通过departmentId查询，然后合并去重
                if (departmentId != null) {
                    List<OperationLog> deptLogs = operationLogService.findByDepartmentId(departmentId);
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
                    java.util.Map<Long, OperationLog> logMap = new java.util.HashMap<>();
                    for (OperationLog log : operatorLogs) logMap.put(log.getId(), log);
                    for (OperationLog log : deptLogs) logMap.put(log.getId(), log);
                    java.util.List<OperationLog> merged = new java.util.ArrayList<>(logMap.values());
                    merged.sort((a, b) -> (b.getCreateTime() == null ? LocalDateTime.MIN : b.getCreateTime())
                            .compareTo(a.getCreateTime() == null ? LocalDateTime.MIN : a.getCreateTime()));
                    return merged;
                }
                return operatorLogs;
            } else if (departmentId != null) {
                List<OperationLog> deptLogs = operationLogService.findByDepartmentId(departmentId);
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
