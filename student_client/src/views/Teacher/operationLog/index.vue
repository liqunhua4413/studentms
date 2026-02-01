<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="操作者" prop="operator">
            <el-input v-model="ruleForm.operator" placeholder="学号/工号" disabled></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="操作类型" prop="operationType">
            <el-select v-model="ruleForm.operationType" placeholder="请选择" clearable>
              <el-option label="新增" value="新增"></el-option>
              <el-option label="编辑" value="编辑"></el-option>
              <el-option label="删除" value="删除"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <el-button type="primary" @click="submitForm('ruleForm')">查询</el-button>
            <el-button @click="resetForm('ruleForm')">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
                v-model="ruleForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="yyyy-MM-dd HH:mm:ss">
            </el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
                v-model="ruleForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="yyyy-MM-dd HH:mm:ss">
            </el-date-picker>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="operator" label="学号/工号" width="180"></el-table-column>
      <el-table-column prop="operationType" label="操作类型" width="120"></el-table-column>
      <el-table-column prop="content" label="操作内容" min-width="400" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="时间" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    const userName = sessionStorage.getItem('name') || ''
    return {
      ruleForm: {
        operator: userName,
        operationType: null,
        startTime: null,
        endTime: null
      },
      tableData: [],
      rules: {}
    };
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const params = { operator: that.ruleForm.operator }
          if (that.ruleForm.operationType) params.operationType = that.ruleForm.operationType
          if (that.ruleForm.startTime) params.startTime = that.ruleForm.startTime
          if (that.ruleForm.endTime) params.endTime = that.ruleForm.endTime

          that.axios.post('/operationLog/findBySearch', params).then(function (resp) {
            that.tableData = resp.data
            that.$message.success('查询成功，共 ' + resp.data.length + ' 条记录');
          });
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.operator = sessionStorage.getItem('name') || '';
      this.ruleForm.operationType = null;
      this.ruleForm.startTime = null;
      this.ruleForm.endTime = null;
      this.loadData();
    },
    loadData() {
      const that = this
      that.axios.get('/operationLog/findAll').then(function (resp) {
        that.tableData = resp.data
      });
    }
  },
  created() {
    this.loadData();
  }
}
</script>
