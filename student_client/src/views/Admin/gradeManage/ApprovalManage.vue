<template>
  <div style="padding: 20px;">
    <el-card>
      <div slot="header" class="clearfix">
        <span>审批管理</span>
      </div>

      <el-tabs v-model="activeTab" @tab-click="onTabClick">
        <el-tab-pane label="待审批" name="pending" />
        <el-tab-pane label="已审批" name="approved" />
      </el-tabs>

      <div class="table-scroll-wrapper">
        <el-table :data="list" border stripe v-loading="loading" style="width: 100%; min-width: 1400px;">
          <el-table-column prop="studentNo" label="学号" width="100" />
          <el-table-column prop="sname" label="学生姓名" width="90" />
          <el-table-column prop="cname" label="课程名称" min-width="110" show-overflow-tooltip />
          <el-table-column prop="term" label="学期" width="100" />
          <el-table-column label="平时" width="120">
            <template slot-scope="scope">
              <span>{{ getScoreFromJson(scope.row.beforeData, 'usual_score') }} → {{ getScoreFromJson(scope.row.afterData, 'usual_score') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="期中" width="120">
            <template slot-scope="scope">
              <span>{{ getScoreFromJson(scope.row.beforeData, 'mid_score') }} → {{ getScoreFromJson(scope.row.afterData, 'mid_score') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="期末" width="120">
            <template slot-scope="scope">
              <span>{{ getScoreFromJson(scope.row.beforeData, 'final_score') }} → {{ getScoreFromJson(scope.row.afterData, 'final_score') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="总成绩" width="120">
            <template slot-scope="scope">
              <span>{{ formatTotalScore(getScoreFromJson(scope.row.beforeData, 'grade')) }} → {{ formatTotalScore(getScoreFromJson(scope.row.afterData, 'grade')) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="申请原因" min-width="120" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="申请时间" width="155">
            <template slot-scope="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="附件" width="120">
            <template slot-scope="scope">
              <template v-if="scope.row.attachmentPath">
                <el-button type="text" size="small" @click="previewAttachment(scope.row)">预览</el-button>
                <el-button type="text" size="small" @click="downloadAttachment(scope.row)">下载</el-button>
              </template>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="当前状态" width="95">
            <template slot-scope="scope">
              <el-tag :type="requestStatusTagType(scope.row.status)" size="small">
                {{ requestStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <template v-if="scope.row.status === 'PENDING'">
                <el-button type="text" size="small" @click="openApprove(scope.row, true)">通过</el-button>
                <el-button type="text" size="small" @click="openApprove(scope.row, false)">拒绝</el-button>
              </template>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-dialog
        :title="(approveApproved ? '通过' : '拒绝') + '申请'"
        :visible.sync="approveDialogVisible"
        width="460px"
      >
        <el-form v-if="!approveApproved" label-width="100px">
          <el-form-item label="拒绝原因" required>
            <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="必填" />
          </el-form-item>
        </el-form>
        <p v-else>确认通过该成绩修改申请？通过后将更新成绩主表并记录日志。</p>
        <span slot="footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="approving" @click="submitApprove">确定</el-button>
        </span>
      </el-dialog>

      <el-dialog title="附件预览" :visible.sync="previewVisible" width="80%" top="5vh" append-to-body @close="revokePreviewUrl">
        <div v-if="previewRow && previewRow.attachmentPath" class="preview-wrap">
          <iframe v-if="isPdfPreview && previewBlobUrl" :src="previewBlobUrl" class="preview-iframe" />
          <img v-else-if="isImagePreview && previewBlobUrl" :src="previewBlobUrl" class="preview-img" alt="预览" />
          <p v-else-if="!previewBlobUrl && previewLoading">加载中…</p>
          <p v-else-if="!previewBlobUrl && previewError">预览失败，请<a href="javascript:;" @click="downloadAttachment(previewRow)">下载</a>后查看。</p>
          <p v-else>该格式暂不支持在线预览，请<a href="javascript:;" @click="downloadAttachment(previewRow)">下载</a>后查看。</p>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { formatTotalScore, formatDateTime, requestStatusLabel, REQUEST_STATUS } from '@/utils/gradeFormat'

export default {
  name: 'ApprovalManage',
  data () {
    return {
      activeTab: 'pending',
      list: [],
      loading: false,
      approveDialogVisible: false,
      approveRow: null,
      approveApproved: true,
      rejectReason: '',
      approving: false,
      previewVisible: false,
      previewRow: null,
      previewBlobUrl: '',
      previewLoading: false,
      previewError: false
    }
  },
  computed: {
    isPdfPreview () {
      const p = (this.previewRow && this.previewRow.attachmentPath) || ''
      return /\.pdf$/i.test(p)
    },
    isImagePreview () {
      const p = (this.previewRow && this.previewRow.attachmentPath) || ''
      return /\.(jpg|jpeg|png|gif|webp|bmp)$/i.test(p)
    }
  },
  created () {
    if (sessionStorage.getItem('type') !== 'admin') {
      this.$message.warning('仅管理员可访问审批管理')
      this.$router.replace('/adminHome')
      return
    }
    this.fetch()
  },
  methods: {
    formatTotalScore,
    formatDateTime,
    requestStatusLabel,
    getScoreFromJson (jsonStr, key) {
      if (!jsonStr) return '—'
      try {
        const data = JSON.parse(jsonStr)
        const val = data[key]
        if (val === null || val === undefined) return '—'
        if (key === 'grade') return Math.round(parseFloat(val)).toString()
        return parseFloat(val).toFixed(2)
      } catch (e) {
        return '—'
      }
    },
    requestStatusTagType (s) {
      if (s === REQUEST_STATUS.APPROVED) return 'success'
      if (s === REQUEST_STATUS.REJECTED) return 'danger'
      if (s === REQUEST_STATUS.DEAN_APPROVED) return 'primary'
      return 'warning'
    },
    onTabClick () {
      this.fetch()
    },
    fetch () {
      this.loading = true
      this.axios.get('/grade/change/admin-list', { params: { tab: this.activeTab } }).then(resp => {
        this.list = resp.data || []
      }).catch(() => { this.$message.error('加载失败') }).finally(() => { this.loading = false })
    },
    openApprove (row, approved) {
      this.approveRow = row
      this.approveApproved = approved
      this.rejectReason = ''
      this.approveDialogVisible = true
    },
    submitApprove () {
      if (!this.approveApproved && !(this.rejectReason || '').trim()) {
        this.$message.warning('拒绝时须填写原因')
        return
      }
      this.approving = true
      this.axios.post('/grade/change/admin-approve', {
        requestId: this.approveRow.id,
        approved: this.approveApproved,
        rejectReason: this.approveApproved ? undefined : this.rejectReason.trim()
      }).then(resp => {
        const d = resp.data || {}
        const ok = d.success === true
        this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '操作成功' : '操作失败'))
        if (ok) {
          this.approveDialogVisible = false
          this.fetch()
        }
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '操作失败')
      }).finally(() => { this.approving = false })
    },
    previewAttachment (row) {
      this.revokePreviewUrl()
      this.previewRow = row
      this.previewBlobUrl = ''
      this.previewLoading = true
      this.previewError = false
      this.previewVisible = true
      this.axios.get('/grade/change/attachment', {
        params: { requestId: row.id, inline: 1 },
        responseType: 'blob'
      }).then(resp => {
        this.previewLoading = false
        this.previewBlobUrl = URL.createObjectURL(resp.data)
      }).catch(() => {
        this.previewLoading = false
        this.previewError = true
      })
    },
    revokePreviewUrl () {
      if (this.previewBlobUrl) {
        URL.revokeObjectURL(this.previewBlobUrl)
        this.previewBlobUrl = ''
      }
      this.previewRow = null
      this.previewError = false
    },
    downloadAttachment (row) {
      this.axios.get('/grade/change/attachment', {
        params: { requestId: row.id },
        responseType: 'blob'
      }).then(resp => {
        const fn = (row.attachmentPath || '').split(/[/\\]/).pop() || '附件'
        const url = URL.createObjectURL(resp.data)
        const a = document.createElement('a')
        a.href = url
        a.download = fn
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
        this.$message.success('下载成功')
      }).catch(() => { this.$message.error('下载失败') })
    }
  }
}
</script>

<style scoped>
.table-scroll-wrapper { max-height: 65vh; overflow: auto; width: 100%; }
.preview-wrap { min-height: 60vh; }
.preview-iframe { width: 100%; height: 70vh; border: none; }
.preview-img { max-width: 100%; max-height: 70vh; }
</style>
