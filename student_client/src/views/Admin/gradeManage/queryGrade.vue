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
        <el-col :span="8">
          <el-form-item label="学期" prop="term">
            <el-select v-model="ruleForm.term" placeholder="请选择学期" clearable>
              <el-option
                  v-for="item in terms"
                  :key="item"
                  :label="item"
                  :value="item">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-form-item label="学院" prop="departmentId">
          <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" clearable @change="handleDepartmentChange">
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
      <el-table-column prop="usualGrade" label="平时成绩" width="100"></el-table-column>
      <el-table-column prop="finalGrade" label="期末成绩" width="100"></el-table-column>
      <el-table-column prop="totalGrade" label="总成绩" width="100">
        <template slot-scope="scope">
          {{ scope.row.totalGrade || scope.row.grade }}
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
          if (that.ruleForm.lowBound !== null && that.ruleForm.lowBound !== 0) params.lowBound = that.ruleForm.lowBound
          if (that.ruleForm.highBound !== null && that.ruleForm.highBound !== 0) params.highBound = that.ruleForm.highBound
          
          this.axios.post('/grade/query', params).then(resp => {
            this.tableData = resp.data
            this.$message.success('查询成功，共 ' + resp.data.length + ' 条记录');
          })
        }
      });
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
    exportReexamination() {
      const params = { ...this.ruleForm }
      // 导出当前查询结果，而不是固定的补考名单
      this.axios.post('/grade/query', params).then(resp => {
        const data = resp.data;
        if (!data || data.length === 0) {
          this.$message.warning('当前查询结果为空，无法导出');
          return;
        }
        // 调用后端通用的导出接口（使用当前查询到的数据列表）
        this.axios.post('/grade/reexamination/export', params, {
          responseType: 'blob'
        }).then(resp => {
          const url = window.URL.createObjectURL(new Blob([resp.data]))
          const link = document.createElement('a')
          link.href = url
          link.download = '成绩查询结果.xlsx'
          link.click()
        });
      });
    }
  },
  created() {
    console.log('QueryGrade initialized, fetching initial data...');
    // 获取学院列表
    this.axios.get('/department/findAll').then(resp => {
      this.departments = resp.data
    }).catch(err => console.error('加载学院失败:', err))
    
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
