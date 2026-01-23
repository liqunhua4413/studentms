-- 学生成绩管理系统完整数据库初始化脚本（最终稳定版）
-- 兼容 studentms 旧项目 + 新规范扩展
-- 执行前请备份数据库

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS studentms;
CREATE DATABASE studentms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE studentms;

-- =========================
-- 基础维度表
-- =========================

DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '学院名称',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `department_id` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_major_department FOREIGN KEY (department_id) REFERENCES department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

DROP TABLE IF EXISTS `class`;
CREATE TABLE `class` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `grade_level` VARCHAR(10),
  `major_id` INT NOT NULL,
  `department_id` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES major(id),
  CONSTRAINT fk_class_department FOREIGN KEY (department_id) REFERENCES department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- =========================
-- 核心用户表
-- =========================

DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `sid` INT AUTO_INCREMENT PRIMARY KEY,
  `student_no` VARCHAR(50) NOT NULL UNIQUE,
  `sname` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `class_id` INT DEFAULT NULL,
  `grade_level` VARCHAR(10),
  `major_id` INT DEFAULT NULL,
  `department_id` INT DEFAULT NULL,
  CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class(id),
  CONSTRAINT fk_student_major FOREIGN KEY (major_id) REFERENCES major(id),
  CONSTRAINT fk_student_department FOREIGN KEY (department_id) REFERENCES department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `tid` INT AUTO_INCREMENT PRIMARY KEY,
  `teacher_no` VARCHAR(50) NOT NULL UNIQUE,
  `tname` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'teacher',
  `department_id` INT DEFAULT NULL,
  CONSTRAINT fk_teacher_department FOREIGN KEY (department_id) REFERENCES department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- =========================
-- 教学业务表
-- =========================

DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `cid` INT AUTO_INCREMENT PRIMARY KEY,
  `cname` VARCHAR(100) NOT NULL,
  `ccredit` TINYINT DEFAULT NULL,
  `major_id` INT DEFAULT NULL,
  `department_id` INT DEFAULT NULL,
  CONSTRAINT fk_course_major FOREIGN KEY (major_id) REFERENCES major(id),
  CONSTRAINT fk_course_department FOREIGN KEY (department_id) REFERENCES department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

DROP TABLE IF EXISTS `course_open`;
CREATE TABLE `course_open` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `course_id` INT NOT NULL,
  `teacher_id` INT NOT NULL,
  `class_id` INT NOT NULL,
  `term` VARCHAR(30) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_open_course FOREIGN KEY (course_id) REFERENCES course(cid),
  CONSTRAINT fk_open_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(tid),
  CONSTRAINT fk_open_class FOREIGN KEY (class_id) REFERENCES class(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开课表';

DROP TABLE IF EXISTS `score`;
CREATE TABLE `score` (
  `sctid` INT AUTO_INCREMENT PRIMARY KEY,
  `sid` INT DEFAULT NULL,
  `cid` INT DEFAULT NULL,
  `tid` INT DEFAULT NULL,
  `grade` FLOAT DEFAULT NULL,
  `term` VARCHAR(30),
  `usual_grade` FLOAT,
  `final_grade` FLOAT,
  `total_grade` FLOAT,
  `class_id` INT,
  `major_id` INT,
  `department_id` INT,
  CONSTRAINT fk_score_student FOREIGN KEY (sid) REFERENCES student(sid),
  CONSTRAINT fk_score_teacher FOREIGN KEY (tid) REFERENCES teacher(tid),
  CONSTRAINT fk_score_course FOREIGN KEY (cid) REFERENCES course(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

DROP TABLE IF EXISTS `score_import_record`;
CREATE TABLE `score_import_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `file_name` VARCHAR(255) NOT NULL,
  `term` VARCHAR(30) NOT NULL,
  `course_id` INT,
  `teacher_id` INT,
  `operator` VARCHAR(50) NOT NULL,
  `status` VARCHAR(20) DEFAULT 'SUCCESS',
  `message` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_import_course FOREIGN KEY (course_id) REFERENCES course(cid),
  CONSTRAINT fk_import_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(tid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩导入记录表';

DROP TABLE IF EXISTS `exam_paper_analysis`;
CREATE TABLE `exam_paper_analysis` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `course_open_id` INT,
  `file_name` VARCHAR(255) NOT NULL,
  `file_path` VARCHAR(500) NOT NULL,
  `upload_by` VARCHAR(50) NOT NULL,
  `uploader_role` VARCHAR(20) NOT NULL,
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_open FOREIGN KEY (course_open_id) REFERENCES course_open(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷分析表';

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `operator` VARCHAR(50),
  `operation_type` VARCHAR(50),
  `target_table` VARCHAR(50),
  `target_id` BIGINT,
  `content` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =========================
-- 初始化数据
-- =========================

INSERT INTO department(name) VALUES
('计算机工程学院'),('机电工程学院'),('财经管理学院'),('艺术设计学院'),('基础教学部');

INSERT INTO major(name,department_id) VALUES
('软件技术',1),('大数据技术',1),('机电一体化',2),('会计',3),('视觉传达设计',4);

INSERT INTO class(name,grade_level,major_id,department_id) VALUES
('23软件1班','2023',1,1),('23软件2班','2023',1,1),('23大数据1班','2023',2,1),
('23机电1班','2023',3,2),('23会计1班','2023',4,3);

INSERT INTO course(cname,ccredit,major_id,department_id) VALUES
('数据结构',4,1,1),('数据库原理',4,1,1),('计算机网络',3,2,1),
('机电控制基础',4,3,2),('基础会计',3,4,3);

INSERT INTO teacher(teacher_no,tname,password,role,department_id) VALUES
('A0001','系统管理员','123456','admin',1),
('D0101','计算机工程学院院长','123456','dean',1),
('D0201','机电工程学院院长','123456','dean',2),
('D0301','财经管理学院院长','123456','dean',3),
('D0401','艺术设计学院院长','123456','dean',4),
('D0501','基础教学部主任','123456','dean',5),
('T1001','张三','123456','teacher',1),
('T1002','李四','123456','teacher',1),
('T2001','王五','123456','teacher',2),
('T3001','赵六','123456','teacher',3),
('T4001','钱七','123456','teacher',4);

INSERT INTO student(student_no,sname,password,class_id,grade_level,major_id,department_id) VALUES
('S230001','学生一','123456',1,'2023',1,1),
('S230002','学生二','123456',1,'2023',1,1),
('S230003','学生三','123456',2,'2023',1,1),
('S230004','学生四','123456',3,'2023',2,1),
('S230005','学生五','123456',4,'2023',3,2);

SET FOREIGN_KEY_CHECKS = 1;
