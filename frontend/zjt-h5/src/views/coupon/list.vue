<template>
  <div class="coupon-list-page">
    <!-- Tab栏 -->
    <van-tabs v-model:active="currentTabIndex" @change="onTabChange" color="#07C160">
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.name" />
    </van-tabs>

    <!-- 优惠券列表 -->
    <div class="coupon-list" v-if="coupons.length > 0">
      <div class="coupon-card" v-for="item in coupons" :key="item.id" @click="onCouponTap(item)">
        <div class="coupon-left">
          <div class="coupon-value">
            <span class="coupon-symbol" v-if="item.type === 'discount'">¥</span>
            <span class="coupon-amount">{{ item.discountValue || item.discountDisplay || '0' }}</span>
            <span class="coupon-unit" v-if="item.type === 'percent'">折</span>
          </div>
          <span class="coupon-threshold">{{ item.thresholdDisplay || '无门槛' }}</span>
        </div>
        <div class="coupon-divider"></div>
        <div class="coupon-right">
          <span class="coupon-name">{{ item.couponName }}</span>
          <span class="coupon-validity">{{ item.validityStart }} - {{ item.validityEnd }}</span>
          <van-tag :type="getStatusType()" size="medium">{{ getStatusText() }}</van-tag>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && coupons.length === 0" description="暂无优惠券" />

    <!-- 加载提示 -->
    <div class="loading-tip" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showDialog } from 'vant'
import { getMyCoupons } from '@/api/coupon'

const tabs = [
  { key: 'available', name: '可用' },
  { key: 'used', name: '已使用' },
  { key: 'expired', name: '已过期' }
]

const currentTabIndex = ref(0)
const coupons = ref<any[]>([])
const loading = ref(false)

onMounted(() => {
  loadCoupons()
})

function onTabChange() {
  loadCoupons()
}

async function loadCoupons() {
  loading.value = true
  try {
    const res: any = await getMyCoupons(tabs[currentTabIndex.value].key)
    coupons.value = res.data || []
  } catch {} finally {
    loading.value = false
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

function onCouponTap(item: any) {
  showDialog({
    title: item.couponName || '优惠券详情',
    message: `优惠：${item.discountDisplay || ''}\n门槛：${item.thresholdDisplay || '无门槛'}\n有效期：${item.validityStart || ''} 至 ${item.validityEnd || ''}`
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
  }
}

.loading-tip {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
