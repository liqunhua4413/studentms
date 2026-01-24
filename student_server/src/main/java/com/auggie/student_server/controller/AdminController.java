package com.auggie.student_server.controller;

import com.auggie.student_server.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: AdminController - 管理员功能控制器
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private InitService initService;

    /**
     * 清空所有数据
     */
    @PostMapping("/clearAllData")
    public String clearAllData() {
        try {
            initService.clearTestData();
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "清空数据失败：" + e.getMessage();
        }
    }

    /**
     * 生成测试数据
     */
    @PostMapping("/generateTestData")
    public String generateTestData() {
        try {
            initService.insertInitialData();
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "生成测试数据失败：" + e.getMessage();
        }
    }
}
