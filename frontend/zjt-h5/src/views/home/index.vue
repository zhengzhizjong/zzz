<template>
  <div class="home-page">
    <!-- 顶部Banner -->
    <div class="header">
      <div class="header-content">
        <h1 class="title">忠济堂·中医养生</h1>
        <p class="subtitle">传承中医精髓 守护健康人生</p>
        <div class="member-info" v-if="userStore.isLogin && userStore.userInfo">
          <span class="member-name">{{ userStore.userInfo.name || userStore.userInfo.nickname || '用户' }}</span>
          <span class="member-level" v-if="userStore.userInfo.levelName">{{ userStore.userInfo.levelName }}</span>
        </div>
        <div class="login-entry" v-else @click="$router.push('/login')">
          <span>登录/注册</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-entry">
      <van-grid :column-num="4" :border="false">
        <van-grid-item icon="shop-o" text="门店" to="/store/list" />
        <van-grid-item icon="friends-o" text="技师" to="/technician/list" />
        <van-grid-item icon="calendar-o" text="预约" to="/appointment/step1" />
        <van-grid-item icon="coupon-o" text="优惠券" to="/coupon/list" />
      </van-grid>
    </div>

    <!-- 热门服务 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">热门服务</span>
      </div>
      <div v-if="serviceLoading" style="text-align: center; padding: 20px 0;">
        <van-loading size="24px">加载中...</van-loading>
      </div>
      <div class="service-list" v-else-if="services.length > 0">
        <div class="service-item" v-for="item in services" :key="item.id">
          <div class="service-icon">💆</div>
          <div class="service-info">
            <span class="service-name">{{ item.itemName || item.name }}</span>
            <span class="service-desc">{{ item.description || '' }}</span>
            <span class="service-price" v-if="item.price">¥{{ item.price }}</span>
          </div>
        </div>
      </div>
      <van-empty v-else description="暂无服务项目" />
    </div>

    <!-- 附近门店 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">附近门店</span>
        <span class="section-more" @click="$router.push('/store/list')">查看更多 ›</span>
      </div>
      <div v-if="storeLoading" style="text-align: center; padding: 20px 0;">
        <van-loading size="24px">加载中...</van-loading>
      </div>
      <div class="store-list" v-else-if="stores.length > 0">
        <div class="store-item" v-for="item in stores" :key="item.id" @click="goStore(item.id)">
          <div class="store-name">{{ item.storeName || item.name }}</div>
          <div class="store-address">{{ item.address || '暂无地址信息' }}</div>
          <div class="store-status">
            <van-tag :type="item.status === 1 ? 'success' : 'danger'" size="medium">
              {{ item.status === 1 ? '营业中' : '已打烊' }}
            </van-tag>
          </div>
        </div>
      </div>
      <van-empty v-else description="暂无门店信息" />
    </div>

    <!-- 推荐技师 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">推荐技师</span>
        <span class="section-more" @click="$router.push('/technician/list')">查看更多 ›</span>
      </div>
      <div v-if="techLoading" style="text-align: center; padding: 20px 0;">
        <van-loading size="24px">加载中...</van-loading>
      </div>
      <div class="tech-list" v-else-if="technicians.length > 0">
        <div class="tech-item" v-for="item in technicians" :key="item.id" @click="goTechnician(item.id)">
          <div class="tech-avatar-placeholder" :style="{background: item.levelColor || '#f0f0f0'}">
            <span>{{ item.levelIcon || '⭐' }}</span>
          </div>
          <div class="tech-info">
            <span class="tech-name">{{ item.name }}</span>
            <span class="tech-level">{{ item.levelName || '' }} · {{ item.storeName || '' }}</span>
          </div>
          <van-tag v-if="item.onDuty" type="success" size="medium">在岗</van-tag>
        </div>
      </div>
      <van-empty v-else description="暂无技师信息" />
    </div>

    <!-- 底部TabBar占位 -->
    <div class="tabbar-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStoreList } from '@/api/store'
import { getTechnicianList } from '@/api/technician'
import { getServiceItemList } from '@/api/service'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const services = ref<any[]>([])
const stores = ref<any[]>([])
const technicians = ref<any[]>([])

