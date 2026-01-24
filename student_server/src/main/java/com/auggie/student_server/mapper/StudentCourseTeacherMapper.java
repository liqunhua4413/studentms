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
                                      @Param("gradeLevel") String gradeLevel);

    @Select("SELECT DISTINCT term FROM studentms.score")
    public List<String> findAllTerm();

    @Select("SELECT id, student_id AS studentId, course_id AS courseId, teacher_id AS teacherId, grade, term, usual_grade AS usualGrade, final_grade AS finalGrade, total_grade AS totalGrade, class_id AS classId, major_id AS majorId, department_id AS departmentId FROM studentms.score WHERE student_id = #{sct.studentId} AND course_id = #{sct.courseId} AND teacher_id = #{sct.teacherId} AND term = #{sct.term}")
    public List<StudentCourseTeacher> findBySCT(@Param("sct") StudentCourseTeacher studentCourseTeacher);

    @Insert("INSERT INTO studentms.score (student_id, course_id, teacher_id, grade, term, usual_grade, final_grade, total_grade, class_id, major_id, department_id) " +
            "VALUES (#{s.studentId}, #{s.courseId}, #{s.teacherId}, #{s.grade}, #{s.term}, #{s.usualGrade}, #{s.finalGrade}, #{s.totalGrade}, #{s.classId}, #{s.majorId}, #{s.departmentId})")
    public boolean insert(@Param("s")StudentCourseTeacher studentCourseTeacher);

    @Update("UPDATE studentms.score SET " +
            "grade = #{s.totalGrade}, " +
            "total_grade = #{s.totalGrade}, " +
            "usual_grade = #{s.usualGrade}, " +
            "final_grade = #{s.finalGrade}, " +
            "course_name = (SELECT cname FROM studentms.course WHERE id = #{s.courseId}), " +
            "teacher_name = (SELECT tname FROM studentms.teacher WHERE id = #{s.teacherId}), " +
            "grade_level = #{s.gradeLevel}, " +
            "major_name = (SELECT name FROM studentms.major WHERE id = #{s.majorId}), " +
            "class_name = (SELECT name FROM studentms.class WHERE id = #{s.classId}), " +
            "department_name = (SELECT name FROM studentms.department WHERE id = #{s.departmentId}), " +
            "department_id = #{s.departmentId}, " +
            "major_id = #{s.majorId}, " +
            "class_id = #{s.classId} " +
            "WHERE student_id = #{s.studentId} AND teacher_id = #{s.teacherId} AND course_id = #{s.courseId} AND term = #{s.term}")
    public boolean updateGrade(@Param("s") StudentCourseTeacher s);

    public boolean batchInsert(@Param("list") List<StudentCourseTeacher> list);

    @Delete("DELETE FROM studentms.score WHERE student_id = #{sct.studentId} AND teacher_id = #{sct.teacherId} AND course_id = #{sct.courseId}")
    public boolean deleteBySCT(@Param("sct") StudentCourseTeacher sct);
}
