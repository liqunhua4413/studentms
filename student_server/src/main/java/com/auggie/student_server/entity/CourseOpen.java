package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * 开课表 course_open。教师任课；教师权限依据此表校验。
 * 唯一约束：uk_course_teacher_term (course_id, teacher_id, term_id)。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("CourseOpen")
public class CourseOpen {
    private Integer id;
    private Integer courseId;
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
