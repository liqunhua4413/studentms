package com.auggie.student_server.controller;

import com.auggie.student_server.entity.GradeChangeRequest;
import com.auggie.student_server.entity.GradeChangeRequestWithDetail;
import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.mapper.GradeChangeRequestMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import com.auggie.student_server.service.GradeChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 成绩修改申请与审批控制器。严格按权限与审批流。
 * 提交 → PENDING；院长/管理员审批 → APPROVED / REJECTED。
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/grade/change")
public class GradeChangeRequestController {

    @Autowired
    private GradeChangeRequestService gradeChangeRequestService;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private GradeChangeRequestMapper gradeChangeRequestMapper;
    @Value("${file.upload.path:${user.home}/studentms/uploads}")
    private String uploadPath;

    /**
     * 提交成绩修改申请。教师/院长可调用。
     * 权限：教师仅本人任课；院长仅本院课程。状态：仅 PUBLISHED 可申请。
     */
    @PostMapping("/request")
    public Map<String, Object> submit(@RequestBody Map<String, Object> body,
                                      @RequestAttribute(value = "operator", required = false) String operator,
                                      @RequestAttribute(value = "userType", required = false) String userType,
                                      @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        Integer scoreId = body.get("scoreId") != null ? Integer.parseInt(body.get("scoreId").toString()) : null;
        String beforeData = body.get("beforeData") != null ? body.get("beforeData").toString() : null;
        String afterData = body.get("afterData") != null ? body.get("afterData").toString() : null;
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        String attachmentPath = body.get("attachmentPath") != null ? body.get("attachmentPath").toString() : null;
        String attachmentName = body.get("attachmentName") != null ? body.get("attachmentName").toString() : null;

        Integer applicantId = resolveTeacherId(operator);
        String applicantRole = "admin".equals(userType) ? "admin" : ("dean".equals(userType) ? "dean" : "teacher");
        if (("teacher".equals(userType) || "dean".equals(userType)) && applicantId == null) {
            return Map.of("success", false, "message", "无法识别申请人身份，请重新登录");
        }
        if ("admin".equals(userType) && applicantId == null) {
            applicantId = 1; // 默认 admin id
        }
        String msg = gradeChangeRequestService.submitRequest(
                scoreId, beforeData, afterData, reason, attachmentPath, attachmentName,
                applicantId, applicantRole, departmentId, operator != null ? operator : "unknown");
        return Map.of("success", msg.startsWith("申请已提交"), "message", msg);
    }

    /**
     * 院长一级审批。仅院长可调用。
     * 权限：仅可审批本院申请。
     */
    @PostMapping("/dean-approve")
    public Map<String, Object> deanApprove(@RequestBody Map<String, Object> body,
                                           @RequestAttribute(value = "operator", required = false) String operator,
                                           @RequestAttribute(value = "userType", required = false) String userType,
                                           @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        if (!"dean".equals(userType)) {
            return Map.of("success", false, "message", "权限不足：仅院长可审批");
        }
        Long requestId = body.get("requestId") != null ? Long.parseLong(body.get("requestId").toString()) : null;
        Boolean approved = body.get("approved") != null && Boolean.parseBoolean(body.get("approved").toString());
        String rejectReason = body.get("rejectReason") != null ? body.get("rejectReason").toString() : null;
        Integer deanId = resolveTeacherId(operator);
        if (deanId == null || departmentId == null) {
            return Map.of("success", false, "message", "院长身份或学院未识别");
        }
        String msg = gradeChangeRequestService.deanApprove(
                requestId, approved, rejectReason, deanId, operator != null ? operator : "unknown", departmentId);
        return Map.of("success", !msg.contains("失败") && !msg.equals("已拒绝"), "message", msg);
    }

