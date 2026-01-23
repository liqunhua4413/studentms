package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Major;
import com.auggie.student_server.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: MajorController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/major")
public class MajorController {
    @Autowired
    private MajorService majorService;

    @PostMapping("/add")
    public boolean addMajor(@RequestBody Major major) {
        System.out.println("正在保存专业对象" + major);
        return majorService.save(major);
    }

    @PostMapping("/findBySearch")
    public List<Major> findBySearch(@RequestBody Major major) {
        return majorService.findBySearch(major.getName(), major.getDepartmentId());
    }

    @GetMapping("/findByDepartmentId/{departmentId}")
    public List<Major> findByDepartmentId(@PathVariable("departmentId") Integer departmentId) {
        return majorService.findByDepartmentId(departmentId);
    }

    @GetMapping("/findById/{id}")
    public Major findById(@PathVariable("id") Integer id) {
        System.out.println("正在查询专业信息 By id " + id);
        return majorService.findById(id);
    }

    @GetMapping("/findAll")
    public List<Major> findAll() {
        return majorService.findAll();
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") int id) {
        System.out.println("正在删除专业 id：" + id);
        return majorService.deleteById(id);
    }

    @PostMapping("/update")
    public boolean updateMajor(@RequestBody Major major) {
        System.out.println("更新 " + major);
        return majorService.updateById(major);
    }

    /**
     * 专业批量导入（Excel）
     */
    @PostMapping("/import")
    public String importMajors(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        return majorService.importFromExcel(file);
    }
}
