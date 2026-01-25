package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.GradeChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 成绩审计表 grade_change_log。仅追加，禁止 DELETE/UPDATE。
 */
@Mapper
@Repository
public interface GradeChangeLogMapper {

    int insert(GradeChangeLog log);

    List<GradeChangeLog> findByScoreId(@Param("scoreId") Integer scoreId);
}
