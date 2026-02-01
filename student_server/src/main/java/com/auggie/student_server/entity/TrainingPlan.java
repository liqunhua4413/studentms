package com.auggie.student_server.entity;

import org.apache.ibatis.type.Alias;
import java.math.BigDecimal;

/**
 * 培养方案表。专业-课程规则，course_type 对应成绩单中的"课程性质"。
 */
@Alias("TrainingPlan")
public class TrainingPlan {
    private Long id;
    private Integer majorId;
    private Integer courseId;
    private String planVersion;
    private String courseType; // REQUIRED/LIMITED/ELECTIVE
    private Integer suggestedGrade;
    private Integer suggestedTermId;
    private BigDecimal minCredit;
    private BigDecimal maxCredit;
    private Integer maxCapacity;
    private Integer status;
    private String remark;

    // 关联显示用字段（非数据库字段）
    private String majorName;
    private String courseName;
    private String termName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMajorId() { return majorId; }
    public void setMajorId(Integer majorId) { this.majorId = majorId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public String getPlanVersion() { return planVersion; }
    public void setPlanVersion(String planVersion) { this.planVersion = planVersion; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public Integer getSuggestedGrade() { return suggestedGrade; }
    public void setSuggestedGrade(Integer suggestedGrade) { this.suggestedGrade = suggestedGrade; }
    public Integer getSuggestedTermId() { return suggestedTermId; }
    public void setSuggestedTermId(Integer suggestedTermId) { this.suggestedTermId = suggestedTermId; }
    public BigDecimal getMinCredit() { return minCredit; }
    public void setMinCredit(BigDecimal minCredit) { this.minCredit = minCredit; }
    public BigDecimal getMaxCredit() { return maxCredit; }
    public void setMaxCredit(BigDecimal maxCredit) { this.maxCredit = maxCredit; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }
}
