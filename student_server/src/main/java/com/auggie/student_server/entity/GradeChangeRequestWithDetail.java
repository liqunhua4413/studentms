package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成绩修改申请（含学号、姓名、课程、学期等展示用字段）。
 * 用于「我的申请」列表联表查询。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeChangeRequestWithDetail {
    private Long id;
    private Integer scoreId;
    private Integer applicantId;
    private String applicantRole;
    private String reason;
    private String attachmentPath;
    private String attachmentName;
    private String beforeData;
    private String afterData;
    private String status;
    private Integer deanApproverId;
    private Integer adminApproverId;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String studentNo;
    private String sname;
    private String cname;
    private String term;

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
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getSname() { return sname; }
    public void setSname(String sname) { this.sname = sname; }
    public String getCname() { return cname; }
    public void setCname(String cname) { this.cname = cname; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
}
