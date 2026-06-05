<template>
  <div class="home-container">
    <div class="home-header">
      <h1>忠济堂·中医养生连锁管理系统</h1>
      <p class="welcome-text">欢迎回来，{{ userStore.userInfo?.name || '管理员' }}</p>
    </div>
    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>今日预约</template>
          <div class="card-value">--</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>今日营收</template>
          <div class="card-value">--</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>在岗技师</template>
          <div class="card-value">--</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>门店数量</template>
          <div class="card-value">--</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped lang="scss">
.home-container {
  padding: 20px;
}

.home-header {
  margin-bottom: 30px;

  h1 {
    font-size: 24px;
    color: #303133;
    margin: 0 0 8px;
  }
}

.welcome-text {
  color: #909399;
  font-size: 14px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #07C160;
  text-align: center;
  padding: 20px 0;
}
</style>
