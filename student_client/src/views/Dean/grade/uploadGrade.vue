<template>
  <div style="padding: 20px;">
    <el-card class="box-card">
      <div slot="header" class="clearfix"><span>成绩上传</span></div>
      <div style="margin-bottom: 20px;">
        <el-alert
          :title="'当前学院：' + (currentDepartmentName || '未知')"
          type="info"
          :closable="false"
          show-icon>
        </el-alert>
      </div>
      <el-upload
          class="upload-demo"
          ref="upload"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="{departmentId: selectedDepartmentId}"
          :on-success="handleSuccess"
          :on-error="handleError"
          :before-upload="beforeUpload"
          :file-list="fileList"
          :auto-upload="false"
          :multiple="true"
          accept=".xlsx,.xls">
        <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
        <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
        <el-button style="margin-left: 10px;" size="small" type="info" @click="downloadTemplate">下载成绩单模板</el-button>
        <div slot="tip" class="el-upload__tip">
          <p>只能上传 Excel 文件（.xlsx, .xls）</p>
          <p>格式要求：第2行课程元信息，第3行教学信息，第4行表头，第5-42行及第47-73行学生数据</p>
          <p>请下载模板查看详细格式要求</p>
        </div>
      </el-upload>
      <div v-if="uploadResult" style="margin-top: 20px; white-space: pre-line;">
        <el-alert :title="uploadResult" type="info" :closable="true" @close="uploadResult=''" show-icon></el-alert>
      </div>
      <div style="margin-top: 40px;">
        <h3>已上传文件列表</h3>
        <el-table :data="records" style="width: 100%" border stripe>
          <el-table-column prop="fileName" label="文件名" min-width="200"></el-table-column>
          <el-table-column prop="term" label="学期" width="150"></el-table-column>
          <el-table-column prop="operator" label="操作员" width="120"></el-table-column>
          <el-table-column prop="createdAt" label="上传时间" width="180">
            <template slot-scope="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="downloadRecord(scope.row)">下载</el-button>
              <el-button size="mini" type="danger" @click="deleteRecord(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    const departmentId = sessionStorage.getItem('departmentId')
    const userType = 'dean'
    return {
      uploadUrl: '/api/grade/upload',
      fileList: [],
      uploadResult: '',
      selectedDepartmentId: departmentId ? parseInt(departmentId) : null,
      records: [],
      currentDepartmentName: null,
      uploadHeaders: {
        'Operator': encodeURIComponent(sessionStorage.getItem('name') || ''),
        'UserType': userType
      }
    }
  },
  created() {
    const departmentId = sessionStorage.getItem('departmentId')
    if (departmentId) {
      this.selectedDepartmentId = parseInt(departmentId)
      this.fetchDepartmentName(departmentId)
    }
    this.fetchRecords()
  },
  methods: {
    fetchRecords() {
      this.axios.get('/grade/records').then(resp => { this.records = resp.data }).catch(() => {})
    },
    fetchDepartmentName(departmentId) {
      this.axios.get(`/department/findById/${departmentId}`).then(resp => {
        if (resp.data) this.currentDepartmentName = resp.data.name
      }).catch(() => {})
    },
    submitUpload() {
      if (!this.selectedDepartmentId) {
        this.$message.error('无法获取学院信息，请重新登录')
        return
      }
      this.$refs.upload.submit()
    },
    beforeUpload(file) {
      if (!this.selectedDepartmentId) {
        this.$message.error('无法获取学院信息，请重新登录')
        return false
      }
      if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
        this.$message.error('只能上传 Excel 文件！')
        return false
      }
      return true
    },
    handleSuccess(response, file) {
      const msg = typeof response === 'string' ? response : (response.message || '上传成功！')
      if (msg.indexOf('失败') !== -1 || msg.indexOf('拒绝') !== -1 || msg.indexOf('错误') !== -1) {
        this.uploadResult = msg
        this.$message.error(msg)
      } else {
        this.uploadResult = msg
        this.$message.success('上传完成！')
        this.fetchRecords()
      }
    },
    handleError(err, file) {
      this.uploadResult = '上传失败：' + (err.message || '未知错误')
      this.$message.error('上传失败！')
    },
    downloadTemplate() {
      this.axios.get('/grade/template', { responseType: 'blob' }).then(resp => {
        const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '成绩单批量导入模板.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
      })
    },
    downloadRecord(row) {
      this.axios.get(`/grade/download/${row.id}`, { responseType: 'blob' }).then(resp => {
        const url = window.URL.createObjectURL(new Blob([resp.data]))
        const link = document.createElement('a')
        link.href = url
        link.download = row.fileName
        link.click()
      })
    },
    deleteRecord(row) {
      this.$confirm('此操作将永久删除该导入记录及文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.axios.delete(`/grade/record/${row.id}`).then(resp => {
          if (resp.data) {
            this.$message.success('删除成功')
            this.fetchRecords()
          }
        })
      })
    },
    formatDateTime(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString()
    }
  }
}
</script>

<style scoped>
.upload-demo { margin: 20px 0; }
</style>
