<template>
  <div class="payment-page">
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <template v-if="orderInfo && !loading">
      <!-- 金额展示 -->
      <div class="amount-card">
        <span class="amount-label">支付金额</span>
        <div class="amount-value">
          <span class="amount-symbol">¥</span>
          <span class="amount-number">{{ orderInfo.payAmount || '0.00' }}</span>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="order-info">
        <van-cell title="门店" :value="orderInfo.storeName || '-'" />
        <van-cell title="服务项目" :value="orderInfo.serviceItemName || '-'" />
        <van-cell title="订单号" :value="orderInfo.orderNo || '-'" />
        <van-cell v-if="orderInfo.discountAmount > 0" title="优惠金额" :value="'¥' + orderInfo.discountAmount" />
        <van-cell title="订单状态" :value="payStatusText" />
      </div>

      <!-- 支付按钮 -->
      <div class="pay-action" v-if="orderInfo.payStatus !== 1">
        <van-button
          type="primary"
          block
          round
          :loading="paying"
          loading-text="支付中..."
          @click="handlePay"
        >
          微信支付 ¥{{ orderInfo.payAmount || '0.00' }}
        </van-button>
      </div>

      <!-- 已支付提示 -->
      <div class="pay-action" v-else>
        <van-button type="success" block round disabled>已支付</van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { createPayment, getPaymentByOrder } from '@/api/payment'
import { getOrderDetail } from '@/api/order'

const router = useRouter()

const orderId = ref('')
const orderInfo = ref<any>(null)
const paymentInfo = ref<any>(null)
const loading = ref(true)
const paying = ref(false)
let pollingTimer: ReturnType<typeof setInterval> | null = null

const payStatusText = computed(() => {
  if (!orderInfo.value) return '-'
  const statusMap: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已取消' }
  return statusMap[orderInfo.value.payStatus] || '未知'
})

onMounted(() => {
  const id = router.currentRoute.value.params.id as string
  if (id) {
    orderId.value = id
    loadData(id)
  }
})

onUnmounted(() => {
  stopPolling()
})

async function loadData(id: string) {
  loading.value = true
  try {
    const [orderRes, paymentRes]: any[] = await Promise.all([
      getOrderDetail(id),
      getPaymentByOrder(id).catch(() => null)
    ])
    orderInfo.value = orderRes.data || null
    paymentInfo.value = paymentRes?.data || null
  } catch {} finally {
    loading.value = false
  }
}

async function handlePay() {
  if (paying.value) return
  paying.value = true

  try {
    const res: any = await createPayment(orderId.value, 'wechat')
    const payParams = res.data

    // H5环境微信支付 - 使用微信JSAPI或跳转支付
    if (payParams.h5_url) {
      window.location.href = payParams.h5_url
    } else if (payParams.mweb_url) {
      window.location.href = payParams.mweb_url
    } else {
      showToast('支付功能暂未开放')
    }

    // 开始轮询支付结果
    startPolling()
  } catch {
    showDialog({
      title: '支付失败',
      message: '支付未成功，请重试',
      showCancelButton: true
    }).then(() => {
      handlePay()
    }).catch(() => {})
  } finally {
    paying.value = false
  }
}

function startPolling() {
  stopPolling()
  pollingTimer = setInterval(async () => {
    try {
      const res: any = await getPaymentByOrder(orderId.value)
      const payment = res.data
      if (payment && payment.status === 1) {
        stopPolling()
        // 刷新订单信息
        const orderRes: any = await getOrderDetail(orderId.value)
        orderInfo.value = orderRes.data || null
        paymentInfo.value = payment
        showToast('支付成功')
      }
    } catch {
      // 轮询失败不中断
    }
  }, 3000)
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}
</script>

<style scoped lang="scss">
.payment-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.amount-card {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #07C160, #06ad56);
  color: #fff;

  .amount-label {
    font-size: 14px;
    opacity: 0.85;
  }

  .amount-value {
    margin-top: 12px;

    .amount-symbol {
      font-size: 20px;
    }

    .amount-number {
      font-size: 40px;
      font-weight: 700;
    }
  }
}

.order-info {
  margin: 12px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}

.pay-action {
  padding: 20px 16px;
}
</style>
