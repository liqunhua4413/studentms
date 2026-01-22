# 学生成绩管理系统升级实现总结

## 已完成功能

### 阶段1：基础数据管理 + 权限控制 ✅

**新增实体类：**
- `Department` - 学院实体类
- `Major` - 专业实体类
- `Class` - 班级实体类

**更新的实体类：**
- `Student` - 添加了 classId, gradeLevel, majorId, departmentId 字段
- `Course` - 添加了 majorId, departmentId 字段
- `Teacher` - 添加了 role 字段（admin/teacher）

**创建的文件：**
- `DepartmentMapper.java` / `DepartmentMapper.xml`
- `MajorMapper.java` / `MajorMapper.xml`
- `ClassMapper.java` / `ClassMapper.xml`
- `DepartmentService.java` / `DepartmentController.java`
- `MajorService.java` / `MajorController.java`
- `ClassService.java` / `ClassController.java`

**数据库脚本：**
- `mysql/studentms_complete.sql` - 完整的数据库初始化脚本（包含基础表和所有升级后的表结构）

### 阶段2：Excel 成绩上传 ✅

**更新的实体类：**
- `StudentCourseTeacher` - 添加了 usualGrade, finalGrade, totalGrade, classId, majorId, departmentId 字段

**创建的文件：**
- `GradeService.java` - Excel 解析和批量插入功能
- `GradeController.java` - 成绩上传接口

**功能：**
- 支持 Excel 文件上传（.xlsx, .xls）
- 解析 Excel 并批量插入成绩数据
- 验证学生、课程、教师是否存在
- 返回详细的成功/失败统计信息

### 阶段3：成绩查询 + 补考导出 ✅

**更新的文件：**
- `SCTInfo.java` - 添加了新字段支持
- `StudentCourseTeacherMapper.xml` - 更新查询支持多维度筛选
- `SCTService.java` - 添加补考查询方法
- `GradeService.java` - 添加 Excel 导出功能
- `GradeController.java` - 添加查询和导出接口

**功能：**
- 多维度成绩查询（支持学院、专业、班级、课程、学期、成绩范围等筛选）
- 补考名单查询（总成绩 < 60）
- 补考名单导出为 Excel 文件

### 阶段4：Word 文件管理 ✅

**创建的文件：**
- `WordPaper.java` - Word 文件实体类
- `WordPaperMapper.java` / `WordPaperMapper.xml`
- `WordPaperService.java` - 文件上传、下载、删除功能
- `WordPaperController.java` - 文件管理接口

**功能：**
- Word 文件上传（.doc, .docx）
- 文件下载
- 文件列表查询
- 文件删除（同时删除物理文件）

### 阶段5：操作日志审计 ✅

**创建的文件：**
- `OperationLog.java` - 操作日志实体类
- `OperationLogMapper.java` / `OperationLogMapper.xml`
- `OperationLogService.java` - 日志记录和查询功能
- `OperationLogAspect.java` - AOP 切面，自动拦截增删改操作
- `OperationLogController.java` - 日志查看接口

**功能：**
- 自动记录所有 Service 层的 INSERT、UPDATE、DELETE 操作
- 记录操作者、操作类型、目标表、目标ID、操作内容、操作时间
- 支持按操作者、操作类型、目标表、时间范围查询日志

## 更新的配置文件

- `pom.xml` - 添加了 AOP 和 Excel 处理依赖
- `application.yml` - 添加了文件上传配置

## API 接口列表

### 基础数据管理
- `POST /api/department/add` - 添加学院
- `GET /api/department/findAll` - 查询所有学院
- `POST /api/department/findBySearch` - 搜索学院
- `POST /api/department/update` - 更新学院
- `GET /api/department/deleteById/{id}` - 删除学院

- `POST /api/major/add` - 添加专业
- `GET /api/major/findAll` - 查询所有专业
- `GET /api/major/findByDepartmentId/{departmentId}` - 按学院查询专业
- `POST /api/major/findBySearch` - 搜索专业
- `POST /api/major/update` - 更新专业
- `GET /api/major/deleteById/{id}` - 删除专业

- `POST /api/class/add` - 添加班级
- `GET /api/class/findAll` - 查询所有班级
- `GET /api/class/findByMajorId/{majorId}` - 按专业查询班级
- `POST /api/class/findBySearch` - 搜索班级
- `POST /api/class/update` - 更新班级
- `GET /api/class/deleteById/{id}` - 删除班级

### 成绩管理
- `POST /api/grade/upload` - 上传 Excel 成绩文件
- `POST /api/grade/query` - 多维度成绩查询
- `POST /api/grade/reexamination` - 查询补考名单
- `POST /api/grade/reexamination/export` - 导出补考名单 Excel

### Word 文件管理
- `POST /api/paper/upload` - 上传 Word 文件
- `GET /api/paper/{id}/download` - 下载 Word 文件
- `GET /api/paper/findAll` - 查询所有文件
- `POST /api/paper/findBySearch` - 搜索文件
- `GET /api/paper/deleteById/{id}` - 删除文件

### 操作日志
- `GET /api/operationLog/findAll` - 查询所有日志
- `GET /api/operationLog/findById/{id}` - 查询单条日志
- `POST /api/operationLog/findBySearch` - 搜索日志（支持操作者、操作类型、目标表、时间范围）

