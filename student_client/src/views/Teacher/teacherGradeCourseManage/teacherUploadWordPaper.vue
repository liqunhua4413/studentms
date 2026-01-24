<template>
  <div>
    <el-upload
        class="upload-demo"
        :action="uploadUrl"
        :data="{uploadBy: uploadBy}"
        :on-success="handleSuccess"
        :on-error="handleError"
        :before-upload="beforeUpload"
        :multiple="true"
        accept=".doc,.docx">
      <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
      <div slot="tip" class="el-upload__tip">
        支持多文件上传，只能上传 Word 文件（.doc, .docx）
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
      uploadUrl: '/api/paper/upload',
      uploadBy: sessionStorage.getItem('name') || 'teacher',
      uploadResult: ''
    }
  },
  methods: {
    beforeUpload(file) {
      const isWord = file.type === 'application/msword' || 
                     file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
      if (!isWord) {
        this.$message.error('只能上传 Word 文件！');
        return false;
      }
      return true;
    },
    handleSuccess(response, file) {
      if (response && response.id) {
        this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + 
                           file.name + ': 上传成功！';
        this.$message({
          message: file.name + ' 上传成功！',
          type: 'success'
        });
      } else {
        this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + 
                           file.name + ': 上传失败：' + (response.message || '未知错误');
        this.$message.error(file.name + ' 上传失败：' + (response.message || '未知错误'));
      }
    },
    handleError(err, file) {
      this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + 
                         file.name + ': 上传失败：' + (err.message || '未知错误');
      this.$message.error(file.name + ' 上传失败！');
    }
  }
}
</script>

<style scoped>
.upload-demo {
  margin: 20px 0;
}
</style>
