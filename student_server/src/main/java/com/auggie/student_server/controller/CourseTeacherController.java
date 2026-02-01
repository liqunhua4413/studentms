package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.CourseTeacher;
import com.auggie.student_server.entity.CourseTeacherInfo;
import com.auggie.student_server.service.CourseTeacherService;
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
 * @Date: 2022/2/10 16:51
 * @Description: CourseTeacherController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/courseTeacher")
public class CourseTeacherController {
    @Autowired
    private CourseTeacherService courseTeacherService;

    @GetMapping("/insert/{cid}/{tid}/{termId}")
    public boolean insert(@PathVariable Integer cid, @PathVariable Integer tid, @PathVariable Integer termId) {
        if (courseTeacherService.findBySearch(cid, tid, termId).size() != 0) {
            return false;
        }
        return courseTeacherService.insertCourseTeacher(cid, tid, termId);
    }

    @GetMapping("/findMyCourse/{tid}/{termId}")
    public List<Course> findMyCourse(@PathVariable Integer tid, @PathVariable Integer termId) {
        return courseTeacherService.findMyCourse(tid, termId);
    }

    @PostMapping("/findCourseTeacherInfo")
    public List<CourseTeacherInfo> findCourseTeacherInfo(@RequestBody Map<String, String> map) {
        return courseTeacherService.findCourseTeacherInfo(map);
    }

    @PostMapping("/deleteById")
    public boolean deleteById(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.deleteById(courseTeacher);
    }

    @GetMapping("/findById/{id}")
    public CourseTeacher findById(@PathVariable Integer id) {
        return courseTeacherService.findById(id);
    }

    @PostMapping("/add")
    public boolean add(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.save(courseTeacher);
    }

    @PostMapping("/update")
    public boolean update(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.updateById(courseTeacher);
    }

    /**
     * 开课表批量导入（Excel）
     */
    @PostMapping("/import")
    public String importCourseTeachers(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "文件格式错误，请上传 Excel 文件（.xlsx 或 .xls）";
        }
        return courseTeacherService.importFromExcel(file);
    }

    /**
     * 下载开课表批量导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = courseTeacherService.generateImportTemplate();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "开课表批量导入模板.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
