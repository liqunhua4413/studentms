package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * 年级表。独立表便于报表、权限分级、统计及长期运行。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("GradeLevel")
public class GradeLevel {
    private Integer id;
    private String name;
    private Integer sortOrder;

    // 手动添加 getter/setter 方法以确保编译通过
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
