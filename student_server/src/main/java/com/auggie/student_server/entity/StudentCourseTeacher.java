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
    private Float grade;  // 保留原有字段，兼容旧数据
    private String term;
    private Float usualGrade;   // 平时成绩
    private Float finalGrade;   // 期末成绩
    private Float totalGrade;   // 总成绩
    private Integer classId;    // 班级ID
    private Integer majorId;    // 专业ID
    private Integer departmentId; // 系ID
    private String courseName;
    private String courseCategory;
    private String courseNature;
    private String examMethod;
    private String teacherName;
    private Integer hours;
    private Float credit;
    private String gradeLevel;
    private String majorName;
    private String className;
    private String departmentName;
}
