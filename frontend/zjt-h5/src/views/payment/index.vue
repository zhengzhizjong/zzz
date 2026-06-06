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
          <span class="amount-number">{{ orderInfo.amount || '0.00' }}</span>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="order-info">
        <van-cell title="门店" :value="orderInfo.storeName || '-'" />
        <van-cell title="服务项目" :value="orderInfo.serviceName || '-'" />
        <van-cell title="订单号" :value="orderInfo.orderNo || '-'" />
        <van-cell v-if="orderInfo.technicianName" title="技师" :value="orderInfo.technicianName" />
        <van-cell v-if="orderInfo.appointmentTime" title="预约时间" :value="orderInfo.appointmentTime" />
      </div>

      <!-- 支付按钮 -->
      <div class="pay-action">
        <van-button
          type="primary"
          block
          round
          :loading="paying"
          loading-text="支付中..."
          @click="handlePay"
        >
          微信支付 ¥{{ orderInfo.amount || '0.00' }}
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { createPayment, getOrder } from '@/api/payment'

const router = useRouter()

const orderId = ref('')
const orderInfo = ref<any>(null)
const loading = ref(true)
const paying = ref(false)

onMounted(() => {
  const id = router.currentRoute.value.params.id as string
  if (id) {
    orderId.value = id
    loadOrder(id)
  }
})

async function loadOrder(id: string) {
  loading.value = true
  try {
    const res: any = await getOrder(id)
    orderInfo.value = res.data || null
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
