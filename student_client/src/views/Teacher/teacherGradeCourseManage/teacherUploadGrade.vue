<template>
  <div>
    <div style="margin-bottom: 20px;" v-if="userType === 'teacher'">
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
          :closable="false"
          show-icon>
      </el-alert>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    const userType = sessionStorage.getItem('type') || 'teacher'
    const departmentId = sessionStorage.getItem('departmentId')
    return {
      uploadUrl: '/api/grade/upload',
      fileList: [],
      uploadResult: '',
      uploadBy: sessionStorage.getItem('name') || 'teacher',
      selectedDepartmentId: userType === 'teacher' && departmentId ? parseInt(departmentId) : null,
      userType: userType,
      currentDepartmentName: null
    }
  },
  created() {
    const userType = sessionStorage.getItem('type') || 'teacher'
    const departmentId = sessionStorage.getItem('departmentId')
    // 教师自动使用自己的学院
    if (userType === 'teacher' && departmentId) {
      this.selectedDepartmentId = parseInt(departmentId)
      this.fetchDepartmentName(departmentId)
    }
  },
  methods: {
    submitUpload() {
      this.$refs.upload.submit();
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                      file.type === 'application/vnd.ms-excel';
      if (!isExcel) {
        this.$message.error('只能上传 Excel 文件！');
        return false;
      }
      return true;
    },
    handleSuccess(response, file) {
      if (typeof response === 'string') {
        this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + 
                           file.name + ': ' + response;
      } else {
        this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + 
                           file.name + ': ' + (response.message || '上传成功！');
      }
      this.$message({
        message: file.name + ' 上传完成！',
        type: 'success'
      });
    },
    handleError(err, file) {
      this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + 
                         file.name + ': 上传失败：' + (err.message || '未知错误');
      this.$message.error(file.name + ' 上传失败！');
    },
    fetchDepartmentName(departmentId) {
      if (!departmentId) return
      this.axios.get(`/department/findById/${departmentId}`).then(resp => {
        if (resp.data && resp.data.name) {
          this.currentDepartmentName = resp.data.name
        } else {
          this.currentDepartmentName = '未知'
        }
      }).catch(() => {
        this.currentDepartmentName = '未知'
      })
    },
    downloadTemplate() {
      const that = this
      axios.get('/grade/template', {
        responseType: 'blob'
      }).then(function (resp) {
        const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '成绩单批量导入模板.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        that.$message({
          message: '模板下载成功！',
          type: 'success'
        });
      }).catch(function (error) {
        that.$message.error('模板下载失败！');
      })
    }
  }
}
</script>

<style scoped>
.upload-demo {
  margin: 20px 0;
}
</style>
