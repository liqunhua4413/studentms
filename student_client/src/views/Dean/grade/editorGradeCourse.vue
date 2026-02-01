<template>
  <div style="padding: 20px;">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>成绩修改申请</span>
      </div>
      <el-form style="width: 80%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院（本院）">
              <el-select v-model="ruleForm.departmentId" placeholder="本院" disabled style="width: 100%">
                <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="courseId">
              <el-select v-model="ruleForm.courseId" placeholder="请先选择课程" style="width: 100%" filterable @change="handleCourseChange">
                <el-option v-for="item in courses" :key="item.id" :label="item.cname" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学期" prop="termId">
              <el-select v-model="ruleForm.termId" placeholder="请选择学期" style="width: 100%" @change="handleCourseOrTermChange">
                <el-option v-for="item in terms" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input
                v-model="ruleForm.studentNo"
                placeholder="输入学号后按回车定位成绩"
                @keyup.enter.native="handleStudentNoEnter"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生姓名">
              <el-input v-model="ruleForm.sname" disabled placeholder="自动根据学号获取" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学生学院">
              <el-input v-model="ruleForm.studentDepartmentName" disabled placeholder="自动获取" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生专业">
              <el-input v-model="ruleForm.studentMajorName" disabled placeholder="自动获取" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学生班级">
              <el-input v-model="ruleForm.studentClassName" disabled placeholder="自动获取" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任课教师">
              <el-input v-model="ruleForm.teacherName" disabled placeholder="自动根据成绩记录获取" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">修改前（只读）</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="平时">
              <el-input :value="formatScore(ruleForm.beforeUsualScore)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期中">
              <el-input :value="formatScore(ruleForm.beforeMidScore)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期末">
              <el-input :value="formatScore(ruleForm.beforeFinalScore)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="总分">
              <el-input :value="formatTotalScore(ruleForm.beforeGrade)" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">修改后（可编辑）</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="平时" prop="usualScore">
              <el-input-number v-model="ruleForm.usualScore" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期中" prop="midScore">
              <el-input-number v-model="ruleForm.midScore" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期末" prop="finalScore">
              <el-input-number v-model="ruleForm.finalScore" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="总分" prop="grade">
              <el-input-number v-model="ruleForm.grade" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="申请原因" prop="reason">
              <el-input v-model="ruleForm.reason" type="textarea" :rows="3" placeholder="必填，说明修改原因" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="证明附件">
              <el-upload
                :action="uploadAction"
                :headers="uploadHeaders"
                :http-request="customUpload"
                :file-list="attachmentList"
                :limit="1"
                :on-success="onAttachmentSuccess"
                :on-error="onAttachmentError"
                :on-remove="onAttachmentRemove"
                accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
              >
                <el-button size="small" type="default">上传附件</el-button>
              </el-upload>
              <span class="attach-hint">可选，支持 pdf / 图片 / Word；最多 1 个。</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item style="margin-top: 20px;">
          <el-button type="primary" :loading="submitting" @click="submitForm('ruleForm')">提交</el-button>
          <el-button @click="resetForm('ruleForm')">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { formatScore, formatTotalScore } from '@/utils/gradeFormat'

