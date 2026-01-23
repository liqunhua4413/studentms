package com.auggie.student_server.service;

import com.auggie.student_server.entity.Department;
import com.auggie.student_server.mapper.DepartmentMapper;
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
 * @Description: DepartmentService
 * @Version 1.0.0
 */

@Service
public class DepartmentService {
    @Autowired
    private DepartmentMapper departmentMapper;

    public List<Department> findAll() {
        return departmentMapper.findAll();
    }

    public Department findById(Integer id) {
        return departmentMapper.findById(id);
    }

    public List<Department> findBySearch(String name) {
        return departmentMapper.findBySearch(name);
    }

    public boolean updateById(Department department) {
        return departmentMapper.updateById(department);
    }

    public boolean save(Department department) {
        return departmentMapper.save(department);
    }

    public boolean deleteById(Integer id) {
        return departmentMapper.deleteById(id);
    }

    /**
     * 批量导入学院（Excel）
     * 模板列顺序：学院名称
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
                    if (name == null || name.isEmpty()) {
                        continue;
                    }
                    Department department = new Department();
                    department.setName(name);
                    boolean ok = departmentMapper.save(department);
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
            return String.format("学院导入完成！成功：%d 条，失败：%d 条\n%s",
                    successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "学院导入失败：" + e.getMessage();
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
}
