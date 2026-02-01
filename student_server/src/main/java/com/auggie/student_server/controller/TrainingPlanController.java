package com.auggie.student_server.controller;

import com.auggie.student_server.entity.TrainingPlan;
import com.auggie.student_server.service.TrainingPlanService;
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

@RestController
@CrossOrigin("*")
@RequestMapping("/trainingPlan")
public class TrainingPlanController {

    @Autowired
    private TrainingPlanService trainingPlanService;

    @GetMapping("/findAll")
    public List<TrainingPlan> findAll() {
        return trainingPlanService.findAll();
    }

    @GetMapping("/findById/{id}")
    public TrainingPlan findById(@PathVariable Long id) {
        return trainingPlanService.findById(id);
    }

    @PostMapping("/findBySearch")
    public List<TrainingPlan> findBySearch(@RequestBody Map<String, Object> params) {
        return trainingPlanService.findBySearch(params);
    }

    @PostMapping("/add")
    public boolean add(@RequestBody TrainingPlan trainingPlan) {
        return trainingPlanService.save(trainingPlan);
    }

    @PostMapping("/update")
    public boolean update(@RequestBody TrainingPlan trainingPlan) {
        return trainingPlanService.updateById(trainingPlan);
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable Long id) {
        return trainingPlanService.deleteById(id);
    }

    @PostMapping("/import")
    public String importTrainingPlans(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        return trainingPlanService.importFromExcel(file);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = trainingPlanService.generateImportTemplate();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "training_plan_template.xlsx");
        return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());
    }
}
