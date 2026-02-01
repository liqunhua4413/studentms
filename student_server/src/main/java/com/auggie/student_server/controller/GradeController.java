package com.auggie.student_server.controller;

import com.auggie.student_server.entity.SCTInfo;
import com.auggie.student_server.entity.ScoreQueryDTO;
import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.mapper.TeacherMapper;
import com.auggie.student_server.service.DeanCollegeService;
import com.auggie.student_server.service.GradeQueryService;
import com.auggie.student_server.service.GradeService;
import com.auggie.student_server.service.GradeUploadService;
import com.auggie.student_server.service.SCTService;
import com.auggie.student_server.service.TermService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成绩管理控制器。严格按权限：admin 全部；teacher 仅 course_open；dean 仅本院。
 * 上传后 status=UPLOADED（已上传），等待管理员发布；发布后 PUBLISHED。查询按 status 与权限过滤。
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/grade")
public class GradeController {
    @Autowired
    private GradeService gradeService;
    @Autowired
    private GradeUploadService gradeUploadService;
    @Autowired
    private GradeQueryService gradeQueryService;
    @Autowired
    private SCTService sctService;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private DeanCollegeService deanCollegeService;
    @Autowired
    private TermService termService;

    /**
     * 上传成绩单。权限：admin 全部；teacher 仅任课；dean 仅本院。上传前检查学号+课程+学期是否已有成绩；上传后 status=UPLOADED，写 grade_change_log。
     */
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "departmentId", required = false) Integer paramDepartmentId,
                              @RequestAttribute(value = "operator", required = false) String operator,
                              @RequestAttribute(value = "userType", required = false) String userType,
                              @RequestAttribute(value = "departmentId", required = false) Integer userDepartmentId) {
        if (operator == null || operator.isEmpty()) operator = "unknown";
        Integer finalDeptId = paramDepartmentId;
        Integer effectiveUserDeptId = userDepartmentId;
        if ("teacher".equals(userType) && userDepartmentId != null) {
            finalDeptId = userDepartmentId;
        } else if ("dean".equals(userType)) {
            if (userDepartmentId != null) {
                finalDeptId = userDepartmentId;
                effectiveUserDeptId = userDepartmentId;
            } else {
                Map<String, Object> college = deanCollegeService.getDeanCollege(operator);
                if (college != null && college.get("collegeId") != null) {
                    Object cid = college.get("collegeId");
                    effectiveUserDeptId = cid instanceof Number ? ((Number) cid).intValue() : Integer.parseInt(cid.toString());
                    finalDeptId = effectiveUserDeptId;
                }
            }
        }
        if ("admin".equals(userType) && finalDeptId == null) {
            return "上传失败：请先选择学院";
        }
        Integer teacherId = ("teacher".equals(userType)) ? getTeacherIdByName(operator) : null;
        return gradeUploadService.uploadExcel(file, operator, userType, effectiveUserDeptId, teacherId, finalDeptId);
    }

    @GetMapping("/records")
    public List<com.auggie.student_server.entity.ScoreImportRecord> getImportRecords(
            @RequestAttribute(value = "operator", required = false) String operator,
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        if ("admin".equals(userType)) {
            return gradeService.findAllRecords();
        }
        Integer deptId = departmentId;
        if ("dean".equals(userType) && deptId == null && operator != null) {
            Map<String, Object> college = deanCollegeService.getDeanCollege(operator);
            if (college != null && college.get("collegeId") != null) {
                Object cid = college.get("collegeId");
                deptId = cid instanceof Number ? ((Number) cid).intValue() : Integer.parseInt(cid.toString());
            }
        }
        if (("dean".equals(userType) || "teacher".equals(userType)) && deptId != null) {
            return gradeService.findRecordsByDepartmentId(deptId);
        }
        return gradeService.findAllRecords();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadRecord(@PathVariable Long id) throws IOException {
        java.io.File file = gradeService.getRecordFile(id);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        
        com.auggie.student_server.entity.ScoreImportRecord record = gradeService.findAllRecords().stream()
                .filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        String fileName = record != null ? record.getFileName() : "score.xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", java.net.URLEncoder.encode(fileName, "UTF-8"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @DeleteMapping("/record/{id}")
    public boolean deleteRecord(@PathVariable Long id) {
        return gradeService.deleteRecord(id);
    }

    /**
     * 发布成绩。仅 admin 可调用。将 UPLOADED（已上传）→PUBLISHED，写 grade_change_log。
     * 权限判断：仅 userType=admin；状态判断：仅 UPLOADED 可发布。
     */
    @PostMapping("/publish")
    public Map<String, Object> publishScores(@RequestBody Map<String, Object> body,
                                             @RequestAttribute(value = "userType", required = false) String userType,
                                             @RequestAttribute(value = "operator", required = false) String operator) {
        if (!"admin".equals(userType)) {
            return Map.of("success", false, "message", "权限不足：仅管理员可发布成绩");
        }
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) body.get("scoreIds");
        List<Integer> scoreIds = ids != null ? ids.stream().map(Number::intValue).collect(Collectors.toList()) : new ArrayList<>();
        Integer adminId = getTeacherIdByName(operator != null ? operator : "系统管理员");
        String msg = gradeUploadService.publishScores(scoreIds, adminId, operator != null ? operator : "系统管理员");
        return Map.of("success", msg.startsWith("发布成功"), "message", msg);
    }

    /**
     * 锁定成绩。仅 admin 可调用。将任意状态改为 LOCKED，写 grade_change_log。
     * 权限判断：仅 userType=admin。
     */
    @PostMapping("/lock")
    public Map<String, Object> lockScores(@RequestBody Map<String, Object> body,
                                          @RequestAttribute(value = "userType", required = false) String userType,
                                          @RequestAttribute(value = "operator", required = false) String operator) {
        if (!"admin".equals(userType)) {
            return Map.of("success", false, "message", "权限不足：仅管理员可锁定成绩");
        }
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) body.get("scoreIds");
        List<Integer> scoreIds = ids != null ? ids.stream().map(Number::intValue).collect(Collectors.toList()) : new ArrayList<>();
        Integer adminId = getTeacherIdByName(operator != null ? operator : "系统管理员");
        String msg = gradeUploadService.lockScores(scoreIds, adminId, operator != null ? operator : "系统管理员");
        return Map.of("success", msg.startsWith("锁定成功"), "message", msg);
    }

    /**
     * 强制修正锁定成绩。仅 admin 可调用。修正后写超高等级日志。
     * 权限判断：仅 userType=admin；状态判断：仅 LOCKED 可强制修正。
     */
    @PostMapping("/force-edit")
    public Map<String, Object> forceEdit(@RequestBody Map<String, Object> body,
                                         @RequestAttribute(value = "userType", required = false) String userType,
                                         @RequestAttribute(value = "operator", required = false) String operator) {
        if (!"admin".equals(userType)) {
            return Map.of("success", false, "message", "权限不足：仅管理员可强制修正锁定成绩");
        }
        Integer scoreId = body.get("scoreId") != null ? Integer.parseInt(body.get("scoreId").toString()) : null;
        Object usualScore = body.get("usualScore");
        Object midScore = body.get("midScore");
        Object finalScore = body.get("finalScore");
        Object grade = body.get("grade");
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        Integer adminId = getTeacherIdByName(operator != null ? operator : "系统管理员");
        String msg = gradeUploadService.forceEdit(scoreId, usualScore, midScore, finalScore, grade, reason, adminId, operator != null ? operator : "系统管理员");
        return Map.of("success", msg.startsWith("修正成功"), "message", msg);
    }

    /**
     * 成绩查询。权限：admin 全部；dean 仅本院；teacher 仅本人任课。状态可按 status 过滤。
     */
    @PostMapping("/query")
    public List<ScoreQueryDTO> queryGrades(@RequestBody Map<String, Object> map,
                                           @RequestAttribute(value = "userType", required = false) String userType,
                                           @RequestAttribute(value = "departmentId", required = false) Integer departmentId,
                                           @RequestAttribute(value = "operator", required = false) String operator) {
        Integer studentId = toInt(map.get("studentId"));
        Integer courseId = toInt(map.get("courseId"));
        Integer termId = resolveTermId(map.get("term"));
        String status = map.get("status") != null ? map.get("status").toString().trim() : null;
        if (status != null && status.isEmpty()) status = null;
        Integer majorId = toInt(map.get("majorId"));
        Integer classId = toInt(map.get("classId"));
        Integer gradeLevelId = toInt(map.get("gradeLevelId"));
        Integer queryTeacherId = toInt(map.get("teacherId")); // 查询条件中的教师ID
        Double lowBound = toDouble(map.get("lowBound"));
        Double highBound = toDouble(map.get("highBound"));
        Integer deptId = null;
        Integer teacherId = null;
        if ("dean".equals(userType) && departmentId != null) deptId = departmentId;
        if ("teacher".equals(userType) && operator != null) teacherId = getTeacherIdByName(operator);
        // 如果查询条件中指定了 teacherId，使用查询条件中的值（admin 可以查询任意教师）
        if (queryTeacherId != null && "admin".equals(userType)) {
            teacherId = queryTeacherId;
        }
        return gradeQueryService.query(studentId, courseId, termId, status, deptId, teacherId,
                majorId, classId, gradeLevelId, lowBound, highBound);
    }

    /**
     * 学生查询本人成绩。仅 status=PUBLISHED。sid 须为当前登录学生 id（前端按会话校验）。
     */
    @GetMapping("/query/student")
    public List<ScoreQueryDTO> queryForStudent(@RequestParam(value = "sid") Integer studentId,
                                               @RequestParam(value = "termId", required = false) Integer termId) {
        return gradeQueryService.queryForStudent(studentId, termId);
    }

    /** 解析 term 参数：支持 termId(Number) 或 term(String 学期名称) */
    private Integer resolveTermId(Object termParam) {
        if (termParam == null) return null;
        if (termParam instanceof Number) return ((Number) termParam).intValue();
        String s = termParam.toString().trim();
        if (s.isEmpty()) return null;
        com.auggie.student_server.entity.Term t = termService.findByName(s);
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

    /**
     * 通过教师姓名查找教师ID
     */
    private Integer getTeacherIdByName(String teacherName) {
        if (teacherName == null || teacherName.isEmpty()) return null;
        try {
            List<Teacher> teachers = teacherMapper.findBySearch(null, teacherName, 0);
            if (teachers != null && !teachers.isEmpty()) return teachers.get(0).getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/reexamination")
    public List<SCTInfo> getReexaminationList(@RequestBody Map<String, Object> map) {
        return sctService.getReexaminationList(map);
    }

    @PostMapping("/reexamination/export")
    public ResponseEntity<byte[]> exportReexamination(@RequestBody Map<String, Object> map,
                                                      @RequestAttribute(value = "userType", required = false) String userType,
                                                      @RequestAttribute(value = "departmentId", required = false) Integer departmentId,
                                                      @RequestAttribute(value = "operator", required = false) String operator) throws IOException {
        // 如果是院长或教师，强制添加 departmentId 过滤
        if (("dean".equals(userType) || "teacher".equals(userType)) && departmentId != null) {
            map.put("departmentId", departmentId);
        }
        // 如果是教师，还需要添加教师ID过滤
        if ("teacher".equals(userType) && operator != null) {
            Integer teacherId = getTeacherIdByName(operator);
            if (teacherId != null) {
                map.put("tid", teacherId);
            }
        }
        // 根据查询条件导出，而不是固定导出补考名单
        List<SCTInfo> list = sctService.findBySearch(map);
        Workbook workbook = gradeService.exportReexaminationToExcel(list);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "成绩查询结果.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    /**
     * 下载成绩单批量导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = gradeService.generateScoreTemplate();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "成绩单批量导入模板.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
