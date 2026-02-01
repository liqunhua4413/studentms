<template>
  <div>
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
        :data="{uploadBy: uploadBy, departmentId: selectedDepartmentId}"
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
        <p>支持多文件上传，只能上传 Excel 文件（.xlsx, .xls）</p>
        <p>格式要求：第2行课程元信息，第3行教学信息，第4行表头，第5-73行学生数据</p>
        <p>请下载模板查看详细格式要求</p>
        <p style="color: red;">注意：教师只能上传成绩，不能修改已录入的成绩</p>
      </div>
    </el-upload>
    <div v-if="uploadResult" style="margin-top: 20px; white-space: pre-line;">
      <el-alert
          :title="uploadResult"
          type="info"
          :closable="true"
          @close="uploadResult=''"
          show-icon>
      </el-alert>
    </div>

    <div style="margin-top: 40px;">
      <h3>已上传文件列表</h3>
      <el-table :data="records" style="width: 100%" border stripe>
        <el-table-column prop="fileName" label="文件名" min-width="200"></el-table-column>
        <el-table-column prop="term" label="学期" width="150"></el-table-column>
        <el-table-column prop="operator" label="操作员" width="120"></el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="downloadRecord(scope.row)">下载</el-button>
            <el-button size="mini" type="danger" @click="deleteRecord(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      uploadUrl: '/api/grade/upload',
      fileList: [],
      uploadResult: '',
      uploadBy: sessionStorage.getItem('name') || 'teacher',
      selectedDepartmentId: null,
      currentDepartmentName: null,
      records: []
    }
  },
  created() {
    this.loadTeacherDepartment()
    this.loadRecords()
  },
  methods: {
    /** 从 teacher 表获取当前教师所属学院名称与 ID */
    loadTeacherDepartment() {
      const tid = sessionStorage.getItem('tid') || sessionStorage.getItem('id')
      if (!tid) {
        this.fallbackDepartmentFromStorage()
        return
      }
      const url = '/teacher/findById/' + encodeURIComponent(String(tid).trim())
      this.axios.get(url).then(resp => {
        const teacher = resp.data
        if (teacher) {
          this.selectedDepartmentId = teacher.departmentId != null ? teacher.departmentId : (teacher.department_id != null ? teacher.department_id : null)
          const name = teacher.departmentName || teacher.department_name || ''
          if (name && String(name).trim() !== '') {
            this.currentDepartmentName = String(name).trim()
          } else if (this.selectedDepartmentId != null) {
            this.currentDepartmentName = '学院' + this.selectedDepartmentId
            this.fetchDepartmentNameFallback(this.selectedDepartmentId)
          } else {
            this.currentDepartmentName = '未知'
          }
        } else {
          this.fallbackDepartmentFromStorage()
        }
      }).catch(() => {
        this.fallbackDepartmentFromStorage()
      })
    },
    /** 有 departmentId 时用学院接口补全名称 */
    fetchDepartmentNameFallback(departmentId) {
      if (!departmentId) return
      this.axios.get('/department/findById/' + departmentId).then(resp => {
        if (resp.data && resp.data.name) {
          this.currentDepartmentName = resp.data.name
        }
      }).catch(() => {})
    },
    /** 教师接口失败时用登录时存的 departmentId 再试一次 */
    fallbackDepartmentFromStorage() {
      const departmentId = sessionStorage.getItem('departmentId')
      if (departmentId) {
        const id = parseInt(departmentId, 10)
        if (!isNaN(id)) {
          this.selectedDepartmentId = id
          this.currentDepartmentName = '学院' + id
          this.fetchDepartmentNameFallback(id)
          return
        }
      }
      this.currentDepartmentName = '未知'
    },
    submitUpload() {
      this.$refs.upload.submit();
    },
    beforeUpload(file) {
      if (!this.selectedDepartmentId) {
        this.$message.error('无法获取学院信息，请重新登录');
        return false;
      }
      const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls');
      if (!isExcel) {
        this.$message.error('只能上传 Excel 文件！');
        return false;
      }
      return true;
    },
    handleSuccess(response, file) {
      const msg = typeof response === 'string' ? response : (response.message || '上传成功！');
      if (msg.indexOf('失败') !== -1 || msg.indexOf('拒绝') !== -1 || msg.indexOf('错误') !== -1) {
        this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + file.name + ': ' + msg;
        this.$message.error(msg);
      } else {
        this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + file.name + ': ' + msg;
        this.$message.success('上传完成！');
        this.loadRecords();
      }
    },
    handleError(err, file) {
      this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') +
                         file.name + ': 上传失败：' + (err.message || '未知错误');
      this.$message.error(file.name + ' 上传失败！');
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
        this.$message.success('模板下载成功！');
      }).catch(() => this.$message.error('模板下载失败！'))
    },
    loadRecords() {
      this.axios.get('/grade/records').then(resp => {
        this.records = resp.data || []
      }).catch(err => console.error('加载记录失败:', err));
    },
    downloadRecord(row) {
      this.axios.get(`/grade/download/${row.id}`, { responseType: 'blob' }).then(resp => {
        const url = window.URL.createObjectURL(new Blob([resp.data]))
        const link = document.createElement('a')
        link.href = url
        link.download = row.fileName
        link.click()
        window.URL.revokeObjectURL(url)
      });
    },
    deleteRecord(row) {
      this.$confirm('此操作将永久删除该导入记录及文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.axios.delete(`/grade/record/${row.id}`).then(resp => {
          if (resp.data) {
            this.$message.success('删除成功');
            this.loadRecords();
          }
        });
      });
    },
    formatDateTime(dateStr) {
      if (!dateStr) return '';
      return new Date(dateStr).toLocaleString();
    }
  }
}
</script>

<style scoped>
.upload-demo { margin: 20px 0; }
</style>
