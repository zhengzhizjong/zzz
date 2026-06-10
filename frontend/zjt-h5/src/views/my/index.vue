<template>
  <div class="my-page">
    <!-- 个人信息卡片 -->
    <div class="profile-card">
      <div class="avatar-wrapper">
        <van-image class="avatar" round width="64" height="64" :src="userInfo?.avatarUrl || userInfo?.avatar || ''" fit="cover">
          <template #error><div class="avatar-placeholder">👤</div></template>
        </van-image>
      </div>
      <template v-if="isLogin">
        <div class="user-info">
          <span class="nickname">{{ userInfo?.nickname || userInfo?.name || '忠济堂会员' }}</span>
          <van-tag v-if="userInfo?.levelName" type="success" size="medium">{{ userInfo.levelName }}</van-tag>
        </div>
      </template>
      <template v-else>
        <van-button type="primary" size="small" round @click="goLogin">点击登录</van-button>
      </template>
    </div>

    <!-- 积分/余额卡片 -->
    <div class="stats-card" v-if="isLogin">
      <div class="stat-item" @click="goPage('/coupon/list')">
        <span class="stat-value">{{ userInfo?.points ?? '--' }}</span>
        <span class="stat-label">积分</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-value">{{ userInfo?.balance != null ? '¥' + userInfo.balance : '--' }}</span>
        <span class="stat-label">余额</span>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-card">
      <van-cell title="我的预约" is-link @click="goPage('/my-appointment/list')" icon="calendar-o" />
      <van-cell title="我的订单" is-link @click="goPage('/order/list')" icon="orders-o" />
      <van-cell title="健康档案" is-link @click="goPage('/health/profile')" icon="like-o" />
      <van-cell title="我的优惠券" is-link @click="goPage('/coupon/list')" icon="coupon-o" />
      <van-cell title="我的疗程卡" is-link @click="goPage('/treatment/list')" icon="card-o" />
      <van-cell title="服务项目" is-link @click="goPage('/service/list')" icon="shop-o" />
    </div>
    <div class="menu-card">
      <van-cell title="个人信息编辑" is-link @click="goPage('/profile/edit')" icon="edit" />
      <van-cell title="关于忠济堂" is-link @click="showAbout" icon="info-o" />
    </div>

    <!-- 退出登录 -->
    <div class="logout-wrap" v-if="isLogin">
      <van-button block plain type="danger" @click="handleLogout">退出登录</van-button>
    </div>

    <!-- 底部TabBar占位 -->
    <div class="tabbar-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showDialog, showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { checkLogin } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(false)
const userInfo = ref<any>(null)

onMounted(() => {
  isLogin.value = checkLogin()
  if (isLogin.value) {
    loadProfile()
  }
})

async function loadProfile() {
  try {
    await userStore.fetchProfile()
    userInfo.value = userStore.userInfo
  } catch {}
}

function goLogin() {
  router.push('/login')
}

function goPage(path: string) {
  if (!checkLogin()) {
    router.push({ path: '/login', query: { redirect: path } })
    return
  }
  router.push(path)
}

function handleLogout() {
  showDialog({
    title: '提示',
    message: '确认退出登录？',
    showCancelButton: true
  }).then(() => {
    userStore.logout()
    isLogin.value = false
    userInfo.value = null
    showToast('已退出登录')
  }).catch(() => {})
}

function showAbout() {
  showDialog({
    title: '关于忠济堂',
    message: '忠济堂·中医养生连锁管理系统\n\n传承中医精髓，守护健康人生\n\n版本：v1.0.0',
    confirmButtonText: '知道了'
  })
}
</script>

<style scoped lang="scss">
.my-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.profile-card {
  display: flex;
  align-items: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, #07C160, #06ad56);
  color: #fff;

  .avatar-wrapper {
    margin-right: 16px;

    .avatar-placeholder {
      width: 64px;
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      font-size: 28px;
    }
  }

  .user-info {
    display: flex;
    flex-direction: column;

    .nickname {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 4px;
    }
  }
}

.stats-card {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: -20px 12px 0;
  padding: 20px 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;

    .stat-value {
      font-size: 20px;
      font-weight: 700;
      color: #303133;
    }

    .stat-label {
      font-size: 12px;
      color: #909399;
    }
  }

  .stat-divider {
    width: 1px;
    height: 30px;
    background: #e8e8e8;
  }
}

.menu-card {
  margin: 12px;
  border-radius: 10px;
  overflow: hidden;
}

.logout-wrap {
  margin: 20px 12px;
}

.tabbar-placeholder {
  height: 60px;
}
</style>
