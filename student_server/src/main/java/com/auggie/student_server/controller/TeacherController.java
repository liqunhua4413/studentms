package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2022/2/9 11:02
 * @Description: TeacherController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/teacher")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;

    @PostMapping("/addTeacher")
    public boolean addTeacher(@RequestBody Teacher teacher) {
        return teacherService.save(teacher);
    }

    @PostMapping("/login")
    public boolean login(@RequestBody Teacher teacher) {
        System.out.println("正在验证教师登陆 " + teacher);
        Teacher t = teacherService.findById(teacher.getTid());
        System.out.println("数据库教师信息" + t);
        if (t == null || !t.getPassword().equals(teacher.getPassword())) {
            return false;
        }
        else {
            return true;
        }
    }

    @GetMapping("/findById/{tid}")
    public Teacher findById(@PathVariable("tid") Integer tid) {
        System.out.println("正在查询学生信息 By id " + tid);
        return teacherService.findById(tid);
    }

    @PostMapping("/findBySearch")
    public List<Teacher> findBySearch(@RequestBody Map<String, String> map) {
        return teacherService.findBySearch(map);
    }

    @GetMapping("/deleteById/{tid}")
    public boolean deleteById(@PathVariable("tid") int tid) {
        System.out.println("正在删除学生 tid：" + tid);
        return teacherService.deleteById(tid);
    }

    @PostMapping("/updateTeacher")
    public boolean updateTeacher(@RequestBody Teacher teacher) {
        System.out.println("更新 " + teacher);
        return teacherService.updateById(teacher);
    }

    /**
     * 教师批量导入（Excel）
     */
    @PostMapping("/import")
    public String importTeachers(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        return teacherService.importFromExcel(file);
    }
}
