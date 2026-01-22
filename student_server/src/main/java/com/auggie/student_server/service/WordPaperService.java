package com.auggie.student_server.service;

import com.auggie.student_server.entity.WordPaper;
import com.auggie.student_server.mapper.WordPaperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: WordPaperService
 * @Version 1.0.0
 */

@Service
public class WordPaperService {
    @Autowired
    private WordPaperMapper wordPaperMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    public List<WordPaper> findAll() {
        return wordPaperMapper.findAll();
    }

    public WordPaper findById(Long id) {
        return wordPaperMapper.findById(id);
    }

    public List<WordPaper> findBySearch(String fileName, String uploadBy) {
        return wordPaperMapper.findBySearch(fileName, uploadBy);
    }

    public WordPaper upload(MultipartFile file, String uploadBy) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".doc") && !originalFilename.endsWith(".docx"))) {
            throw new IllegalArgumentException("文件格式错误，请上传 Word 文件（.doc 或 .docx）");
        }

        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(fileName);

        // 保存文件
        file.transferTo(filePath.toFile());

        // 保存到数据库
        WordPaper paper = new WordPaper();
        paper.setFileName(originalFilename);
        paper.setFilePath(filePath.toString());
        paper.setUploadBy(uploadBy);
        paper.setUploadTime(LocalDateTime.now());

        wordPaperMapper.save(paper);
        return paper;
    }

    public File download(Long id) {
        WordPaper paper = wordPaperMapper.findById(id);
        if (paper == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        File file = new File(paper.getFilePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        return file;
    }

    public boolean deleteById(Long id) {
        WordPaper paper = wordPaperMapper.findById(id);
        if (paper != null) {
            // 删除文件
            File file = new File(paper.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
        return wordPaperMapper.deleteById(id);
    }
}
