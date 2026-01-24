package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2022/2/9 13:29
 * @Description: Course
 * @Version 1.0.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Alias("Course")
public class Course {
    private Integer id;
    private String cname;
    private Integer ccredit;
    private Integer majorId;
    private Integer departmentId;

    // 同时提供 id 和 cid 字段的 getter/setter，确保前后端双向兼容
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("cid")
    public Integer getCid() {
        return id;
    }

    @JsonProperty("cid")
    public void setCid(Integer cid) {
        this.id = cid;
    }
}
