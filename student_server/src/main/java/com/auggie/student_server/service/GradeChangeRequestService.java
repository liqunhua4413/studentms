package com.auggie.student_server.service;

import com.auggie.student_server.common.GradeChangeRequestStatus;
import com.auggie.student_server.common.ScoreStatus;
import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.GradeChangeRequest;
import com.auggie.student_server.entity.GradeChangeRequestWithDetail;
import com.auggie.student_server.entity.Score;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.CourseOpenMapper;
import com.auggie.student_server.mapper.GradeChangeRequestMapper;
import com.auggie.student_server.mapper.ScoreMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩修改申请与审批服务。
 * 提交 → PENDING；院长/管理员审批 → APPROVED / REJECTED；仅 APPROVED 时更新 score 并写 grade_change_log。
 * 权限：教师/院长可提交；院长可一级审批；管理员终审。教师只能申请本人任课；院长只能审批本院。
 */
@Service
public class GradeChangeRequestService {

    @Autowired
    private GradeChangeRequestMapper gradeChangeRequestMapper;
    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private CourseOpenMapper courseOpenMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private GradeChangeLogService gradeChangeLogService;
    @Autowired
    private OperationLogService operationLogService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 提交成绩修改申请。状态 PENDING。
     * 权限判断：教师仅能申请本人任课课程（course_open）；院长仅能申请本学院课程（course.department_id）；admin 可申请全部。
     * 状态判断：仅当 score.status = PUBLISHED 时允许申请；不允许改已上传/锁定。
     *
     * @param scoreId       目标成绩 id
     * @param beforeData   原成绩JSON：{usual_score, mid_score, final_score, grade}
     * @param afterData    目标成绩JSON：{usual_score, mid_score, final_score, grade}
     * @param reason        申请原因
     * @param attachmentPath 证明附件相对路径，可选
     * @param attachmentName 证明附件原始文件名，可选
     * @param applicantId   申请人 id（教师/院长/admin）
     * @param applicantRole teacher / dean / admin
     * @param applicantDeptId 申请人学院 id（院长必有）
     * @param applicantName 申请人姓名（用于日志）
     * @return 成功或错误信息
     */
    public String submitRequest(Integer scoreId, String beforeData, String afterData, String reason, String attachmentPath, String attachmentName,
                               Integer applicantId, String applicantRole, Integer applicantDeptId, String applicantName) {
        if (scoreId == null || beforeData == null || afterData == null || reason == null || reason.trim().isEmpty()) {
            return "提交失败：成绩 id、原成绩、目标成绩、申请原因均必填";
        }
        Score s = scoreMapper.findById(scoreId);
        if (s == null) {
            return "提交失败：成绩记录不存在";
        }
        if (!ScoreStatus.PUBLISHED.equals(s.getStatus())) {
            return "提交失败：仅可对已发布成绩申请修改（当前状态：" + s.getStatus() + "）";
        }

        /* 权限：教师仅本人任课；院长仅本院课程；admin 全部 */
        if ("teacher".equals(applicantRole)) {
            if (applicantId == null) return "提交失败：教师身份未识别";
            if (courseOpenMapper.countByTeacherCourseTerm(applicantId, s.getCourseId(), s.getTerm()) <= 0) {
                return "提交失败：权限不足，仅可申请本人任课课程";
            }
        } else if ("dean".equals(applicantRole)) {
            if (applicantDeptId == null) return "提交失败：院长学院未识别";
            Course course = courseMapper.findByCourseId(s.getCourseId());
            if (course == null || !applicantDeptId.equals(course.getDepartmentId())) {
                return "提交失败：权限不足，仅可申请本学院课程";
            }
        } else if (!"admin".equals(applicantRole)) {
            return "提交失败：权限不足";
        }

        // 验证成绩是否有变化
        try {
            Map<String, Object> before = objectMapper.readValue(beforeData, Map.class);
            Map<String, Object> after = objectMapper.readValue(afterData, Map.class);
            boolean hasChange = false;
            for (String key : new String[]{"usual_score", "mid_score", "final_score", "grade"}) {
                Object beforeVal = before.get(key);
                Object afterVal = after.get(key);
                if (beforeVal == null && afterVal == null) continue;
                if (beforeVal == null || afterVal == null) {
                    hasChange = true;
                    break;
                }
                double beforeNum = beforeVal instanceof Number ? ((Number) beforeVal).doubleValue() : Double.parseDouble(beforeVal.toString());
                double afterNum = afterVal instanceof Number ? ((Number) afterVal).doubleValue() : Double.parseDouble(afterVal.toString());
                if (Math.abs(beforeNum - afterNum) > 0.01) {
                    hasChange = true;
                    break;
                }
            }
            if (!hasChange) {
                return "提交失败：未检测到修改，请至少修改一项成绩";
            }
        } catch (Exception e) {
            return "提交失败：成绩数据格式错误";
        }

        GradeChangeRequest req = new GradeChangeRequest();
        req.setScoreId(scoreId);
        req.setApplicantId(applicantId);
        req.setApplicantRole(applicantRole);
        req.setReason(reason.trim());
        req.setAttachmentPath(attachmentPath);
        req.setAttachmentName(attachmentName);
        req.setBeforeData(beforeData);
        req.setAfterData(afterData);
        req.setStatus(GradeChangeRequestStatus.PENDING);
        gradeChangeRequestMapper.insert(req);

        // 写入 grade_change_log（REQUEST 操作）
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("request_id", req.getId());
            logData.put("before_data", objectMapper.readValue(beforeData, Map.class));
            logData.put("after_data", objectMapper.readValue(afterData, Map.class));
            logData.put("reason", reason.trim());
            String logJson = objectMapper.writeValueAsString(logData);
            gradeChangeLogService.appendLog(scoreId, "REQUEST", applicantId, applicantName, beforeData, logJson);
        } catch (Exception e) {
            // 日志写入失败不影响申请提交
            System.err.println("写入申请日志失败: " + e.getMessage());
            e.printStackTrace();
        }

