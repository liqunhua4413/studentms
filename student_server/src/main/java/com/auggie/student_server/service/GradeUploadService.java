package com.auggie.student_server.service;

import com.auggie.student_server.common.ScoreStatus;
import com.auggie.student_server.entity.*;
import com.auggie.student_server.mapper.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 成绩上传与发布服务。
 * 权限：admin 全部；teacher 仅 course_open 任课；dean 仅本学院 course.department_id。
 * 上传前检查：学号+课程+学期是否已有成绩，有则拒绝整单并提示提交修改申请。
 * 上传后 status=UPLOADED（已上传，已入库），等待管理员审核发布；发布后 PUBLISHED。
 */
@Service
public class GradeUploadService {

    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private GradeChangeLogService gradeChangeLogService;
    @Autowired
    private CourseOpenMapper courseOpenMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private ClassMapper classMapper;
    @Autowired
    private OperationLogService operationLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private ScoreImportRecordMapper scoreImportRecordMapper;

    @Value("${file.upload.path:${user.home}/studentms/uploads}")
    private String uploadPath;

    /**
     * 上传成绩单。
     * 上传前检查：学号+课程+学期 是否已有成绩；有则拒绝并提示「本学期本课程"学号"成绩已存在，如需修改请提交修改申请」。
     * 权限判断：admin 放行；teacher 必须任课（course_open）；dean 必须本学院课程（course.department_id）。
     * 插入 score 时 status=UPLOADED（已上传）；每次插入后写 grade_change_log（IMPORT）。
     *
     * @param file                  Excel 文件
     * @param operator              操作人姓名（如 系统管理员、教师名）
     * @param userType              admin / dean / teacher
     * @param userDepartmentId      当前用户所属学院 id（dean/teacher 必有）
     * @param userTeacherId         当前用户教师 id（teacher 必有）
     * @param selectedDepartmentId  上传所选学院（admin 必选；dean/teacher 强制为本院）
     * @return 成功或错误信息
     */
    public String uploadExcel(MultipartFile file, String operator, String userType,
                              Integer userDepartmentId, Integer userTeacherId, Integer selectedDepartmentId) {
        if (file == null || file.isEmpty()) {
            return "上传失败：请选择文件";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return "上传失败：请上传 Excel 文件（.xlsx / .xls）";
        }
        if (selectedDepartmentId == null) {
            return "上传失败：请先选择学院";
        }
        Department dept = departmentMapper.findById(selectedDepartmentId);
        if (dept == null) {
            return "上传失败：选择的学院不存在";
        }

        Path savedFilePath;
        try {
            String resolved = uploadPath;
            if (resolved.startsWith("./") || resolved.startsWith(".\\")) {
                resolved = new File(".").getCanonicalPath() + File.separator + resolved.substring(2);
            }
            Path uploadDir = Paths.get(resolved, "grade_excel");
            Files.createDirectories(uploadDir);
            String savedFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
            savedFilePath = uploadDir.resolve(savedFileName);
            file.transferTo(savedFilePath.toFile());
        } catch (IOException e) {
            return "上传失败：文件保存异常 " + e.getMessage();
        }

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(savedFilePath.toFile());
            Sheet sheet = workbook.getSheetAt(0);

            Row row2 = sheet.getRow(1);
            Row row3 = sheet.getRow(2);
            String row2Text = getMergedCellValue(sheet, row2, 0);
            String row3Text = getMergedCellValue(sheet, row3, 0);

            String courseName = extractValue(row2Text, "课程名称：");
            String teacherName = extractValue(row2Text, "任课教师：");
            String term = extractValue(row3Text, "开课学期：");
            String fullClassName = extractValue(row3Text, "开课班级：");

            if (courseName == null || courseName.trim().isEmpty()) {
                return "上传失败：成绩单中未提取到课程名称";
            }
            if (teacherName == null || teacherName.trim().isEmpty()) {
                return "上传失败：成绩单中未提取到任课教师";
            }
            if (term == null || term.trim().isEmpty()) {
                return "上传失败：成绩单中未提取到开课学期";
            }

            List<Course> courses = courseMapper.findBySearch(null, courseName.trim(), 0, null, null);
            if (courses == null || courses.isEmpty()) {
                return "上传失败：课程【" + courseName + "】不存在，请联系管理员维护基础数据";
            }
            Course targetCourse = courses.get(0);

            List<Teacher> teachers = teacherMapper.findBySearch(null, teacherName.trim(), 0);
            if (teachers == null || teachers.isEmpty()) {
                return "上传失败：教师【" + teacherName + "】不存在，请与系统内姓名完全一致";
            }
            Integer targetTeacherId = teachers.get(0).getId();

            /* 权限校验：教师只能上传自己任课课程；院长只能上传本学院课程；管理员全部 */
            if ("teacher".equals(userType)) {
                if (userTeacherId == null) {
                    return "上传失败：教师身份未识别，请重新登录";
                }
                if (courseOpenMapper.countByTeacherCourseTerm(userTeacherId, targetCourse.getId(), term) <= 0) {
                    return "上传失败：权限不足，仅可上传本人任课课程（course_open）";
                }
            } else if ("dean".equals(userType)) {
                if (userDepartmentId == null) {
                    return "上传失败：院长学院未识别，请重新登录";
                }
                if (!userDepartmentId.equals(targetCourse.getDepartmentId())) {
                    return "上传失败：权限不足，仅可上传本学院课程（course.department_id）";
                }
            }

            String extractedGradeLevel = "";
            String extractedMajorName = "";
            String extractedClassName = "";
            if (fullClassName != null && !fullClassName.isEmpty()) {
                java.util.regex.Matcher gm = java.util.regex.Pattern.compile("(\\d{2}级)").matcher(fullClassName);
                if (gm.find()) extractedGradeLevel = "20" + gm.group(1);
                java.util.regex.Matcher cm = java.util.regex.Pattern.compile("(\\d+班)$").matcher(fullClassName);
                if (cm.find()) extractedClassName = cm.group(1);
                extractedMajorName = fullClassName.replaceAll("\\d{2}级", "").replaceAll("\\d+班$", "").trim();
            }

            List<Major> majorList = majorMapper.findBySearch(extractedMajorName, selectedDepartmentId);
            if (majorList == null || majorList.isEmpty()) {
                return "上传失败：专业【" + extractedMajorName + "】在所属学院下未定义";
            }
            Major targetMajor = majorList.get(0);

            List<com.auggie.student_server.entity.Class> classList = classMapper.findBySearch(
                    extractedClassName, targetMajor.getId(), selectedDepartmentId);
            if (classList == null || classList.isEmpty()) {
                return "上传失败：班级【" + extractedClassName + "】在对应专业下未定义";
            }
            com.auggie.student_server.entity.Class targetClass = classList.get(0);

            List<Score> validList = new ArrayList<>();
            List<String> errorReports = new ArrayList<>();

            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                if (i >= 42 && i <= 45) continue;
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String first = getStringValue(row.getCell(0));
                if (first != null && (first.startsWith("各档成绩百分比") || first.contains("签字"))) break;

                validateAndAdd(row, 0, targetCourse.getId(), targetTeacherId, term, targetMajor, targetClass,
                        extractedGradeLevel, validList, errorReports, i + 1);
                validateAndAdd(row, 7, targetCourse.getId(), targetTeacherId, term, targetMajor, targetClass,
                        extractedGradeLevel, validList, errorReports, i + 1);
            }

