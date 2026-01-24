package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.service.TeacherService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public boolean login(@RequestBody Map<String, String> loginForm) {
        System.out.println("正在验证教师登陆 " + loginForm);
        // 只能通过teacherNo登录，不能用id登录
        String teacherNo = loginForm.get("teacherNo");
        if (teacherNo == null || teacherNo.isEmpty()) {
            return false;
        }
        
        Teacher t = teacherService.findByTeacherNo(teacherNo);
        System.out.println("数据库教师信息" + t);
        String password = loginForm.get("password");
        if (t == null || password == null || !t.getPassword().equals(password)) {
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

    @PostMapping("/findByNo")
    public Teacher findByTeacherNo(@RequestBody Map<String, String> map) {
        String teacherNo = map.get("teacherNo");
        System.out.println("正在通过teacherNo查询教师信息: " + teacherNo);
        // 只能通过teacherNo查找，不能用id查找
        return teacherService.findByTeacherNo(teacherNo);
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

    /**
     * 下载教师批量导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = teacherService.generateImportTemplate();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "教师批量导入模板.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
