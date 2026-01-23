package com.auggie.student_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface InitMapper {

    void clearTestData();

    void insertInitialData();

}

