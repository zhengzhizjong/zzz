<template>
  <div class="my-page">
    <!-- 顶部个人信息 -->
    <div class="profile-card">
      <div class="avatar">{{ userInfo.name?.charAt(0) || '技' }}</div>
      <div class="profile-info">
        <div class="name">{{ userInfo.name || '技师' }}</div>
        <div class="tags">
          <van-tag type="primary" size="medium">{{ userInfo.level || '初级技师' }}</van-tag>
          <van-tag plain size="medium">{{ userInfo.storeName || '忠济堂' }}</van-tag>
        </div>
      </div>
    </div>

    <!-- 菜单列表 -->
    <div class="menu-section">
      <van-cell title="排班查看" is-link icon="calendar-o" @click="goSchedule" />
      <van-cell title="个人信息" is-link icon="user-o" />
      <van-cell title="修改密码" is-link icon="lock" />
      <van-cell title="关于" is-link icon="info-o" />
      <van-cell title="退出登录" is-link icon="revoke" @click="handleLogout" class="logout-cell" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showDialog } from 'vant'
import { useRouter } from 'vue-router'
import { getProfile } from '../../api/user'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref<any>({})

async function loadProfile() {
  try {
    const res: any = await getProfile()
    userInfo.value = res.data || {}
    userStore.setUserInfo(res.data)
  } catch {
    userInfo.value = userStore.userInfo || { name: '技师', level: '初级技师', storeName: '忠济堂' }
  }
}

function goSchedule() {
  router.push('/my/schedule')
}

function handleLogout() {
  showDialog({
    title: '提示',
    message: '确定要退出登录吗？'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  })
}

onMounted(loadProfile)
</script>

<style scoped>
.my-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.profile-card {
  background: linear-gradient(135deg, #07C160, #06AD56);
  padding: 30px 20px;
  display: flex;
  align-items: center;
  color: #fff;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  flex-shrink: 0;
}

.profile-info {
  margin-left: 16px;
}

.name {
  font-size: 20px;
  font-weight: bold;
}

.tags {
  margin-top: 8px;
  display: flex;
  gap: 6px;
}

.tags .van-tag {
  background: rgba(255,255,255,0.2) !important;
  color: #fff !important;
  border-color: rgba(255,255,255,0.4) !important;
}

.menu-section {
  margin: 12px;
  border-radius: 12px;
  overflow: hidden;
}

.logout-cell :deep(.van-cell__title) {
  color: #ee0a24;
}
</style>
