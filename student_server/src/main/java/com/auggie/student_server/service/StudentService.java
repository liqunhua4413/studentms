package com.auggie.student_server.service;

import com.auggie.student_server.entity.Student;
import com.auggie.student_server.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
        student.setSid(sid);
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
     * 学生姓名、初始密码、班级ID、年级、专业ID、学院ID
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
                    // 学生姓名
                    Cell cell0 = row.getCell(0);
                    String sname = getStringValue(cell0);
                    if (sname == null || sname.isEmpty()) {
                        continue;
                    }

                    // 初始密码
                    Cell cell1 = row.getCell(1);
                    String password = getStringValue(cell1);
                    if (password == null || password.isEmpty()) {
                        password = "123456";
                    }

                    // 班级ID
                    Cell cell2 = row.getCell(2);
                    Integer classId = getIntValue(cell2);

                    // 年级
                    Cell cell3 = row.getCell(3);
                    String gradeLevel = getStringValue(cell3);

                    // 专业ID
                    Cell cell4 = row.getCell(4);
                    Integer majorId = getIntValue(cell4);

                    // 学院ID
                    Cell cell5 = row.getCell(5);
                    Integer departmentId = getIntValue(cell5);

                    // 生成学号（格式：S + 年级后两位 + 序号，例如 S230001）
                    String studentNo = "S" + (gradeLevel != null && gradeLevel.length() >= 2 
                        ? gradeLevel.substring(gradeLevel.length() - 2) : "23") 
                        + String.format("%04d", i);

                    Student student = new Student();
                    student.setStudentNo(studentNo);
                    student.setSname(sname);
                    student.setPassword(password);
                    student.setClassId(classId);
                    student.setGradeLevel(gradeLevel);
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
}
