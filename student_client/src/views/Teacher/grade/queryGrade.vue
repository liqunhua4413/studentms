<template>
  <div style="padding: 20px;">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成绩查询</span>
      </div>

      <el-form :model="form" :inline="true" label-width="100px" class="query-form">
        <el-form-item label="学期">
          <el-select v-model="form.termId" placeholder="请先选择学期" clearable style="width: 140px;" @change="loadTeacherCourses">
            <el-option label="请选择学期" :value="null" />
            <el-option v-for="t in terms" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="请先选择学期" clearable filterable style="width: 180px;" :disabled="!form.termId">
            <el-option v-for="c in courses" :key="c.id" :label="c.cname" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="请先选择学期、课程" clearable style="width: 140px;" :disabled="!form.termId || !form.courseId" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.studentName" placeholder="姓名（模糊）" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="已上传" value="UPLOADED" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="锁定" value="LOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院（本院）">
          <el-select v-model="form.departmentId" placeholder="本院" disabled style="width: 140px;">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生学院">
          <el-select v-model="form.studentCollegeId" placeholder="全部" clearable style="width: 140px;" @change="handleStudentCollegeChange">
            <el-option v-for="c in studentColleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生专业">
          <el-select v-model="form.studentMajorId" placeholder="全部" clearable filterable style="width: 140px;" @change="handleStudentMajorChange">
            <el-option v-for="m in studentMajors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生班级">
          <el-select v-model="form.studentClassId" placeholder="全部" clearable filterable style="width: 120px;">
            <el-option v-for="cls in studentClasses" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="最低分">
          <el-input-number v-model="form.lowBound" :min="0" :max="100" :precision="2" :step="0.1" placeholder="不限制" controls-position="right" style="width: 120px;" :value-on-clear="null" />
        </el-form-item>
        <el-form-item label="最高分">
          <el-input-number v-model="form.highBound" :min="0" :max="100" :precision="2" :step="0.1" placeholder="不限制" controls-position="right" style="width: 120px;" :value-on-clear="null" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="query">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button v-if="list.length" type="text" :disabled="!list.length" @click="exportExcel">导出 Excel</el-button>
        </el-form-item>
      </el-form>

      <div class="table-scroll-wrapper">
        <el-table
          ref="table"
          :data="paginatedList"
          border
          stripe
          style="width: 100%; min-width: 1200px;"
          @sort-change="onSortChange"
        >
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column prop="sname" label="姓名" width="90" />
          <el-table-column prop="studentDepartmentName" label="学生学院" width="120" show-overflow-tooltip />
          <el-table-column prop="gradeLevel" label="年级" width="90" />
          <el-table-column prop="majorName" label="学生专业" width="100" show-overflow-tooltip />
          <el-table-column prop="className" label="学生班级" width="90" />
          <el-table-column prop="cname" label="课程" width="120" show-overflow-tooltip />
          <el-table-column prop="tname" label="教师" width="90">
            <template slot-scope="scope">{{ scope.row.teacherRealName || scope.row.tname || '—' }}</template>
          </el-table-column>
          <el-table-column prop="usualScore" label="平时" width="80" sortable="custom">
            <template slot-scope="scope">{{ formatScore(scope.row.usualScore) }}</template>
          </el-table-column>
          <el-table-column prop="midScore" label="期中" width="80" sortable="custom">
            <template slot-scope="scope">{{ formatScore(scope.row.midScore) }}</template>
          </el-table-column>
          <el-table-column prop="finalScore" label="期末" width="80" sortable="custom">
            <template slot-scope="scope">{{ formatScore(scope.row.finalScore) }}</template>
          </el-table-column>
          <el-table-column prop="grade" label="总分" width="80" sortable="custom">
            <template slot-scope="scope">{{ formatTotalScore(scope.row.grade) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
          <el-table-column prop="term" label="学期" width="110" />
          <el-table-column prop="status" label="状态" width="90">
            <template slot-scope="scope">
              <el-tag :type="statusTagType(scope.row.status)" size="small">
                {{ scoreStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :total="filteredList.length"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px;"
        @size-change="pageSize = $event"
        @current-change="page = $event"
      />
    </el-card>
  </div>
</template>

<script>
import { formatScore, formatTotalScore, scoreStatusLabel, SCORE_STATUS } from '@/utils/gradeFormat'

export default {
  name: 'TeacherQueryGrade',
  data () {
    return {
      form: {
        termId: null,
        courseId: null,
        studentNo: '',
        studentName: '',
        status: '',
        departmentId: null,
        studentCollegeId: null,
        studentMajorId: null,
        studentClassId: null,
        lowBound: null,
        highBound: null
      },
      departments: [],
      courses: [],
      terms: [],
      studentColleges: [],
      studentMajors: [],
      studentClasses: [],
      list: [],
      page: 1,
      pageSize: 20,
      sortProp: null,
      sortOrder: null
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
    }
  },
  created () {
    this.loadTeacherDepartment()
    this.loadTerms()
    this.loadStudentColleges()
  },
  methods: {
    formatScore,
    formatTotalScore,
    scoreStatusLabel,
    statusTagType (s) {
      if (s === SCORE_STATUS.PUBLISHED) return 'success'
      if (s === SCORE_STATUS.UPLOADED) return 'warning'
      if (s === SCORE_STATUS.LOCKED) return 'info'
      return ''
    },
    loadTeacherDepartment () {
      const tid = sessionStorage.getItem('tid')
      if (!tid) return
      this.axios.get('/teacher/findById/' + tid).then(resp => {
        const teacher = resp.data
        if (teacher && teacher.departmentId != null) {
          const id = teacher.departmentId
          const name = (teacher.departmentName != null && teacher.departmentName !== '')
            ? teacher.departmentName
            : ('学院' + id)
          this.departments = [{ id, name }]
          this.form.departmentId = id
        }
      }).catch(() => {
        const deptId = sessionStorage.getItem('departmentId')
        if (deptId) {
          const cid = parseInt(deptId, 10)
          if (!Number.isNaN(cid)) {
            this.departments = [{ id: cid, name: '学院' + cid }]
            this.form.departmentId = cid
          }
        }
      })
    },
    loadTeacherCourses () {
      this.form.courseId = null
      this.courses = []
      const tid = sessionStorage.getItem('tid')
      const termId = this.form.termId
      if (!tid || !termId) return
      this.axios.get('/courseTeacher/findMyCourse/' + tid + '/' + termId).then(resp => {
        this.courses = resp.data || []
      }).catch(() => { this.courses = [] })
    },
    loadTerms () {
      this.axios.get('/term/findAll').then(resp => {
        this.terms = resp.data || []
      }).catch(() => { this.terms = [] })
    },
    loadStudentColleges () {
      this.axios.get('/student/colleges').then(resp => {
        this.studentColleges = resp.data || []
      }).catch(() => {})
    },
    handleStudentCollegeChange (val) {
      this.form.studentMajorId = null
      this.form.studentClassId = null
      this.studentMajors = []
      this.studentClasses = []
      if (val) {
        this.axios.get('/student/majors', { params: { collegeId: val } }).then(resp => {
          this.studentMajors = resp.data || []
        }).catch(() => {})
      }
    },
    handleStudentMajorChange (val) {
      this.form.studentClassId = null
      this.studentClasses = []
      if (val) {
        this.axios.get('/student/classes', { params: { majorId: val } }).then(resp => {
          this.studentClasses = resp.data || []
        }).catch(() => {})
      }
    },
    buildQueryParams () {
      const p = {}
      if (this.form.termId) p.term = this.form.termId
      if (this.form.courseId) p.courseId = this.form.courseId
      if (this.form.studentNo) p.studentNo = this.form.studentNo
      if (this.form.studentName) p.studentName = this.form.studentName
      if (this.form.status) p.status = this.form.status
      if (this.form.studentMajorId) p.majorId = this.form.studentMajorId
      if (this.form.studentClassId) p.classId = this.form.studentClassId
      if (this.form.lowBound != null && this.form.lowBound !== '' && this.form.lowBound > 0) p.lowBound = this.form.lowBound
      if (this.form.highBound != null && this.form.highBound !== '' && this.form.highBound > 0) p.highBound = this.form.highBound
      return p
    },
    query () {
      if (!this.form.termId || !this.form.courseId) {
        this.$message.warning('请先选择学期和课程')
        return
      }
      if (this.form.studentNo && (!this.form.termId || !this.form.courseId)) {
        this.$message.warning('按学号查询时请先选择学期和课程')
        return
      }
      const doQuery = (params) => {
        this.axios.post('/grade/query', params).then(resp => {
          this.list = resp.data || []
          this.page = 1
          this.$message.success('查询成功，共 ' + this.list.length + ' 条')
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '查询失败')
        })
      }
      const params = this.buildQueryParams()
      if (this.form.studentNo) {
        this.axios.post('/student/findByStudentNo', { studentNo: this.form.studentNo }).then(resp => {
          if (resp.data && resp.data.id) {
            params.studentId = resp.data.id
            doQuery(params)
          } else {
            this.$message.warning('未找到学号为【' + this.form.studentNo + '】的学生')
          }
        }).catch(() => { this.$message.error('查询学生失败') })
      } else {
        doQuery(params)
      }
    },
    reset () {
      this.form.termId = null
      this.form.courseId = null
      this.form.studentNo = ''
      this.form.studentName = ''
      this.form.status = ''
      this.form.studentCollegeId = null
      this.form.studentMajorId = null
      this.form.studentClassId = null
      this.form.lowBound = null
      this.form.highBound = null
      this.courses = []
      this.studentMajors = []
      this.studentClasses = []
      this.list = []
      this.page = 1
      this.loadStudentColleges()
    },
    exportExcel () {
      const rows = this.filteredList
      if (!rows.length) {
        this.$message.warning('无查询结果可导出')
        return
      }
      const XLSX = require('xlsx')
      const headers = ['学号', '姓名', '学生学院', '年级', '学生专业', '学生班级', '课程', '教师', '平时', '期中', '期末', '总分', '备注', '学期', '状态']
      const data = rows.map(r => [
        r.studentNo || '',
        r.sname || '',
        r.studentDepartmentName || r.departmentName || '',
        r.gradeLevel || '',
        r.majorName || '',
        r.className || '',
        r.cname || '',
        (r.teacherRealName || r.tname) || '',
        this.formatScore(r.usualScore),
        this.formatScore(r.midScore),
        this.formatScore(r.finalScore),
        this.formatTotalScore(r.grade),
        r.remark || '',
        r.term || '',
        this.scoreStatusLabel(r.status)
      ])
      const ws = XLSX.utils.aoa_to_sheet([headers, ...data])
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, '成绩列表')
      XLSX.writeFile(wb, '成绩查询结果.xlsx')
      this.$message.success('导出成功')
    },
    onSortChange ({ prop, order }) {
      this.sortProp = prop || null
      this.sortOrder = order || null
    }
  }
}
</script>

<style scoped>
.query-form { margin-bottom: 0; }
.table-scroll-wrapper {
  margin-top: 16px;
  max-height: 60vh;
  overflow: auto;
  width: 100%;
}
.table-scroll-wrapper >>> .el-table { margin-top: 0; }
</style>
