package com.auggie.student_server.common;

/**
 * 成绩主表 score.status 枚举。
 * 上传后=已上传(UPLOADED)，已入库，等待管理员审核发布；发布后=已发布(PUBLISHED)。
 */
public final class ScoreStatus {
    /** 已上传：成绩已入库，等待管理员审核发布，学生不可见 */
    public static final String UPLOADED = "UPLOADED";
    /** 已发布：管理员发布后生效，学生可见 */
    public static final String PUBLISHED = "PUBLISHED";
    /** 锁定：禁止再修改 */
    public static final String LOCKED = "LOCKED";

    private ScoreStatus() {}
}
