package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * 成绩查询 DTO。联表 score + student + course + teacher，含 status。
 * 用于权限过滤后的查询结果展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("ScoreQueryDTO")
public class ScoreQueryDTO {
    private Integer id;
    private Integer studentId;
    private Integer courseId;
    private Integer teacherId;
    private Integer termId;
    /** 学期名称，联表 term 得到，用于展示 */
    private String term;
    private BigDecimal usualScore;
    private BigDecimal midScore;
    private BigDecimal finalScore;
    private BigDecimal grade;
    private String remark;
    /** UPLOADED / PUBLISHED / LOCKED */
    private String status;

    private String studentNo;
    private String sname;
    private String cname;
    private String tname;
    private String teacherRealName;
    private Integer departmentId;
    private String departmentName;
    private Integer studentDepartmentId;
    private String studentDepartmentName;
    private Integer majorId;
    private String majorName;
    private Integer classId;
    private String className;
    private Integer gradeLevelId;
    private String gradeLevel;
    private BigDecimal credit;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
