package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Department;
import com.auggie.student_server.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: DepartmentController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @PostMapping("/add")
    public boolean addDepartment(@RequestBody Department department) {
        System.out.println("正在保存学院对象" + department);
        return departmentService.save(department);
    }

    @PostMapping("/findBySearch")
    public List<Department> findBySearch(@RequestBody Department department) {
        return departmentService.findBySearch(department.getName());
    }

    @GetMapping("/findById/{id}")
    public Department findById(@PathVariable("id") Integer id) {
        System.out.println("正在查询学院信息 By id " + id);
        return departmentService.findById(id);
    }

    @GetMapping("/findAll")
    public List<Department> findAll() {
        return departmentService.findAll();
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") int id) {
        System.out.println("正在删除学院 id：" + id);
        return departmentService.deleteById(id);
    }

    @PostMapping("/update")
    public boolean updateDepartment(@RequestBody Department department) {
        System.out.println("更新 " + department);
        return departmentService.updateById(department);
    }
}
