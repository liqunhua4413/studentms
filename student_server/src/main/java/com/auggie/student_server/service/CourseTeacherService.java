package com.auggie.student_server.service;

import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.CourseTeacher;
import com.auggie.student_server.entity.CourseTeacherInfo;
import com.auggie.student_server.entity.Department;
import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.CourseTeacherMapper;
import com.auggie.student_server.mapper.DepartmentMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 16:50
 * @Description: CourseTeacherService
 * @Version 1.0.0
 */

@Service
public class CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private TermService termService;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    public boolean insertCourseTeacher(Integer cid, Integer tid, Integer termId) {
        return courseTeacherMapper.insertCourseTeacher(cid, tid, termId);
    }

    public List<Course> findMyCourse(Integer tid, Integer termId) {
        return courseTeacherMapper.findMyCourse(tid, termId);
    }

    public List<CourseTeacherInfo> findCourseTeacherInfo(Map<String, String> map) {
        Integer tid = null, cid = null;
        Integer tFuzzy = null, cFuzzy = null;
        String tname = null, cname = null;
        if (map.containsKey("tid")) {
            try {
                tid = Integer.parseInt(map.get("tid"));
            }
            catch (Exception e) {
            }
        }
        if (map.containsKey("cid")) {
            try {
                cid = Integer.parseInt(map.get("cid"));
            }
            catch (Exception e) {
            }
        }
        if (map.containsKey("tname")) {
            tname = map.get("tname");
        }
        if (map.containsKey("cname")) {
            cname = map.get("cname");
        }
        if (map.containsKey("tFuzzy")) {
            tFuzzy = (map.get("tFuzzy").equals("true")) ? 1 : 0;
        }
        if (map.containsKey("cFuzzy")) {
            cFuzzy = (map.get("cFuzzy").equals("true")) ? 1 : 0;
        }
        System.out.println("ct 模糊查询" + map);
        System.out.println(courseTeacherMapper.findCourseTeacherInfo(tid, tname, tFuzzy, cid, cname, cFuzzy));
        return courseTeacherMapper.findCourseTeacherInfo(tid, tname, tFuzzy, cid, cname, cFuzzy);
    }

    public List<CourseTeacher> findBySearch(Integer cid, Integer tid, Integer termId) {
        return courseTeacherMapper.findBySearch(cid, tid, termId);
    }

    public List<CourseTeacher> findBySearch(Map<String, String> map) {
        Integer cid = null;
        Integer tid = null;
        Integer termId = null;

        if (map.containsKey("termId")) {
            try { termId = Integer.parseInt(map.get("termId")); } catch (Exception ignored) {}
        } else if (map.containsKey("term")) {
            String termStr = map.get("term");
            if (termStr != null && !termStr.trim().isEmpty()) {
                com.auggie.student_server.entity.Term t = termService.findByName(termStr.trim());
                if (t != null) termId = t.getId();
            }
        }

        if (map.containsKey("tid")) {
            try {
                tid = Integer.parseInt(map.get("tid"));
            }
            catch (Exception e) {
            }
        }

        if (map.containsKey("cid")) {
            try {
                cid = Integer.parseInt(map.get("cid"));
            }
            catch (Exception e) {
            }
        }
        System.out.println("开课表查询：" + map);
        return courseTeacherMapper.findBySearch(cid, tid, termId);
    }

    public boolean deleteById(CourseTeacher courseTeacher) {
        return courseTeacherMapper.deleteById(courseTeacher);
    }

    public CourseTeacher findById(Integer id) {
        return courseTeacherMapper.findById(id);
    }

    public boolean save(CourseTeacher courseTeacher) {
        if (courseTeacher == null || courseTeacher.getCourseId() == null || courseTeacher.getTeacherId() == null || courseTeacher.getTermId() == null) {
            return false;
        }
        return courseTeacherMapper.save(courseTeacher) > 0;
    }

    public boolean updateById(CourseTeacher courseTeacher) {
        if (courseTeacher == null || courseTeacher.getId() == null) return false;
        return courseTeacherMapper.updateById(courseTeacher) > 0;
    }

    /**
     * 批量导入开课表（Excel）
     * 模板列顺序：课程名称、学院名称、教师工号、开课学期名称
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
                    String departmentName = getStringValue(row.getCell(1));
                    String teacherNo = getStringValue(row.getCell(2));
                    String termName = getStringValue(row.getCell(3));

                    if (courseName == null || courseName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：课程名称不能为空\n");
                        continue;
                    }
                    if (departmentName == null || departmentName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学院名称不能为空\n");
                        continue;
                    }
                    if (teacherNo == null || teacherNo.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：教师工号不能为空\n");
                        continue;
                    }
                    if (termName == null || termName.trim().isEmpty()) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：开课学期名称不能为空\n");
                        continue;
                    }

                    courseName = courseName.trim();
                    departmentName = departmentName.trim();
                    teacherNo = teacherNo.trim();
                    termName = termName.trim();

                    // 按学院名称查询学院ID
                    Department department = departmentMapper.findByName(departmentName);
                    if (department == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学院名称【").append(departmentName).append("】不存在\n");
                        continue;
                    }

                    // 按课程名称+学院ID查询课程ID
                    Course course = courseMapper.findByNameAndDepartmentId(courseName, department.getId());
                    if (course == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：课程【").append(courseName).append("】在学院【").append(departmentName).append("】中不存在\n");
                        continue;
                    }

                    // 按教师工号查询教师ID
                    Teacher teacher = teacherMapper.findByTeacherNo(teacherNo);
                    if (teacher == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：教师工号【").append(teacherNo).append("】不存在\n");
                        continue;
                    }

                    // 按学期名称查询学期ID
                    com.auggie.student_server.entity.Term termEntity = termService.findByName(termName);
                    if (termEntity == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：学期名称【").append(termName).append("】不存在\n");
                        continue;
                    }

                    Integer cid = course.getId();
                    Integer tid = teacher.getId();
                    Integer termId = termEntity.getId();

                    // 检查是否已存在
                    if (courseTeacherMapper.findBySearch(cid, tid, termId).size() > 0) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：开课记录【课程:").append(courseName).append(",教师:").append(teacherNo).append(",学期:").append(termName).append("】已存在\n");
                        continue;
                    }

                    boolean ok = courseTeacherMapper.insertCourseTeacher(cid, tid, termId);
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
            return String.format("开课表导入完成！成功：%d 条，失败：%d 条\n%s",
                    successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "开课表导入失败：" + e.getMessage();
        }
    }

    /**
     * 生成开课表批量导入模板
     */
    public Workbook generateImportTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("开课表导入模板");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"课程名称", "学院名称", "教师工号", "开课学期名称"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 添加示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("基础会计");
        exampleRow.createCell(1).setCellValue("经济管理学院");
        exampleRow.createCell(2).setCellValue("T1001");
        exampleRow.createCell(3).setCellValue("2024-2025-1");

        return workbook;
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
