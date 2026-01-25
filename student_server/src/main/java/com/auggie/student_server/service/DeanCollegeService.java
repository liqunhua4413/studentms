package com.auggie.student_server.service;

import com.auggie.student_server.entity.Class;
import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.Department;
import com.auggie.student_server.entity.Major;
import com.auggie.student_server.entity.Teacher;
import com.auggie.student_server.mapper.ClassMapper;
import com.auggie.student_server.mapper.CourseMapper;
import com.auggie.student_server.mapper.DepartmentMapper;
import com.auggie.student_server.mapper.MajorMapper;
import com.auggie.student_server.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 院长所属学院服务。根据院长姓名解析其所属学院，用于成绩上传、成绩查询等。
 * 与现有 admin 服务风格一致的 Spring Boot Service。
 */
@Service
public class DeanCollegeService {

    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private ClassMapper classMapper;
    @Autowired
    private CourseMapper courseMapper;

    /**
     * 根据院长姓名（Operator）解析其所属学院。
     *
     * @param operator 院长姓名（与 teacher.tname 对应，登录后为 name）
     * @return 若存在则返回含 collegeId、collegeName 的 Map；否则返回 null
     */
    public Map<String, Object> getDeanCollege(String operator) {
        if (operator == null || operator.trim().isEmpty()) {
            return null;
        }
        Teacher dean = teacherMapper.findDeanByTname(operator.trim());
        if (dean == null || dean.getDepartmentId() == null) {
            return null;
        }
        Department dept = departmentMapper.findById(dean.getDepartmentId());
        if (dept == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("collegeId", dept.getId());
        result.put("collegeName", dept.getName());
        return result;
    }

    /**
     * 院长可见的本学院专业列表（按学院过滤）。
     */
    public List<Major> getMajorsForDean(Integer departmentId) {
        if (departmentId == null) return new ArrayList<>();
        return majorMapper.findByDepartmentId(departmentId);
    }

    /**
     * 院长可见的班级列表（按专业过滤，且专业须属于院长学院）。
     */
    public List<Class> getClassesForDean(Integer majorId) {
        if (majorId == null) return new ArrayList<>();
        return classMapper.findByMajorId(majorId);
    }

    /**
     * 院长可见的本学院开设课程列表。
     */
    public List<Course> getCoursesForDean(Integer departmentId) {
        if (departmentId == null) return new ArrayList<>();
        return courseMapper.findByDepartmentId(departmentId);
    }
}
