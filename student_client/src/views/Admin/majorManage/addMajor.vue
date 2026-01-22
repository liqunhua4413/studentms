<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
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
        <el-button type="primary" @click="submitForm('ruleForm')">立即创建</el-button>
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
          axios.post('/major/add', that.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '添加成功', type: 'success' });
              that.$router.push('/majorList')
            } else {
              that.$message({ showClose: true, message: '添加失败', type: 'error' });
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
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data
    })
  }
}
</script>