export default {
  name: 'DeanEditorGradeCourse',
  data () {
    return {
      ruleForm: {
        scoreId: null,
        studentId: null,
        studentNo: '',
        courseId: null,
        teacherId: null,
        teacherName: '',
        termId: null,
        departmentId: null,
        sname: '',
        studentDepartmentName: '',
        studentMajorName: '',
        studentClassName: '',
        beforeUsualScore: null,
        beforeMidScore: null,
        beforeFinalScore: null,
        beforeGrade: null,
        usualScore: null,
        midScore: null,
        finalScore: null,
        grade: null,
        reason: '',
        attachmentPath: '',
        attachmentName: ''
      },
      attachmentList: [],
      departments: [],
      courses: [],
      terms: [],
      submitting: false,
      rules: {
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        termId: [{ required: true, message: '请选择学期', trigger: 'change' }],
        studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        grade: [{ required: true, message: '请输入总分', trigger: 'change' }],
        reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
      }
    }
  },
  computed: {
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
  created () {
    this.initData()
  },
  methods: {
    formatScore,
    formatTotalScore,
    async initData () {
      await this.loadDeanCollege()
      await Promise.all([
        this.loadCourses(),
        this.loadTerms()
      ])
    },
    loadDeanCollege () {
      return this.axios.get('/dean/college').then(resp => {
        const d = resp.data
        if (d && d.collegeId != null) {
          const cid = typeof d.collegeId === 'number' ? d.collegeId : parseInt(d.collegeId, 10)
          this.ruleForm.departmentId = cid
          this.departments = [{ id: cid, name: d.collegeName || '' }]
          sessionStorage.setItem('departmentId', String(cid))
        }
      }).catch(() => {})
    },
    loadCourses () {
      return this.axios.get('/dean/courses').then(resp => {
        this.courses = resp.data || []
      }).catch(() => {})
    },
    loadTerms () {
      return this.axios.get('/term/findAll').then(resp => {
        this.terms = resp.data || []
      }).catch(() => { this.terms = [] })
    },
    handleCourseChange () {
      this.ruleForm.termId = null
      this.ruleForm.studentNo = ''
      this.ruleForm.sname = ''
      this.ruleForm.studentId = null
      this.ruleForm.scoreId = null
      this.clearGradeData()
    },
    handleCourseOrTermChange () {
      if (this.ruleForm.courseId && this.ruleForm.termId) {
        if (this.ruleForm.studentNo) {
          this.loadGradeByStudentNo()
        }
      } else {
        this.clearGradeData()
      }
    },
    handleStudentNoEnter () {
      if (!this.ruleForm.studentNo) {
        this.$message.warning('请输入学号')
        return
      }
      if (!this.ruleForm.courseId) {
        this.$message.warning('请先选择课程')
        return
      }
      if (!this.ruleForm.termId) {
        this.$message.warning('请先选择学期')
        return
      }
      this.loadGradeByStudentNo()
    },
    loadGradeByStudentNo () {
      if (!this.ruleForm.courseId || !this.ruleForm.termId || !this.ruleForm.studentNo) return
      this.axios.get('/dean/grade/list', {
        params: {
          courseId: this.ruleForm.courseId,
          termId: this.ruleForm.termId
        }
      }).then(resp => {
        const list = resp.data || []
        const row = list.find(r => r.studentNo === this.ruleForm.studentNo)
        if (row) {
          this.ruleForm.scoreId = row.id
          this.ruleForm.studentId = row.studentId
          this.ruleForm.sname = row.sname || ''
          this.ruleForm.teacherId = row.teacherId
          this.ruleForm.teacherName = (row.teacherRealName || row.tname) || '—'
          this.ruleForm.studentDepartmentName = row.studentDepartmentName || row.departmentName || '—'
          this.ruleForm.studentMajorName = row.majorName || '—'
          this.ruleForm.studentClassName = row.className || '—'
          const u = row.usualScore != null ? parseFloat(row.usualScore) : null
          const m = row.midScore != null ? parseFloat(row.midScore) : null
          const f = row.finalScore != null ? parseFloat(row.finalScore) : null
          const g = row.grade != null ? parseFloat(row.grade) : null
          this.ruleForm.beforeUsualScore = u
          this.ruleForm.beforeMidScore = m
          this.ruleForm.beforeFinalScore = f
          this.ruleForm.beforeGrade = g
          this.ruleForm.usualScore = u
          this.ruleForm.midScore = m
          this.ruleForm.finalScore = f
          this.ruleForm.grade = g
          this.$message.success('已定位到该学生的成绩记录')
        } else {
          this.$message.warning('未找到该学生在本课程本学期的成绩记录')
          this.clearGradeData()
        }
      }).catch(() => {
        this.$message.error('查询成绩失败')
        this.clearGradeData()
      })
    },
    clearGradeData () {
      this.ruleForm.scoreId = null
      this.ruleForm.studentId = null
      this.ruleForm.sname = ''
      this.ruleForm.teacherName = ''
      this.ruleForm.studentDepartmentName = ''
      this.ruleForm.studentMajorName = ''
      this.ruleForm.studentClassName = ''
      this.ruleForm.beforeUsualScore = null
      this.ruleForm.beforeMidScore = null
      this.ruleForm.beforeFinalScore = null
      this.ruleForm.beforeGrade = null
      this.ruleForm.usualScore = null
      this.ruleForm.midScore = null
      this.ruleForm.finalScore = null
      this.ruleForm.grade = null
    },
    submitForm (formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return
        if (!this.ruleForm.scoreId) {
          this.$message.warning('请先按课程、学期、学号定位成绩记录')
          return
        }
        const before = {
          usual_score: this.ruleForm.beforeUsualScore,
          mid_score: this.ruleForm.beforeMidScore,
          final_score: this.ruleForm.beforeFinalScore,
          grade: this.ruleForm.beforeGrade
        }
        const after = {
          usual_score: this.ruleForm.usualScore,
          mid_score: this.ruleForm.midScore,
          final_score: this.ruleForm.finalScore,
          grade: this.ruleForm.grade
        }
        this.submitting = true
        this.axios.post('/dean/grade/modify-apply', {
          scoreId: this.ruleForm.scoreId,
          beforeData: JSON.stringify(before),
          afterData: JSON.stringify(after),
          reason: (this.ruleForm.reason || '').trim(),
          attachmentPath: this.ruleForm.attachmentPath || undefined,
          attachmentName: this.ruleForm.attachmentName || undefined
        }).then(resp => {
          const d = resp.data || {}
          const ok = d.success === true
          this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '申请已提交' : '提交失败'))
          if (ok) {
            this.$message.info('可在「我的申请」中查看')
            this.resetForm(formName)
          }
        }).catch(err => {
          const msg = (err.response && err.response.data && err.response.data.message) || err.message || '提交失败'
          this.$message.error(msg)
        }).finally(() => { this.submitting = false })
      })
    },
    resetForm (formName) {
      this.$refs[formName].resetFields()
      this.ruleForm.studentNo = ''
      this.ruleForm.sname = ''
      this.ruleForm.studentId = null
      this.ruleForm.scoreId = null
      this.ruleForm.termId = null
      this.ruleForm.attachmentPath = ''
      this.ruleForm.attachmentName = ''
      this.attachmentList = []
      this.clearGradeData()
    },
    customUpload (options) {
      const form = new FormData()
      form.append('file', options.file)
      this.axios.post('/grade/change/upload-attachment', form).then(r => {
        const d = r.data || {}
        const path = d.path || d.url || null
        const fileName = d.fileName || options.file.name || ''
        if (path) {
          this.ruleForm.attachmentPath = path
          this.ruleForm.attachmentName = fileName
        }
        options.onSuccess(r)
      }).catch(err => {
        this.$message.warning('附件上传失败，可留空提交')
        options.onError(err)
      })
    },
    onAttachmentSuccess (resp) {
      const d = (resp && resp.data) || {}
      const path = d.path || d.url
      const fileName = d.fileName || ''
      if (path) {
        this.ruleForm.attachmentPath = path
        this.ruleForm.attachmentName = fileName
      }
    },
    onAttachmentError () {
      this.$message.warning('附件上传失败，可留空提交')
    },
    onAttachmentRemove () {
      this.ruleForm.attachmentPath = ''
      this.ruleForm.attachmentName = ''
      this.attachmentList = []
    }
  }
}
</script>

<style scoped>
.attach-hint { margin-left: 8px; color: #909399; font-size: 12px; }
</style>
