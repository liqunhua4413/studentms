<template>
  <el-dialog
    title="成绩修改申请"
    :visible.sync="visible"
    width="560px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="row">
      <p><strong>学生：</strong>{{ row.sname }}（{{ row.studentNo || row.studentId }}）</p>
      <p><strong>课程：</strong>{{ row.cname }}</p>
      <p><strong>学期：</strong>{{ row.term }}</p>
      <p><strong>原总成绩：</strong>{{ formatScore(row.grade) }}</p>
    </div>
    <el-form ref="form" :model="form" :rules="rules" label-width="100px" style="margin-top: 16px;">
      <el-form-item label="目标成绩" prop="targetGrade">
        <el-input-number
          v-model="form.targetGrade"
          :min="0"
          :max="100"
          :precision="2"
          :step="0.01"
          placeholder="请输入"
          style="width: 100%;"
        />
      </el-form-item>
      <el-form-item label="申请原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          placeholder="必填"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="证明附件">
        <el-upload
          :auto-upload="true"
          :action="uploadAction"
          :headers="uploadHeaders"
          :http-request="customUpload"
          :on-success="onAttachmentSuccess"
          :on-error="onAttachmentError"
          :file-list="attachmentList"
          :limit="1"
          accept=".pdf,.jpg,.jpeg,.png"
        >
          <el-button size="small" type="primary">选择附件</el-button>
        </el-upload>
        <span class="hint">可选；支持 pdf / 图片。若无上传接口可留空提交。</span>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交（状态 PENDING）</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { formatScore } from '@/utils/gradeFormat'

export default {
  name: 'GradeChangeRequestDialog',
  props: {
    value: { type: Boolean, default: false },
    row: { type: Object, default: null }
  },
  data () {
    return {
      form: { targetGrade: null, reason: '', attachmentPath: '' },
      rules: {
        targetGrade: [{ required: true, message: '请输入目标成绩', trigger: 'blur' }],
        reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
      },
      submitting: false,
      attachmentList: []
    }
  },
  computed: {
    visible: {
      get () { return this.value },
      set (v) { this.$emit('input', v) }
    },
    uploadAction () {
      return (this.axios.defaults.baseURL || '/api') + '/grade/change/upload-attachment'
    },
    uploadHeaders () {
      const name = sessionStorage.getItem('name')
      const type = sessionStorage.getItem('type')
      const depId = sessionStorage.getItem('departmentId')
      const h = { Operator: encodeURIComponent(name || ''), UserType: type || '' }
      if (depId) h.DepartmentId = depId
      return h
    }
  },
  watch: {
    value (v) {
      if (v && this.row) {
        this.form.targetGrade = null
        this.form.reason = ''
        this.form.attachmentPath = ''
        this.attachmentList = []
      }
    }
  },
  methods: {
    formatScore,
    customUpload (options) {
      const form = new FormData()
      form.append('file', options.file)
      this.axios.post('/grade/change/upload-attachment', form).then(r => {
        const path = (r.data && (r.data.path || r.data.url)) || null
        if (path) this.form.attachmentPath = path
        options.onSuccess(r)
      }).catch(err => {
        this.$message.warning('附件上传失败，可留空提交')
        options.onError(err)
      })
    },
    onAttachmentSuccess (resp, file) {
      const path = resp && (resp.path || resp.url)
      if (path) this.form.attachmentPath = path
    },
    onAttachmentError () {
      this.$message.warning('附件上传失败，可留空提交')
    },
    submit () {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (!this.row || !this.row.id) {
          this.$message.error('缺少成绩记录')
          return
        }
        this.submitting = true
        this.axios.post('/grade/change/request', {
          scoreId: this.row.id,
          targetGrade: this.form.targetGrade,
          reason: this.form.reason.trim(),
          attachmentPath: this.form.attachmentPath || undefined
        }).then(resp => {
          const data = resp.data || {}
          const ok = data.success === true
          this.$message[ok ? 'success' : 'warning'](data.message || (ok ? '提交成功' : '提交失败'))
          if (ok) {
            this.visible = false
            this.$emit('submitted')
          }
        }).catch(err => {
          const msg = (err.response && err.response.data && err.response.data.message) || err.message || '提交失败'
          this.$message.error(msg)
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleClose () {
      this.$refs.form && this.$refs.form.resetFields()
    }
  }
}
</script>

<style scoped>
.hint { font-size: 12px; color: #909399; margin-left: 8px; }
</style>
