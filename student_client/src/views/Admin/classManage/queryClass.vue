<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="班级名称" prop="name">
        <el-input v-model="ruleForm.name" placeholder="支持模糊搜索"></el-input>
      </el-form-item>
      <el-form-item label="年级" prop="gradeLevelId">
        <el-select v-model="ruleForm.gradeLevelId" placeholder="请选择年级" clearable style="width: 100%">
          <el-option v-for="item in gradeLevels" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属专业" prop="majorId">
        <el-select v-model="ruleForm.majorId" placeholder="请选择专业" clearable>
          <el-option
              v-for="item in majors"
              :key="item.id"
              :label="item.name"
              :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属学院" prop="departmentId">
        <el-select v-model="ruleForm.departmentId" placeholder="请选择学院" clearable>
          <el-option
              v-for="item in departments"
              :key="item.id"
              :label="item.name"
              :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('ruleForm')">查询</el-button>
        <el-button @click="resetForm('ruleForm')">重置</el-button>
      </el-form-item>
    </el-form>
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        name: null,
        gradeLevelId: null,
        majorId: null,
        departmentId: null
      },
      gradeLevels: [],
      majors: [],
      departments: [],
      rules: {}
    };
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          that.$router.push({
            path: '/queryClass/classList',
            query: { ruleForm: that.ruleForm }
          })
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  },
  created() {
    const that = this
    axios.get('/gradeLevel/findAll').then(function (resp) {
      that.gradeLevels = resp.data || []
    })
    axios.get('/major/findAll').then(function (resp) {
      that.majors = resp.data || []
    })
    axios.get('/department/findAll').then(function (resp) {
      that.departments = resp.data || []
    })
  }
}
</script>
