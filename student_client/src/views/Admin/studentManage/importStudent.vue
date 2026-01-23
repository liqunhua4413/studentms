<template>
  <div>
    <el-upload
        class="upload-demo"
        ref="upload"
        :action="uploadUrl"
        :on-success="handleSuccess"
        :on-error="handleError"
        :before-upload="beforeUpload"
        :file-list="fileList"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls">
      <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
      <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
      <div slot="tip" class="el-upload__tip">
        模板列顺序：学生姓名、初始密码、班级ID、年级、专业ID、学院ID。仅管理员可操作。
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
      uploadUrl: '/api/student/import',
      fileList: [],
      uploadResult: ''
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
        this.uploadResult = response;
      } else {
        this.uploadResult = response.message || '上传成功！';
      }
      this.$message({
        message: '上传完成！',
        type: 'success'
      });
    },
    handleError(err, file) {
      this.uploadResult = '上传失败：' + (err.message || '未知错误');
      this.$message.error('上传失败！');
    }
  }
}
</script>

<style scoped>
.upload-demo {
  margin: 20px 0;
}
</style>

