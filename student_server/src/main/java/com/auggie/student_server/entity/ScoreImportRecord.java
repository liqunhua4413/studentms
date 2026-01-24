package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 成绩单导入记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("ScoreImportRecord")
public class ScoreImportRecord {
    private Long id;
    private String fileName;
    private String filePath;
    private String term;
    private Integer courseId;
    private Integer teacherId;
    private Integer departmentId;
    private String operator;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}


