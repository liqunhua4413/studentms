package com.auggie.student_server.controller;

import com.auggie.student_server.entity.GradeLevel;
import com.auggie.student_server.service.GradeLevelService;
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

    @PostMapping("/add")
    public boolean add(@RequestBody GradeLevel gradeLevel) {
        return gradeLevelService.save(gradeLevel);
    }

    @PostMapping("/update")
    public boolean update(@RequestBody GradeLevel gradeLevel) {
        return gradeLevelService.updateById(gradeLevel);
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") Integer id) {
        return gradeLevelService.deleteById(id);
    }

    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) return "文件为空，请选择文件";
        if (file.getOriginalFilename() == null || (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")))
            return "请上传 Excel 文件（.xlsx 或 .xls）";
        return gradeLevelService.importFromExcel(file);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = gradeLevelService.generateImportTemplate();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.write(os);
        workbook.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "年级批量导入模板.xlsx");
        return ResponseEntity.ok().headers(headers).body(os.toByteArray());
    }
}
