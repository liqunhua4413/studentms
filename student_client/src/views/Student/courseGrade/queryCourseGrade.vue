<template>
  <div>
    <el-form >
      <el-form-item label="选择学期">
        <el-select v-model="term" placeholder="请选择学期">
          <el-option label="所有学期" value="all"></el-option>
          <el-option v-for="(item, index) in termList" :key="index" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <el-card>
      <el-table
          :data="tableData"
          border
          style="width: 100%">
        <el-table-column
            prop="cname"
            label="课程名称"
            width="180">
        </el-table-column>
        <el-table-column
            prop="tname"
            label="教师名称"
            width="150">
          <template slot-scope="scope">
            {{ scope.row.teacherRealName || scope.row.tname }}
          </template>
        </el-table-column>
        <el-table-column
            prop="credit"
            label="学分"
            width="100">
          <template slot-scope="scope">
            {{ scope.row.credit || scope.row.ccredit }}
          </template>
        </el-table-column>
        <el-table-column
            prop="usualScore"
            label="平时成绩"
            width="100">
          <template slot-scope="scope">
            {{ formatScore(scope.row.usualScore || scope.row.usualGrade) }}
          </template>
        </el-table-column>
        <el-table-column
            prop="midScore"
            label="期中成绩"
            width="100">
          <template slot-scope="scope">
            {{ formatScore(scope.row.midScore) }}
          </template>
        </el-table-column>
        <el-table-column
            prop="finalScore"
            label="期末成绩"
            width="100">
          <template slot-scope="scope">
            {{ formatScore(scope.row.finalScore || scope.row.finalGrade) }}
          </template>
        </el-table-column>
        <el-table-column
            prop="grade"
            label="总成绩"
            width="120">
          <template slot-scope="scope">
            {{ formatScore(scope.row.grade || scope.row.totalGrade) }}
          </template>
        </el-table-column>
        <el-table-column
            prop="term"
            label="学期"
            width="150">
        </el-table-column>
      </el-table>
      <p>
        平均成绩：{{ avg.toFixed(2) }}
      </p>
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
    calculateAvg() {
        let totalScore = 0
        this.avg = 0
        const list = this.tmpList || []
        let count = 0
        for (let i = 0; i < list.length; i++) {
          const grade = list[i].grade || list[i].totalGrade
          const credit = list[i].credit || list[i].ccredit || 0
          if (grade != null && grade > 0) {
            totalScore += credit
            this.avg += credit * grade
            count++
          }
        }
        if (totalScore === 0)
          this.avg = 0
        else
          this.avg /= totalScore
    },
    formatScore(score) {
      if (score == null || score === undefined) return '-'
      return parseFloat(score).toFixed(2)
    }
  },
  data() {
    return {
      tableData: null,
      pageSize: 10,
      total: null,
      tmpList: null,
      avg: 0,
      term: sessionStorage.getItem('currentTerm'),
      termList: null
    }
  },
  created() {
    const that = this
    this.axios.get('/SCT/findAllTerm').then(function (resp) {
      that.termList = resp.data
    })
  },
  watch: {
    term: {
      handler(newTerm, oldTerm) {
        const sid = sessionStorage.getItem('sid')
        const that = this
        let url = '/SCT/findBySid/' + sid
        if (newTerm !== 'all') {
          url += '/' + newTerm
        }
        
        this.axios.get(url).then(function (resp) {
            that.tmpList = resp.data || []
            that.total = that.tmpList.length
            let start = 0, end = that.pageSize
            let length = that.tmpList.length
            let ans = (end < length) ? end : length
            that.tableData = that.tmpList.slice(start, ans)
            that.calculateAvg()
        })
      },
      immediate: true
    }
  }
}
</script>
