<template>
  <div style="padding: 20px;">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成绩审计日志</span>
        <span class="tips">（grade_change_log：导入 / 发布 / 修改）</span>
      </div>
      <el-form :inline="true" label-width="80px" style="margin-bottom: 16px;">
        <el-form-item label="成绩ID">
          <el-input v-model="scoreId" placeholder="可选" clearable style="width: 120px;" type="number" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetch">查询</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="apiMissing" title="请联系管理员开通成绩审计日志接口（GET /api/grade/change/log）" type="warning" :closable="false" show-icon style="margin-bottom: 16px;" />
      <el-table v-else :data="list" border stripe style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="scoreId" label="成绩ID" width="90" />
        <el-table-column prop="operation" label="操作类型" width="100">
          <template slot-scope="scope">{{ logOperationLabel(scope.row.operation) }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="beforeJson" label="变更前" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.beforeJson">{{ String(scope.row.beforeJson).slice(0, 80) }}…</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="afterJson" label="变更后" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.afterJson">{{ String(scope.row.afterJson).slice(0, 80) }}…</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { formatDateTime, LOG_OPERATION_LABELS } from '@/utils/gradeFormat'

export default {
  name: 'GradeChangeLog',
  data () {
    return {
      scoreId: null,
      list: [],
      loading: false,
      apiMissing: false
    }
  },
  created () {
    this.fetch()
  },
  methods: {
    formatDateTime,
    logOperationLabel (op) {
      return LOG_OPERATION_LABELS[op] || op || '—'
    },
    fetch () {
      this.loading = true
      this.apiMissing = false
      const params = this.scoreId ? { scoreId: this.scoreId } : {}
      this.axios.get('/grade/change/log', { params }).then(resp => {
        this.list = (resp.data && resp.data.list) || resp.data || []
      }).catch(err => {
        if (err.response && err.response.status === 404) {
          this.apiMissing = true
          this.list = []
        } else {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '查询失败')
        }
      }).finally(() => { this.loading = false })
    }
  }
}
</script>

<style scoped>
.tips { font-size: 13px; color: #909399; margin-left: 8px; }
</style>
