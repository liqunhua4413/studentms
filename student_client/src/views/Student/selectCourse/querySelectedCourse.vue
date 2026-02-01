<template>
  <div>
    <el-card>
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="选择学期">
          <el-select v-model="termId" placeholder="请选择学期" @change="loadCourses">
            <el-option label="所有学期" :value="null"></el-option>
            <el-option v-for="item in termList" :key="item.id" :label="item.name" :value="item.id"></el-option>
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
      if (that.termId) {
        sessionStorage.setItem('currentTermId', String(that.termId))
        const t = that.termList.find(x => x.id === that.termId)
        if (t) sessionStorage.setItem('currentTerm', t.name)
      }
      
      if (!sid) {
        that.$message.error('未找到学生ID')
        return
      }
      
      // 如果选择"所有学期"，需要查询所有学期的课程
      if (!that.termId) {
        let allCourses = []
        let promiseList = []
        
        that.termList.forEach(t => {
          promiseList.push(
            that.axios.get('/SCT/findBySid/' + sid + '/' + t.id).then(function (resp) {
              const courses = resp.data || []
              courses.forEach(c => {
                c.term = t.name
                c.termId = t.id
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
        that.axios.get('/SCT/findBySid/' + sid + '/' + that.termId).then(function (resp) {
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
      termId: sessionStorage.getItem('currentTermId') ? parseInt(sessionStorage.getItem('currentTermId'), 10) : null,
      termList: []
    }
  },
  created() {
    const that = this
    // 获取所有学期列表
    that.axios.get('/term/findAll').then(function (termResp) {
      that.termList = termResp.data || []
      that.loadCourses()
    })
  },
}
</script>
