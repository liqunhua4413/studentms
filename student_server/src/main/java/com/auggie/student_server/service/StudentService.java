package com.auggie.student_server.service;

import com.auggie.student_server.entity.GradeLevel;
import com.auggie.student_server.entity.Student;
import com.auggie.student_server.mapper.GradeLevelMapper;
import com.auggie.student_server.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2022/2/9 08:27
 * @Description: StudentService
 * @Version 1.0.0
 */

@Service
public class StudentService {
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private GradeLevelMapper gradeLevelMapper;

    public List<Student> findByPage(Integer num, Integer size) {
        // num：第几页，size：一页多大
        // num：从零开始
        List<Student> studentList = studentMapper.findAll();
        ArrayList<Student> list = new ArrayList<Student>();

        int start = size * num;
        int end = size * (num + 1);
        int sz = studentList.size();

        for (int i = start; i < end && i < sz; i++) {
            list.add(studentList.get(i));
        }

        return list;
    }

    public List<Student> findBySearch(Integer sid, String sname, Integer fuzzy) {
        Student student = new Student();
        student.setId(sid);
        student.setSname(sname);
        fuzzy = (fuzzy == null) ? 0 : fuzzy;

        System.out.println();

        return studentMapper.findBySearch(student, fuzzy);
    }

    public Integer getLength() {
        return studentMapper.findAll().size();
    }

    public Student findById(Integer sid) {
        return studentMapper.findById(sid);
    }

    public Student findByStudentNo(String studentNo) {
        return studentMapper.findByStudentNo(studentNo);
    }

    public boolean updateById(Student student) {
        return studentMapper.updateById(student);
    }

    public boolean save(Student student) {
        return studentMapper.save(student);
    }

    public boolean deleteById(Integer sid) {
        return studentMapper.deleteById(sid);
    }

    /**
     * 批量导入学生基础数据（Excel）
     * 模板列顺序：
     * 学号、学生姓名、初始密码、班级ID、年级、专业ID、学院ID
     */
    public String importFromExcel(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();

            // 从第二行开始读取（第一行是表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    // 学号（第一列，必填）
                    Cell cell0 = row.getCell(0);
                    String studentNo = getStringValue(cell0);
                    if (studentNo == null || studentNo.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学号不能为空\n");
                        continue;
                    }
                    studentNo = studentNo.trim();

                    // 学生姓名
                    Cell cell1 = row.getCell(1);
                    String sname = getStringValue(cell1);
                    if (sname == null || sname.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学生姓名不能为空\n");
                        continue;
                    }
                    sname = sname.trim();

                    // 初始密码
                    Cell cell2 = row.getCell(2);
                    String password = getStringValue(cell2);
                    if (password == null || password.trim().isEmpty()) {
                        password = "123456";
                    } else {
                        password = password.trim();
                    }

                    // 班级ID
                    Cell cell3 = row.getCell(3);
                    Integer classId = getIntValue(cell3);

                    // 年级（须在 grade_level 表中存在）
                    Cell cell4 = row.getCell(4);
                    String gradeLevelName = getStringValue(cell4);
                    Integer gradeLevelId = null;
                    if (gradeLevelName != null && !gradeLevelName.trim().isEmpty()) {
                        GradeLevel gl = gradeLevelMapper.findByName(gradeLevelName.trim());
                        if (gl != null) gradeLevelId = gl.getId();
                        else {
                            failCount++;
                            errorMsg.append("第").append(i + 1).append("行：年级【").append(gradeLevelName).append("】在 grade_level 表中不存在\n");
                            continue;
                        }
                    }

                    // 专业ID
                    Cell cell5 = row.getCell(5);
                    Integer majorId = getIntValue(cell5);

                    // 学院ID
                    Cell cell6 = row.getCell(6);
                    Integer departmentId = getIntValue(cell6);

                    // 检查学号是否已存在
                    Student existing = studentMapper.findByStudentNo(studentNo);
                    if (existing != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学号【").append(studentNo).append("】已存在\n");
                        continue;
                    }

                    Student student = new Student();
                    student.setStudentNo(studentNo);
                    student.setSname(sname);
                    student.setPassword(password);
                    student.setClassId(classId);
                    student.setGradeLevelId(gradeLevelId);
                    student.setMajorId(majorId);
                    student.setDepartmentId(departmentId);

                    boolean ok = studentMapper.save(student);
                    if (ok) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：数据库插入失败\n");
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
                }
            }

            workbook.close();
            inputStream.close();

            return String.format("学生导入完成！成功：%d 条，失败：%d 条\n%s",
                    successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "学生导入失败：" + e.getMessage();
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

    /**
     * 生成学生批量导入模板
     * 列顺序：学号、学生姓名、初始密码、班级ID、年级、专业ID、学院ID
     */
    public Workbook generateImportTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生导入模板");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "学生姓名", "初始密码", "班级ID", "年级", "专业ID", "学院ID"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 添加示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("S24001");
        exampleRow.createCell(1).setCellValue("张三");
        exampleRow.createCell(2).setCellValue("123456");
        exampleRow.createCell(3).setCellValue(1);
        exampleRow.createCell(4).setCellValue("2024级");
        exampleRow.createCell(5).setCellValue(1);
        exampleRow.createCell(6).setCellValue(1);

        return workbook;
    }

    public List<Map<String, Object>> findDistinctColleges() {
        return studentMapper.findDistinctColleges();
    }

    public List<Map<String, Object>> findDistinctMajorsByCollegeId(Integer collegeId) {
        if (collegeId == null) return new ArrayList<>();
        return studentMapper.findDistinctMajorsByCollegeId(collegeId);
    }

    public List<Map<String, Object>> findDistinctClassesByMajorId(Integer majorId) {
        if (majorId == null) return new ArrayList<>();
        return studentMapper.findDistinctClassesByMajorId(majorId);
    }
}
