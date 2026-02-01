package com.auggie.student_server.service;

import com.auggie.student_server.entity.Class;
import com.auggie.student_server.entity.Department;
import com.auggie.student_server.entity.GradeLevel;
import com.auggie.student_server.entity.Major;
import com.auggie.student_server.mapper.ClassMapper;
import com.auggie.student_server.mapper.DepartmentMapper;
import com.auggie.student_server.mapper.GradeLevelMapper;
import com.auggie.student_server.mapper.MajorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.io.InputStream;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: ClassService
 * @Version 1.0.0
 */

@Service
public class ClassService {
    @Autowired
    private ClassMapper classMapper;
    @Autowired
    private GradeLevelMapper gradeLevelMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private MajorMapper majorMapper;

    public List<Class> findAll() {
        return classMapper.findAll();
    }

    public Class findById(Integer id) {
        return classMapper.findById(id);
    }

    public List<Class> findBySearch(String name, Integer gradeLevelId, Integer majorId, Integer departmentId) {
        return classMapper.findBySearch(name, gradeLevelId, majorId, departmentId);
    }

    public List<Class> findByMajorId(Integer majorId) {
        return classMapper.findByMajorId(majorId);
    }

    public boolean updateById(Class clazz) {
        return classMapper.updateById(clazz);
    }

    public boolean save(Class clazz) {
        return classMapper.save(clazz);
    }

    public boolean deleteById(Integer id) {
        return classMapper.deleteById(id);
    }

    /**
     * 批量导入班级（Excel）
     * 模板列顺序：班级名称、年级名称、专业名称、学院名称
     */
    public String importFromExcel(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String className = getStringValue(row.getCell(0));
                    String gradeLevelName = getStringValue(row.getCell(1));
                    String majorName = getStringValue(row.getCell(2));
                    String departmentName = getStringValue(row.getCell(3));

                    if (className == null || className.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：班级名称不能为空\n");
                        continue;
                    }
                    if (gradeLevelName == null || gradeLevelName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：年级名称不能为空\n");
                        continue;
                    }
                    if (majorName == null || majorName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：专业名称不能为空\n");
                        continue;
                    }
                    if (departmentName == null || departmentName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学院名称不能为空\n");
                        continue;
                    }

                    className = className.trim();
                    gradeLevelName = gradeLevelName.trim();
                    majorName = majorName.trim();
                    departmentName = departmentName.trim();

                    // 按学院名称查询学院ID
                    Department department = departmentMapper.findByName(departmentName);
                    if (department == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学院名称【").append(departmentName).append("】不存在\n");
                        continue;
                    }

                    // 按专业名称+学院ID查询专业ID
                    Major major = majorMapper.findByNameAndDepartmentId(majorName, department.getId());
                    if (major == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：专业【").append(majorName).append("】在学院【").append(departmentName).append("】中不存在\n");
                        continue;
                    }

                    // 按年级名称查询年级ID
                    GradeLevel gradeLevel = gradeLevelMapper.findByName(gradeLevelName);
                    if (gradeLevel == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：年级名称【").append(gradeLevelName).append("】不存在\n");
                        continue;
                    }

                    // 检查班级是否已存在
                    Class existing = classMapper.findByNameAndMajorIdAndGradeLevelId(className, major.getId(), gradeLevel.getId());
                    if (existing != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：班级【").append(className).append("】在专业【").append(majorName).append("】年级【").append(gradeLevelName).append("】中已存在\n");
                        continue;
                    }

                    Class clazz = new Class();
                    clazz.setName(className);
                    clazz.setGradeLevelId(gradeLevel.getId());
                    clazz.setMajorId(major.getId());
                    clazz.setDepartmentId(department.getId());

                    boolean ok = classMapper.save(clazz);
                    if (ok) successCount++;
                    else {
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
            return String.format("班级导入完成！成功：%d 条，失败：%d 条\n%s",
                    successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "班级导入失败：" + e.getMessage();
        }
    }

    /** 解析年级：数字为ID，字符串为年级名称，查表得ID */
    private Integer parseGradeLevelId(Cell cell) {
        if (cell == null) return null;
        Integer idVal = getIntValue(cell);
        if (idVal != null && idVal > 0) return idVal;
        String s = getStringValue(cell);
        if (s == null || s.trim().isEmpty()) return null;
        GradeLevel gl = gradeLevelMapper.findByName(s.trim());
        return gl != null ? gl.getId() : null;
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
     * 生成班级批量导入模板
     */
    public Workbook generateImportTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("班级导入模板");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"班级名称", "年级名称", "专业名称", "学院名称"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 添加示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("1班");
        exampleRow.createCell(1).setCellValue("2024级");
        exampleRow.createCell(2).setCellValue("会计学");
        exampleRow.createCell(3).setCellValue("经济管理学院");

        return workbook;
    }
}
