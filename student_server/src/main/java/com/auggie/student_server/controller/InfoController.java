package com.auggie.student_server.controller;

import com.auggie.student_server.entity.Term;
import com.auggie.student_server.service.TermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/info")
@CrossOrigin("*")
public class InfoController {
    private final boolean FORBID_COURSE_SELECTION = false;

    @Autowired
    private TermService termService;

    /** 返回当前学期名称（兼容旧前端）；优先取 term 表第一个启用的学期 */
    @GetMapping("/getCurrentTerm")
    public String getCurrentTerm() {
        List<Term> terms = termService.findAll();
        if (terms != null && !terms.isEmpty()) {
            return terms.get(0).getName();
        }
        return "2024-2025-1";
    }

    /** 返回当前学期对象 {id, name}，供前端设置 currentTermId */
    @GetMapping("/getCurrentTermInfo")
    public Term getCurrentTermInfo() {
        List<Term> terms = termService.findAll();
        if (terms != null && !terms.isEmpty()) {
            return terms.get(0);
        }
        return null;
    }

    @RequestMapping("/getForbidCourseSelection")
    public boolean getForbidCourseSelection() {
        return FORBID_COURSE_SELECTION;
    }
}
