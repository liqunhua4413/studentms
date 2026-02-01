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

@Service
public class WordPaperService {

    @Autowired
    private WordPaperMapper wordPaperMapper;

    // 上传根路径（跨平台）
    @Value("${file.upload.path:${user.home}/studentms/uploads}")
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
    
    public List<WordPaper> findByDepartmentId(Integer departmentId) {
        return wordPaperMapper.findByDepartmentId(departmentId);
    }

    public List<WordPaper> findByUploadBy(String uploadBy) {
        return wordPaperMapper.findByUploadBy(uploadBy);
    }

    public WordPaper upload(MultipartFile file, String uploadBy) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                !(originalFilename.toLowerCase().endsWith(".doc") || originalFilename.toLowerCase().endsWith(".docx"))) {
            throw new IllegalArgumentException("文件格式错误，请上传 Word 文件（.doc 或 .docx）");
        }

        // 处理路径，支持相对路径和绝对路径
        String resolvedPath = uploadPath;
        if (resolvedPath.startsWith("./") || resolvedPath.startsWith(".\\")) {
            // 相对路径，转换为绝对路径
            resolvedPath = new File(".").getCanonicalPath() + File.separator + resolvedPath.substring(2);
        } else if (!Paths.get(resolvedPath).isAbsolute()) {
            // 如果不是绝对路径，使用系统属性解析
            resolvedPath = System.getProperty("user.home") + File.separator + resolvedPath.replace("${user.home}/", "").replace("${user.home}\\", "");
        }

        // wordpaper 子目录
        Path uploadDir = Paths.get(resolvedPath, "wordpaper");

        // 自动创建多级目录（跨平台）
        try {
            Files.createDirectories(uploadDir);
        } catch (Exception e) {
            throw new IOException("无法创建上传目录: " + uploadDir.toString() + ", 错误: " + e.getMessage(), e);
        }

        // 安全文件名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + extension;
        Path filePath = uploadDir.resolve(fileName);

        // 保存文件
        try {
            file.transferTo(filePath.toFile());
        } catch (Exception e) {
            throw new IOException("文件保存失败: " + e.getMessage(), e);
        }

        // 保存数据库
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
            File file = new File(paper.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
        return wordPaperMapper.deleteById(id);
    }
}
