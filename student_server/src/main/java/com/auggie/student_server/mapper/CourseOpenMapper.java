package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.CourseOpen;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 开课表 course_open。教师权限依据此表校验。
 * 唯一约束 uk_course_teacher_term (course_id, teacher_id, term)。
 */
@Mapper
@Repository
public interface CourseOpenMapper {

    /** 校验教师是否任课该课程该学期。返回 >0 表示存在。 */
    int countByTeacherCourseTerm(@Param("teacherId") Integer teacherId,
                                 @Param("courseId") Integer courseId,
                                 @Param("term") String term);

    List<CourseOpen> findByTeacherTerm(@Param("teacherId") Integer teacherId,
                                       @Param("term") String term);

    List<CourseOpen> findBySearch(@Param("courseId") Integer courseId,
                                  @Param("teacherId") Integer teacherId,
                                  @Param("term") String term);
}
