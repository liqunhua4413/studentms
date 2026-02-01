package com.auggie.student_server.service;

import com.auggie.student_server.entity.TrainingPlan;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.MajorMapper;
import com.auggie.student_server.mapper.TrainingPlanMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TrainingPlanService {

    @Autowired
    private TrainingPlanMapper trainingPlanMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private CourseMapper courseMapper;

    public List<TrainingPlan> findAll() {
        return trainingPlanMapper.findAll();
    }

    public TrainingPlan findById(Long id) {
        return id == null ? null : trainingPlanMapper.findById(id);
    }

    public List<TrainingPlan> findBySearch(Map<String, Object> params) {
        Integer majorId = params.get("majorId") != null ? Integer.parseInt(params.get("majorId").toString()) : null;
        Integer courseId = params.get("courseId") != null ? Integer.parseInt(params.get("courseId").toString()) : null;
        String planVersion = params.get("planVersion") != null ? params.get("planVersion").toString() : null;
        String courseType = params.get("courseType") != null ? params.get("courseType").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        return trainingPlanMapper.findBySearch(majorId, courseId, planVersion, courseType, status);
    }

    public boolean save(TrainingPlan tp) {
        if (tp == null || tp.getMajorId() == null || tp.getCourseId() == null || 
            tp.getPlanVersion() == null || tp.getPlanVersion().trim().isEmpty()) {
            return false;
        }
        // 检查是否已存在
        if (trainingPlanMapper.findByMajorCourseVersion(tp.getMajorId(), tp.getCourseId(), tp.getPlanVersion().trim()) != null) {
            return false;
        }
        if (tp.getStatus() == null) tp.setStatus(1);
        return trainingPlanMapper.insert(tp) > 0;
    }

    public boolean updateById(TrainingPlan tp) {
        if (tp == null || tp.getId() == null) return false;
        return trainingPlanMapper.updateById(tp) > 0;
    }

    public boolean deleteById(Long id) {
        if (id == null) return false;
        return trainingPlanMapper.deleteById(id) > 0;
    }

    /**
     * 批量导入培养方案（Excel）
     * 模板列：专业ID、课程ID、方案版本、课程类型(REQUIRED/LIMITED/ELECTIVE)、建议年级、建议学期ID、最低学分、最高学分、最大容量、状态、备注
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
                    Integer majorId = getIntValue(row.getCell(0));
                    Integer courseId = getIntValue(row.getCell(1));
                    String planVersion = getStringValue(row.getCell(2));
                    
                    if (majorId == null || courseId == null || planVersion == null || planVersion.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：专业ID、课程ID、方案版本不能为空\n");
                        continue;
                    }
                    
                    // 检查专业和课程是否存在
                    if (majorMapper.findById(majorId) == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：专业ID【").append(majorId).append("】不存在\n");
                        continue;
                    }
                    if (courseMapper.findByCourseId(courseId) == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：课程ID【").append(courseId).append("】不存在\n");
                        continue;
                    }
                    
                    // 检查是否已存在
                    if (trainingPlanMapper.findByMajorCourseVersion(majorId, courseId, planVersion.trim()) != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：该专业-课程-版本组合已存在\n");
                        continue;
                    }

                    TrainingPlan tp = new TrainingPlan();
                    tp.setMajorId(majorId);
                    tp.setCourseId(courseId);
                    tp.setPlanVersion(planVersion.trim());
                    
                    String courseType = getStringValue(row.getCell(3));
                    if (courseType != null && !courseType.isEmpty()) {
                        courseType = courseType.toUpperCase();
                        if (!courseType.equals("REQUIRED") && !courseType.equals("LIMITED") && !courseType.equals("ELECTIVE")) {
                            failCount++;
                            errorMsg.append("第").append(i + 1).append("行：课程类型必须是 REQUIRED/LIMITED/ELECTIVE\n");
                            continue;
                        }
                        tp.setCourseType(courseType);
                    } else {
                        tp.setCourseType("REQUIRED"); // 默认必修
                    }
                    
                    tp.setSuggestedGrade(getIntValue(row.getCell(4)));
                    tp.setSuggestedTermId(getIntValue(row.getCell(5)));
                    tp.setMinCredit(getDecimalValue(row.getCell(6)));
                    tp.setMaxCredit(getDecimalValue(row.getCell(7)));
                    Integer maxCapacity = getIntValue(row.getCell(8));
                    tp.setMaxCapacity(maxCapacity != null ? maxCapacity : 0);
                    Integer status = getIntValue(row.getCell(9));
                    tp.setStatus(status != null ? status : 1);
                    tp.setRemark(getStringValue(row.getCell(10)));

                    if (trainingPlanMapper.insert(tp) > 0) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：插入失败\n");
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
                }
            }
            workbook.close();
            return String.format("培养方案导入完成！成功：%d 条，失败：%d 条\n%s", successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "培养方案导入失败：" + e.getMessage();
        }
    }

    public Workbook generateImportTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("培养方案导入模板");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("专业ID");
        headerRow.createCell(1).setCellValue("课程ID");
        headerRow.createCell(2).setCellValue("方案版本");
        headerRow.createCell(3).setCellValue("课程类型");
        headerRow.createCell(4).setCellValue("建议年级");
        headerRow.createCell(5).setCellValue("建议学期ID");
        headerRow.createCell(6).setCellValue("最低学分");
        headerRow.createCell(7).setCellValue("最高学分");
        headerRow.createCell(8).setCellValue("最大容量");
        headerRow.createCell(9).setCellValue("状态");
        headerRow.createCell(10).setCellValue("备注");
        
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue(1);
        exampleRow.createCell(1).setCellValue(1);
        exampleRow.createCell(2).setCellValue("2025版");
        exampleRow.createCell(3).setCellValue("REQUIRED");
        exampleRow.createCell(4).setCellValue(1);
        exampleRow.createCell(5).setCellValue(1);
        exampleRow.createCell(6).setCellValue(3.0);
        exampleRow.createCell(7).setCellValue(4.0);
        exampleRow.createCell(8).setCellValue(0);
        exampleRow.createCell(9).setCellValue(1);
        exampleRow.createCell(10).setCellValue("大一第一学期必修课");
        return workbook;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        DataFormatter df = new DataFormatter();
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

    private BigDecimal getDecimalValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC)
                return BigDecimal.valueOf(cell.getNumericCellValue());
            String s = getStringValue(cell);
            return (s == null || s.isEmpty()) ? null : new BigDecimal(s);
        } catch (Exception e) { return null; }
    }
}