    /**
     * 管理员终审。仅 admin 可调用。通过则更新 score 并写 grade_change_log。
     */
    @PostMapping("/admin-approve")
    public Map<String, Object> adminApprove(@RequestBody Map<String, Object> body,
                                            @RequestAttribute(value = "operator", required = false) String operator,
                                            @RequestAttribute(value = "userType", required = false) String userType) {
        if (!"admin".equals(userType)) {
            return Map.of("success", false, "message", "权限不足：仅管理员可终审");
        }
        Long requestId = body.get("requestId") != null ? Long.parseLong(body.get("requestId").toString()) : null;
        Boolean approved = body.get("approved") != null && Boolean.parseBoolean(body.get("approved").toString());
        String rejectReason = body.get("rejectReason") != null ? body.get("rejectReason").toString() : null;
        Integer adminId = resolveTeacherId(operator);
        String msg = gradeChangeRequestService.adminApprove(
                requestId, approved, rejectReason, adminId, operator != null ? operator : "系统管理员");
        return Map.of("success", msg.startsWith("审批通过") || msg.equals("已拒绝"), "message", msg);
    }

    /**
     * 查询本人提交的申请（教师/院长）。
     */
    @GetMapping("/my")
    public List<GradeChangeRequest> myRequests(@RequestAttribute(value = "operator", required = false) String operator,
                                               @RequestAttribute(value = "userType", required = false) String userType) {
        Integer applicantId = resolveTeacherId(operator);
        if (applicantId == null) return List.of();
        return gradeChangeRequestService.findByApplicant(applicantId);
    }

    /**
     * 查询本人提交的申请（含学号、姓名、课程、学期等），供「我的申请」列表使用。
     */
    @GetMapping("/my-details")
    public List<GradeChangeRequestWithDetail> myRequestsWithDetails(
            @RequestAttribute(value = "operator", required = false) String operator,
            @RequestAttribute(value = "userType", required = false) String userType) {
        Integer applicantId = resolveTeacherId(operator);
        if (applicantId == null) return List.of();
        return gradeChangeRequestService.findByApplicantWithDetails(applicantId);
    }

    /**
     * 按状态查询（管理员查 PENDING/DEAN_APPROVED）。
     */
    @GetMapping("/by-status")
    public List<GradeChangeRequest> byStatus(@RequestParam String status,
                                             @RequestAttribute(value = "userType", required = false) String userType) {
        return gradeChangeRequestService.findByStatus(status);
    }

    /**
     * 院长查本院申请。
     */
    @GetMapping("/by-department")
    public List<GradeChangeRequest> byDepartment(@RequestAttribute(value = "departmentId", required = false) Integer departmentId,
                                                 @RequestAttribute(value = "userType", required = false) String userType) {
        if (!"dean".equals(userType) || departmentId == null) return List.of();
        return gradeChangeRequestService.findByDepartment(departmentId);
    }

    /**
     * 审批管理列表（仅 admin）。tab=pending 待审批，tab=approved 已审批（通过+拒绝）。
     */
    @GetMapping("/admin-list")
    public List<GradeChangeRequestWithDetail> adminList(
            @RequestParam(value = "tab", defaultValue = "pending") String tab,
            @RequestAttribute(value = "userType", required = false) String userType) {
        if (!"admin".equals(userType)) return List.of();
        if ("approved".equals(tab)) {
            return gradeChangeRequestService.findByStatusInWithDetails(Arrays.asList("APPROVED", "REJECTED"));
        }
        return gradeChangeRequestService.findByStatusWithDetails("PENDING");
    }

