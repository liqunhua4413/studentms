<template>
  <div>
    <el-table
        :data="displayTableData"
        border
        stripe
        style="width: 100%">
      <el-table-column
          prop="cid"
          label="课程号"
          width="100">
      </el-table-column>
      <el-table-column
          prop="cname"
          label="课程名称"
          width="160"
          show-overflow-tooltip>
      </el-table-column>
      <el-table-column
          prop="ccredit"
          label="学分"
          width="80">
      </el-table-column>
      <el-table-column
          prop="category"
          label="课程类别"
          width="120"
          show-overflow-tooltip>
      </el-table-column>
      <el-table-column
          prop="examMethod"
          label="考核方式"
          width="120"
          show-overflow-tooltip>
      </el-table-column>
      <el-table-column
          prop="hours"
          label="学时"
          width="80">
      </el-table-column>
      <el-table-column
          prop="departmentName"
          label="所属学院"
          width="160">
      </el-table-column>
      <el-table-column
          label="操作"
          width="120">
        <template slot-scope="scope">
          <el-popconfirm
              confirm-button-text='删除'
              cancel-button-text='取消'
              icon="el-icon-info"
              icon-color="red"
              title="删除不可复原"
              @confirm="deleteCourse(scope.row)"
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
</template>

<script>
export default {
  data() {
    return {
      tableData: null,
      pageSize: 10,
      total: null,
      tmpList: null,
      type: sessionStorage.getItem("type"),
      departmentMap: {},
      departmentLoaded: false
    }
  },
  props: {
    ruleForm: Object,
    isActive: Boolean
  },
  computed: {
    displayTableData() {
      if (!this.tableData) return []
      const that = this
      return this.tableData.map(row => ({
        ...row,
        departmentName: that.departmentMap[row.departmentId] || '-'
      }))
    }
  },
  created() {
    const that = this
    // 先加载学院数据，完成后再触发课程查询
    axios.get('/department/findAll').then(function (resp) {
      const list = resp.data || []
      list.forEach(function (d) {
        that.departmentMap[d.id] = d.name
      })
      that.departmentLoaded = true
      // 学院加载完成后，如果有 ruleForm，触发查询
      if (that.ruleForm) {
        that.loadCourseData(that.ruleForm)
      }
    })
  },
  watch: {
    ruleForm: {
      handler(newRuleForm) {
        // 只有学院数据加载完成后才查询课程
        if (this.departmentLoaded) {
          this.loadCourseData(newRuleForm)
        }
      },
      deep: true
    }
  },
  methods: {
    loadCourseData(formData) {
      const that = this
      that.tmpList = null
      that.total = null
      that.tableData = null
      axios.post("/course/findBySearch", formData).then(function (resp) {
        that.tmpList = resp.data
        that.total = resp.data.length
        let start = 0, end = that.pageSize
        let length = that.tmpList.length
        let ans = (end < length) ? end : length
        that.tableData = that.tmpList.slice(start, ans)
      })
    },
    deleteCourse(row) {
      const that = this
      axios.get('/course/deleteById/' + row.cid).then(function (resp) {
        if (resp.data === true) {
          that.$message({
            showClose: true,
            message: '删除成功',
            type: 'success'
          });
          window.location.reload()
        } else {
          that.$message({
            showClose: true,
            message: '删除出错，请查询数据库连接',
            type: 'error'
          });
        }
      }).catch(function () {
        that.$message({
          showClose: true,
          message: '删除出错，存在外键依赖',
          type: 'error'
        });
      })
    },
    changePage(page) {
      page = page - 1
      const that = this
      let start = page * that.pageSize, end = that.pageSize * (page + 1)
      let length = that.tmpList.length
      let ans = (end < length) ? end : length
      that.tableData = that.tmpList.slice(start, ans)
    },
    editor(row) {
      this.$router.push({
        path: '/editorCourse',
        query: {
          cid: row.cid
        }
      })
    }
  }
}
</script>