const serviceLoading = ref(false)
const storeLoading = ref(false)
const techLoading = ref(false)

onMounted(() => {
  loadHomeData()
})

async function loadHomeData() {
  if (userStore.isLogin && !userStore.userInfo) {
    userStore.fetchProfile()
  }
  loadServices()
  loadStores()
  loadTechnicians()
}

async function loadServices() {
  serviceLoading.value = true
  try {
    const res: any = await getServiceItemList({ status: 1 })
    services.value = res.data?.list || res.data || []
  } catch {
    services.value = []
  } finally {
    serviceLoading.value = false
  }
}

async function loadStores() {
  storeLoading.value = true
  try {
    const storeRes: any = await getStoreList({ page: 1, pageSize: 3 })
    stores.value = storeRes.data?.list || storeRes.data || []
  } catch {
    stores.value = []
  } finally {
    storeLoading.value = false
  }
}

async function loadTechnicians() {
  techLoading.value = true
  try {
    const techRes: any = await getTechnicianList({ page: 1, pageSize: 3 })
    const list = techRes.data?.list || techRes.data || []
    technicians.value = list.map((item: any) => ({
      ...item,
      name: item.name || item.techNo || '技师',
      levelName: item.levelName || SKILL_LEVEL_MAP[item.skillLevel] || '',
      levelColor: SKILL_LEVEL_COLOR[item.skillLevel] || '#f0f0f0',
      levelIcon: SKILL_LEVEL_ICON[item.skillLevel] || '⭐'
    }))
  } catch {
    technicians.value = []
  } finally {
    techLoading.value = false
  }
}

const SKILL_LEVEL_MAP: Record<number, string> = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '资深',
  5: '首席'
}

const SKILL_LEVEL_COLOR: Record<number, string> = {
  1: 'rgba(144,147,153,0.15)',
  2: 'rgba(7,193,96,0.15)',
  3: 'rgba(25,137,250,0.15)',
  4: 'rgba(230,162,60,0.15)',
  5: 'rgba(245,108,108,0.15)'
}

const SKILL_LEVEL_ICON: Record<number, string> = {
  1: '🌱', 2: '🌿', 3: '⭐', 4: '🏆', 5: '👑'
}

function goStore(id: number) {
  router.push(`/store/detail/${id}`)
}

function goTechnician(id: number) {
  router.push(`/technician/detail/${id}`)
}
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #07C160, #06ad56);
  padding: 40px 20px 30px;
  color: #fff;

  .title {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
  }

  .subtitle {
    font-size: 14px;
    opacity: 0.85;
    margin: 0;
  }

  .member-info {
    margin-top: 10px;
    display: flex;
    align-items: center;
    gap: 8px;

    .member-name {
      font-size: 14px;
      font-weight: 500;
    }

    .member-level {
      font-size: 12px;
      background: rgba(255, 255, 255, 0.2);
      padding: 2px 8px;
      border-radius: 10px;
    }
  }

  .login-entry {
    margin-top: 10px;

    span {
      font-size: 14px;
      opacity: 0.9;
      cursor: pointer;
    }
  }
}

.quick-entry {
  margin: -20px 12px 12px;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section {
  margin: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 16px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .section-more {
    font-size: 13px;
    color: #07C160;
  }
}

.service-list {
  .service-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .service-icon {
      font-size: 28px;
      margin-right: 12px;
    }

    .service-info {
      display: flex;
      flex-direction: column;

      .service-name {
        font-size: 15px;
        color: #303133;
        font-weight: 500;
      }

      .service-desc {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

.store-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .store-name {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
  }

  .store-address {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .store-status {
    margin-top: 6px;
  }
}

.tech-list {
  .tech-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .tech-avatar {
      margin-right: 10px;
      flex-shrink: 0;
    }

    .tech-avatar-placeholder {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      font-size: 20px;
      margin-right: 10px;
      flex-shrink: 0;
    }

    .tech-info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .tech-name {
        font-size: 15px;
        font-weight: 500;
        color: #303133;
      }

      .tech-level {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }
  }
}

.tabbar-placeholder {
  height: 60px;
}
</style>
