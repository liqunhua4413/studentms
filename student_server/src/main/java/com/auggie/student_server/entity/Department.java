package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: Department - 学院实体类
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("Department")
public class Department {
    private Integer id;
    private String name;
}
