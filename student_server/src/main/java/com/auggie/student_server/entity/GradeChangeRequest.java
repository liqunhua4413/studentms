package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩修改申请 grade_change_request。流程控制层。
 * 审批流：PENDING → DEAN_APPROVED(可选) → APPROVED / REJECTED。
 * 仅审批通过后由管理员服务更新 score，并写 grade_change_log。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("GradeChangeRequest")
public class GradeChangeRequest {
    private Long id;
    private Integer scoreId;
    private Integer applicantId;
    private String applicantRole;
    private String reason;
    private String attachmentPath;
    private String attachmentName;
    /** 原成绩JSON：{usual_score, mid_score, final_score, grade} */
    private String beforeData;
    /** 目标成绩JSON：{usual_score, mid_score, final_score, grade} */
    private String afterData;
    /** 兼容旧字段，已废弃，使用 beforeData/afterData */
    @Deprecated
    private BigDecimal originalGrade;
    /** 兼容旧字段，已废弃，使用 beforeData/afterData */
    @Deprecated
    private BigDecimal targetGrade;
    /** PENDING / DEAN_APPROVED / APPROVED / REJECTED */
    private String status;
    private Integer deanApproverId;
    private Integer adminApproverId;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 手动添加 getter/setter 方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getScoreId() { return scoreId; }
    public void setScoreId(Integer scoreId) { this.scoreId = scoreId; }
    public Integer getApplicantId() { return applicantId; }
    public void setApplicantId(Integer applicantId) { this.applicantId = applicantId; }
    public String getApplicantRole() { return applicantRole; }
    public void setApplicantRole(String applicantRole) { this.applicantRole = applicantRole; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    public String getBeforeData() { return beforeData; }
    public void setBeforeData(String beforeData) { this.beforeData = beforeData; }
    public String getAfterData() { return afterData; }
    public void setAfterData(String afterData) { this.afterData = afterData; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDeanApproverId() { return deanApproverId; }
    public void setDeanApproverId(Integer deanApproverId) { this.deanApproverId = deanApproverId; }
    public Integer getAdminApproverId() { return adminApproverId; }
    public void setAdminApproverId(Integer adminApproverId) { this.adminApproverId = adminApproverId; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
