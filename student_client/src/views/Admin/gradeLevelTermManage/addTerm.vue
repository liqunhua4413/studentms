<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="学期名称" prop="name">
        <el-input v-model="ruleForm.name" placeholder="如：2025-2026学年第一学期"></el-input>
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="ruleForm.startDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择开始日期"
          style="width: 100%"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="ruleForm.endDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择结束日期"
          style="width: 100%"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="ruleForm.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="ruleForm.remark" type="textarea" placeholder="可选"></el-input>
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
        startDate: null,
        endDate: null,
        status: 1,
        remark: null
      },
      rules: {
        name: [
          { required: true, message: '请输入学期名称', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    submitForm(formName) {
      const that = this;
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.axios.post('/term/add', that.ruleForm).then((resp) => {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '添加成功', type: 'success' });
              that.$router.push('/termList');
            } else {
              that.$message({ showClose: true, message: resp.data && resp.data.message ? resp.data.message : '添加失败，学期名称可能已存在', type: 'error' });
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
      this.ruleForm.status = 1;
    }
  }
};
</script>
