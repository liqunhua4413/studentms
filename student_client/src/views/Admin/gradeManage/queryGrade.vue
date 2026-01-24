<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="ruleForm.studentNo" placeholder="输入学号查询" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="学生姓名" prop="sname">
            <el-input v-model="ruleForm.sname" placeholder="支持模糊搜索" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="课程名" prop="cname">
            <el-select v-model="ruleForm.cname" placeholder="请选择课程" clearable filterable>
              <el-option
                  v-for="item in courses"
                  :key="item.id"
                  :label="item.cname"
                  :value="item.cname">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="学期" prop="term">
            <el-select v-model="ruleForm.term" placeholder="请选择学期" clearable>
              <el-option label="所有学期" value="all"></el-option>
              <el-option
                  v-for="item in terms"
                  :key="item"
                  :label="item"
                  :value="item">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="学院" prop="departmentId">
            <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" :disabled="userType === 'dean'" clearable @change="handleDepartmentChange">
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
            <el-select v-model="ruleForm.majorId" placeholder="请选择专业" clearable @change="handleMajorChange">
              <el-option
                  v-for="item in majors"
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
        <el-col :span="8">
          <el-form-item label="最低分" prop="lowBound">
            <el-input-number v-model="ruleForm.lowBound" :min="0" :max="100" :precision="2" :step="0.01" placeholder="不填则不限制"></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="最高分" prop="highBound">
            <el-input-number v-model="ruleForm.highBound" :min="0" :max="100" :precision="2" :step="0.01" placeholder="不填则不限制"></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">查询</el-button>
        <el-button @click="resetForm('ruleForm')">重置</el-button>
        <el-button type="success" @click="exportGradeList">导出名单</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="studentNo" label="学号" width="120">
        <template slot-scope="scope">
          {{ scope.row.studentNo || scope.row.sid }}
        </template>
      </el-table-column>
      <el-table-column prop="sname" label="学生姓名" width="120"></el-table-column>
      <el-table-column prop="departmentName" label="学院" width="150">
        <template slot-scope="scope">
          {{ scope.row.departmentName || scope.row.departmentId }}
        </template>
      </el-table-column>
      <el-table-column prop="majorName" label="专业" width="150">
        <template slot-scope="scope">
          {{ scope.row.majorName || scope.row.majorId }}
        </template>
      </el-table-column>
      <el-table-column prop="className" label="班级" width="120">
        <template slot-scope="scope">
          {{ scope.row.className || scope.row.classId }}
        </template>
      </el-table-column>
      <el-table-column prop="cname" label="课程名" width="150"></el-table-column>
      <el-table-column prop="tname" label="教师姓名" width="120">
        <template slot-scope="scope">
          {{ scope.row.teacherRealName || scope.row.tname }}
        </template>
      </el-table-column>
      <el-table-column prop="usualScore" label="平时成绩" width="100">
        <template slot-scope="scope">
          {{ formatScore(scope.row.usualScore || scope.row.usualGrade) }}
        </template>
      </el-table-column>
      <el-table-column prop="midScore" label="期中成绩" width="100">
        <template slot-scope="scope">
          {{ formatScore(scope.row.midScore) }}
        </template>
      </el-table-column>
      <el-table-column prop="finalScore" label="期末成绩" width="100">
        <template slot-scope="scope">
          {{ formatScore(scope.row.finalScore || scope.row.finalGrade) }}
        </template>
      </el-table-column>
      <el-table-column prop="grade" label="总成绩" width="100">
        <template slot-scope="scope">
          {{ formatScore(scope.row.grade || scope.row.totalGrade) }}
        </template>
      </el-table-column>
      <el-table-column prop="term" label="学期" width="120"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        studentNo: null,
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
      courses: [],
      terms: [],
      tableData: [],
      rules: {},
      userType: sessionStorage.getItem('type') || 'admin'
    };
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 如果输入了学号，先根据学号查找学生ID
          if (that.ruleForm.studentNo) {
            this.axios.post('/student/findByStudentNo', { studentNo: that.ruleForm.studentNo }).then(studentResp => {
              if (studentResp.data && studentResp.data.id) {
                that.executeQuery({ sid: studentResp.data.id })
              } else {
                that.$message.warning('未找到学号为【' + that.ruleForm.studentNo + '】的学生')
              }
            }).catch(err => {
              that.$message.error('查询学生失败：' + err.message)
            })
          } else {
            // 没有输入学号，直接查询
            that.executeQuery({})
          }
        }
      });
    },
    executeQuery(baseParams) {
      const params = { ...baseParams }
      // 处理学期参数：如果选择"所有学期"，则不传term参数
      if (this.ruleForm.term && this.ruleForm.term !== 'all') {
        params.term = this.ruleForm.term
      }
      // 其他查询条件
      if (this.ruleForm.sname) params.sname = this.ruleForm.sname
      if (this.ruleForm.cname) params.cname = this.ruleForm.cname
      if (this.ruleForm.departmentId) params.departmentId = this.ruleForm.departmentId
      if (this.ruleForm.majorId) params.majorId = this.ruleForm.majorId
      if (this.ruleForm.classId) params.classId = this.ruleForm.classId
      // 修复分数查询逻辑：只有当用户明确输入了分数（且大于0）时才传递参数
      if (this.ruleForm.lowBound !== null && this.ruleForm.lowBound !== undefined && this.ruleForm.lowBound > 0) {
        params.lowBound = this.ruleForm.lowBound
      }
      if (this.ruleForm.highBound !== null && this.ruleForm.highBound !== undefined && this.ruleForm.highBound > 0) {
        params.highBound = this.ruleForm.highBound
      }
      
      this.axios.post('/grade/query', params).then(resp => {
        this.tableData = resp.data
        this.$message.success('查询成功，共 ' + resp.data.length + ' 条记录');
      }).catch(err => {
        this.$message.error('查询失败：' + err.message)
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.tableData = [];
      this.majors = [];
      this.classes = [];
    },
    handleDepartmentChange(val) {
      this.ruleForm.majorId = null;
      this.ruleForm.classId = null;
      this.majors = [];
      this.classes = [];
      if (val) {
        this.axios.get(`/major/findByDepartmentId/${val}`).then(resp => {
          this.majors = resp.data;
        }).catch(err => {
          console.error('加载专业失败:', err);
          this.$message.error('加载专业列表失败');
        });
      }
    },
    handleMajorChange(val) {
      this.ruleForm.classId = null;
      this.classes = [];
      if (val) {
        this.axios.get(`/class/findByMajorId/${val}`).then(resp => {
          this.classes = resp.data;
        }).catch(err => {
          console.error('加载班级失败:', err);
          this.$message.error('加载班级列表失败');
        });
      }
    },
    exportGradeList() {
      // 如果当前没有查询结果，先执行查询
      if (!this.tableData || this.tableData.length === 0) {
        this.$message.warning('请先执行查询，然后再导出');
        return;
      }
      
      // 构建查询参数（与当前查询条件一致）
      const params = {}
      if (this.ruleForm.studentNo) {
        // 如果有学号，需要先查找学生ID
        this.axios.post('/student/findByStudentNo', { studentNo: this.ruleForm.studentNo }).then(studentResp => {
          if (studentResp.data && studentResp.data.id) {
            params.sid = studentResp.data.id
            this.executeExport(params)
          } else {
            this.$message.warning('未找到学号为【' + this.ruleForm.studentNo + '】的学生')
          }
        }).catch(err => {
          this.$message.error('查询学生失败：' + err.message)
        })
      } else {
        // 没有学号，直接导出
        if (this.ruleForm.term && this.ruleForm.term !== 'all') params.term = this.ruleForm.term
        if (this.ruleForm.sname) params.sname = this.ruleForm.sname
        if (this.ruleForm.cname) params.cname = this.ruleForm.cname
        if (this.ruleForm.departmentId) params.departmentId = this.ruleForm.departmentId
        if (this.ruleForm.majorId) params.majorId = this.ruleForm.majorId
        if (this.ruleForm.classId) params.classId = this.ruleForm.classId
        if (this.ruleForm.lowBound !== null && this.ruleForm.lowBound !== undefined && this.ruleForm.lowBound > 0) params.lowBound = this.ruleForm.lowBound
        if (this.ruleForm.highBound !== null && this.ruleForm.highBound !== undefined && this.ruleForm.highBound > 0) params.highBound = this.ruleForm.highBound
        this.executeExport(params)
      }
    },
    executeExport(params) {
      // 调用后端导出接口（根据查询条件导出）
      this.axios.post('/grade/reexamination/export', params, {
        responseType: 'blob'
      }).then(resp => {
        const url = window.URL.createObjectURL(new Blob([resp.data]))
        const link = document.createElement('a')
        link.href = url
        link.download = '成绩查询结果.xlsx'
        link.click()
        this.$message.success('导出成功');
      }).catch(err => {
        this.$message.error('导出失败：' + err.message)
      });
    },
    formatScore(score) {
      if (score === null || score === undefined || score === '') {
        return ''
      }
      const num = parseFloat(score)
      if (isNaN(num)) {
        return score
      }
      return num.toFixed(2)
    }
  },
  created() {
    console.log('QueryGrade initialized, fetching initial data...');
    const userType = sessionStorage.getItem('type') || 'admin'
    const departmentId = sessionStorage.getItem('departmentId')
    
    // 如果是院长，自动设置并锁定学院
    if (userType === 'dean' && departmentId) {
      this.ruleForm.departmentId = parseInt(departmentId)
      // 获取学院名称
      this.axios.get(`/department/findById/${departmentId}`).then(resp => {
        if (resp.data) {
          this.departments = [resp.data] // 只显示自己的学院
        }
      }).catch(err => console.error('加载学院失败:', err))
    } else {
      // 管理员可以查看所有学院
      this.axios.get('/department/findAll').then(resp => {
        this.departments = resp.data
      }).catch(err => console.error('加载学院失败:', err))
    }
    
    // 获取课程列表
    this.axios.get('/course/findAll').then(resp => {
      this.courses = resp.data
    }).catch(err => console.error('加载课程失败:', err))
    
    // 获取学期列表
    this.axios.get('/SCT/findAllTerm').then(resp => {
      this.terms = resp.data || [];
    }).catch(err => console.error('加载学期失败:', err))
  }
}
</script>
