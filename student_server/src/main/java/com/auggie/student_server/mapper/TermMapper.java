package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.Term;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学期表 term。供成绩、开课等模块使用。
 */
@Mapper
@Repository
public interface TermMapper {

    List<Term> findAll();

    Term findById(@Param("id") Integer id);

    Term findByName(@Param("name") String name);

    int insert(Term term);

    int updateById(Term term);

    int deleteById(@Param("id") Integer id);
}
