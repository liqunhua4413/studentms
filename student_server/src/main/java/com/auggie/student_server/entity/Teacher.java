package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2022/2/9 10:50
 * @Description: Teacher
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("Teacher")
public class Teacher {
    private Integer id;
    private String teacherNo;
    private String tname;
    private String password;
    private String role;  // admin / teacher / dean
    private Integer departmentId;
    /** 所属学院名称，仅查询时关联 department 表填充，不持久化 */
    private String departmentName;

    // 同时提供 id 和 tid 字段的 getter/setter，确保前后端双向兼容
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("tid")
    public Integer getTid() {
        return id;
    }

    @JsonProperty("tid")
    public void setTid(Integer tid) {
        this.id = tid;
    }

    // 手动添加其他 getter/setter 方法以确保编译通过
    public String getTeacherNo() { return teacherNo; }
    public void setTeacherNo(String teacherNo) { this.teacherNo = teacherNo; }
    public String getTname() { return tname; }
    public void setTname(String tname) { this.tname = tname; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
