package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.GradeChangeRequest;
import com.auggie.student_server.entity.GradeChangeRequestWithDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 成绩修改申请 grade_change_request。审批流控制。
 */
@Mapper
@Repository
public interface GradeChangeRequestMapper {

    int insert(GradeChangeRequest req);

    int updateById(GradeChangeRequest req);

    GradeChangeRequest findById(@Param("id") Long id);

    List<GradeChangeRequest> findByApplicant(@Param("applicantId") Integer applicantId);

    List<GradeChangeRequest> findByStatus(@Param("status") String status);

    List<GradeChangeRequest> findByDepartmentId(@Param("departmentId") Integer departmentId);

    List<GradeChangeRequestWithDetail> findByApplicantWithDetails(@Param("applicantId") Integer applicantId);

    List<GradeChangeRequestWithDetail> findByStatusWithDetails(@Param("status") String status);

    List<GradeChangeRequestWithDetail> findByStatusInWithDetails(@Param("statuses") List<String> statuses);

    int updateForResubmit(@Param("id") Long id, @Param("beforeData") String beforeData, @Param("afterData") String afterData,
                          @Param("reason") String reason, @Param("attachmentPath") String attachmentPath, @Param("attachmentName") String attachmentName);
}
