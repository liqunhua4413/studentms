package com.auggie.student_server.service;

import com.auggie.student_server.common.ScoreStatus;
import com.auggie.student_server.entity.ScoreQueryDTO;
import com.auggie.student_server.mapper.ScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成绩查询服务。严格按权限与 status 过滤。
 * 教师：仅本人任课；院长：仅本学院；管理员：全部。学生仅可见 PUBLISHED。
 */
@Service
public class GradeQueryService {

    @Autowired
    private ScoreMapper scoreMapper;

    /**
     * 按权限查询成绩（联表，含 status）。
     * 权限判断：admin 全部；dean 仅 course.department_id=本院；teacher 仅 course_open 任课。
     * 状态判断：仅查询时可按 status 过滤；不强制只查 PUBLISHED（管理员可查已上传）。
     * 支持专业、班级、最低分、最高分筛选。
     *
     * @param studentId    可选
     * @param courseId     可选
     * @param termId       可选，学期 id
     * @param status       可选，如 PUBLISHED、UPLOADED
     * @param departmentId 强制：院长/教师时必传本院
     * @param teacherId    强制：教师时必传本人 id
     * @param majorId      可选，按专业
     * @param classId      可选，按班级
     * @param lowBound     可选，总分最低分
     * @param highBound    可选，总分最高分
     * @return 过滤后的 ScoreQueryDTO 列表
     */
    public List<ScoreQueryDTO> query(Integer studentId, Integer courseId, Integer termId, String status,
                                     Integer departmentId, Integer teacherId,
                                     Integer majorId, Integer classId, Integer gradeLevelId,
                                     Double lowBound, Double highBound) {
        return scoreMapper.findScoreQueryBySearch(studentId, courseId, termId, status, departmentId, teacherId,
                majorId, classId, gradeLevelId, lowBound, highBound);
    }

    /**
     * 学生查询本人成绩。仅 status=PUBLISHED，强制 sid。
     *
     * @param studentId 学生 id
     * @param termId    可选，空则不过滤学期
     */
    public List<ScoreQueryDTO> queryForStudent(Integer studentId, Integer termId) {
        return scoreMapper.findScoreQueryBySearch(studentId, null, termId, ScoreStatus.PUBLISHED, null, null,
                null, null, null, null, null);
    }
}
