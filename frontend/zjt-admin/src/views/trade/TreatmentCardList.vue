<template>
  <div class="treatment-card-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input v-model="searchForm.keyword" placeholder="卡名/项目/会员" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option
              v-for="item in CARD_STATUS_OPTIONS"
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
          <el-button type="primary" @click="handleAdd">新增疗程卡</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="name" label="卡名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目" min-width="120" show-overflow-tooltip />
        <el-table-column prop="totalCount" label="总次数" width="90" align="center" />
        <el-table-column prop="remainingCount" label="剩余" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.remainingCount <= 2 }">{{ row.remainingCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberName" label="会员" width="120" />
        <el-table-column prop="price" label="价格" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewUsage(row)">使用记录</el-button>
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

    <!-- 新增弹窗 -->
    <el-dialog
      v-model="formVisible"
      title="新增疗程卡"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="卡名" prop="name">
          <el-input v-model="form.name" placeholder="请输入疗程卡名称" />
        </el-form-item>
        <el-form-item label="项目" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入关联项目" />
        </el-form-item>
        <el-form-item label="总次数" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="会员ID" prop="memberId">
          <el-input-number v-model="form.memberId" :min="1" style="width: 100%" placeholder="请输入会员ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 使用记录弹窗 -->
    <el-dialog
      v-model="usageVisible"
      title="使用记录"
      width="640px"
      destroy-on-close
    >
      <el-table :data="usageList" v-loading="usageLoading" stripe border max-height="400">
        <el-table-column prop="usedAt" label="使用时间" width="170" />
        <el-table-column prop="serviceName" label="服务项目" min-width="150" show-overflow-tooltip />
        <el-table-column prop="technicianName" label="技师" width="120" />
        <el-table-column prop="remainingAfter" label="剩余次数" width="100" align="center" />
      </el-table>
      <template #footer>
        <el-button @click="usageVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTreatmentCardList, createTreatmentCard, getTreatmentCardUsageHistory, type TreatmentCardInfo } from '@/api/trade/treatment-card'

const CARD_STATUS_OPTIONS = [
  { value: 1, label: '正常', type: 'success' },
  { value: 2, label: '已用完', type: 'warning' },
  { value: 3, label: '已过期', type: 'info' },
  { value: 4, label: '已退款', type: 'danger' },
]

const loading = ref(false)
const submitLoading = ref(false)
const usageLoading = ref(false)
const tableData = ref<TreatmentCardInfo[]>([])
const formVisible = ref(false)
const usageVisible = ref(false)
const usageList = ref<any[]>([])
const formRef = ref<FormInstance>()

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  name: '',
  projectName: '',
  totalCount: 10,
  price: 0,
  memberId: undefined as number | undefined,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入疗程卡名称', trigger: 'blur' }],
  projectName: [{ required: true, message: '请输入关联项目', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入总次数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  memberId: [{ required: true, message: '请输入会员ID', trigger: 'blur' }],
}

function getStatusLabel(status: number): string {
  return CARD_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return CARD_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status,
    }
    const res: any = await getTreatmentCardList(params)
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
  Object.assign(form, {
    name: '',
    projectName: '',
    totalCount: 10,
    price: 0,
    memberId: undefined,
  })
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  submitLoading.value = true
  try {
    await createTreatmentCard(form)
    ElMessage.success('创建成功')
    formVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

async function handleViewUsage(row: TreatmentCardInfo) {
  usageVisible.value = true
  usageLoading.value = true
  try {
    const res: any = await getTreatmentCardUsageHistory(row.id)
    usageList.value = res.data || []
  } catch {
    usageList.value = []
  } finally {
    usageLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.treatment-card-list {
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

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
