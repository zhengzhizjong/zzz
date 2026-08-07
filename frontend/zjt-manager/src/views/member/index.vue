<template>
  <div class="member-page">
    <!-- 搜索 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="搜索">
          <el-input v-model="filters.keyword" placeholder="姓名/手机号" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="filters.levelId" placeholder="全部" clearable style="width: 140px;">
            <el-option label="普通会员" :value="1" />
            <el-option label="银卡会员" :value="2" />
            <el-option label="金卡会员" :value="3" />
            <el-option label="钻石会员" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 会员列表 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="memberList" stripe v-loading="loading">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="level" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelType(row.level)" size="small">{{ levelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="100">
          <template #default="{ row }">¥{{ row.balance }}</template>
        </el-table-column>
        <el-table-column prop="visitCount" label="到店次数" width="100" />
        <el-table-column prop="lastVisitAt" label="最近到店" width="180" />
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 会员详情对话框 -->
    <el-dialog v-model="detailVisible" title="会员详情" width="700px">
      <template v-if="currentMember">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentMember.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentMember.phone }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">
            <el-tag :type="levelType(currentMember.level)" size="small">{{ levelLabel(currentMember.level) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="余额">¥{{ currentMember.balance }}</el-descriptions-item>
          <el-descriptions-item label="到店次数">{{ currentMember.visitCount }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentMember.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="最近到店" :span="2">{{ currentMember.lastVisitAt }}</el-descriptions-item>
        </el-descriptions>

        <!-- 理疗卡 -->
        <div v-if="currentMember.treatmentCards?.length" style="margin-top: 20px;">
          <h4 style="margin: 0 0 8px;">理疗卡</h4>
          <el-table :data="currentMember.treatmentCards" border size="small">
            <el-table-column prop="cardName" label="卡名称" />
            <el-table-column prop="totalTimes" label="总次数" width="80" />
            <el-table-column prop="remainingTimes" label="剩余次数" width="80" />
            <el-table-column prop="expireDate" label="到期日" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '有效' : '已过期' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 健康档案 -->
        <div v-if="currentMember.healthProfile" style="margin-top: 20px;">
          <h4 style="margin: 0 0 8px;">健康档案</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="体质类型">{{ currentMember.healthProfile.constitution || '-' }}</el-descriptions-item>
            <el-descriptions-item label="过敏史">{{ currentMember.healthProfile.allergies || '无' }}</el-descriptions-item>
            <el-descriptions-item label="主要症状" :span="2">{{ currentMember.healthProfile.symptoms || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ currentMember.healthProfile.notes || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 最近订单 -->
        <div v-if="currentMember.recentOrders?.length" style="margin-top: 20px;">
          <h4 style="margin: 0 0 8px;">最近订单</h4>
          <el-table :data="currentMember.recentOrders" border size="small">
            <el-table-column prop="orderNo" label="订单号" width="160" />
            <el-table-column prop="serviceName" label="服务项目" />
            <el-table-column prop="amount" label="金额" width="80">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="160" />
          </el-table>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMemberList, getMemberDetail } from '@/api/member'

const userStore = useUserStore()

const loading = ref(false)
const detailVisible = ref(false)
const currentMember = ref<Record<string, any> | null>(null)

const filters = reactive({ keyword: '', levelId: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const memberList = ref<Array<Record<string, any>>>([])

function levelType(level: number) {
  const map: Record<number, string> = { 1: 'info', 2: '', 3: 'warning', 4: 'danger' }
  return map[level] || ''
}

function levelLabel(level: number) {
  const map: Record<number, string> = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员', 4: '钻石会员' }
  return map[level] || '未知'
}

function resetFilters() {
  filters.keyword = ''
  filters.levelId = ''
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize, storeId: userStore.storeId }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.levelId) params.levelId = filters.levelId
    const res: any = await getMemberList(params)
    memberList.value = res.data?.list || res.data?.records || res.data || []
    pagination.total = res.data?.pagination?.total || res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function handleViewDetail(row: Record<string, any>) {
  try {
    const res: any = await getMemberDetail(row.id)
    currentMember.value = res.data || row
  } catch {
    currentMember.value = row
  }
  detailVisible.value = true
}

onMounted(() => loadData())
</script>

<style scoped>
.member-page {
  padding: 0;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