    /**
     * 附件下载/预览。admin 或 申请人可访问。inline=1 时用于预览（Content-Disposition: inline）。
     */
    @GetMapping("/attachment")
    public ResponseEntity<byte[]> attachment(
            @RequestParam Long requestId,
            @RequestParam(value = "inline", required = false) Boolean inline,
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "operator", required = false) String operator) throws IOException {
        GradeChangeRequest req = gradeChangeRequestMapper.findById(requestId);
        if (req == null || req.getAttachmentPath() == null || req.getAttachmentPath().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Integer applicantId = resolveTeacherId(operator);
        boolean isAdmin = "admin".equals(userType);
        boolean isApplicant = applicantId != null && applicantId.equals(req.getApplicantId());
        if (!isAdmin && !isApplicant) {
            return ResponseEntity.status(403).build();
        }
        String raw = uploadPath;
        if (raw.startsWith("./") || raw.startsWith(".\\")) {
            raw = new java.io.File(".").getCanonicalPath() + java.io.File.separator + raw.substring(2);
        }
        Path base = Paths.get(raw);
        Path file = base.resolve(req.getAttachmentPath()).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = Files.readAllBytes(file);
        String fn = req.getAttachmentName();
        if (fn == null || fn.isEmpty()) {
            fn = file.getFileName().toString();
        }
        HttpHeaders headers = new HttpHeaders();
        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        String lower = fn.toLowerCase();
        if (lower.endsWith(".pdf")) mt = MediaType.parseMediaType("application/pdf");
        else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) mt = MediaType.IMAGE_JPEG;
        else if (lower.endsWith(".png")) mt = MediaType.IMAGE_PNG;
        else if (lower.endsWith(".gif")) mt = MediaType.IMAGE_GIF;
        else if (lower.endsWith(".webp")) mt = MediaType.parseMediaType("image/webp");
        else if (lower.endsWith(".bmp")) mt = MediaType.parseMediaType("image/bmp");
        headers.setContentType(mt);
        if (Boolean.TRUE.equals(inline)) {
            headers.set("Content-Disposition", "inline; filename=\"" + java.net.URLEncoder.encode(fn, "UTF-8") + "\"");
        } else {
            headers.setContentDispositionFormData("attachment", java.net.URLEncoder.encode(fn, "UTF-8"));
        }
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * 上传附件（申请/重新提交时可选）。返回 { path, fileName }。
     * 存储路径：grades/change_request/YYYY/MM/UUID_yyyyMMdd.ext
     */
    @PostMapping("/upload-attachment")
    public Map<String, Object> uploadAttachment(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Map.of("success", false, "message", "请选择文件");
        }
        String orig = file.getOriginalFilename();
        if (orig == null || orig.isEmpty()) {
            return Map.of("success", false, "message", "文件名无效");
        }
        String ext = "";
        if (orig.contains(".")) {
            ext = orig.substring(orig.lastIndexOf("."));
        }
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + "_" + dateStr + ext;
        String relativePath = "grades/change_request/" + year + "/" + month + "/" + fileName;
        String base = uploadPath;
        if (base.startsWith("./") || base.startsWith(".\\")) {
            base = new java.io.File(".").getCanonicalPath() + java.io.File.separator + base.substring(2);
        }
        Path fullPath = Paths.get(base, relativePath).normalize();
        Path basePath = Paths.get(base).normalize();
        if (!fullPath.startsWith(basePath)) {
            return Map.of("success", false, "message", "路径安全检查失败");
        }
        Files.createDirectories(fullPath.getParent());
        file.transferTo(fullPath.toFile());
        return Map.of("success", true, "path", relativePath, "fileName", orig);
    }

    /**
     * 重新提交（仅申请人，仅 REJECTED）。更新原申请，状态回到 PENDING。
     */
    @PostMapping("/resubmit")
    public Map<String, Object> resubmit(@RequestBody Map<String, Object> body,
                                        @RequestAttribute(value = "operator", required = false) String operator,
                                        @RequestAttribute(value = "userType", required = false) String userType) {
        Integer applicantId = resolveTeacherId(operator);
        if (applicantId == null) {
            return Map.of("success", false, "message", "无法识别申请人身份，请重新登录");
        }
        Long requestId = body.get("requestId") != null ? Long.parseLong(body.get("requestId").toString()) : null;
        String beforeData = body.get("beforeData") != null ? body.get("beforeData").toString() : null;
        String afterData = body.get("afterData") != null ? body.get("afterData").toString() : null;
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        String attachmentPath = body.get("attachmentPath") != null ? body.get("attachmentPath").toString() : null;
        String attachmentName = body.get("attachmentName") != null ? body.get("attachmentName").toString() : null;
        String msg = gradeChangeRequestService.resubmit(requestId, beforeData, afterData, reason, attachmentPath, attachmentName,
                applicantId, operator != null ? operator : "unknown");
        return Map.of("success", msg.startsWith("已重新提交"), "message", msg);
    }

    private Integer resolveTeacherId(String operator) {
        if (operator == null || operator.isEmpty()) return null;
        if ("admin".equals(operator) || "系统管理员".equals(operator)) {
            List<Teacher> admins = teacherMapper.findBySearch(null, "系统管理员", 0);
            if (admins != null && !admins.isEmpty()) return admins.get(0).getId();
        }
        List<Teacher> list = teacherMapper.findBySearch(null, operator, 0);
        return (list != null && !list.isEmpty()) ? list.get(0).getId() : null;
    }
}
