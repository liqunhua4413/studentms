package com.auggie.student_server.service;

import com.auggie.student_server.entity.Class;
import com.auggie.student_server.entity.Department;
import com.auggie.student_server.entity.GradeLevel;
import com.auggie.student_server.entity.Major;
import com.auggie.student_server.entity.Student;
import com.auggie.student_server.mapper.ClassMapper;
import com.auggie.student_server.mapper.DepartmentMapper;
import com.auggie.student_server.mapper.GradeLevelMapper;
import com.auggie.student_server.mapper.MajorMapper;
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
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private ClassMapper classMapper;

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
     * 学生学号、学生姓名、初始密码、班级名称、年级名称、专业名称、学院名称
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
                    // 学生学号（第一列，必填）
                    String studentNo = getStringValue(row.getCell(0));
                    if (studentNo == null || studentNo.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学生学号不能为空\n");
                        continue;
                    }
                    studentNo = studentNo.trim();

                    // 学生姓名
                    String sname = getStringValue(row.getCell(1));
                    if (sname == null || sname.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学生姓名不能为空\n");
                        continue;
                    }
                    sname = sname.trim();

                    // 初始密码
                    String password = getStringValue(row.getCell(2));
                    password = (password == null || password.trim().isEmpty()) ? "123456" : password.trim();

                    // 班级名称
                    String className = getStringValue(row.getCell(3));
                    // 年级名称
                    String gradeLevelName = getStringValue(row.getCell(4));
                    // 专业名称
                    String majorName = getStringValue(row.getCell(5));
                    // 学院名称
                    String departmentName = getStringValue(row.getCell(6));

                    if (departmentName == null || departmentName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学院名称不能为空\n");
                        continue;
                    }
                    departmentName = departmentName.trim();

                    // 按学院名称查询学院ID
                    Department department = departmentMapper.findByName(departmentName);
                    if (department == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学院名称【").append(departmentName).append("】不存在\n");
                        continue;
                    }

                    Integer majorId = null;
                    if (majorName != null && !majorName.trim().isEmpty()) {
                        Major major = majorMapper.findByNameAndDepartmentId(majorName.trim(), department.getId());
                        if (major == null) {
                            failCount++;
                            errorMsg.append("第").append(i + 1).append("行：专业【").append(majorName.trim()).append("】在学院【").append(departmentName).append("】中不存在\n");
                            continue;
                        }
                        majorId = major.getId();
                    }

                    Integer gradeLevelId = null;
                    if (gradeLevelName != null && !gradeLevelName.trim().isEmpty()) {
                        GradeLevel gl = gradeLevelMapper.findByName(gradeLevelName.trim());
                        if (gl == null) {
                            failCount++;
                            errorMsg.append("第").append(i + 1).append("行：年级名称【").append(gradeLevelName.trim()).append("】不存在\n");
                            continue;
                        }
                        gradeLevelId = gl.getId();
                    }

                    Integer classId = null;
                    if (className != null && !className.trim().isEmpty() && majorId != null && gradeLevelId != null) {
                        Class clazz = classMapper.findByNameAndMajorIdAndGradeLevelId(className.trim(), majorId, gradeLevelId);
                        if (clazz == null) {
                            failCount++;
                            errorMsg.append("第").append(i + 1).append("行：班级【").append(className.trim()).append("】在专业【").append(majorName).append("】年级【").append(gradeLevelName).append("】中不存在\n");
                            continue;
                        }
                        classId = clazz.getId();
                    }

                    // 检查学号是否已存在
                    Student existing = studentMapper.findByStudentNo(studentNo);
                    if (existing != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学生学号【").append(studentNo).append("】已存在\n");
                        continue;
                    }

                    Student student = new Student();
                    student.setStudentNo(studentNo);
                    student.setSname(sname);
                    student.setPassword(password);
                    student.setClassId(classId);
                    student.setGradeLevelId(gradeLevelId);
                    student.setMajorId(majorId);
                    student.setDepartmentId(department.getId());

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
        String[] headers = {"学生学号", "学生姓名", "初始密码", "班级名称", "年级名称", "专业名称", "学院名称"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 添加示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("S240101");
        exampleRow.createCell(1).setCellValue("赵小明");
        exampleRow.createCell(2).setCellValue("123456");
        exampleRow.createCell(3).setCellValue("1班");
        exampleRow.createCell(4).setCellValue("2024级");
        exampleRow.createCell(5).setCellValue("会计学");
        exampleRow.createCell(6).setCellValue("经济管理学院");

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
