<template>
  <div style="padding: 20px;">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的申请</span>
      </div>
      <el-table :data="list" border stripe style="width: 100%;" v-loading="loading">
        <el-table-column prop="studentNo" label="学号" width="110" />
        <el-table-column prop="sname" label="姓名" width="90" />
        <el-table-column prop="cname" label="课程名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="term" label="学期" width="110" />
        <el-table-column label="修改前" width="200">
          <template slot-scope="scope">
            <div v-if="scope.row.beforeData" class="score-cell">
              <span>平时 {{ getScoreFromJson(scope.row.beforeData, 'usual_score') }}</span>
              <span>期中 {{ getScoreFromJson(scope.row.beforeData, 'mid_score') }}</span>
              <span>期末 {{ getScoreFromJson(scope.row.beforeData, 'final_score') }}</span>
              <span>总分 {{ formatTotalScore(getScoreFromJson(scope.row.beforeData, 'grade')) }}</span>
            </div>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="修改后" width="200">
          <template slot-scope="scope">
            <div v-if="scope.row.afterData" class="score-cell">
              <span>平时 {{ getScoreFromJson(scope.row.afterData, 'usual_score') }}</span>
              <span>期中 {{ getScoreFromJson(scope.row.afterData, 'mid_score') }}</span>
              <span>期末 {{ getScoreFromJson(scope.row.afterData, 'final_score') }}</span>
              <span>总分 {{ formatTotalScore(getScoreFromJson(scope.row.afterData, 'grade')) }}</span>
            </div>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请原因" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" min-width="120" show-overflow-tooltip />
        <el-table-column label="附件" width="100">
          <template slot-scope="scope">
            <template v-if="scope.row.attachmentPath">
              <el-button type="text" size="small" @click="previewAtt(scope.row)">预览</el-button>
              <el-button type="text" size="small" @click="downloadAtt(scope.row)">下载</el-button>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="requestStatusTagType(scope.row.status)" size="small">
              {{ requestStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 'REJECTED'"
              type="text"
              size="small"
              @click="openResubmit(scope.row)"
            >
              重新编辑
            </el-button>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="附件预览" :visible.sync="previewVisible" width="75%" top="5vh" append-to-body @close="revokePreviewUrl">
      <div v-if="previewRow && previewRow.attachmentPath" class="preview-wrap">
        <iframe v-if="isPdfPreview && previewBlobUrl" :src="previewBlobUrl" class="preview-iframe" />
        <img v-else-if="isImagePreview && previewBlobUrl" :src="previewBlobUrl" class="preview-img" alt="预览" />
        <p v-else-if="previewLoading">加载中…</p>
        <p v-else>该格式暂不支持在线预览，请使用下载。</p>
      </div>
    </el-dialog>

    <el-dialog title="重新编辑并再次提交" :visible.sync="resubmitVisible" width="520px" :close-on-click-modal="false">
      <el-form ref="resubmitForm" :model="resubmitForm" label-width="100px">
        <el-form-item label="学号">{{ resubmitRow.studentNo }}</el-form-item>
        <el-form-item label="姓名">{{ resubmitRow.sname }}</el-form-item>
        <el-form-item label="课程">{{ resubmitRow.cname }}</el-form-item>
        <el-form-item label="学期">{{ resubmitRow.term }}</el-form-item>
        <el-form-item label="修改前（只读）">
          <span>平时 {{ resubmitBefore.usual }} 期中 {{ resubmitBefore.mid }} 期末 {{ resubmitBefore.final }} 总分 {{ resubmitBefore.grade }}</span>
        </el-form-item>
        <el-form-item label="修改后" required>
          <el-row :gutter="8">
            <el-col :span="6">
              <el-input-number v-model="resubmitForm.usualScore" :min="0" :max="100" :precision="2" :step="0.1" placeholder="平时" controls-position="right" style="width:100%" />
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="resubmitForm.midScore" :min="0" :max="100" :precision="2" :step="0.1" placeholder="期中" controls-position="right" style="width:100%" />
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="resubmitForm.finalScore" :min="0" :max="100" :precision="2" :step="0.1" placeholder="期末" controls-position="right" style="width:100%" />
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="resubmitForm.grade" :min="0" :max="100" :precision="2" :step="0.1" placeholder="总分" controls-position="right" style="width:100%" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="申请原因" required>
          <el-input v-model="resubmitForm.reason" type="textarea" :rows="3" placeholder="必填" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="resubmitVisible = false">取消</el-button>
        <el-button type="primary" :loading="resubmitting" @click="submitResubmit">再次提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { formatTotalScore, formatDateTime, requestStatusLabel, REQUEST_STATUS } from '@/utils/gradeFormat'

export default {
  name: 'MyGradeChangeRequests',
  data () {
    return {
      list: [],
      loading: false,
      resubmitVisible: false,
      resubmitRow: {},
      resubmitForm: { usualScore: null, midScore: null, finalScore: null, grade: null, reason: '' },
      resubmitBefore: { usual: '—', mid: '—', final: '—', grade: '—' },
      resubmitting: false,
      previewVisible: false,
      previewRow: null,
      previewBlobUrl: '',
      previewLoading: false
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
    fetch () {
      this.loading = true
      this.axios.get('/grade/change/my-details').then(resp => {
        this.list = resp.data || []
      }).catch(() => { this.$message.error('加载我的申请失败') }).finally(() => { this.loading = false })
    },
    parseScoreJson (jsonStr) {
      const out = { usual: '—', mid: '—', final: '—', grade: '—' }
      if (!jsonStr) return out
      try {
        const d = JSON.parse(jsonStr)
        if (d.usual_score != null) out.usual = Number(d.usual_score).toFixed(2)
        if (d.mid_score != null) out.mid = Number(d.mid_score).toFixed(2)
        if (d.final_score != null) out.final = Number(d.final_score).toFixed(2)
        if (d.grade != null) out.grade = Math.round(Number(d.grade)).toString()
      } catch (e) {}
      return out
    },
    parseScoreNumbers (jsonStr) {
      const out = { usualScore: null, midScore: null, finalScore: null, grade: null }
      if (!jsonStr) return out
      try {
        const d = JSON.parse(jsonStr)
        if (d.usual_score != null) out.usualScore = parseFloat(d.usual_score)
        if (d.mid_score != null) out.midScore = parseFloat(d.mid_score)
        if (d.final_score != null) out.finalScore = parseFloat(d.final_score)
        if (d.grade != null) out.grade = parseFloat(d.grade)
      } catch (e) {}
      return out
    },
    openResubmit (row) {
      this.resubmitRow = row
      this.resubmitBefore = this.parseScoreJson(row.beforeData)
      const after = this.parseScoreNumbers(row.afterData)
      this.resubmitForm = {
        usualScore: after.usualScore,
        midScore: after.midScore,
        finalScore: after.finalScore,
        grade: after.grade,
        reason: (row.reason || '').trim()
      }
      this.resubmitVisible = true
    },
    submitResubmit () {
      if (!(this.resubmitForm.reason || '').trim()) {
        this.$message.warning('请填写申请原因')
        return
      }
      const beforeData = this.resubmitRow.beforeData
      const afterData = JSON.stringify({
        usual_score: this.resubmitForm.usualScore,
        mid_score: this.resubmitForm.midScore,
        final_score: this.resubmitForm.finalScore,
        grade: this.resubmitForm.grade
      })
      this.resubmitting = true
      this.axios.post('/grade/change/resubmit', {
        requestId: this.resubmitRow.id,
        beforeData,
        afterData,
        reason: this.resubmitForm.reason.trim()
      }).then(resp => {
        const d = resp.data || {}
        const ok = d.success === true
        this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '已重新提交' : '提交失败'))
        if (ok) {
          this.resubmitVisible = false
          this.fetch()
        }
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '提交失败')
      }).finally(() => { this.resubmitting = false })
    },
    previewAtt (row) {
      this.revokePreviewUrl()
      this.previewRow = row
      this.previewBlobUrl = ''
      this.previewLoading = true
      this.previewVisible = true
      this.axios.get('/grade/change/attachment', { params: { requestId: row.id, inline: 1 }, responseType: 'blob' }).then(r => {
        this.previewLoading = false
        this.previewBlobUrl = URL.createObjectURL(r.data)
      }).catch(() => { this.previewLoading = false; this.$message.error('预览失败') })
    },
    revokePreviewUrl () {
      if (this.previewBlobUrl) { URL.revokeObjectURL(this.previewBlobUrl); this.previewBlobUrl = '' }
      this.previewRow = null
    },
    downloadAtt (row) {
      this.axios.get('/grade/change/attachment', { params: { requestId: row.id }, responseType: 'blob' }).then(r => {
        const fn = (row.attachmentPath || '').split(/[/\\]/).pop() || '附件'
        const url = URL.createObjectURL(r.data)
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
.score-cell { display: flex; flex-wrap: wrap; gap: 4px 10px; font-size: 12px; }
.score-cell span { white-space: nowrap; }
.preview-wrap { min-height: 50vh; }
.preview-iframe { width: 100%; height: 65vh; border: none; }
.preview-img { max-width: 100%; max-height: 65vh; }
</style>
