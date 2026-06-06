<template>
  <div class="step-store-page">
    <!-- 步骤条 -->
    <van-steps :active="0" active-color="#07C160">
      <van-step>选择门店</van-step>
      <van-step>选择技师</van-step>
      <van-step>选择时间</van-step>
      <van-step>确认预约</van-step>
    </van-steps>

    <!-- 搜索栏 -->
    <van-search
      v-model="keyword"
      placeholder="搜索门店名称"
      shape="round"
      @search="onSearch"
      @clear="onSearch"
    />

    <!-- 门店列表 -->
    <div class="store-list" v-if="!loading">
      <div
        class="store-card"
        :class="{ selected: selectedStoreId === item.id }"
        v-for="item in storeList"
        :key="item.id"
        @click="onStoreTap(item)"
      >
        <div class="store-header">
          <span class="store-name">{{ item.name }}</span>
          <van-tag :type="item.businessStatus === 1 ? 'success' : 'danger'" size="medium">
            {{ item.businessStatus === 1 ? '🟢 营业中' : '🔴 已打烊' }}
          </van-tag>
        </div>
        <div class="store-info">
          <span>📍 {{ item.address }}</span>
        </div>
        <div class="store-info">
          <span>🕐 {{ item.businessHoursStart || '09:00' }} - {{ item.businessHoursEnd || '21:00' }}</span>
        </div>
        <van-icon v-if="selectedStoreId === item.id" name="success" color="#07C160" size="20" class="check-icon" />
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
        :disabled="!selectedStoreId"
        @click="onNextStep"
      >
        下一步
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStoreList } from '@/api/store'
import { trackFunnel } from '@/api/appointment'

const router = useRouter()

const storeList = ref<any[]>([])
const loading = ref(true)
const keyword = ref('')
const selectedStoreId = ref<number | string>('')
const sessionId = ref('')

onMounted(() => {
  sessionId.value = 'sid_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8)
  loadStoreList()
  track('browse_store')
})

async function loadStoreList() {
  loading.value = true
  try {
    const res: any = await getStoreList({ keyword: keyword.value })
    storeList.value = res.data || []
  } catch {} finally {
    loading.value = false
  }
}

function onSearch() {
  loadStoreList()
}

function onStoreTap(item: any) {
  selectedStoreId.value = item.id
  track('select_store', { storeId: item.id })
}

function onNextStep() {
  if (!selectedStoreId.value) return
  const store = storeList.value.find(s => s.id === selectedStoreId.value)
  const storeName = store ? store.name : ''
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
