package com.auggie.student_server.service;

import com.auggie.student_server.entity.Class;
import com.auggie.student_server.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: ClassService
 * @Version 1.0.0
 */

@Service
public class ClassService {
    @Autowired
    private ClassMapper classMapper;

    public List<Class> findAll() {
        return classMapper.findAll();
    }

    public Class findById(Integer id) {
        return classMapper.findById(id);
    }

    public List<Class> findBySearch(String name, Integer majorId, Integer departmentId) {
        return classMapper.findBySearch(name, majorId, departmentId);
    }

    public List<Class> findByMajorId(Integer majorId) {
        return classMapper.findByMajorId(majorId);
    }

    public boolean updateById(Class clazz) {
        return classMapper.updateById(clazz);
    }

    public boolean save(Class clazz) {
        return classMapper.save(clazz);
    }

    public boolean deleteById(Integer id) {
        return classMapper.deleteById(id);
    }
}
