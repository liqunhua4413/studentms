package com.auggie.student_server.controller;

import com.auggie.student_server.entity.GradeLevel;
import com.auggie.student_server.service.GradeLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 年级表。供成绩查询等页面年级下拉使用。
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/gradeLevel")
public class GradeLevelController {
    @Autowired
    private GradeLevelService gradeLevelService;

    @GetMapping("/findAll")
    public List<GradeLevel> findAll() {
        return gradeLevelService.findAll();
    }

    @GetMapping("/findById/{id}")
    public GradeLevel findById(@PathVariable("id") Integer id) {
        return gradeLevelService.findById(id);
    }
}
