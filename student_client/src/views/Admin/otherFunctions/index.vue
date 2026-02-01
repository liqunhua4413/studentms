<template>
  <div>
    <el-container>
      <el-main>
        <el-card>
          <div slot="header">
            <span>系统数据维护</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header">
                  <span>清空所有数据</span>
                </div>
                <p style="color: red; margin-bottom: 20px;">
                  警告：此操作将清空所有数据，包括学生、教师、课程、成绩等所有信息，此操作不可恢复！
                </p>
                <el-button type="danger" @click="clearAllData" :loading="clearing">
                  <i class="el-icon-delete"></i> 清空所有数据
                </el-button>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header">
                  <span>生成初始数据</span>
                </div>
                <p style="margin-bottom: 20px;">
                  此操作将按照 mysql/studentms.sql 文件中的初始数据生成系统数据，包括3个学院、3个专业、3个班级、3位院长、3位教师、5个学生、4门课程等。
                </p>
                <el-button type="primary" @click="generateTestData" :loading="generating">
                  <i class="el-icon-plus"></i> 生成初始数据
                </el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "otherFunctions",
  data() {
    return {
      clearing: false,
      generating: false
    }
  },
  methods: {
    clearAllData() {
      this.$confirm('此操作将清空所有数据，是否继续？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.clearing = true;
        const that = this;
        axios.post('/admin/clearAllData').then(function (resp) {
          that.clearing = false;
          if (resp.data === true || resp.data === 'success') {
            that.$message({
              type: 'success',
              message: '清空数据成功！'
            });
          } else {
            that.$message({
              type: 'error',
              message: '清空数据失败：' + (resp.data || '未知错误')
            });
          }
        }).catch(function (err) {
          that.clearing = false;
          that.$message({
            type: 'error',
            message: '清空数据失败：' + (err.message || '未知错误')
          });
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
        });
      });
    },
    generateTestData() {
      this.$confirm('此操作将按照 mysql/studentms.sql 文件生成初始数据，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.generating = true;
        const that = this;
        axios.post('/admin/generateTestData').then(function (resp) {
          that.generating = false;
          if (resp.data === true || resp.data === 'success') {
            that.$message({
              type: 'success',
              message: '生成初始数据成功！'
            });
          } else {
            that.$message({
              type: 'error',
              message: '生成初始数据失败：' + (resp.data || '未知错误')
            });
          }
        }).catch(function (err) {
          that.generating = false;
          that.$message({
            type: 'error',
            message: '生成测试数据失败：' + (err.message || '未知错误')
          });
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
        });
      });
    }
  }
}
</script>

<style scoped>
</style>
