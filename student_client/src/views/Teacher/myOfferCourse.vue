<template>
  <div>
    <el-container>
      <el-main>
        <h1>我开设的课程</h1>
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
              stripe
              style="width: 100%">
            <el-table-column
                fixed
                prop="id"
                label="课程号"
                width="150">
            </el-table-column>
            <el-table-column
                prop="cname"
                label="课程名"
                width="200">
            </el-table-column>
            <el-table-column
                prop="ccredit"
                label="学分"
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
      if (that.term === 'all' || !that.term) {
        // 获取所有学期
        that.axios.get('/SCT/findAllTerm').then(function (termResp) {
          const allTerms = termResp.data || []
          let allCourses = []
          let promiseList = []
          
          // 为每个学期查询课程
          allTerms.forEach(t => {
            promiseList.push(
              that.axios.get('/courseTeacher/findMyCourse/' + that.tid + '/' + t).then(function (resp) {
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
        })
      } else {
        // 查询指定学期的课程
        that.axios.get('/courseTeacher/findMyCourse/' + that.tid + '/' + that.term).then(function (resp) {
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
      term: sessionStorage.getItem("currentTerm") || 'all',
      termList: []
    }
  },
  created() {
    this.tid = sessionStorage.getItem("tid")
    
    // 获取所有学期列表
    const that = this
    this.axios.get('/SCT/findAllTerm').then(function (resp) {
      that.termList = resp.data || []
      // 加载课程
      that.loadCourses()
    })
  }

}
</script>