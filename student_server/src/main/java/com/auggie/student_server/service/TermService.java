package com.auggie.student_server.service;

import com.auggie.student_server.entity.Term;
import com.auggie.student_server.mapper.TermMapper;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 学期服务。供成绩查询、开课管理、成绩上传等模块使用。
 */
@Service
public class TermService {

    @Autowired
    private TermMapper termMapper;

    public List<Term> findAll() {
        return termMapper.findAll();
    }

    public Term findById(Integer id) {
        return id == null ? null : termMapper.findById(id);
    }

    /**
     * 按学期名称查找，用于 Excel 上传时解析到的学期字符串解析为 term_id。
     */
    public Term findByName(String name) {
        return (name == null || name.trim().isEmpty()) ? null : termMapper.findByName(name.trim());
    }

    public boolean save(Term term) {
        if (term == null || term.getName() == null || term.getName().trim().isEmpty()) return false;
        if (termMapper.findByName(term.getName().trim()) != null) return false;
        if (term.getStatus() == null) term.setStatus(1);
        return termMapper.insert(term) > 0;
    }

    public boolean updateById(Term term) {
        if (term == null || term.getId() == null) return false;
        return termMapper.updateById(term) > 0;
    }

    public boolean deleteById(Integer id) {
        if (id == null) return false;
        return termMapper.deleteById(id) > 0;
    }

    /**
     * 批量导入学期（Excel）
     * 模板列：学期名称、开始日期、结束日期、状态(1启用/0停用)、备注
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
                    if (termMapper.findByName(name.trim()) != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学期【").append(name).append("】已存在\n");
                        continue;
                    }
                    Term term = new Term();
                    term.setName(name.trim());
                    LocalDate startDate = parseDateCell(row.getCell(1));
                    term.setStartDate(startDate);
                    term.setEndDate(parseDateCell(row.getCell(2)));
                    Integer status = getIntValue(row.getCell(3));
                    term.setStatus(status != null ? status : 1);
                    term.setRemark(getStringValue(row.getCell(4)));

                    if (termMapper.insert(term) > 0) successCount++;
                    else { failCount++; errorMsg.append("第").append(i + 1).append("行：插入失败\n"); }
                } catch (Exception e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
                }
            }
            workbook.close();
            return String.format("学期导入完成！成功：%d 条，失败：%d 条\n%s", successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "学期导入失败：" + e.getMessage();
        }
    }

    public Workbook generateImportTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学期导入模板");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("学期名称");
        headerRow.createCell(1).setCellValue("开始日期");
        headerRow.createCell(2).setCellValue("结束日期");
        headerRow.createCell(3).setCellValue("状态");
        headerRow.createCell(4).setCellValue("备注");
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("2025-2026-1");
        exampleRow.createCell(1).setCellValue("2025-09-01");
        exampleRow.createCell(2).setCellValue("2026-01-15");
        exampleRow.createCell(3).setCellValue(1);
        exampleRow.createCell(4).setCellValue("2025-2026 学年第一学期");
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

    /**
     * 解析日期单元格，支持 Excel 日期类型和多种字符串格式
     */
    private LocalDate parseDateCell(Cell cell) {
        if (cell == null) return null;
        
        // 如果是数值类型（Excel 日期存储为数值）
        if (cell.getCellType() == CellType.NUMERIC) {
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                // Excel 日期单元格，直接获取日期值
                java.util.Date date = cell.getDateCellValue();
                if (date != null) {
                    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                }
            }
        }
        
        // 否则作为字符串解析
        String dateStr = getStringValue(cell);
        if (dateStr == null || dateStr.isEmpty()) return null;
        
        // 尝试多种日期格式
        String[] patterns = {
            "yyyy-MM-dd",   // 2025-09-01
            "yyyy/MM/dd",   // 2025/09/01
            "yyyy-M-d",     // 2025-9-1
            "yyyy/M/d",     // 2025/9/1
            "yyyy/M/dd",    // 2025/9/01
            "yyyy/MM/d",    // 2025/09/1
            "yyyy-M-dd",    // 2025-9-01
            "yyyy-MM-d"     // 2025-09-1
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
