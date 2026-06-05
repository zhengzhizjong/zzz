<template>
  <div class="service-item-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="项目名称/编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option
              v-for="item in SERVICE_ITEM_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部" clearable>
            <el-option
              v-for="cat in categoryOptions"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增项目</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="itemNo" label="项目编号" width="120" />
        <el-table-column prop="itemName" label="项目名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="commissionType" label="提成方式" width="100" align="center">
          <template #default="{ row }">
            {{ getCommissionLabel(row.commissionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              下架
            </el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              上架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <ServiceItemForm
      v-model:visible="formVisible"
      :item-id="currentItemId"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceItemList, updateServiceItemStatus } from '@/api/content/service-item'
import { SERVICE_ITEM_STATUS_OPTIONS, COMMISSION_TYPE_OPTIONS } from '@/types/service-item'
import type { ServiceItem } from '@/types/service-item'
import ServiceItemForm from './ServiceItemForm.vue'

interface CategoryOption {
  id: number
  name: string
}

const loading = ref(false)
const tableData = ref<ServiceItem[]>([])
const formVisible = ref(false)
const currentItemId = ref<number | undefined>(undefined)
const categoryOptions = ref<CategoryOption[]>([])

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined,
  categoryId: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function getStatusLabel(status: number): string {
  return SERVICE_ITEM_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return SERVICE_ITEM_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

function getCommissionLabel(type: number): string {
  return COMMISSION_TYPE_OPTIONS.find(o => o.value === type)?.label || '未知'
}

async function fetchCategoryOptions() {
  // TODO: 替换为实际的分类API
  categoryOptions.value = [
    { id: 1, name: '推拿按摩' },
    { id: 2, name: '艾灸理疗' },
    { id: 3, name: '拔罐刮痧' },
    { id: 4, name: '足浴养生' },
    { id: 5, name: '套餐项目' },
  ]
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getServiceItemList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status,
      categoryId: searchForm.categoryId,
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
  searchForm.keyword = ''
  searchForm.status = undefined
  searchForm.categoryId = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  currentItemId.value = undefined
  formVisible.value = true
}

function handleEdit(row: ServiceItem) {
  currentItemId.value = row.id
  formVisible.value = true
}

async function handleToggleStatus(row: ServiceItem) {
  const newStatus = row.status === 1 ? 2 : 1
  const label = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确认将项目「${row.itemName}」${label}？`, '状态变更', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateServiceItemStatus(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

onMounted(() => {
  fetchCategoryOptions()
  fetchData()
})
</script>

<style scoped lang="scss">
.service-item-list {
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