        operationLogService.recordOperation(applicantName, "新增", "grade_change_request", req.getId(),
                "提交成绩修改申请 scoreId=" + scoreId);
        return "申请已提交，状态 PENDING，等待审批";
    }

    /**
     * 院长一级审批。可选：通过 → DEAN_APPROVED；拒绝 → REJECTED。
     * 权限判断：仅能审批本学院课程的申请（通过 score → course.department_id）。
     *
     * @param requestId    申请 id
     * @param approved     true 通过，false 拒绝
     * @param rejectReason 拒绝原因（拒绝时必填）
     * @param deanId       院长 id
     * @param deanName     院长姓名
     * @param deanDeptId   院长学院 id
     * @return 成功或错误信息
     */
    public String deanApprove(Long requestId, boolean approved, String rejectReason,
                              Integer deanId, String deanName, Integer deanDeptId) {
        if (requestId == null || deanId == null || deanDeptId == null) {
            return "审批失败：参数缺失";
        }
        GradeChangeRequest req = gradeChangeRequestMapper.findById(requestId);
        if (req == null) return "审批失败：申请不存在";
        if (!GradeChangeRequestStatus.PENDING.equals(req.getStatus())) {
            return "审批失败：仅可审批 PENDING 状态申请";
        }
        Score sc = scoreMapper.findById(req.getScoreId());
        if (sc == null) return "审批失败：关联成绩不存在";
        Course course = courseMapper.findByCourseId(sc.getCourseId());
        if (course == null || !deanDeptId.equals(course.getDepartmentId())) {
            return "审批失败：权限不足，仅可审批本学院申请";
        }
        if (!approved && (rejectReason == null || rejectReason.trim().isEmpty())) {
            return "拒绝时须填写拒绝原因";
        }
        req.setStatus(approved ? GradeChangeRequestStatus.DEAN_APPROVED : GradeChangeRequestStatus.REJECTED);
        req.setDeanApproverId(deanId);
        req.setRejectReason(rejectReason != null ? rejectReason.trim() : null);
        gradeChangeRequestMapper.updateById(req);
        operationLogService.recordOperation(deanName, "编辑", "grade_change_request", requestId,
                (approved ? "院长通过" : "院长拒绝") + " 申请");
        return approved ? "院长审批已通过，待管理员终审" : "已拒绝";
    }

    /**
     * 管理员终审。通过 → 校验状态 PENDING、更新 score、写 grade_change_log、operation_log、申请 APPROVED；拒绝 → 仅更新申请状态。
     * 仅 admin 可调用。全过程事务，任一步失败回滚。
     * 仅可审批 PENDING 状态的申请。
     */
    @Transactional(rollbackFor = Exception.class)
    public String adminApprove(Long requestId, boolean approved, String rejectReason,
                               Integer adminId, String adminName) {
        if (requestId == null) return "审批失败：申请 id 缺失";
        GradeChangeRequest req = gradeChangeRequestMapper.findById(requestId);
        if (req == null) return "审批失败：申请不存在";
        if (!GradeChangeRequestStatus.PENDING.equals(req.getStatus())) {
            return "审批失败：仅可审批待审批（PENDING）状态的申请";
        }
        if (!approved && (rejectReason == null || rejectReason.trim().isEmpty())) {
            return "拒绝时须填写拒绝原因";
        }

        req.setStatus(approved ? GradeChangeRequestStatus.APPROVED : GradeChangeRequestStatus.REJECTED);
        req.setAdminApproverId(adminId);
        req.setRejectReason(rejectReason != null ? rejectReason.trim() : null);
        gradeChangeRequestMapper.updateById(req);

        if (approved) {
            Score sc = scoreMapper.findById(req.getScoreId());
            if (sc == null) return "审批失败：关联成绩不存在";
            Score before = new Score();
            before.setId(sc.getId());
            before.setUsualScore(sc.getUsualScore());
            before.setMidScore(sc.getMidScore());
            before.setFinalScore(sc.getFinalScore());
            before.setGrade(sc.getGrade());
            before.setStatus(sc.getStatus());
            
            // 从 afterData JSON 中读取四个成绩字段
            try {
                if (req.getAfterData() != null) {
                    Map<String, Object> after = objectMapper.readValue(req.getAfterData(), Map.class);
                    if (after.containsKey("usual_score")) {
                        Object val = after.get("usual_score");
                        sc.setUsualScore(val != null ? new BigDecimal(val.toString()) : null);
                    }
                    if (after.containsKey("mid_score")) {
                        Object val = after.get("mid_score");
                        sc.setMidScore(val != null ? new BigDecimal(val.toString()) : null);
                    }
                    if (after.containsKey("final_score")) {
                        Object val = after.get("final_score");
                        sc.setFinalScore(val != null ? new BigDecimal(val.toString()) : null);
                    }
                    if (after.containsKey("grade")) {
                        Object val = after.get("grade");
                        if (val != null) {
                            BigDecimal grade = new BigDecimal(val.toString());
                            // 总分四舍五入为整数
                            grade = grade.setScale(0, java.math.RoundingMode.HALF_UP);
                            sc.setGrade(grade);
                        }
                    }
                }
            } catch (Exception e) {
                return "审批失败：成绩数据格式错误";
            }
            
            scoreMapper.updateById(sc);
            gradeChangeLogService.logChange(before, sc, adminId, adminName);
        }

        operationLogService.recordOperation(adminName, "编辑", "grade_change_request", requestId,
                (approved ? "管理员通过并更新 score、写 grade_change_log" : "管理员拒绝"));
        return approved ? "审批通过，成绩已更新并写入审计" : "已拒绝";
    }

    /**
     * 查询本人提交的申请（教师/院长）。
     */
    public List<GradeChangeRequest> findByApplicant(Integer applicantId) {
        return gradeChangeRequestMapper.findByApplicant(applicantId);
    }

    /**
     * 查询本人提交的申请（含学号、姓名、课程、学期等展示字段）。
     */
    public List<GradeChangeRequestWithDetail> findByApplicantWithDetails(Integer applicantId) {
        return gradeChangeRequestMapper.findByApplicantWithDetails(applicantId);
    }

    /**
     * 按状态查询（管理员查 PENDING/DEAN_APPROVED 待审）。
     */
    public List<GradeChangeRequest> findByStatus(String status) {
        return gradeChangeRequestMapper.findByStatus(status);
    }

    /**
     * 院长查本院申请。
     */
    public List<GradeChangeRequest> findByDepartment(Integer departmentId) {
        return gradeChangeRequestMapper.findByDepartmentId(departmentId);
    }

    public List<GradeChangeRequestWithDetail> findByStatusWithDetails(String status) {
        return gradeChangeRequestMapper.findByStatusWithDetails(status);
    }

    public List<GradeChangeRequestWithDetail> findByStatusInWithDetails(List<String> statuses) {
        return gradeChangeRequestMapper.findByStatusInWithDetails(statuses);
    }

    /**
     * 申请人重新编辑并再次提交（仅 REJECTED 可操作）。更新原申请，状态回到 PENDING。
     */
    public String resubmit(Long requestId, String beforeData, String afterData, String reason, String attachmentPath, String attachmentName,
                          Integer applicantId, String applicantName) {
        if (requestId == null) return "提交失败：申请 id 缺失";
        GradeChangeRequest req = gradeChangeRequestMapper.findById(requestId);
        if (req == null) return "提交失败：申请不存在";
        if (!GradeChangeRequestStatus.REJECTED.equals(req.getStatus())) {
            return "仅被拒绝的申请可重新提交";
        }
        if (!req.getApplicantId().equals(applicantId)) {
            return "仅申请人本人可重新提交";
        }
        if (beforeData == null || afterData == null || reason == null || reason.trim().isEmpty()) {
            return "原成绩、修改后成绩、申请原因均必填";
        }
        try {
            Map<String, Object> before = objectMapper.readValue(beforeData, Map.class);
            Map<String, Object> after = objectMapper.readValue(afterData, Map.class);
            boolean hasChange = false;
            for (String key : new String[]{"usual_score", "mid_score", "final_score", "grade"}) {
                Object b = before.get(key), a = after.get(key);
                if (b == null && a == null) continue;
                if (b == null || a == null) { hasChange = true; break; }
                double bd = ((Number) b).doubleValue(), ad = ((Number) a).doubleValue();
                if (Math.abs(bd - ad) > 0.01) { hasChange = true; break; }
            }
            if (!hasChange) return "提交失败：未检测到修改，请至少修改一项成绩";
        } catch (Exception e) {
            return "提交失败：成绩数据格式错误";
        }
        int n = gradeChangeRequestMapper.updateForResubmit(requestId, beforeData, afterData, reason.trim(), attachmentPath, attachmentName);
        if (n <= 0) return "更新失败";
        operationLogService.recordOperation(applicantName, "编辑", "grade_change_request", requestId, "重新提交申请");
        return "已重新提交，状态已更新为待审批";
    }
}
