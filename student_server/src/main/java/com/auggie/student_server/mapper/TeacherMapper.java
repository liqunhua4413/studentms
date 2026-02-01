package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Student;
import com.auggie.student_server.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2022/2/9 10:51
 * @Description: TeacherMapper
 * @Version 1.0.0
 */

@Repository
@Mapper
public interface TeacherMapper {
    //    select
    public List<Teacher> findAll();

    public Teacher findById(@Param("tid") Integer tid);

    /** 按 id 查询教师并关联学院名称（用于教师端展示本院等） */
    Teacher findByIdWithDepartment(@Param("tid") Integer tid);

    public Teacher findByTeacherNo(@Param("teacherNo") String teacherNo);

    public List<Teacher> findBySearch(@Param("tid") Integer tid, @Param("tname") String tname, @Param("fuzzy") Integer fuzzy);

    /** 按条件查询教师并关联学院名称（用于 Admin 教师列表展示角色、所属学院）；支持按所属学院、角色筛选 */
    List<Teacher> findBySearchWithDepartment(@Param("tid") Integer tid, @Param("tname") String tname, @Param("fuzzy") Integer fuzzy,
                                             @Param("departmentId") Integer departmentId, @Param("role") String role);

    /**
     * 根据姓名查询院长（role=dean），用于获取院长所属学院。
     */
    Teacher findDeanByTname(@Param("tname") String tname);

    /**
     * 根据学院ID查询该院院长（role=dean），用于成绩修改申请时解析 applicant_id。
     */
    Teacher findDeanByDepartmentId(@Param("departmentId") Integer departmentId);

    //    update
    public boolean updateById(@Param("teacher") Teacher teacher);

    //    insert
    public boolean save(@Param("teacher") Teacher teacher);

    //    delete
    public boolean deleteById(@Param("tid") Integer tid);

}
