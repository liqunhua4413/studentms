package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: Major - 专业实体类
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("Major")
public class Major {
    private Integer id;
    private String name;
    private Integer departmentId;
}
