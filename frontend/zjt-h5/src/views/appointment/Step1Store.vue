<template>
  <div class="step-store-page">
    <!-- 步骤条 -->
    <van-steps :active="0" active-color="#07C160">
      <van-step>选门店</van-step>
      <van-step>选技师</van-step>
      <van-step>选时间</van-step>
      <van-step>确认</van-step>
    </van-steps>

    <!-- 搜索栏 -->
    <van-search
      v-model="keyword"
      placeholder="搜索门店名称"
      shape="round"
      @search="onSearch"
      @clear="onSearch"
    />

    <!-- 到店分配技师选项 -->
    <div
      class="assign-card"
      :class="{ selected: assignMode }"
      @click="onAssignTap"
    >
      <div class="assign-left">
        <div class="assign-icon">🏪</div>
        <div class="assign-text">
          <span class="assign-title">到店分配技师</span>
          <span class="assign-desc">无需选择技师，到店后由门店为您安排</span>
        </div>
      </div>
      <van-icon v-if="assignMode" name="success" color="#07C160" size="22" />
    </div>

    <!-- 门店列表 -->
    <div class="store-list" v-if="!loading">
      <div
        class="store-card"
        :class="{ selected: !assignMode && selectedStoreId === item.id }"
        v-for="item in storeList"
        :key="item.id"
        @click="onStoreTap(item)"
      >
        <div class="store-header">
          <span class="store-name">{{ item.storeName || item.name }}</span>
          <van-tag :type="item.status === 1 ? 'success' : 'danger'" size="medium">
            {{ item.status === 1 ? '营业中' : '已打烊' }}
          </van-tag>
        </div>
        <div class="store-info">
          <span>📍 {{ item.address || '暂无地址信息' }}</span>
        </div>
        <div class="store-info">
          <span>🕐 {{ item.businessStartTime || '09:00' }} - {{ item.businessEndTime || '21:00' }}</span>
        </div>
        <div class="store-info" v-if="item.distance !== undefined">
          <span>📏 {{ formatDistance(item.distance) }}</span>
        </div>
        <van-icon v-if="!assignMode && selectedStoreId === item.id" name="success" color="#07C160" size="20" class="check-icon" />
      </div>

      <van-empty v-if="storeList.length === 0" description="暂无门店信息" />
    </div>

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 底部按钮 -->
    <div class="bottom-bar">
      <van-button
        type="primary"
        block
        round
        :disabled="!canNext"
        @click="onNextStep"
      >
        下一步
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStoreList } from '@/api/store'
import { trackFunnel } from '@/api/appointment'

const router = useRouter()

const storeList = ref<any[]>([])
const loading = ref(true)
const keyword = ref('')
const selectedStoreId = ref<number | string>('')
const assignMode = ref(false)
const sessionId = ref('')
const currentPage = ref(1)
const pageSize = 20
const userLat = ref(0)
const userLng = ref(0)

const canNext = computed(() => {
  if (assignMode.value) return true
  return !!selectedStoreId.value
})

onMounted(() => {
  sessionId.value = 'sid_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8)
  getUserLocation()
  loadStoreList()
  track('browse_store')
})

function getUserLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        userLat.value = pos.coords.latitude
        userLng.value = pos.coords.longitude
        // 重新计算距离
        if (storeList.value.length > 0) {
          storeList.value = storeList.value.map((s: any) => ({
            ...s,
            distance: calcDistance(userLat.value, userLng.value, s.latitude, s.longitude)
          }))
          sortStoreByDistance()
        }
      },
      () => {}
    )
  }
}

function calcDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
  if (!lat2 || !lng2) return -1
  const R = 6371
  const dLat = ((lat2 - lat1) * Math.PI) / 180
  const dLng = ((lng2 - lng1) * Math.PI) / 180
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

function formatDistance(km: number): string {
  if (km < 0) return '距离未知'
  if (km < 1) return `${Math.round(km * 1000)}m`
  return `${km.toFixed(1)}km`
}

function sortStoreByDistance() {
  storeList.value.sort((a: any, b: any) => {
    if (a.distance < 0) return 1
    if (b.distance < 0) return -1
    return a.distance - b.distance
  })
}

async function loadStoreList() {
  loading.value = true
  try {
    const res: any = await getStoreList({
      keyword: keyword.value,
      page: currentPage.value,
      pageSize
    })
    const list = res.data?.list || res.data || []
    storeList.value = list.map((s: any) => ({
      ...s,
      distance: userLat.value ? calcDistance(userLat.value, userLng.value, s.latitude, s.longitude) : -1
    }))
    if (userLat.value) sortStoreByDistance()
  } catch {
    storeList.value = []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  currentPage.value = 1
  loadStoreList()
}

function onAssignTap() {
  assignMode.value = !assignMode.value
  if (assignMode.value) {
    selectedStoreId.value = ''
  }
}

function onStoreTap(item: any) {
  assignMode.value = false
  selectedStoreId.value = item.id
  track('select_store', { storeId: item.id })
}

function onNextStep() {
  if (!canNext.value) return

  if (assignMode.value) {
    // 到店分配：直接跳到 step3，techId=0
    router.push({
      path: '/appointment/step3',
      query: {
        storeId: '',
        storeName: encodeURIComponent('到店分配'),
        techId: '0',
        techName: encodeURIComponent('到店分配技师'),
        sessionId: sessionId.value
      }
    })
    return
  }

  const store = storeList.value.find((s: any) => s.id === selectedStoreId.value)
  const storeName = store ? (store.storeName || store.name) : ''
  router.push({
    path: '/appointment/step2',
    query: {
      storeId: String(selectedStoreId.value),
      storeName: encodeURIComponent(storeName),
      sessionId: sessionId.value
    }
  })
}

function track(event: string, extra?: Record<string, any>) {
  trackFunnel({ sessionId: sessionId.value, step: 'step1', event, extra: extra || {} }).catch(() => {})
}
</script>

<style scoped lang="scss">
.step-store-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.assign-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 10px;
  border: 2px solid transparent;
  transition: border-color 0.2s;

  &.selected {
    border-color: #07C160;
    background: #f0faf4;
  }

  .assign-left {
    display: flex;
    align-items: center;

    .assign-icon {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e6f9ee;
      border-radius: 10px;
      font-size: 22px;
      margin-right: 12px;
    }

    .assign-text {
      display: flex;
      flex-direction: column;

      .assign-title {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }

      .assign-desc {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }
  }
}

.store-list {
  padding: 0 12px;
}

.store-card {
  position: relative;
  padding: 14px;
  margin-top: 10px;
  background: #fff;
  border-radius: 10px;
  border: 2px solid transparent;
  transition: border-color 0.2s;

  &.selected {
    border-color: #07C160;
  }

  .store-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .store-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .store-info {
    margin-top: 4px;
    font-size: 13px;
    color: #606266;
  }

  .check-icon {
    position: absolute;
    top: 14px;
    right: 14px;
  }
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
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
