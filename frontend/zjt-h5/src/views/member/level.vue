<template>
  <div class="member-level-page">
    <van-nav-bar title="会员等级" left-arrow @click-left="router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 当前等级卡片 -->
      <div class="current-level-card">
        <div class="level-card-bg">
          <div class="level-icon">{{ currentLevelInfo.icon }}</div>
          <div class="level-info">
            <div class="level-name">{{ currentLevelInfo.name }}</div>
            <div class="level-points">当前积分：{{ currentPoints }}</div>
          </div>
          <div class="level-benefits">
            <div class="benefit-item" v-for="(benefit, index) in currentLevelInfo.benefits" :key="index">
              {{ benefit }}
            </div>
          </div>
        </div>
      </div>

      <!-- 等级列表 -->
      <div class="level-list-section">
        <div class="section-title">会员等级说明</div>
        <div class="level-list">
          <div
            v-for="level in levels"
            :key="level.key"
            class="level-item"
            :class="{ 'level-item--active': level.key === currentLevel }"
          >
            <div class="level-item-left">
              <span class="level-item-icon">{{ level.icon }}</span>
              <div class="level-item-info">
                <span class="level-item-name">{{ level.name }}</span>
                <span class="level-item-require">{{ level.requiredPoints }} 积分</span>
              </div>
            </div>
            <div class="level-item-right">
              <van-tag :type="level.key === currentLevel ? 'success' : 'default'" size="medium">
                {{ level.discount }}折优惠
              </van-tag>
            </div>
          </div>
        </div>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { get } from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const currentPoints = ref(0)
const currentLevel = ref('normal')
const refreshing = ref(false)

interface LevelInfo {
  key: string
  name: string
  icon: string
  requiredPoints: number
  discount: number
  benefits: string[]
}

const levels: LevelInfo[] = [
  { key: 'normal', name: '普通会员', icon: '🥉', requiredPoints: 0, discount: 9.8, benefits: ['基础会员权益', '生日提醒'] },
  { key: 'silver', name: '银卡会员', icon: '🥈', requiredPoints: 1000, discount: 9.5, benefits: ['基础会员权益', '专属折扣', '优先预约'] },
  { key: 'gold', name: '金卡会员', icon: '🥇', requiredPoints: 5000, discount: 9.0, benefits: ['基础会员权益', '专属折扣', '优先预约', '免费体验'] },
  { key: 'diamond', name: '钻石会员', icon: '💎', requiredPoints: 20000, discount: 8.5, benefits: ['全部权益', '专属折扣', '优先预约', '免费体验', '私人顾问'] }
]

const currentLevelInfo = computed(() => {
  return levels.find((l) => l.key === currentLevel.value) || levels[0]
})

onMounted(() => {
  loadMemberLevel()
})

async function loadMemberLevel() {
  if (userStore.userInfo) {
    currentPoints.value = userStore.userInfo.points || 0
    currentLevel.value = (userStore.userInfo as any).level || 'normal'
  }
  try {
    const res: any = await get('/api/v1/user/member-levels')
    if (res.data) {
      currentPoints.value = res.data.points ?? currentPoints.value
      currentLevel.value = res.data.level ?? currentLevel.value
    }
  } catch {
    // 使用默认值
  }
}

async function onRefresh() {
  try {
    await loadMemberLevel()
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped lang="scss">
.member-level-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.current-level-card {
  padding: 16px;

  .level-card-bg {
    background: linear-gradient(135deg, #07C160, #06ad56);
    border-radius: 12px;
    padding: 24px 20px;
    color: #fff;

    .level-icon {
      font-size: 40px;
      margin-bottom: 8px;
    }

    .level-info {
      margin-bottom: 12px;

      .level-name {
        font-size: 20px;
        font-weight: 600;
      }

      .level-points {
        font-size: 14px;
        opacity: 0.85;
        margin-top: 4px;
      }
    }

    .level-benefits {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .benefit-item {
        font-size: 12px;
        background: rgba(255, 255, 255, 0.2);
        padding: 3px 10px;
        border-radius: 12px;
      }
    }
  }
}

.level-list-section {
  margin: 0 16px 16px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }
}

.level-list {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}

.level-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &--active {
    background: rgba(7, 193, 96, 0.05);
  }

  .level-item-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .level-item-icon {
      font-size: 28px;
    }

    .level-item-info {
      display: flex;
      flex-direction: column;

      .level-item-name {
        font-size: 15px;
        font-weight: 500;
        color: #303133;
      }

      .level-item-require {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }
  }

  .level-item-right {
    flex-shrink: 0;
  }
}
</style>
