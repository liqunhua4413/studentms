<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="班级ID" prop="id">
        <el-input v-model="ruleForm.id" disabled></el-input>
      </el-form-item>
      <el-form-item label="班级名称" prop="name">
        <el-input v-model="ruleForm.name"></el-input>
      </el-form-item>
      <el-form-item label="所属专业" prop="majorId">
        <el-select v-model="ruleForm.majorId" placeholder="请选择专业" @change="onMajorChange">
          <el-option
              v-for="item in majors"
              :key="item.id"
              :label="item.name"
              :value="item.id">
          </el-option>
        </el-select>
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
        majorId: null,
        departmentId: null
      },
      majors: [],
      departments: [],
      rules: {
        name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
        majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
        departmentId: [{ required: true, message: '请选择学院', trigger: 'change' }]
      }
    };
  },
  methods: {
    onMajorChange(majorId) {
      const that = this
      if (majorId) {
        axios.get('/major/findById/' + majorId).then(function (resp) {
          that.ruleForm.departmentId = resp.data.departmentId
        })
      }
    },
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          axios.post('/class/update', that.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '更新成功', type: 'success' });
              that.$router.push('/classList')
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
    axios.get('/class/findById/' + id).then(function (resp) {
      that.ruleForm = resp.data
    })
    axios.get('/major/findAll').then(function (resp) {
      that.majors = resp.data
    })
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data
    })
  }
}
</script>
