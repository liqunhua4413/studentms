<template>
  <div>
    <el-card>
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="选择学期">
          <el-select v-model="term" placeholder="请选择学期" @change="loadCourses">
            <el-option label="所有学期" value="all"></el-option>
            <el-option v-for="(item, index) in termList" :key="index" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <el-table
          :data="tableData"
          border
          style="width: 100%">
        <el-table-column
            fixed
            prop="cid"
            label="课号"
            width="150">
        </el-table-column>
        <el-table-column
            prop="cname"
            label="课程名"
            width="150">
        </el-table-column>
        <el-table-column
            prop="tid"
            label="教师号"
            width="150">
        </el-table-column>
        <el-table-column
            prop="tname"
            label="教师名称"
            width="150">
        </el-table-column>
        <el-table-column
            prop="ccredit"
            label="学分"
            width="150">
        </el-table-column>
        <el-table-column
            prop="term"
            label="学期"
            width="150">
        </el-table-column>
        <el-table-column
            label="操作"
            width="100">

          <template slot-scope="scope">
            <el-popconfirm
                confirm-button-text='退课'
                cancel-button-text='取消'
                icon="el-icon-info"
                title="确定退课？"
                @confirm="deleteSCT(scope.row)"
            >
              <el-button slot="reference" type="text" size="small">退课</el-button>
            </el-popconfirm>
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
    </el-card>
  </div>
</template>

<script>
export default {
  methods: {
    deleteSCT(row) {
      const cid = row.cid
      const tid = row.tid
      const sid = sessionStorage.getItem('sid')
      const term = row.term || sessionStorage.getItem('currentTerm')
      const sct = {
        cid: cid,
        tid: tid,
        sid: sid,
        term: term
      }

      const that = this
      axios.post('/SCT/deleteBySCT', sct).then(function (resp) {
        if (resp.data === true) {
          that.$message({
            showClose: true,
            message: '退课成功',
            type: 'success'
          });
          that.loadCourses()
        }
        else {
          that.$message({
            showClose: true,
            message: '退课失败，请联系管理员',
            type: 'error'
          });
        }
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
    loadCourses() {
      const sid = sessionStorage.getItem('sid')
      const that = this
      
      if (!sid) {
        that.$message.error('未找到学生ID')
        return
      }
      
      // 如果选择"所有学期"，需要查询所有学期的课程
      if (that.term === 'all' || !that.term) {
        // 获取所有学期
        let allCourses = []
        let promiseList = []
        
        // 为每个学期查询课程
        that.termList.forEach(t => {
          promiseList.push(
            that.axios.get('/SCT/findBySid/' + sid + '/' + t).then(function (resp) {
              const courses = resp.data || []
              // 为每个课程添加学期信息
              courses.forEach(c => {
                c.term = t
                allCourses.push(c)
              })
            })
          )
        })
        
        // 等待所有查询完成
        Promise.all(promiseList).then(() => {
          that.tmpList = allCourses
          that.total = allCourses.length
          let start = 0, end = that.pageSize
          let length = that.tmpList.length
          let ans = (end < length) ? end : length
          that.tableData = that.tmpList.slice(start, ans)
        })
      } else {
        // 查询指定学期的课程
        axios.get('/SCT/findBySid/' + sid + '/' + that.term).then(function (resp) {
          that.tmpList = resp.data || []
          that.total = that.tmpList.length
          let start = 0, end = that.pageSize
          let length = that.tmpList.length
          let ans = (end < length) ? end : length
          that.tableData = that.tmpList.slice(start, ans)
        })
      }
    }
  },
  data() {
    return {
      tableData: [],
      pageSize: 10,
      total: 0,
      tmpList: [],
      type: sessionStorage.getItem('type'),
      term: sessionStorage.getItem('currentTerm') || 'all',
      termList: []
    }
  },
  created() {
    const that = this
    // 获取所有学期列表
    this.axios.get('/SCT/findAllTerm').then(function (termResp) {
      that.termList = termResp.data || []
      that.loadCourses()
    })
  },
}
</script>
