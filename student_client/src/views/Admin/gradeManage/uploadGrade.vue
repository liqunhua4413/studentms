<template>
  <div style="padding: 20px;">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>成绩上传</span>
      </div>
      <!-- 权限说明：管理员选学院；院长/教师固定本院 -->
      <div v-if="userType === 'admin'" style="margin-bottom: 20px;">
        <el-form :inline="true">
          <el-form-item label="选择学院" required>
            <el-select v-model="selectedDepartmentId" placeholder="请选择学院" clearable>
              <el-option
                v-for="item in departments"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div v-else style="margin-bottom: 20px;">
        <el-alert
          :title="'当前学院：' + (currentDepartmentName || '未知')"
          type="info"
          :closable="false"
          show-icon
        />
        <p class="tips">
          <span v-if="userType === 'teacher'">教师仅可上传本人任课课程（course_open）。</span>
          <span v-else-if="userType === 'dean'">院长仅可上传本学院课程（course.department_id）。</span>
        </p>
      </div>

      <el-upload
        ref="upload"
        class="upload-demo"
        :auto-upload="false"
        :file-list="fileList"
        :on-change="onFileChange"
        :on-remove="onFileRemove"
        accept=".xlsx,.xls"
        :limit="1"
      >
        <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
        <el-button size="small" type="success" :loading="uploading" :disabled="!canUpload" @click="submitUpload">
          上传到服务器
        </el-button>
        <el-button size="small" type="info" @click="downloadTemplate">下载成绩单模板</el-button>
        <div slot="tip" class="el-upload__tip">
          <p>仅支持 Excel（.xlsx / .xls）。上传后成绩<strong>已入库</strong>，状态为<strong>已上传</strong>，等待管理员审核发布后学生可见。</p>
          <p>上传前会检查学号+课程+学期是否已有成绩；若已存在将拒绝上传并提示提交修改申请。</p>
          <p>格式要求：第2行课程/教师，第3行学期/班级等，第4行起为学生成绩。</p>
        </div>
      </el-upload>

      <div v-if="uploadResult" style="margin-top: 20px; white-space: pre-line;">
        <el-alert
          :title="uploadResult"
          :type="uploadSuccess ? 'success' : 'error'"
          :closable="true"
          show-icon
          @close="uploadResult = ''"
        />
      </div>

      <div style="margin-top: 40px;">
        <h3>已上传文件列表</h3>
        <el-table :data="records" border stripe style="width: 100%">
          <el-table-column prop="fileName" label="文件名" min-width="200" />
          <el-table-column prop="term" label="学期" width="120" />
          <el-table-column prop="operator" label="操作人" width="120" />
          <el-table-column prop="message" label="说明" min-width="180" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="上传时间" width="160">
            <template slot-scope="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="downloadRecord(scope.row)">下载</el-button>
              <el-button v-if="userType === 'admin'" size="mini" type="danger" @click="deleteRecord(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { formatDateTime } from '@/utils/gradeFormat'

