<template>
  <div>
    <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="教师工号" prop="teacherNo">
        <el-input v-model="ruleForm.teacherNo" placeholder="如：T001"></el-input>
      </el-form-item>
      <el-form-item label="教师姓名" prop="tname">
        <el-input v-model="ruleForm.tname"></el-input>
      </el-form-item>
      <el-form-item label="初始密码" prop="password">
        <el-input v-model="ruleForm.password" show-password placeholder="登录密码"></el-input>
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
        teacherNo: '',
        tname: '',
        password: '',
        role: 'teacher',
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
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data || []
    })
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const that = this
          if (that.ruleForm.tname === 'admin') {
            that.$message({ showClose: true, message: 'admin 不可添加', type: 'error' });
            return
          }
          axios.post("/teacher/addTeacher", this.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '插入成功', type: 'success' });
              that.$router.push("/queryTeacher")
            } else {
              that.$message.error('插入失败，请检查工号是否重复或数据库');
            }
          }).catch(function () {
            that.$message.error('插入失败');
          })
        } else {
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.role = 'teacher'
    }
  }
}
</script>