package com.auggie.student_server.service;

import com.auggie.student_server.entity.Major;
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
 * @Description: MajorService
 * @Version 1.0.0
 */

@Service
public class MajorService {
    @Autowired
    private MajorMapper majorMapper;

    public List<Major> findAll() {
        return majorMapper.findAll();
    }

    public Major findById(Integer id) {
        return majorMapper.findById(id);
    }

    public List<Major> findBySearch(String name, Integer departmentId) {
        return majorMapper.findBySearch(name, departmentId);
    }

    public List<Major> findByDepartmentId(Integer departmentId) {
        return majorMapper.findByDepartmentId(departmentId);
    }

    public boolean updateById(Major major) {
        return majorMapper.updateById(major);
    }

    public boolean save(Major major) {
        return majorMapper.save(major);
    }

    public boolean deleteById(Integer id) {
        return majorMapper.deleteById(id);
    }

    /**
     * 批量导入专业（Excel）
     * 模板列顺序：专业名称、所属学院ID
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
                    Integer departmentId = getIntValue(row.getCell(1));
                    if (name == null || name.isEmpty() || departmentId == null) {
                        continue;
                    }
                    Major major = new Major();
                    major.setName(name);
                    major.setDepartmentId(departmentId);
                    boolean ok = majorMapper.save(major);
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
            return String.format("专业导入完成！成功：%d 条，失败：%d 条\n%s",
                    successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "专业导入失败：" + e.getMessage();
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
