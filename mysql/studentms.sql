-- =============================================================================
-- 学生成绩管理系统 · 数据库初始化脚本
--
-- 架构目标：
-- - 支撑多学院、多专业、公共课场景
-- - 支撑成绩全流程（上传 / 审核发布 / 锁定 / 修改申请 / 审计）
-- - 支撑后续扩展：选课、培养方案管理、毕业审核
--
-- 含：基础数据强管控、培养方案模型、成绩四段式结构、发布机制、权限边界支撑
-- =============================================================================
--
-- 【教务系统三层架构（数据库四段式实现）】
--
-- 本系统采用标准教务系统“三层架构”，在数据库中以“四段式结构”落地：
--
-- 一、课程定义层（资源层 / 静态层）
--    course
--    - 描述课程本身属性（名称、学分、学时、考核方式、归属学院等）
--    - 不再与任何专业直接绑定
--    - 作为全校课程资源池存在
--    相关基础表：
--      department  学院表，定义学校所有学院
--      major       专业表，属于某学院
--      class       班级表，属于某专业、某学院
--      student     学生表，关联班级、年级、专业、学院
--      teacher     教师表，关联学院、角色
--
-- 二、培养方案层（规则层 / 方案层）
--    training_plan
--    - 描述“哪个专业，需要学习哪些课程”
--    - 定义课程在专业中的角色：必修 / 限选 / 任选
--    - 是选课范围、成绩范围、毕业审核的核心依据
--
-- 三、教学运行层（业务事实层 / 学期层）
--    course_open           开课与任课（这学期谁教什么课）
--    score                 成绩事实数据
--    grade_change_request  成绩修改申请流程
--    grade_change_log      成绩审计追溯日志
--
-- 数据流向：
--    课程定义 → 培养方案 → 教学实施 → 成绩事实
--    course → training_plan → course_open → score
--
-- 权限核心边界：
-- - 课程归属学院：course.department_id
-- - 任课边界：course_open.teacher_id
-- - 成绩边界：
--      score.course_id → course.department_id
--      解释：成绩记录关联的课程的所属学院就是成绩可操作/可查看的权限范围。
--      即一个院长或教师只能对自己学院的课程成绩进行上传或修改申请。
--
-- 重要设计原则：
-- - 课程 ≠ 专业课程
-- - 成绩不可直接修改，只能通过申请与审计流程变更
-- - 所有教学业务，必须基于培养方案层扩展
-- =============================================================================



SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS studentms;
CREATE DATABASE studentms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE studentms;

-- -----------------------------------------------------------------------------
-- 一、基础数据表（仅 admin 维护，服务于 admin 管理）
-- -----------------------------------------------------------------------------

-- 1. 学院表
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE COMMENT '学院名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '学院表，仅 admin 维护';

