package com.auggie.student_server.controller;

import com.auggie.student_server.entity.SCTInfo;
import com.auggie.student_server.service.GradeService;
import com.auggie.student_server.service.SCTService;
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
 * @Date: 2024/01/01
 * @Description: GradeController - 成绩管理控制器
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/grade")
public class GradeController {
    @Autowired
    private GradeService gradeService;
    @Autowired
    private SCTService sctService;

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              @RequestAttribute(value = "operator", required = false) String operator) {
        if (file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        if (operator == null || operator.isEmpty()) {
            operator = "unknown";
        }
        return gradeService.uploadExcel(file, operator);
    }

    @PostMapping("/query")
    public List<SCTInfo> queryGrades(@RequestBody Map<String, String> map) {
        return sctService.findBySearch(map);
    }

    @PostMapping("/reexamination")
    public List<SCTInfo> getReexaminationList(@RequestBody Map<String, String> map) {
        return sctService.getReexaminationList(map);
    }

    @PostMapping("/reexamination/export")
    public ResponseEntity<byte[]> exportReexamination(@RequestBody Map<String, String> map) throws IOException {
        List<SCTInfo> list = sctService.getReexaminationList(map);
        Workbook workbook = gradeService.exportReexaminationToExcel(list);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "补考名单.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
