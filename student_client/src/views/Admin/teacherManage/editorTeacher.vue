<template>
  <div>
    <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="教师工号" prop="teacherNo">
        <el-input v-model="ruleForm.teacherNo" placeholder="教师工号"></el-input>
      </el-form-item>
      <el-form-item label="教师姓名" prop="tname">
        <el-input v-model="ruleForm.tname"></el-input>
      </el-form-item>
      <el-form-item label="初始密码" prop="password">
        <el-input v-model="ruleForm.password" show-password></el-input>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="ruleForm.role" placeholder="请选择角色" style="width: 100%">
          <el-option label="教师" value="teacher"></el-option>
          <el-option label="管理员" value="admin"></el-option>
          <el-option label="院长" value="dean"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属学院" prop="departmentId">
        <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" style="width: 100%">
          <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id"></el-option>
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
        tid: null,
        teacherNo: null,
        tname: null,
        password: null,
        role: null,
        departmentId: null
      },
      departments: [],
      rules: {
        teacherNo: [{ required: true, message: '请输入教师工号', trigger: 'blur' }],
        tname: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        password: [{ required: true, message: '请输入密码', trigger: 'change' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        departmentId: [{ required: true, message: '请选择所属学院', trigger: 'change' }]
      }
    };
  },
  created() {
    const that = this
    const tid = this.$route.query.tid
    if (tid === undefined) {
      this.ruleForm.tid = 6
    } else {
      this.ruleForm.tid = tid
    }
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data || []
    })
    axios.get('/teacher/findById/' + this.ruleForm.tid).then(function (resp) {
      that.ruleForm = { ...that.ruleForm, ...(resp.data || {}) }
    })
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const that = this
          if (that.ruleForm.tname === 'admin') {
            that.$message({ showClose: true, message: 'admin 不可编辑', type: 'error' });
            this.$router.push('/queryTeacher')
            return
          }
          axios.post("/teacher/updateTeacher", this.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '编辑成功', type: 'success' });
            } else {
              that.$message.error('编辑失败，请检查数据库');
            }
            that.$router.push("/queryTeacher")
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