package com.auggie.student_server.service;

import com.auggie.student_server.entity.StudentCourseTeacher;
import com.auggie.student_server.entity.ScoreImportRecord;
import com.auggie.student_server.mapper.StudentCourseTeacherMapper;
import com.auggie.student_server.mapper.StudentMapper;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import com.auggie.student_server.mapper.ScoreImportRecordMapper;
import com.auggie.student_server.mapper.MajorMapper;
import com.auggie.student_server.mapper.ClassMapper;
import com.auggie.student_server.entity.Student;
import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.entity.Major;
import com.auggie.student_server.entity.SCTInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.auggie.student_server.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: GradeService - 成绩上传服务
 * @Version 1.0.0
 */

@Service
public class GradeService {
    @Autowired
    private StudentCourseTeacherMapper studentCourseTeacherMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private ScoreImportRecordMapper scoreImportRecordMapper;
    @Autowired
    private StudentService studentService;
    @Autowired
    private DepartmentMapper departmentMapper;

    @Value("${file.upload.path:${user.home}/studentms/uploads}")
    private String uploadPath;

    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private ClassMapper classMapper;

    /**
     * 解析 Excel 文件并批量插入成绩（含严格业务校验）
     */
    public String uploadExcel(MultipartFile file, String operator, Integer selectedDepartmentId) {
        try {
            // 1. 基本检查与保存文件
            if (selectedDepartmentId == null) return "上传失败：请先选择学院";
            com.auggie.student_server.entity.Department dept = departmentMapper.findById(selectedDepartmentId);
            if (dept == null) return "上传失败：选择的学院不存在";

            String originalFilename = file.getOriginalFilename();
            String resolvedPath = uploadPath;
            if (resolvedPath.startsWith("./") || resolvedPath.startsWith(".\\")) {
                resolvedPath = new File(".").getCanonicalPath() + File.separator + resolvedPath.substring(2);
            }
            Path uploadDir = Paths.get(resolvedPath, "grade_excel");
            Files.createDirectories(uploadDir);
            String savedFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
            Path savedFilePath = uploadDir.resolve(savedFileName);
            file.transferTo(savedFilePath.toFile());

            // 2. 解析 Excel - 使用保存后的文件，避免 MultipartFile 临时文件被删除的问题
            Workbook workbook = new XSSFWorkbook(savedFilePath.toFile());
            Sheet sheet = workbook.getSheetAt(0);
            List<StudentCourseTeacher> validGradeList = new ArrayList<>();
            List<String> errorReports = new ArrayList<>();

            // 解析第2行：课程与教师
            Row row2 = sheet.getRow(1);
            String row2Text = getMergedCellValue(sheet, row2, 0);
            String courseName = extractValue(row2Text, "课程名称：");
            String courseCategory = extractValue(row2Text, "课程类别：");
            String courseNature = extractValue(row2Text, "课程性质：");
            String examMethod = extractValue(row2Text, "考核方式：");
            String teacherName = extractValue(row2Text, "任课教师：");
            
            // 解析第3行：教学信息
            Row row3 = sheet.getRow(2);
            String row3Text = getMergedCellValue(sheet, row3, 0);
            String term = extractValue(row3Text, "开课学期：");
            String creditStr = extractValue(row3Text, "学分：");
            String hoursStr = extractValue(row3Text, "学时：");
            String fullClassName = extractValue(row3Text, "开课班级：");

            Float credit = 0.0f;
            try { if (creditStr != null) credit = Float.parseFloat(creditStr); } catch (Exception ignored) {}
            Integer hours = 0;
            try { if (hoursStr != null) hours = Integer.parseInt(hoursStr); } catch (Exception ignored) {}
            
            // 校验课程是否存在，不存在则创建
            List<Course> courses = courseMapper.findBySearch(null, courseName != null ? courseName.trim() : "", 0, null, null);
            Course targetCourse;
            if (courses == null || courses.isEmpty()) {
                // 自动创建课程
                targetCourse = new Course();
                targetCourse.setCname(courseName != null ? courseName.trim() : "未知课程");
                targetCourse.setCcredit(credit.intValue()); // 暂时存为 int，如果需要 float 可修改实体
                targetCourse.setCategory(courseCategory);
                targetCourse.setNature(courseNature);
                targetCourse.setExamMethod(examMethod);
                targetCourse.setHours(hours);
                targetCourse.setDepartmentId(selectedDepartmentId);
                // 这里可能还需要设置 majorId，如果能从班级信息推断的话
                courseMapper.insertCourse(targetCourse);
            } else {
                targetCourse = courses.get(0);
                // 也可以选择更新课程信息
                boolean needUpdate = false;
                if (targetCourse.getCategory() == null) { targetCourse.setCategory(courseCategory); needUpdate = true; }
                if (targetCourse.getNature() == null) { targetCourse.setNature(courseNature); needUpdate = true; }
                if (targetCourse.getExamMethod() == null) { targetCourse.setExamMethod(examMethod); needUpdate = true; }
                if (targetCourse.getHours() == null || targetCourse.getHours() == 0) { targetCourse.setHours(hours); needUpdate = true; }
                if (needUpdate) courseMapper.updateById(targetCourse);
            }

            // 复杂解析：24级信息安全技术应用2班
            String extractedGradeLevel = "";
            String extractedMajorName = "";
            String extractedClassName = "";
            if (fullClassName != null && !fullClassName.isEmpty()) {
                java.util.regex.Matcher gradeMatcher = java.util.regex.Pattern.compile("(\\d{2}级)").matcher(fullClassName);
                if (gradeMatcher.find()) extractedGradeLevel = "20" + gradeMatcher.group(1);
                
                java.util.regex.Matcher classMatcher = java.util.regex.Pattern.compile("(\\d+班)$").matcher(fullClassName);
                if (classMatcher.find()) extractedClassName = classMatcher.group(1);
                
                extractedMajorName = fullClassName.replaceAll("\\d{2}级", "").replaceAll("\\d+班$", "").trim();
            }

            // 校验专业和班级是否存在
            List<Major> majorList = majorMapper.findBySearch(extractedMajorName, selectedDepartmentId);
            if (majorList == null || majorList.isEmpty()) return "上传失败：专业【" + extractedMajorName + "】在所属学院下未定义";
            Major targetMajor = majorList.get(0);

            List<com.auggie.student_server.entity.Class> classList = classMapper.findBySearch(extractedClassName, targetMajor.getId(), selectedDepartmentId);
            if (classList == null || classList.isEmpty()) return "上传失败：班级【" + extractedClassName + "】在对应专业下未定义";
            com.auggie.student_server.entity.Class targetClass = classList.get(0);

            // 获取教师 ID（仅精确匹配，否则报错）
            Integer targetTeacherId;
            if (teacherName == null || teacherName.trim().isEmpty()) {
                return "上传失败：成绩单中未提取到任课教师姓名";
            }
            List<Teacher> teachers = teacherMapper.findBySearch(null, teacherName.trim(), 0);
            if (teachers == null || teachers.isEmpty()) {
                return "上传失败：未找到教师【" + teacherName.trim() + "】，请确保教师姓名与系统内完全一致";
            }
            targetTeacherId = teachers.get(0).getId();

            // 3. 逐行校验学生数据
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                if (i >= 42 && i <= 45) continue; // 跳过中间重复的标题和表头行
                
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 实时探测页脚内容，只要匹配到“各档成绩百分比”等字样立刻终止
                String firstCellText = getStringValue(row.getCell(0));
                if (firstCellText != null && (firstCellText.startsWith("各档成绩百分比") || firstCellText.contains("签字"))) {
                    System.out.println("检测到页脚标识【" + firstCellText + "】，解析提前结束。");
                    break;
                }

                // 处理左右两列学生 (A-G 和 H-N)
                validateAndAddStudent(row, 0, targetCourse, targetTeacherId, term, dept, targetMajor, targetClass, extractedGradeLevel, validGradeList, errorReports, i + 1);
                validateAndAddStudent(row, 7, targetCourse, targetTeacherId, term, dept, targetMajor, targetClass, extractedGradeLevel, validGradeList, errorReports, i + 1);
            }

            // 4. 结果处理
            if (!errorReports.isEmpty()) {
                // 如果有任何一条错误，拒绝整份文件录入
                return "上传被拒绝，发现以下错误：\n" + String.join("\n", errorReports.subList(0, Math.min(errorReports.size(), 10))) + (errorReports.size() > 10 ? "\n...等更多错误" : "");
            }

            if (!validGradeList.isEmpty()) {
                studentCourseTeacherMapper.batchInsert(validGradeList);
            }

            // 记录导入
            ScoreImportRecord record = new ScoreImportRecord();
            record.setFileName(originalFilename);
            record.setFilePath(savedFilePath.toString());
            record.setTerm(term);
            record.setCourseId(targetCourse.getId());
            record.setDepartmentId(selectedDepartmentId);
            record.setTeacherId(targetTeacherId);
            record.setOperator(operator);
            record.setStatus("SUCCESS");
            record.setMessage("成功导入 " + validGradeList.size() + " 条记录");
            record.setCreatedAt(LocalDateTime.now());
            scoreImportRecordMapper.save(record);

            return "上传成功！已导入 " + validGradeList.size() + " 条成绩记录";
        } catch (Exception e) {
            e.printStackTrace();
            return "系统内部错误：" + e.getMessage();
        }
    }

    private void validateAndAddStudent(Row row, int startCol, Course course, Integer teacherId, String term, 
                                      com.auggie.student_server.entity.Department dept, Major major, 
                                      com.auggie.student_server.entity.Class targetClass, String gradeLevel,
                                      List<StudentCourseTeacher> validList, List<String> errorReports, int lineNum) {
        String studentNo = getStringValue(row.getCell(startCol));
        String studentName = getStringValue(row.getCell(startCol + 1));
        if (studentNo == null || studentNo.isEmpty()) return;
        
        // 长度校验，防止数据库报 Data too long 错误
        if (studentNo.length() > 50) {
            errorReports.add("第" + lineNum + "行：学号【" + studentNo.substring(0, 10) + "...】长度超过50位，请检查格式");
            return;
        }
        if (studentName != null && studentName.length() > 50) {
            errorReports.add("第" + lineNum + "行：姓名【" + studentName.substring(0, 10) + "...】长度超过50位，请检查格式");
            return;
        }

        // 1. 校验学生是否存在，不存在则自动创建
        Student student = studentMapper.findByStudentNo(studentNo);
        if (student == null) {
            // 自动创建学生
            System.out.println("检测到新学生，正在自动创建：" + studentNo + " " + studentName);
            student = new Student();
            student.setStudentNo(studentNo);
            student.setSname(studentName);
            student.setPassword("123456"); // 默认密码
            student.setDepartmentId(dept.getId());
            student.setMajorId(major.getId());
            student.setClassId(targetClass.getId());
            student.setGradeLevel(gradeLevel);
            studentMapper.save(student);
            // 重新查询以获取数据库分配的 ID
            student = studentMapper.findByStudentNo(studentNo);
            if (student == null) {
                errorReports.add("第" + lineNum + "行：自动创建学生【" + studentNo + "】失败");
                return;
            }
        } else if (!student.getSname().equals(studentName)) {
            errorReports.add("第" + lineNum + "行：学号【" + studentNo + "】与系统姓名【" + student.getSname() + "】不匹配");
            return;
        }

        // 2. 成绩唯一性校验 (同一学生+同一课程+同一学期)
        List<SCTInfo> existing = studentCourseTeacherMapper.findBySearch(student.getId(), null, 0, course.getId(), null, 0, null, null, 0, null, null, term, null, null, null, null, null, null, null);
        if (existing != null && !existing.isEmpty()) {
            errorReports.add("第" + lineNum + "行：冲突！学生【" + studentName + "】在【" + term + "】学期的【" + course.getCname() + "】成绩已存在");
            return;
        }

        // 3. 组装数据
        StudentCourseTeacher sct = new StudentCourseTeacher();
        sct.setStudentId(student.getId());
        sct.setCourseId(course.getId());
        sct.setTeacherId(teacherId); // 如果找不到教师，这里可能是 null，但至少会尝试设置
        sct.setTerm(term);
        
        // 读取成绩：平时成绩（第3列）、期中成绩（第4列）、期末成绩（第5列）、总成绩（第6列）
        Float usualScore = getFloatValue(row.getCell(startCol + 2));
        Float midScore = getFloatValue(row.getCell(startCol + 3)); // 期中成绩
        Float finalScore = getFloatValue(row.getCell(startCol + 4));
        Float grade = getFloatValue(row.getCell(startCol + 5)); // 总成绩
        String remark = getStringValue(row.getCell(startCol + 6));
        
        // 验证总成绩：如果总成绩为空，记录警告（但不阻止导入）
        Cell gradeCell = row.getCell(startCol + 5);
        if (grade == null) {
            String cellInfo = gradeCell != null ? "单元格类型=" + gradeCell.getCellType() + ", 值=" + getCellValueAsString(gradeCell) : "单元格为空";
            System.out.println("警告：第" + lineNum + "行，学号" + studentNo + " 的总成绩为空 (" + cellInfo + ")");
            // 如果总成绩为空但其他成绩有值，给出提示
            if ((usualScore != null && usualScore > 0) || (finalScore != null && finalScore > 0)) {
                System.out.println("  提示：该学生有平时成绩或期末成绩，但总成绩为空，请检查Excel文件");
            }
        } else {
            System.out.println("第" + lineNum + "行，学号" + studentNo + "：总成绩=" + grade);
        }
        
        sct.setUsualScore(usualScore);
        sct.setMidScore(midScore);
        sct.setFinalScore(finalScore);
        sct.setGrade(grade);  // 允许为 null，数据库字段允许 NULL
        sct.setRemark(remark);
        
        validList.add(sct);
    }

    public List<ScoreImportRecord> findAllRecords() {
        return scoreImportRecordMapper.findAll();
    }

    public boolean deleteRecord(Long id) {
        ScoreImportRecord record = scoreImportRecordMapper.findById(id);
        if (record != null) {
            File file = new File(record.getFilePath());
            if (file.exists()) file.delete();
            return scoreImportRecordMapper.deleteById(id);
        }
        return false;
    }

    public File getRecordFile(Long id) {
        ScoreImportRecord record = scoreImportRecordMapper.findById(id);
        if (record != null) {
            File file = new File(record.getFilePath());
            if (file.exists()) return file;
        }
        return null;
    }

    /**
     * 获取合并单元格的值
     */
    private String getMergedCellValue(Sheet sheet, Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell != null) {
            return getStringValue(cell);
        }
        // 检查是否是合并单元格
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(row.getRowNum(), cellIndex)) {
                Row firstRow = sheet.getRow(region.getFirstRow());
                if (firstRow != null) {
                    Cell firstCell = firstRow.getCell(region.getFirstColumn());
                    if (firstCell != null) {
                        return getStringValue(firstCell);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从文本中提取值
     */
    private String extractValue(String text, String prefix) {
        if (text == null) return null;
        // 支持全角和半角冒号
        String p1 = prefix.replace("：", ":");
        String p2 = prefix.replace(":", "：");
        int index = text.indexOf(p1);
        String usedPrefix = p1;
        if (index == -1) {
            index = text.indexOf(p2);
            usedPrefix = p2;
        }
        
        if (index == -1) return null;
        
        int start = index + usedPrefix.length();
        // 查找下一个明显的间隔
        int end = text.length();
        String[] separators = {"  ", " 课程", " 教师", " 学分", " 学时", " 开课"};
        for (String sep : separators) {
            int sepIndex = text.indexOf(sep, start);
            if (sepIndex != -1 && sepIndex < end) {
                end = sepIndex;
            }
        }
        
        return text.substring(start, end).trim();
    }

    /**
     * 处理学生行数据
     */
    private void processStudentRow(Sheet sheet, Row row, int startCol, int endCol, 
                                   Integer courseId, Integer teacherId, String term,
                                   List<StudentCourseTeacher> gradeList, StringBuilder errorMsg,
                                   int rowNum) {
        try {
            // 学生学号（第0列或第7列）
            Cell studentNoCell = row.getCell(startCol);
            if (studentNoCell == null) return;
            
            String studentNo = getStringValue(studentNoCell);
            if (studentNo == null || studentNo.isEmpty()) return;

            // 通过学号查找学生
            Student student = studentService.findByStudentNo(studentNo);
            if (student == null) {
                errorMsg.append("第").append(rowNum).append("行：学号 ").append(studentNo).append(" 不存在\n");
                return;
            }

            // 学生姓名（第1列或第8列）
            Cell nameCell = row.getCell(startCol + 1);
            String studentName = getStringValue(nameCell);

            // 平时成绩（第2列或第9列）
            Cell usualCell = row.getCell(startCol + 2);
            Float usualGrade = getFloatValue(usualCell);

            // 期中成绩（第3列或第10列）
            Cell midCell = row.getCell(startCol + 3);
            Float midGrade = getFloatValue(midCell);

            // 期末成绩（第4列或第11列）
            Cell finalCell = row.getCell(startCol + 4);
            Float finalGrade = getFloatValue(finalCell);

            // 总成绩（第5列或第12列）
            Cell totalCell = row.getCell(startCol + 5);
            Float totalGrade = getFloatValue(totalCell);

            if (courseId == null || teacherId == null) {
                errorMsg.append("第").append(rowNum).append("行：课程或教师信息缺失\n");
                return;
            }

            StudentCourseTeacher sct = new StudentCourseTeacher();
            sct.setStudentId(student.getId());
            sct.setCourseId(courseId);
            sct.setTeacherId(teacherId);
            sct.setUsualScore(usualGrade);
            sct.setMidScore(midGrade);
            sct.setFinalScore(finalGrade);
            sct.setGrade(totalGrade);
            sct.setTerm(term);

            gradeList.add(sct);
        } catch (Exception e) {
            errorMsg.append("第").append(rowNum).append("行：").append(e.getMessage()).append("\n");
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private Integer getIntValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private Float getFloatValue(Cell cell) {
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (float) cell.getNumericCellValue();
                case STRING:
                    String strValue = cell.getStringCellValue().trim();
                    if (strValue.isEmpty()) return null;
                    return Float.parseFloat(strValue);
                case FORMULA:
                    // 公式单元格：尝试获取计算后的数值
                    try {
                        return (float) cell.getNumericCellValue();
                    } catch (Exception e) {
                        // 如果公式结果是字符串，尝试解析
                        try {
                            String formulaResult = cell.getStringCellValue().trim();
                            if (formulaResult.isEmpty()) return null;
                            return Float.parseFloat(formulaResult);
                        } catch (Exception e2) {
                            return null;
                        }
                    }
                case BLANK:
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("解析单元格值失败: " + e.getMessage());
            return null;
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "null";
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case STRING:
                    return cell.getStringCellValue();
                case FORMULA:
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e) {
                        return cell.getStringCellValue();
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case BLANK:
                    return "(空白)";
                default:
                    return "(未知类型:" + cell.getCellType() + ")";
            }
        } catch (Exception e) {
            return "(错误:" + e.getMessage() + ")";
        }
    }

    /**
     * 导出成绩列表到 Excel
     */
    public Workbook exportReexaminationToExcel(List<com.auggie.student_server.entity.SCTInfo> list) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成绩列表");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "学生姓名", "学院", "专业", "班级", "课程名", "教师姓名", "平时成绩", "期中成绩", "期末成绩", "总成绩", "学期"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (com.auggie.student_server.entity.SCTInfo info : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(info.getStudentNo() != null ? info.getStudentNo() : "");
            row.createCell(1).setCellValue(info.getSname() != null ? info.getSname() : "");
            row.createCell(2).setCellValue(info.getDepartmentName() != null ? info.getDepartmentName() : "");
            row.createCell(3).setCellValue(info.getMajorName() != null ? info.getMajorName() : "");
            row.createCell(4).setCellValue(info.getClassName() != null ? info.getClassName() : "");
            row.createCell(5).setCellValue(info.getCname() != null ? info.getCname() : "");
            String teacherName = info.getTeacherRealName() != null ? info.getTeacherRealName() : info.getTname();
            row.createCell(6).setCellValue(teacherName != null ? teacherName : "");
            row.createCell(7).setCellValue(info.getUsualScore() != null ? info.getUsualScore() : 0);
            row.createCell(8).setCellValue(info.getMidScore() != null ? info.getMidScore() : 0);
            row.createCell(9).setCellValue(info.getFinalScore() != null ? info.getFinalScore() : 0);
            row.createCell(10).setCellValue(info.getGrade() != null ? info.getGrade() : 0);
            row.createCell(11).setCellValue(info.getTerm() != null ? info.getTerm() : "");
        }

        return workbook;
    }

    /**
     * 生成成绩单批量导入模板（按特定格式要求）
     * 第 1 行：合并A-N列，内容"邯郸应用技术职业学院课程成绩单"
     * 第 2 行：合并A-N列，内容"课程名称：高等数学  课程类别：必修  课程性质：理论课  考核方式：考试  任课教师：张老师"
     * 第 3 行：合并A-N列，内容"开课学期：2023-2024-1  学分：4  学时：64  开课班级：23级计算机1班"
     * 第 4 行：表头（A-G 为第一个学生，H-N 为第二个学生）
     * 第 5-42 行：学生数据
     * 第 43-45 行：重复第1-3行内容
     * 第 46 行：重复第4行内容（表头）
     * 第 47-73 行：学生数据
     */
    public Workbook generateScoreTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成绩单模板");

        // 设置通用样式
        CellStyle infoStyle = workbook.createCellStyle();
        infoStyle.setAlignment(HorizontalAlignment.LEFT);

        CellStyle titleStyle = workbook.createCellStyle();
        org.apache.poi.xssf.usermodel.XSSFFont titleFont = ((XSSFWorkbook) workbook).createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // 生成前4行
        createHeaderRows(workbook, sheet, 0);

        // 第 5-42 行：学生数据（示例数据）
        int rowIndex = 4; // 从第5行开始（索引4）
        for (int i = 0; i < 38; i++) {
            Row dataRow = sheet.createRow(rowIndex++);
            // 填充示例数据... (省略或简化)
        }

        // 第 43-45 行：重复第1-3行内容
        createHeaderRows(workbook, sheet, 42); // 43, 44, 45, 46

        // 第 47-73 行：学生数据
        rowIndex = 46; // 从第47行开始（索引46）
        for (int i = 0; i < 28; i++) {
            Row dataRow = sheet.createRow(rowIndex++);
        }

        return workbook;
    }

    private void createHeaderRows(Workbook workbook, Sheet sheet, int startRow) {
        CellStyle titleStyle = workbook.createCellStyle();
        org.apache.poi.xssf.usermodel.XSSFFont titleFont = ((XSSFWorkbook) workbook).createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle infoStyle = workbook.createCellStyle();
        infoStyle.setAlignment(HorizontalAlignment.LEFT);

        // 第 1/43 行：标题
        Row titleRow = sheet.createRow(startRow);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("邯郸应用技术职业学院课程成绩单");
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 0, 13));
        titleCell.setCellStyle(titleStyle);

        // 第 2/44 行：课程元信息
        Row row2 = sheet.createRow(startRow + 1);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue("课程名称：高等数学  课程类别：必修  课程性质：理论课  考核方式：考试  任课教师：张老师");
        sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 1, 0, 13));
        cell2.setCellStyle(infoStyle);

        // 第 3/45 行：教学信息
        Row row3 = sheet.getRow(startRow + 2);
        if (row3 == null) row3 = sheet.createRow(startRow + 2);
        Cell cell3 = row3.createCell(0);
        cell3.setCellValue("开课学期：2023-2024-1  学分：4  学时：64  开课班级：24级信息安全技术应用1班");
        sheet.addMergedRegion(new CellRangeAddress(startRow + 2, startRow + 2, 0, 13));
        cell3.setCellStyle(infoStyle);

        // 第 4/46 行：表头
        Row headerRow = sheet.getRow(startRow + 3);
        if (headerRow == null) headerRow = sheet.createRow(startRow + 3);
        String[] headers = {"学生学号", "学生姓名", "平时", "期中", "期末", "总成绩", "标志", "学生学号", "学生姓名", "平时", "期中", "期末", "总成绩", "标志"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }
}
