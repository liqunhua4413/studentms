<template>
  <div>
    <el-upload
        class="upload-demo"
        :action="uploadUrl"
        :http-request="customUpload"
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
          :closable="true"
          @close="uploadResult=''"
          show-icon>
      </el-alert>
    </div>

    <div style="margin-top: 40px;">
      <h3>已上传文件列表</h3>
      <el-table :data="tableData" border style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="100"></el-table-column>
        <el-table-column prop="fileName" label="文件名" width="300"></el-table-column>
        <el-table-column prop="uploadBy" label="上传者" width="120"></el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="180"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="download(scope.row)">下载</el-button>
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
      uploadUrl: '/api/paper/upload',
      uploadBy: sessionStorage.getItem('name') || 'teacher',
      uploadResult: '',
      tableData: []
    }
  },
  methods: {
    customUpload(options) {
      const { file, data, onSuccess, onError } = options;
      const formData = new FormData();
      formData.append('file', file);
      if (data && data.uploadBy) formData.append('uploadBy', data.uploadBy);
      this.axios.post('/paper/upload', formData)
        .then(res => { onSuccess(res.data); })
        .catch(err => {
          const msg = (err.response && err.response.data) ? String(err.response.data) : (err.message || '上传失败');
          onError(new Error(msg));
        });
    },
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
        this.uploadResult = (this.uploadResult ? this.uploadResult + '\n' : '') + file.name + ': 上传成功！';
        this.$message.success(file.name + ' 上传成功！');
        this.loadData();
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
    },
    loadData() {
      this.axios.get('/paper/findAll').then(resp => {
        this.tableData = resp.data || []
      });
    },
    download(row) {
      window.open('/api/paper/' + row.id + '/download', '_blank');
    }
  },
  created() {
    this.loadData();
  }
}
</script>

<style scoped>
.upload-demo { margin: 20px 0; }
</style>
