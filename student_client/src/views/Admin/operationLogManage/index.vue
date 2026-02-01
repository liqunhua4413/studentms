<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="操作者" prop="operator">
            <el-input v-model="ruleForm.operator" placeholder="学号/工号"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="操作类型" prop="operationType">
            <el-select v-model="ruleForm.operationType" placeholder="请选择" clearable>
              <el-option label="新增" value="新增"></el-option>
              <el-option label="编辑" value="编辑"></el-option>
              <el-option label="删除" value="删除"></el-option>
              <el-option label="批量导入" value="批量导入"></el-option>
              <el-option label="上传" value="上传"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="模块" prop="targetTable">
            <el-select v-model="ruleForm.targetTable" placeholder="请选择模块" clearable>
              <el-option label="学生管理" value="student"></el-option>
              <el-option label="教师管理" value="teacher"></el-option>
              <el-option label="课程管理" value="course"></el-option>
              <el-option label="开课管理" value="course_open"></el-option>
              <el-option label="学院管理" value="department"></el-option>
              <el-option label="专业管理" value="major"></el-option>
              <el-option label="班级管理" value="class"></el-option>
              <el-option label="成绩管理" value="score"></el-option>
              <el-option label="试卷分析" value="exam_paper_analysis"></el-option>
              <el-option label="系统维护" value="all_tables"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
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
      <el-table-column prop="operator" label="操作者" width="150"></el-table-column>
      <el-table-column prop="operationType" label="操作类型" width="100"></el-table-column>
      <el-table-column prop="targetTable" label="模块" width="120">
        <template slot-scope="scope">
          {{ getModuleName(scope.row.targetTable) }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="操作内容" min-width="350" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="时间" width="180"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        operator: null,
        operationType: null,
        targetTable: null,
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
          const params = {}
          if (that.ruleForm.operator) params.operator = that.ruleForm.operator
          if (that.ruleForm.operationType) params.operationType = that.ruleForm.operationType
          if (that.ruleForm.targetTable) params.targetTable = that.ruleForm.targetTable
          if (that.ruleForm.startTime) params.startTime = that.ruleForm.startTime
          if (that.ruleForm.endTime) params.endTime = that.ruleForm.endTime
          axios.post('/operationLog/findBySearch', params).then(function (resp) {
            that.tableData = resp.data
            that.$message.success('查询成功，共 ' + resp.data.length + ' 条记录');
          })
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.loadData();
    },
    loadData() {
      const that = this
      axios.get('/operationLog/findAll').then(function (resp) {
        that.tableData = resp.data
      })
    },
    getModuleName(targetTable) {
      const moduleMap = {
        'student': '学生管理',
        'teacher': '教师管理',
        'course': '课程管理',
        'course_open': '开课管理',
        'department': '学院管理',
        'major': '专业管理',
        'class': '班级管理',
        'score': '成绩管理',
        'exam_paper_analysis': '试卷分析',
        'all_tables': '系统维护'
      }
      return moduleMap[targetTable] || targetTable || '—'
    }
  },
  created() {
    this.loadData();
  }
}
</script>
