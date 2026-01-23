package com.auggie.student_server.service;

import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.mapper.StudentMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

/**
 * @Auther: auggie
 * @Date: 2022/2/9 10:55
 * @Description: TeacherService
 * @Version 1.0.0
 */

@Service
public class TeacherService {
    @Autowired
    private TeacherMapper teacherMapper;

    public List<Teacher> findBySearch(Map<String, String> map) {
        Integer tid = null;
        String tname = null;
        Integer fuzzy = null;
        if (map.containsKey("tid")) {
            try {
                tid = Integer.parseInt(map.get("tid"));
            }
            catch (Exception e) {
            }
        }
        if (map.containsKey("tname")) {
            tname = map.get("tname");
        }
        if (map.containsKey("fuzzy")) {
            fuzzy = map.get("fuzzy").equals("true") ? 1 : 0;
        }
        System.out.println(map);
        System.out.println("查询类型：" + tid  + ", " + tname + ", " + fuzzy);
        return teacherMapper.findBySearch(tid, tname, fuzzy);
    }

    public List<Teacher> findByPage(Integer num, Integer size) {
        // num：第几页，size：一页多大
        // num：从零开始
        List<Teacher> teacherList = teacherMapper.findAll();
        ArrayList<Teacher> list = new ArrayList<Teacher>();

        int start = size * num;
        int end = size * (num + 1);
        int sz = teacherList.size();

        for (int i = start; i < end && i < sz; i++) {
            list.add(teacherList.get(i));
        }

        return list;
    }

    public Integer getLength() {
        return teacherMapper.findAll().size();
    }

    public Teacher findById(Integer tid) {
        return teacherMapper.findById(tid);
    }

    public boolean updateById(Teacher teacher) {
        return teacherMapper.updateById(teacher);
    }

    public boolean save(Teacher teacher) {
        return teacherMapper.save(teacher);
    }

    public boolean deleteById(Integer tid) {
        return teacherMapper.deleteById(tid);
    }

    /**
     * 批量导入教师（Excel）
     * 模板列顺序：
     * 教师工号、教师姓名、初始密码、角色(admin/teacher/dean)、所属学院ID
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
                    String teacherNo = getStringValue(row.getCell(0));
                    String name = getStringValue(row.getCell(1));
                    String password = getStringValue(row.getCell(2));
                    String role = getStringValue(row.getCell(3));
                    Integer departmentId = getIntValue(row.getCell(4));

                    if (teacherNo == null || teacherNo.isEmpty() || name == null || name.isEmpty()) {
                        continue;
                    }
                    if (password == null || password.isEmpty()) {
                        password = "123456";
                    }
                    if (role == null || role.isEmpty()) {
                        role = "teacher";
                    }

                    Teacher teacher = new Teacher();
                    teacher.setTeacherNo(teacherNo);
                    teacher.setTname(name);
                    teacher.setPassword(password);
                    teacher.setRole(role);
                    teacher.setDepartmentId(departmentId);

                    boolean ok = teacherMapper.save(teacher);
                    if (ok) {
                        successCount++;
                    } else {
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

            return String.format("教师导入完成！成功：%d 条，失败：%d 条\n%s",
                    successCount, failCount, errorMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "教师导入失败：" + e.getMessage();
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
