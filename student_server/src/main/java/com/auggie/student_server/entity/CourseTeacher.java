package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 16:55
 * @Description: CourseTeacher
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("CourseTeacher")
public class CourseTeacher {
    @JsonProperty("ctid")
    private Integer id;
    @JsonProperty("cid")
    private Integer courseId;
    @JsonProperty("tid")
    private Integer teacherId;
    private Integer termId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public Integer getTermId() { return termId; }
    public void setTermId(Integer termId) { this.termId = termId; }
}
