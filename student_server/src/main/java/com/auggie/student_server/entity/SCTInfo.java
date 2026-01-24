package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2022/2/11 11:14
 * @Description: SCTInfo
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("SCTInfo")
public class SCTInfo {
    @JsonProperty("sid")
    private Integer studentId;
    @JsonProperty("tid")
    private Integer teacherId;
    @JsonProperty("cid")
    private Integer courseId;
    private String sname;
    private String tname;
    private String teacherRealName; // 关联查询出的真实姓名
    private String cname;
    private Float grade;
    private String term;
    private Float usualGrade;
    private Float finalGrade;
    private Float totalGrade;
    private Integer classId;
    private Integer majorId;
    private Integer departmentId;
    private String className;
    private String majorName;
    private String departmentName;
    private String gradeLevel;
    private String courseCategory;
    private String courseNature;
    private String examMethod;
    private Integer hours;
    private Float credit;
}
