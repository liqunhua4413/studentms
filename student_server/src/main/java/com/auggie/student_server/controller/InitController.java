package com.auggie.student_server.controller;

import com.auggie.student_server.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统初始化与测试数据管理
 * 仅管理员可调用（通过 AdminInterceptor 控制）
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/init")
public class InitController {

    @Autowired
    private InitService initService;

    /**
     * 一键清空测试数据
     */
    @PostMapping("/clearTestData")
    public String clearTestData() {
        initService.clearTestData();
        return "测试数据已清空";
    }

    /**
     * 按模板重新导入基础数据（学院/专业/班级/教师/学生/课程）
     */
    @PostMapping("/importBaseData")
    public String importBaseData() {
        initService.insertInitialData();
        return "基础数据已按模板重新导入";
    }
}

