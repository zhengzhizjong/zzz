<template>
  <div class="home-page">
    <!-- 顶部Banner -->
    <div class="header">
      <div class="header-content">
        <h1 class="title">忠济堂·中医养生</h1>
        <p class="subtitle">传承中医精髓 守护健康人生</p>
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
      <div class="service-list">
        <div class="service-item" v-for="item in services" :key="item.name">
          <div class="service-icon">{{ item.icon }}</div>
          <div class="service-info">
            <span class="service-name">{{ item.name }}</span>
            <span class="service-desc">{{ item.desc }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 附近门店 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">附近门店</span>
        <span class="section-more" @click="$router.push('/store/list')">查看更多 ›</span>
      </div>
      <div class="store-list" v-if="stores.length > 0">
        <div class="store-item" v-for="item in stores" :key="item.id" @click="goStore(item.id)">
          <div class="store-name">{{ item.name }}</div>
          <div class="store-address">{{ item.address || '暂无地址信息' }}</div>
          <div class="store-status">
            <van-tag :type="item.businessStatus === 1 ? 'success' : 'danger'" size="medium">
              {{ item.businessStatus === 1 ? '营业中' : '已打烊' }}
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
      <div class="tech-list" v-if="technicians.length > 0">
        <div class="tech-item" v-for="item in technicians" :key="item.id" @click="goTechnician(item.id)">
          <van-image class="tech-avatar" round width="48" height="48" :src="item.avatarUrl || ''" fit="cover">
            <template #error><div class="avatar-placeholder">👤</div></template>
          </van-image>
          <div class="tech-info">
            <span class="tech-name">{{ item.name }}</span>
            <span class="tech-level">{{ item.levelName || '' }}</span>
          </div>
          <van-rate v-model="item.rating" :size="12" color="#07C160" void-color="#eee" readonly allow-half />
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

const router = useRouter()

const services = ref([
  { name: '中医推拿', desc: '舒筋活络 缓解疲劳', icon: '💆' },
  { name: '艾灸理疗', desc: '温经散寒 扶阳固本', icon: '🔥' },
  { name: '拔罐养生', desc: '行气活血 祛风散寒', icon: '🫙' }
])

const stores = ref<any[]>([])
const technicians = ref<any[]>([])

onMounted(() => {
  loadHomeData()
})

async function loadHomeData() {
  try {
    const storeRes: any = await getStoreList({ page: 1, pageSize: 3 })
    stores.value = storeRes.data?.list || storeRes.data || []
  } catch {}
  try {
    const techRes: any = await getTechnicianList({ page: 1, pageSize: 3 })
    technicians.value = techRes.data?.list || techRes.data || []
  } catch {}
}

function goStore(_id: number) {
  router.push(`/store/list`)
}

function goTechnician(_id: number) {
  router.push(`/technician/list`)
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

    .avatar-placeholder {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      border-radius: 50%;
      font-size: 20px;
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
