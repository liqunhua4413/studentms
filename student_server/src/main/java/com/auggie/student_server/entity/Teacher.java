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
}
