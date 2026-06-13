<template>
  <div class="promotion-page">
    <!-- 推广海报区域 -->
    <div class="poster-section">
      <div class="poster-placeholder">
        <van-icon name="poster" size="40" color="#07C160" />
        <div class="poster-text">推广海报</div>
      </div>
      <div class="poster-actions">
        <van-button size="small" type="primary" icon="photo-o">生成海报</van-button>
        <van-button size="small" type="primary" icon="share-o">分享</van-button>
        <van-button size="small" type="primary" icon="down">保存相册</van-button>
      </div>
    </div>

    <!-- 邀请码 -->
    <div class="invite-section">
      <div class="invite-label">我的邀请码</div>
      <div class="invite-code">
        <span class="code-text">{{ inviteCode }}</span>
        <van-button size="mini" type="primary" @click="copyCode">复制</van-button>
      </div>
    </div>

    <!-- 推广数据卡片 -->
    <div class="stats-grid">
      <div class="stats-item">
        <div class="stats-value">{{ promotionStats.clicks }}</div>
        <div class="stats-label">点击量</div>
      </div>
      <div class="stats-item">
        <div class="stats-value">{{ promotionStats.registrations }}</div>
        <div class="stats-label">注册量</div>
      </div>
      <div class="stats-item">
        <div class="stats-value">{{ promotionStats.visits }}</div>
        <div class="stats-label">到店量</div>
      </div>
      <div class="stats-item">
        <div class="stats-value">{{ promotionStats.conversionRate }}%</div>
        <div class="stats-label">转化率</div>
      </div>
    </div>

    <!-- 佣金明细 -->
    <div class="section">
      <div class="section-title">佣金明细</div>
      <div class="commission-summary">
        <div class="commission-item">
          <div class="commission-value">{{ commission.settled }}</div>
          <div class="commission-label">已结算(元)</div>
        </div>
        <div class="commission-item">
          <div class="commission-value">{{ commission.pending }}</div>
          <div class="commission-label">待结算(元)</div>
        </div>
      </div>
      <div class="commission-list">
        <div v-for="item in commissionList" :key="item.id" class="commission-record">
          <div class="record-info">
            <div class="record-name">{{ item.name }}</div>
            <div class="record-date">{{ item.date }}</div>
          </div>
          <div class="record-amount" :class="{ pending: item.status === 'pending' }">
            +{{ item.amount }}元
          </div>
        </div>
      </div>
    </div>

    <!-- 推广排行榜 -->
    <div class="section">
      <div class="section-title">本周推广排行</div>
      <div class="rank-list">
        <div v-for="(item, index) in rankList" :key="index" class="rank-item">
          <div class="rank-num">
            <span v-if="index < 3" class="rank-medal" :class="['gold', 'silver', 'bronze'][index]">{{ index + 1 }}</span>
            <span v-else class="rank-normal">{{ index + 1 }}</span>
          </div>
          <div class="rank-name">{{ item.name }}</div>
          <div class="rank-count">{{ item.count }}人</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getPromotion, getPromotionStats, getCommissions, getRanking } from '../../api/promotion'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const inviteCode = ref('ZJT888')
const promotionStats = ref({
  clicks: 0,
  registrations: 0,
  visits: 0,
  conversionRate: '0.0'
})
const commission = ref({ settled: '0.00', pending: '0.00' })
const commissionList = ref<any[]>([])
const rankList = ref<any[]>([])

function copyCode() {
  navigator.clipboard?.writeText(inviteCode.value).then(() => {
    showToast('复制成功')
  }).catch(() => {
    showToast('复制失败，请手动复制')
  })
}

