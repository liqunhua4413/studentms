<template>
  <div>
    <el-container>
      <el-main>
        <el-card>
          <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
            <el-form-item label="学号" prop="sid">
              <el-input v-model.number="ruleForm.sid"></el-input>
            </el-form-item>
            <el-form-item label="学生名" prop="sname">
              <el-input v-model="ruleForm.sname"></el-input>
            </el-form-item>
            <el-form-item label="模糊查询" prop="sFuzzy">
              <el-switch v-model="ruleForm.sFuzzy"></el-switch>
            </el-form-item>
            <el-form-item label="课程号" prop="cid">
              <el-input v-model.number="ruleForm.cid"></el-input>
            </el-form-item>
            <el-form-item label="课程名" prop="cname">
              <el-input v-model="ruleForm.cname"></el-input>
            </el-form-item>
            <el-form-item label="模糊查询" prop="cFuzzy">
              <el-switch v-model="ruleForm.cFuzzy"></el-switch>
            </el-form-item>
            <el-form-item label="成绩下限" prop="lowBound">
              <el-input v-model.number="ruleForm.lowBound"></el-input>
            </el-form-item>
            <el-form-item label="成绩上限" prop="highBound">
              <el-input v-model.number="ruleForm.highBound"></el-input>
            </el-form-item>
            <el-form-item label="选择学期">
              <el-select v-model="ruleForm.termId" placeholder="请选择学期" clearable>
                <el-option v-for="item in termList" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="resetForm('ruleForm')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card style="margin-top: 10px">
          <teacher-grade-course-list :rule-form="ruleForm"></teacher-grade-course-list>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>
<script>
import GradeCourseList from "@/views/Admin/gradeCourseManage/gradeCourseList";
import TeacherGradeCourseList from "@/views/Teacher/teacherGradeCourseManage/teacherGradeCourseList";
export default {
  components: {TeacherGradeCourseList, GradeCourseList},
  data() {
    return {
      termList: null,
      ruleForm: {
        sid: null,
        sname: null,
        sFuzzy: true,
        tid: parseInt(sessionStorage.getItem('tid')),
        tname: null,
        tFuzzy: true,
        cid: null,
        cname: null,
        cFuzzy: true,
        lowBound: null,
        highBound: null,
        term: sessionStorage.getItem('currentTerm'),
        departmentId: sessionStorage.getItem('departmentId') ? parseInt(sessionStorage.getItem('departmentId')) : null
      },
      rules: {
        cid: [
          { type: 'number', message: '必须是数字类型' }
        ],
        tid: [
          { type: 'number', message: '必须是数字类型' }
        ],
        sid: [
          { type: 'number', message: '必须是数字类型' }
        ],
        cname: [
        ],
        lowBound: [
          { type: 'number', message: '必须是数字类型' }
        ],
        highBound: [
          { type: 'number', message: '必须是数字类型' }
        ],
      }
    };
  },
  created() {
    const that = this
    that.axios.get('/term/findAll').then(function (resp) {
      that.termList = resp.data || []
    })
  },
  methods: {
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  }
}
</script>