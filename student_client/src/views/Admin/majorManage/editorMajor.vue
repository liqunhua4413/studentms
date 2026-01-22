<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="专业ID" prop="id">
        <el-input v-model="ruleForm.id" disabled></el-input>
      </el-form-item>
      <el-form-item label="专业名称" prop="name">
        <el-input v-model="ruleForm.name"></el-input>
      </el-form-item>
      <el-form-item label="所属学院" prop="departmentId">
        <el-select v-model="ruleForm.departmentId" placeholder="请选择学院">
          <el-option
              v-for="item in departments"
              :key="item.id"
              :label="item.name"
              :value="item.id">
          </el-option>
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
        name: null,
        departmentId: null
      },
      departments: [],
      rules: {
        name: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
        departmentId: [{ required: true, message: '请选择学院', trigger: 'change' }]
      }
    };
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          axios.post('/major/update', that.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '更新成功', type: 'success' });
              that.$router.push('/majorList')
            } else {
              that.$message({ showClose: true, message: '更新失败', type: 'error' });
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
    axios.get('/major/findById/' + id).then(function (resp) {
      that.ruleForm = resp.data
    })
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data
    })
  }
}
</script>
