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
    public WordPaper upload(@RequestParam("file") MultipartFile file,
                           @RequestParam(value = "uploadBy", defaultValue = "admin") String uploadBy) throws IOException {
        return wordPaperService.upload(file, uploadBy);
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
    public List<WordPaper> findAll() {
        return wordPaperService.findAll();
    }

    @GetMapping("/findById/{id}")
    public WordPaper findById(@PathVariable("id") Long id) {
        return wordPaperService.findById(id);
    }

    @PostMapping("/findBySearch")
    public List<WordPaper> findBySearch(@RequestBody Map<String, String> map) {
        String fileName = map.get("fileName");
        String uploadBy = map.get("uploadBy");
        return wordPaperService.findBySearch(fileName, uploadBy);
    }

    @GetMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") Long id) {
        return wordPaperService.deleteById(id);
    }
}
