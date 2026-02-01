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
    private Integer departmentId;
    private String category;
    private String nature;
    private String courseType;  // 来自培养方案：REQUIRED/LIMITED/ELECTIVE，用于“我开设的课程”课程性质显示
    private String examMethod;
    private Integer hours;

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

    // 手动添加其他 getter/setter 方法以确保编译通过
    public String getCname() { return cname; }
    public void setCname(String cname) { this.cname = cname; }
    public Integer getCcredit() { return ccredit; }
    public void setCcredit(Integer ccredit) { this.ccredit = ccredit; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNature() { return nature; }
    public void setNature(String nature) { this.nature = nature; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public String getExamMethod() { return examMethod; }
    public void setExamMethod(String examMethod) { this.examMethod = examMethod; }
    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }
}
