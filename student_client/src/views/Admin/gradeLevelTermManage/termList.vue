<template>
  <div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column fixed prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="学期名称" min-width="200"></el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="120"></el-table-column>
      <el-table-column prop="endDate" label="结束日期" width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-popconfirm
            confirm-button-text="删除"
            cancel-button-text="取消"
            icon="el-icon-info"
            icon-color="red"
            title="删除不可复原，确认删除？"
            @confirm="deleteRow(scope.row)"
          >
            <el-button slot="reference" type="text" size="small">删除</el-button>
          </el-popconfirm>
          <el-button @click="editor(scope.row)" type="text" size="small">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      @current-change="changePage"
    ></el-pagination>
  </div>
</template>

<script>
export default {
  data() {
    return {
      tableData: [],
      fullList: [],
      pageSize: 10,
      total: 0
    };
  },
  created() {
    this.loadList();
  },
  methods: {
    loadList() {
      const that = this;
      this.axios.get('/term/findAll').then((resp) => {
        that.fullList = resp.data || [];
        that.total = that.fullList.length;
        that.changePage(1);
      }).catch(() => {
        that.$message.error('加载学期列表失败');
      });
    },
    changePage(page) {
      page = page - 1;
      const start = page * this.pageSize;
      const end = start + this.pageSize;
      this.tableData = this.fullList.slice(start, end);
    },
    deleteRow(row) {
      const that = this;
      this.axios.get('/term/deleteById/' + row.id).then((resp) => {
        if (resp.data === true) {
          that.$message.success('删除成功');
          that.loadList();
        } else {
          that.$message.error('删除失败');
        }
      }).catch((err) => {
        const msg = (err.response && err.response.data && err.response.data.message) || err.message || '删除失败，可能存在外键依赖';
        that.$message.error(msg);
      });
    },
    editor(row) {
      this.$router.push({ path: '/editorTerm', query: { id: row.id } });
    }
  }
};
</script>
