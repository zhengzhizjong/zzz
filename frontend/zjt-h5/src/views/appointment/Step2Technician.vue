<template>
  <div class="step-technician-page">
    <!-- 步骤条 -->
    <van-steps :active="1" active-color="#07C160">
      <van-step>选择门店</van-step>
      <van-step>选择技师</van-step>
      <van-step>选择时间</van-step>
      <van-step>确认预约</van-step>
    </van-steps>

    <!-- 已选门店 -->
    <div class="selected-store-bar">
      <span class="label">已选门店：</span>
      <span class="name">{{ storeName }}</span>
    </div>

    <!-- 不指定技师选项 -->
    <div
      class="no-tech-card"
      :class="{ selected: selectedTechId === 0 }"
      @click="onNoTechTap"
    >
      <div class="no-tech-left">
        <div class="no-tech-avatar">👤</div>
        <span class="no-tech-name">不指定技师</span>
      </div>
      <van-icon v-if="selectedTechId === 0" name="success" color="#07C160" size="20" />
    </div>

    <!-- 技师列表 -->
    <div class="tech-list" v-if="!loading">
      <div
        class="tech-card"
        :class="{
          selected: selectedTechId === item.id,
          full: item.isFull
        }"
        v-for="item in technicianList"
        :key="item.id"
        @click="onTechnicianTap(item)"
      >
        <!-- 约满遮罩 -->
        <div class="full-mask" v-if="item.isFull">
          <span class="full-text">约满</span>
        </div>

        <div class="tech-content">
          <div class="tech-left">
            <div class="avatar-wrap">
              <van-image class="tech-avatar" round width="48" height="48" :src="item.avatarUrl || ''" fit="cover">
                <template #error><div class="avatar-placeholder">👤</div></template>
              </van-image>
              <span class="online-dot" :class="item.onDuty ? 'online' : 'offline'"></span>
            </div>
          </div>

          <div class="tech-info">
            <div class="tech-name-row">
              <span class="tech-name">{{ item.name }}</span>
              <van-tag v-if="item.levelName" type="success" size="medium">{{ item.levelName }}</van-tag>
            </div>
            <div class="tech-rating" v-if="item.rating">
              <span class="rating-star">⭐</span>
              <span class="rating-value">{{ item.rating }}</span>
            </div>
            <div class="tech-skills" v-if="item.skillTags && item.skillTags.length">
              <van-tag v-for="tag in item.skillTags" :key="tag" plain size="medium" type="primary">{{ tag }}</van-tag>
            </div>
          </div>
        </div>
      </div>

      <van-empty v-if="technicianList.length === 0" description="暂无技师信息" />
    </div>

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 底部按钮 -->
    <div class="bottom-bar">
      <van-button type="primary" block round @click="onNextStep">下一步</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTechnicianList } from '@/api/technician'
import { trackFunnel } from '@/api/appointment'

const router = useRouter()
const route = useRoute()

const technicianList = ref<any[]>([])
const loading = ref(true)
const storeId = ref('')
const storeName = ref('')
const selectedTechId = ref(0)
const selectedTechName = ref('不指定')
const sessionId = ref('')

const skillLevelMap: Record<number, string> = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '资深',
  5: '首席'
}

onMounted(() => {
  storeId.value = (route.query.storeId as string) || ''
  storeName.value = decodeURIComponent((route.query.storeName as string) || '')
  sessionId.value = (route.query.sessionId as string) || ''
  loadTechnicianList()
  track('browse_tech')
})

async function loadTechnicianList() {
  loading.value = true
  try {
    const res: any = await getTechnicianList({ storeId: storeId.value })
    const list = res.data?.list || res.data || []
    technicianList.value = list.map((item: any) => ({
      ...item,
      name: item.name || item.technicianNo || '技师',
      levelName: item.levelName || skillLevelMap[item.skillLevel] || ''
    }))
  } catch {} finally {
    loading.value = false
  }
}

function onNoTechTap() {
  selectedTechId.value = 0
  selectedTechName.value = '不指定'
}

function onTechnicianTap(item: any) {
  if (item.isFull) return
  selectedTechId.value = item.id
  selectedTechName.value = item.name
  track('select_tech', { techId: item.id })
}

function onNextStep() {
  router.push({
    path: '/appointment/step3',
    query: {
      storeId: storeId.value,
      storeName: encodeURIComponent(storeName.value),
      techId: String(selectedTechId.value),
      techName: encodeURIComponent(selectedTechName.value),
      sessionId: sessionId.value
    }
  })
}

function track(event: string, extra?: Record<string, any>) {
  trackFunnel({ sessionId: sessionId.value, step: 'step2', event, extra: extra || {} }).catch(() => {})
}
</script>

<style scoped lang="scss">
.step-technician-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.selected-store-bar {
  padding: 10px 16px;
  background: #fff;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;

  .label { color: #909399; }
  .name { color: #303133; font-weight: 500; }
}

.no-tech-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  margin: 10px 12px;
  background: #fff;
  border-radius: 10px;
  border: 2px solid transparent;

  &.selected { border-color: #07C160; }

  .no-tech-left {
    display: flex;
    align-items: center;

    .no-tech-avatar {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      border-radius: 50%;
      font-size: 18px;
      margin-right: 10px;
    }

    .no-tech-name {
      font-size: 15px;
      color: #303133;
    }
  }
}

.tech-list {
  padding: 0 12px;
}

.tech-card {
  position: relative;
  padding: 14px;
  margin-top: 10px;
  background: #fff;
  border-radius: 10px;
  border: 2px solid transparent;

  &.selected { border-color: #07C160; }
  &.full { opacity: 0.6; }

  .full-mask {
    position: absolute;
    top: 0;
    right: 0;
    width: 60px;
    height: 60px;
    overflow: hidden;

    .full-text {
      position: absolute;
      top: 8px;
      right: -16px;
      width: 80px;
      text-align: center;
      background: #fa5151;
      color: #fff;
      font-size: 11px;
      padding: 2px 0;
      transform: rotate(45deg);
    }
  }

  .tech-content {
    display: flex;
    align-items: flex-start;
  }

  .tech-left {
    margin-right: 12px;
    flex-shrink: 0;

    .avatar-wrap {
      position: relative;

      .avatar-placeholder {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0f0f0;
        border-radius: 50%;
        font-size: 22px;
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
  }

  .tech-info {
    flex: 1;

    .tech-name-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;

      .tech-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .tech-rating {
      display: flex;
      align-items: center;
      gap: 4px;
      margin: 4px 0;

      .rating-star { font-size: 12px; }
      .rating-value { font-size: 12px; color: #606266; }
    }

    .tech-skills {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
      margin-top: 6px;
    }
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
