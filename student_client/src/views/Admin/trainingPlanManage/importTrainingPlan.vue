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
        accept=".xlsx,.xls">
      <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
      <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
      <el-button style="margin-left: 10px;" size="small" type="info" @click="downloadTemplate">下载模板</el-button>
      <div slot="tip" class="el-upload__tip">
        模板列顺序：专业名称、学院名称、课程名称、方案版本、课程类型(REQUIRED/LIMITED/ELECTIVE)、建议年级、建议学期名称、最低学分、最高学分、最大容量、状态(1启用/0停用)、备注。仅管理员可操作。
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
      this.$refs.upload.submit();
    },
    customUpload(options) {
      const { file, onSuccess, onError } = options;
      const formData = new FormData();
      formData.append('file', file);
      this.axios.post('/trainingPlan/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      }).then(res => {
        const result = typeof res.data === 'string' ? res.data : (res.data?.message || '上传成功！');
        onSuccess(result);
      }).catch(err => {
        const msg = (err.response && err.response.data && err.response.data.message) || err.message || '上传失败';
        onError(new Error(msg));
      });
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
    handleSuccess(response) {
      if (typeof response === 'string') {
        this.uploadResult = response;
      } else {
        this.uploadResult = response.message || '上传成功！';
      }
      this.$message.success('上传完成！');
    },
    handleError(err) {
      this.uploadResult = '上传失败：' + (err.message || '未知错误');
      this.$message.error('上传失败！');
    },
    downloadTemplate() {
      const that = this
      axios.get('/trainingPlan/template', {
        responseType: 'blob'
      }).then(function (resp) {
        const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '培养方案批量导入模板.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        that.$message.success('模板下载成功！');
      }).catch(function () {
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