export default {
  name: 'UploadGrade',
  data () {
    const userType = sessionStorage.getItem('type') || 'admin'
    const departmentId = sessionStorage.getItem('departmentId')
    return {
      userType,
      selectedDepartmentId: (userType === 'dean' || userType === 'teacher') && departmentId
        ? parseInt(departmentId, 10) : null,
      departments: [],
      currentDepartmentName: null,
      fileList: [],
      uploadResult: '',
      uploadSuccess: false,
      uploading: false,
      records: []
    }
  },
  computed: {
    canUpload () {
      if (!this.selectedDepartmentId) return false
      if (this.fileList.length === 0) return false
      const f = this.fileList[0]
      if (!f || !f.raw) return false
      const n = (f.name || '').toLowerCase()
      return n.endsWith('.xlsx') || n.endsWith('.xls')
    }
  },
  created () {
    if (this.userType === 'admin') {
      this.fetchDepartments()
    } else if (this.userType === 'dean') {
      this.fetchDeanCollege()
    } else {
      const depId = sessionStorage.getItem('departmentId')
      if (depId) {
        this.selectedDepartmentId = parseInt(depId, 10)
        this.fetchDepartmentName(depId)
      } else {
        this.fetchCurrentUserDepartment()
      }
    }
    this.fetchRecords()
  },
  methods: {
    formatDateTime,
    fetchDepartments () {
      this.axios.get('/department/findAll').then(resp => {
        this.departments = resp.data || []
      }).catch(() => {
        this.$message.error('加载学院列表失败')
      })
    },
    fetchDepartmentName (id) {
      this.axios.get(`/department/findById/${id}`).then(resp => {
        if (resp.data && resp.data.name) {
          this.currentDepartmentName = resp.data.name
          if (!this.selectedDepartmentId) {
            this.selectedDepartmentId = resp.data.id
          }
        }
      }).catch(() => {})
    },
    fetchCurrentUserDepartment () {
      const depId = sessionStorage.getItem('departmentId')
      if (!depId) return
      this.axios.get('/department/findAll').then(resp => {
        const depts = resp.data || []
        const dept = depts.find(d => d.id === parseInt(depId, 10))
        if (dept) {
          this.currentDepartmentName = dept.name
          this.selectedDepartmentId = dept.id
        }
      }).catch(() => {})
    },
    fetchDeanCollege () {
      this.axios.get('/dean/college').then(resp => {
        const d = resp.data
        if (d && d.collegeId != null) {
          this.selectedDepartmentId = typeof d.collegeId === 'number' ? d.collegeId : parseInt(d.collegeId, 10)
          this.currentDepartmentName = d.collegeName || null
          sessionStorage.setItem('departmentId', String(this.selectedDepartmentId))
        }
      }).catch(() => {
        const depId = sessionStorage.getItem('departmentId')
        if (depId) {
          this.selectedDepartmentId = parseInt(depId, 10)
          this.currentDepartmentName = null
        }
      })
    },
    fetchRecords () {
      this.axios.get('/grade/records').then(resp => {
        this.records = resp.data || []
      }).catch(() => {})
    },
    onFileChange (file, list) {
      this.fileList = list.slice(-1)
      const n = (file.name || '').toLowerCase()
      if (n && !n.endsWith('.xlsx') && !n.endsWith('.xls')) {
        this.$message.warning('仅支持 .xlsx / .xls 格式')
        this.fileList = []
      }
    },
    onFileRemove () {
      this.fileList = []
    },
    submitUpload () {
      if (!this.selectedDepartmentId) {
        this.$message.warning(this.userType === 'admin' ? '请先选择学院' : '无法获取学院信息，请重新登录')
        return
      }
      if (!this.fileList.length || !this.fileList[0].raw) {
        this.$message.warning('请先选择 Excel 文件')
        return
      }
      const file = this.fileList[0].raw
      const form = new FormData()
      form.append('file', file)
      form.append('departmentId', this.selectedDepartmentId)
      this.uploading = true
      this.uploadResult = ''
      this.axios.post('/grade/upload', form).then(resp => {
        const msg = typeof resp.data === 'string' ? resp.data : (resp.data?.message || '上传成功')
        this.uploadSuccess = msg.indexOf('失败') === -1 && msg.indexOf('拒绝') === -1 && msg.indexOf('错误') === -1
        this.uploadResult = msg
        if (this.uploadSuccess) {
          this.$message.success('上传完成')
          this.fileList = []
          this.fetchRecords()
        } else {
          this.$message.error(msg)
        }
      }).catch(err => {
        const msg = (err.response && err.response.data && err.response.data.message) || err.message || '上传失败'
        this.uploadResult = msg
        this.uploadSuccess = false
        this.$message.error('上传失败')
      }).finally(() => {
        this.uploading = false
      })
    },
    downloadTemplate () {
      this.axios.get('/grade/template', { responseType: 'blob' }).then(resp => {
        const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = '成绩单批量导入模板.xlsx'
        a.click()
        window.URL.revokeObjectURL(url)
      }).catch(() => this.$message.error('下载模板失败'))
    },
    downloadRecord (row) {
      this.axios.get(`/grade/download/${row.id}`, { responseType: 'blob' }).then(resp => {
        const url = window.URL.createObjectURL(new Blob([resp.data]))
        const a = document.createElement('a')
        a.href = url
        a.download = row.fileName || 'score.xlsx'
        a.click()
        window.URL.revokeObjectURL(url)
      }).catch(() => this.$message.error('下载失败'))
    },
    deleteRecord (row) {
      this.$confirm('确定删除该导入记录及文件？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.axios.delete(`/grade/record/${row.id}`).then(() => {
          this.$message.success('删除成功')
          this.fetchRecords()
        }).catch(() => this.$message.error('删除失败'))
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.tips { margin-top: 8px; color: #606266; font-size: 13px; }
.upload-demo { margin: 20px 0; }
</style>
