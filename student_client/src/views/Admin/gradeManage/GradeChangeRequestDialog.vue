<template>
  <el-dialog
    title="申请修改成绩"
    :visible.sync="visible"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="form" label-width="120px">
      <!-- 第一步：输入学号 -->
      <el-form-item label="学号" prop="studentNo">
        <el-input
          v-model="form.studentNo"
          placeholder="请输入学号"
          style="width: 300px;"
          clearable
        >
          <el-button slot="append" @click="queryStudent" :loading="queryingStudent">查询</el-button>
        </el-input>
      </el-form-item>

      <!-- 学生信息（只读） -->
      <el-form-item v-if="studentInfo" label="学生信息">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-input v-model="studentInfo.departmentName" placeholder="学院" disabled />
          </el-col>
          <el-col :span="12">
            <el-input v-model="studentInfo.majorName" placeholder="专业" disabled />
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="12">
            <el-input v-model="studentInfo.className" placeholder="班级" disabled />
          </el-col>
          <el-col :span="12">
            <el-input v-model="studentInfo.gradeLevel" placeholder="年级" disabled />
          </el-col>
        </el-row>
      </el-form-item>

      <!-- 第二步：选择学期和课程 -->
      <el-form-item label="学期" prop="termId">
        <el-select v-model="form.termId" placeholder="请选择学期" clearable style="width: 300px;">
          <el-option v-for="t in terms" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="课程" prop="courseId">
        <el-select v-model="form.courseId" placeholder="请选择课程" clearable filterable style="width: 300px;" @change="loadScore">
          <el-option v-for="c in courses" :key="c.id" :label="c.cname" :value="c.id" />
        </el-select>
      </el-form-item>

      <!-- 原始成绩（只读） -->
      <el-form-item v-if="originalScore" label="原始成绩（只读）">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input v-model="originalScore.usualScore" placeholder="平时成绩" disabled>
              <template slot="prepend">平时</template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input v-model="originalScore.midScore" placeholder="期中成绩" disabled>
              <template slot="prepend">期中</template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input v-model="originalScore.finalScore" placeholder="期末成绩" disabled>
              <template slot="prepend">期末</template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input v-model="originalScore.grade" placeholder="总成绩" disabled>
              <template slot="prepend">总分</template>
            </el-input>
          </el-col>
        </el-row>
      </el-form-item>

      <!-- 申请修改成绩（可编辑） -->
      <el-form-item label="申请修改成绩" prop="newScores">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input-number
              v-model="form.usualScoreNew"
              :min="0"
              :max="100"
              :precision="2"
              :step="0.1"
              placeholder="平时成绩"
              style="width: 100%;"
            >
              <template slot="prepend">平时</template>
            </el-input-number>
          </el-col>
          <el-col :span="6">
            <el-input-number
              v-model="form.midScoreNew"
              :min="0"
              :max="100"
              :precision="2"
              :step="0.1"
              placeholder="期中成绩"
              style="width: 100%;"
            >
              <template slot="prepend">期中</template>
            </el-input-number>
          </el-col>
          <el-col :span="6">
            <el-input-number
              v-model="form.finalScoreNew"
              :min="0"
              :max="100"
              :precision="2"
              :step="0.1"
              placeholder="期末成绩"
              style="width: 100%;"
            >
              <template slot="prepend">期末</template>
            </el-input-number>
          </el-col>
          <el-col :span="6">
            <el-input-number
              v-model="form.gradeNew"
              :min="0"
              :max="100"
              :precision="0"
              :step="1"
              placeholder="总成绩"
              style="width: 100%;"
            >
              <template slot="prepend">总分</template>
            </el-input-number>
          </el-col>
        </el-row>
      </el-form-item>

      <!-- 修改原因 -->
      <el-form-item label="修改原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          placeholder="请详细说明修改原因（必填）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 附件 -->
      <el-form-item label="证明材料">
        <el-upload
          :action="uploadUrl"
          :http-request="customUpload"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemove"
          :file-list="fileList"
          :limit="1"
        >
          <el-button size="small" type="primary">点击上传</el-button>
          <div slot="tip" class="el-upload__tip">只能上传一个文件，支持 jpg/png/pdf/doc/docx 格式</div>
        </el-upload>
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="submitRequest" :loading="submitting">保存申请</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'GradeChangeRequestDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      form: {
        studentNo: '',
        termId: null,
        courseId: null,
        usualScoreNew: null,
        midScoreNew: null,
        finalScoreNew: null,
        gradeNew: null,
        reason: '',
        attachmentPath: ''
      },
      rules: {
        studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        termId: [{ required: true, message: '请选择学期', trigger: 'change' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        reason: [{ required: true, message: '请输入修改原因', trigger: 'blur' }]
      },
      studentInfo: null,
      originalScore: null,
      courses: [],
      terms: [],
      queryingStudent: false,
      submitting: false,
      fileList: [],
      uploadUrl: '/api/paper/upload'
    }
  },
  watch: {
    visible (val) {
      if (val) {
        this.loadTerms()
        this.loadCourses()
      }
    }
  },
  methods: {
    loadTerms () {
      this.axios.get('/term/findAll').then(resp => {
        this.terms = resp.data || []
      }).catch(() => { this.terms = [] })
    },
    loadCourses () {
      this.axios.get('/course/findAll').then(resp => {
        this.courses = resp.data || []
      }).catch(() => {})
    },
    queryStudent () {
      if (!this.form.studentNo) {
        this.$message.warning('请输入学号')
        return
      }
      this.queryingStudent = true
      this.axios.post('/student/findByStudentNo', { studentNo: this.form.studentNo }).then(resp => {
        if (resp.data && resp.data.id) {
          this.studentInfo = {
            departmentName: resp.data.departmentName || '',
            majorName: resp.data.majorName || '',
            className: resp.data.className || '',
            gradeLevel: resp.data.gradeLevelName || ''
          }
        } else {
          this.$message.warning('未找到该学号')
          this.studentInfo = null
        }
      }).catch(() => {
        this.$message.error('查询学号失败')
        this.studentInfo = null
      }).finally(() => {
        this.queryingStudent = false
      })
    },
    loadScore () {
      if (!this.form.studentNo || !this.form.termId || !this.form.courseId) {
        return
      }
      // 先查询学生ID
      this.axios.post('/student/findByStudentNo', { studentNo: this.form.studentNo }).then(studentResp => {
        if (!studentResp.data || !studentResp.data.id) {
          this.$message.warning('请先查询学生信息')
          return
        }
        const studentId = studentResp.data.id
        // 查询成绩
        this.axios.post('/grade/query', {
          studentId: studentId,
          courseId: this.form.courseId,
          termId: this.form.termId,
          status: 'PUBLISHED'
        }).then(scoreResp => {
          if (scoreResp.data && scoreResp.data.length > 0) {
            const score = scoreResp.data[0]
            this.originalScore = {
              usualScore: score.usualScore != null ? parseFloat(score.usualScore).toFixed(2) : '—',
              midScore: score.midScore != null ? parseFloat(score.midScore).toFixed(2) : '—',
              finalScore: score.finalScore != null ? parseFloat(score.finalScore).toFixed(2) : '—',
              grade: score.grade != null ? Math.round(parseFloat(score.grade)).toString() : '—',
              scoreId: score.id
            }
            // 默认值 = 原始成绩
            this.form.usualScoreNew = score.usualScore != null ? parseFloat(score.usualScore) : null
            this.form.midScoreNew = score.midScore != null ? parseFloat(score.midScore) : null
            this.form.finalScoreNew = score.finalScore != null ? parseFloat(score.finalScore) : null
            this.form.gradeNew = score.grade != null ? Math.round(parseFloat(score.grade)) : null
          } else {
            this.$message.warning('未找到该学生的已发布成绩')
            this.originalScore = null
          }
        }).catch(() => {
          this.$message.error('查询成绩失败')
          this.originalScore = null
        })
      }).catch(() => {
        this.$message.error('查询学生失败')
      })
    },
    customUpload (options) {
      const { file, onSuccess, onError } = options
      const formData = new FormData()
      formData.append('file', file)
      const uploadBy = sessionStorage.getItem('name') || 'admin'
      formData.append('uploadBy', uploadBy)
      this.axios.post('/paper/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then(res => {
        if (res.data && res.data.id) {
          this.form.attachmentPath = res.data.filePath || res.data.file_path || String(res.data.id) || ''
          this.fileList = [{ name: file.name, url: `/paper/${res.data.id}/download` }]
          onSuccess(res.data, file)
        } else {
          onError(new Error('上传失败：响应格式错误'))
        }
      }).catch(err => {
        const msg = (err.response && err.response.data && err.response.data.message) || err.message || '上传失败'
        onError(new Error(msg))
      })
    },
    handleUploadSuccess (response, file) {
      // 已在 customUpload 中处理
    },
    handleRemove () {
      this.form.attachmentPath = ''
      this.fileList = []
    },
    submitRequest () {
      this.$refs.form.validate((valid) => {
        if (!valid) return false
        if (!this.originalScore || !this.originalScore.scoreId) {
          this.$message.warning('请先查询并加载成绩信息')
          return
        }
        // 对比新旧成绩
        const before = {
          usual_score: this.originalScore.usualScore === '—' ? null : parseFloat(this.originalScore.usualScore),
          mid_score: this.originalScore.midScore === '—' ? null : parseFloat(this.originalScore.midScore),
          final_score: this.originalScore.finalScore === '—' ? null : parseFloat(this.originalScore.finalScore),
          grade: this.originalScore.grade === '—' ? null : parseInt(this.originalScore.grade)
        }
        const after = {
          usual_score: this.form.usualScoreNew,
          mid_score: this.form.midScoreNew,
          final_score: this.form.finalScoreNew,
          grade: this.form.gradeNew
        }
        // 检查是否有变化（考虑 null 和数值比较）
        const compareValue = (a, b) => {
          if (a === null && b === null) return true
          if (a === null || b === null) return false
          return Math.abs(parseFloat(a) - parseFloat(b)) < 0.01
        }
        const hasChange = 
          !compareValue(before.usual_score, after.usual_score) ||
          !compareValue(before.mid_score, after.mid_score) ||
          !compareValue(before.final_score, after.final_score) ||
          !compareValue(before.grade, after.grade)
        if (!hasChange) {
          this.$message.warning('未检测到修改，请至少修改一项成绩')
          return
        }
        this.submitting = true
        this.axios.post('/grade/change/request', {
          scoreId: this.originalScore.scoreId,
          beforeData: JSON.stringify(before),
          afterData: JSON.stringify(after),
          reason: this.form.reason,
          attachmentPath: this.form.attachmentPath
        }).then(resp => {
          if (resp.data && resp.data.success) {
            this.$message.success(resp.data.message || '申请已提交')
            this.$emit('success')
            this.handleClose()
          } else {
            this.$message.error(resp.data?.message || '提交失败')
          }
        }).catch(err => {
          this.$message.error(err.response?.data?.message || err.message || '提交失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleClose () {
      this.$refs.form && this.$refs.form.resetFields()
      this.studentInfo = null
      this.originalScore = null
      this.fileList = []
      this.form = {
        studentNo: '',
        termId: null,
        courseId: null,
        usualScoreNew: null,
        midScoreNew: null,
        finalScoreNew: null,
        gradeNew: null,
        reason: '',
        attachmentPath: ''
      }
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped>
</style>
