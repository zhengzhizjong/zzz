<template>
  <div class="treatment-list-page">
    <!-- 疗程卡列表 -->
    <div class="card-list" v-if="cards.length > 0">
      <div class="treatment-card" v-for="item in cards" :key="item.id" @click="onCardTap(item)">
        <div class="card-top">
          <div class="card-info">
            <span class="card-name">{{ item.cardName }}</span>
            <span class="card-project">{{ item.serviceItemName || '' }}</span>
          </div>
          <div class="card-count">
            <span class="count-remaining">{{ item.remainingCount }}</span>
            <span class="count-total">/{{ item.totalCount }}次</span>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-bar">
          <van-progress :percentage="item.progress" :show-pivot="false" color="#07C160" track-color="#f0f0f0" stroke-width="6" />
          <span class="progress-text">已用{{ item.progress }}%</span>
        </div>

        <!-- 有效期 -->
        <div class="card-footer">
          <span class="card-validity">有效期至 {{ item.expiryDate || '长期有效' }}</span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && cards.length === 0" description="暂无疗程卡" />

    <!-- 加载提示 -->
    <div class="loading-tip" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 使用记录弹窗 -->
    <van-popup v-model:show="showUsagePopup" round position="bottom" :style="{ maxHeight: '80%' }">
      <div class="popup-content">
        <div class="popup-header">
          <span class="popup-title">使用记录</span>
          <van-icon name="cross" size="20" @click="showUsagePopup = false" />
        </div>
        <div class="popup-card-name" v-if="currentCard">
          <span>{{ currentCard.cardName }}</span>
        </div>
        <div class="usage-list" v-if="usageHistory.length > 0">
          <div class="usage-item" v-for="item in usageHistory" :key="item.id">
            <div class="usage-left">
              <span class="usage-service">{{ item.serviceItemName || '服务' }}</span>
              <span class="usage-date">{{ item.usedAt }}</span>
            </div>
            <span class="usage-count">-1次</span>
          </div>
        </div>
        <van-empty v-else description="暂无使用记录" />
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyCards, getCardUsageHistory } from '@/api/treatment'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const cards = ref<any[]>([])
const loading = ref(false)
const showUsagePopup = ref(false)
const currentCard = ref<any>(null)
const usageHistory = ref<any[]>([])

onMounted(() => {
  loadCards()
})

async function loadCards() {
  loading.value = true
  try {
    const memberId = userStore.userInfo?.id
    if (!memberId) return
    const res: any = await getMyCards(memberId)
    const list = (res.data || []).map((card: any) => {
      const total = card.totalCount || 0
      const remaining = card.remainingCount || 0
      const used = total - remaining
      const progress = total > 0 ? Math.round((used / total) * 100) : 0
      return { ...card, used, progress }
    })
    cards.value = list
  } catch {} finally {
    loading.value = false
  }
}

async function onCardTap(item: any) {
  currentCard.value = item
  showUsagePopup.value = true
  try {
    const res: any = await getCardUsageHistory(item.id)
    usageHistory.value = res.data || []
  } catch {
    usageHistory.value = []
  }
}
</script>

<style scoped lang="scss">
.treatment-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.card-list {
  padding: 12px;
}

.treatment-card {
  padding: 16px;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 10px;

  .card-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .card-info {
      .card-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        display: block;
      }

      .card-project {
        font-size: 13px;
        color: #909399;
        margin-top: 4px;
        display: block;
      }
    }

    .card-count {
      .count-remaining {
        font-size: 24px;
        font-weight: 700;
        color: #07C160;
      }

      .count-total {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .progress-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;

    :deep(.van-progress) {
      flex: 1;
    }

    .progress-text {
      font-size: 12px;
      color: #909399;
      white-space: nowrap;
    }
  }

  .card-footer {
    .card-validity {
      font-size: 12px;
      color: #909399;
    }
  }
}

.loading-tip {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.popup-content {
  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .popup-card-name {
    padding: 12px 16px;
    font-size: 15px;
    font-weight: 500;
    color: #303133;
    border-bottom: 1px solid #f0f0f0;
  }

  .usage-list {
    padding: 0 16px;
    max-height: 400px;
    overflow-y: auto;
  }

  .usage-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child { border-bottom: none; }

    .usage-left {
      display: flex;
      flex-direction: column;

      .usage-service {
        font-size: 14px;
        color: #303133;
      }

      .usage-date {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }

    .usage-count {
      font-size: 14px;
      color: #fa5151;
    }
  }
}
</style>
