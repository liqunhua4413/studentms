<template>
  <div>
    <el-container>
      <el-main>
        <el-card>
          <el-form :inline="true" :model="ruleForm" ref="ruleForm" label-width="100px" class="demo-ruleForm">
            <el-form-item label="专业" prop="majorId">
              <el-select v-model="ruleForm.majorId" placeholder="请选择专业" clearable filterable style="width: 180px">
                <el-option v-for="item in majors" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="ruleForm.courseId" placeholder="请选择课程" clearable filterable style="width: 180px">
                <el-option v-for="item in courses" :key="item.id" :label="item.cname" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="方案版本" prop="planVersion">
              <el-input v-model="ruleForm.planVersion" placeholder="如 2025版" style="width: 120px"></el-input>
            </el-form-item>
            <el-form-item label="课程类型" prop="courseType">
              <el-select v-model="ruleForm.courseType" placeholder="请选择" clearable style="width: 120px">
                <el-option label="必修" value="REQUIRED"></el-option>
                <el-option label="限选" value="LIMITED"></el-option>
                <el-option label="任选" value="ELECTIVE"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="ruleForm.status" placeholder="请选择" clearable style="width: 100px">
                <el-option label="启用" :value="1"></el-option>
                <el-option label="停用" :value="0"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="resetForm('ruleForm')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card style="margin-top: 10px">
          <div>
            <el-table :data="tableData" border stripe style="width: 100%">
              <el-table-column prop="id" label="ID" width="70"></el-table-column>
              <el-table-column prop="majorName" label="专业" width="150" show-overflow-tooltip></el-table-column>
              <el-table-column prop="courseName" label="课程" width="150" show-overflow-tooltip></el-table-column>
              <el-table-column prop="planVersion" label="方案版本" width="100"></el-table-column>
              <el-table-column prop="courseType" label="课程类型" width="100" :formatter="fmtCourseType"></el-table-column>
              <el-table-column prop="suggestedGrade" label="建议年级" width="90"></el-table-column>
              <el-table-column prop="termName" label="建议学期" width="140" show-overflow-tooltip></el-table-column>
              <el-table-column prop="minCredit" label="最低学分" width="90"></el-table-column>
              <el-table-column prop="maxCredit" label="最高学分" width="90"></el-table-column>
              <el-table-column prop="maxCapacity" label="最大容量" width="90"></el-table-column>
              <el-table-column prop="status" label="状态" width="80" :formatter="fmtStatus"></el-table-column>
              <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip></el-table-column>
              <el-table-column label="操作" width="120">
                <template slot-scope="scope">
                  <el-popconfirm
                      confirm-button-text='删除'
                      cancel-button-text='取消'
                      icon="el-icon-info"
                      icon-color="red"
                      title="删除不可复原"
                      @confirm="deleteRow(scope.row)"
                  >
                    <el-button slot="reference" type="text" size="small">删除</el-button>
                  </el-popconfirm>
                  <el-button @click="editor(scope.row)" type="text" size="small">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="pageSize"
                @current-change="changePage"
            >
            </el-pagination>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        majorId: null,
        courseId: null,
        planVersion: null,
        courseType: null,
        status: null
      },
      majors: [],
      courses: [],
      tableData: [],
      pageSize: 10,
      total: 0,
      tmpList: []
    };
  },
  created() {
    const that = this
    axios.get('/major/findAll').then(resp => { that.majors = resp.data || [] })
    axios.post('/course/findBySearch', {}).then(resp => { that.courses = resp.data || [] })
    this.loadData()
  },
  watch: {
    ruleForm: {
      handler() {
        this.loadData()
      },
      deep: true
    }
  },
  methods: {
    loadData() {
      const that = this
      axios.post("/trainingPlan/findBySearch", this.ruleForm || {}).then(function (resp) {
        that.tmpList = resp.data || []
        that.total = that.tmpList.length
        that.tableData = that.tmpList.slice(0, that.pageSize)
      })
    },
    changePage(page) {
      page = page - 1
      let start = page * this.pageSize
      let end = Math.min(start + this.pageSize, this.tmpList.length)
      this.tableData = this.tmpList.slice(start, end)
    },
    deleteRow(row) {
      const that = this
      axios.get('/trainingPlan/deleteById/' + row.id).then(function (resp) {
        if (resp.data === true) {
          that.$message.success('删除成功')
          that.loadData()
        } else {
          that.$message.error('删除失败')
        }
      }).catch(function () {
        that.$message.error('删除失败')
      })
    },
    editor(row) {
      this.$router.push({ path: '/editorTrainingPlan', query: { id: row.id } })
    },
    fmtCourseType(row) {
      const map = { 'REQUIRED': '必修', 'LIMITED': '限选', 'ELECTIVE': '任选' }
      return map[row.courseType] || row.courseType
    },
    fmtStatus(row) {
      return row.status === 1 ? '启用' : '停用'
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  }
}
</script>
