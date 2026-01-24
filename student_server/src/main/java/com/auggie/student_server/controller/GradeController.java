package com.auggie.student_server.controller;

import com.auggie.student_server.entity.SCTInfo;
import com.auggie.student_server.service.GradeService;
import com.auggie.student_server.service.SCTService;
import com.auggie.student_server.mapper.TeacherMapper;
import com.auggie.student_server.entity.Teacher;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: GradeController - 成绩管理控制器
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/grade")
public class GradeController {
    @Autowired
    private GradeService gradeService;
    @Autowired
    private SCTService sctService;
    @Autowired
    private TeacherMapper teacherMapper;

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "departmentId", required = false) Integer paramDepartmentId,
                              @RequestAttribute(value = "operator", required = false) String operator,
                              @RequestAttribute(value = "userType", required = false) String userType,
                              @RequestAttribute(value = "departmentId", required = false) Integer userDepartmentId) {
        if (file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        if (operator == null || operator.isEmpty()) {
            operator = "unknown";
        }
        Integer finalDepartmentId = paramDepartmentId;
        // 如果是教师或院长，强制使用自己的学院
        if (("teacher".equals(userType) || "dean".equals(userType)) && userDepartmentId != null) {
            finalDepartmentId = userDepartmentId;
        }
        // 如果是管理员但没有选择学院，返回错误
        if ("admin".equals(userType) && finalDepartmentId == null) {
            return "上传失败：请先选择学院";
        }
        return gradeService.uploadExcel(file, operator, finalDepartmentId);
    }

    @GetMapping("/records")
    public List<com.auggie.student_server.entity.ScoreImportRecord> getImportRecords(
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        // admin 可以查看所有记录
        if ("admin".equals(userType)) {
            return gradeService.findAllRecords();
        }
        // 如果是院长或教师，只返回自己学院的记录
        if (("dean".equals(userType) || "teacher".equals(userType)) && departmentId != null) {
            return gradeService.findRecordsByDepartmentId(departmentId);
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

    @PostMapping("/query")
    public List<SCTInfo> queryGrades(@RequestBody Map<String, Object> map,
                                     @RequestAttribute(value = "userType", required = false) String userType,
                                     @RequestAttribute(value = "departmentId", required = false) Integer departmentId,
                                     @RequestAttribute(value = "operator", required = false) String operator) {
        // 如果是院长或教师，强制添加 departmentId 过滤，确保只能查询自己学院的数据
        if (("dean".equals(userType) || "teacher".equals(userType)) && departmentId != null) {
            map.put("departmentId", departmentId);
        }
        // 如果是教师，还需要添加教师ID过滤，确保只能查询自己授课的课程成绩
        if ("teacher".equals(userType) && operator != null) {
            // 通过操作者姓名查找教师ID
            Integer teacherId = getTeacherIdByName(operator);
            if (teacherId != null) {
                map.put("tid", teacherId);
            }
        }
        return sctService.findBySearch(map);
    }
    
    /**
     * 通过教师姓名查找教师ID
     */
    private Integer getTeacherIdByName(String teacherName) {
        if (teacherName == null || teacherName.isEmpty()) {
            return null;
        }
        try {
            List<Teacher> teachers = teacherMapper.findBySearch(null, teacherName, 0);
            if (teachers != null && !teachers.isEmpty()) {
                return teachers.get(0).getId();
            }
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
