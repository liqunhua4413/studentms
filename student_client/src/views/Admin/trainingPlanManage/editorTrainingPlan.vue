<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
      <el-form-item label="专业" prop="majorId">
        <el-select v-model="ruleForm.majorId" placeholder="请选择专业" filterable style="width: 100%">
          <el-option v-for="item in majors" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="课程" prop="courseId">
        <el-select v-model="ruleForm.courseId" placeholder="请选择课程" filterable style="width: 100%">
          <el-option v-for="item in courses" :key="item.id" :label="item.cname + ' (ID:' + item.id + ')'" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="方案版本" prop="planVersion">
        <el-input v-model="ruleForm.planVersion" placeholder="如 2025版"></el-input>
      </el-form-item>
      <el-form-item label="课程类型" prop="courseType">
        <el-select v-model="ruleForm.courseType" placeholder="请选择" style="width: 100%">
          <el-option label="必修" value="REQUIRED"></el-option>
          <el-option label="限选" value="LIMITED"></el-option>
          <el-option label="任选" value="ELECTIVE"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="建议年级" prop="suggestedGrade">
        <el-input-number v-model="ruleForm.suggestedGrade" :min="1" :max="6" placeholder="1-6"></el-input-number>
      </el-form-item>
      <el-form-item label="建议学期" prop="suggestedTermId">
        <el-select v-model="ruleForm.suggestedTermId" placeholder="请选择学期" clearable style="width: 100%">
          <el-option v-for="item in terms" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="最低学分" prop="minCredit">
        <el-input-number v-model="ruleForm.minCredit" :min="0" :max="20" :precision="1" :step="0.5"></el-input-number>
      </el-form-item>
      <el-form-item label="最高学分" prop="maxCredit">
        <el-input-number v-model="ruleForm.maxCredit" :min="0" :max="20" :precision="1" :step="0.5"></el-input-number>
      </el-form-item>
      <el-form-item label="最大容量" prop="maxCapacity">
        <el-input-number v-model="ruleForm.maxCapacity" :min="0" placeholder="0表示不限"></el-input-number>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="ruleForm.status" placeholder="请选择" style="width: 100%">
          <el-option label="启用" :value="1"></el-option>
          <el-option label="停用" :value="0"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="ruleForm.remark" type="textarea" :rows="2"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">立即更新</el-button>
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
        id: null,
        majorId: null,
        courseId: null,
        planVersion: '',
        courseType: 'REQUIRED',
        suggestedGrade: 1,
        suggestedTermId: null,
        minCredit: null,
        maxCredit: null,
        maxCapacity: 0,
        status: 1,
        remark: ''
      },
      majors: [],
      courses: [],
      terms: [],
      rules: {
        majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        planVersion: [{ required: true, message: '请输入方案版本', trigger: 'blur' }],
        courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }]
      }
    };
  },
  created() {
    const that = this
    const id = this.$route.query.id
    if (!id) {
      this.$message.error('缺少ID参数')
      return
    }
    axios.get('/major/findAll').then(resp => { that.majors = resp.data || [] })
    axios.post('/course/findBySearch', {}).then(resp => { that.courses = resp.data || [] })
    axios.get('/term/findAll').then(resp => { that.terms = resp.data || [] })
    axios.get('/trainingPlan/findById/' + id).then(function (resp) {
      if (resp.data) {
        that.ruleForm = { ...that.ruleForm, ...resp.data }
      }
    })
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          axios.post('/trainingPlan/update', that.ruleForm).then(function (resp) {
            if (resp.data === true) {
              that.$message.success('更新成功')
              that.$router.push('/queryTrainingPlan')
            } else {
              that.$message.error('更新失败')
            }
          }).catch(function () {
            that.$message.error('更新失败')
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
