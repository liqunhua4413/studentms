package com.auggie.student_server.service;

import com.auggie.student_server.entity.Class;
import com.auggie.student_server.mapper.ClassMapper;
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

    public List<Class> findAll() {
        return classMapper.findAll();
    }

    public Class findById(Integer id) {
        return classMapper.findById(id);
    }

    public List<Class> findBySearch(String name, Integer majorId, Integer departmentId) {
        return classMapper.findBySearch(name, majorId, departmentId);
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
     * 模板列顺序：班级名称、专业ID、学院ID
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
                    String name = getStringValue(row.getCell(0));
                    Integer majorId = getIntValue(row.getCell(1));
                    Integer departmentId = getIntValue(row.getCell(2));

                    if (name == null || name.isEmpty() || majorId == null || departmentId == null) {
                        continue;
                    }

                    Class clazz = new Class();
                    clazz.setName(name);
                    clazz.setMajorId(majorId);
                    clazz.setDepartmentId(departmentId);

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
        String[] headers = {"班级名称", "专业ID", "学院ID"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 添加示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("计算机1班");
        exampleRow.createCell(1).setCellValue(1);
        exampleRow.createCell(2).setCellValue(1);

        return workbook;
    }
}
