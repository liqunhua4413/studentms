<template>
  <div>
    <el-card>
      <div slot="header" class="clearfix">
        <span>已上传成绩单列表</span>
      </div>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="100"></el-table-column>
        <el-table-column prop="fileName" label="文件名" width="200"></el-table-column>
        <el-table-column prop="term" label="学期" width="150"></el-table-column>
        <el-table-column prop="operator" label="上传者" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="100"></el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180"></el-table-column>
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
      tableData: []
    }
  },
  methods: {
    download(row) {
      window.open('/api/grade/download/' + row.id, '_blank');
    },
    loadData() {
      const that = this
      axios.get('/grade/records').then(function (resp) {
        that.tableData = resp.data || []
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
