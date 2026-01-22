package com.auggie.student_server.service;

import com.auggie.student_server.entity.Department;
import com.auggie.student_server.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: DepartmentService
 * @Version 1.0.0
 */

@Service
public class DepartmentService {
    @Autowired
    private DepartmentMapper departmentMapper;

    public List<Department> findAll() {
        return departmentMapper.findAll();
    }

    public Department findById(Integer id) {
        return departmentMapper.findById(id);
    }

    public List<Department> findBySearch(String name) {
        return departmentMapper.findBySearch(name);
    }

    public boolean updateById(Department department) {
        return departmentMapper.updateById(department);
    }

    public boolean save(Department department) {
        return departmentMapper.save(department);
    }

    public boolean deleteById(Integer id) {
        return departmentMapper.deleteById(id);
    }
}
