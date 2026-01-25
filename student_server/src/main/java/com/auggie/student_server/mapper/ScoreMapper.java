package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Score;
import com.auggie.student_server.entity.ScoreQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 成绩主表 score。唯一约束 uk_score_student_course_term (student_id, course_id, term)。
 * 所有 UPDATE 仅存在于管理员审批服务中。
 */
@Mapper
@Repository
public interface ScoreMapper {

    int insert(Score score);

    /** 批量插入；每条 status 必须为 UPLOADED。用于上传成绩。 */
    int batchInsert(@Param("list") List<Score> list);

    int updateById(Score score);

    Score findById(@Param("id") Integer id);

    Score findByStudentCourseTerm(@Param("studentId") Integer studentId,
                                  @Param("courseId") Integer courseId,
                                  @Param("term") String term);

    List<Score> findBySearch(@Param("studentId") Integer studentId,
                             @Param("courseId") Integer courseId,
                             @Param("term") String term,
                             @Param("status") String status,
                             @Param("departmentId") Integer departmentId,
                             @Param("teacherId") Integer teacherId);

    List<Integer> findScoreIdsByTermCourse(@Param("term") String term,
                                           @Param("courseId") Integer courseId);

    /** 联表查询，用于成绩查询 API；含 status 过滤与权限相关过滤；支持专业、班级、最低分、最高分。 */
    List<ScoreQueryDTO> findScoreQueryBySearch(@Param("studentId") Integer studentId,
                                               @Param("courseId") Integer courseId,
                                               @Param("term") String term,
                                               @Param("status") String status,
                                               @Param("departmentId") Integer departmentId,
                                               @Param("teacherId") Integer teacherId,
                                               @Param("majorId") Integer majorId,
                                               @Param("classId") Integer classId,
                                               @Param("gradeLevelId") Integer gradeLevelId,
                                               @Param("lowBound") Double lowBound,
                                               @Param("highBound") Double highBound);

    /** 院长专用查询：按课程所属学院过滤，支持学生维度筛选（学生学院、专业、班级）。 */
    List<ScoreQueryDTO> findScoreQueryForDean(@Param("courseId") Integer courseId,
                                               @Param("term") String term,
                                               @Param("status") String status,
                                               @Param("courseDepartmentId") Integer courseDepartmentId,
                                               @Param("studentId") Integer studentId,
                                               @Param("studentNo") String studentNo,
                                               @Param("studentName") String studentName,
                                               @Param("studentCollegeId") Integer studentCollegeId,
                                               @Param("studentMajorId") Integer studentMajorId,
                                               @Param("studentClassId") Integer studentClassId,
                                               @Param("lowBound") Double lowBound,
                                               @Param("highBound") Double highBound);
}
