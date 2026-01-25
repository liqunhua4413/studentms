package com.auggie.student_server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: WordPaper - Word 文件实体类
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("WordPaper")
public class WordPaper {
    private Long id;
    private String fileName;
    private String filePath;
    private String uploadBy;
    private LocalDateTime uploadTime;

    // 手动添加 getter/setter 方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getUploadBy() { return uploadBy; }
    public void setUploadBy(String uploadBy) { this.uploadBy = uploadBy; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
}
