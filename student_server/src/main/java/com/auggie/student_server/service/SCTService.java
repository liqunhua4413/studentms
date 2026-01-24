package com.auggie.student_server.service;

import com.auggie.student_server.entity.CourseTeacherInfo;
import com.auggie.student_server.entity.SCTInfo;
import com.auggie.student_server.entity.StudentCourseTeacher;
import com.auggie.student_server.mapper.StudentCourseTeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: auggie
 * @Date: 2022/2/10 20:10
 * @Description: SCTService
 * @Version 1.0.0
 */

@Service
public class SCTService {
    @Autowired
    private StudentCourseTeacherMapper studentCourseTeacherMapper;

    public List<CourseTeacherInfo> findBySid(Integer sid, String term) {
        return studentCourseTeacherMapper.findByStudentId(sid, term);
    }

    public List<String> findAllTerm() {
        return studentCourseTeacherMapper.findAllTerm();
    }

    public boolean isSCTExist(StudentCourseTeacher studentCourseTeacher) {
        return studentCourseTeacherMapper.findBySCT(studentCourseTeacher).size() != 0;
    }

    public boolean save(StudentCourseTeacher studentCourseTeacher) {
        return studentCourseTeacherMapper.insert(studentCourseTeacher);
    }

    public boolean deleteBySCT(StudentCourseTeacher studentCourseTeacher) {
        return studentCourseTeacherMapper.deleteBySCT(studentCourseTeacher);
    }

    public boolean deleteById(Integer sid, Integer cid, Integer tid, String  term) {
        StudentCourseTeacher studentCourseTeacher = new StudentCourseTeacher();
        studentCourseTeacher.setStudentId(sid);
        studentCourseTeacher.setCourseId(cid);
        studentCourseTeacher.setTeacherId(tid);
        studentCourseTeacher.setTerm(term);
        return studentCourseTeacherMapper.deleteBySCT(studentCourseTeacher);
    }

    public SCTInfo findByIdWithTerm(Integer sid, Integer cid, Integer tid, String term) {
        return studentCourseTeacherMapper.findBySearch(
                sid, null, 0,
                cid, null, 0,
                tid, null, 0,
                null, null, term,
                null, null, null,
                null, null, null, null).get(0);
    }

    public boolean updateGrade(StudentCourseTeacher sct) {
        return studentCourseTeacherMapper.updateGrade(sct);
    }

    public List<SCTInfo> findBySearch(Map<String, Object> map) {
        Integer sid = null, cid = null, tid = null;
        String sname = null, cname = null, tname = null, term = null;
        Integer sFuzzy = null, cFuzzy = null, tFuzzy = null;
        Integer lowBound = null, highBound = null;

        if (map.containsKey("cid")) {
            cid = getIntFromMap(map, "cid");
        }
        if (map.containsKey("sid")) {
            sid = getIntFromMap(map, "sid");
        }
        if (map.containsKey("tid")) {
            tid = getIntFromMap(map, "tid");
        }
        if (map.containsKey("sname")) {
            sname = (String) map.get("sname");
        }
        if (map.containsKey("tname")) {
            tname = (String) map.get("tname");
        }
        if (map.containsKey("cname")) {
            cname = (String) map.get("cname");
        }
        if (map.containsKey("term")) {
            term = (String) map.get("term");
        }
        if (map.containsKey("sFuzzy")) {
            Object val = map.get("sFuzzy");
            sFuzzy = "true".equals(val.toString()) ? 1 : 0;
        }
        if (map.containsKey("tFuzzy")) {
            Object val = map.get("tFuzzy");
            tFuzzy = "true".equals(val.toString()) ? 1 : 0;
        }
        if (map.containsKey("cFuzzy")) {
            Object val = map.get("cFuzzy");
            cFuzzy = "true".equals(val.toString()) ? 1 : 0;
        }
        if (map.containsKey("lowBound")) {
            lowBound = getIntFromMap(map, "lowBound");
        }
        if (map.containsKey("highBound")) {
            highBound = getIntFromMap(map, "highBound");
        }

        Integer classId = null;
        Integer majorId = null;
        Integer departmentId = null;
        String className = null;
        String majorName = null;
        String departmentName = null;
        String gradeLevel = null;

        if (map.containsKey("classId")) {
            classId = getIntFromMap(map, "classId");
        }
        if (map.containsKey("majorId")) {
            majorId = getIntFromMap(map, "majorId");
        }
        if (map.containsKey("departmentId")) {
            departmentId = getIntFromMap(map, "departmentId");
        }
        if (map.containsKey("className")) {
            className = (String) map.get("className");
        }
        if (map.containsKey("majorName")) {
            majorName = (String) map.get("majorName");
        }
        if (map.containsKey("departmentName")) {
            departmentName = (String) map.get("departmentName");
        }
        if (map.containsKey("gradeLevel")) {
            gradeLevel = (String) map.get("gradeLevel");
        }

        System.out.println("SCT 查询：" + map);
        return studentCourseTeacherMapper.findBySearch(
                sid, sname, sFuzzy,
                cid, cname, cFuzzy,
                tid, tname, tFuzzy,
                lowBound, highBound, term,
                classId, majorId, departmentId,
                className, majorName, departmentName, gradeLevel);
    }

    private Integer getIntFromMap(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取补考学生列表（总成绩 < 60）
     */
    public List<SCTInfo> getReexaminationList(Map<String, Object> map) {
        map.put("highBound", 59);
        return findBySearch(map);
    }
}
