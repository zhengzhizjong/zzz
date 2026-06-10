<template>
  <div class="coupon-list-page">
    <!-- Tab栏 -->
    <van-tabs v-model:active="currentTabIndex" @change="onTabChange" color="#07C160">
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.name" />
    </van-tabs>

    <!-- 领券中心 -->
    <template v-if="tabs[currentTabIndex].key === 'claim'">
      <div class="coupon-list" v-if="availableCoupons.length > 0">
        <div class="coupon-card" v-for="item in availableCoupons" :key="item.id">
          <div class="coupon-left coupon-left--claim">
            <div class="coupon-value">
              <span class="coupon-symbol" v-if="item.couponType === 1">¥</span>
              <span class="coupon-amount">{{ item.discountValue || '0' }}</span>
              <span class="coupon-unit" v-if="item.couponType === 2">折</span>
            </div>
            <span class="coupon-threshold">{{ item.thresholdAmount ? `满${item.thresholdAmount}可用` : '无门槛' }}</span>
          </div>
          <div class="coupon-divider"></div>
          <div class="coupon-right">
            <span class="coupon-name">{{ item.couponName }}</span>
            <span class="coupon-validity">{{ formatDate(item.validFrom) }} - {{ formatDate(item.validTo) }}</span>
            <span class="coupon-remaining">剩余 {{ item.remainingQuantity ?? 0 }} 张</span>
          </div>
          <div class="coupon-action">
            <van-button
              size="small"
              round
              type="success"
              :disabled="claimingId === item.id || !item.remainingQuantity"
              @click="onClaim(item)"
            >
              {{ claimingId === item.id ? '领取中' : '领取' }}
            </van-button>
          </div>
        </div>
      </div>
      <van-empty v-if="!loading && availableCoupons.length === 0" description="暂无可领取优惠券" />
    </template>

    <!-- 我的优惠券列表 -->
    <template v-else>
      <div class="coupon-list" v-if="coupons.length > 0">
        <div class="coupon-card" v-for="item in coupons" :key="item.id" @click="onCouponTap(item)">
          <div class="coupon-left" :class="{ 'coupon-left--used': tabs[currentTabIndex].key === 'used', 'coupon-left--expired': tabs[currentTabIndex].key === 'expired' }">
            <div class="coupon-value">
              <span class="coupon-symbol" v-if="item.couponType === 1">¥</span>
              <span class="coupon-amount">{{ item.discountValue || '0' }}</span>
              <span class="coupon-unit" v-if="item.couponType === 2">折</span>
            </div>
            <span class="coupon-threshold">{{ item.thresholdAmount ? `满${item.thresholdAmount}可用` : '无门槛' }}</span>
          </div>
          <div class="coupon-divider"></div>
          <div class="coupon-right">
            <span class="coupon-name">{{ item.couponName }}</span>
            <span class="coupon-validity">{{ formatDate(item.validFrom) }} - {{ formatDate(item.validTo) }}</span>
            <van-tag :type="getStatusType()" size="medium">{{ getStatusText() }}</van-tag>
          </div>
        </div>
      </div>
      <van-empty v-if="!loading && coupons.length === 0" description="暂无优惠券" />
    </template>

    <!-- 加载提示 -->
    <div class="loading-tip" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showDialog, showToast } from 'vant'
import { useRouter } from 'vue-router'
import { getMyCoupons, getCouponList, claimCoupon } from '@/api/coupon'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const STATUS_MAP: Record<string, number> = {
  available: 1,
  used: 2,
  expired: 3
}

const tabs = [
  { key: 'available', name: '可用' },
  { key: 'used', name: '已使用' },
  { key: 'expired', name: '已过期' },
  { key: 'claim', name: '领券中心' }
]

const currentTabIndex = ref(0)
const coupons = ref<any[]>([])
const availableCoupons = ref<any[]>([])
const loading = ref(false)
const claimingId = ref<number | null>(null)

