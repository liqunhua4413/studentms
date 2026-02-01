package com.auggie.student_server.controller;

import com.auggie.student_server.entity.WordPaper;
import com.auggie.student_server.service.WordPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: WordPaperController
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/paper")
public class WordPaperController {
    @Autowired
    private WordPaperService wordPaperService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                           @RequestParam(value = "uploadBy", defaultValue = "admin") String uploadBy,
                           @RequestAttribute(value = "operator", required = false) String operator) {
        try {
            // 如果request中有operator，使用operator作为uploadBy（用于日志记录）
            // 但数据库中仍然保存uploadBy参数（可能是教师姓名）
            WordPaper paper = wordPaperService.upload(file, uploadBy);
            return ResponseEntity.ok(paper);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) throws IOException {
        File file = wordPaperService.download(id);
        WordPaper paper = wordPaperService.findById(id);

        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + paper.getFileName() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/findAll")
    public List<WordPaper> findAll(
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId,
            @RequestAttribute(value = "operator", required = false) String operator) {
        // admin 可以查看所有文件
        if ("admin".equals(userType)) {
            return wordPaperService.findAll();
        }
        // 院长：只能查看本学院的文件（不含系统管理员上传的，已在 findByDepartmentId 中排除 role=admin）
        if ("dean".equals(userType) && departmentId != null) {
            return wordPaperService.findByDepartmentId(departmentId);
        }
        // 教师：只能查看本人上传的文件，不能看到院长或其他人上传的
        if ("teacher".equals(userType) && operator != null && !operator.isEmpty()) {
            return wordPaperService.findByUploadBy(operator);
        }
        return wordPaperService.findAll();
    }

    @GetMapping("/findById/{id}")
    public WordPaper findById(@PathVariable("id") Long id) {
        return wordPaperService.findById(id);
    }

    @PostMapping("/findBySearch")
    public List<WordPaper> findBySearch(@RequestBody Map<String, String> map,
                                        @RequestAttribute(value = "userType", required = false) String userType,
                                        @RequestAttribute(value = "departmentId", required = false) Integer departmentId,
                                        @RequestAttribute(value = "operator", required = false) String operator) {
        String fileName = map.get("fileName");
        String uploadBy = map.get("uploadBy");
        
        // admin 可以查看所有文件
        if ("admin".equals(userType)) {
            return wordPaperService.findBySearch(fileName, uploadBy);
        }
        
        // 院长：只能查看本学院的文件（已排除 admin 上传）
        if ("dean".equals(userType) && departmentId != null) {
            List<WordPaper> deptFiles = wordPaperService.findByDepartmentId(departmentId);
            if ((fileName != null && !fileName.isEmpty()) || (uploadBy != null && !uploadBy.isEmpty())) {
                return deptFiles.stream()
                    .filter(file -> {
                        boolean match = true;
                        if (fileName != null && !fileName.isEmpty()) {
                            match = match && file.getFileName() != null && file.getFileName().contains(fileName);
                        }
                        if (uploadBy != null && !uploadBy.isEmpty()) {
                            match = match && file.getUploadBy() != null && file.getUploadBy().contains(uploadBy);
                        }
                        return match;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            return deptFiles;
        }
        
        // 教师：只能查看本人上传的文件
        if ("teacher".equals(userType) && operator != null && !operator.isEmpty()) {
            List<WordPaper> myFiles = wordPaperService.findByUploadBy(operator);
            if ((fileName != null && !fileName.isEmpty()) || (uploadBy != null && !uploadBy.isEmpty())) {
                return myFiles.stream()
                    .filter(file -> {
                        boolean match = true;
                        if (fileName != null && !fileName.isEmpty()) {
                            match = match && file.getFileName() != null && file.getFileName().contains(fileName);
                        }
                        if (uploadBy != null && !uploadBy.isEmpty()) {
                            match = match && file.getUploadBy() != null && file.getUploadBy().contains(uploadBy);
                        }
                        return match;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            return myFiles;
        }
        
        return wordPaperService.findBySearch(fileName, uploadBy);
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") Long id) {
        return wordPaperService.deleteById(id);
    }
}
