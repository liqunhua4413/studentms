<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="课程" prop="courseId">
        <el-select v-model="ruleForm.courseId" placeholder="请选择课程" filterable style="width: 100%">
          <el-option v-for="item in courses" :key="item.id" :label="item.cname + ' (' + item.cid + ')'" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="教师" prop="teacherId">
        <el-select v-model="ruleForm.teacherId" placeholder="请选择教师" filterable style="width: 100%">
          <el-option v-for="item in teachers" :key="item.id" :label="item.tname + ' (' + item.tid + ')'" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="学期" prop="termId">
        <el-select v-model="ruleForm.termId" placeholder="请选择学期" style="width: 100%">
          <el-option v-for="item in terms" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">立即更新</el-button>
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
        id: null,
        courseId: null,
        teacherId: null,
        termId: null
      },
      courses: [],
      teachers: [],
      terms: [],
      rules: {
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
        termId: [{ required: true, message: '请选择学期', trigger: 'change' }]
      }
    };
  },
  created() {
    const that = this
    const id = this.$route.query.id
    if (!id) {
      this.$message.error('缺少开课ID')
      return
    }
    this.ruleForm.id = parseInt(id)
    
    axios.post('/course/findBySearch', {}).then(function (resp) {
      that.courses = resp.data || []
    })
    axios.get('/teacher/findAll').then(function (resp) {
      that.teachers = resp.data || []
    })
    axios.get('/term/findAll').then(function (resp) {
      that.terms = resp.data || []
    })
    
    axios.get('/courseTeacher/findById/' + id).then(function (resp) {
      const data = resp.data || {}
      that.ruleForm.courseId = data.courseId
      that.ruleForm.teacherId = data.teacherId
      that.ruleForm.termId = data.termId
    })
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (!that.ruleForm.id) {
            that.$message.error('未找到开课记录ID')
            return
          }
          axios.post('/courseTeacher/update', that.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '更新成功', type: 'success' });
              that.$router.push('/queryCourseTeacher')
            } else {
              that.$message({ showClose: true, message: '更新失败', type: 'error' });
            }
          }).catch(function () {
            that.$message.error('更新失败');
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
};
</script>
