<template>
  <div class="order-list-page">
    <!-- Tab栏 -->
    <van-tabs v-model:active="currentTabIndex" @change="onTabChange" color="#07C160">
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.name" />
    </van-tabs>

    <!-- 订单列表 -->
    <van-list
      v-model:loading="loading"
      :finished="!hasMore"
      finished-text="没有更多了"
      @load="loadOrders"
    >
      <div class="order-card" v-for="item in orders" :key="item.id">
        <div class="card-header">
          <span class="order-no">订单号：{{ item.orderNo }}</span>
          <van-tag :type="getOrderStatusType(item.status) as any" size="medium">{{ getOrderStatusText(item.status) }}</van-tag>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="label">门店</span>
            <span class="value">{{ item.storeName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">服务项目</span>
            <span class="value">{{ item.serviceItemName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">技师</span>
            <span class="value">{{ item.technicianName || '待分配' }}</span>
          </div>
          <div class="info-row" v-if="item.appointmentTime">
            <span class="label">预约时间</span>
            <span class="value">{{ formatTime(item.appointmentTime) }}</span>
          </div>
        </div>

        <div class="card-amount">
          <span class="amount-item">总额：¥{{ item.totalAmount }}</span>
          <span class="amount-item discount" v-if="item.discountAmount > 0">优惠：-¥{{ item.discountAmount }}</span>
          <span class="amount-item pay">实付：<em>¥{{ item.payAmount }}</em></span>
        </div>

        <div class="card-footer">
          <van-button
            v-if="item.payStatus === 0"
            size="small"
            type="success"
            round
            @click="onPayTap(item)"
          >去支付</van-button>
          <van-button
            size="small"
            plain
            type="primary"
            round
            @click="onDetailTap(item)"
          >查看详情</van-button>
        </div>
      </div>
    </van-list>

    <!-- 空状态 -->
    <van-empty v-if="!loading && orders.length === 0" description="暂无订单记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList } from '@/api/order'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const tabs = [
  { key: 0, name: '全部' },
  { key: 1, name: '待支付' },
  { key: 2, name: '已支付' },
  { key: 3, name: '服务中' },
  { key: 4, name: '已完成' },
  { key: 5, name: '已取消' }
]

const currentTabIndex = ref(0)
const orders = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)

onMounted(() => {
  if (!userStore.isLogin || !userStore.userInfo?.id) {
    router.replace({ path: '/login', query: { redirect: '/order/list' } })
    return
  }
})

function onTabChange() {
  orders.value = []
  page.value = 1
  hasMore.value = true
}

async function loadOrders() {
  if (loading.value) return
  if (!userStore.userInfo?.id) return

  loading.value = true
  try {
    const params: Record<string, any> = {
      memberId: userStore.userInfo.id,
      page: page.value,
      pageSize
    }
    const status = tabs[currentTabIndex.value].key
    if (status !== 0) {
      params.status = status
    }

    const res: any = await getOrderList(params)
    const list = res.data?.list || res.data?.records || res.data || []
    orders.value = orders.value.concat(list)
    page.value++
    hasMore.value = list.length >= pageSize
  } catch {
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

function getOrderStatusType(status: number): string {
  const map: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'primary',
    4: 'success',
    5: 'default',
    6: 'danger'
  }
  return map[status] || 'default'
}

function getOrderStatusText(status: number) {
  const map: Record<number, string> = {
    1: '待支付',
    2: '已支付',
    3: '服务中',
    4: '已完成',
    5: '已取消',
    6: '已退款'
  }
  return map[status] || '未知'
}

function formatTime(timeStr: string) {
  if (!timeStr) return ''
  return timeStr.substring(0, 16).replace('T', ' ')
}

function onPayTap(item: any) {
  router.push(`/payment/${item.id}`)
}

function onDetailTap(item: any) {
  router.push({ path: '/order/detail', query: { id: item.id } })
}
</script>

<style scoped lang="scss">
.order-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.order-card {
  margin: 10px 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 10px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .order-no {
      font-size: 13px;
      color: #909399;
    }
  }

  .card-body {
    margin-bottom: 10px;

    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 3px 0;

      .label {
        font-size: 13px;
        color: #909399;
      }

      .value {
        font-size: 13px;
        color: #303133;
      }
    }
  }

  .card-amount {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 0;
    border-top: 1px solid #f0f0f0;

    .amount-item {
      font-size: 12px;
      color: #909399;

      &.discount {
        color: #07C160;
      }

      &.pay {
        margin-left: auto;
        color: #303133;

        em {
          font-style: normal;
          font-size: 16px;
          font-weight: 700;
          color: #FA5151;
        }
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 8px;
  }
}
</style>
