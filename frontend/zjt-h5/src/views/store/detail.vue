<template>
  <div class="store-detail-page">
    <van-nav-bar :title="store?.storeName || '门店详情'" left-arrow @click-left="router.back()" />

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && !store" description="门店信息不存在" />

    <!-- 门店详情内容 -->
    <template v-if="!loading && store">
      <!-- 门店信息卡片 -->
      <div class="info-card">
        <div class="store-header">
          <span class="store-name">{{ store.storeName }}</span>
          <van-tag :type="store.status === 1 ? 'success' : 'danger'" size="medium">
            {{ store.status === 1 ? '营业中' : '已打烊' }}
          </van-tag>
        </div>

        <div class="info-row">
          <van-icon name="location-o" size="16" color="#07C160" />
          <span class="info-text">{{ store.address || '暂无地址信息' }}</span>
        </div>
        <div class="info-row">
          <van-icon name="clock-o" size="16" color="#07C160" />
          <span class="info-text">{{ businessHours }}</span>
        </div>
        <div class="info-row" @click="onCallPhone">
          <van-icon name="phone-o" size="16" color="#07C160" />
          <span class="info-text phone-link">{{ store.contactPhone || '暂无' }}</span>
        </div>
      </div>

      <!-- 地图占位区域 -->
      <div class="map-placeholder" v-if="store.address">
        <div class="map-inner">
          <van-icon name="location-o" size="24" color="#07C160" />
          <span class="map-address">{{ store.address }}</span>
        </div>
      </div>

      <!-- 门店介绍 -->
      <div class="section-card" v-if="store.description">
        <div class="section-title">门店介绍</div>
        <div class="desc-content">{{ store.description }}</div>
      </div>

      <!-- 门店技师 -->
      <div class="section-card">
        <div class="section-title">门店技师</div>

        <div class="loading-wrap" v-if="techLoading">
          <van-loading size="20px">加载中...</van-loading>
        </div>

        <template v-if="!techLoading">
          <div class="tech-scroll" v-if="technicians.length > 0">
            <div
              class="tech-card"
              v-for="tech in technicians"
              :key="tech.id"
              @click="goTechnician(tech)"
            >
              <van-image class="tech-avatar" round width="56" height="56" :src="tech.avatarUrl || ''" fit="cover">
                <template #error>
                  <div class="avatar-placeholder">👤</div>
                </template>
              </van-image>
              <span class="tech-name">{{ tech.name || '技师' }}</span>
              <van-tag v-if="skillLevelText(tech.skillLevel)" type="success" size="medium" plain>
                {{ skillLevelText(tech.skillLevel) }}
              </van-tag>
            </div>
          </div>
          <van-empty v-else description="暂无技师信息" image="search" />
        </template>
      </div>

      <!-- 底部预约按钮 -->
      <div class="bottom-bar">
        <van-button type="primary" block round @click="onBookNow">立即预约</van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { getStoreDetail } from '@/api/store'
import { getTechnicianList } from '@/api/technician'

const router = useRouter()
const route = useRoute()

const store = ref<any>(null)
const technicians = ref<any[]>([])
const loading = ref(true)
const techLoading = ref(true)

const SKILL_LEVEL_MAP: Record<number, string> = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '专家'
}

const businessHours = computed(() => {
  if (!store.value) return '暂无营业时间'
  const start = store.value.businessStartTime || '09:00'
  const end = store.value.businessEndTime || '21:00'
  return `${start} - ${end}`
})

function skillLevelText(level: number) {
  return SKILL_LEVEL_MAP[level] || ''
}

onMounted(() => {
  const id = route.params.id as string
  if (id) {
    loadStoreDetail(id)
    loadTechnicians(id)
  } else {
    loading.value = false
  }
})

async function loadStoreDetail(id: string) {
  loading.value = true
  try {
    const res: any = await getStoreDetail(id)
    store.value = res.data || null
  } catch {
    store.value = null
  } finally {
    loading.value = false
  }
}

async function loadTechnicians(storeId: string) {
  techLoading.value = true
  try {
    const res: any = await getTechnicianList({ storeId })
    const list = res.data?.list || res.data || []
    technicians.value = list.map((item: any) => ({
      ...item,
      name: item.name || item.technicianNo || '技师'
    }))
  } catch {
    technicians.value = []
  } finally {
    techLoading.value = false
  }
}

function onCallPhone() {
  if (!store.value?.contactPhone) return
  window.location.href = `tel:${store.value.contactPhone}`
}

function goTechnician(tech: any) {
  router.push(`/technician/detail/${tech.id}`)
}

function onBookNow() {
  if (!store.value) return
  if (store.value.status !== 1) {
    showToast('该门店已打烊，暂时无法预约')
    return
  }
  const sessionId = 'sid_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8)
  router.push({
    path: '/appointment/step2',
    query: {
      storeId: String(store.value.id),
      storeName: encodeURIComponent(store.value.storeName || ''),
      sessionId
    }
  })
}
</script>

<style scoped lang="scss">
.store-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.info-card {
  margin: 10px 12px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;

  .store-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14px;

    .store-name {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      flex: 1;
      margin-right: 8px;
    }
  }

  .info-row {
    display: flex;
    align-items: center;
    padding: 8px 0;

    .info-text {
      font-size: 14px;
      color: #606266;
      margin-left: 8px;
      flex: 1;
    }

    .phone-link {
      color: #07C160;
      text-decoration: underline;
    }
  }
}

.map-placeholder {
  margin: 10px 12px;
  border-radius: 10px;
  overflow: hidden;
  background: #e8f5e9;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;

  .map-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;

    .map-address {
      font-size: 13px;
      color: #606266;
      max-width: 260px;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}

.section-card {
  margin: 10px 12px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }
}

.desc-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.tech-scroll {
  display: flex;
  overflow-x: auto;
  gap: 16px;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    display: none;
  }

  .tech-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 80px;
    flex-shrink: 0;

    .tech-avatar {
      margin-bottom: 6px;
    }

    .avatar-placeholder {
      width: 56px;
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      border-radius: 50%;
      font-size: 24px;
    }

    .tech-name {
      font-size: 13px;
      color: #303133;
      font-weight: 500;
      margin-bottom: 4px;
      max-width: 80px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      text-align: center;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;

  :deep(.van-button--primary) {
    background: #07C160;
    border-color: #07C160;
  }
}
</style>
