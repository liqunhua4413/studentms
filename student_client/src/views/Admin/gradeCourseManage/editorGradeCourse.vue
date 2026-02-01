<template>
  <div style="padding: 20px;">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>成绩修改申请</span>
      </div>
      <el-form style="width: 80%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院" prop="departmentId">
              <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" @change="handleDepartmentChange" style="width: 100%">
                <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="majorId">
              <el-select v-model="ruleForm.majorId" placeholder="请选择专业" @change="handleMajorChange" style="width: 100%">
                <el-option v-for="item in majors" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="courseId">
              <el-select v-model="ruleForm.courseId" placeholder="请选择课程" style="width: 100%" filterable @change="handleCourseOrTermChange">
                <el-option v-for="item in courses" :key="item.id" :label="item.cname" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="gradeLevelId">
              <el-select v-model="ruleForm.gradeLevelId" placeholder="请选择年级" clearable style="width: 100%">
                <el-option v-for="g in gradeLevels" :key="g.id" :label="g.name" :value="g.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级" prop="classId">
              <el-select v-model="ruleForm.classId" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="item in classes" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input
                v-model="ruleForm.studentNo"
                placeholder="输入学号后按回车确认"
                @keyup.enter.native="handleStudentNoEnter"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生姓名">
              <el-input v-model="ruleForm.sname" disabled placeholder="自动根据学号获取"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期" prop="termId">
              <el-select v-model="ruleForm.termId" placeholder="请选择学期" style="width: 100%" @change="handleCourseOrTermChange">
                <el-option v-for="item in terms" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任课教师">
              <el-input v-model="ruleForm.teacherName" disabled placeholder="自动根据成绩记录获取"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">修改前（只读）</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="平时">
              <el-input :value="formatScore(ruleForm.beforeUsualScore)" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期中">
              <el-input :value="formatScore(ruleForm.beforeMidScore)" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期末">
              <el-input :value="formatScore(ruleForm.beforeFinalScore)" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="总分">
              <el-input :value="formatTotalScore(ruleForm.beforeGrade)" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">修改后（可编辑）</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="平时" prop="usualScore">
              <el-input-number v-model="ruleForm.usualScore" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期中" prop="midScore">
              <el-input-number v-model="ruleForm.midScore" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期末" prop="finalScore">
              <el-input-number v-model="ruleForm.finalScore" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="总分" prop="grade">
              <el-input-number v-model="ruleForm.grade" :min="0" :max="100" :precision="2" :step="0.01" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="申请原因" prop="reason">
              <el-input v-model="ruleForm.reason" type="textarea" :rows="3" placeholder="必填，说明修改原因" maxlength="500" show-word-limit></el-input>
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
        majorId: null,
        classId: null,
        gradeLevelId: null,
        sname: '',
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
      majors: [],
      classes: [],
      courses: [],
      terms: [],
      gradeLevels: [],
      submitting: false,
      rules: {
        departmentId: [{ required: true, message: '请选择学院', trigger: 'change' }],
        majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
        studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        termId: [{ required: true, message: '请选择学期', trigger: 'change' }],
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
    const { sid, cid, tid, termId, term } = this.$route.query
    this.initData().then(() => {
      if (sid && cid) {
        this.ruleForm.studentId = parseInt(sid, 10)
        this.ruleForm.courseId = parseInt(cid, 10)
        if (termId) {
          this.ruleForm.termId = parseInt(termId, 10)
        } else if (term && this.terms.length) {
          const t = this.terms.find(x => x.name === term)
          if (t) this.ruleForm.termId = t.id
        }
        this.ruleForm.teacherId = tid ? parseInt(tid, 10) : null
        this.axios.get(`/student/findById/${sid}`).then(sResp => {
          if (sResp.data) {
            this.ruleForm.studentNo = sResp.data.studentNo || ''
            this.ruleForm.sname = sResp.data.sname || ''
          }
        }).catch(() => {})
        this.loadGradeRecord()
      }
    }).catch(() => {})
  },
  methods: {
    formatScore,
    formatTotalScore,
    initData () {
      const userType = sessionStorage.getItem('type') || 'admin'
      const departmentId = sessionStorage.getItem('departmentId')
      
      // 如果是院长，只加载本学院的部门
      const deptPromise = userType === 'dean' && departmentId
        ? this.axios.get(`/department/findById/${departmentId}`).then(r => {
            this.departments = r.data ? [r.data] : []
            // 初始化院长的departmentId
            if (r.data && !this.ruleForm.departmentId) {
              this.ruleForm.departmentId = r.data.id
              this.fetchMajors(r.data.id)
            }
          })
        : this.axios.get('/department/findAll').then(r => { this.departments = r.data || [] })
      
      return Promise.all([
        deptPromise,
        this.axios.get('/course/findAll').then(r => { this.courses = r.data || [] }),
        this.axios.get('/term/findAll').then(r => { this.terms = r.data || [] }),
        this.axios.get('/gradeLevel/findAll').then(r => { this.gradeLevels = r.data || [] })
      ])
    },
    handleDepartmentChange (val) {
      this.ruleForm.majorId = null
      this.ruleForm.classId = null
      this.majors = []
      this.classes = []
      if (val) this.fetchMajors(val)
    },
    handleMajorChange (val) {
      this.ruleForm.classId = null
      this.classes = []
      if (val) this.fetchClasses(val)
    },
    handleStudentNoEnter () {
      if (!this.ruleForm.studentNo) return
      this.axios.post('/student/findByStudentNo', { studentNo: this.ruleForm.studentNo }).then(resp => {
        if (resp.data) {
          const s = resp.data
          this.ruleForm.sname = s.sname
          this.ruleForm.studentId = s.id
          this.ruleForm.departmentId = s.departmentId
          this.ruleForm.majorId = s.majorId
          this.ruleForm.classId = s.classId
          this.ruleForm.gradeLevelId = (this.gradeLevels || []).find(g => g.name === (s.gradeLevelName || s.gradeLevel))?.id ?? null
          if (s.departmentId) this.fetchMajors(s.departmentId)
          if (s.majorId) this.fetchClasses(s.majorId)
          this.$message.success(`已找到学生：${s.sname}`)
          if (this.ruleForm.courseId && this.ruleForm.termId) this.loadGradeRecord()
        } else {
          this.$message.error('未找到该学号对应的学生')
          this.ruleForm.sname = ''
          this.ruleForm.studentId = null
        }
      }).catch(() => this.$message.error('查询学生失败'))
    },
    handleCourseOrTermChange () {
      if (this.ruleForm.studentId && this.ruleForm.courseId && this.ruleForm.termId) this.loadGradeRecord()
    },
    fetchMajors (deptId) {
      this.axios.get(`/major/findByDepartmentId/${deptId}`).then(r => { this.majors = r.data || [] })
    },
    fetchClasses (majorId) {
      this.axios.get(`/class/findByMajorId/${majorId}`).then(r => { this.classes = r.data || [] })
    },
    loadGradeRecord () {
      if (!this.ruleForm.studentId || !this.ruleForm.courseId || !this.ruleForm.termId) return
      const params = {
        studentId: this.ruleForm.studentId,
        courseId: this.ruleForm.courseId,
        termId: this.ruleForm.termId,
        status: 'PUBLISHED'
      }
      this.axios.post('/grade/query', params).then(resp => {
        const list = resp.data || []
        const row = list[0]
        if (row) {
          this.ruleForm.scoreId = row.id
          this.ruleForm.teacherId = row.teacherId
          this.ruleForm.teacherName = (row.teacherRealName || row.tname) || '—'
          if (row.departmentId) {
            this.ruleForm.departmentId = row.departmentId
            this.fetchMajors(row.departmentId)
          }
          if (row.majorId) {
            this.ruleForm.majorId = row.majorId
            this.fetchClasses(row.majorId)
          }
          if (row.classId) this.ruleForm.classId = row.classId
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
          this.$message.success('已加载已发布成绩，修改后提交将生成申请')
        } else {
          this.$message.warning('未找到该学生本课程本学期的已发布成绩，仅已发布成绩可申请修改')
          this.ruleForm.scoreId = null
          this.ruleForm.beforeUsualScore = null
          this.ruleForm.beforeMidScore = null
          this.ruleForm.beforeFinalScore = null
          this.ruleForm.beforeGrade = null
          this.ruleForm.usualScore = null
          this.ruleForm.midScore = null
          this.ruleForm.finalScore = null
          this.ruleForm.grade = null
        }
      }).catch(() => this.$message.error('查询成绩失败'))
    },
    submitForm (formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return
        if (!this.ruleForm.scoreId) {
          this.$message.warning('请先按学号、课程、学期加载已发布成绩')
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
        this.axios.post('/grade/change/request', {
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
      this.ruleForm.sname = ''
      this.ruleForm.studentId = null
      this.ruleForm.scoreId = null
      this.ruleForm.beforeUsualScore = null
      this.ruleForm.beforeMidScore = null
      this.ruleForm.beforeFinalScore = null
      this.ruleForm.beforeGrade = null
      this.ruleForm.attachmentPath = ''
      this.ruleForm.attachmentName = ''
      this.attachmentList = []
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
