<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="学期ID" prop="id">
        <el-input v-model="ruleForm.id" disabled></el-input>
      </el-form-item>
      <el-form-item label="学期名称" prop="name">
        <el-input v-model="ruleForm.name"></el-input>
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
        <el-input v-model="ruleForm.remark" type="textarea"></el-input>
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
          this.axios.post('/term/update', that.ruleForm).then((resp) => {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '更新成功', type: 'success' });
              that.$router.push('/termList');
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
      this.$message.error('缺少学期ID');
      return;
    }
    this.axios.get('/term/findById/' + id).then((resp) => {
      const data = resp.data || {};
      that.ruleForm = {
        id: data.id,
        name: data.name,
        startDate: data.startDate,
        endDate: data.endDate,
        status: data.status != null ? data.status : 1,
        remark: data.remark
      };
    }).catch(() => {
      that.$message.error('加载学期信息失败');
    });
  }
};
</script>
