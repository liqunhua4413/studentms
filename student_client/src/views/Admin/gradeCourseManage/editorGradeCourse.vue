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
              <el-select v-model="ruleForm.courseId" placeholder="请选择课程" style="width: 100%">
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
              <el-select v-model="ruleForm.classId" placeholder="请选择班级" @change="handleClassChange" style="width: 100%">
                <el-option v-for="item in classes" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentId">
              <el-select v-model="ruleForm.studentId" placeholder="请选择学号" @change="handleStudentChange" style="width: 100%" filterable>
                <el-option v-for="item in students" :key="item.sid" :label="item.studentNo" :value="item.sid"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生姓名">
              <el-input v-model="ruleForm.sname" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="平时成绩" prop="usualGrade">
              <el-input-number v-model="ruleForm.usualGrade" :min="0" :max="100"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期末成绩" prop="finalGrade">
              <el-input-number v-model="ruleForm.finalGrade" :min="0" :max="100"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总成绩 (分数)" prop="totalGrade">
              <el-input-number v-model="ruleForm.totalGrade" :min="0" :max="100"></el-input-number>
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
        courseId: null,
        teacherId: null,
        term: null,
        departmentId: null,
        majorId: null,
        classId: null,
        departmentName: '',
        majorName: '',
        courseName: '',
        gradeLevel: '',
        className: '',
        sname: '',
        usualGrade: 0,
        finalGrade: 0,
        totalGrade: 0
      },
      departments: [],
      majors: [],
      classes: [],
      courses: [],
      students: [],
      rules: {
        departmentId: [{ required: true, message: '请选择学院', trigger: 'change' }],
        majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
        studentId: [{ required: true, message: '请选择学号', trigger: 'change' }],
        totalGrade: [{ required: true, message: '请输入分数', trigger: 'change' }]
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
          courseId: data.cid,
          teacherId: data.tid,
          term: data.term,
          departmentId: data.departmentId,
          majorId: data.majorId,
          classId: data.classId,
          departmentName: data.departmentName,
          majorName: data.majorName,
          courseName: data.cname,
          gradeLevel: data.gradeLevel,
          className: data.className,
          sname: data.sname,
          usualGrade: data.usualGrade || 0,
          finalGrade: data.finalGrade || 0,
          totalGrade: data.totalGrade || data.grade || 0
        };
        // 加载联动数据
        if (data.departmentId) this.fetchMajors(data.departmentId);
        if (data.majorId) this.fetchClasses(data.majorId);
        if (data.classId) this.fetchStudents(data.classId);
      });
    }
  },
  methods: {
    initData() {
      this.axios.get('/department/findAll').then(resp => this.departments = resp.data);
      this.axios.get('/course/findAll').then(resp => this.courses = resp.data);
    },
    handleDepartmentChange(val) {
      this.ruleForm.majorId = null;
      this.ruleForm.classId = null;
      this.ruleForm.studentId = null;
      this.ruleForm.sname = '';
      this.majors = [];
      this.classes = [];
      this.students = [];
      if (val) this.fetchMajors(val);
    },
    handleMajorChange(val) {
      this.ruleForm.classId = null;
      this.ruleForm.studentId = null;
      this.ruleForm.sname = '';
      this.classes = [];
      this.students = [];
      if (val) this.fetchClasses(val);
    },
    handleClassChange(val) {
      this.ruleForm.studentId = null;
      this.ruleForm.sname = '';
      this.students = [];
      if (val) this.fetchStudents(val);
    },
    handleStudentChange(val) {
      const student = this.students.find(s => s.sid === val);
      if (student) {
        this.ruleForm.sname = student.sname;
      }
    },
    fetchMajors(deptId) {
      this.axios.get(`/major/findByDepartmentId/${deptId}`).then(resp => this.majors = resp.data);
    },
    fetchClasses(majorId) {
      this.axios.get(`/class/findByMajorId/${majorId}`).then(resp => this.classes = resp.data);
    },
    fetchStudents(classId) {
      // 假设有一个根据班级查学生的接口，如果没有，暂用 findBySearch 模拟
      this.axios.get('/student/findAll').then(resp => {
        this.students = resp.data.filter(s => s.classId === classId);
      });
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.axios.post("/SCT/update", this.ruleForm).then(resp => {
            if (resp.data === true) {
              this.$message.success('编辑成功');
              this.$router.back();
            } else {
              this.$message.error('编辑失败');
            }
          });
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  }
}
</script>