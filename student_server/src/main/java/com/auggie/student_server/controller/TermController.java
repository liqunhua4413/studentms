package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Term;
import com.auggie.student_server.service.TermService;
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
 * 学期表。供成绩查询、开课管理等页面的学期下拉使用。
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/term")
public class TermController {

    @Autowired
    private TermService termService;

    @GetMapping("/findAll")
    public List<Term> findAll() {
        return termService.findAll();
    }

    @GetMapping("/findById/{id}")
    public Term findById(@PathVariable Integer id) {
        return termService.findById(id);
    }

    @PostMapping("/add")
    public boolean add(@RequestBody Term term) {
        return termService.save(term);
    }

    @PostMapping("/update")
    public boolean update(@RequestBody Term term) {
        return termService.updateById(term);
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") Integer id) {
        return termService.deleteById(id);
    }

    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) return "文件为空，请选择文件";
        if (file.getOriginalFilename() == null || (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")))
            return "请上传 Excel 文件（.xlsx 或 .xls）";
        return termService.importFromExcel(file);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = termService.generateImportTemplate();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.write(os);
        workbook.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "学期批量导入模板.xlsx");
        return ResponseEntity.ok().headers(headers).body(os.toByteArray());
    }
}
