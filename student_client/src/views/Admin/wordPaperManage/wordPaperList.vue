<template>
  <div>
    <el-upload
        class="upload-demo"
        :action="uploadUrl"
        :data="{uploadBy: uploadBy}"
        :on-success="handleSuccess"
        :on-error="handleError"
        :before-upload="beforeUpload"
        :limit="1"
        accept=".doc,.docx">
      <el-button slot="trigger" size="small" type="primary">上传 Word 文件</el-button>
      <div slot="tip" class="el-upload__tip">只能上传 Word 文件（.doc, .docx）</div>
    </el-upload>

    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" style="margin-top: 20px;">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="文件名" prop="fileName">
            <el-input v-model="ruleForm.fileName" placeholder="支持模糊搜索"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="上传者" prop="uploadBy">
            <el-input v-model="ruleForm.uploadBy" placeholder="支持模糊搜索"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item>
            <el-button type="primary" @click="submitForm('ruleForm')">查询</el-button>
            <el-button @click="resetForm('ruleForm')">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-table :data="tableData" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="100"></el-table-column>
      <el-table-column prop="fileName" label="文件名" width="300"></el-table-column>
      <el-table-column prop="uploadBy" label="上传者" width="120"></el-table-column>
      <el-table-column prop="uploadTime" label="上传时间" width="180"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button @click="download(scope.row)" type="text" size="small">下载</el-button>
          <el-popconfirm
              confirm-button-text='删除'
              cancel-button-text='取消'
              icon="el-icon-info"
              icon-color="red"
              title="删除不可复原"
              @confirm="deletePaper(scope.row)">
            <el-button slot="reference" type="text" size="small">删除</el-button>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      uploadUrl: '/api/paper/upload',
      uploadBy: sessionStorage.getItem('name') || 'admin',
      ruleForm: {
        fileName: null,
        uploadBy: null
      },
      tableData: [],
      rules: {}
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
      this.$message({
        message: '上传成功！',
        type: 'success'
      });
      this.loadData();
    },
    handleError(err, file) {
      this.$message.error('上传失败！');
    },
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          axios.post('/paper/findBySearch', that.ruleForm).then(function (resp) {
            that.tableData = resp.data
          })
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.loadData();
    },
    download(row) {
      window.open('/api/paper/' + row.id + '/download', '_blank');
    },
    deletePaper(row) {
      const that = this
      axios.get('/paper/deleteById/' + row.id).then(function (resp) {
        if (resp.data === true) {
          that.$message({
            showClose: true,
            message: '删除成功',
            type: 'success'
          });
          that.loadData();
        } else {
          that.$message.error('删除失败');
        }
      })
    },
    loadData() {
      const that = this
      axios.get('/paper/findAll').then(function (resp) {
        that.tableData = resp.data
      })
    }
  },
  created() {
    this.loadData();
  }
}
</script>

<style scoped>
.upload-demo {
  margin: 20px 0;
}
</style>
