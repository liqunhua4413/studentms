<template>
  <div>
    <el-container>
      <el-main>
        <el-card>
          <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
            <el-form-item label="所属学院" prop="departmentId">
              <el-select v-model="ruleForm.departmentId" placeholder="全部学院" clearable style="width: 160px;">
                <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="角色" prop="role">
              <el-select v-model="ruleForm.role" placeholder="全部角色" clearable style="width: 140px;">
                <el-option label="系统管理员" value="admin" />
                <el-option label="院长" value="dean" />
                <el-option label="教师" value="teacher" />
              </el-select>
            </el-form-item>
            <el-form-item label="工号" prop="tid">
              <el-input v-model.number="ruleForm.tid" placeholder="工号" clearable style="width: 120px;"></el-input>
            </el-form-item>
            <el-form-item label="教师姓名" prop="tname">
              <el-input v-model="ruleForm.tname" placeholder="姓名" clearable style="width: 120px;"></el-input>
            </el-form-item>
            <el-form-item label="模糊查询" prop="fuzzy">
              <el-switch v-model="ruleForm.fuzzy"></el-switch>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="resetForm('ruleForm')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card>
          <teacher-list :ruleForm="this.ruleForm"></teacher-list>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>
<script>
import TeacherList from "@/views/Admin/teacherManage/teacherList";
export default {
  components: {TeacherList},
  data() {
    return {
      departments: [],
      ruleForm: {
        departmentId: null,
        role: null,
        tid: null,
        tname: null,
        fuzzy: true
      },
      rules: {
        tid: [
          { type: 'number', message: '必须是数字类型' }
        ],
        tname: [],
      }
    };
  },
  created() {
    this.loadDepartments();
  },
  methods: {
    loadDepartments() {
      this.axios.get('/department/findAll').then(resp => {
        this.departments = resp.data || [];
      }).catch(() => { this.departments = []; });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.departmentId = null;
      this.ruleForm.role = null;
    }
  }
}
</script>