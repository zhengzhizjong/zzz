<template>
  <div class="my-appointment-page">
    <!-- Tab栏 -->
    <van-tabs v-model:active="currentTabIndex" @change="onTabChange" color="#07C160">
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.name" />
    </van-tabs>

    <!-- 预约列表 -->
    <van-list
      v-model:loading="loading"
      :finished="!hasMore"
      finished-text="没有更多了"
      @load="loadAppointments"
    >
      <div class="appointment-card" v-for="item in appointments" :key="item.id">
        <div class="card-header">
          <span class="store-name">{{ item.storeName }}</span>
          <van-tag :type="(getStatusType(item.status) as any)" size="medium">{{ item.statusDisplay }}</van-tag>
        </div>

        <div class="card-body">
          <van-cell title="服务项目" :value="item.serviceItemName || '-'" />
          <van-cell title="技师" :value="item.techName || '待分配'" />
          <van-cell title="日期" :value="item.date" />
          <van-cell title="时段" :value="item.timeSlot" />
        </div>

        <div class="card-footer" v-if="tabs[currentTabIndex].key === 'pending'">
          <van-button
            v-if="item.canModify"
            size="small"
            plain
            type="primary"
            @click="onModifyTap(item)"
          >修改</van-button>
          <van-button
            v-if="item.canCancel"
            size="small"
            plain
            type="danger"
            @click="onCancelTap(item)"
          >取消</van-button>
        </div>
      </div>
    </van-list>

    <!-- 空状态 -->
    <van-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />

    <!-- 修改预约弹窗 -->
    <van-popup v-model:show="showModifyPopup" round position="bottom" :style="{ maxHeight: '80%' }">
      <div class="popup-content">
        <div class="popup-header">
          <span class="popup-title">修改预约</span>
          <van-icon name="cross" size="20" @click="showModifyPopup = false" />
        </div>
        <div class="popup-body" v-if="currentAppointment">
          <p>如需修改预约，请重新选择时间</p>
          <van-button type="primary" block round @click="goRebook">重新选择</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 取消预约弹窗 -->
    <van-popup v-model:show="showCancelPopup" round position="bottom" :style="{ maxHeight: '80%' }">
      <div class="popup-content">
        <div class="popup-header">
          <span class="popup-title">取消预约</span>
          <van-icon name="cross" size="20" @click="showCancelPopup = false" />
        </div>
        <div class="popup-body" v-if="currentAppointment">
          <p>确认取消该预约？</p>
          <van-field
            v-model="cancelReason"
            label="取消原因"
            type="textarea"
            placeholder="请输入取消原因（可选）"
            rows="3"
          />
          <van-button type="danger" block round @click="confirmCancel">确认取消</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getMyAppointments, cancelAppointment } from '@/api/appointment'

const router = useRouter()

const tabs = [
  { key: 'pending', name: '待服务' },
  { key: 'completed', name: '已完成' },
  { key: 'cancelled', name: '已取消' }
]

const currentTabIndex = ref(0)
const appointments = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)

const showModifyPopup = ref(false)
const showCancelPopup = ref(false)
const currentAppointment = ref<any>(null)
const cancelReason = ref('')

onMounted(() => {
  loadAppointments(true)
})

function onTabChange() {
  appointments.value = []
  page.value = 1
  hasMore.value = true
  loadAppointments(true)
}

async function loadAppointments(reset = false) {
  if (loading.value) return
  const currentPage = reset ? 1 : page.value
  loading.value = true

  try {
    const res: any = await getMyAppointments({
      status: tabs[currentTabIndex.value].key,
      page: currentPage,
      pageSize: 10
    })
    const list = res.data?.list || []
    const total = res.data?.total || 0
    appointments.value = reset ? list : appointments.value.concat(list)
    page.value = currentPage + 1
    hasMore.value = (reset ? list.length : appointments.value.length) < total
  } catch {
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    pending: 'primary',
    confirmed: 'success',
    completed: 'success',
    cancelled: 'danger',
    no_show: 'warning'
  }
  return map[status] || 'default'
}

function onModifyTap(item: any) {
  currentAppointment.value = item
  showModifyPopup.value = true
}

function onCancelTap(item: any) {
  currentAppointment.value = item
  cancelReason.value = ''
  showCancelPopup.value = true
}

function goRebook() {
  showModifyPopup.value = false
  router.push('/appointment/step1')
}

async function confirmCancel() {
  if (!currentAppointment.value) return
  try {
    await cancelAppointment(currentAppointment.value.id, { reason: cancelReason.value })
    showToast('取消成功')
    showCancelPopup.value = false
    currentAppointment.value = null
    loadAppointments(true)
  } catch {}
}
</script>

<style scoped lang="scss">
.my-appointment-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.appointment-card {
  margin: 10px 12px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px 8px;

    .store-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .card-body {
    :deep(.van-cell) {
      padding: 4px 16px;
    }
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 8px 16px 14px;
  }
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

  .popup-body {
    padding: 16px;

    p {
      font-size: 14px;
      color: #606266;
      margin-bottom: 16px;
    }
  }
}
</style>
