package com.auggie.student_server.service;

import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.Department;
import com.auggie.student_server.entity.Major;
import com.auggie.student_server.entity.Term;
import com.auggie.student_server.entity.TrainingPlan;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.DepartmentMapper;
import com.auggie.student_server.mapper.MajorMapper;
import com.auggie.student_server.mapper.TermMapper;
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
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private TermMapper termMapper;

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
     * 模板列：专业名称、学院名称、课程名称、方案版本、课程类型(REQUIRED/LIMITED/ELECTIVE)、建议年级名称、建议学期名称、最低学分、最高学分、最大容量、状态、备注
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
                    String majorName = getStringValue(row.getCell(0));
                    String departmentName = getStringValue(row.getCell(1));
                    String courseName = getStringValue(row.getCell(2));
                    String planVersion = getStringValue(row.getCell(3));
                    
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
                    if (courseName == null || courseName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：课程名称不能为空\n");
                        continue;
                    }
                    if (planVersion == null || planVersion.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：方案版本不能为空\n");
                        continue;
                    }

                    majorName = majorName.trim();
                    departmentName = departmentName.trim();
                    courseName = courseName.trim();
                    planVersion = planVersion.trim();

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

                    // 按课程名称+学院ID查询课程ID
                    Course course = courseMapper.findByNameAndDepartmentId(courseName, department.getId());
                    if (course == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：课程【").append(courseName).append("】在学院【").append(departmentName).append("】中不存在\n");
                        continue;
                    }
                    
                    // 检查是否已存在
                    if (trainingPlanMapper.findByMajorCourseVersion(major.getId(), course.getId(), planVersion) != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：专业【").append(majorName).append("】-课程【").append(courseName).append("】-版本【").append(planVersion).append("】组合已存在\n");
                        continue;
                    }

                    TrainingPlan tp = new TrainingPlan();
                    tp.setMajorId(major.getId());
                    tp.setCourseId(course.getId());
                    tp.setPlanVersion(planVersion);
                    
                    String courseType = getStringValue(row.getCell(4));
                    if (courseType != null && !courseType.trim().isEmpty()) {
                        courseType = courseType.trim().toUpperCase();
                        if (!courseType.equals("REQUIRED") && !courseType.equals("LIMITED") && !courseType.equals("ELECTIVE")) {
                            failCount++;
                            errorMsg.append("第").append(i + 1).append("行：课程类型必须是 REQUIRED/LIMITED/ELECTIVE\n");
                            continue;
                        }
                        tp.setCourseType(courseType);
                    } else {
                        tp.setCourseType("REQUIRED"); // 默认必修
                    }
                    
                    // 建议年级名称
                    String suggestedGradeName = getStringValue(row.getCell(5));
                    if (suggestedGradeName != null && !suggestedGradeName.trim().isEmpty()) {
                        tp.setSuggestedGrade(Integer.parseInt(suggestedGradeName.trim()));
                    }
                    
                    // 建议学期名称
                    String suggestedTermName = getStringValue(row.getCell(6));
                    if (suggestedTermName != null && !suggestedTermName.trim().isEmpty()) {
                        Term term = termMapper.findByName(suggestedTermName.trim());
                        if (term != null) {
                            tp.setSuggestedTermId(term.getId());
                        }
                    }
                    
                    tp.setMinCredit(getDecimalValue(row.getCell(7)));
                    tp.setMaxCredit(getDecimalValue(row.getCell(8)));
                    Integer maxCapacity = getIntValue(row.getCell(9));
                    tp.setMaxCapacity(maxCapacity != null ? maxCapacity : 0);
                    Integer status = getIntValue(row.getCell(10));
                    tp.setStatus(status != null ? status : 1);
                    tp.setRemark(getStringValue(row.getCell(11)));

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
        headerRow.createCell(0).setCellValue("专业名称");
        headerRow.createCell(1).setCellValue("学院名称");
        headerRow.createCell(2).setCellValue("课程名称");
        headerRow.createCell(3).setCellValue("方案版本");
        headerRow.createCell(4).setCellValue("课程类型");
        headerRow.createCell(5).setCellValue("建议年级");
        headerRow.createCell(6).setCellValue("建议学期名称");
        headerRow.createCell(7).setCellValue("最低学分");
        headerRow.createCell(8).setCellValue("最高学分");
        headerRow.createCell(9).setCellValue("最大容量");
        headerRow.createCell(10).setCellValue("状态");
        headerRow.createCell(11).setCellValue("备注");
        
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("会计学");
        exampleRow.createCell(1).setCellValue("经济管理学院");
        exampleRow.createCell(2).setCellValue("基础会计");
        exampleRow.createCell(3).setCellValue("2024");
        exampleRow.createCell(4).setCellValue("REQUIRED");
        exampleRow.createCell(5).setCellValue("1");
        exampleRow.createCell(6).setCellValue("2024-2025-1");
        exampleRow.createCell(7).setCellValue(3.0);
        exampleRow.createCell(8).setCellValue(3.0);
        exampleRow.createCell(9).setCellValue(60);
        exampleRow.createCell(10).setCellValue(1);
        exampleRow.createCell(11).setCellValue("必修课");
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