## 数据库初始化

**推荐方式（全新安装）：**
执行 `mysql/studentms_complete.sql` 脚本初始化完整数据库。脚本包含：
1. 创建所有基础表（学生、教师、课程、成绩等）
2. 创建学院、专业、班级表
3. 创建 Word 文件表和操作日志表
4. 所有表都包含升级后的完整字段结构
5. 创建必要的索引
6. 插入初始测试数据

**执行命令：**
```bash
mysql -u root -p < mysql/studentms_complete.sql
```

**注意：** 
- 如果已有数据库需要升级，可以使用 `mysql/upgrade_safe.sql`（使用存储过程检查，避免重复添加列的错误）
- 详细说明请参考 `Doc/database_setup.md`

## 新增功能（已完成）

### 权限拦截器 ✅

**创建的文件：**
- `AdminInterceptor.java` - Admin 权限拦截器
- `WebMvcConfig.java` - Web MVC 配置，注册拦截器

**功能：**
- 拦截需要 admin 权限的接口（学院、专业、班级管理，成绩上传，Word文件管理等）
- 从请求头或参数中获取操作者信息
- 验证操作者是否为 admin，非 admin 用户返回权限错误

### 操作日志操作者获取 ✅

**更新的文件：**
- `OperationLogAspect.java` - 从请求中获取操作者信息
- `axios.js` - 前端请求拦截器，自动添加 Operator 请求头

**功能：**
- 前端自动从 sessionStorage 获取用户信息，设置到请求头
- 后端 AOP 切面从请求头或 request 属性中获取操作者
- 支持 admin、teacher、student 三种角色

### 前端页面实现 ✅

**创建的前端页面：**

#### 基础数据管理
- `departmentManage/index.vue` - 学院管理主页面
- `departmentManage/departmentList.vue` - 学院列表
- `departmentManage/addDepartment.vue` - 添加学院
- `departmentManage/editorDepartment.vue` - 编辑学院
- `departmentManage/queryDepartment.vue` - 搜索学院

- `majorManage/index.vue` - 专业管理主页面
- `majorManage/majorList.vue` - 专业列表
- `majorManage/addMajor.vue` - 添加专业
- `majorManage/editorMajor.vue` - 编辑专业
- `majorManage/queryMajor.vue` - 搜索专业

- `classManage/index.vue` - 班级管理主页面
- `classManage/classList.vue` - 班级列表
- `classManage/addClass.vue` - 添加班级
- `classManage/editorClass.vue` - 编辑班级
- `classManage/queryClass.vue` - 搜索班级

#### 成绩管理
- `gradeManage/index.vue` - 成绩管理主页面
- `gradeManage/uploadGrade.vue` - Excel 成绩上传页面
- `gradeManage/queryGrade.vue` - 成绩查询和补考导出页面

#### Word 文件管理
- `wordPaperManage/index.vue` - Word 文件管理主页面
- `wordPaperManage/wordPaperList.vue` - 文件列表、上传、下载、删除页面

#### 操作日志
- `operationLogManage/index.vue` - 操作日志查看页面，支持多条件查询

**更新的文件：**
- `router/index.js` - 添加所有新页面的路由配置
- `plugins/axios.js` - 添加请求拦截器，自动设置 Operator 请求头

## 注意事项

1. **文件上传路径**：Word 文件默认保存在 `./uploads` 目录，可在 `application.yml` 中配置
2. **操作日志操作者**：已实现从请求头获取操作者信息，前端会自动设置 Operator 请求头
3. **Excel 格式**：成绩上传 Excel 格式为：学号、学生姓名、课程名、教师姓名、平时成绩、期末成绩、总成绩、学期、班级ID、专业ID、系ID
4. **权限控制**：已实现权限拦截器，需要 admin 权限的接口会自动验证操作者身份
5. **前端路由**：所有新页面已添加到 admin 路由下，可通过侧边栏菜单访问

## 使用说明

### 后端启动
1. 执行 `mysql/studentms_complete.sql` 脚本初始化数据库（全新安装）
2. 启动 Spring Boot 应用（端口 10086）

### 前端启动
1. 进入 `student_client` 目录
2. 运行 `npm install` 安装依赖
3. 运行 `npm run serve` 启动开发服务器

### 访问页面
- 登录页面：`http://localhost:8080/login`
- 使用 admin 账号登录后，可在侧边栏看到新增的菜单：
  - 学院管理
  - 专业管理
  - 班级管理
  - 成绩管理（成绩上传、成绩查询）
  - Word文件管理
  - 操作日志

## 已完成功能清单

✅ 阶段1：基础数据管理 + 权限控制（后端 + 前端）
✅ 阶段2：Excel 成绩上传（后端 + 前端）
✅ 阶段3：成绩查询 + 补考导出（后端 + 前端）
✅ 阶段4：Word 文件管理（后端 + 前端）
✅ 阶段5：操作日志审计（后端 + 前端）
✅ 权限拦截器实现
✅ 操作日志操作者获取完善
✅ 前端页面完整实现

## 后续优化建议

1. 添加数据验证和异常处理增强
2. 添加单元测试
3. 优化前端用户体验（加载动画、错误提示等）
4. 添加批量操作功能
5. 实现数据导出功能增强
