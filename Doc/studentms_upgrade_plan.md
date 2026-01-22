# 学生成绩管理系统升级开发计划（含Cursor一键生成脚本）

## 一、项目概述

在现有 `studentms` 项目基础上，升级系统以满足学院、专业、班级、教师、学生的完整管理需求，同时支持成绩导入、补考筛选、文档管理和操作审计。

**技术环境**：
- 后端：Java 17 + Spring Boot + MyBatis  
- 前端：Vue.js + Element UI  
- 数据库：MySQL 8.0（现有备份为 MySQL 5.7）  
- 构建工具：Maven 3.9.9  
- Node：20.20.0  

## 二、功能需求整合与阶段规划

| 阶段 | 涵盖原需求 | 开发逻辑/依赖 |
|--------|------------|---------------|
| 阶段1 | 基础数据管理 + 权限 | 学院、专业、班级、教师、学生、课程 CRUD；admin 权限控制；为后续成绩和查询提供基础数据 |
| 阶段2 | 成绩上传与解析 | 上传 Excel → 解析 → 写入数据库（含平时、期末、总成绩、学期、班级、专业、系） |
| 阶段3 | 成绩查询与补考处理 | 多维度查询 + 筛选成绩 < 60 → 导出 Excel；依赖阶段1和阶段2的数据 |
| 阶段4 | Word 文件管理 | 上传/下载 Word 文件，前端展示列表 |
| 阶段5 | 操作日志审计 | AOP 拦截增删改操作 → 写入操作日志 → 前端查看日志 |

## 三、数据库改造建议

```sql
-- 学生表改造
ALTER TABLE s
ADD COLUMN class_id INT,
ADD COLUMN grade_level VARCHAR(10),
ADD COLUMN major_id INT,
ADD COLUMN department_id INT;

-- 课程表改造
ALTER TABLE c
ADD COLUMN major_id INT,
ADD COLUMN department_id INT;

-- 学生成绩表改造
ALTER TABLE sct
ADD COLUMN usual_grade FLOAT,  -- 平时成绩
ADD COLUMN final_grade FLOAT,  -- 期末成绩
ADD COLUMN total_grade FLOAT;  -- 总成绩

-- Word 文件表
CREATE TABLE word_papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255),
    file_path VARCHAR(255),
    upload_by VARCHAR(50),
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志表
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator VARCHAR(50),
    operation_type VARCHAR(50),
    target_table VARCHAR(50),
    target_id BIGINT,
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 四、Cursor 一键生成脚本（按阶段）

### 阶段1：基础数据管理 + 权限控制
```text
Generate Java entity classes: Department(id, name), Major(id, name, departmentId), Class(id, name, majorId, departmentId), Teacher(id, name, password, role), Student(id, name, password, classId, gradeLevel, majorId, departmentId), Course(id, name, credit, majorId, departmentId)
Generate MyBatis Mapper interfaces and XML for CRUD
Generate Service + ServiceImpl for all entities
Generate REST Controllers with admin-only access
Generate Vue.js pages with table + add/edit/delete + pagination + search
```

### 阶段2：Excel 成绩上传
```text
Generate Java entity class: Grade(id, studentId, courseId, teacherId, usualGrade, finalGrade, totalGrade, semester, classId, majorId, departmentId)
Generate MyBatis Mapper and XML for Grade insert/batch insert
Generate GradeService with method uploadExcel(MultipartFile file) to parse Excel and save to DB
Generate GradeController with POST /api/grades/upload
Generate Vue.js page with file upload button, preview, and submit
```

### 阶段3：成绩查询 + 补考导出
```text
Generate Controller methods: GET /api/grades/query, GET /api/grades/reexamination, GET /api/grades/reexamination/export
Generate GradeService methods: queryGrades(filterConditions), getReexaminationList(filterConditions), exportReexaminationToExcel(list)
Generate Vue.js page with filters: department, major, class, course, semester, min/max grade, export button
```

### 阶段4：Word 文件管理
```text
Generate Java entity class: WordPaper(id, fileName, filePath, uploadBy, uploadTime)
Generate MyBatis Mapper and XML for WordPaper
Generate WordPaperService with methods: upload(MultipartFile file), download(Long id)
Generate WordPaperController with POST /api/papers/upload, GET /api/papers/{id}/download
Generate Vue.js page to list uploaded files with upload/download buttons
```

### 阶段5：操作日志审计
```text
Generate Java entity class: OperationLog(id, operator, operationType, targetTable, targetId, content, createTime)
Generate MyBatis Mapper and XML for OperationLog insert/query
Generate OperationLogService with method recordOperation(operator, operationType, targetTable, targetId, content)
Generate Spring AOP interceptor to capture all Service layer insert/update/delete operations and call OperationLogService
Generate Vue.js page to list logs with filters: operator, operation type, time range
```

## 五、开发步骤详细指南

1. 执行数据库改造（新增字段和新表）
2. 阶段1: 基础数据管理 + 权限控制
3. 阶段2: Excel 成绩上传模块
4. 阶段3: 多维度成绩查询 + 补考导出
5. 阶段4: Word 文件管理模块
6. 阶段5: 操作日志审计模块

> 建议: Cursor 先生成模块骨架，然后手动完善复杂业务逻辑（Excel解析、Word存储、多条件动态 SQL、日志格式）。

