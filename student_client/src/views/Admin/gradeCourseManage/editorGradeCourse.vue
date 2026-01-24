<template>
  <div style="padding: 20px;">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>编辑成绩</span>
      </div>
      <el-form style="width: 80%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院" prop="departmentId">
              <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" @change="handleDepartmentChange" style="width: 100%">
                <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="majorId">
              <el-select v-model="ruleForm.majorId" placeholder="请选择专业" @change="handleMajorChange" style="width: 100%">
                <el-option v-for="item in majors" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="courseId">
              <el-select v-model="ruleForm.courseId" placeholder="请选择课程" style="width: 100%" filterable @change="handleCourseOrTermChange">
                <el-option v-for="item in courses" :key="item.id" :label="item.cname" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="gradeLevel">
              <el-input v-model="ruleForm.gradeLevel" placeholder="如：2024级"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级" prop="classId">
              <el-select v-model="ruleForm.classId" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="item in classes" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input 
                v-model="ruleForm.studentNo" 
                placeholder="输入学号后按回车确认" 
                @keyup.enter.native="handleStudentNoEnter"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生姓名">
              <el-input v-model="ruleForm.sname" disabled placeholder="自动根据学号获取"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期" prop="term">
              <el-select v-model="ruleForm.term" placeholder="请选择学期" style="width: 100%" @change="handleCourseOrTermChange">
                <el-option v-for="item in terms" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任课教师">
              <el-input v-model="ruleForm.teacherName" disabled placeholder="自动根据成绩记录获取"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="平时成绩" prop="usualScore">
              <el-input-number v-model="ruleForm.usualScore" :min="0" :max="100" :precision="2" :step="0.01"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期中成绩" prop="midScore">
              <el-input-number v-model="ruleForm.midScore" :min="0" :max="100" :precision="2" :step="0.01"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="期末成绩" prop="finalScore">
              <el-input-number v-model="ruleForm.finalScore" :min="0" :max="100" :precision="2" :step="0.01"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总成绩" prop="grade">
              <el-input-number v-model="ruleForm.grade" :min="0" :max="100" :precision="2" :step="0.01"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="备注/标志" prop="remark">
              <el-input v-model="ruleForm.remark" placeholder="备注信息"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item style="margin-top: 20px;">
          <el-button type="primary" @click="submitForm('ruleForm')">提交</el-button>
          <el-button @click="resetForm('ruleForm')">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        studentId: null,
        studentNo: '',
        courseId: null,
        teacherId: null,
        teacherName: '',
        term: null,
        departmentId: null,
        majorId: null,
        classId: null,
        gradeLevel: '',
        sname: '',
        usualScore: null,
        midScore: null,
        finalScore: null,
        grade: null,
        remark: ''
      },
      departments: [],
      majors: [],
      classes: [],
      courses: [],
      terms: [],
      rules: {
        departmentId: [{ required: true, message: '请选择学院', trigger: 'change' }],
        majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
        studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        grade: [{ required: true, message: '请输入总成绩', trigger: 'change' }],
        term: [{ required: true, message: '请选择学期', trigger: 'change' }]
      }
    };
  },
  created() {
    this.initData();
    const { sid, cid, tid, term } = this.$route.query;
    if (sid && cid && tid && term) {
      this.axios.get(`/SCT/findById/${sid}/${cid}/${tid}/${term}`).then(resp => {
        const data = resp.data;
        this.ruleForm = {
          studentId: data.sid,
          studentNo: data.studentNo || '',
          courseId: data.cid,
          teacherId: data.tid,
          term: data.term,
          departmentId: data.departmentId,
          majorId: data.majorId,
          classId: data.classId,
          gradeLevel: data.gradeLevel,
          sname: data.sname,
          usualScore: data.usualScore || data.usualGrade || null,
          midScore: data.midScore || null,
          finalScore: data.finalScore || data.finalGrade || null,
          grade: data.grade || data.totalGrade || null,
          remark: data.remark || ''
        };
        // 自动触发一次根据学号获取信息以补充 studentNo
        if (data.sid && !data.studentNo) {
            this.axios.get(`/student/findById/${data.sid}`).then(sResp => {
                if (sResp.data) this.ruleForm.studentNo = sResp.data.studentNo;
            });
        }
        // 加载联动数据
        if (data.departmentId) this.fetchMajors(data.departmentId);
        if (data.majorId) this.fetchClasses(data.majorId);
      });
    }
  },
  methods: {
    initData() {
      this.axios.get('/department/findAll').then(resp => this.departments = resp.data);
      this.axios.get('/course/findAll').then(resp => this.courses = resp.data);
      this.axios.get('/SCT/findAllTerm').then(resp => this.terms = resp.data);
    },
    handleDepartmentChange(val) {
      this.ruleForm.majorId = null;
      this.ruleForm.classId = null;
      this.majors = [];
      this.classes = [];
      if (val) this.fetchMajors(val);
    },
    handleMajorChange(val) {
      this.ruleForm.classId = null;
      this.classes = [];
      if (val) this.fetchClasses(val);
    },
    handleStudentNoEnter() {
      if (!this.ruleForm.studentNo) return;
      this.axios.post('/student/findByStudentNo', { studentNo: this.ruleForm.studentNo }).then(resp => {
        if (resp.data) {
          const student = resp.data;
          this.ruleForm.sname = student.sname;
          this.ruleForm.studentId = student.id;
          // 可选：根据学生信息自动填充学院专业班级
          this.ruleForm.departmentId = student.departmentId;
          this.ruleForm.majorId = student.majorId;
          this.ruleForm.classId = student.classId;
          this.ruleForm.gradeLevel = student.gradeLevel;
          
          if (student.departmentId) this.fetchMajors(student.departmentId);
          if (student.majorId) this.fetchClasses(student.majorId);
          
          this.$message.success(`已找到学生：${student.sname}`);
          
          // 如果已经选择了课程和学期，自动查询成绩记录
          if (this.ruleForm.courseId && this.ruleForm.term) {
            this.loadGradeRecord();
          }
        } else {
          this.$message.error('未找到该学号对应的学生');
          this.ruleForm.sname = '';
          this.ruleForm.studentId = null;
        }
      }).catch(err => {
          this.$message.error('查询学生失败');
      });
    },
    handleCourseOrTermChange() {
      // 当课程或学期改变时，如果已有学号，自动查询成绩记录
      if (this.ruleForm.studentId && this.ruleForm.courseId && this.ruleForm.term) {
        this.loadGradeRecord();
      }
    },
    loadGradeRecord() {
      if (!this.ruleForm.studentId || !this.ruleForm.courseId || !this.ruleForm.term) {
        return;
      }
      
      const queryParams = {
        studentId: this.ruleForm.studentId,
        courseId: this.ruleForm.courseId,
        term: this.ruleForm.term
      };
      
      this.axios.post('/SCT/findByStudentCourseTerm', queryParams).then(resp => {
        if (resp.data) {
          const record = resp.data;
          // 填充成绩信息
          this.ruleForm.usualScore = record.usualScore;
          this.ruleForm.midScore = record.midScore;
          this.ruleForm.finalScore = record.finalScore;
          this.ruleForm.grade = record.grade;
          this.ruleForm.remark = record.remark || '';
          this.ruleForm.teacherId = record.teacherId;
          
          // 查询教师姓名
          if (record.teacherId) {
            this.axios.get(`/teacher/findById/${record.teacherId}`).then(teacherResp => {
              if (teacherResp.data) {
                this.ruleForm.teacherName = teacherResp.data.tname || '';
              }
            }).catch(() => {});
          }
          
          this.$message.success('已加载该学生的成绩记录');
        } else {
          // 没有找到记录，清空成绩字段（保留其他信息）
          this.ruleForm.usualScore = null;
          this.ruleForm.midScore = null;
          this.ruleForm.finalScore = null;
          this.ruleForm.grade = null;
          this.ruleForm.remark = '';
          this.ruleForm.teacherId = null;
          this.ruleForm.teacherName = '';
          this.$message.info('未找到该学生的成绩记录，可以新建');
        }
      }).catch(err => {
        console.error('查询成绩记录失败:', err);
        this.$message.warning('查询成绩记录失败，请手动填写');
      });
    },
    fetchMajors(deptId) {
      this.axios.get(`/major/findByDepartmentId/${deptId}`).then(resp => this.majors = resp.data);
    },
    fetchClasses(majorId) {
      this.axios.get(`/class/findByMajorId/${majorId}`).then(resp => this.classes = resp.data);
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (!this.ruleForm.studentId) {
              this.$message.warning('请先输入有效的学号并按回车确认');
              return;
          }
          if (!this.ruleForm.courseId) {
              this.$message.warning('请选择课程');
              return;
          }
          if (!this.ruleForm.term) {
              this.$message.warning('请选择学期');
              return;
          }
          // 构建提交数据，确保字段名正确
          const submitData = {
            studentId: this.ruleForm.studentId,
            courseId: this.ruleForm.courseId,
            teacherId: this.ruleForm.teacherId,
            term: this.ruleForm.term,
            usualScore: this.ruleForm.usualScore,
            midScore: this.ruleForm.midScore,
            finalScore: this.ruleForm.finalScore,
            grade: this.ruleForm.grade,
            remark: this.ruleForm.remark
          };
          this.axios.post("/SCT/update", submitData).then(resp => {
            if (resp.data === true) {
              this.$message.success('编辑成功');
              this.$router.back();
            } else {
              this.$message.error('编辑失败');
            }
          }).catch(err => {
            console.error('更新失败:', err);
            this.$message.error('更新失败：' + (err.response?.data || err.message));
          });
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.sname = '';
      this.ruleForm.studentId = null;
    }
  }
}
</script>