async function loadData() {
  try {
    const techId = userStore.userInfo?.technicianId || userStore.userInfo?.id || 0
    if (!techId) return
    const today = new Date()
    const monthAgo = new Date(today.getTime() - 30 * 86400000)
    const startDate = monthAgo.toISOString().split('T')[0]
    const endDate = today.toISOString().split('T')[0]

    const [promoRes, statsRes] = await Promise.all([
      getPromotion(techId),
      getPromotionStats(techId, startDate, endDate)
    ])
    const promoData = (promoRes as any).data || {}
    const statsData = (statsRes as any).data || {}
    inviteCode.value = promoData.promoCode || 'ZJT888'
    promotionStats.value = {
      clicks: statsData.totalClicks || 0,
      registrations: statsData.totalRegistrations || 0,
      visits: statsData.totalVisits || 0,
      conversionRate: statsData.conversionRate ? Number(statsData.conversionRate).toFixed(1) : '0.0'
    }
  } catch {
    // 模拟数据
    promotionStats.value = { clicks: 256, registrations: 48, visits: 32, conversionRate: '12.5' }
  }

  // 佣金
  try {
    const techId = userStore.userInfo?.technicianId || userStore.userInfo?.id || 0
    const commRes: any = await getCommissions(techId)
    const commData = commRes.data || {}
    commission.value = {
      settled: commData.settledAmount ? Number(commData.settledAmount).toFixed(2) : '0.00',
      pending: commData.pendingAmount ? Number(commData.pendingAmount).toFixed(2) : '0.00'
    }
    commissionList.value = commData.records || []
  } catch {
    commission.value = { settled: '680.00', pending: '320.00' }
    commissionList.value = [
      { id: 1, name: '张三到店消费', date: '2024-01-15', amount: '50.00', status: 'settled' },
      { id: 2, name: '李四注册成功', date: '2024-01-14', amount: '30.00', status: 'pending' },
      { id: 3, name: '王五到店消费', date: '2024-01-13', amount: '50.00', status: 'settled' }
    ]
  }

  // 排行
  try {
    const techId = userStore.userInfo?.technicianId || userStore.userInfo?.id || 0
    const rankRes: any = await getRanking(techId)
    const rankData = rankRes.data || {}
    rankList.value = rankData.rankings || []
  } catch {
    rankList.value = [
      { name: '王技师', count: 15 },
      { name: '李技师', count: 12 },
      { name: '张技师', count: 10 },
      { name: '赵技师', count: 8 },
      { name: '刘技师', count: 6 }
    ]
  }
}

onMounted(loadData)
</script>

<style scoped>
.promotion-page {
  padding: 12px;
}

.poster-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.poster-placeholder {
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.poster-text {
  margin-top: 8px;
  color: #999;
  font-size: 14px;
}

.poster-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.invite-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-top: 12px;
  text-align: center;
}

.invite-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.invite-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.code-text {
  font-size: 28px;
  font-weight: bold;
  color: #07C160;
  letter-spacing: 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 12px;
}

.stats-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.stats-value {
  font-size: 22px;
  font-weight: bold;
  color: #07C160;
}

.stats-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-top: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
}

.commission-summary {
  display: flex;
  margin-bottom: 12px;
}

.commission-item {
  flex: 1;
  text-align: center;
}

.commission-value {
  font-size: 20px;
  font-weight: bold;
  color: #07C160;
}

.commission-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.commission-record {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.commission-record:last-child {
  border-bottom: none;
}

.record-info {
  flex: 1;
}

.record-name {
  font-size: 14px;
  color: #333;
}

.record-date {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.record-amount {
  font-size: 14px;
  font-weight: bold;
  color: #07C160;
}

.record-amount.pending {
  color: #ff976a;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.rank-num {
  width: 28px;
  text-align: center;
}

.rank-medal {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
}

.rank-medal.gold { background: #FFD700; }
.rank-medal.silver { background: #C0C0C0; }
.rank-medal.bronze { background: #CD7F32; }

.rank-normal {
  font-size: 14px;
  color: #999;
}

.rank-name {
  flex: 1;
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

.rank-count {
  font-size: 14px;
  color: #07C160;
  font-weight: 500;
}
</style>
