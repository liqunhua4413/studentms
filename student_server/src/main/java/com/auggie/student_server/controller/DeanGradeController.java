package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.entity.ScoreQueryDTO;
import com.auggie.student_server.mapper.ScoreMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import com.auggie.student_server.service.DeanCollegeService;
import com.auggie.student_server.service.GradeChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 院长专用成绩管理接口。所有接口强制校验：course.college_id = 当前院长学院ID
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/dean/grade")
public class DeanGradeController {

    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private DeanCollegeService deanCollegeService;
    @Autowired
    private GradeChangeRequestService gradeChangeRequestService;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private com.auggie.student_server.service.TermService termService;

    /**
     * 院长成绩查询。强制过滤：course.college_id = 当前院长学院ID
     * 支持学生维度筛选（学生学院、专业、班级），不要求学生属于本学院。
     */
    @PostMapping("/query")
    public ResponseEntity<List<ScoreQueryDTO>> query(
            @RequestBody Map<String, Object> params,
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId,
            @RequestAttribute(value = "operator", required = false) String operator) {
        if (!"dean".equalsIgnoreCase(userType) || departmentId == null) {
            return ResponseEntity.status(403).body(List.of());
        }

        Integer courseId = toInt(params.get("courseId"));
        Integer termId = toInt(params.get("termId"));
        if (termId == null && params.get("term") != null) {
            String termStr = toString(params.get("term"));
            if (termStr != null) {
                com.auggie.student_server.entity.Term t = termService.findByName(termStr);
                if (t != null) termId = t.getId();
            }
        }
        String status = toString(params.get("status"));
        Integer studentId = toInt(params.get("studentId"));
        String studentNo = toString(params.get("studentNo"));
        String studentName = toString(params.get("studentName"));
        Integer studentCollegeId = toInt(params.get("studentCollegeId"));
        Integer studentMajorId = toInt(params.get("studentMajorId"));
        Integer studentClassId = toInt(params.get("studentClassId"));
        Double lowBound = toDouble(params.get("lowBound"));
        Double highBound = toDouble(params.get("highBound"));

        List<ScoreQueryDTO> results = scoreMapper.findScoreQueryForDean(
                courseId, termId, status, departmentId,
                studentId, studentNo, studentName,
                studentCollegeId, studentMajorId, studentClassId,
                lowBound, highBound
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 获取指定课程的成绩列表。用于成绩修改申请页面。
     * 强制过滤：course.college_id = 当前院长学院ID
     */
    @GetMapping("/list")
    public ResponseEntity<List<ScoreQueryDTO>> listByCourse(
            @RequestParam(value = "courseId") Integer courseId,
            @RequestParam(value = "termId", required = false) Integer termIdParam,
            @RequestParam(value = "term", required = false) String termStr,
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        if (!"dean".equalsIgnoreCase(userType) || departmentId == null) {
            return ResponseEntity.status(403).body(List.of());
        }
        Integer termId = termIdParam;
        if (termId == null && termStr != null && !termStr.trim().isEmpty()) {
            com.auggie.student_server.entity.Term t = termService.findByName(termStr.trim());
            if (t != null) termId = t.getId();
        }

        List<ScoreQueryDTO> results = scoreMapper.findScoreQueryForDean(
                courseId, termId, null, departmentId,
                null, null, null,
                null, null, null,
                null, null
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 提交成绩修改申请。仅生成申请记录，不允许直接修改成绩。
     * 强制校验：成绩所属课程必须属于本学院。
     */
    @PostMapping("/modify-apply")
    public ResponseEntity<Map<String, Object>> modifyApply(
            @RequestBody Map<String, Object> params,
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId,
            @RequestAttribute(value = "operator", required = false) String operator) {
        Map<String, Object> result = new HashMap<>();
        if (!"dean".equalsIgnoreCase(userType) || departmentId == null) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }

        Integer scoreId = toInt(params.get("scoreId"));
        String beforeData = toString(params.get("beforeData"));
        String afterData = toString(params.get("afterData"));
        String reason = toString(params.get("reason"));
        String attachmentPath = toString(params.get("attachmentPath"));
        String attachmentName = toString(params.get("attachmentName"));

        if (scoreId == null || beforeData == null || afterData == null || reason == null || reason.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return ResponseEntity.badRequest().body(result);
        }

        // 与 GradeChangeRequestController.submit 一致：先按 operator 解析申请人 ID，院长再按学院兜底
        Integer applicantId = resolveTeacherId(operator);
        if (applicantId == null && departmentId != null) {
            Teacher dean = teacherMapper.findDeanByDepartmentId(departmentId);
            if (dean != null) applicantId = dean.getId();
        }
        if (applicantId == null) {
            result.put("success", false);
            result.put("message", "无法识别申请人身份，请重新登录");
            return ResponseEntity.ok(result);
        }

        // 使用现有的 GradeChangeRequestService，它会校验课程是否属于本学院
        String message = gradeChangeRequestService.submitRequest(
                scoreId, beforeData, afterData, reason, attachmentPath, attachmentName,
                applicantId, "dean", departmentId, operator != null ? operator : "unknown"
        );

        boolean success = message != null && (message.contains("已提交") || message.contains("成功"));
        result.put("success", success);
        result.put("message", message != null ? message : "提交失败");
        return ResponseEntity.ok(result);
    }

    /** 与 GradeChangeRequestController 一致：按 operator 解析教师/院长 ID（先姓名再工号）. */
    private Integer resolveTeacherId(String operator) {
        if (operator == null || operator.isEmpty()) return null;
        if ("admin".equals(operator) || "系统管理员".equals(operator)) {
            List<Teacher> admins = teacherMapper.findBySearch(null, "系统管理员", 0);
            if (admins != null && !admins.isEmpty()) return admins.get(0).getId();
        }
        List<Teacher> list = teacherMapper.findBySearch(null, operator, 0);
        if (list != null && !list.isEmpty()) return list.get(0).getId();
        Teacher t = teacherMapper.findByTeacherNo(operator);
        return t != null ? t.getId() : null;
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString().trim()); } catch (Exception e) { return null; }
    }

    private static Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString().trim()); } catch (Exception e) { return null; }
    }

    private static String toString(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
