package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.GradeLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface GradeLevelMapper {
    List<GradeLevel> findAll();

    GradeLevel findById(@Param("id") Integer id);

    GradeLevel findByName(@Param("name") String name);

    int insert(GradeLevel gradeLevel);

    int updateById(GradeLevel gradeLevel);

    int deleteById(@Param("id") Integer id);
}
