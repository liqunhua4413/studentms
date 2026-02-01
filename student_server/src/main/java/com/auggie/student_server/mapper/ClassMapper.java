package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Class;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: ClassMapper
 * @Version 1.0.0
 */

@Mapper
@Repository
public interface ClassMapper {
    List<Class> findAll();
    Class findById(@Param("id") Integer id);
    
    /** 按班级名称+专业ID+年级ID精确查询（班级在专业+年级内唯一） */
    Class findByNameAndMajorIdAndGradeLevelId(@Param("name") String name, @Param("majorId") Integer majorId, @Param("gradeLevelId") Integer gradeLevelId);
    
    List<Class> findBySearch(@Param("name") String name, @Param("gradeLevelId") Integer gradeLevelId, @Param("majorId") Integer majorId, @Param("departmentId") Integer departmentId);
    List<Class> findByMajorId(@Param("majorId") Integer majorId);
    boolean updateById(@Param("class") Class clazz);
    boolean save(@Param("class") Class clazz);
    boolean deleteById(@Param("id") Integer id);
}