-- 2. 专业表
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '专业名称',
  `department_id` INT NOT NULL COMMENT '所属学院',
  UNIQUE KEY `uk_major_dept` (`name`, `department_id`),
  KEY `idx_major_department` (`department_id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '专业表，仅 admin 维护';

-- 3. 年级表（独立表，便于报表、权限分级、统计及长期运行，需在 class 前创建因 class 有 FK）
DROP TABLE IF EXISTS `grade_level`;
CREATE TABLE `grade_level` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(30) NOT NULL UNIQUE COMMENT '年级名称，如 2024级',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '年级表，仅 admin 维护';

-- 4. 班级表
DROP TABLE IF EXISTS `class`;
CREATE TABLE `class` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '班级名，如 1班、2班',
  `grade_level_id` INT DEFAULT NULL COMMENT '年级 id，关联 grade_level',
  `major_id` INT NOT NULL COMMENT '所属专业',
  `department_id` INT NOT NULL COMMENT '所属学院',
  UNIQUE KEY `uk_class_major_dept` (`name`, `major_id`, `department_id`),
  KEY `idx_class_major` (`major_id`),
  KEY `idx_class_department` (`department_id`),
  KEY `idx_class_grade_level` (`grade_level_id`),
  FOREIGN KEY (`major_id`) REFERENCES `major` (`id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  FOREIGN KEY (`grade_level_id`) REFERENCES `grade_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '班级表，仅 admin 维护';

-- 5. 学生表
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '学号',
  `sname` VARCHAR(50) NOT NULL COMMENT '姓名',
  `password` VARCHAR(100) NOT NULL DEFAULT '123' COMMENT '登录密码',
  `class_id` INT COMMENT '班级 id',
  `grade_level_id` INT COMMENT '年级 id，关联 grade_level',
  `major_id` INT COMMENT '专业 id',
  `department_id` INT COMMENT '学院 id',
  `current_status` ENUM('ACTIVE','SUSPENDED','DROPPED') NOT NULL DEFAULT 'ACTIVE' COMMENT '当前学籍状态：ACTIVE=在读, SUSPENDED=休学, DROPPED=退学',
  KEY `idx_student_class` (`class_id`),
  KEY `idx_student_grade_level` (`grade_level_id`),
  KEY `idx_student_department` (`department_id`),
  FOREIGN KEY (`class_id`) REFERENCES `class` (`id`),
  FOREIGN KEY (`grade_level_id`) REFERENCES `grade_level` (`id`),
  FOREIGN KEY (`major_id`) REFERENCES `major` (`id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '学生表，仅 admin 维护';

-- 6. 教师表
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `teacher_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '工号',
  `tname` VARCHAR(50) NOT NULL COMMENT '姓名',
  `password` VARCHAR(100) NOT NULL DEFAULT '123' COMMENT '登录密码',
  `role` VARCHAR(20) NOT NULL DEFAULT 'teacher' COMMENT '角色：admin / dean / teacher',
  `department_id` INT COMMENT '所属学院，权限边界：院长/教师按此过滤',
  KEY `idx_teacher_role` (`role`),
  KEY `idx_teacher_department` (`department_id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '教师表，仅 admin 维护';

-- 7. 学期表
DROP TABLE IF EXISTS `term`;
CREATE TABLE `term` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '学期名称，如 2024-2025-1',
  `start_date` DATE COMMENT '学期开始日期',
  `end_date` DATE COMMENT '学期结束日期',
  `status` TINYINT DEFAULT 1 COMMENT '1 启用 / 0 停用',
  `remark` VARCHAR(255) COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '学期表，管理学期信息';

-- 8. 课程表（权限边界：按 course.department_id 判断本院，即院长有没有权限查询或修改成绩）
-- course 只描述课程本身属性（名称、学分、性质、考核方式等），不再与任何专业建立直接隶属关系。
-- 专业与课程的培养方案关系，统一由 training_plan 表维护。
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `cname` VARCHAR(100) NOT NULL UNIQUE COMMENT '课程名称',
  `ccredit` DECIMAL(3,1) COMMENT '学分',
  `department_id` INT NOT NULL COMMENT '归属学院，权限判断依据',
  `category` VARCHAR(50) COMMENT '课程类别',
  `exam_method` VARCHAR(50) COMMENT '考核方式',
  `hours` INT COMMENT '学时',
  KEY `idx_course_department` (`department_id`),
  FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '课程资源定义层。只描述课程本身属性，不直接隶属专业；培养方案关系由 training_plan 维护';

-- 9. 培养方案表（【培养方案层】专业-课程规则）
-- 培养方案表（专业-课程规则层）。用于描述课程在某专业中的培养方案角色（必修/限选/任选），
-- 是学生选课、成绩范围、毕业审核、方案管理的唯一依据。
-- 培养方案表（专业-课程规则，支持版本化和多学期）
DROP TABLE IF EXISTS `training_plan`;
CREATE TABLE `training_plan` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `major_id` INT NOT NULL COMMENT '专业 id',
    `course_id` INT NOT NULL COMMENT '课程 id',
    `plan_version` VARCHAR(20) NOT NULL COMMENT '培养方案版本（如 2025版、2026修订版）',
    `course_type` ENUM('REQUIRED','LIMITED','ELECTIVE') NOT NULL COMMENT '课程类型：必修/限选/任选',
    `suggested_grade` INT COMMENT '建议修读年级（1-4）',
    `suggested_term_id` INT COMMENT '建议学期 id，关联 term 表',
    `min_credit` DECIMAL(4,1) COMMENT '模块最低学分',
    `max_credit` DECIMAL(4,1) COMMENT '模块最高学分',
    `max_capacity` INT DEFAULT 0 COMMENT '本专业该课程学生的最大容量，0表示不限制',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 启用 / 0 停用',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_tp_major_course_version` (`major_id`, `course_id`, `plan_version`),
    KEY `idx_tp_major` (`major_id`),
    KEY `idx_tp_course` (`course_id`),
    FOREIGN KEY (`major_id`) REFERENCES `major` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`suggested_term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培养方案表（版本化）。同专业不同版本可共存，记录课程在专业中的角色';


-- 10. 开课表（【教学实施层】教师任课；教师权限按 course_open 校验）
DROP TABLE IF EXISTS `course_open`;
CREATE TABLE `course_open` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `course_id` INT NOT NULL COMMENT '课程 id',
  `teacher_id` INT NOT NULL COMMENT '任课教师 id',
  `term_id` INT NOT NULL COMMENT '学期 id，关联 term 表',
  UNIQUE KEY `uk_course_teacher_term` (`course_id`, `teacher_id`, `term_id`),
  KEY `idx_course_open_teacher` (`teacher_id`),
  KEY `idx_course_open_term` (`term_id`),
  FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`),
  FOREIGN KEY (`term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '开课表（教学实施层）；教师权限依据此表';

-- 11. 开课班级表
DROP TABLE IF EXISTS `course_open_class`;
CREATE TABLE `course_open_class` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `course_open_id` INT NOT NULL COMMENT '开课 id',
  `class_id` INT NOT NULL COMMENT '班级 id',
  UNIQUE KEY `uk_course_open_class` (`course_open_id`, `class_id`),
  FOREIGN KEY (`course_open_id`) REFERENCES `course_open` (`id`),
  FOREIGN KEY (`class_id`) REFERENCES `class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '开课班级表；关联开课和班级';

-- -----------------------------------------------------------------------------
-- 二、成绩事实层（依赖 课程资源层 → 培养方案层 → 教学实施层）
-- -----------------------------------------------------------------------------

-- 12. 成绩主表（事实层：当前有效成绩；学生仅可见 status=已发布）
DROP TABLE IF EXISTS `grade_change_log`;
DROP TABLE IF EXISTS `grade_change_request`;
DROP TABLE IF EXISTS `score`;

CREATE TABLE `score` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` INT NOT NULL COMMENT '学生 id',
  `course_id` INT NOT NULL COMMENT '课程 id',
  `teacher_id` INT COMMENT '任课教师 id',
  `term_id` INT NOT NULL COMMENT '学期 id',
  `usual_score` DECIMAL(5,2) COMMENT '平时成绩',
  `mid_score` DECIMAL(5,2) COMMENT '期中成绩',
  `final_score` DECIMAL(5,2) COMMENT '期末成绩',
  `grade` DECIMAL(5,2) COMMENT '总成绩',
  `remark` VARCHAR(500) COMMENT '备注',
  `status` VARCHAR(20) NOT NULL DEFAULT 'UPLOADED' COMMENT '状态：UPLOADED 已上传 / PUBLISHED 已发布 / LOCKED 锁定',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_score_student_course_term` (`student_id`, `course_id`, `term_id`),
  KEY `idx_score_course` (`course_id`),
  KEY `idx_score_term` (`term_id`),
  KEY `idx_score_status` (`status`),
  KEY `idx_score_teacher` (`teacher_id`),
  FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`),
  FOREIGN KEY (`term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '成绩主表（事实层）；学号+课程+学期唯一';

-- 13. 成绩修改申请表（流程控制层）
CREATE TABLE `grade_change_request` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `score_id` INT NOT NULL COMMENT '目标成绩记录 id',
  `applicant_id` INT NOT NULL COMMENT '申请人 id（教师/院长）',
  `applicant_role` VARCHAR(20) NOT NULL COMMENT '申请人角色 teacher/dean/admin',
  `reason` VARCHAR(500) NOT NULL COMMENT '申请原因',
  `attachment_path` VARCHAR(255) COMMENT '证明附件相对路径，如 grades/change_request/2026/01/uuid_20260126.pdf',
  `attachment_name` VARCHAR(255) COMMENT '附件原始文件名',
  `before_data` JSON COMMENT '原成绩JSON：{usual_score, mid_score, final_score, grade}',
  `after_data` JSON COMMENT '目标成绩JSON：{usual_score, mid_score, final_score, grade}',
  `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING 待审 / DEAN_APPROVED 院长已通过 / APPROVED 管理员通过 / REJECTED 拒绝',
  `dean_approver_id` INT COMMENT '院长审批人 id',
  `admin_approver_id` INT COMMENT '管理员审批人 id',
  `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审批时间',
  KEY `idx_request_score` (`score_id`),
  KEY `idx_request_applicant` (`applicant_id`),
  KEY `idx_request_status` (`status`),
  FOREIGN KEY (`score_id`) REFERENCES `score` (`id`),
  FOREIGN KEY (`applicant_id`) REFERENCES `teacher` (`id`),
  FOREIGN KEY (`dean_approver_id`) REFERENCES `teacher` (`id`),
  FOREIGN KEY (`admin_approver_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '成绩修改申请；仅申请+审批流，禁止直接改 score';

-- 14. 成绩审计表（审计追溯层；不可删除、不可修改）
CREATE TABLE `grade_change_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `score_id` INT NOT NULL COMMENT '关联成绩 id',
  `operation` VARCHAR(30) NOT NULL COMMENT '操作：IMPORT 导入 / PUBLISH 发布 / CHANGE 修改',
  `operator_id` INT COMMENT '操作人 id',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `before_json` JSON COMMENT '变更前快照',
  `after_json` JSON COMMENT '变更后快照',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  KEY `idx_log_score` (`score_id`),
  KEY `idx_log_created` (`created_at`),
  FOREIGN KEY (`score_id`) REFERENCES `score` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '成绩审计表；只追加，禁止 DELETE/UPDATE';

-- -----------------------------------------------------------------------------
-- 三、辅助表（导入记录、操作日志、试卷分析）
-- -----------------------------------------------------------------------------

-- 15. 导入记录表
DROP TABLE IF EXISTS `score_import_record`;
CREATE TABLE `score_import_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) COMMENT '存储路径',
  `term_id` INT COMMENT '学期 id，关联 term 表',
  `course_id` INT COMMENT '课程 id',
  `teacher_id` INT COMMENT '任课教师 id',
  `department_id` INT COMMENT '学院 id',
  `operator` VARCHAR(50) COMMENT '上传人',
  `status` VARCHAR(20) COMMENT '导入状态',
  `message` TEXT COMMENT '说明',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_import_department` (`department_id`),
  KEY `idx_import_created` (`created_at`),
  FOREIGN KEY (`term_id`) REFERENCES `term`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `operator` VARCHAR(50) COMMENT '操作者',
  `operation_type` VARCHAR(50) COMMENT '操作类型',
  `target_table` VARCHAR(50) COMMENT '目标表',
  `target_id` BIGINT COMMENT '目标 id',
  `content` TEXT COMMENT '内容',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_op_operator` (`operator`),
  KEY `idx_op_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. 试卷分析表
DROP TABLE IF EXISTS `exam_paper_analysis`;
CREATE TABLE `exam_paper_analysis` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) COMMENT '文件路径',
  `upload_by` VARCHAR(50) COMMENT '上传人',
  `uploader_role` VARCHAR(20) DEFAULT 'teacher' COMMENT '上传人角色',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 18. 教师职务/部门历史变动表（标准化）
DROP TABLE IF EXISTS `teacher_assignment_history`;
CREATE TABLE `teacher_assignment_history` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  teacher_id INT NOT NULL COMMENT '教师id',
  old_department_id INT COMMENT '原所属学院/部门',
  new_department_id INT NULL COMMENT '新所属学院/部门，可为空（离职、挂职）',
  old_position VARCHAR(100) COMMENT '原岗位/职务',
  new_position VARCHAR(100) COMMENT '新岗位/职务，可为空',
  change_type ENUM('TRANSFER','RESIGN','SECONDMENT','PART_TIME','OTHER') NOT NULL COMMENT '变动类型：调岗/离职/挂职/兼职/其他',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 离职，1 在职，2 挂职/兼职等',
  effective_date DATE COMMENT '变动生效日期',
  remark VARCHAR(255) COMMENT '备注或原因',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  FOREIGN KEY (teacher_id) REFERENCES `teacher`(id),
  FOREIGN KEY (old_department_id) REFERENCES `department`(id),
  FOREIGN KEY (new_department_id) REFERENCES `department`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师职务/部门历史变动表（支持离职、挂职、兼职等）';

-- 19. 学生学籍状态/班级/专业/学院变动历史表（标准化）
DROP TABLE IF EXISTS `student_status_history`;
CREATE TABLE `student_status_history` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL COMMENT '学生id',
  old_department_id INT NULL COMMENT '原所属学院',
  old_major_id INT NULL COMMENT '原专业',
  old_class_id INT NULL COMMENT '原班级',
  new_department_id INT NULL COMMENT '新所属学院',
  new_major_id INT NULL COMMENT '新专业',
  new_class_id INT NULL COMMENT '新班级',
  change_type ENUM(
      'TRANSFER_MAJOR',  -- 转专业
      'TRANSFER_CLASS',  -- 调班
      'SUSPEND',         -- 休学
      'RESUME',          -- 复学
      'DROP_OUT',        -- 退学
      'OTHER'            -- 其他
  ) NOT NULL COMMENT '变动类型：TRANSFER_MAJOR=转专业, TRANSFER_CLASS=调班, SUSPEND=休学, RESUME=复学, DROP_OUT=退学, OTHER=其他',
  status TINYINT COMMENT '学生状态：0 退学/休学, 1 在读',
  reason VARCHAR(255) COMMENT '变动原因说明',
  effective_date DATE COMMENT '生效日期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  FOREIGN KEY (student_id) REFERENCES `student`(id),
  FOREIGN KEY (old_department_id) REFERENCES `department`(id),
  FOREIGN KEY (new_department_id) REFERENCES `department`(id),
  FOREIGN KEY (old_major_id) REFERENCES `major`(id),
  FOREIGN KEY (new_major_id) REFERENCES `major`(id),
  FOREIGN KEY (old_class_id) REFERENCES `class`(id),
  FOREIGN KEY (new_class_id) REFERENCES `class`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生学籍历史变动表（支持退学/休学/转班/转专业等）';

-- =============================================================================
-- 四、初始数据（admin / dean / teacher / student 示例，密码均为 123）
-- 课程为资源层；专业-课程关系通过 training_plan 初始化。
-- =============================================================================

-- 学院（3 个）
INSERT INTO `department` (`id`, `name`) VALUES
(1, '经济管理学院'),
(2, '医学技术学院'),
(3, '康复治疗学院');

-- 专业（每学院 1 个，共 3 个）
INSERT INTO `major` (`id`, `name`, `department_id`) VALUES
(1, '会计学', 1),
(2, '医学检验技术', 2),
(3, '康复治疗技术', 3);

-- 年级（4 个年级，需在 class 前插入，因 class 有 grade_level_id 外键）
INSERT INTO `grade_level` (`id`, `name`, `sort_order`) VALUES
(1, '2022级', 1),
(2, '2023级', 2),
(3, '2024级', 3),
(4, '2025级', 4);

-- 班级（会计学 2 个班，其它各 1 个班，共 4 个；grade_level_id 3=2024级）
INSERT INTO `class` (`id`, `name`, `grade_level_id`, `major_id`, `department_id`) VALUES
(1, '1班', 3, 1, 1),
(2, '2班', 3, 1, 1),
(3, '1班', 3, 2, 2),
(4, '1班', 3, 3, 3);

-- 教师（1 管理员 + 3 院长 + 3 教师，密码 123）
INSERT INTO `teacher` (`id`, `teacher_no`, `tname`, `password`, `role`, `department_id`) VALUES
(1, 'admin', '系统管理员', '123', 'admin', 1),
(2, 'dean1', '经济管理学院院长', '123', 'dean', 1),
(3, 'dean2', '医学技术学院院长', '123', 'dean', 2),
(4, 'dean3', '康复治疗学院院长', '123', 'dean', 3),
(5, 'T1001', '张老师', '123', 'teacher', 1),
(6, 'T2001', '李老师', '123', 'teacher', 2),
(7, 'T3001', '王老师', '123', 'teacher', 3);

-- 学期（4 个学期：当前及历史）
INSERT INTO `term` (`id`, `name`, `start_date`, `end_date`, `status`, `remark`) VALUES
(1, '2024-2025-1', '2024-09-01', '2025-01-15', 1, '2024-2025 学年第一学期'),
(2, '2024-2025-2', '2025-02-20', '2025-07-10', 1, '2024-2025 学年第二学期'),
(3, '2023-2024-1', '2023-09-01', '2024-01-15', 1, '2023-2024 学年第一学期'),
(4, '2023-2024-2', '2024-02-20', '2024-07-10', 1, '2023-2024 学年第二学期');

-- 学生（8 人，分布各班，密码 123；学号规则：S+年级后2位+班号2位+序号2位）
INSERT INTO `student` (`id`, `student_no`, `sname`, `password`, `class_id`, `grade_level_id`, `major_id`, `department_id`) VALUES
(1, 'student', '学生本人', '123', 1, 3, 1, 1),
(2, 'S240101', '赵小明', '123', 1, 3, 1, 1),
(3, 'S240102', '钱小红', '123', 1, 3, 1, 1),
(4, 'S240201', '孙小军', '123', 2, 3, 1, 1),
(5, 'S240301', '李医学', '123', 3, 3, 2, 2),
(6, 'S240302', '周检验', '123', 3, 3, 2, 2),
(7, 'S240401', '吴康复', '123', 4, 3, 3, 3),
(8, 'S240402', '郑治疗', '123', 4, 3, 3, 3);

-- 课程（6 门：3 门专业课 + 2 门公共课，course 为资源层不含专业绑定）
INSERT INTO `course` (`id`, `cname`, `ccredit`, `department_id`, `category`, `exam_method`, `hours`) VALUES
(1, '基础会计', 3, 1, '专业课', '考试', 48),
(2, '经济学基础', 3, 1, '专业课', '考试', 48),
(3, '临床检验基础', 4, 2, '专业课', '考试', 64),
(4, '康复评定技术', 4, 3, '专业课', '考试', 64),
(5, '大学英语', 4, 1, '公共课', '考试', 64),
(6, '马克思主义基本原理', 3, 1, '公共课', '考试', 48);

-- 培养方案（专业-课程关系，2024版；公共课 5、6 纳入各专业）
INSERT INTO `training_plan` (`major_id`, `course_id`, `plan_version`, `course_type`, `suggested_grade`, `suggested_term_id`, `min_credit`, `status`) VALUES
(1, 1, '2024版', 'REQUIRED', 1, 1, 3, 1),
(1, 2, '2024版', 'REQUIRED', 1, 1, 3, 1),
(1, 5, '2024版', 'REQUIRED', 1, 1, 4, 1),
(1, 6, '2024版', 'REQUIRED', 1, 1, 3, 1),
(2, 3, '2024版', 'REQUIRED', 1, 1, 4, 1),
(2, 5, '2024版', 'REQUIRED', 1, 1, 4, 1),
(2, 6, '2024版', 'REQUIRED', 1, 1, 3, 1),
(3, 4, '2024版', 'REQUIRED', 1, 1, 4, 1),
(3, 5, '2024版', 'REQUIRED', 1, 1, 4, 1),
(3, 6, '2024版', 'REQUIRED', 1, 1, 3, 1);

-- 开课（2024-2025-1 学期，6 门课；张老师教 1、2、5、6，李老师教 3，王老师教 4）
INSERT INTO `course_open` (`id`, `course_id`, `teacher_id`, `term_id`) VALUES
(1, 1, 5, 1),
(2, 2, 5, 1),
(3, 3, 6, 1),
(4, 4, 7, 1),
(5, 5, 5, 1),
(6, 6, 5, 1);

-- 开课班级（课程 1、2 面向会计 1 班、2 班；3 面向医学 1 班；4 面向康复 1 班；5、6 面向全部班级）
INSERT INTO `course_open_class` (`course_open_id`, `class_id`) VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 1),
(5, 2),
(5, 3),
(5, 4),
(6, 1),
(6, 2),
(6, 3),
(6, 4);