            if (!errorReports.isEmpty()) {
                return "上传被拒绝：\n" + String.join("\n", errorReports.subList(0, Math.min(20, errorReports.size())))
                        + (errorReports.size() > 20 ? "\n...等" + errorReports.size() + "条" : "");
            }
            if (validList.isEmpty()) {
                return "上传被拒绝：无有效成绩行";
            }

            for (Score s : validList) {
                scoreMapper.insert(s);
                gradeChangeLogService.logImport(s, null, operator);
            }

            ScoreImportRecord record = new ScoreImportRecord();
            record.setFileName(originalFilename);
            record.setFilePath(savedFilePath.toString());
            record.setTerm(term);
            record.setCourseId(targetCourse.getId());
            record.setTeacherId(targetTeacherId);
            record.setDepartmentId(selectedDepartmentId);
            record.setOperator(operator);
            record.setStatus("SUCCESS");
            record.setMessage("成功导入 " + validList.size() + " 条，status=UPLOADED（已上传）");
            record.setCreatedAt(LocalDateTime.now());
            scoreImportRecordMapper.save(record);

            operationLogService.recordOperation(operator, "新增", "score", null,
                    "成绩上传 " + originalFilename + "，" + validList.size() + " 条已上传，已写 grade_change_log");

            return "上传成功！已导入 " + validList.size() + " 条成绩（已上传），已入库，等待管理员审核发布后学生可见。";
        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败：" + e.getMessage();
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (IOException ignored) {}
            }
        }
    }

    /**
     * 发布成绩。仅 admin 可调用。
     * 状态判断：将指定 score 从 UPLOADED（已上传）改为 PUBLISHED；每条写 grade_change_log（PUBLISH）。
     *
     * @param scoreIds    要发布的成绩 id 列表
     * @param operatorId  操作人 id（admin）
     * @param operatorName 操作人姓名
     * @return 成功或错误信息
     */
    public String publishScores(List<Integer> scoreIds, Integer operatorId, String operatorName) {
        if (scoreIds == null || scoreIds.isEmpty()) {
            return "请选择要发布的成绩";
        }
        int ok = 0;
        for (Integer id : scoreIds) {
            Score s = scoreMapper.findById(id);
            if (s == null) continue;
            if (!ScoreStatus.UPLOADED.equals(s.getStatus())) {
                continue;
            }
            Score before = new Score();
            before.setId(s.getId());
            before.setStudentId(s.getStudentId());
            before.setCourseId(s.getCourseId());
            before.setTeacherId(s.getTeacherId());
            before.setTerm(s.getTerm());
            before.setUsualScore(s.getUsualScore());
            before.setMidScore(s.getMidScore());
            before.setFinalScore(s.getFinalScore());
            before.setGrade(s.getGrade());
            before.setRemark(s.getRemark());
            before.setStatus(s.getStatus());
            s.setStatus(ScoreStatus.PUBLISHED);
            scoreMapper.updateById(s);
            gradeChangeLogService.logPublish(before, s, operatorId, operatorName);
            ok++;
        }
        operationLogService.recordOperation(operatorName, "编辑", "score", null,
                "发布成绩 " + ok + " 条，UPLOADED→PUBLISHED");
        return "发布成功，共 " + ok + " 条成绩已对学生可见。";
    }

    /**
     * 锁定成绩。仅 admin 可调用。将任意状态改为 LOCKED，写 grade_change_log。
     * 权限判断：仅 userType=admin。
     *
     * @param scoreIds    要锁定的成绩 id 列表
     * @param operatorId  操作人 id（admin）
     * @param operatorName 操作人姓名
     * @return 成功或错误信息
     */
    @Transactional(rollbackFor = Exception.class)
    public String lockScores(List<Integer> scoreIds, Integer operatorId, String operatorName) {
        if (scoreIds == null || scoreIds.isEmpty()) {
            return "请选择要锁定的成绩";
        }
        int ok = 0;
        int skipped = 0;
        for (Integer id : scoreIds) {
            Score s = scoreMapper.findById(id);
            if (s == null) continue;
            if (ScoreStatus.LOCKED.equals(s.getStatus())) {
                skipped++; // 已锁定，跳过
                continue;
            }
            // 只有 PUBLISHED 状态才能锁定
            if (!ScoreStatus.PUBLISHED.equals(s.getStatus())) {
                skipped++; // 未发布，跳过
                continue;
            }
            Score before = new Score();
            before.setId(s.getId());
            before.setStudentId(s.getStudentId());
            before.setCourseId(s.getCourseId());
            before.setTeacherId(s.getTeacherId());
            before.setTerm(s.getTerm());
            before.setUsualScore(s.getUsualScore());
            before.setMidScore(s.getMidScore());
            before.setFinalScore(s.getFinalScore());
            before.setGrade(s.getGrade());
            before.setRemark(s.getRemark());
            before.setStatus(s.getStatus());
            s.setStatus(ScoreStatus.LOCKED);
            scoreMapper.updateById(s);
            // 写入 grade_change_log（LOCK 操作）
            gradeChangeLogService.appendLog(s.getId(), "LOCK", operatorId, operatorName,
                    toJson(before), toJson(s));
            ok++;
        }
        operationLogService.recordOperation(operatorName, "编辑", "score", null,
                "锁定成绩 " + ok + " 条");
        if (skipped > 0) {
            return "锁定成功，共 " + ok + " 条成绩已锁定。" + (skipped > 0 ? " 跳过 " + skipped + " 条（未发布或已锁定）。" : "");
        }
        return "锁定成功，共 " + ok + " 条成绩已锁定。";
    }

    /**
     * 强制修正锁定成绩。仅 admin 可调用。修正后写超高等级日志。
     * 权限判断：仅 userType=admin；状态判断：仅 LOCKED 可强制修正。
     *
     * @param scoreId     成绩 id
     * @param usualScore  平时成绩
     * @param midScore    期中成绩
     * @param finalScore  期末成绩
     * @param grade       总成绩
     * @param reason      修正原因（必填）
     * @param operatorId  操作人 id（admin）
     * @param operatorName 操作人姓名
     * @return 成功或错误信息
     */
    @Transactional(rollbackFor = Exception.class)
    public String forceEdit(Integer scoreId, Object usualScore, Object midScore, Object finalScore, Object grade,
                           String reason, Integer operatorId, String operatorName) {
        if (scoreId == null) {
            return "修正失败：成绩 id 缺失";
        }
        if (reason == null || reason.trim().isEmpty()) {
            return "修正失败：修正原因必填";
        }
        Score s = scoreMapper.findById(scoreId);
        if (s == null) {
            return "修正失败：成绩记录不存在";
        }
        if (!ScoreStatus.LOCKED.equals(s.getStatus())) {
            return "修正失败：仅可修正锁定状态的成绩（当前状态：" + s.getStatus() + "）";
        }

        Score before = new Score();
        before.setId(s.getId());
        before.setUsualScore(s.getUsualScore());
        before.setMidScore(s.getMidScore());
        before.setFinalScore(s.getFinalScore());
        before.setGrade(s.getGrade());
        before.setStatus(s.getStatus());

        // 更新成绩字段
        if (usualScore != null) {
            s.setUsualScore(toDecimal(parseFloat(usualScore)));
        }
        if (midScore != null) {
            s.setMidScore(toDecimal(parseFloat(midScore)));
        }
        if (finalScore != null) {
            s.setFinalScore(toDecimal(parseFloat(finalScore)));
        }
        if (grade != null) {
            BigDecimal gradeValue = toDecimal(parseFloat(grade));
            // 总分四舍五入为整数
            if (gradeValue != null) {
                gradeValue = gradeValue.setScale(0, java.math.RoundingMode.HALF_UP);
            }
            s.setGrade(gradeValue);
        }

        scoreMapper.updateById(s);

        // 写入超高等级日志（FORCE_EDIT 操作）
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("reason", reason.trim());
            logData.put("before", toJson(before));
            logData.put("after", toJson(s));
            String logJson = objectMapper.writeValueAsString(logData);
            gradeChangeLogService.appendLog(scoreId, "FORCE_EDIT", operatorId, operatorName,
                    toJson(before), logJson);
        } catch (Exception e) {
            System.err.println("写入强制修正日志失败: " + e.getMessage());
            e.printStackTrace();
        }

        operationLogService.recordOperation(operatorName, "强制修正", "score", scoreId != null ? scoreId.longValue() : null,
                "强制修正锁定成绩，原因：" + reason.trim());
        return "修正成功，成绩已更新并写入超高等级日志。";
    }

    private BigDecimal toDecimal(Float f) {
        if (f == null) return null;
        return BigDecimal.valueOf(f);
    }

    private Float parseFloat(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).floatValue();
        try {
            return Float.parseFloat(o.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Score s) {
        if (s == null) return null;
        try {
            return objectMapper.writeValueAsString(s);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void validateAndAdd(Row row, int startCol, int courseId, int teacherId, String term,
                               Major major, com.auggie.student_server.entity.Class targetClass, String gradeLevel,
                               List<Score> validList, List<String> errorReports, int lineNum) {
        String studentNo = getStringValue(row.getCell(startCol));
        if (studentNo == null || studentNo.isEmpty()) return;
        Student st = studentMapper.findByStudentNo(studentNo);
        if (st == null) {
            errorReports.add("第" + lineNum + "行：学号【" + studentNo + "】不存在，请先在学生管理中导入该学生");
            return;
        }
        Score exist = scoreMapper.findByStudentCourseTerm(st.getId(), courseId, term);
        if (exist != null) {
            errorReports.add("本学期本课程\"" + studentNo + "\"成绩已存在，如需修改请提交修改申请。");
            return;
        }
        boolean alreadyInList = validList.stream().anyMatch(v ->
                v.getStudentId().equals(st.getId()) && v.getCourseId().equals(courseId) && term.equals(v.getTerm()));
        if (alreadyInList) return;
        BigDecimal usual = toDecimal(getFloatValue(row.getCell(startCol + 2)));
        BigDecimal mid = toDecimal(getFloatValue(row.getCell(startCol + 3)));
        BigDecimal final_ = toDecimal(getFloatValue(row.getCell(startCol + 4)));
        BigDecimal grade = toDecimal(getFloatValue(row.getCell(startCol + 5)));
        String remark = getStringValue(row.getCell(startCol + 6));

        // 总分四舍五入为整数，其它分数保持小数
        if (grade != null) {
            grade = grade.setScale(0, java.math.RoundingMode.HALF_UP);
        }

        Score s = new Score();
        s.setStudentId(st.getId());
        s.setCourseId(courseId);
        s.setTeacherId(teacherId);
        s.setTerm(term);
        s.setUsualScore(usual);
        s.setMidScore(mid);
        s.setFinalScore(final_);
        s.setGrade(grade);
        s.setRemark(remark);
        s.setStatus(ScoreStatus.UPLOADED);
        validList.add(s);
    }

    private Float getFloatValue(Cell c) {
        if (c == null) return null;
        try {
            switch (c.getCellType()) {
                case NUMERIC:
                    return (float) c.getNumericCellValue();
                case STRING:
                    String s = c.getStringCellValue().trim();
                    if (s.isEmpty()) return null;
                    return Float.parseFloat(s);
                case FORMULA:
                    try { return (float) c.getNumericCellValue(); }
                    catch (Exception e) {
                        try { return Float.parseFloat(c.getStringCellValue().trim()); } catch (Exception e2) { return null; }
                    }
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getStringValue(Cell c) {
        if (c == null) return null;
        org.apache.poi.ss.usermodel.DataFormatter df = new org.apache.poi.ss.usermodel.DataFormatter();
        return df.formatCellValue(c).trim();
    }

    private String getMergedCellValue(Sheet sheet, Row row, int col) {
        if (row == null) return null;
        Cell cell = row.getCell(col);
        if (cell != null) return getStringValue(cell);
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress r = sheet.getMergedRegion(i);
            if (r.isInRange(row.getRowNum(), col)) {
                Row fr = sheet.getRow(r.getFirstRow());
                if (fr != null) {
                    Cell fc = fr.getCell(r.getFirstColumn());
                    if (fc != null) return getStringValue(fc);
                }
            }
        }
        return null;
    }

    /**
     * 从文本中提取值。成绩单格式：行2「课程名称:基础会计 课程类别:专业课 ...」，行3「开课学期:... 开课班级:24级会计学1班」。
     * 支持全角/半角冒号（：、:），冒号后无空格或有空格均正确截取；使用明显间隔截断，避免误取后续字段。
     */
    private String extractValue(String text, String prefix) {
        if (text == null) return null;
        String p1 = prefix.replace("：", ":");
        String p2 = prefix.replace(":", "：");
        int index = text.indexOf(p1);
        String usedPrefix = p1;
        if (index == -1) {
            index = text.indexOf(p2);
            usedPrefix = p2;
        }
        if (index == -1) return null;
        int start = index + usedPrefix.length();
        int end = text.length();
        String[] separators = {"  ", " 课程", " 课程类别", " 教师", " 任课", " 学分", " 学时", " 开课"};
        for (String sep : separators) {
            int sepIndex = text.indexOf(sep, start);
            if (sepIndex != -1 && sepIndex < end) end = sepIndex;
        }
        return text.substring(start, end).trim();
    }
}
