<template>
  <div>
    <el-upload
      class="upload-demo"
      ref="upload"
      :http-request="customUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :file-list="fileList"
      :auto-upload="false"
      :multiple="true"
      accept=".xlsx,.xls"
    >
      <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
      <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
      <el-button style="margin-left: 10px;" size="small" type="info" @click="downloadTemplate">下载模板</el-button>
      <div slot="tip" class="el-upload__tip">
        模板列顺序：年级名称、排序（可选，默认0）。仅管理员可操作。
      </div>
    </el-upload>
    <div v-if="uploadResult" style="margin-top: 20px; white-space: pre-line;">
      <el-alert :title="uploadResult" type="info" :closable="false" show-icon />
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      fileList: [],
      uploadResult: ''
    }
  },
  methods: {
    submitUpload() {
      const upload = this.$refs.upload;
      const uploadFiles = upload && upload.uploadFiles;
      if (!uploadFiles || uploadFiles.length === 0) {
        this.$message.warning('请先选择文件')
        return
      }
      this.$refs.upload.submit()
    },
    customUpload(options) {
      const { file, onSuccess, onError } = options
      const formData = new FormData()
      formData.append('file', file)
      this.axios
        .post('/gradeLevel/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
        .then(res => {
          const result = typeof res.data === 'string' ? res.data : (res.data?.message || '上传成功！')
          onSuccess(result)
        })
        .catch(err => {
          const msg = (err.response && err.response.data && err.response.data.message) || err.message || '上传失败'
          onError(new Error(msg))
        })
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
        file.type === 'application/vnd.ms-excel'
      if (!isExcel) {
        this.$message.error('只能上传 Excel 文件！')
        return false
      }
      return true
    },
    handleSuccess(response) {
      this.uploadResult = typeof response === 'string' ? response : (response?.message || '上传成功！')
      this.$message.success('上传完成！')
    },
    handleError(err) {
      this.uploadResult = '上传失败：' + (err.message || '未知错误')
      this.$message.error('上传失败！')
    },
    downloadTemplate() {
      this.axios.get('/gradeLevel/template', { responseType: 'blob' }).then(resp => {
        const blob = new Blob([resp.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '年级批量导入模板.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('模板下载成功！')
      }).catch(() => this.$message.error('模板下载失败！'))
    }
  }
}
</script>

<style scoped>
.upload-demo { margin: 20px 0; }
</style>
