package com.auggie.student_server.service;

import com.auggie.student_server.entity.Major;
import com.auggie.student_server.mapper.MajorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: MajorService
 * @Version 1.0.0
 */

@Service
public class MajorService {
    @Autowired
    private MajorMapper majorMapper;

    public List<Major> findAll() {
        return majorMapper.findAll();
    }

    public Major findById(Integer id) {
        return majorMapper.findById(id);
    }

    public List<Major> findBySearch(String name, Integer departmentId) {
        return majorMapper.findBySearch(name, departmentId);
    }

    public List<Major> findByDepartmentId(Integer departmentId) {
        return majorMapper.findByDepartmentId(departmentId);
    }

    public boolean updateById(Major major) {
        return majorMapper.updateById(major);
    }

    public boolean save(Major major) {
        return majorMapper.save(major);
    }

    public boolean deleteById(Integer id) {
        return majorMapper.deleteById(id);
    }
}
