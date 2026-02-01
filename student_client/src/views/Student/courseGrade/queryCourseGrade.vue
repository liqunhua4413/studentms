<template>
  <div>
    <el-form :inline="true">
      <el-form-item label="选择学期">
        <el-select v-model="termId" placeholder="请选择学期" clearable @change="fetch">
          <el-option label="所有学期" :value="null" />
          <el-option v-for="item in termList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetch">查询</el-button>
      </el-form-item>
    </el-form>
    <el-card v-loading="loading">
      <el-table :data="paginatedList" border style="width: 100%;">
        <el-table-column prop="cname" label="课程名称" width="180" />
        <el-table-column prop="tname" label="教师" width="120">
          <template slot-scope="scope">{{ scope.row.teacherRealName || scope.row.tname || '—' }}</template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="90">
          <template slot-scope="scope">{{ formatScore(scope.row.credit) }}</template>
        </el-table-column>
        <el-table-column prop="usualScore" label="平时成绩" width="100">
          <template slot-scope="scope">{{ formatScore(scope.row.usualScore) }}</template>
        </el-table-column>
        <el-table-column prop="midScore" label="期中成绩" width="100">
          <template slot-scope="scope">{{ formatScore(scope.row.midScore) }}</template>
        </el-table-column>
        <el-table-column prop="finalScore" label="期末成绩" width="100">
          <template slot-scope="scope">{{ formatScore(scope.row.finalScore) }}</template>
        </el-table-column>
        <el-table-column prop="grade" label="总成绩" width="100">
          <template slot-scope="scope">{{ formatScore(scope.row.grade) }}</template>
        </el-table-column>
        <el-table-column prop="term" label="学期" width="120" />
      </el-table>
      <p style="margin-top: 16px;">平均成绩：{{ formatScore(avg) }}</p>
      <el-pagination
        :current-page="page"
        :page-size="pageSize"
        :total="list.length"
        layout="prev, pager, next"
        style="margin-top: 12px;"
        @current-change="page = $event"
      />
    </el-card>
  </div>
</template>

<script>
import { formatScore } from '@/utils/gradeFormat'

export default {
  name: 'QueryCourseGrade',
  data () {
    const tid = sessionStorage.getItem('currentTermId')
    return {
      termId: tid ? parseInt(tid, 10) : null,
      termList: [],
      list: [],
      page: 1,
      pageSize: 10,
      loading: false
    }
  },
  computed: {
    paginatedList () {
      const start = (this.page - 1) * this.pageSize
      return (this.list || []).slice(start, start + this.pageSize)
    },
    avg () {
      const L = this.list || []
      let sum = 0
      let cnt = 0
      for (const row of L) {
        const g = row.grade != null ? Number(row.grade) : null
        const c = row.credit != null ? Number(row.credit) : 0
        if (g != null && !isNaN(g) && c > 0) {
          sum += g * c
          cnt += c
        }
      }
      return cnt > 0 ? sum / cnt : 0
    }
  },
  created () {
    this.axios.get('/term/findAll').then(r => {
      this.termList = r.data || []
    }).catch(() => {})
    this.fetch()
  },
  methods: {
    formatScore,
    fetch () {
      const sid = sessionStorage.getItem('sid')
      if (this.termId) {
        sessionStorage.setItem('currentTermId', String(this.termId))
        const t = (this.termList || []).find(x => x.id === this.termId)
        if (t) sessionStorage.setItem('currentTerm', t.name)
      }
      if (!sid) {
        this.$message.warning('请先登录')
        return
      }
      this.loading = true
      const params = { sid }
      if (this.termId) params.termId = this.termId
      this.axios.get('/grade/query/student', { params }).then(r => {
        this.list = r.data || []
        this.page = 1
      }).catch(() => {
        this.$message.error('查询失败')
        this.list = []
      }).finally(() => { this.loading = false })
    }
  }
}
</script>
