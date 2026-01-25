package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2022/2/8 16:11
 * @Description: Student
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("Student")
public class Student {
    private Integer id;
    private String studentNo;
    private String sname;
    private String password;
    private Integer classId;
    private Integer gradeLevelId;
    private Integer majorId;
    private Integer departmentId;

    // 冗余字段用于展示（含 join grade_level 的 name）
    private String className;
    private String gradeLevelName;
    private String majorName;
    private String departmentName;

    // 同时提供 id 和 sid 字段的 getter/setter，确保前后端双向兼容
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("sid")
    public Integer getSid() {
        return id;
    }

    @JsonProperty("sid")
    public void setSid(Integer sid) {
        this.id = sid;
    }

    // 手动添加其他 getter/setter 方法以确保编译通过
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getSname() { return sname; }
    public void setSname(String sname) { this.sname = sname; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public Integer getGradeLevelId() { return gradeLevelId; }
    public void setGradeLevelId(Integer gradeLevelId) { this.gradeLevelId = gradeLevelId; }
    public Integer getMajorId() { return majorId; }
    public void setMajorId(Integer majorId) { this.majorId = majorId; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getGradeLevelName() { return gradeLevelName; }
    public void setGradeLevelName(String gradeLevelName) { this.gradeLevelName = gradeLevelName; }
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
