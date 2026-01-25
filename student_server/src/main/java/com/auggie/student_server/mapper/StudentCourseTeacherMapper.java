package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.CourseTeacher;
import com.auggie.student_server.entity.CourseTeacherInfo;
import com.auggie.student_server.entity.SCTInfo;
import com.auggie.student_server.entity.StudentCourseTeacher;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 19:58
 * @Description: StudentCourseTeacherMapper
 * @Version 1.0.0
 */

@Repository
@Mapper
public interface StudentCourseTeacherMapper {

    public List<CourseTeacherInfo> findByStudentId(@Param("sid") Integer sid,
                                                   @Param("term") String term);

    public List<SCTInfo> findBySearch(@Param("sid") Integer sid,
                                      @Param("sname") String sname,
                                      @Param("sFuzzy") Integer sFuzzy,
                                      @Param("cid") Integer cid,
                                      @Param("cname") String cname,
                                      @Param("cFuzzy") Integer cFuzzy,
                                      @Param("tid") Integer tid,
                                      @Param("tname") String tname,
                                      @Param("tFuzzy") Integer tFuzzy,
                                      @Param("lowBound") Integer lowBound,
                                      @Param("highBound") Integer highBound,
                                      @Param("term") String term,
                                      @Param("classId") Integer classId,
                                      @Param("majorId") Integer majorId,
                                      @Param("departmentId") Integer departmentId,
                                      @Param("className") String className,
                                      @Param("majorName") String majorName,
                                      @Param("departmentName") String departmentName,
                                      @Param("gradeLevelId") Integer gradeLevelId);

    @Select("SELECT DISTINCT term FROM studentms.score")
    public List<String> findAllTerm();

    @Select("SELECT id, student_id AS studentId, course_id AS courseId, teacher_id AS teacherId, grade, term, usual_score AS usualScore, mid_score AS midScore, final_score AS finalScore, remark FROM studentms.score WHERE student_id = #{sct.studentId} AND course_id = #{sct.courseId} AND term = #{sct.term}")
    public List<StudentCourseTeacher> findBySCT(@Param("sct") StudentCourseTeacher studentCourseTeacher);

    @Insert("INSERT INTO studentms.score (student_id, course_id, teacher_id, term, usual_score, mid_score, final_score, grade, remark) " +
            "VALUES (#{s.studentId}, #{s.courseId}, #{s.teacherId}, #{s.term}, #{s.usualScore}, #{s.midScore}, #{s.finalScore}, #{s.grade}, #{s.remark})")
    public boolean insert(@Param("s")StudentCourseTeacher studentCourseTeacher);

    @Update("UPDATE studentms.score SET " +
            "teacher_id = #{s.teacherId}, " +
            "usual_score = #{s.usualScore}, " +
            "mid_score = #{s.midScore}, " +
            "final_score = #{s.finalScore}, " +
            "grade = #{s.grade}, " +
            "remark = #{s.remark} " +
            "WHERE student_id = #{s.studentId} AND course_id = #{s.courseId} AND term = #{s.term}")
    public boolean updateGrade(@Param("s") StudentCourseTeacher s);

    public boolean batchInsert(@Param("list") List<StudentCourseTeacher> list);

    @Delete("DELETE FROM studentms.score WHERE student_id = #{sct.studentId} AND teacher_id = #{sct.teacherId} AND course_id = #{sct.courseId}")
    public boolean deleteBySCT(@Param("sct") StudentCourseTeacher sct);
}
