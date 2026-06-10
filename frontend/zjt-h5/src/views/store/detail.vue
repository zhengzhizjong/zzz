<template>
  <div class="store-detail-page">
    <!-- 导航栏 -->
    <van-nav-bar
      title="门店详情"
      left-arrow
      @click-left="router.back()"
    />

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && !store" description="门店信息不存在" />

    <!-- 门店信息 -->
    <template v-if="!loading && store">
      <!-- 门店基本信息卡片 -->
      <div class="info-card">
        <div class="store-header">
          <span class="store-name">{{ store.storeName || store.name }}</span>
          <van-tag :type="store.status === 1 ? 'success' : 'danger'" size="medium">
            {{ store.status === 1 ? '营业中' : '已打烊' }}
          </van-tag>
        </div>

        <van-cell-group :border="false" class="info-group">
          <van-cell title="地址" :label="store.address || '暂无地址信息'" icon="location-o" />
          <van-cell title="营业时间" :label="businessHours" icon="clock-o" />
          <van-cell title="联系电话" :label="store.contactPhone || '暂无'" icon="phone-o" />
        </van-cell-group>
      </div>

      <!-- 门店描述卡片 -->
      <div class="desc-card" v-if="store.description">
        <div class="card-title">门店介绍</div>
        <div class="desc-content">{{ store.description }}</div>
      </div>

      <!-- 门店技师列表卡片 -->
      <div class="tech-card">
        <div class="card-title">门店技师</div>

        <div class="loading-wrap" v-if="techLoading">
          <van-loading size="20px">加载中...</van-loading>
        </div>

        <template v-if="!techLoading">
          <div
            class="tech-item"
            v-for="tech in technicians"
            :key="tech.id"
            @click="onTechnicianTap(tech)"
          >
            <div class="tech-left">
              <van-image class="tech-avatar" round width="44" height="44" :src="tech.avatarUrl || ''" fit="cover">
                <template #error><div class="avatar-placeholder">👤</div></template>
              </van-image>
              <span class="online-dot" :class="tech.onDuty ? 'online' : 'offline'"></span>
            </div>
            <div class="tech-info">
              <div class="tech-name-row">
                <span class="tech-name">{{ tech.name }}</span>
                <van-tag v-if="skillLevelText(tech.skillLevel)" type="success" size="medium">{{ skillLevelText(tech.skillLevel) }}</van-tag>
              </div>
              <div class="tech-rating" v-if="tech.rating">
                <van-rate v-model="tech.rating" :size="12" color="#07C160" void-color="#eee" readonly allow-half />
                <span class="rating-text">{{ tech.rating }}</span>
              </div>
            </div>
            <van-icon name="arrow" color="#c0c4cc" />
          </div>

          <van-empty v-if="technicians.length === 0" description="暂无技师信息" image="search" />
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
  const id = route.params.id || route.query.id
  if (id) {
    loadStoreDetail(id as string)
    loadTechnicians(id as string)
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

function onTechnicianTap(tech: any) {
  router.push({ path: `/technician/detail/${tech.id}` })
}

function onBookNow() {
  if (!store.value) return
  if (store.value.status !== 1) {
    showToast('该门店已打烊，暂时无法预约')
    return
  }
  router.push({
    path: '/appointment/step2',
    query: {
      storeId: String(store.value.id),
      storeName: encodeURIComponent(store.value.name)
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
  padding: 14px;
  background: #fff;
  border-radius: 10px;

  .store-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .store-name {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .info-group {
    margin-top: 4px;
  }
}

.desc-card {
  margin: 10px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
  }

  .desc-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
  }
}

.tech-card {
  margin: 10px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
  }
}

.tech-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  .tech-left {
    position: relative;
    margin-right: 12px;
    flex-shrink: 0;

    .avatar-placeholder {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      border-radius: 50%;
      font-size: 20px;
    }

    .online-dot {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 10px;
      height: 10px;
      border-radius: 50%;
      border: 2px solid #fff;

      &.online { background: #07C160; }
      &.offline { background: #c0c4cc; }
    }
  }

  .tech-info {
    flex: 1;

    .tech-name-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;

      .tech-name {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }
    }

    .tech-rating {
      display: flex;
      align-items: center;
      gap: 4px;

      .rating-text {
        font-size: 12px;
        color: #606266;
      }
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
}
</style>
