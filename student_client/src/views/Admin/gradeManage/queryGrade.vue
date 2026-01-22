<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="学生姓名" prop="sname">
            <el-input v-model="ruleForm.sname" placeholder="支持模糊搜索"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="课程名" prop="cname">
            <el-input v-model="ruleForm.cname" placeholder="支持模糊搜索"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="学期" prop="term">
            <el-input v-model="ruleForm.term"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="学院" prop="departmentId">
            <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" clearable>
              <el-option
                  v-for="item in departments"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="专业" prop="majorId">
            <el-select v-model="ruleForm.majorId" placeholder="请选择专业" clearable>
              <el-option
                  v-for="item in majors"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="班级" prop="classId">
            <el-select v-model="ruleForm.classId" placeholder="请选择班级" clearable>
              <el-option
                  v-for="item in classes"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="最低分" prop="lowBound">
            <el-input-number v-model="ruleForm.lowBound" :min="0" :max="100"></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="最高分" prop="highBound">
            <el-input-number v-model="ruleForm.highBound" :min="0" :max="100"></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">查询</el-button>
        <el-button @click="resetForm('ruleForm')">重置</el-button>
        <el-button type="success" @click="exportReexamination">导出补考名单</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="sid" label="学号" width="100"></el-table-column>
      <el-table-column prop="sname" label="学生姓名" width="120"></el-table-column>
      <el-table-column prop="cname" label="课程名" width="150"></el-table-column>
      <el-table-column prop="tname" label="教师姓名" width="120"></el-table-column>
      <el-table-column prop="usualGrade" label="平时成绩" width="100"></el-table-column>
      <el-table-column prop="finalGrade" label="期末成绩" width="100"></el-table-column>
      <el-table-column prop="totalGrade" label="总成绩" width="100"></el-table-column>
      <el-table-column prop="term" label="学期" width="120"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        sname: null,
        cname: null,
        term: null,
        departmentId: null,
        majorId: null,
        classId: null,
        lowBound: null,
        highBound: null
      },
      departments: [],
      majors: [],
      classes: [],
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
          if (that.ruleForm.sname) params.sname = that.ruleForm.sname
          if (that.ruleForm.cname) params.cname = that.ruleForm.cname
          if (that.ruleForm.term) params.term = that.ruleForm.term
          if (that.ruleForm.departmentId) params.departmentId = that.ruleForm.departmentId
          if (that.ruleForm.majorId) params.majorId = that.ruleForm.majorId
          if (that.ruleForm.classId) params.classId = that.ruleForm.classId
          if (that.ruleForm.lowBound !== null) params.lowBound = that.ruleForm.lowBound
          if (that.ruleForm.highBound !== null) params.highBound = that.ruleForm.highBound
          params.sFuzzy = 'true'
          params.cFuzzy = 'true'

          axios.post('/grade/query', params).then(function (resp) {
            that.tableData = resp.data
            that.$message({
              message: '查询成功，共 ' + resp.data.length + ' 条记录',
              type: 'success'
            });
          })
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.tableData = [];
    },
    exportReexamination() {
      const that = this
      const params = {}
      if (that.ruleForm.departmentId) params.departmentId = that.ruleForm.departmentId
      if (that.ruleForm.majorId) params.majorId = that.ruleForm.majorId
      if (that.ruleForm.classId) params.classId = that.ruleForm.classId
      if (that.ruleForm.term) params.term = that.ruleForm.term

      axios.post('/grade/reexamination/export', params, {
        responseType: 'blob'
      }).then(function (resp) {
        const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '补考名单.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        that.$message({
          message: '导出成功！',
          type: 'success'
        });
      }).catch(function (error) {
        that.$message.error('导出失败！');
      })
    }
  },
  created() {
    const that = this
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data
    })
    axios.get('/major/findAll').then(function (resp) {
      that.majors = resp.data
    })
    axios.get('/class/findAll').then(function (resp) {
      that.classes = resp.data
    })
  }
}
</script>
