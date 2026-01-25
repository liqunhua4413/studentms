package com.auggie.student_server.common;

/**
 * 成绩修改申请 grade_change_request.status 枚举。
 * 审批流：PENDING → DEAN_APPROVED(可选) → APPROVED / REJECTED。
 */
public final class GradeChangeRequestStatus {
    /** 待审 */
    public static final String PENDING = "PENDING";
    /** 院长已通过（一级审批） */
    public static final String DEAN_APPROVED = "DEAN_APPROVED";
    /** 管理员通过（终审） */
    public static final String APPROVED = "APPROVED";
    /** 拒绝 */
    public static final String REJECTED = "REJECTED";

    private GradeChangeRequestStatus() {}
}