onMounted(() => {
  if (!userStore.isLogin || !userStore.userInfo?.id) {
    router.replace({ path: '/login', query: { redirect: '/coupon/list' } })
    return
  }
  loadCoupons()
})

function onTabChange() {
  const key = tabs[currentTabIndex.value].key
  if (key === 'claim') {
    loadAvailableCoupons()
  } else {
    loadCoupons()
  }
}

async function loadCoupons() {
  loading.value = true
  try {
    const key = tabs[currentTabIndex.value].key
    const status = STATUS_MAP[key]
    const memberId = userStore.userInfo!.id
    const res: any = await getMyCoupons({ memberId, status })
    coupons.value = res.data || []
  } catch {} finally {
    loading.value = false
  }
}

async function loadAvailableCoupons() {
  loading.value = true
  try {
    const res: any = await getCouponList({ page: 1, pageSize: 100, status: 1 })
    availableCoupons.value = res.data?.records || res.data?.list || res.data || []
  } catch {} finally {
    loading.value = false
  }
}

async function onClaim(item: any) {
  if (claimingId.value) return
  claimingId.value = item.id
  try {
    const memberId = userStore.userInfo!.id
    await claimCoupon(item.id, memberId)
    showToast('领取成功')
    item.remainingQuantity = (item.remainingQuantity ?? 0) - 1
  } catch {} finally {
    claimingId.value = null
  }
}

function getStatusType() {
  const key = tabs[currentTabIndex.value].key
  if (key === 'available') return 'success'
  if (key === 'used') return 'default'
  return 'danger'
}

function getStatusText() {
  const key = tabs[currentTabIndex.value].key
  if (key === 'available') return '可用'
  if (key === 'used') return '已使用'
  return '已过期'
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

function onCouponTap(item: any) {
  const key = tabs[currentTabIndex.value].key
  const lines = [
    `优惠：${item.discountValue || ''}${item.couponType === 2 ? '折' : '元'}`,
    `门槛：${item.thresholdAmount ? '满' + item.thresholdAmount : '无门槛'}`,
    `有效期：${formatDate(item.validFrom)} 至 ${formatDate(item.validTo)}`
  ]
  if (key === 'used' && item.usedAt) {
    lines.push(`使用时间：${formatDate(item.usedAt)}`)
  }
  showDialog({
    title: item.couponName || '优惠券详情',
    message: lines.join('\n')
  })
}
</script>

<style scoped lang="scss">
.coupon-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.coupon-list {
  padding: 12px;
}

.coupon-card {
  display: flex;
  align-items: stretch;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;

  .coupon-left {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    width: 100px;
    padding: 16px 8px;
    background: linear-gradient(135deg, #07C160, #06ad56);
    color: #fff;

    .coupon-value {
      .coupon-symbol { font-size: 14px; }
      .coupon-amount { font-size: 28px; font-weight: 700; }
      .coupon-unit { font-size: 14px; }
    }

    .coupon-threshold {
      font-size: 11px;
      opacity: 0.85;
      margin-top: 4px;
    }

    &.coupon-left--used {
      background: linear-gradient(135deg, #c0c4cc, #909399);
    }

    &.coupon-left--expired {
      background: linear-gradient(135deg, #f56c6c, #e6474b);
    }

    &.coupon-left--claim {
      background: linear-gradient(135deg, #409EFF, #3a8ee6);
    }
  }

  .coupon-divider {
    width: 1px;
    background: repeating-linear-gradient(
      to bottom,
      #e4e7ed 0,
      #e4e7ed 4px,
      transparent 4px,
      transparent 8px
    );
  }

  .coupon-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 12px 14px;

    .coupon-name {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
    }

    .coupon-validity {
      font-size: 12px;
      color: #909399;
      margin: 4px 0;
    }

    .coupon-remaining {
      font-size: 12px;
      color: #E6A23C;
      margin-top: 2px;
    }
  }

  .coupon-action {
    display: flex;
    align-items: center;
    padding: 0 12px;
  }
}

.loading-tip {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
