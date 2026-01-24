package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: OperationLogMapper
 * @Version 1.0.0
 */

@Mapper
@Repository
public interface OperationLogMapper {
    List<OperationLog> findAll();
    OperationLog findById(@Param("id") Long id);
    List<OperationLog> findBySearch(@Param("operator") String operator,
                                    @Param("operationType") String operationType,
                                    @Param("targetTable") String targetTable,
                                    @Param("startTime") String startTime,
                                    @Param("endTime") String endTime);
    List<OperationLog> findByDepartmentId(@Param("departmentId") Integer departmentId);
    boolean save(@Param("log") OperationLog log);
}
