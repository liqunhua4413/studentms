package com.auggie.student_server.service;

import com.auggie.student_server.entity.GradeLevel;
import com.auggie.student_server.mapper.GradeLevelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeLevelService {
    @Autowired
    private GradeLevelMapper gradeLevelMapper;

    public List<GradeLevel> findAll() {
        return gradeLevelMapper.findAll();
    }

    public GradeLevel findById(Integer id) {
        return gradeLevelMapper.findById(id);
    }

    public GradeLevel findByName(String name) {
        return gradeLevelMapper.findByName(name);
    }
}
