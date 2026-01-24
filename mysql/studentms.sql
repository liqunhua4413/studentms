-- 学生成绩管理系统数据库初始化脚本
-- 包含完整的模拟数据和初始用户

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS studentms;
CREATE DATABASE studentms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE studentms;

-- 1. 学院表
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE COMMENT '学院名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 专业表
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '专业名称',
  `department_id` INT NOT NULL,
  UNIQUE KEY `uk_major_dept` (`name`, `department_id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 班级表
DROP TABLE IF EXISTS `class`;
CREATE TABLE `class` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '如：1班, 2班',
  `major_id` INT NOT NULL,
  `department_id` INT NOT NULL,
  UNIQUE KEY `uk_class_major_dept` (`name`, `major_id`, `department_id`),
  FOREIGN KEY (`major_id`) REFERENCES `major` (`id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 学生表
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_no` VARCHAR(50) NOT NULL UNIQUE,
  `sname` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL DEFAULT '123',
  `class_id` INT,
  `grade_level` VARCHAR(20) COMMENT '年级，如：2024级',
  `major_id` INT,
  `department_id` INT,
  FOREIGN KEY (`class_id`) REFERENCES `class` (`id`),
  FOREIGN KEY (`major_id`) REFERENCES `major` (`id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 教师表
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `teacher_no` VARCHAR(50) NOT NULL UNIQUE,
  `tname` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL DEFAULT '123',
  `role` VARCHAR(20) DEFAULT 'teacher', -- admin / teacher / dean
  `department_id` INT,
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 课程表
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `cname` VARCHAR(100) NOT NULL UNIQUE,
  `ccredit` FLOAT,
  `major_id` INT,
  `department_id` INT,
  `category` VARCHAR(50) COMMENT '课程类别',
  `nature` VARCHAR(50) COMMENT '课程性质',
  `exam_method` VARCHAR(50) COMMENT '考试方式',
  `hours` INT COMMENT '学时',
  FOREIGN KEY (`major_id`) REFERENCES `major` (`id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 成绩表
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` INT NOT NULL,
  `course_id` INT NOT NULL,
  `teacher_id` INT,
  `term` VARCHAR(30) NOT NULL,
  `usual_score` DECIMAL(5,2) COMMENT '平时成绩',
  `mid_score` DECIMAL(5,2) COMMENT '期中成绩',
  `final_score` DECIMAL(5,2) COMMENT '期末成绩',
  `grade` DECIMAL(5,2) COMMENT '总成绩',
  `remark` TEXT COMMENT '备注信息',
  UNIQUE KEY `uk_score_unique` (`student_id`, `course_id`, `term`),
  FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 选课/开课表 (用于关联教师和课程)
DROP TABLE IF EXISTS `course_open`;
CREATE TABLE `course_open` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `course_id` INT NOT NULL,
  `teacher_id` INT NOT NULL,
  `term` VARCHAR(30) NOT NULL,
  UNIQUE KEY `uk_course_teacher_term` (`course_id`, `teacher_id`, `term`),
  FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 导入记录与日志
DROP TABLE IF EXISTS `score_import_record`;
CREATE TABLE `score_import_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `file_name` VARCHAR(255) NOT NULL,
  `file_path` VARCHAR(500),
  `term` VARCHAR(30),
  `course_id` INT,
  `teacher_id` INT,
  `department_id` INT,
  `operator` VARCHAR(50),
  `status` VARCHAR(20),
  `message` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `operator` VARCHAR(50),
  `operation_type` VARCHAR(50),
  `target_table` VARCHAR(50),
  `target_id` BIGINT,
  `content` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 试卷分析表
DROP TABLE IF EXISTS `exam_paper_analysis`;
CREATE TABLE `exam_paper_analysis` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) COMMENT '文件路径',
  `upload_by` VARCHAR(50) COMMENT '上传人',
  `uploader_role` VARCHAR(20) DEFAULT 'teacher' COMMENT '上传人角色',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- 初始数据填充
-- ==========================================

-- 1. 学院（3 个）
INSERT INTO department (id, name) VALUES 
(1, '经济管理学院'), 
(2, '医学技术学院'), 
(3, '康复治疗学院');

-- 2. 专业（每个学院 1 个专业）
INSERT INTO major (id, name, department_id) VALUES 
(1, '会计学', 1), 
(2, '医学检验技术', 2), 
(3, '康复治疗技术', 3);

-- 3. 班级（每个学院 1 个班）
INSERT INTO class (id, name, major_id, department_id) VALUES 
(1, '1班', 1, 1), 
(2, '1班', 2, 2), 
(3, '1班', 3, 3);

-- 4. 教师（1 个 admin + 3 个院长 + 各学院 1 名教师）
INSERT INTO teacher (id, teacher_no, tname, password, role, department_id) VALUES 
(1, 'admin', '系统管理员', '123', 'admin', 1),
(2, 'dean1', '经济管理学院院长', '123', 'dean', 1),
(3, 'dean2', '医学技术学院院长', '123', 'dean', 2),
(4, 'dean3', '康复治疗学院院长', '123', 'dean', 3),
(5, 'T1001', '张老师', '123', 'teacher', 1),
(6, 'T2001', '李老师', '123', 'teacher', 2),
(7, 'T3001', '王老师', '123', 'teacher', 3);

-- 5. 学生（初始用户：student，及各学院示例学生）
INSERT INTO student (id, student_no, sname, password, class_id, grade_level, major_id, department_id) VALUES 
(1, 'student', '学生本人', '123', 1, '2024级', 1, 1),
(2, 'S24001', '赵小明', '123', 1, '2024级', 1, 1),
(3, 'S24002', '钱小红', '123', 1, '2024级', 1, 1),
(4, 'S24003', '孙小军', '123', 2, '2024级', 2, 2),
(5, 'S24004', '周小丽', '123', 3, '2024级', 3, 3);

-- 6. 课程（每个学院 1–2 门课）
INSERT INTO course (id, cname, ccredit, major_id, department_id, category, nature, exam_method, hours) VALUES 
(1, '基础会计', 3, 1, 1, '必修', '理论', '考试', 48),
(2, '经济学基础', 3, 1, 1, '必修', '理论', '考试', 48),
(3, '临床检验基础', 4, 2, 2, '必修', '理论', '考试', 64),
(4, '康复评定技术', 4, 3, 3, '必修', '理论', '考试', 64);

-- 7. 成绩
INSERT INTO score (
  student_id, course_id, teacher_id, term, 
  usual_score, mid_score, final_score, grade
) VALUES 
(1, 1, 5, '2024-2025-1', 85, NULL, 90, 88.5),
(2, 1, 5, '2024-2025-1', 78, NULL, 82, 80.8),
(3, 1, 5, '2024-2025-1', 92, NULL, 88, 89.2),
(1, 2, 5, '2024-2025-1', 90, NULL, 85, 86.5),
(4, 3, 6, '2024-2025-1', 88, NULL, 86, 86.8),
(5, 4, 7, '2024-2025-1', 82, NULL, 90, 87.2);

-- 8. 开课数据
INSERT INTO course_open (course_id, teacher_id, term) VALUES 
(1, 5, '2024-2025-1'), (2, 5, '2024-2025-1'), 
(3, 6, '2024-2025-1'), (4, 7, '2024-2025-1');

SET FOREIGN_KEY_CHECKS = 1;
