-- 学生成绩管理系统完整数据库初始化脚本
-- 包含基础表和升级后的所有表结构
-- 执行前请备份数据库（如果已有数据）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 删除并创建数据库
DROP DATABASE IF EXISTS studentms;
CREATE DATABASE studentms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE studentms;

-- ============================================
-- 基础表结构
-- ============================================

-- 课程表
DROP TABLE IF EXISTS `c`;
CREATE TABLE `c` (
  `cid` int(11) NOT NULL AUTO_INCREMENT,
  `cname` varchar(30) NOT NULL,
  `ccredit` tinyint(4) DEFAULT NULL,
  `major_id` int(11) DEFAULT NULL COMMENT '专业ID',
  `department_id` int(11) DEFAULT NULL COMMENT '学院ID',
  PRIMARY KEY (`cid`),
  KEY `idx_course_major` (`major_id`),
  KEY `idx_course_department` (`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;

-- 教师表（包含 admin 和 teacher，通过 role 字段区分）
DROP TABLE IF EXISTS `t`;
CREATE TABLE `t` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `tname` varchar(30) NOT NULL,
  `password` varchar(30) NOT NULL,
  `role` varchar(20) DEFAULT 'teacher' COMMENT '角色：admin 或 teacher',
  PRIMARY KEY (`tid`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;

-- 学生表
DROP TABLE IF EXISTS `s`;
CREATE TABLE `s` (
  `sid` int(11) NOT NULL AUTO_INCREMENT,
  `sname` varchar(30) NOT NULL,
  `password` varchar(30) NOT NULL,
  `class_id` int(11) DEFAULT NULL COMMENT '班级ID',
  `grade_level` varchar(10) DEFAULT NULL COMMENT '年级',
  `major_id` int(11) DEFAULT NULL COMMENT '专业ID',
  `department_id` int(11) DEFAULT NULL COMMENT '学院ID',
  PRIMARY KEY (`sid`),
  KEY `idx_student_class` (`class_id`),
  KEY `idx_student_major` (`major_id`),
  KEY `idx_student_department` (`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4;

-- 课程教师关联表
DROP TABLE IF EXISTS `ct`;
CREATE TABLE `ct` (
  `ctid` int(11) NOT NULL AUTO_INCREMENT,
  `cid` int(11) DEFAULT NULL,
  `tid` int(11) DEFAULT NULL,
  `term` varchar(30) NOT NULL,
  PRIMARY KEY (`ctid`),
  KEY `cid` (`cid`),
  KEY `tid` (`tid`),
  CONSTRAINT `ct_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `c` (`cid`),
  CONSTRAINT `ct_ibfk_2` FOREIGN KEY (`tid`) REFERENCES `t` (`tid`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;

-- 学生课程成绩表
DROP TABLE IF EXISTS `sct`;
CREATE TABLE `sct` (
  `sctid` int(11) NOT NULL AUTO_INCREMENT,
  `sid` int(11) DEFAULT NULL,
  `cid` int(11) DEFAULT NULL,
  `tid` int(11) DEFAULT NULL,
  `grade` float DEFAULT NULL COMMENT '总成绩（兼容旧数据）',
  `term` varchar(30) DEFAULT NULL,
  `usual_grade` float DEFAULT NULL COMMENT '平时成绩',
  `final_grade` float DEFAULT NULL COMMENT '期末成绩',
  `total_grade` float DEFAULT NULL COMMENT '总成绩',
  `class_id` int(11) DEFAULT NULL COMMENT '班级ID',
  `major_id` int(11) DEFAULT NULL COMMENT '专业ID',
  `department_id` int(11) DEFAULT NULL COMMENT '学院ID',
  PRIMARY KEY (`sctid`),
  KEY `sid` (`sid`),
  KEY `tid` (`tid`),
  KEY `cid` (`cid`),
  KEY `idx_sct_class` (`class_id`),
  KEY `idx_sct_major` (`major_id`),
  KEY `idx_sct_department` (`department_id`),
  CONSTRAINT `sct_ibfk_1` FOREIGN KEY (`sid`) REFERENCES `s` (`sid`),
  CONSTRAINT `sct_ibfk_2` FOREIGN KEY (`tid`) REFERENCES `t` (`tid`),
  CONSTRAINT `sct_ibfk_3` FOREIGN KEY (`cid`) REFERENCES `c` (`cid`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 升级后的新表
-- ============================================

-- 学院表
CREATE TABLE IF NOT EXISTS `department` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 专业表
CREATE TABLE IF NOT EXISTS `major` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `department_id` INT,
    FOREIGN KEY (`department_id`) REFERENCES `department`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 班级表
CREATE TABLE IF NOT EXISTS `class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `major_id` INT,
    `department_id` INT,
    FOREIGN KEY (`major_id`) REFERENCES `major`(`id`),
    FOREIGN KEY (`department_id`) REFERENCES `department`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Word 文件表
CREATE TABLE IF NOT EXISTS `word_papers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `upload_by` VARCHAR(50),
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `operator` VARCHAR(50),
    `operation_type` VARCHAR(50),
    `target_table` VARCHAR(50),
    `target_id` BIGINT,
    `content` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_operation_log_operator` (`operator`),
    KEY `idx_operation_log_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 初始化数据
-- ============================================

-- 插入课程数据
INSERT INTO `c` (`cid`, `cname`, `ccredit`) VALUES
(7, '数据结构', 6),
(8, '组合数学', 3),
(9, '计算机网络', 5),
(10, '计算机组成原理', 5),
(11, 'RJC教你做选课系统', 10);

-- 插入教师数据（注意：admin 用户的 role 设置为 'admin'）
INSERT INTO `t` (`tid`, `tname`, `password`, `role`) VALUES
(4, '李玉民', '123', 'teacher'),
(6, 'admin', '123', 'admin'),
(13, '张三', '123', 'teacher');

-- 插入学生数据
INSERT INTO `s` (`sid`, `sname`, `password`) VALUES
(1, '阮健乘', '1234'),
(2, '张四', '123'),
(3, '李四', '1234'),
(4, '彭昊辉', '123456'),
(6, '林春霞', '123'),
(7, '董一超', '12345'),
(8, '董超', '123'),
(9, '张千', '10086'),
(10, '李万', '10085'),
(14, '薇尔莉特', '123'),
(21, '庄亮', '123'),
(22, '钟平', '1234'),
(23, '李煜豪', '123');

-- 插入课程教师关联数据
INSERT INTO `ct` (`ctid`, `cid`, `tid`, `term`) VALUES
(5, 8, 4, '22-春季学期'),
(6, 7, 4, '22-春季学期'),
(7, 10, 13, '22-春季学期'),
(8, 9, 13, '22-春季学期'),
(9, 11, 4, '22-春季学期');

-- 插入学生课程成绩数据
INSERT INTO `sct` (`sctid`, `sid`, `cid`, `tid`, `grade`, `term`) VALUES
(10, 2, 8, 4, 1, '22-春季学期'),
(11, 2, 10, 13, NULL, '22-春季学期'),
(12, 2, 7, 4, NULL, '22-春季学期'),
(13, 4, 8, 4, 10, '22-春季学期'),
(14, 4, 7, 4, NULL, '22-春季学期'),
(15, 4, 10, 13, NULL, '22-春季学期');

-- ============================================
-- 完成
-- ============================================

SET FOREIGN_KEY_CHECKS = 1;
