package com.auggie.student_server.service;

import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.Department;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

/**
 * @Auther: auggie
 * @Date: 2022/2/9 13:46
 * @Description: CourseService
 * @Version 1.0.0
 */

@Service
public class CourseService {
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private DepartmentMapper departmentMapper;

    public List<Course> findBySearch(Map<String, String> map) {
        Integer cid = null;
        Integer lowBound = null;
        Integer highBound = null;
        Integer fuzzy = 0;
        String cname = null;

        if (map.containsKey("cid")) {
            try {
                cid = Integer.parseInt(map.get("cid"));
            }
            catch (Exception e) {
            }
        }
        if (map.containsKey("lowBound")) {
            try {
                lowBound = Integer.parseInt(map.get("lowBound"));
            }
            catch (Exception e) {
            }
        }
        if (map.containsKey("highBound")) {
            try {
                highBound = Integer.valueOf(map.get("highBound"));
            }
            catch (Exception e) {
            }
        }
        if (map.containsKey("cname")) {
            cname = map.get("cname");
        }
        if (map.containsKey("fuzzy")) {
            fuzzy = (map.get("fuzzy").equals("true")) ? 1 : 0;
        }
        System.out.println("查询课程 map：" + map);
        System.out.println(cid + " " + cname + " " + fuzzy + " " + lowBound + " " + highBound);
        return courseMapper.findBySearch(cid, cname, fuzzy, lowBound, highBound);
    }

    public List<Course> findBySearch(Integer cid) {
        return courseMapper.findBySearch(cid, null, 0, null, null);
    }

    public List<Course> findById(Integer cid) {
        HashMap<String, String> map = new HashMap<>();
        if (cid != null)
            map.put("cid", String.valueOf(cid));
        return findBySearch(map);
    }

    public boolean updateById(Course course) {
        return courseMapper.updateById(course);
    }

    public boolean insertCourse(Course course) {
        return courseMapper.insertCourse(course);
    }

    public boolean deleteById(Integer cid) {
        return courseMapper.deleteById(cid);
    }

    /**
     * 批量导入课程（Excel）
     * 模板列顺序：课程名称、学分、课程类别、考核方式、学时、所属学院名称
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
                    String courseName = getStringValue(row.getCell(0));
                    Integer credit = getIntValue(row.getCell(1));
                    String category = getStringValue(row.getCell(2));
                    String examMethod = getStringValue(row.getCell(3));
                    Integer hours = getIntValue(row.getCell(4));
                    String departmentName = getStringValue(row.getCell(5));

                    if (courseName == null || courseName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：课程名称不能为空\n");
                        continue;
                    }
                    if (departmentName == null || departmentName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：所属学院名称不能为空\n");
                        continue;
                    }

                    courseName = courseName.trim();
                    departmentName = departmentName.trim();

                    // 按学院名称查询学院ID
                    Department department = departmentMapper.findByName(departmentName);
                    if (department == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学院名称【").append(departmentName).append("】不存在\n");
                        continue;
                    }

                    // 检查课程是否已存在
                    Course existing = courseMapper.findByNameAndDepartmentId(courseName, department.getId());
                    if (existing != null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：课程【").append(courseName).append("】在学院【").append(departmentName).append("】中已存在\n");
                        continue;
                    }

                    Course course = new Course();
                    course.setCname(courseName);
                    course.setCcredit(credit);
                    course.setCategory(category);
                    course.setExamMethod(examMethod);
                    course.setHours(hours);
                    course.setDepartmentId(department.getId());

                    boolean ok = courseMapper.insertCourse(course);
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
            return String.format("课程导入完成！成功：%d 条，失败：%d 条\n%s",
                    successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "课程导入失败：" + e.getMessage();
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
     * 生成课程批量导入模板
     */
    public Workbook generateImportTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("课程导入模板");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"课程名称", "学分", "课程类别", "考核方式", "学时", "所属学院名称"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 添加示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("基础会计");
        exampleRow.createCell(1).setCellValue(3);
        exampleRow.createCell(2).setCellValue("专业课");
        exampleRow.createCell(3).setCellValue("考试");
        exampleRow.createCell(4).setCellValue(48);
        exampleRow.createCell(5).setCellValue("经济管理学院");

        return workbook;
    }
}
