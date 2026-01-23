package com.auggie.student_server.service;

import com.auggie.student_server.mapper.InitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InitService {

    @Autowired
    private InitMapper initMapper;

    @Transactional
    public void clearTestData() {
        initMapper.clearTestData();
    }

    @Transactional
    public void insertInitialData() {
        initMapper.insertInitialData();
    }
}

