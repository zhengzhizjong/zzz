<template>
  <div class="promotion-page">
    <van-nav-bar title="推广中心" :border="false" />

    <!-- 我的推荐码 -->
    <div class="referral-card">
      <div class="referral-title">我的推荐码</div>
      <div class="referral-code-wrap">
        <span class="referral-code">{{ referralCode }}</span>
        <van-button type="primary" size="small" round plain @click="copyReferralCode">复制</van-button>
      </div>
      <div class="referral-tip">分享推荐码给好友，好友注册后双方均可获得积分奖励</div>
    </div>

    <!-- 会员权益 -->
    <div class="benefits-card" v-if="userStore.isLogin && userStore.userInfo">
      <div class="section-title">会员权益</div>
      <div class="benefits-row">
        <div class="benefit-item">
          <span class="benefit-value">{{ userStore.userInfo.points ?? 0 }}</span>
          <span class="benefit-label">当前积分</span>
        </div>
        <div class="benefit-divider"></div>
        <div class="benefit-item">
          <span class="benefit-value">{{ userStore.userInfo.levelName || '普通会员' }}</span>
          <span class="benefit-label">会员等级</span>
        </div>
        <div class="benefit-divider"></div>
        <div class="benefit-item">
          <span class="benefit-value">{{ availableBenefits }}</span>
          <span class="benefit-label">可用权益</span>
        </div>
      </div>
    </div>

    <!-- 热门推广 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">热门推广</span>
      </div>
      <div class="promo-list">
        <div class="promo-card" v-for="item in promoList" :key="item.id" :style="{ background: item.bgColor }">
          <div class="promo-tag">{{ item.tag }}</div>
          <div class="promo-name">{{ item.name }}</div>
          <div class="promo-desc">{{ item.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 分享按钮 -->
    <div class="share-wrap">
      <van-button type="primary" block round size="large" icon="share-o" @click="handleShare">分享给好友</van-button>
    </div>

    <!-- 底部TabBar占位 -->
    <div class="tabbar-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const referralCode = computed(() => {
  const phone = userStore.userInfo?.phone || ''
  if (phone.length >= 4) {
    return 'ZJT' + phone.slice(-4)
  }
  return 'ZJT0000'
})

const availableBenefits = computed(() => {
  const points = userStore.userInfo?.points || 0
  if (points >= 1000) return 5
  if (points >= 500) return 3
  if (points >= 100) return 2
  return 1
})

const promoList = ref([
  {
    id: 1,
    tag: '新人专享',
    name: '新人专享优惠',
    desc: '首次到店享8折',
    bgColor: 'linear-gradient(135deg, #07C160, #06ad56)'
  },
  {
    id: 2,
    tag: '推荐有礼',
    name: '推荐有礼',
    desc: '推荐好友各得100积分',
    bgColor: 'linear-gradient(135deg, #1989fa, #0570db)'
  },
  {
    id: 3,
    tag: '充值优惠',
    name: '疗程卡充值优惠',
    desc: '充值满1000送200',
    bgColor: 'linear-gradient(135deg, #ff976a, #e56b3c)'
  },
  {
    id: 4,
    tag: '生日特惠',
    name: '生日特惠',
    desc: '生日当月享专属折扣',
    bgColor: 'linear-gradient(135deg, #f5a623, #e08e0b)'
  }
])

onMounted(() => {
  if (userStore.isLogin && !userStore.userInfo) {
    userStore.fetchProfile()
  }
})

function copyReferralCode() {
  const code = referralCode.value
  if (navigator.clipboard) {
    navigator.clipboard.writeText(code).then(() => {
      showToast('推荐码已复制')
    }).catch(() => {
      fallbackCopy(code)
    })
  } else {
    fallbackCopy(code)
  }
}

function fallbackCopy(text: string) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    showToast('推荐码已复制')
  } catch {
    showToast('复制失败，请手动复制')
  }
  document.body.removeChild(textarea)
}

function handleShare() {
  const shareData = {
    title: '忠济堂·中医养生',
    text: `我在忠济堂享受养生服务，推荐码：${referralCode.value}，快来体验吧！`,
    url: window.location.origin
  }
  if (navigator.share) {
    navigator.share(shareData).catch(() => {
      fallbackCopy(shareData.text + ' ' + shareData.url)
    })
  } else {
    fallbackCopy(shareData.text + ' ' + shareData.url)
  }
}
</script>

<style scoped lang="scss">
.promotion-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.referral-card {
  margin: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #07C160, #06ad56);
  border-radius: 10px;
  color: #fff;

  .referral-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .referral-code-wrap {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;

    .referral-code {
      font-size: 28px;
      font-weight: 700;
      letter-spacing: 4px;
    }

    :deep(.van-button) {
      border-color: #fff !important;
      color: #fff !important;
    }
  }

  .referral-tip {
    font-size: 12px;
    opacity: 0.8;
  }
}

.benefits-card {
  margin: 0 12px 12px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 14px;
  }

  .benefits-row {
    display: flex;
    align-items: center;

    .benefit-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .benefit-value {
        font-size: 18px;
        font-weight: 700;
        color: #07C160;
      }

      .benefit-label {
        font-size: 12px;
        color: #909399;
      }
    }

    .benefit-divider {
      width: 1px;
      height: 30px;
      background: #e8e8e8;
    }
  }
}

.section {
  margin: 0 12px 12px;

  .section-header {
    margin-bottom: 10px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.promo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.promo-card {
  padding: 16px;
  border-radius: 10px;
  color: #fff;

  .promo-tag {
    display: inline-block;
    font-size: 10px;
    background: rgba(255, 255, 255, 0.25);
    padding: 2px 8px;
    border-radius: 10px;
    margin-bottom: 8px;
  }

  .promo-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .promo-desc {
    font-size: 13px;
    opacity: 0.85;
  }
}

.share-wrap {
  margin: 20px 12px;
}

.tabbar-placeholder {
  height: 60px;
}
</style>