-- 成绩（2024-2025-1 学期，部分已发布、部分已上传待审核）
INSERT INTO `score` (`student_id`, `course_id`, `teacher_id`, `term_id`, `usual_score`, `mid_score`, `final_score`, `grade`, `status`) VALUES
(1, 1, 5, 1, 85.00, 80.00, 90.00, 88.50, 'PUBLISHED'),
(2, 1, 5, 1, 78.00, 75.00, 82.00, 80.80, 'PUBLISHED'),
(3, 1, 5, 1, 92.00, 90.00, 88.00, 89.20, 'PUBLISHED'),
(4, 1, 5, 1, 88.00, 85.00, 90.00, 88.50, 'PUBLISHED'),
(1, 2, 5, 1, 90.00, 85.00, 85.00, 86.50, 'PUBLISHED'),
(2, 2, 5, 1, 82.00, 78.00, 88.00, 84.80, 'PUBLISHED'),
(5, 3, 6, 1, 88.00, 82.00, 86.00, 86.80, 'PUBLISHED'),
(6, 3, 6, 1, 75.00, 72.00, 78.00, 76.20, 'PUBLISHED'),
(7, 4, 7, 1, 82.00, 80.00, 90.00, 87.20, 'UPLOADED'),
(8, 4, 7, 1, 91.00, 88.00, 93.00, 91.20, 'PUBLISHED'),
(1, 5, 5, 1, 90.00, 85.00, 88.00, 88.00, 'PUBLISHED'),
(5, 5, 5, 1, 85.00, 80.00, 82.00, 82.00, 'PUBLISHED'),
(1, 6, 5, 1, 88.00, 82.00, 85.00, 85.00, 'PUBLISHED');

-- 操作日志示例（便于测试日志查询）
INSERT INTO `operation_log` (`operator`, `operation_type`, `target_table`, `content`, `created_at`) VALUES
('系统管理员', '新增', 'score', '成绩批量导入，成功 12 条', NOW()),
('系统管理员', '编辑', 'score', '发布成绩，score_id=1', NOW());

SET FOREIGN_KEY_CHECKS = 1;
