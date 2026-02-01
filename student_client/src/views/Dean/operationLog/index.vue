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
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="ruleForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="operator" label="操作者" width="150" />
      <el-table-column prop="operationType" label="操作类型" width="100" />
      <el-table-column prop="targetTable" label="模块" width="120">
        <template slot-scope="scope">
          {{ getModuleName(scope.row.targetTable) }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="操作内容" min-width="350" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>
  </div>
</template>

<script>
export default {
  data () {
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
    }
  },
  methods: {
    submitForm (formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid) return
        const params = {}
        if (this.ruleForm.operator) params.operator = this.ruleForm.operator
        if (this.ruleForm.operationType) params.operationType = this.ruleForm.operationType
        if (this.ruleForm.targetTable) params.targetTable = this.ruleForm.targetTable
        if (this.ruleForm.startTime) params.startTime = this.ruleForm.startTime
        if (this.ruleForm.endTime) params.endTime = this.ruleForm.endTime
        this.axios.post('/operationLog/findBySearch', params).then((resp) => {
          this.tableData = resp.data || []
          this.$message.success('查询成功，共 ' + this.tableData.length + ' 条记录')
        }).catch(() => {
          this.$message.error('查询失败')
        })
      })
    },
    resetForm (formName) {
      this.$refs[formName].resetFields()
      this.loadData()
    },
    loadData () {
      this.axios.get('/operationLog/findAll').then((resp) => {
        this.tableData = resp.data || []
        if (this.tableData.length === 0) {
          this.$message.info('暂无操作日志')
        }
      }).catch((err) => {
        this.$message.error('加载操作日志失败')
        console.error(err)
      })
    },
    getModuleName (targetTable) {
      const map = {
        student: '学生管理',
        teacher: '教师管理',
        course: '课程管理',
        course_open: '开课管理',
        department: '学院管理',
        major: '专业管理',
        class: '班级管理',
        score: '成绩管理',
        exam_paper_analysis: '试卷分析',
        all_tables: '系统维护'
      }
      return map[targetTable] || targetTable || '—'
    }
  },
  created () {
    this.loadData()
  }
}
</script>
