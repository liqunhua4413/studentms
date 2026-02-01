package com.auggie.student_server.service;

import com.auggie.student_server.entity.GradeLevel;
import com.auggie.student_server.mapper.GradeLevelMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
public class GradeLevelService {
    @Autowired
    private GradeLevelMapper gradeLevelMapper;

    public List<GradeLevel> findAll() {
        return gradeLevelMapper.findAll();
    }

    public GradeLevel findById(Integer id) {
        return gradeLevelMapper.findById(id);
    }

    public GradeLevel findByName(String name) {
        return gradeLevelMapper.findByName(name);
    }

    public boolean save(GradeLevel gradeLevel) {
        if (gradeLevel == null) return false;
        if (gradeLevel.getName() == null || gradeLevel.getName().trim().isEmpty()) return false;
        if (gradeLevelMapper.findByName(gradeLevel.getName().trim()) != null) return false;
        if (gradeLevel.getSortOrder() == null) gradeLevel.setSortOrder(0);
        return gradeLevelMapper.insert(gradeLevel) > 0;
    }

    public boolean updateById(GradeLevel gradeLevel) {
        if (gradeLevel == null || gradeLevel.getId() == null) return false;
        return gradeLevelMapper.updateById(gradeLevel) > 0;
    }

    public boolean deleteById(Integer id) {
        if (id == null) return false;
        return gradeLevelMapper.deleteById(id) > 0;
    }

    /**
     * 批量导入年级（Excel）
     * 模板列：年级名称、排序（可选，默认0）
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
                    if (name == null || name.trim().isEmpty()) continue;
                    Integer sortOrder = getIntValue(row.getCell(1));
                    if (sortOrder == null) sortOrder = 0;
                    if (gradeLevelMapper.findByName(name.trim()) != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：年级【").append(name).append("】已存在\n");
                        continue;
                    }
                    GradeLevel gl = new GradeLevel();
                    gl.setName(name.trim());
                    gl.setSortOrder(sortOrder);
                    if (gradeLevelMapper.insert(gl) > 0) successCount++;
                    else { failCount++; errorMsg.append("第").append(i + 1).append("行：插入失败\n"); }
                } catch (Exception e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
                }
            }
            workbook.close();
            return String.format("年级导入完成！成功：%d 条，失败：%d 条\n%s", successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "年级导入失败：" + e.getMessage();
        }
    }

    public Workbook generateImportTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("年级导入模板");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("年级名称");
        headerRow.createCell(1).setCellValue("排序");
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("2024级");
        exampleRow.createCell(1).setCellValue(1);
        return workbook;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        org.apache.poi.ss.usermodel.DataFormatter df = new org.apache.poi.ss.usermodel.DataFormatter();
        return df.formatCellValue(cell).trim();
    }

    private Integer getIntValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC)
                return (int) cell.getNumericCellValue();
            String s = getStringValue(cell);
            return (s == null || s.isEmpty()) ? null : Integer.parseInt(s);
        } catch (Exception e) { return null; }
    }
}
