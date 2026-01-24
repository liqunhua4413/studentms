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
    private String gradeLevel;
    private Integer majorId;
    private Integer departmentId;

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
}
