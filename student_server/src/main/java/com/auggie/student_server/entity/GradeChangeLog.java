package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 成绩审计表 grade_change_log。审计追溯层。
 * 不可删除、不可修改；仅追加。before_json/after_json 存 JSON 快照。
 * operation：IMPORT / PUBLISH / CHANGE。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("GradeChangeLog")
public class GradeChangeLog {
    private Long id;
    private Integer scoreId;
    private String operation;
    private Integer operatorId;
    private String operatorName;
    /** JSON 变更前快照 */
    private String beforeJson;
    /** JSON 变更后快照 */
    private String afterJson;
    private LocalDateTime createdAt;

    // 手动添加 getter/setter 方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getScoreId() { return scoreId; }
    public void setScoreId(Integer scoreId) { this.scoreId = scoreId; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getBeforeJson() { return beforeJson; }
    public void setBeforeJson(String beforeJson) { this.beforeJson = beforeJson; }
    public String getAfterJson() { return afterJson; }
    public void setAfterJson(String afterJson) { this.afterJson = afterJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
