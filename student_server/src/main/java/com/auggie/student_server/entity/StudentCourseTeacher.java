package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 19:57
 * @Description: StudentCourseTeacher
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("StudentCourseTeacher")
public class StudentCourseTeacher {
    private Integer id;
    private Integer studentId;
    private Integer courseId;
    private Integer teacherId;
    private Integer termId;
    private Float usualScore;   // 平时成绩
    private Float midScore;     // 期中成绩
    private Float finalScore;   // 期末成绩
    private Float grade;        // 总成绩
    private String remark;      // 备注信息

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
    public Float getUsualScore() { return usualScore; }
    public void setUsualScore(Float usualScore) { this.usualScore = usualScore; }
    public Float getMidScore() { return midScore; }
    public void setMidScore(Float midScore) { this.midScore = midScore; }
    public Float getFinalScore() { return finalScore; }
    public void setFinalScore(Float finalScore) { this.finalScore = finalScore; }
    public Float getGrade() { return grade; }
    public void setGrade(Float grade) { this.grade = grade; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
