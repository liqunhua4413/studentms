<template>
  <div>
    <el-container>
      <el-main>
        <h1>student home</h1>
        <info-card></info-card>
        <el-card style="width: 100%; margin-top: 10px">
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
import InfoCard from "@/components/infoCard";
export default {
  name: "home",
  components: {InfoCard, Logout},
}
</script>

<style scoped>

</style>
