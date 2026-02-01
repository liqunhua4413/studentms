<template>
  <div>
    <el-container>
      <el-main>
        <h1>我开设的课程</h1>
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
              stripe
              style="width: 100%">
            <el-table-column
                fixed
                prop="id"
                label="课程号"
                width="100">
            </el-table-column>
            <el-table-column
                prop="cname"
                label="课程名"
                width="180">
            </el-table-column>
            <el-table-column
                prop="ccredit"
                label="学分"
                width="80">
            </el-table-column>
            <el-table-column
                prop="category"
                label="课程类别"
                width="120">
            </el-table-column>
            <el-table-column
                prop="courseType"
                label="课程性质"
                width="120"
                :formatter="fmtCourseType">
            </el-table-column>
            <el-table-column
                prop="examMethod"
                label="考核方式"
                width="120">
            </el-table-column>
            <el-table-column
                prop="hours"
                label="学时"
                width="80">
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
      </el-main>
    </el-container>
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
    fmtCourseType(row, column, cellValue) {
      const t = (row.courseType || cellValue || '').toUpperCase()
      if (t === 'REQUIRED') return '必修'
      if (t === 'LIMITED') return '限选'
      if (t === 'ELECTIVE') return '任选'
      return '-'
    },
    loadCourses() {
      const that = this
      if (!that.tid) {
        that.tid = sessionStorage.getItem("tid")
      }
      if (!that.tid) {
        that.$message.error('未找到教师ID')
        return
      }
      
      // 如果选择"所有学期"，需要查询所有学期的课程
      if (!that.termId) {
        that.axios.get('/term/findAll').then(function (termResp) {
          const allTerms = termResp.data || []
          let allCourses = []
          let promiseList = []
          
          allTerms.forEach(t => {
            promiseList.push(
              that.axios.get('/courseTeacher/findMyCourse/' + that.tid + '/' + t.id).then(function (resp) {
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
        })
      } else {
        sessionStorage.setItem('currentTermId', String(that.termId))
        const t = that.termList.find(x => x.id === that.termId)
        if (t) sessionStorage.setItem('currentTerm', t.name)
        // 查询指定学期的课程
        that.axios.get('/courseTeacher/findMyCourse/' + that.tid + '/' + that.termId).then(function (resp) {
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
      tid: null,
      termId: sessionStorage.getItem("currentTermId") ? parseInt(sessionStorage.getItem("currentTermId"), 10) : null,
      termList: []
    }
  },
  created() {
    this.tid = sessionStorage.getItem("tid")
    const that = this
    // 先加载学期列表（保证下拉有选项），再加载课程
    that.axios.get('/term/findAll').then(function (termResp) {
      const list = termResp.data
      that.termList = Array.isArray(list) ? list : []
      that.loadCourses()
    })
  }

}
</script>