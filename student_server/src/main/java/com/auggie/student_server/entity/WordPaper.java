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
}
