package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.CourseTeacher;
import com.auggie.student_server.entity.CourseTeacherInfo;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 16:43
 * @Description: CourseTeacherMapper
 * @Version 1.0.0
 */
@Repository
@Mapper
public interface CourseTeacherMapper {

    @Insert("INSERT INTO studentms.course_open (course_id, teacher_id, term_id) VALUES (#{cid}, #{tid}, #{termId})")
    boolean insertCourseTeacher(@Param("cid") Integer cid,
                                @Param("tid") Integer tid,
                                @Param("termId") Integer termId);

    List<CourseTeacher> findBySearch(@Param("cid") Integer cid,
                                     @Param("tid") Integer tid,
                                     @Param("termId") Integer termId);

    List<Course> findMyCourse(@Param("tid") Integer tid,
                              @Param("termId") Integer termId);

    public List<CourseTeacherInfo> findCourseTeacherInfo(@Param("tid") Integer tid,
                                                         @Param("tname") String tname,
                                                         @Param("tFuzzy") Integer tFuzzy,
                                                         @Param("cid") Integer cid,
                                                         @Param("cname") String cname,
                                                         @Param("cFuzzy") Integer cFuzzy);

    @Delete("DELETE FROM studentms.course_open WHERE course_id = #{c.courseId} AND teacher_id = #{c.teacherId}")
    public boolean deleteById(@Param("c") CourseTeacher courseTeacher);

    CourseTeacher findById(@Param("id") Integer id);

    int save(@Param("ct") CourseTeacher courseTeacher);

    int updateById(@Param("ct") CourseTeacher courseTeacher);
}
