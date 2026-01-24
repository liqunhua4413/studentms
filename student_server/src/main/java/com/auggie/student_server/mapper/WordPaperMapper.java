package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.WordPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: WordPaperMapper
 * @Version 1.0.0
 */

@Mapper
@Repository
public interface WordPaperMapper {
    List<WordPaper> findAll();
    WordPaper findById(@Param("id") Long id);
    List<WordPaper> findBySearch(@Param("fileName") String fileName, @Param("uploadBy") String uploadBy);
    List<WordPaper> findByDepartmentId(@Param("departmentId") Integer departmentId);
    boolean save(@Param("paper") WordPaper paper);
    boolean deleteById(@Param("id") Long id);
}
