package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩主表 score。仅存当前有效成绩。
 * 唯一约束：uk_score_student_course_term (student_id, course_id, term_id)。
 * status：UPLOADED 已上传 / PUBLISHED 已发布 / LOCKED 锁定；学生仅可见 PUBLISHED。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("Score")
public class Score {
    private Integer id;
    private Integer studentId;
    private Integer courseId;
    private Integer teacherId;
    private Integer termId;
    private BigDecimal usualScore;
    private BigDecimal midScore;
    private BigDecimal finalScore;
    private BigDecimal grade;
    private String remark;
    /** UPLOADED / PUBLISHED / LOCKED */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 手动添加 getter/setter 方法以确保编译通过
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public Integer getTermId() { return termId; }
    public void setTermId(Integer termId) { this.termId = termId; }
    public BigDecimal getUsualScore() { return usualScore; }
    public void setUsualScore(BigDecimal usualScore) { this.usualScore = usualScore; }
    public BigDecimal getMidScore() { return midScore; }
    public void setMidScore(BigDecimal midScore) { this.midScore = midScore; }
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    public BigDecimal getGrade() { return grade; }
    public void setGrade(BigDecimal grade) { this.grade = grade; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
