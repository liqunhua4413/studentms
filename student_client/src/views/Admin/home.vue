<template>
  <div>
    <el-container>
      <el-main>
        <h1>admin home</h1>
        <el-card style="width: 40%">
          <p><i class="el-icon-s-custom" style="margin-right: 18px"></i>liqunhua</p>
          <p><i class="el-icon-location-outline" style="margin-right: 18px"></i>邯郸应用技术职业学院</p>
          <p><i class="el-icon-chat-round" style="margin-right: 18px"></i>QQ：2361271255</p>
          <p><i class="el-icon-s-flag" style="margin-right: 18px"></i>标语：立德树人，知行合一</p>
        </el-card>
        <logout></logout>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import Logout from "@/components/logout";
export default {
  name: "home",
  components: {Logout},
}
</script>

<style scoped>

</style>
