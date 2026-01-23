package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Student;
import com.auggie.student_server.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Auther: auggie
 * @Date: 2022/2/8 17:37
 * @Description: StudentController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/addStudent")
    public boolean addStudent(@RequestBody Student student) {
        System.out.println("正在保存学生对象" + student);
        return studentService.save(student);
    }

    @PostMapping("/login")
    public boolean login(@RequestBody Student student) {
        System.out.println("正在验证学生登陆 " + student);
        Student s = studentService.findById(student.getSid());
        if (s == null || !s.getPassword().equals(student.getPassword())) {
            return false;
        }
        else {
            return true;
        }
    }

    @PostMapping("/findBySearch")
    public List<Student> findBySearch(@RequestBody Student student) {
        Integer fuzzy = (student.getPassword() == null) ? 0 : 1;
        return studentService.findBySearch(student.getSid(), student.getSname(), fuzzy);
    }

    @GetMapping("/findById/{sid}")
    public Student findById(@PathVariable("sid") Integer sid) {
        System.out.println("正在查询学生信息 By id " + sid);
        return studentService.findById(sid);
    }

    @GetMapping("/findByPage/{page}/{size}")
    public List<Student> findByPage(@PathVariable("page") int page, @PathVariable("size") int size) {
        System.out.println("查询学生列表分页 " + page + " " + size);
        return studentService.findByPage(page, size);
    }

    @GetMapping("/getLength")
    public Integer getLength() {
        return studentService.getLength();
    }

    @GetMapping("/deleteById/{sid}")
    public boolean deleteById(@PathVariable("sid") int sid) {
        System.out.println("正在删除学生 sid：" + sid);
        return studentService.deleteById(sid);
    }

    @PostMapping("/updateStudent")
    public boolean updateStudent(@RequestBody Student student) {
        System.out.println("更新 " + student);
        return studentService.updateById(student);
    }

    /**
     * 学生批量导入（Excel）
     */
    @PostMapping("/import")
    public String importStudents(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        return studentService.importFromExcel(file);
    }
}
