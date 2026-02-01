<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="年级ID" prop="id">
        <el-input v-model="ruleForm.id" disabled></el-input>
      </el-form-item>
      <el-form-item label="年级名称" prop="name">
        <el-input v-model="ruleForm.name"></el-input>
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="ruleForm.sortOrder" :min="0"></el-input-number>
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
          this.axios.post('/gradeLevel/update', that.ruleForm).then((resp) => {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '更新成功', type: 'success' });
              that.$router.push('/gradeLevelList');
            } else {
              that.$message({ showClose: true, message: '更新失败', type: 'error' });
            }
          }).catch(() => {
            that.$message({ showClose: true, message: '更新失败', type: 'error' });
          });
        } else {
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  },
  created() {
    const that = this;
    const id = this.$route.query.id;
    if (!id) {
      this.$message.error('缺少年级ID');
      return;
    }
    this.axios.get('/gradeLevel/findById/' + id).then((resp) => {
      that.ruleForm = resp.data || {};
      if (that.ruleForm.sortOrder == null) that.ruleForm.sortOrder = 0;
    }).catch(() => {
      that.$message.error('加载年级信息失败');
    });
  }
};
</script>
