<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="年级名称" prop="name">
        <el-input v-model="ruleForm.name" placeholder="如：2024级"></el-input>
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="ruleForm.sortOrder" :min="0" placeholder="数字越小越靠前"></el-input-number>
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
        sortOrder: 0
      },
      rules: {
        name: [
          { required: true, message: '请输入年级名称', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    submitForm(formName) {
      const that = this;
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.axios.post('/gradeLevel/add', that.ruleForm).then((resp) => {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '添加成功', type: 'success' });
              that.$router.push('/gradeLevelList');
            } else {
              that.$message({ showClose: true, message: resp.data && resp.data.message ? resp.data.message : '添加失败，年级名称可能已存在', type: 'error' });
            }
          }).catch((err) => {
            const msg = (err.response && err.response.data && err.response.data.message) || err.message || '添加失败';
            that.$message({ showClose: true, message: msg, type: 'error' });
          });
        } else {
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.sortOrder = 0;
    }
  }
};
</script>
