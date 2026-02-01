package com.auggie.student_server.controller;

import com.auggie.student_server.entity.CourseTeacherInfo;
import com.auggie.student_server.entity.SCTInfo;
import com.auggie.student_server.entity.StudentCourseTeacher;
import com.auggie.student_server.service.SCTService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 20:15
 * @Description: SCTcontroller
 * @Version 1.0.0
 */

@RestController
@CrossOrigin("*")
@RequestMapping("/SCT")
public class SCTcontroller {
    @Autowired
    private SCTService sctService;

    @PostMapping("/save")
    public String save(@RequestBody StudentCourseTeacher studentCourseTeacher) {
        if (sctService.isSCTExist(studentCourseTeacher)) {
            return "禁止重复选课";
        }
        System.out.println("正在保存 sct 记录：" + studentCourseTeacher);
        return sctService.save(studentCourseTeacher) ? "选课成功" : "选课失败，联系管理员";
    }

    @GetMapping("/findBySid/{sid}/{termId}")
    public List<CourseTeacherInfo> findBySid(@PathVariable Integer sid, @PathVariable Integer termId) {
        return sctService.findBySid(sid, termId);
    }

    @GetMapping("/findAllTerm")
    public List<com.auggie.student_server.entity.Term> findAllTerm() {
        return sctService.findAllTerm();
    }

    @PostMapping("/deleteBySCT")
    public boolean deleteBySCT(@RequestBody StudentCourseTeacher studentCourseTeacher) {
        System.out.println("正在删除 sct 记录：" + studentCourseTeacher);
        return sctService.deleteBySCT(studentCourseTeacher);
    }

    @PostMapping("/findBySearch")
    public List<SCTInfo> findBySearch(@RequestBody Map<String, Object> map) {
        return sctService.findBySearch(map);
    }

    @GetMapping("/findById/{sid}/{cid}/{tid}/{termId}")
    public SCTInfo findById(@PathVariable Integer sid,
                            @PathVariable Integer cid,
                            @PathVariable Integer tid,
                            @PathVariable Integer termId) {
        return sctService.findByIdWithTerm(sid, cid, tid, termId);
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody StudentCourseTeacher studentCourseTeacher) {
        boolean result = sctService.updateGrade(studentCourseTeacher);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(500).body("更新失败");
        }
    }

    /** 通过 URL 参数更新成绩（兼容旧前端） */
    @GetMapping("/updateById/{sid}/{cid}/{tid}/{termId}/{grade}")
    public ResponseEntity<?> updateById(
            @PathVariable Integer sid, @PathVariable Integer cid, @PathVariable Integer tid,
            @PathVariable Integer termId, @PathVariable Float grade) {
        StudentCourseTeacher sct = new StudentCourseTeacher();
        sct.setStudentId(sid);
        sct.setCourseId(cid);
        sct.setTeacherId(tid);
        sct.setTermId(termId);
        sct.setGrade(grade);
        boolean result = sctService.updateGrade(sct);
        return result ? ResponseEntity.ok(true) : ResponseEntity.status(500).body("更新失败");
    }

    @GetMapping("/deleteById/{sid}/{cid}/{tid}/{termId}")
    public boolean deleteById(@PathVariable Integer sid,
                              @PathVariable Integer cid,
                              @PathVariable Integer tid,
                              @PathVariable Integer termId) {
        return sctService.deleteById(sid, cid, tid, termId);
    }

    @PostMapping("/reexamination")
    public List<SCTInfo> getReexaminationList(@RequestBody Map<String, Object> map) {
        return sctService.getReexaminationList(map);
    }

    @PostMapping("/findByStudentCourseTerm")
    public ResponseEntity<?> findByStudentCourseTerm(@RequestBody StudentCourseTeacher sct) {
        List<StudentCourseTeacher> list = sctService.findByStudentCourseTerm(sct);
        if (list != null && !list.isEmpty()) {
            return ResponseEntity.ok(list.get(0));
        }
        return ResponseEntity.ok(null);
    }
}
