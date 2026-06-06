<template>
  <div class="coupon-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option
              v-for="item in COUPON_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable>
            <el-option
              v-for="item in COUPON_TYPE_OPTIONS"
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
          <el-button type="primary" @click="handleAdd">新增优惠券</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            {{ getCouponTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column label="优惠值" width="110" align="right">
          <template #default="{ row }">
            {{ row.type === 1 ? `¥${row.value}` : `${row.value}折` }}
          </template>
        </el-table-column>
        <el-table-column label="门槛" width="110" align="right">
          <template #default="{ row }">
            {{ row.minAmount > 0 ? `满¥${row.minAmount}` : '无门槛' }}
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100" align="center">
          <template #default="{ row }">
            {{ row.stock - row.usedCount }} / {{ row.stock }}
          </template>
        </el-table-column>
        <el-table-column label="有效期" min-width="180" align="center">
          <template #default="{ row }">
            {{ row.validStartTime }} ~ {{ row.validEndTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleIssue(row)">发放</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑优惠券' : '新增优惠券'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option
              v-for="item in COUPON_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠值" prop="value">
          <el-input-number v-model="form.value" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期" prop="validDateRange">
          <el-date-picker
            v-model="form.validDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 发放弹窗 -->
    <el-dialog
      v-model="issueVisible"
      title="发放优惠券"
      width="500px"
      destroy-on-close
    >
      <el-form ref="issueFormRef" :model="issueForm" :rules="issueFormRules" label-width="80px">
        <el-form-item label="优惠券">
          <el-input :model-value="currentCoupon?.name" disabled />
        </el-form-item>
        <el-form-item label="会员ID" prop="memberIds">
          <el-select
            v-model="issueForm.memberIds"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入会员ID"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="issueLoading" @click="handleIssueSubmit">确认发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCouponList, createCoupon, updateCoupon, issueCoupon, type CouponInfo } from '@/api/trade/coupon'

const COUPON_TYPE_OPTIONS = [
  { value: 1, label: '满减券' },
  { value: 2, label: '折扣券' },
]

const COUPON_STATUS_OPTIONS = [
  { value: 1, label: '启用', type: 'success' },
  { value: 2, label: '停用', type: 'danger' },
  { value: 3, label: '已过期', type: 'info' },
]

const loading = ref(false)
const submitLoading = ref(false)
const issueLoading = ref(false)
const tableData = ref<CouponInfo[]>([])
const formVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const issueVisible = ref(false)
const currentCoupon = ref<CouponInfo | null>(null)
const issueFormRef = ref<FormInstance>()

const searchForm = reactive({
  status: undefined as number | undefined,
  type: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  name: '',
  type: undefined as number | undefined,
  value: 0,
  minAmount: 0,
  stock: 100,
  validDateRange: null as [string, string] | null,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  validDateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }],
}

const issueForm = reactive({
  memberIds: [] as number[],
})

const issueFormRules: FormRules = {
  memberIds: [{ required: true, message: '请输入会员ID', trigger: 'change' }],
}

function getCouponTypeLabel(type: number): string {
  return COUPON_TYPE_OPTIONS.find(o => o.value === type)?.label || '未知'
}

function getStatusLabel(status: number): string {
  return COUPON_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return COUPON_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: searchForm.status,
      type: searchForm.type,
    }
    const res: any = await getCouponList(params)
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
  searchForm.type = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = undefined
  Object.assign(form, {
    name: '',
    type: undefined,
    value: 0,
    minAmount: 0,
    stock: 100,
    validDateRange: null,
  })
  formVisible.value = true
}

function handleEdit(row: CouponInfo) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    name: row.name,
    type: row.type,
    value: row.value,
    minAmount: row.minAmount,
    stock: row.stock,
    validDateRange: [row.validStartTime, row.validEndTime] as [string, string],
  })
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (!form.validDateRange || form.validDateRange.length !== 2) return

  submitLoading.value = true
  try {
    const data = {
      name: form.name,
      type: form.type,
      value: form.value,
      minAmount: form.minAmount,
      stock: form.stock,
      validStartTime: form.validDateRange[0],
      validEndTime: form.validDateRange[1],
    }
    if (isEdit.value && currentId.value) {
      await updateCoupon(currentId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createCoupon(data)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

function handleIssue(row: CouponInfo) {
  currentCoupon.value = row
  issueForm.memberIds = []
  issueVisible.value = true
}

async function handleIssueSubmit() {
  if (!issueFormRef.value) return
  await issueFormRef.value.validate()
  if (!currentCoupon.value) return

  issueLoading.value = true
  try {
    await issueCoupon(currentCoupon.value.id, issueForm.memberIds)
    ElMessage.success('发放成功')
    issueVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    issueLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.coupon-list {
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
