<template>
  <div>
    <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="ruleForm.studentNo" placeholder="学号，唯一"></el-input>
      </el-form-item>
      <el-form-item label="学生姓名" prop="sname">
        <el-input v-model="ruleForm.sname"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="ruleForm.password" show-password></el-input>
      </el-form-item>
      <el-form-item label="年级" prop="gradeLevelId">
        <el-select v-model="ruleForm.gradeLevelId" placeholder="请选择年级" clearable style="width: 100%">
          <el-option v-for="item in gradeLevels" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属学院" prop="departmentId">
        <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" clearable style="width: 100%" @change="onDepartmentChange">
          <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属专业" prop="majorId">
        <el-select v-model="ruleForm.majorId" placeholder="请选择专业" clearable style="width: 100%" @change="onMajorChange">
          <el-option v-for="item in majors" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属班级" prop="classId">
        <el-select v-model="ruleForm.classId" placeholder="请选择班级" clearable style="width: 100%">
          <el-option v-for="item in classes" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">提交</el-button>
        <el-button @click="resetForm('ruleForm')">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
export default {
  data() {
    return {
      ruleForm: {
        sid: null,
        studentNo: null,
        sname: null,
        password: null,
        gradeLevelId: null,
        departmentId: null,
        majorId: null,
        classId: null
      },
      gradeLevels: [],
      departments: [],
      majors: [],
      classes: [],
      rules: {
        studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        sname: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        password: [{ required: true, message: '请输入密码', trigger: 'change' }]
      }
    };
  },
  created() {
    const that = this
    const sid = this.$route.query.sid
    if (sid === undefined) {
      this.ruleForm.sid = 1
    } else {
      this.ruleForm.sid = sid
    }
    axios.get('/gradeLevel/findAll').then(function (resp) { that.gradeLevels = resp.data || [] })
    axios.get('/department/findAll').then(function (resp) { 
      that.departments = resp.data || []
    })
    axios.get('/student/findById/' + this.ruleForm.sid).then(function (resp) {
      that.ruleForm = { ...that.ruleForm, ...(resp.data || {}) }
      if (that.ruleForm.departmentId) {
        axios.get('/major/findByDepartmentId/' + that.ruleForm.departmentId).then(function (r) {
          that.majors = r.data || []
        })
      }
      if (that.ruleForm.majorId) {
        axios.get('/class/findByMajorId/' + that.ruleForm.majorId).then(function (r) {
          that.classes = r.data || []
        })
      }
    })
  },
  methods: {
    onDepartmentChange(departmentId) {
      this.ruleForm.majorId = null
      this.ruleForm.classId = null
      this.classes = []
      if (!departmentId) { this.majors = []; return }
      const that = this
      axios.get('/major/findByDepartmentId/' + departmentId).then(function (resp) {
        that.majors = resp.data || []
      })
    },
    onMajorChange(majorId) {
      this.ruleForm.classId = null
      if (!majorId) { this.classes = []; return }
      const that = this
      axios.get('/class/findByMajorId/' + majorId).then(function (resp) {
        that.classes = resp.data || []
      })
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const that = this
          axios.post("/student/updateStudent", this.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '编辑成功', type: 'success' });
              that.$router.push("/studentList")
            } else {
              that.$message.error('编辑失败，请检查数据库');
            }
          }).catch(function () {
            that.$message.error('编辑失败');
          })
        } else {
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  }
}
</script>