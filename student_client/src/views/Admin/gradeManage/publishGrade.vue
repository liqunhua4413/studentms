<template>
  <div style="padding: 20px;">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成绩发布</span>
      </div>

      <el-form :model="form" :inline="true" label-width="80px" class="query-form">
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="全部" clearable filterable style="width: 160px;">
            <el-option v-for="c in courses" :key="c.id" :label="c.cname" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="form.departmentId" placeholder="全部" clearable style="width: 140px;" @change="handleDepartmentChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
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
        <el-form-item label="年级">
          <el-select v-model="form.gradeLevelId" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="g in gradeLevels" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传时间">
          <el-date-picker
            v-model="form.uploadTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 360px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="query">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <template v-if="list.length">
            <el-checkbox v-model="selectAll" style="margin-left: 16px;">全选</el-checkbox>
            <span class="batch-actions">
              <el-button
                type="text"
                style="color: #67c23a;"
                :loading="publishing"
                :disabled="!selectedIds.length"
                @click="publishSelected"
              >
                发布选中（{{ selectedIds.length }} 条）
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
          style="width: 100%"
          @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="55" :selectable="row => row.status === 'UPLOADED'" />
          <el-table-column prop="studentNo" label="学号" width="120" fixed />
          <el-table-column prop="sname" label="姓名" width="100" />
          <el-table-column prop="cname" label="课程名称" min-width="150" />
          <el-table-column prop="term" label="学期" width="120" />
          <el-table-column prop="departmentName" label="学院" width="120" />
          <el-table-column prop="majorName" label="专业" width="120" />
          <el-table-column prop="className" label="班级" width="120" />
          <el-table-column prop="gradeLevel" label="年级" width="100" />
          <el-table-column prop="usualScore" label="平时" width="80" align="right">
            <template slot-scope="scope">
              {{ formatScore(scope.row.usualScore) }}
            </template>
          </el-table-column>
          <el-table-column prop="midScore" label="期中" width="80" align="right">
            <template slot-scope="scope">
              {{ formatScore(scope.row.midScore) }}
            </template>
          </el-table-column>
          <el-table-column prop="finalScore" label="期末" width="80" align="right">
            <template slot-scope="scope">
              {{ formatScore(scope.row.finalScore) }}
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="总分" width="80" align="right">
            <template slot-scope="scope">
              {{ formatTotalScore(scope.row.grade) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="statusTagType(scope.row.status)" size="small">
                {{ scoreStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="上传时间" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        v-if="filteredList.length > pageSize"
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :total="filteredList.length"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right;"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script>
import { formatScore, formatTotalScore, scoreStatusLabel } from '@/utils/gradeFormat'

const SCORE_STATUS = {
  UPLOADED: 'UPLOADED',
  PUBLISHED: 'PUBLISHED',
  LOCKED: 'LOCKED'
}

export default {
  name: 'PublishGrade',
  data () {
    return {
      form: {
        courseId: null,
        departmentId: null,
        majorId: null,
        classId: null,
        gradeLevelId: null,
        uploadTimeRange: null
      },
      list: [],
      selectedRows: [],
      selectAll: false,
      page: 1,
      pageSize: 20,
      publishing: false,
      courses: [],
      departments: [],
      majors: [],
      classes: [],
      gradeLevels: [],
      sortProp: null,
      sortOrder: null
    }
  },
  watch: {
    selectAll (val) {
      this.$nextTick(() => {
        const t = this.$refs.table
        if (t) {
          if (val) {
            this.paginatedList.forEach(row => {
              if (row.status === SCORE_STATUS.UPLOADED) {
                t.toggleRowSelection(row, true)
              }
            })
          } else {
            t.clearSelection()
          }
        }
      })
    },
    paginatedList () {
      if (this.selectAll) this.$nextTick(() => this.applySelectAllToTable())
    }
  },
  computed: {
    selectedIds () {
      return (this.selectAll ? this.filteredList : (this.selectedRows || []))
        .filter(r => r.status === SCORE_STATUS.UPLOADED)
        .map(r => r.id)
    },
    filteredList () {
      let arr = this.list || []
      // 只显示 UPLOADED 状态的成绩
      arr = arr.filter(r => r.status === SCORE_STATUS.UPLOADED)
      
      // 按上传时间范围过滤
      if (this.form.uploadTimeRange && this.form.uploadTimeRange.length === 2) {
        const [startTime, endTime] = this.form.uploadTimeRange
        arr = arr.filter(r => {
          if (!r.createdAt) return false
          const uploadTime = new Date(r.createdAt).getTime()
          const start = new Date(startTime).getTime()
          const end = new Date(endTime).getTime()
          return uploadTime >= start && uploadTime <= end
        })
      }
      
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
    this.loadDepartments()
    this.loadCourses()
    this.loadGradeLevels()
    this.query()
  },
  methods: {
    formatScore,
    formatTotalScore,
    scoreStatusLabel,
    formatDateTime (dt) {
      if (!dt) return '—'
      try {
        const d = new Date(dt)
        return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
      } catch {
        return dt
      }
    },
    statusTagType (s) {
      if (s === SCORE_STATUS.PUBLISHED) return 'success'
      if (s === SCORE_STATUS.UPLOADED) return 'warning'
      if (s === SCORE_STATUS.LOCKED) return 'info'
      return ''
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
    loadGradeLevels () {
      this.axios.get('/gradeLevel/findAll').then(resp => {
        this.gradeLevels = resp.data || []
      }).catch(() => {})
    },
    buildQueryParams () {
      const p = { status: 'UPLOADED' } // 只查询已上传状态的成绩
      if (this.form.courseId) p.courseId = this.form.courseId
      if (this.form.departmentId) p.departmentId = this.form.departmentId
      if (this.form.gradeLevelId) p.gradeLevelId = this.form.gradeLevelId
      if (this.form.majorId) p.majorId = this.form.majorId
      if (this.form.classId) p.classId = this.form.classId
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
      this.doQuery(params)
    },
    doQuery (params) {
      this.axios.post('/grade/query', params).then(resp => {
        this.list = resp.data || []
        this.page = 1
        this.$message.success('查询成功，共 ' + this.list.length + ' 条已上传成绩')
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '查询失败')
      })
    },
    reset () {
      this.form = {
        courseId: null,
        departmentId: null,
        majorId: null,
        classId: null,
        gradeLevelId: null,
        uploadTimeRange: null
      }
      this.majors = []
      this.classes = []
      this.selectAll = false
      this.selectedRows = []
      this.$refs.table && this.$refs.table.clearSelection()
      this.query()
    },
    onSelectionChange (rows) {
      this.selectedRows = rows
      const allSelected = this.paginatedList.length > 0 && 
        this.paginatedList.filter(r => r.status === SCORE_STATUS.UPLOADED).every(r => 
          rows.some(sr => sr.id === r.id)
        )
      this.selectAll = allSelected
    },
    applySelectAllToTable () {
      if (!this.selectAll) return
      this.$nextTick(() => {
        const t = this.$refs.table
        if (t) {
          this.paginatedList.forEach(row => {
            if (row.status === SCORE_STATUS.UPLOADED) {
              t.toggleRowSelection(row, true)
            }
          })
        }
      })
    },
    handleSizeChange (val) {
      this.pageSize = val
      this.page = 1
    },
    handlePageChange (val) {
      this.page = val
    },
    publishSelected () {
      if (!this.selectedIds.length) {
        this.$message.warning('请选择要发布的成绩')
        return
      }
      this.$confirm(`确定要发布选中的 ${this.selectedIds.length} 条成绩吗？发布后学生将可见。`, '确认发布', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.publishing = true
        this.axios.post('/grade/publish', { scoreIds: this.selectedIds }).then(resp => {
          if (resp.data && resp.data.success) {
            this.$message.success(resp.data.message || '发布成功')
            this.query()
            this.selectAll = false
            this.selectedRows = []
            this.$refs.table && this.$refs.table.clearSelection()
          } else {
            this.$message.error(resp.data?.message || '发布失败')
          }
        }).catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || err.message || '发布失败')
        }).finally(() => {
          this.publishing = false
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.query-form {
  margin-bottom: 20px;
}
.table-scroll-wrapper {
  max-height: 600px;
  overflow: auto;
}
.batch-actions {
  margin-left: 16px;
}
.batch-actions .el-button {
  margin-left: 8px;
}
</style>
