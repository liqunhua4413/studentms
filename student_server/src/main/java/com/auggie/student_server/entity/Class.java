package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: Class - 班级实体类
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("Class")
public class Class {
    private Integer id;
    private String name;
    private Integer majorId;
    private Integer departmentId;
}
