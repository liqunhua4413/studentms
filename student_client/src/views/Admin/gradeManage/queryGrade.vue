<template>
  <div style="padding: 20px;">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成绩查询</span>
        <el-button
          v-if="userType === 'teacher' || userType === 'dean'"
          type="text"
          style="float: right;"
          @click="$router.push(userType === 'teacher' ? '/teacherMyGradeChangeRequests' : '/deanMyGradeChangeRequests')"
        >
          我的申请
        </el-button>
      </div>

      <el-form :model="form" :inline="true" label-width="80px" class="query-form">
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="学号" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="全部" clearable filterable style="width: 160px;">
            <el-option v-for="c in courses" :key="c.id" :label="c.cname" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="form.termId" placeholder="全部" clearable style="width: 140px;">
            <el-option label="所有学期" :value="null" />
            <el-option v-for="t in terms" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="已上传" value="UPLOADED" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="锁定" value="LOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="userType === 'admin'" label="学院">
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
        <el-form-item label="最低分">
          <el-input-number v-model="form.lowBound" :min="0" :max="100" :precision="2" :step="0.1" placeholder="不限制" controls-position="right" style="width: 120px;" :value-on-clear="null" />
        </el-form-item>
        <el-form-item label="最高分">
          <el-input-number v-model="form.highBound" :min="0" :max="100" :precision="2" :step="0.1" placeholder="不限制" controls-position="right" style="width: 120px;" :value-on-clear="null" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="query">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <template v-if="userType === 'admin' && list.length">
            <el-checkbox v-model="selectAll" style="margin-left: 16px;">全选</el-checkbox>
            <span class="batch-actions">
              <el-button type="text" :disabled="!exportRows.length" @click="exportExcel">导出 Excel</el-button>
              <el-button
                v-if="selectedUploadedIds.length"
                type="text"
                style="color: #67c23a;"
                :loading="publishing"
                @click="publishSelected"
              >
                发布选中（{{ selectedUploadedIds.length }} 条）
              </el-button>
              <el-button
                v-if="selectedLockableIds.length"
                type="text"
                style="color: #e6a23c;"
                :loading="locking"
                @click="lockSelected"
              >
                锁定选中（{{ selectedLockableIds.length }} 条）
              </el-button>
            </span>
          </template>
        </el-form-item>
      </el-form>

      <div class="table-scroll-wrapper">
        <el-table
          ref="table"
          :data="paginatedList"
          border
          stripe
          style="width: 100%; min-width: 1200px;"
          @selection-change="onSelectionChange"
          @sort-change="onSortChange"
        >
          <el-table-column v-if="userType === 'admin'" type="selection" width="48" :selectable="selectable" />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column prop="sname" label="姓名" width="90" />
          <el-table-column prop="departmentName" label="学院" width="120" show-overflow-tooltip />
          <el-table-column prop="gradeLevel" label="年级" width="90" />
          <el-table-column prop="majorName" label="专业" width="100" show-overflow-tooltip />
          <el-table-column prop="className" label="班级" width="90" />
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
  name: 'QueryGrade',
  data () {
    const userType = sessionStorage.getItem('type') || 'admin'
    const departmentId = sessionStorage.getItem('departmentId')
    return {
      userType,
      form: {
        studentNo: '',
        courseId: null,
        termId: null,
        status: '',
        departmentId: (userType === 'dean' && departmentId) ? parseInt(departmentId, 10) : null,
        gradeLevelId: null,
        majorId: null,
        classId: null,
        lowBound: null,
        highBound: null
      },
      departments: [],
      courses: [],
      terms: [],
      gradeLevels: [],
      majors: [],
      classes: [],
      list: [],
      selectedRows: [],
      selectAll: false,
      page: 1,
      pageSize: 20,
      publishing: false,
      locking: false,
      sortProp: null,
      sortOrder: null
    }
  },
  watch: {
    selectAll (val) {
      this.$nextTick(() => {
        const t = this.$refs.table
        if (!t) return
        if (val) this.applySelectAllToTable()
        else t.clearSelection()
      })
    },
    paginatedList () {
      if (this.selectAll) this.$nextTick(() => this.applySelectAllToTable())
    }
  },
  computed: {
    exportRows () {
      if (this.selectAll && this.filteredList.length) return this.filteredList
      return this.selectedRows || []
    },
    selectedUploadedIds () {
      return (this.selectAll ? this.filteredList : (this.selectedRows || []))
        .filter(r => r.status === SCORE_STATUS.UPLOADED).map(r => r.id)
    },
    selectedLockableIds () {
      return (this.selectAll ? this.filteredList : (this.selectedRows || []))
        .filter(r => r.status === SCORE_STATUS.PUBLISHED).map(r => r.id)
    },
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
      return (this.selectedRows || []).filter(r => r.status === SCORE_STATUS.UPLOADED).map(r => r.id)
    }
  },
  created () {
    this.loadDepartments()
    this.loadCourses()
    this.loadTerms()
    this.loadGradeLevels()
    if (this.userType === 'dean') {
      // 初始化院长的departmentId
      const depId = sessionStorage.getItem('departmentId')
      if (depId) {
        this.form.departmentId = parseInt(depId, 10)
        this.handleDepartmentChange(this.form.departmentId)
      }
    }
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
    loadDepartments () {
      if (this.userType === 'dean') {
        const id = sessionStorage.getItem('departmentId')
        if (!id) return
        this.axios.get(`/department/findById/${id}`).then(resp => {
          this.departments = resp.data ? [resp.data] : []
        }).catch(() => {})
      } else {
        this.axios.get('/department/findAll').then(resp => {
          this.departments = resp.data || []
        }).catch(() => {})
      }
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
    buildQueryParams () {
      const p = {}
      if (this.form.courseId) p.courseId = this.form.courseId
      if (this.form.termId) p.termId = this.form.termId
      if (this.form.status) p.status = this.form.status
      if (this.form.departmentId) p.departmentId = this.form.departmentId
      if (this.form.gradeLevelId) p.gradeLevelId = this.form.gradeLevelId
      if (this.form.majorId) p.majorId = this.form.majorId
      if (this.form.classId) p.classId = this.form.classId
      // 只有当值不为 null 且不为 0 时才传递分数限制
      if (this.form.lowBound != null && this.form.lowBound !== '' && this.form.lowBound > 0) {
        p.lowBound = this.form.lowBound
      }
      if (this.form.highBound != null && this.form.highBound !== '' && this.form.highBound > 0) {
        p.highBound = this.form.highBound
      }
      return p
    },
    handleDepartmentChange (val) {
      this.form.majorId = null
      this.form.classId = null
      this.majors = []
      this.classes = []
      if (val) {
        this.axios.get(`/major/findByDepartmentId/${val}`).then(resp => { this.majors = resp.data || [] }).catch(() => {})
      }
    },
    handleMajorChange (val) {
      this.form.classId = null
      this.classes = []
      if (val) {
        this.axios.get(`/class/findByMajorId/${val}`).then(resp => { this.classes = resp.data || [] }).catch(() => {})
      }
    },
    query () {
      const params = this.buildQueryParams()
      if (this.form.studentNo) {
        this.axios.post('/student/findByStudentNo', { studentNo: this.form.studentNo }).then(sResp => {
          if (sResp.data && sResp.data.id) {
            params.studentId = sResp.data.id
            this.doQuery(params)
          } else {
            this.$message.warning('未找到该学号')
          }
        }).catch(() => this.$message.error('查询学号失败'))
      } else {
        this.doQuery(params)
      }
    },
    doQuery (params) {
      this.axios.post('/grade/query', params).then(resp => {
        this.list = resp.data || []
        this.page = 1
        this.$message.success('查询成功，共 ' + this.list.length + ' 条')
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '查询失败')
      })
    },
    reset () {
      this.form.studentNo = ''
      this.form.courseId = null
      this.form.termId = null
      this.form.status = ''
      this.form.gradeLevelId = null
      this.form.majorId = null
      this.form.classId = null
      this.form.lowBound = null
      this.form.highBound = null
      if (this.userType !== 'dean') {
        this.form.departmentId = null
        this.majors = []
        this.classes = []
      } else if (this.form.departmentId) {
        this.majors = []
        this.classes = []
        this.handleDepartmentChange(this.form.departmentId)
      }
      this.list = []
      this.page = 1
      this.selectedRows = []
      this.selectAll = false
      this.$nextTick(() => {
        const t = this.$refs.table
        if (t) t.clearSelection()
      })
    },
    selectable () {
      return true
    },
    applySelectAllToTable () {
      const t = this.$refs.table
      if (!t || !this.paginatedList.length) return
      this.paginatedList.forEach(row => { t.toggleRowSelection(row, true) })
    },
    onSelectionChange (rows) {
      this.selectedRows = rows
      const pl = this.paginatedList || []
      this.selectAll = pl.length > 0 && rows.length === pl.length
    },
    publishSelected () {
      if (!this.selectedUploadedIds.length) {
        this.$message.warning('请勾选已上传成绩')
        return
      }
      this.$confirm(`确认发布 ${this.selectedUploadedIds.length} 条已上传成绩？`, '确认发布', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.publishing = true
        this.axios.post('/grade/publish', { scoreIds: this.selectedUploadedIds }).then(resp => {
          const d = resp.data || {}
          const ok = d.success === true
          this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '发布成功' : '发布失败'))
          if (ok) { this.query(); this.selectAll = false }
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '发布失败')
        }).finally(() => { this.publishing = false })
      }).catch(() => {})
    },
    publishOne (row) {
      if (row.status !== SCORE_STATUS.UPLOADED) return
      this.publishing = true
      this.axios.post('/grade/publish', { scoreIds: [row.id] }).then(resp => {
        const d = resp.data || {}
        const ok = d.success === true
        this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '发布成功' : '发布失败'))
        if (ok) this.query()
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '发布失败')
      }).finally(() => { this.publishing = false })
    },
    lockSelected () {
      if (!this.selectedLockableIds.length) {
        this.$message.warning('请勾选要锁定的成绩')
        return
      }
      this.$confirm(`确认锁定 ${this.selectedLockableIds.length} 条成绩？`, '确认锁定', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.locking = true
        this.axios.post('/grade/lock', { scoreIds: this.selectedLockableIds }).then(resp => {
          const d = resp.data || {}
          const ok = d.success === true
          this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '锁定成功' : '锁定失败'))
          if (ok) { this.query(); this.selectAll = false }
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '锁定失败')
        }).finally(() => { this.locking = false })
      }).catch(() => {})
    },
    lockOne (row) {
      if (row.status === SCORE_STATUS.LOCKED) return
      this.locking = true
      this.axios.post('/grade/lock', { scoreIds: [row.id] }).then(resp => {
        const d = resp.data || {}
        const ok = d.success === true
        this.$message[ok ? 'success' : 'warning'](d.message || (ok ? '锁定成功' : '锁定失败'))
        if (ok) this.query()
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '锁定失败')
      }).finally(() => { this.locking = false })
    },
    exportExcel () {
      const rows = this.exportRows
      if (!rows.length) {
        this.$message.warning(this.selectAll ? '当前无查询结果可导出' : '请勾选要导出的记录或勾选全选')
        return
      }
      this.doExport(rows)
    },
    doExport (rows) {
      const XLSX = require('xlsx')
      const headers = ['学号', '姓名', '学院', '年级', '专业', '班级', '课程', '教师', '平时', '期中', '期末', '总分', '备注', '学期', '状态']
      const data = rows.map(r => [
        r.studentNo || '',
        r.sname || '',
        r.departmentName || '',
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
.batch-actions { margin-left: 12px; }
.batch-actions .el-button { padding: 0 8px; margin-left: 4px; }
.table-scroll-wrapper {
  margin-top: 16px;
  max-height: 60vh;
  overflow: auto;
  width: 100%;
}
.table-scroll-wrapper >>> .el-table { margin-top: 0; }
</style>
