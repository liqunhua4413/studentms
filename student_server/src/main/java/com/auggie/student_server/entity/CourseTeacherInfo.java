package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 18:45
 * @Description: CourseTeacherInfo
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("CourseTeacherInfo")
public class CourseTeacherInfo {
    private Integer id;
    @JsonProperty("cid")
    private Integer courseId;
    @JsonProperty("tid")
    private Integer teacherId;
    private Integer termId;
    private String cname;
    private String tname;
    private String termName;
    private Integer ccredit;
    private Float grade;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public Integer getTermId() { return termId; }
    public void setTermId(Integer termId) { this.termId = termId; }
    public String getCname() { return cname; }
    public void setCname(String cname) { this.cname = cname; }
    public String getTname() { return tname; }
    public void setTname(String tname) { this.tname = tname; }
    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }
    public Integer getCcredit() { return ccredit; }
    public void setCcredit(Integer ccredit) { this.ccredit = ccredit; }
    public Float getGrade() { return grade; }
    public void setGrade(Float grade) { this.grade = grade; }
}
