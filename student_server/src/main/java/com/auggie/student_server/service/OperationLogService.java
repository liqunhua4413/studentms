package com.auggie.student_server.service;

import com.auggie.student_server.entity.OperationLog;
import com.auggie.student_server.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: OperationLogService
 * @Version 1.0.0
 */

@Service
public class OperationLogService {
    @Autowired
    private OperationLogMapper operationLogMapper;

    public void recordOperation(String operator, String operationType, String targetTable, Long targetId, String content) {
        OperationLog log = new OperationLog();
        log.setOperator(operator);
        log.setOperationType(operationType);
        log.setTargetTable(targetTable);
        log.setTargetId(targetId);
        log.setContent(content);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    public List<OperationLog> findAll() {
        return operationLogMapper.findAll();
    }

    public OperationLog findById(Long id) {
        return operationLogMapper.findById(id);
    }

    public List<OperationLog> findBySearch(String operator, String operationType, String targetTable, String startTime, String endTime) {
        return operationLogMapper.findBySearch(operator, operationType, targetTable, startTime, endTime);
    }
}
