package com.auggie.student_server.service;

import com.auggie.student_server.common.GradeChangeLogOperation;
import com.auggie.student_server.entity.GradeChangeLog;
import com.auggie.student_server.entity.Score;
import com.auggie.student_server.mapper.GradeChangeLogMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 成绩审计 GradeChangeLog 服务。
 * 仅追加，禁止 DELETE/UPDATE。每次导入、发布、修改均须写入。
 */
@Service
public class GradeChangeLogService {

    @Autowired
    private GradeChangeLogMapper gradeChangeLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 追加审计日志。不可删除、不可修改。
     *
     * @param scoreId     关联成绩 id
     * @param operation   IMPORT / PUBLISH / CHANGE
     * @param operatorId  操作人 id
     * @param operatorName 操作人姓名
     * @param before      变更前快照（JSON），可为 null
     * @param after       变更后快照（JSON）
     */
    public void appendLog(Integer scoreId, String operation, Integer operatorId, String operatorName,
                          String before, String after) {
        GradeChangeLog log = new GradeChangeLog();
        log.setScoreId(scoreId);
        log.setOperation(operation);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setBeforeJson(before);
        log.setAfterJson(after);
        gradeChangeLogMapper.insert(log);
    }

    /**
     * 写入 IMPORT 审计。上传成绩后调用，after 为插入的 score 快照。
     */
    public void logImport(Score score, Integer operatorId, String operatorName) {
        String after = toJson(score);
        appendLog(score.getId(), GradeChangeLogOperation.IMPORT, operatorId, operatorName, null, after);
    }

    /**
     * 写入 PUBLISH 审计。发布成绩后调用，before 为 UPLOADED 快照，after 为 PUBLISHED 快照。
     */
    public void logPublish(Score before, Score after, Integer operatorId, String operatorName) {
        appendLog(after.getId(), GradeChangeLogOperation.PUBLISH, operatorId, operatorName,
                toJson(before), toJson(after));
    }

    /**
     * 写入 CHANGE 审计。审批通过修改 score 后调用。
     */
    public void logChange(Score before, Score after, Integer operatorId, String operatorName) {
        appendLog(after.getId(), GradeChangeLogOperation.CHANGE, operatorId, operatorName,
                toJson(before), toJson(after));
    }

    private String toJson(Score s) {
        if (s == null) return null;
        try {
            return objectMapper.writeValueAsString(s);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
