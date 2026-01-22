<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="学院ID" prop="id">
        <el-input v-model="ruleForm.id" disabled></el-input>
      </el-form-item>
      <el-form-item label="学院名称" prop="name">
        <el-input v-model="ruleForm.name"></el-input>
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
        name: null
      },
      rules: {
        name: [
          { required: true, message: '请输入学院名称', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          axios.post('/department/update', that.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({
                showClose: true,
                message: '更新成功',
                type: 'success'
              });
              that.$router.push('/departmentList')
            } else {
              that.$message({
                showClose: true,
                message: '更新失败',
                type: 'error'
              });
            }
          })
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  },
  created() {
    const that = this
    const id = this.$route.query.id
    axios.get('/department/findById/' + id).then(function (resp) {
      that.ruleForm = resp.data
    })
  }
}
</script>
