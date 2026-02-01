package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Class;
import com.auggie.student_server.service.ClassService;
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

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: ClassController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/class")
public class ClassController {
    @Autowired
    private ClassService classService;

    @PostMapping("/add")
    public boolean addClass(@RequestBody Class clazz) {
        System.out.println("正在保存班级对象" + clazz);
        return classService.save(clazz);
    }

    @PostMapping("/findBySearch")
    public List<Class> findBySearch(@RequestBody Class clazz) {
        return classService.findBySearch(clazz.getName(), clazz.getGradeLevelId(), clazz.getMajorId(), clazz.getDepartmentId());
    }

    @GetMapping("/findByMajorId/{majorId}")
    public List<Class> findByMajorId(@PathVariable("majorId") Integer majorId) {
        return classService.findByMajorId(majorId);
    }

    @GetMapping("/findById/{id}")
    public Class findById(@PathVariable("id") Integer id) {
        System.out.println("正在查询班级信息 By id " + id);
        return classService.findById(id);
    }

    @GetMapping("/findAll")
    public List<Class> findAll() {
        return classService.findAll();
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") int id) {
        System.out.println("正在删除班级 id：" + id);
        return classService.deleteById(id);
    }

    @PostMapping("/update")
    public boolean updateClass(@RequestBody Class clazz) {
        System.out.println("更新 " + clazz);
        return classService.updateById(clazz);
    }

    /**
     * 班级批量导入（Excel）
     */
    @PostMapping("/import")
    public String importClasses(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        return classService.importFromExcel(file);
    }

    /**
     * 下载班级批量导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = classService.generateImportTemplate();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "班级批量导入模板.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
