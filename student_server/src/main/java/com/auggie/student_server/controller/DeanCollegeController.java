package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Class;
import com.auggie.student_server.entity.Course;
import com.auggie.student_server.entity.Major;
import com.auggie.student_server.service.DeanCollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 院长角色专用接口。提供院长所属学院、本学院专业/班级等，供成绩上传、成绩查询等前端使用。
 * userType=dean 可访问；与现有 GradeController、Department 风格一致。
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/dean")
public class DeanCollegeController {

    @Autowired
    private DeanCollegeService deanCollegeService;

    /**
     * 获取当前院长所属学院。
     * 仅 userType=dean 可访问；返回 collegeId、collegeName。
     */
    @GetMapping("/college")
    public ResponseEntity<Map<String, Object>> getCollege(
            @RequestAttribute(value = "operator", required = false) String operator,
            @RequestAttribute(value = "userType", required = false) String userType) {
        if (!"dean".equalsIgnoreCase(userType)) {
            return ResponseEntity.status(403).build();
        }
        Map<String, Object> college = deanCollegeService.getDeanCollege(operator);
        if (college == null || college.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(college);
    }

    /**
     * 院长可见的本学院专业列表。供成绩查询等筛选使用；替代 /major/findByDepartmentId（admin 接口院长不可访问）。
     */
    @GetMapping("/majors")
    public ResponseEntity<List<Major>> getMajors(
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        if (!"dean".equalsIgnoreCase(userType) || departmentId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(deanCollegeService.getMajorsForDean(departmentId));
    }

    /**
     * 院长可见的班级列表（按专业）。供成绩查询等筛选使用；替代 /class/findByMajorId。
     */
    @GetMapping("/classes")
    public ResponseEntity<List<Class>> getClasses(
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestParam(value = "majorId", required = false) Integer majorId) {
        if (!"dean".equalsIgnoreCase(userType) || majorId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(deanCollegeService.getClassesForDean(majorId));
    }

    /**
     * 院长可见的本学院开设课程列表。供成绩查询、成绩修改申请等使用。
     */
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses(
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestAttribute(value = "departmentId", required = false) Integer departmentId) {
        if (!"dean".equalsIgnoreCase(userType) || departmentId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(deanCollegeService.getCoursesForDean(departmentId));
    }
}
