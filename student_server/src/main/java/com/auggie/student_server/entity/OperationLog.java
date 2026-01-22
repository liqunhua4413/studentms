package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: OperationLog - 操作日志实体类
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("OperationLog")
public class OperationLog {
    private Long id;
    private String operator;
    private String operationType;  // INSERT, UPDATE, DELETE
    private String targetTable;
    private Long targetId;
    private String content;
    private LocalDateTime createTime;
}
