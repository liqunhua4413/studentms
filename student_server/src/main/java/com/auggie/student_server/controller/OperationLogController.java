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
                                      @RequestAttribute(value = "userType", required = false) String userType) {
        // 权限控制：非 admin 用户只能查看自己的日志
        if (!"admin".equals(userType)) {
            return operationLogService.findBySearch(currentOperator, null, null, null, null);
        }
        return operationLogService.findAll();
    }

    @GetMapping("/findById/{id}")
    public OperationLog findById(@PathVariable("id") Long id) {
        return operationLogService.findById(id);
    }

    @PostMapping("/findBySearch")
    public List<OperationLog> findBySearch(@RequestBody Map<String, String> map,
                                           @RequestAttribute(value = "operator", required = false) String currentOperator,
                                           @RequestAttribute(value = "userType", required = false) String userType) {
        String operator = map.get("operator");
        
        // 权限控制：非 admin 用户只能查看自己的日志
        if (!"admin".equals(userType)) {
            operator = currentOperator;
        }
        
        String operationType = map.get("operationType");
        String targetTable = map.get("targetTable");
        String startTime = map.get("startTime");
        String endTime = map.get("endTime");
        return operationLogService.findBySearch(operator, operationType, targetTable, startTime, endTime);
    }
}
