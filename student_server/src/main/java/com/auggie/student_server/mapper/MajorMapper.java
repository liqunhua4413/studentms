package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: MajorMapper
 * @Version 1.0.0
 */

@Mapper
@Repository
public interface MajorMapper {
    List<Major> findAll();
    Major findById(@Param("id") Integer id);
    
    /** 按专业名称+学院ID精确查询（专业在学院内唯一） */
    Major findByNameAndDepartmentId(@Param("name") String name, @Param("departmentId") Integer departmentId);
    
    List<Major> findBySearch(@Param("name") String name, @Param("departmentId") Integer departmentId);
    List<Major> findByDepartmentId(@Param("departmentId") Integer departmentId);
    boolean updateById(@Param("major") Major major);
    boolean save(@Param("major") Major major);
    boolean deleteById(@Param("id") Integer id);
}
