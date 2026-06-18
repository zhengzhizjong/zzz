<template>
  <div class="order-detail-page">
    <van-nav-bar title="订单详情" left-arrow @click-left="onBack" />

    <div v-if="order" class="detail-content">
      <!-- 订单状态 -->
      <div class="status-bar">
        <span class="status-text">{{ getOrderStatusText(order.status) }}</span>
        <van-tag :type="getOrderStatusType(order.status) as any" size="large">
          {{ getOrderStatusText(order.status) }}
        </van-tag>
      </div>

      <!-- 门店信息 -->
      <van-cell-group inset class="info-group">
        <van-cell title="门店" :value="order.storeName || '-'" />
        <van-cell title="服务项目" :value="order.serviceItemName || '-'" />
        <van-cell title="技师" :value="order.technicianName || '待分配'" />
        <van-cell
          v-if="order.appointmentTime"
          title="预约时间"
          :value="formatTime(order.appointmentTime)"
        />
      </van-cell-group>

      <!-- 订单信息 -->
      <van-cell-group inset class="info-group">
        <van-cell title="订单号" :value="order.orderNo" />
        <van-cell title="订单状态" :value="getOrderStatusText(order.status)" />
      </van-cell-group>

      <!-- 金额信息 -->
      <van-cell-group inset class="info-group">
        <van-cell title="订单总额" :value="`¥${order.totalAmount}`" />
        <van-cell
          v-if="order.discountAmount > 0"
          title="优惠金额"
          :value="`-¥${order.discountAmount}`"
          value-class="discount-value"
        />
        <van-cell title="实付金额" :value="`¥${order.payAmount}`" value-class="pay-value" />
      </van-cell-group>

      <!-- 操作按钮 -->
      <div class="action-bar" v-if="order.status === 1 || order.status === 2">
        <van-button
          v-if="order.status === 1"
          type="success"
          round
          block
          @click="onPay"
        >去支付</van-button>
        <van-button
          v-if="order.status === 1 || order.status === 2"
          plain
          type="primary"
          round
          block
          @click="onCancel"
        >取消订单</van-button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-else class="loading-wrap">
      <van-loading type="spinner" />
    </div>

    <!-- 取消确认弹窗 -->
    <van-dialog
      v-model:show="showCancelDialog"
      title="确认取消"
      message="确定要取消该订单吗？"
      show-cancel-button
      @confirm="confirmCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrderDetail } from '@/api/order'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()

const orderId = route.query.id as string
const order = ref<any>(null)
const showCancelDialog = ref(false)

onMounted(() => {
  if (orderId) {
    fetchOrderDetail()
  } else {
    showToast('订单ID不存在')
    router.back()
  }
})

async function fetchOrderDetail() {
  try {
    const res: any = await getOrderDetail(orderId)
    order.value = res.data || res
  } catch {
    showToast('获取订单详情失败')
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

function getOrderStatusText(status: number): string {
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

function onBack() {
  router.back()
}

function onPay() {
  router.push(`/payment/${orderId}`)
}

function onCancel() {
  showCancelDialog.value = true
}

async function confirmCancel() {
  showToast('订单已取消')
  fetchOrderDetail()
}
</script>

<style scoped lang="scss">
.order-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  margin: 10px 12px;
  background: #fff;
  border-radius: 10px;

  .status-text {
    font-size: 18px;
    font-weight: 700;
    color: #303133;
  }
}

.info-group {
  margin: 10px 12px;
}

.discount-value {
  color: #07C160 !important;
}

.pay-value {
  color: #FA5151 !important;
  font-weight: 700;
}

.action-bar {
  display: flex;
  gap: 10px;
  padding: 16px 12px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding-top: 80px;
}
</style>
