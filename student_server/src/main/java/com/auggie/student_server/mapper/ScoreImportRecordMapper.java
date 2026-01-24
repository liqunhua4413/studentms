package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.ScoreImportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ScoreImportRecordMapper {

    boolean save(@Param("record") ScoreImportRecord record);

    java.util.List<ScoreImportRecord> findAll();
    
    java.util.List<ScoreImportRecord> findByDepartmentId(@Param("departmentId") Integer departmentId);

    boolean deleteById(@Param("id") Long id);

    ScoreImportRecord findById(@Param("id") Long id);
}

