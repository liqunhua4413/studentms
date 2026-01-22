package com.auggie.student_server.service;

import com.auggie.student_server.entity.StudentCourseTeacher;
import com.auggie.student_server.mapper.StudentCourseTeacherMapper;
import com.auggie.student_server.mapper.StudentMapper;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import com.auggie.student_server.entity.Student;
import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.Teacher;
import org.apache.poi.ss.usermodel.*;
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

    /**
     * 解析 Excel 文件并批量插入成绩
     * Excel 格式：学号、学生姓名、课程名、教师姓名、平时成绩、期末成绩、总成绩、学期、班级、专业、系
     */
    public String uploadExcel(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<StudentCourseTeacher> gradeList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();

            // 从第二行开始读取（第一行是表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    StudentCourseTeacher sct = new StudentCourseTeacher();

                    // 读取学号
                    Cell cell0 = row.getCell(0);
                    if (cell0 == null) continue;
                    Integer sid = getIntValue(cell0);
                    if (sid == null) continue;

                    // 读取学生姓名（用于验证）
                    Cell cell1 = row.getCell(1);
                    String sname = getStringValue(cell1);

                    // 读取课程名
                    Cell cell2 = row.getCell(2);
                    String cname = getStringValue(cell2);

                    // 读取教师姓名
                    Cell cell3 = row.getCell(3);
                    String tname = getStringValue(cell3);

                    // 读取平时成绩
                    Cell cell4 = row.getCell(4);
                    Float usualGrade = getFloatValue(cell4);

                    // 读取期末成绩
                    Cell cell5 = row.getCell(5);
                    Float finalGrade = getFloatValue(cell5);

                    // 读取总成绩
                    Cell cell6 = row.getCell(6);
                    Float totalGrade = getFloatValue(cell6);

                    // 读取学期
                    Cell cell7 = row.getCell(7);
                    String term = getStringValue(cell7);

                    // 读取班级ID
                    Cell cell8 = row.getCell(8);
                    Integer classId = getIntValue(cell8);

                    // 读取专业ID
                    Cell cell9 = row.getCell(9);
                    Integer majorId = getIntValue(cell9);

                    // 读取系ID
                    Cell cell10 = row.getCell(10);
                    Integer departmentId = getIntValue(cell10);

                    // 验证学生是否存在
                    Student student = studentMapper.findById(sid);
                    if (student == null) {
                        errorMsg.append("第").append(i + 1).append("行：学号 ").append(sid).append(" 不存在\n");
                        failCount++;
                        continue;
                    }

                    // 查找课程ID
                    List<Course> courses = courseMapper.findBySearch(null, cname, 1, null, null);
                    if (courses == null || courses.isEmpty()) {
                        errorMsg.append("第").append(i + 1).append("行：课程 ").append(cname).append(" 不存在\n");
                        failCount++;
                        continue;
                    }
                    Integer cid = courses.get(0).getCid();

                    // 查找教师ID
                    List<Teacher> teachers = teacherMapper.findBySearch(null, tname, 1);
                    if (teachers == null || teachers.isEmpty()) {
                        errorMsg.append("第").append(i + 1).append("行：教师 ").append(tname).append(" 不存在\n");
                        failCount++;
                        continue;
                    }
                    Integer tid = teachers.get(0).getTid();

                    sct.setSid(sid);
                    sct.setCid(cid);
                    sct.setTid(tid);
                    sct.setUsualGrade(usualGrade);
                    sct.setFinalGrade(finalGrade);
                    sct.setTotalGrade(totalGrade);
                    sct.setTerm(term);
                    sct.setClassId(classId);
                    sct.setMajorId(majorId);
                    sct.setDepartmentId(departmentId);

                    gradeList.add(sct);
                    successCount++;
                } catch (Exception e) {
                    errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
                    failCount++;
                }
            }

            // 批量插入
            if (!gradeList.isEmpty()) {
                studentCourseTeacherMapper.batchInsert(gradeList);
            }

            workbook.close();
            inputStream.close();

            return String.format("上传完成！成功：%d 条，失败：%d 条\n%s", successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败：" + e.getMessage();
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
            row.createCell(0).setCellValue(info.getSid() != null ? info.getSid() : 0);
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
}
