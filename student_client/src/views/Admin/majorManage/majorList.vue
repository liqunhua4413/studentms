<template>
  <div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="100"></el-table-column>
      <el-table-column prop="name" label="专业名称" width="200"></el-table-column>
      <el-table-column prop="departmentId" label="学院名称" width="180" :formatter="fmtDepartmentName"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-popconfirm
              confirm-button-text='删除'
              cancel-button-text='取消'
              icon="el-icon-info"
              icon-color="red"
              title="删除不可复原"
              @confirm="deleteMajor(scope.row)">
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
        @current-change="changePage">
    </el-pagination>
  </div>
</template>

<script>
export default {
  methods: {
    deleteMajor(row) {
      const that = this
      axios.get('/major/deleteById/' + row.id).then(function (resp) {
        if (resp.data === true) {
          that.$message({ showClose: true, message: '删除成功', type: 'success' });
          if (that.tmpList === null) {
            window.location.reload()
          } else {
            that.$router.push('/queryMajor')
          }
        } else {
          that.$message({ showClose: true, message: '删除出错', type: 'error' });
        }
      }).catch(function (e) {
        that.$message({ showClose: true, message: '删除出错，存在外键依赖', type: 'error' });
      })
    },
    changePage(page) {
      page = page - 1
      if (this.tmpList === null) {
        const that = this
        axios.get('/major/findAll').then(function (resp) {
          let start = page * that.pageSize
          let end = start + that.pageSize
          that.tableData = resp.data.slice(start, end)
        })
      } else {
        let that = this
        let start = page * that.pageSize, end = that.pageSize * (page + 1)
        let length = that.tmpList.length
        let ans = end < length ? end : length
        that.tableData = that.tmpList.slice(start, ans)
      }
    },
    editor(row) {
      this.$router.push({ path: '/editorMajor', query: { id: row.id } })
    },
    fmtDepartmentName(row, column, cellValue) {
      return this.departmentMap[row.departmentId] != null ? this.departmentMap[row.departmentId] : (cellValue || '-')
    }
  },
  data() {
    return {
      tableData: null,
      pageSize: 10,
      total: null,
      ruleForm: null,
      tmpList: null,
      departmentMap: {}
    }
  },
  created() {
    if (this.tmpList !== null) this.tmpList = null
    const that = this
    this.ruleForm = this.$route.query.ruleForm
    axios.get('/department/findAll').then(function (resp) {
      const list = resp.data || []
      list.forEach(function (d) {
        that.departmentMap[d.id] = d.name
      })
    })
    if (this.$route.query.ruleForm === undefined || (this.ruleForm && this.ruleForm.name === null)) {
      axios.get('/major/findAll').then(function (resp) {
        that.total = resp.data.length
        let start = 0, end = that.pageSize
        that.tableData = resp.data.slice(start, end)
      })
    } else {
      axios.post('/major/findBySearch', this.ruleForm).then(function (resp) {
        that.tmpList = resp.data
        that.total = resp.data.length
        let start = 0, end = that.pageSize
        let length = that.tmpList.length
        let ans = end < length ? end : length
        that.tableData = that.tmpList.slice(start, ans)
      })
    }
  }
}
</script>
