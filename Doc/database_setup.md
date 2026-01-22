# 数据库设置说明

## 数据库脚本文件

### 主要脚本

1. **`mysql/studentms_complete.sql`** ⭐ **推荐使用**
   - 完整的数据库初始化脚本
   - 包含所有基础表和升级后的表结构
   - 包含初始测试数据
   - 适用于全新安装

## 数据库结构

### 基础表
- `s` - 学生表（包含 class_id, grade_level, major_id, department_id）
- `t` - 教师表（包含 role 字段，区分 admin 和 teacher）
- `c` - 课程表（包含 major_id, department_id）
- `ct` - 课程教师关联表
- `sct` - 学生课程成绩表（包含 usual_grade, final_grade, total_grade 等）

### 新增表
- `department` - 学院表
- `major` - 专业表
- `class` - 班级表
- `word_papers` - Word 文件表
- `operation_log` - 操作日志表

## 安装步骤

### 方式1：全新安装（推荐）

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 执行完整初始化脚本
source mysql/studentms_complete.sql

# 或者直接执行
mysql -u root -p < mysql/studentms_complete.sql
```

## 默认账号

执行 `studentms_complete.sql` 后，系统包含以下默认账号：

- **Admin 账号：**
  - ID: 6
  - 用户名: admin
  - 密码: 123
  - 角色: admin

- **Teacher 账号：**
  - ID: 4, 用户名: 李玉民, 密码: 123
  - ID: 13, 用户名: 张三, 密码: 123

- **Student 账号：**
  - 多个测试学生账号，密码多为 123 或 1234

## 注意事项

1. **字符集**：数据库使用 utf8mb4 字符集，支持中文和特殊字符
2. **外键约束**：表之间设置了外键约束，删除数据时需要注意
3. **索引**：已为常用查询字段创建索引，提升查询性能
4. **角色字段**：教师表的 `role` 字段默认值为 'teacher'，admin 用户需要设置为 'admin'

## 数据库维护

### 备份数据库
```bash
mysqldump -u root -p studentms > studentms_backup.sql
```

### 恢复数据库
```bash
mysql -u root -p studentms < studentms_backup.sql
```

### 查看表结构
```sql
USE studentms;
SHOW TABLES;
DESC table_name;  -- 查看具体表结构
```

## 常见问题

### Q: 如何重置数据库？
A: 删除数据库后重新执行 `studentms_complete.sql`：
```sql
DROP DATABASE studentms;
-- exit后，然后执行 mysql -u root -p < studentms_complete.sql

```

### Q: Admin 账号无法登录？
A: 检查 `t` 表中 admin 用户的 `role` 字段是否为 'admin'：
```sql
SELECT * FROM t WHERE tname = 'admin';
UPDATE t SET role = 'admin' WHERE tname = 'admin';
```
