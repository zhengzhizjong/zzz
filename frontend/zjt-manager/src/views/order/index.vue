<template>
  <div class="order-page">
    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="订单状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px;">
            <el-option label="待支付" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已退款" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="filters.keyword" placeholder="订单编号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="orderList" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单编号" width="160" />
        <el-table-column prop="memberName" label="会员" width="100" />
        <el-table-column prop="technicianName" label="技师" width="100" />
        <el-table-column prop="serviceName" label="服务项目" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" />
        <el-table-column prop="createdAt" label="下单时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 2" type="danger" link size="small" @click="handleRefund(row)">退款</el-button>
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

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusType(currentOrder.status)" size="small">{{ statusLabel(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="会员">{{ currentOrder.memberName }}</el-descriptions-item>
        <el-descriptions-item label="技师">{{ currentOrder.technicianName }}</el-descriptions-item>
        <el-descriptions-item label="服务项目" :span="2">{{ currentOrder.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentOrder.amount }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" :span="2">{{ currentOrder.completedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <!-- 订单服务明细 -->
      <div v-if="currentOrder?.items?.length" style="margin-top: 16px;">
        <h4 style="margin: 0 0 8px;">服务明细</h4>
        <el-table :data="currentOrder.items" border size="small">
          <el-table-column prop="serviceName" label="服务项目" />
          <el-table-column prop="technicianName" label="技师" width="100" />
          <el-table-column prop="duration" label="时长(分钟)" width="100" />
          <el-table-column prop="price" label="单价" width="80">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getOrderList, getOrderDetail, refundOrder } from '@/api/order'

const userStore = useUserStore()

const loading = ref(false)
const detailVisible = ref(false)
const currentOrder = ref<Record<string, any> | null>(null)

const filters = reactive({
  status: '' as number | string,
  dateRange: null as string[] | null,
  keyword: ''
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const orderList = ref<Array<Record<string, any>>>([])

function statusType(status: number) {
  const map: Record<number, string> = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger', 5: 'info' }
  return map[status] || 'info'
}

function statusLabel(status: number) {
  const map: Record<number, string> = { 1: '待支付', 2: '已支付', 3: '已完成', 4: '已退款', 5: '已取消' }
  return map[status] || '未知'
}

function resetFilters() {
  filters.status = ''
  filters.dateRange = null
  filters.keyword = ''
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize, storeId: userStore.storeId }
    if (filters.status) params.status = filters.status
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    const res: any = await getOrderList(params)
    orderList.value = res.data?.list || res.data?.records || res.data || []
    pagination.total = res.data?.pagination?.total || res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function handleViewDetail(row: Record<string, any>) {
  try {
    const res: any = await getOrderDetail(row.id)
    currentOrder.value = res.data || row
  } catch {
    currentOrder.value = row
  }
  detailVisible.value = true
}

async function handleRefund(row: Record<string, any>) {
  await ElMessageBox.confirm(`确定对订单 ${row.orderNo} 进行退款吗？退款金额 ¥${row.amount}`, '退款确认', { type: 'warning' })
  await refundOrder(row.id)
  ElMessage.success('退款申请已提交')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.order-page {
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
