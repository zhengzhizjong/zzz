<template>
  <div class="store-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="门店名称/编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option
              v-for="item in STORE_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增门店</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="storeNo" label="门店编号" width="120" />
        <el-table-column prop="storeName" label="门店名称" min-width="150" />
        <el-table-column prop="storeType" label="类型" width="80" align="center">
          <template #default="{ row }">
            {{ getStoreTypeLabel(row.storeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="营业时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.businessStartTime }} - {{ row.businessEndTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd: number) => handleStatusChange(row, cmd)">
              <el-button type="warning" link size="small">
                变更状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="opt in STORE_STATUS_OPTIONS"
                    :key="opt.value"
                    :command="opt.value"
                    :disabled="opt.value === row.status"
                  >
                    <el-tag :type="opt.type" size="small" style="margin-right: 4px">{{ opt.label }}</el-tag>
                    {{ opt.value === row.status ? '（当前）' : '' }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
    <StoreForm
      v-model:visible="formVisible"
      :store-id="currentStoreId"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getStoreList, updateStoreStatus } from '@/api/store/info'
import { STORE_STATUS_OPTIONS, STORE_TYPE_OPTIONS } from '@/types/store'
import type { StoreInfo } from '@/types/store'
import StoreForm from './StoreForm.vue'

const loading = ref(false)
const tableData = ref<StoreInfo[]>([])
const formVisible = ref(false)
const currentStoreId = ref<number | undefined>(undefined)

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function getStatusLabel(status: number): string {
  return STORE_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return STORE_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

function getStoreTypeLabel(type: number): string {
  return STORE_TYPE_OPTIONS.find(o => o.value === type)?.label || '未知'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getStoreList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status,
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
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  currentStoreId.value = undefined
  formVisible.value = true
}

function handleEdit(row: StoreInfo) {
  currentStoreId.value = row.id
  formVisible.value = true
}

async function handleStatusChange(row: StoreInfo, newStatus: number) {
  if (newStatus === row.status) return
  const label = getStatusLabel(newStatus)
  try {
    await ElMessageBox.confirm(`确认将门店「${row.storeName}」设为${label}状态？`, '状态变更', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateStoreStatus(row.id, newStatus)
    ElMessage.success('状态变更成功')
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.store-list {
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
