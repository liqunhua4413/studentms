<template>
  <div>
    <el-card>
      <div slot="header" class="clearfix">
        <span>已上传试卷分析列表</span>
      </div>
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="文件名" prop="fileName">
              <el-input v-model="ruleForm.fileName" placeholder="支持模糊搜索"></el-input>
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
        <el-table-column prop="fileName" label="文件名" width="250"></el-table-column>
        <el-table-column prop="uploadBy" label="上传者" width="150"></el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="180"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="download(scope.row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        fileName: null
      },
      tableData: [],
      rules: {}
    }
  },
  methods: {
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
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
</style>
