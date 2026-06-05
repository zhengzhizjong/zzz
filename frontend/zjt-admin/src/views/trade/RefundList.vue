<template>
  <div class="refund-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
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
        <el-form-item label="搜索">
          <el-input v-model="searchForm.keyword" placeholder="退款号/订单号/客户名" clearable @keyup.enter="handleSearch" />
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
        <el-table-column prop="refundNo" label="退款号" width="160" />
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="amount" label="退款金额" width="110" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              link
              size="small"
              @click="handleReject(row)"
            >
              拒绝
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

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveVisible" title="退款审批" width="500px" destroy-on-close>
      <el-descriptions :column="1" border v-if="currentRefund">
        <el-descriptions-item label="退款号">{{ currentRefund.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRefund.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentRefund.customerName }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ currentRefund.amount?.toFixed(2) ?? '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRefund.reason }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="approveAction === 'reject'" style="margin-top: 16px">
        <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectFormRules" label-width="80px">
          <el-form-item label="拒绝原因" prop="rejectReason">
            <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button
          v-if="approveAction === 'approve'"
          type="success"
          :loading="approveLoading"
          @click="handleApproveSubmit"
        >
          确认通过
        </el-button>
        <el-button
          v-if="approveAction === 'reject'"
          type="danger"
          :loading="approveLoading"
          @click="handleRejectSubmit"
        >
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getRefundList, approveRefund, rejectRefund, type RefundInfo } from '@/api/trade/refund'

// 待审核(橙)/已通过(绿)/已拒绝(红)/已退款(灰)
const STATUS_OPTIONS = [
  { value: 0, label: '待审核', type: 'warning' },
  { value: 1, label: '已通过', type: 'success' },
  { value: 2, label: '已拒绝', type: 'danger' },
  { value: 3, label: '已退款', type: 'info' },
]

const loading = ref(false)
const approveLoading = ref(false)
const tableData = ref<RefundInfo[]>([])
const approveVisible = ref(false)
const approveAction = ref<'approve' | 'reject'>('approve')
const currentRefund = ref<RefundInfo | null>(null)
const rejectFormRef = ref<FormInstance>()

const searchForm = reactive({
  status: undefined as number | undefined,
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const rejectForm = reactive({
  rejectReason: '',
})

const rejectFormRules: FormRules = {
  rejectReason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }],
}

function getStatusLabel(status: number): string {
  return STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getRefundList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: searchForm.status,
      keyword: searchForm.keyword || undefined,
    })
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
  searchForm.status = undefined
  searchForm.keyword = ''
  pagination.page = 1
  fetchData()
}

function handleApprove(row: RefundInfo) {
  currentRefund.value = row
  approveAction.value = 'approve'
  approveVisible.value = true
}

function handleReject(row: RefundInfo) {
  currentRefund.value = row
  approveAction.value = 'reject'
  rejectForm.rejectReason = ''
  approveVisible.value = true
}

async function handleApproveSubmit() {
  if (!currentRefund.value) return
  approveLoading.value = true
  try {
    await approveRefund(currentRefund.value.id)
    ElMessage.success('退款已通过')
    approveVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    approveLoading.value = false
  }
}

async function handleRejectSubmit() {
  if (!currentRefund.value) return
  const valid = await rejectFormRef.value?.validate().catch(() => false)
  if (!valid) return

  approveLoading.value = true
  try {
    await rejectRefund(currentRefund.value.id, { rejectReason: rejectForm.rejectReason })
    ElMessage.success('退款已拒绝')
    approveVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    approveLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.refund-list {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
