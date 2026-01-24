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
    private String term;
    private Float usualScore;   // 平时成绩
    private Float midScore;     // 期中成绩
    private Float finalScore;   // 期末成绩
    private Float grade;        // 总成绩
    private String remark;      // 备注信息
}
