package com.auggie.student_server.common;

/**
 * 成绩审计表 grade_change_log.operation 枚举。
 * 每次导入、发布、修改均须写入审计。
 */
public final class GradeChangeLogOperation {
    /** 导入：上传 Excel 写入 score */
    public static final String IMPORT = "IMPORT";
    /** 发布：管理员将已上传成绩发布 */
    public static final String PUBLISH = "PUBLISH";
    /** 修改：审批通过后更新 score */
    public static final String CHANGE = "CHANGE";

    private GradeChangeLogOperation() {}
}
