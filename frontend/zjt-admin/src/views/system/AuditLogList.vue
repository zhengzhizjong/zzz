<template>
  <div class="audit-log-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="操作人/内容" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="searchForm.module" placeholder="全部模块" clearable>
            <el-option
              v-for="item in MODULE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.action" placeholder="全部类型" clearable>
            <el-option
              v-for="item in ACTION_OPTIONS"
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="120" align="center">
          <template #default="{ row }">
            {{ getModuleLabel(row.module) }}
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">
              {{ getActionLabel(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="操作对象" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
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
    <el-dialog v-model="detailVisible" title="日志详情" width="560px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="时间">{{ currentDetail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentDetail.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ getModuleLabel(currentDetail.module) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getActionType(currentDetail.action)" size="small">
            {{ getActionLabel(currentDetail.action) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作对象" :span="2">{{ currentDetail.target }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentDetail.ip }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentDetail.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="详细信息" :span="2">
          <div class="detail-content">{{ currentDetail.detail || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogList, type AuditLogInfo } from '@/api/system/audit-log'

const MODULE_OPTIONS = [
  { value: 'user', label: '用户管理' },
  { value: 'store', label: '门店管理' },
  { value: 'technician', label: '技师管理' },
  { value: 'appointment', label: '预约管理' },
  { value: 'order', label: '订单管理' },
  { value: 'coupon', label: '优惠券' },
  { value: 'system', label: '系统管理' },
]

const ACTION_OPTIONS = [
  { value: 'create', label: '新增' },
  { value: 'update', label: '修改' },
  { value: 'delete', label: '删除' },
  { value: 'login', label: '登录' },
  { value: 'export', label: '导出' },
]

const loading = ref(false)
const tableData = ref<AuditLogInfo[]>([])
const detailVisible = ref(false)
const currentDetail = ref<AuditLogInfo | null>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  keyword: '',
  module: undefined as string | undefined,
  action: undefined as string | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function getModuleLabel(module: string): string {
  return MODULE_OPTIONS.find(o => o.value === module)?.label || module
}

function getActionLabel(action: string): string {
  return ACTION_OPTIONS.find(o => o.value === action)?.label || action
}

function getActionType(action: string): string {
  if (action === 'create') return 'success'
  if (action === 'update') return 'warning'
  if (action === 'delete') return 'danger'
  if (action === 'login') return ''
  return 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      module: searchForm.module,
      action: searchForm.action,
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res: any = await getAuditLogList(params)
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
  searchForm.keyword = ''
  searchForm.module = undefined
  searchForm.action = undefined
  dateRange.value = null
  pagination.page = 1
  fetchData()
}

function handleViewDetail(row: AuditLogInfo) {
  currentDetail.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.audit-log-list {
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

.detail-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
  font-size: 13px;
  line-height: 1.6;
}
</style>
