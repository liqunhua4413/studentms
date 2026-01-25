package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2022/2/8 16:12
 * @Description: StudentMapper
 * @Version 1.0.0
 */

@Mapper
@Repository
public interface StudentMapper {

//    select
    public List<Student> findAll();

    public Student findById(@Param("sid") Integer sid);

    public Student findByStudentNo(@Param("studentNo") String studentNo);

    public List<Student> findBySearch(@Param("student") Student student, @Param("fuzzy") Integer fuzzy);

    /** 获取所有学生学院（去重） */
    java.util.List<java.util.Map<String, Object>> findDistinctColleges();

    /** 获取指定学院的学生专业（去重） */
    java.util.List<java.util.Map<String, Object>> findDistinctMajorsByCollegeId(@Param("collegeId") Integer collegeId);

    /** 获取指定专业的学生班级（去重） */
    java.util.List<java.util.Map<String, Object>> findDistinctClassesByMajorId(@Param("majorId") Integer majorId);

//    update
    public boolean updateById(@Param("student") Student student);

//    insert
    public boolean save(@Param("student") Student student);

//    delete
    public boolean deleteById(@Param("sid") Integer sid);

}
