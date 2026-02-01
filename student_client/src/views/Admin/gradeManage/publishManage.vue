<template>
  <div style="padding: 20px;">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成绩发布/锁定管理</span>
      </div>

      <el-form :model="form" :inline="true" label-width="80px" class="query-form">
        <el-form-item label="学期">
          <el-select v-model="form.termId" placeholder="全部" clearable style="width: 140px;">
            <el-option label="所有学期" :value="null" />
            <el-option v-for="t in terms" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="form.departmentId" placeholder="全部" clearable style="width: 140px;" @change="handleDepartmentChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.gradeLevelId" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="g in gradeLevels" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="form.majorId" placeholder="全部" clearable filterable style="width: 140px;" @change="handleMajorChange">
            <el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.classId" placeholder="全部" clearable filterable style="width: 120px;">
            <el-option v-for="cls in classes" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="全部" clearable filterable style="width: 160px;">
            <el-option v-for="c in courses" :key="c.id" :label="c.cname" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="form.teacherId" placeholder="全部" clearable filterable style="width: 140px;">
            <el-option v-for="t in teachers" :key="t.id" :label="t.tname" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="学号" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.studentName" placeholder="学生姓名" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="已上传" value="UPLOADED" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="锁定" value="LOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="query">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button
            v-if="selectedUploadedIds.length"
            type="success"
            :loading="publishing"
            @click="publishSelected"
          >
            发布选中（{{ selectedUploadedIds.length }} 条已上传）
          </el-button>
          <el-button
            v-if="selectedIds.length"
            type="warning"
            :loading="locking"
            @click="lockSelected"
          >
            锁定选中（{{ selectedIds.length }} 条）
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
        ref="table"
        :data="paginatedList"
        border
        stripe
        style="width: 100%; margin-top: 16px;"
        @selection-change="onSelectionChange"
        @sort-change="onSortChange"
      >
        <el-table-column type="selection" width="48" :selectable="selectable" />
        <el-table-column prop="cname" label="课程名称" width="150" show-overflow-tooltip />
        <el-table-column prop="departmentName" label="学院" width="120" show-overflow-tooltip />
        <el-table-column prop="majorName" label="专业" width="120" show-overflow-tooltip />
        <el-table-column prop="term" label="学期" width="120" />
        <el-table-column prop="className" label="班级" width="100" />
        <el-table-column prop="gradeLevel" label="年级" width="90" />
        <el-table-column prop="tname" label="任课教师" width="100">
          <template slot-scope="scope">{{ scope.row.teacherRealName || scope.row.tname }}</template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="110" />
        <el-table-column prop="sname" label="学生姓名" width="90" />
        <el-table-column prop="usualScore" label="平时" width="80">
          <template slot-scope="scope">{{ formatScore(scope.row.usualScore) }}</template>
        </el-table-column>
        <el-table-column prop="midScore" label="期中" width="80">
          <template slot-scope="scope">{{ formatScore(scope.row.midScore) }}</template>
        </el-table-column>
        <el-table-column prop="finalScore" label="期末" width="80">
          <template slot-scope="scope">{{ formatScore(scope.row.finalScore) }}</template>
        </el-table-column>
        <el-table-column prop="grade" label="总分" width="80" sortable="custom">
          <template slot-scope="scope">{{ formatTotalScore(scope.row.grade) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">
              {{ scoreStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="最近导入时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 'UPLOADED'"
              size="mini"
              type="success"
              @click="publishOne(scope.row)"
            >
              发布
            </el-button>
            <el-button
              v-if="scope.row.status !== 'LOCKED'"
              size="mini"
              type="warning"
              @click="lockOne(scope.row)"
            >
              锁定
            </el-button>
            <el-button
              v-if="scope.row.status === 'LOCKED'"
              size="mini"
              type="danger"
              @click="forceEdit(scope.row)"
            >
              强制修正
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="list.length"
        style="margin-top: 16px;"
      />
    </el-card>

    <!-- 强制修正对话框 -->
    <el-dialog
      title="强制修正（锁定成绩）"
      :visible.sync="forceEditDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="forceEditForm" label-width="120px">
        <el-form-item label="学生">
          <span>{{ forceEditForm.studentName }}（{{ forceEditForm.studentNo }}）</span>
        </el-form-item>
        <el-form-item label="课程">
          <span>{{ forceEditForm.courseName }}</span>
        </el-form-item>
        <el-form-item label="学期">
          <span>{{ forceEditForm.term }}</span>
        </el-form-item>
        <el-form-item label="原成绩（只读）">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input v-model="forceEditForm.originalUsualScore" disabled>
                <template slot="prepend">平时</template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-input v-model="forceEditForm.originalMidScore" disabled>
                <template slot="prepend">期中</template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-input v-model="forceEditForm.originalFinalScore" disabled>
                <template slot="prepend">期末</template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-input v-model="forceEditForm.originalGrade" disabled>
                <template slot="prepend">总分</template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="修正成绩">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input-number
                v-model="forceEditForm.usualScore"
                :min="0"
                :max="100"
                :precision="2"
                :step="0.1"
                style="width: 100%;"
              >
                <template slot="prepend">平时</template>
              </el-input-number>
            </el-col>
            <el-col :span="6">
              <el-input-number
                v-model="forceEditForm.midScore"
                :min="0"
                :max="100"
                :precision="2"
                :step="0.1"
                style="width: 100%;"
              >
                <template slot="prepend">期中</template>
              </el-input-number>
            </el-col>
            <el-col :span="6">
              <el-input-number
                v-model="forceEditForm.finalScore"
                :min="0"
                :max="100"
                :precision="2"
                :step="0.1"
                style="width: 100%;"
              >
                <template slot="prepend">期末</template>
              </el-input-number>
            </el-col>
            <el-col :span="6">
              <el-input-number
                v-model="forceEditForm.grade"
                :min="0"
                :max="100"
                :precision="0"
                :step="1"
                style="width: 100%;"
              >
                <template slot="prepend">总分</template>
              </el-input-number>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="修正原因" required>
          <el-input
            v-model="forceEditForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请详细说明强制修正的原因（必填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="forceEditDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitForceEdit" :loading="forceEditing">确认修正</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatScore, formatTotalScore, formatDateTime, scoreStatusLabel, SCORE_STATUS } from '@/utils/gradeFormat'

export default {
  name: 'PublishManage',
  data () {
    return {
      form: {
        termId: null,
        departmentId: null,
        gradeLevelId: null,
        majorId: null,
        classId: null,
        courseId: null,
        teacherId: null,
        studentNo: '',
        studentName: '',
        status: ''
      },
      departments: [],
      gradeLevels: [],
      majors: [],
      classes: [],
      courses: [],
      teachers: [],
      terms: [],
      list: [],
      selectedRows: [],
      page: 1,
      pageSize: 20,
      sortProp: null,
      sortOrder: null,
      publishing: false,
      locking: false,
      forceEditDialogVisible: false,
      forceEditing: false,
      forceEditForm: {
        scoreId: null,
        studentName: '',
        studentNo: '',
        courseName: '',
        term: '',
        originalUsualScore: '',
        originalMidScore: '',
        originalFinalScore: '',
        originalGrade: '',
        usualScore: null,
        midScore: null,
        finalScore: null,
        grade: null,
        reason: ''
      }
    }
  },
  computed: {
    filteredList () {
      let arr = this.list || []
      if (this.sortProp && this.sortOrder) {
        arr = [...arr].sort((a, b) => {
          const va = a[this.sortProp]
          const vb = b[this.sortProp]
          const na = Number(va)
          const nb = Number(vb)
          if (!Number.isNaN(na) && !Number.isNaN(nb)) return this.sortOrder === 'ascending' ? na - nb : nb - na
          const sa = String(va ?? '')
          const sb = String(vb ?? '')
          return this.sortOrder === 'ascending' ? sa.localeCompare(sb) : sb.localeCompare(sa)
        })
      }
      return arr
    },
    paginatedList () {
      const start = (this.page - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    },
    selectedIds () {
      return (this.selectedRows || []).map(r => r.id)
    },
    selectedUploadedIds () {
      return (this.selectedRows || []).filter(r => r.status === SCORE_STATUS.UPLOADED).map(r => r.id)
    }
  },
  created () {
    this.loadDepartments()
    this.loadCourses()
    this.loadTerms()
    this.loadGradeLevels()
    this.loadTeachers()
  },
  methods: {
    formatScore,
    formatTotalScore,
    formatDateTime,
    scoreStatusLabel,
    statusTagType (s) {
      if (s === SCORE_STATUS.PUBLISHED) return 'success'
      if (s === SCORE_STATUS.UPLOADED) return 'warning'
      if (s === SCORE_STATUS.LOCKED) return 'info'
      return ''
    },
    selectable (row) {
      // 可以选择已上传、已发布、锁定状态的成绩进行发布或锁定
      return true
    },
    loadDepartments () {
      this.axios.get('/department/findAll').then(resp => {
        this.departments = resp.data || []
      }).catch(() => {})
    },
    loadCourses () {
      this.axios.get('/course/findAll').then(resp => {
        this.courses = resp.data || []
      }).catch(() => {})
    },
    loadTerms () {
      this.axios.get('/term/findAll').then(resp => {
        this.terms = resp.data || []
      }).catch(() => { this.terms = [] })
    },
    loadGradeLevels () {
      this.axios.get('/gradeLevel/findAll').then(resp => {
        this.gradeLevels = resp.data || []
      }).catch(() => {})
    },
    loadTeachers () {
      this.axios.get('/teacher/findAll').then(resp => {
        this.teachers = resp.data || []
      }).catch(() => {})
    },
    handleDepartmentChange (val) {
      this.form.majorId = null
      this.form.classId = null
      this.majors = []
      this.classes = []
      if (val) {
        this.axios.get(`/major/findByDepartmentId/${val}`).then(resp => {
          this.majors = resp.data || []
        }).catch(() => {})
      }
    },
    handleMajorChange (val) {
      this.form.classId = null
      this.classes = []
      if (val) {
        this.axios.get(`/class/findByMajorId/${val}`).then(resp => {
          this.classes = resp.data || []
        }).catch(() => {})
      }
    },
    buildQueryParams () {
      const p = {}
      if (this.form.courseId) p.courseId = this.form.courseId
      if (this.form.termId) p.termId = this.form.termId
      if (this.form.status) p.status = this.form.status
      if (this.form.departmentId) p.departmentId = this.form.departmentId
      if (this.form.gradeLevelId) p.gradeLevelId = this.form.gradeLevelId
      if (this.form.majorId) p.majorId = this.form.majorId
      if (this.form.classId) p.classId = this.form.classId
      if (this.form.teacherId) p.teacherId = this.form.teacherId
      if (this.form.studentNo) {
        // 先查询学生ID
        return this.axios.post('/student/findByStudentNo', { studentNo: this.form.studentNo }).then(studentResp => {
          if (studentResp.data && studentResp.data.id) {
            p.studentId = studentResp.data.id
          }
          if (this.form.studentName) {
            // 如果同时有学号和姓名，需要进一步过滤
          }
          return p
        }).catch(() => p)
      }
      return Promise.resolve(p)
    },
    query () {
      this.buildQueryParams().then(params => {
        this.axios.post('/grade/query', params).then(resp => {
          let results = resp.data || []
          // 如果指定了学生姓名，进行前端过滤
          if (this.form.studentName) {
            const name = this.form.studentName.toLowerCase()
            results = results.filter(r => r.sname && r.sname.toLowerCase().includes(name))
          }
          this.list = results
          this.page = 1
          this.$message.success('查询成功，共 ' + this.list.length + ' 条')
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '查询失败')
        })
      })
    },
    reset () {
      this.form = {
        termId: null,
        departmentId: null,
        gradeLevelId: null,
        majorId: null,
        classId: null,
        courseId: null,
        teacherId: null,
        studentNo: '',
        studentName: '',
        status: ''
      }
      this.majors = []
      this.classes = []
      this.list = []
      this.page = 1
      this.selectedRows = []
    },
    onSelectionChange (rows) {
      this.selectedRows = rows
    },
    onSortChange ({ prop, order }) {
      this.sortProp = prop
      this.sortOrder = order === 'ascending' ? 'ascending' : (order === 'descending' ? 'descending' : null)
    },
    publishSelected () {
      if (!this.selectedUploadedIds.length) {
        this.$message.warning('请先选择要发布的已上传成绩')
        return
      }
      this.$confirm(`确认发布 ${this.selectedUploadedIds.length} 条已上传成绩？发布后学生可见。`, '确认发布', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.publishing = true
        const ids = this.selectedUploadedIds
        this.axios.post('/grade/publish', { scoreIds: ids }).then(resp => {
          const d = resp.data || {}
          const ok = d.success === true
          this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '发布成功' : '发布失败'))
          if (ok) {
            this.query()
            this.selectedRows = []
          }
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '发布失败')
        }).finally(() => {
          this.publishing = false
        })
      }).catch(() => {})
    },
    publishOne (row) {
      this.selectedRows = [row]
      this.publishSelected()
    },
    lockSelected () {
      if (!this.selectedIds.length) {
        this.$message.warning('请先选择要锁定的成绩')
        return
      }
      this.$confirm(`确认锁定 ${this.selectedIds.length} 条成绩？锁定后将禁止修改。`, '确认锁定', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.locking = true
        this.axios.post('/grade/lock', { scoreIds: this.selectedIds }).then(resp => {
          const d = resp.data || {}
          const ok = d.success === true
          this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '锁定成功' : '锁定失败'))
          if (ok) {
            this.query()
            this.selectedRows = []
          }
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '锁定失败')
        }).finally(() => {
          this.locking = false
        })
      }).catch(() => {})
    },
    lockOne (row) {
      this.selectedRows = [row]
      this.lockSelected()
    },
    forceEdit (row) {
      this.forceEditForm = {
        scoreId: row.id,
        studentName: row.sname,
        studentNo: row.studentNo,
        courseName: row.cname || row.courseName,
        term: row.term,
        originalUsualScore: row.usualScore != null ? parseFloat(row.usualScore).toFixed(2) : '—',
        originalMidScore: row.midScore != null ? parseFloat(row.midScore).toFixed(2) : '—',
        originalFinalScore: row.finalScore != null ? parseFloat(row.finalScore).toFixed(2) : '—',
        originalGrade: row.grade != null ? Math.round(parseFloat(row.grade)).toString() : '—',
        usualScore: row.usualScore != null ? parseFloat(row.usualScore) : null,
        midScore: row.midScore != null ? parseFloat(row.midScore) : null,
        finalScore: row.finalScore != null ? parseFloat(row.finalScore) : null,
        grade: row.grade != null ? Math.round(parseFloat(row.grade)) : null,
        reason: ''
      }
      this.forceEditDialogVisible = true
    },
    submitForceEdit () {
      if (!this.forceEditForm.reason || !this.forceEditForm.reason.trim()) {
        this.$message.warning('请填写修正原因')
        return
      }
      this.$confirm('确认强制修正该锁定成绩？此操作将记录超高等级日志。', '确认修正', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.forceEditing = true
        this.axios.post('/grade/force-edit', {
          scoreId: this.forceEditForm.scoreId,
          usualScore: this.forceEditForm.usualScore,
          midScore: this.forceEditForm.midScore,
          finalScore: this.forceEditForm.finalScore,
          grade: this.forceEditForm.grade,
          reason: this.forceEditForm.reason.trim()
        }).then(resp => {
          const d = resp.data || {}
          const ok = d.success === true
          this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '修正成功' : '修正失败'))
          if (ok) {
            this.forceEditDialogVisible = false
            this.query()
          }
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '修正失败')
        }).finally(() => {
          this.forceEditing = false
        })
      }).catch(() => {})
    },
    handleSizeChange (val) {
      this.pageSize = val
      this.page = 1
    },
    handleCurrentChange (val) {
      this.page = val
    }
  }
}
</script>

<style scoped>
.query-form {
  margin-bottom: 16px;
}
</style>
