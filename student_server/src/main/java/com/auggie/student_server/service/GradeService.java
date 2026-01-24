package com.auggie.student_server.service;

import com.auggie.student_server.entity.StudentCourseTeacher;
import com.auggie.student_server.entity.ScoreImportRecord;
import com.auggie.student_server.mapper.StudentCourseTeacherMapper;
import com.auggie.student_server.mapper.StudentMapper;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import com.auggie.student_server.mapper.ScoreImportRecordMapper;
import com.auggie.student_server.entity.Student;
import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.Teacher;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 解析 Excel 文件并批量插入成绩
     * 新格式：
     * 第1行：合并单元格，标题
     * 第2行：合并单元格，课程元信息（需要解析）
     * 第3行：合并单元格，教学信息（需要解析）
     * 第4行：表头
     * 第5-42行：学生数据
     * 第43-45行：重复表头（跳过）
     * 第46-73行：学生数据
     */
    public String uploadExcel(MultipartFile file, String operator) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<StudentCourseTeacher> gradeList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();

            // 解析第2行：课程元信息（合并单元格）
            String courseName = null;
            String courseCategory = null;
            String courseNature = null;
            String examMethod = null;
            String teacherName = null;
            
            Row row2 = sheet.getRow(1);
            if (row2 != null) {
                String row2Text = getMergedCellValue(sheet, row2, 0);
                if (row2Text != null) {
                    // 解析：课程名称：高等数学  课程类别：必修  课程性质：理论课  考核方式：考试  任课教师：张老师
                    courseName = extractValue(row2Text, "课程名称：");
                    courseCategory = extractValue(row2Text, "课程类别：");
                    courseNature = extractValue(row2Text, "课程性质：");
                    examMethod = extractValue(row2Text, "考核方式：");
                    teacherName = extractValue(row2Text, "任课教师：");
                }
            }

            // 解析第3行：教学信息（合并单元格）
            String term = null;
            Integer credit = null;
            Integer hours = null;
            String className = null;
            String gradeLevel = null;
            
            Row row3 = sheet.getRow(2);
            if (row3 != null) {
                String row3Text = getMergedCellValue(sheet, row3, 0);
                if (row3Text != null) {
                    // 解析：开课学期：2023-2024-1  学分：4  学时：64  开课班级：23级计算机1班  年级：2023级
                    term = extractValue(row3Text, "开课学期：");
                    String creditStr = extractValue(row3Text, "学分：");
                    if (creditStr != null) {
                        try {
                            credit = Integer.parseInt(creditStr.trim());
                        } catch (Exception e) {}
                    }
                    String hoursStr = extractValue(row3Text, "学时：");
                    if (hoursStr != null) {
                        try {
                            hours = Integer.parseInt(hoursStr.trim());
                        } catch (Exception e) {}
                    }
                    className = extractValue(row3Text, "开课班级：");
                    gradeLevel = extractValue(row3Text, "年级：");
                }
            }

            // 查找课程和教师
            Integer courseIdForRecord = null;
            Integer teacherIdForRecord = null;
            
            if (courseName != null && !courseName.isEmpty()) {
                List<Course> courses = courseMapper.findBySearch(null, courseName.trim(), 1, null, null);
                if (courses != null && !courses.isEmpty()) {
                    courseIdForRecord = courses.get(0).getId();
                }
            }
            
            if (teacherName != null && !teacherName.isEmpty()) {
                List<Teacher> teachers = teacherMapper.findBySearch(null, teacherName.trim(), 1);
                if (teachers != null && !teachers.isEmpty()) {
                    teacherIdForRecord = teachers.get(0).getId();
                }
            }

            // 从第5行开始读取学生数据（索引4），跳过第43-45行（索引42-44）
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                // 跳过第43-45行（索引42-44）
                if (i >= 42 && i <= 44) {
                    continue;
                }
                
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 处理第一个学生（A-G列，索引0-6）
                int beforeSize = gradeList.size();
                processStudentRow(sheet, row, 0, 6, courseIdForRecord, teacherIdForRecord, term, 
                    gradeList, errorMsg, i + 1);
                if (gradeList.size() > beforeSize) {
                    successCount++;
                } else {
                    failCount++;
                }
                
                // 处理第二个学生（H-N列，索引7-13）
                beforeSize = gradeList.size();
                processStudentRow(sheet, row, 7, 13, courseIdForRecord, teacherIdForRecord, term, 
                    gradeList, errorMsg, i + 1);
                if (gradeList.size() > beforeSize) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            // 批量插入
            if (!gradeList.isEmpty()) {
                studentCourseTeacherMapper.batchInsert(gradeList);
            }

            workbook.close();
            inputStream.close();

            // 记录导入结果
            ScoreImportRecord record = new ScoreImportRecord();
            record.setFileName(file.getOriginalFilename());
            record.setTerm(term);
            record.setCourseId(courseIdForRecord);
            record.setTeacherId(teacherIdForRecord);
            record.setOperator(operator);
            record.setStatus(failCount == 0 ? "SUCCESS" : (successCount == 0 ? "FAILED" : "PARTIAL"));
            record.setMessage(String.format("成功：%d 条，失败：%d 条\n%s", successCount, failCount, errorMsg.toString()));
            record.setCreatedAt(java.time.LocalDateTime.now());
            scoreImportRecordMapper.save(record);

            return String.format("上传完成！成功：%d 条，失败：%d 条\n%s", successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败：" + e.getMessage();
        }
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
        int index = text.indexOf(prefix);
        if (index == -1) return null;
        int start = index + prefix.length();
        int end = text.indexOf("  ", start); // 两个空格分隔
        if (end == -1) {
            end = text.length();
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
            sct.setUsualGrade(usualGrade);
            sct.setFinalGrade(finalGrade);
            sct.setTotalGrade(totalGrade);
            sct.setTerm(term);
            sct.setClassId(student.getClassId());
            sct.setMajorId(student.getMajorId());
            sct.setDepartmentId(student.getDepartmentId());

            gradeList.add(sct);
        } catch (Exception e) {
            errorMsg.append("第").append(rowNum).append("行：").append(e.getMessage()).append("\n");
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
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
        switch (cell.getCellType()) {
            case NUMERIC:
                return (float) cell.getNumericCellValue();
            case STRING:
                try {
                    return Float.parseFloat(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * 导出补考名单到 Excel
     */
    public Workbook exportReexaminationToExcel(List<com.auggie.student_server.entity.SCTInfo> list) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("补考名单");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "学生姓名", "课程名", "教师姓名", "平时成绩", "期末成绩", "总成绩", "学期", "班级ID", "专业ID", "系ID"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (com.auggie.student_server.entity.SCTInfo info : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(info.getStudentId() != null ? info.getStudentId() : 0);
            row.createCell(1).setCellValue(info.getSname() != null ? info.getSname() : "");
            row.createCell(2).setCellValue(info.getCname() != null ? info.getCname() : "");
            row.createCell(3).setCellValue(info.getTname() != null ? info.getTname() : "");
            row.createCell(4).setCellValue(info.getUsualGrade() != null ? info.getUsualGrade() : 0);
            row.createCell(5).setCellValue(info.getFinalGrade() != null ? info.getFinalGrade() : 0);
            row.createCell(6).setCellValue(info.getTotalGrade() != null ? info.getTotalGrade() : 0);
            row.createCell(7).setCellValue(info.getTerm() != null ? info.getTerm() : "");
            row.createCell(8).setCellValue(info.getClassId() != null ? info.getClassId() : 0);
            row.createCell(9).setCellValue(info.getMajorId() != null ? info.getMajorId() : 0);
            row.createCell(10).setCellValue(info.getDepartmentId() != null ? info.getDepartmentId() : 0);
        }

        return workbook;
    }

    /**
     * 生成成绩单批量导入模板（按特定格式要求）
     * 第 1 行：合并A-N列，内容"邯郸应用技术职业学院课程成绩单"
     * 第 2 行：合并A-N列，内容"课程名称：高等数学  课程类别：必修  课程性质：理论课  考核方式：考试  任课教师：张老师"
     * 第 3 行：合并A-N列，内容"开课学期：2023-2024-1  学分：4  学时：64  开课班级：23级计算机1班  年级：2023级"
     * 第 4 行：表头（A-G 为第一个学生，H-N 为第二个学生）
     * 第 5-42 行：学生数据
     * 第 43-45 行：重复第1-3行内容
     * 第 46-73 行：学生数据
     */
    public Workbook generateScoreTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成绩单模板");

        // 第 1 行：合并A-N列，标题
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("邯郸应用技术职业学院课程成绩单");
        // 合并A1-N1
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));
        // 设置样式
        CellStyle titleStyle = workbook.createCellStyle();
        org.apache.poi.xssf.usermodel.XSSFFont titleFont = ((XSSFWorkbook) workbook).createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);

        // 第 2 行：合并A-N列，课程元信息
        Row courseInfoRow = sheet.createRow(1);
        Cell courseInfoCell = courseInfoRow.createCell(0);
        courseInfoCell.setCellValue("课程名称：高等数学  课程类别：必修  课程性质：理论课  考核方式：考试  任课教师：张老师");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 13));
        CellStyle infoStyle = workbook.createCellStyle();
        infoStyle.setAlignment(HorizontalAlignment.LEFT);
        courseInfoCell.setCellStyle(infoStyle);

        // 第 3 行：合并A-N列，教学信息
        Row teachingInfoRow = sheet.createRow(2);
        Cell teachingInfoCell = teachingInfoRow.createCell(0);
        teachingInfoCell.setCellValue("开课学期：2023-2024-1  学分：4  学时：64  开课班级：23级计算机1班  年级：2023级");
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 13));
        teachingInfoCell.setCellStyle(infoStyle);

        // 第 4 行：表头（A-G 为第一个学生，H-N 为第二个学生）
        Row headerRow = sheet.createRow(3);
        // 第一个学生表头（A-G）
        headerRow.createCell(0).setCellValue("学生学号");
        headerRow.createCell(1).setCellValue("学生姓名");
        headerRow.createCell(2).setCellValue("平时");
        headerRow.createCell(3).setCellValue("期中");
        headerRow.createCell(4).setCellValue("期末");
        headerRow.createCell(5).setCellValue("总成绩");
        headerRow.createCell(6).setCellValue("标志");
        // 第二个学生表头（H-N）
        headerRow.createCell(7).setCellValue("学生学号");
        headerRow.createCell(8).setCellValue("学生姓名");
        headerRow.createCell(9).setCellValue("平时");
        headerRow.createCell(10).setCellValue("期中");
        headerRow.createCell(11).setCellValue("期末");
        headerRow.createCell(12).setCellValue("总成绩");
        headerRow.createCell(13).setCellValue("标志");

        // 第 5-42 行：学生数据（示例数据）
        int rowIndex = 4; // 从第5行开始（索引4）
        for (int i = 0; i < 38; i++) { // 5-42行共38行
            Row dataRow = sheet.createRow(rowIndex);
            // 第一个学生数据（A-G列）
            dataRow.createCell(0).setCellValue("S23000" + String.format("%02d", i + 1));
            dataRow.createCell(1).setCellValue("学生" + (i + 1));
            dataRow.createCell(2).setCellValue(80 + i % 20); // 平时
            dataRow.createCell(3).setCellValue(75 + i % 25); // 期中
            dataRow.createCell(4).setCellValue(70 + i % 30); // 期末
            dataRow.createCell(5).setCellValue(75 + i % 25); // 总成绩
            dataRow.createCell(6).setCellValue(""); // 标志
            // 第二个学生数据（H-N列）
            if (i < 19) { // 只填充前19个学生的第二个位置
                dataRow.createCell(7).setCellValue("S23000" + String.format("%02d", i + 20));
                dataRow.createCell(8).setCellValue("学生" + (i + 20));
                dataRow.createCell(9).setCellValue(80 + (i + 19) % 20);
                dataRow.createCell(10).setCellValue(75 + (i + 19) % 25);
                dataRow.createCell(11).setCellValue(70 + (i + 19) % 30);
                dataRow.createCell(12).setCellValue(75 + (i + 19) % 25);
                dataRow.createCell(13).setCellValue("");
            }
            rowIndex++;
        }

        // 第 43-45 行：重复第1-3行内容
        Row repeatTitleRow = sheet.createRow(42); // 第43行（索引42）
        Cell repeatTitleCell = repeatTitleRow.createCell(0);
        repeatTitleCell.setCellValue("邯郸应用技术职业学院课程成绩单");
        sheet.addMergedRegion(new CellRangeAddress(42, 42, 0, 13));
        repeatTitleCell.setCellStyle(titleStyle);

        Row repeatCourseInfoRow = sheet.createRow(43); // 第44行（索引43）
        Cell repeatCourseInfoCell = repeatCourseInfoRow.createCell(0);
        repeatCourseInfoCell.setCellValue("课程名称：高等数学  课程类别：必修  课程性质：理论课  考核方式：考试  任课教师：张老师");
        sheet.addMergedRegion(new CellRangeAddress(43, 43, 0, 13));
        repeatCourseInfoCell.setCellStyle(infoStyle);

        Row repeatTeachingInfoRow = sheet.createRow(44); // 第45行（索引44）
        Cell repeatTeachingInfoCell = repeatTeachingInfoRow.createCell(0);
        repeatTeachingInfoCell.setCellValue("开课学期：2023-2024-1  学分：4  学时：64  开课班级：23级计算机1班  年级：2023级");
        sheet.addMergedRegion(new CellRangeAddress(44, 44, 0, 13));
        repeatTeachingInfoCell.setCellStyle(infoStyle);

        // 第 46-73 行：学生数据（示例数据）
        rowIndex = 45; // 从第46行开始（索引45）
        for (int i = 0; i < 28; i++) { // 46-73行共28行
            Row dataRow = sheet.createRow(rowIndex);
            // 第一个学生数据（A-G列）
            dataRow.createCell(0).setCellValue("S23000" + String.format("%02d", i + 39));
            dataRow.createCell(1).setCellValue("学生" + (i + 39));
            dataRow.createCell(2).setCellValue(80 + (i + 38) % 20);
            dataRow.createCell(3).setCellValue(75 + (i + 38) % 25);
            dataRow.createCell(4).setCellValue(70 + (i + 38) % 30);
            dataRow.createCell(5).setCellValue(75 + (i + 38) % 25);
            dataRow.createCell(6).setCellValue("");
            // 第二个学生数据（H-N列）
            if (i < 14) { // 只填充前14个学生的第二个位置
                dataRow.createCell(7).setCellValue("S23000" + String.format("%02d", i + 58));
                dataRow.createCell(8).setCellValue("学生" + (i + 58));
                dataRow.createCell(9).setCellValue(80 + (i + 57) % 20);
                dataRow.createCell(10).setCellValue(75 + (i + 57) % 25);
                dataRow.createCell(11).setCellValue(70 + (i + 57) % 30);
                dataRow.createCell(12).setCellValue(75 + (i + 57) % 25);
                dataRow.createCell(13).setCellValue("");
            }
            rowIndex++;
        }

        return workbook;
    }
}
