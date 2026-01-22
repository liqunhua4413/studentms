package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: DepartmentMapper
 * @Version 1.0.0
 */

@Mapper
@Repository
public interface DepartmentMapper {
    List<Department> findAll();
    Department findById(@Param("id") Integer id);
    List<Department> findBySearch(@Param("name") String name);
    boolean updateById(@Param("department") Department department);
    boolean save(@Param("department") Department department);
    boolean deleteById(@Param("id") Integer id);
}
