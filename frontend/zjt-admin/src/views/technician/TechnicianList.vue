<template>
  <div class="technician-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="技师编号/姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="门店">
          <el-select v-model="searchForm.storeId" placeholder="全部" clearable>
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option
              v-for="item in TECHNICIAN_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="在岗">
          <el-select v-model="searchForm.isOnline" placeholder="全部" clearable>
            <el-option label="在岗" :value="1" />
            <el-option label="离岗" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增技师</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="technicianNo" label="技师编号" width="110" />
        <el-table-column prop="employeeName" label="姓名" width="90" />
        <el-table-column prop="storeName" label="门店" min-width="140" show-overflow-tooltip />
        <el-table-column prop="skillLevel" label="技能等级" width="90" align="center">
          <template #default="{ row }">
            {{ getSkillLevelLabel(row.skillLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="skilledItems" label="擅长项目" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="row.skilledItems">
              <el-tag
                v-for="item in parseSkilledItems(row.skilledItems)"
                :key="item"
                size="small"
                type="primary"
                effect="plain"
                style="margin: 2px 4px 2px 0"
              >
                {{ item }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="monthServiceCount" label="本月服务数" width="100" align="center" />
        <el-table-column prop="monthRevenue" label="本月业绩" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.monthRevenue?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="monthRating" label="评分" width="80" align="center">
          <template #default="{ row }">
            {{ row.monthRating?.toFixed(1) ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isOnline" label="在岗状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isOnline === 1"
              :disabled="row.status === 3"
              @change="handleOnlineChange(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.isOnline === 0"
              type="success"
              link
              size="small"
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>
            <el-button
              v-else
              type="warning"
              link
              size="small"
              @click="handleCheckOut(row)"
            >
              签退
            </el-button>
            <el-button type="info" link size="small" @click="handleSchedule(row)">排班</el-button>
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
    <TechnicianForm
      v-model:visible="formVisible"
      :technician-id="currentTechnicianId"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTechnicianList, checkIn, checkOut } from '@/api/store/technician'
import { getStoreList } from '@/api/store/info'
import { TECHNICIAN_STATUS_OPTIONS, SKILL_LEVEL_OPTIONS } from '@/types/technician'
import type { TechnicianInfo } from '@/types/technician'
import type { StoreInfo } from '@/types/store'
import TechnicianForm from './TechnicianForm.vue'

const loading = ref(false)
const router = useRouter()
const tableData = ref<TechnicianInfo[]>([])
const formVisible = ref(false)
const currentTechnicianId = ref<number | undefined>(undefined)
const storeOptions = ref<StoreInfo[]>([])

const searchForm = reactive({
  keyword: '',
  storeId: undefined as number | undefined,
  status: undefined as number | undefined,
  isOnline: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function getStatusLabel(status: number): string {
  return TECHNICIAN_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return TECHNICIAN_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

function getSkillLevelLabel(level: number): string {
  return SKILL_LEVEL_OPTIONS.find(o => o.value === level)?.label || '未知'
}

function parseSkilledItems(items: string): string[] {
  if (!items) return []
  return items.split(/[,，、]/).map(s => s.trim()).filter(Boolean)
}

function handleSchedule(row: TechnicianInfo) {
  router.push({ path: '/store/schedules', query: { technicianId: String(row.id) } })
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
    const res: any = await getTechnicianList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      storeId: searchForm.storeId,
      status: searchForm.status,
      isOnline: searchForm.isOnline,
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
  searchForm.storeId = undefined
  searchForm.status = undefined
  searchForm.isOnline = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  currentTechnicianId.value = undefined
  formVisible.value = true
}

function handleEdit(row: TechnicianInfo) {
  currentTechnicianId.value = row.id
  formVisible.value = true
}

async function handleOnlineChange(row: TechnicianInfo, val: boolean) {
  try {
    if (val) {
      await checkIn(row.id)
      ElMessage.success('签到成功')
    } else {
      await checkOut(row.id)
      ElMessage.success('签退成功')
    }
    fetchData()
  } catch {
    // 请求失败已在拦截器中处理
  }
}

async function handleCheckIn(row: TechnicianInfo) {
  try {
    await ElMessageBox.confirm(`确认技师「${row.employeeName || row.technicianNo}」签到？`, '签到确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })
    await checkIn(row.id)
    ElMessage.success('签到成功')
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

async function handleCheckOut(row: TechnicianInfo) {
  try {
    await ElMessageBox.confirm(`确认技师「${row.employeeName || row.technicianNo}」签退？`, '签退确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })
    await checkOut(row.id)
    ElMessage.success('签退成功')
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
.technician-list {
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
