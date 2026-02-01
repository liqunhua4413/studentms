<template>
  <div>
    <el-container>
      <el-header>
        <div style="text-align: center; font-size: 25px; font-weight: bolder">
          <i class="el-icon-s-home" style="margin-right: 25px"></i>
          邯郸应用技术职业学院考务管理系统
        </div>
      </el-header>
      <el-main>
        <el-card class="login-module">
          <div slot="header" class="clearfix">
            <span style="text-align: center; font-size: 20px; font-family: 'Microsoft YaHei'">
              <p><i class="el-icon-office-building" style="margin-right: 18px"></i>登陆</p>
            </span>
          </div>
          <div>
            <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
              <el-form-item label="学号/工号" prop="id">
                <el-input v-model="ruleForm.id" placeholder="学生请输入学号，教师/院长/管理员请输入工号" prefix-icon="el-icon-user"></el-input>
              </el-form-item>
              <el-form-item label="用户密码" prop="password">
                <el-input v-model="ruleForm.password" placeholder="请输入密码" show-password prefix-icon="el-icon-ice-cream-round"></el-input>
              </el-form-item>
              <el-form-item label="用户类型" prop="type">
                <el-select v-model="ruleForm.type" placeholder="请选择用户类型" style="width: 100%">
                  <el-option label="学生" value="student"></el-option>
                  <el-option label="老师" value="teacher"></el-option>
                  <el-option label="院长" value="dean"></el-option>
                  <el-option label="管理员" value="admin"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitForm('ruleForm')">登陆</el-button>
                <el-button @click="resetForm('ruleForm')">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {
        id: null,
        password: null,
        type: null,
      },
      rules: {
        id: [{ required: true, message: '请输入学号或工号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        type: [{ required: true, message: '请选择', trigger: 'change' }],
      }
    }
  },
  methods: {
    submitForm(formName) {
      const that = this
      this.$refs[formName].validate(valid => {
        if (!valid) {
          console.log('error submit!!')
          return false
        }

        // 获取当前学期、禁止选课信息
        axios.get('/info/getCurrentTerm').then(resp => {
          sessionStorage.setItem("currentTerm", resp.data)
        })
        axios.get('/info/getCurrentTermInfo').then(resp => {
          const t = resp.data
          if (t && t.id != null) {
            sessionStorage.setItem("currentTermId", String(t.id))
          }
        }).catch(() => {})
        axios.get('/info/getForbidCourseSelection').then(resp => {
          sessionStorage.setItem("ForbidCourseSelection", resp.data)
        })

        if (that.ruleForm.type === 'admin' || that.ruleForm.type === 'teacher' || that.ruleForm.type === 'dean') {
          // 教师/管理员/院长登录
          let form = { teacherNo: that.ruleForm.id, password: that.ruleForm.password }
          axios.post("/teacher/login", form).then(resp => {
            const check = resp.data
            if (check === true) {
              axios.post("/teacher/findByNo", { teacherNo: that.ruleForm.id }).then(resp2 => {
                const user = resp2.data
                // 彻底兼容多种 ID 命名
                const userId = user ? (user.tid || user.id) : null
                if (!user || !userId) {
                  console.error("User structure error:", user)
                  that.$message.error("登录失败，用户数据异常")
                  return
                }

                sessionStorage.setItem("token", 'true')
                sessionStorage.setItem("type", that.ruleForm.type)
                sessionStorage.setItem("name", user.tname)
                sessionStorage.setItem("tid", userId)
                // 保存 departmentId，用于院长权限控制
                if (user.departmentId) {
                  sessionStorage.setItem("departmentId", user.departmentId)
                }

                const role = user.role || 'teacher'
                if ((that.ruleForm.type === 'admin' && role === 'admin') ||
                    (that.ruleForm.type === 'teacher' && role === 'teacher') ||
                    (that.ruleForm.type === 'dean' && role === 'dean')) {

                  that.$message.success("登陆成功，欢迎 " + user.tname + "!")
                  if (that.ruleForm.type === 'admin') that.$router.push('/admin')
                  else if (that.ruleForm.type === 'dean') that.$router.push('/dean')
                  else that.$router.push('/teacher')

                } else {
                  that.$message.error("登录失败，角色不匹配")
                }
              }).catch(() => {
                that.$message.error("登录失败，用户不存在")
              })
            } else {
              that.$message.error("账号密码错误")
            }
          })

        } else if (that.ruleForm.type === 'student') {
          // 学生登录：仅支持学号
          const account = that.ruleForm.id && String(that.ruleForm.id).trim()
          if (!account) {
            that.$message.error("请输入学号")
            return
          }
          const loginSuccess = (student) => {
            const userId = student.sid || student.id
            sessionStorage.setItem("token", 'true')
            sessionStorage.setItem("type", "student")
            sessionStorage.setItem("name", student.sname)
            sessionStorage.setItem("sid", userId)
            that.$message.success("登陆成功，欢迎 " + student.sname + "!")
            that.$router.push('/student')
          }
          axios.post("/student/login", { studentNo: account, password: that.ruleForm.password }).then(loginResp => {
            if (loginResp.data !== true) {
              that.$message.error("学号或密码错误")
              return
            }
            axios.post("/student/findByStudentNo", { studentNo: account }).then(resp => {
              const student = resp.data
              if (!student || (student.sid == null && student.id == null)) {
                that.$message.error("登录失败，用户数据异常")
                return
              }
              loginSuccess(student)
            }).catch(() => {
              that.$message.error("登录失败，用户数据异常")
            })
          }).catch(() => {
            that.$message.error("学号或密码错误")
          })

        } else {
          that.$message.error("未知用户类型")
        }

      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>

<style>
.login-module {
  margin-top: 60px;
  position: absolute;
  right: 500px;
  text-align: center;
  width: 30%;
}
.el-header {
  background-color: #B3C0D1;
  color: #333;
  line-height: 60px;
}
</style>
