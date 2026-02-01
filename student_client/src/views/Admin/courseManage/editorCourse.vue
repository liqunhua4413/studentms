<template>
  <div>
    <el-form style="width: 60%" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="课程号" prop="cid">
        <el-input v-model="ruleForm.cid" disabled></el-input>
      </el-form-item>
      <el-form-item label="课程名称" prop="cname">
        <el-input v-model="ruleForm.cname"></el-input>
      </el-form-item>
      <el-form-item label="学分" prop="ccredit">
        <el-input-number v-model="ruleForm.ccredit" :min="0" :max="99"></el-input-number>
      </el-form-item>
      <el-form-item label="课程类别" prop="category">
        <el-input v-model="ruleForm.category" placeholder="如：专业课、公共课"></el-input>
      </el-form-item>
      <el-form-item label="考核方式" prop="examMethod">
        <el-input v-model="ruleForm.examMethod" placeholder="如：考试、考查"></el-input>
      </el-form-item>
      <el-form-item label="学时" prop="hours">
        <el-input-number v-model="ruleForm.hours" :min="0" :max="999"></el-input-number>
      </el-form-item>
      <el-form-item label="所属学院" prop="departmentId">
        <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" style="width: 100%">
          <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">提交</el-button>
        <el-button @click="resetForm('ruleForm')">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
export default {
  data() {
    return {
      ruleForm: {
        cid: null,
        cname: null,
        ccredit: null,
        category: null,
        examMethod: null,
        hours: null,
        departmentId: null
      },
      departments: [],
      rules: {
        cname: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
        ccredit: [
          { required: true, message: '请输入学分', trigger: 'change' },
          { type: 'number', message: '请输入数字', trigger: 'change' }
        ]
      }
    };
  },
  created() {
    const that = this
    const cid = this.$route.query.cid
    if (cid === undefined) {
      this.ruleForm.cid = 1
    } else {
      this.ruleForm.cid = cid
    }
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data || []
    })
    axios.get('/course/findById/' + this.ruleForm.cid).then(function (resp) {
      const list = resp.data
      if (list && list.length > 0) {
        that.ruleForm = { ...list[0] }
      }
    })
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const that = this
          axios.post("/course/updateCourse", this.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message({ showClose: true, message: '编辑成功', type: 'success' });
              that.$router.push("/queryCourse")
            } else {
              that.$message.error('编辑失败，请检查数据库');
            }
          }).catch(function () {
            that.$message.error('编辑失败');
          })
        } else {
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  }
}
</script>