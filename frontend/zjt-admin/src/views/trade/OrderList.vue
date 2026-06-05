<template>
  <div class="order-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="门店">
          <el-select v-model="searchForm.storeId" placeholder="全部门店" clearable>
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option
              v-for="item in STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="searchForm.keyword" placeholder="订单号/客户名/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column label="客户" width="140">
          <template #default="{ row }">
            <div>{{ row.customerName }}</div>
            <div class="sub-text">{{ row.customerPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="门店" min-width="140" show-overflow-tooltip />
        <el-table-column prop="serviceName" label="服务项目" width="120" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              type="success"
              link
              size="small"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              link
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="560px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="订单号">{{ currentDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDetail.status)" size="small">
            {{ getStatusLabel(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ currentDetail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDetail.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="门店">{{ currentDetail.storeName }}</el-descriptions-item>
        <el-descriptions-item label="服务项目">{{ currentDetail.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentDetail.amount?.toFixed(2) ?? '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentDetail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentDetail.paidAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentDetail.completedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, completeOrder, cancelOrder, type OrderInfo } from '@/api/trade/order'
import { getStoreList } from '@/api/store/info'
import type { StoreInfo } from '@/types/store'

// 待支付(橙)/已支付(蓝)/服务中(绿)/已完成(灰)/已取消(红)/已退款(灰)
const STATUS_OPTIONS = [
  { value: 0, label: '待支付', type: 'warning' },
  { value: 1, label: '已支付', type: '' },
  { value: 2, label: '服务中', type: 'success' },
  { value: 3, label: '已完成', type: 'info' },
  { value: 4, label: '已取消', type: 'danger' },
  { value: 5, label: '已退款', type: 'info' },
]

const loading = ref(false)
const tableData = ref<OrderInfo[]>([])
const storeOptions = ref<StoreInfo[]>([])
const detailVisible = ref(false)
const currentDetail = ref<OrderInfo | null>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  storeId: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function getStatusLabel(status: number): string {
  return STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

async function fetchStoreOptions() {
  try {
    const res: any = await getStoreList({ page: 1, pageSize: 999 })
    storeOptions.value = res.data.list || []
  } catch {
    storeOptions.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      storeId: searchForm.storeId,
      status: searchForm.status,
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res: any = await getOrderList(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.pagination.total
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.storeId = undefined
  searchForm.status = undefined
  searchForm.keyword = ''
  dateRange.value = null
  pagination.page = 1
  fetchData()
}

async function handleViewDetail(row: OrderInfo) {
  try {
    const res: any = await getOrderDetail(row.id)
    currentDetail.value = res.data || null
  } catch {
    currentDetail.value = row
  }
  detailVisible.value = true
}

async function handleComplete(row: OrderInfo) {
  try {
    await ElMessageBox.confirm(`确认完成订单「${row.orderNo}」？`, '完成确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })
    await completeOrder(row.id)
    ElMessage.success('订单已完成')
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

async function handleCancel(row: OrderInfo) {
  try {
    await ElMessageBox.confirm(`确认取消订单「${row.orderNo}」？取消后不可恢复`, '取消确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cancelOrder(row.id)
    ElMessage.success('订单已取消')
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

onMounted(() => {
  fetchStoreOptions()
  fetchData()
})
</script>

<style scoped lang="scss">
.order-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
