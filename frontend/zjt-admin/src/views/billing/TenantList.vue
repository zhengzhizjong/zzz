<template>
  <div class="tenant-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="租户名称/联系人" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option
              v-for="item in TENANT_STATUS_OPTIONS"
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
          <el-button type="primary" @click="handleAdd">新增租户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="tenantName" label="租户名称" min-width="150" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="industry" label="行业" width="100" />
        <el-table-column prop="planName" label="当前套餐" width="120" />
        <el-table-column label="门店配额" width="120" align="center">
          <template #default="{ row }">
            {{ row.currentStores }} / {{ row.maxStores }}
          </template>
        </el-table-column>
        <el-table-column label="技师配额" width="120" align="center">
          <template #default="{ row }">
            {{ row.currentTechnicians }} / {{ row.maxTechnicians }}
          </template>
        </el-table-column>
        <el-table-column prop="expireAt" label="到期时间" width="170" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleWhiteLabel(row)">白标配置</el-button>
            <el-dropdown trigger="click" @command="(cmd: number) => handleStatusChange(row, cmd)">
              <el-button type="warning" link size="small">
                变更状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="opt in TENANT_STATUS_OPTIONS"
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
      :title="currentTenantId ? '编辑租户' : '新增租户'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="租户名称" prop="tenantName">
          <el-input v-model="editForm.tenantName" placeholder="请输入租户名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="editForm.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="行业" prop="industry">
          <el-input v-model="editForm.industry" placeholder="请输入行业" />
        </el-form-item>
        <el-form-item label="最大门店数" prop="maxStores">
          <el-input-number v-model="editForm.maxStores" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="最大技师数" prop="maxTechnicians">
          <el-input-number v-model="editForm.maxTechnicians" :min="1" :max="99999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确认</el-button>
      </template>
    </el-dialog>

    <!-- 白标配置弹窗 -->
    <el-dialog
      v-model="whiteLabelVisible"
      title="白标配置"
      width="500px"
      destroy-on-close
    >
      <el-form ref="whiteLabelFormRef" :model="whiteLabelForm" label-width="100px">
        <el-form-item label="Logo URL">
          <el-input v-model="whiteLabelForm.logoUrl" placeholder="请输入Logo URL" />
        </el-form-item>
        <el-form-item label="主题色">
          <el-color-picker v-model="whiteLabelForm.primaryColor" />
        </el-form-item>
        <el-form-item label="自定义域名">
          <el-input v-model="whiteLabelForm.customDomain" placeholder="请输入自定义域名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="whiteLabelVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveWhiteLabel">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getTenantList, updateTenantStatus, createTenant, updateTenant, updateWhiteLabel, type TenantInfo } from '@/api/billing/tenant'

const TENANT_STATUS_OPTIONS = [
  { value: 0, label: '试用', type: '' },
  { value: 1, label: '正式', type: 'success' },
  { value: 2, label: '欠费', type: 'warning' },
  { value: 3, label: '停用', type: 'danger' },
]

const loading = ref(false)
const tableData = ref<TenantInfo[]>([])
const formVisible = ref(false)
const whiteLabelVisible = ref(false)
const currentTenantId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()
const whiteLabelFormRef = ref<FormInstance>()

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const editForm = reactive({
  tenantName: '',
  contactName: '',
  contactPhone: '',
  industry: '',
  maxStores: 5,
  maxTechnicians: 20,
})

const formRules: FormRules = {
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}

const whiteLabelForm = reactive({
  logoUrl: '',
  primaryColor: '#07C160',
  customDomain: '',
})

function getStatusLabel(status: number): string {
  return TENANT_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return TENANT_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getTenantList({
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
  currentTenantId.value = undefined
  editForm.tenantName = ''
  editForm.contactName = ''
  editForm.contactPhone = ''
  editForm.industry = ''
  editForm.maxStores = 5
  editForm.maxTechnicians = 20
  formVisible.value = true
}

function handleEdit(row: TenantInfo) {
  currentTenantId.value = row.id
  editForm.tenantName = row.tenantName
  editForm.contactName = row.contactName
  editForm.contactPhone = row.contactPhone
  editForm.industry = row.industry
  editForm.maxStores = row.maxStores
  editForm.maxTechnicians = row.maxTechnicians
  formVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (currentTenantId.value) {
      await updateTenant(currentTenantId.value, editForm)
    } else {
      await createTenant(editForm)
    }
    ElMessage.success(currentTenantId.value ? '编辑成功' : '新增成功')
    formVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

function handleWhiteLabel(row: TenantInfo) {
  currentTenantId.value = row.id
  whiteLabelForm.logoUrl = row.logoUrl || ''
  whiteLabelForm.primaryColor = row.primaryColor || '#07C160'
  whiteLabelForm.customDomain = row.customDomain || ''
  whiteLabelVisible.value = true
}

async function handleSaveWhiteLabel() {
  try {
    await updateWhiteLabel(currentTenantId.value!, whiteLabelForm)
    ElMessage.success('白标配置保存成功')
    whiteLabelVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleStatusChange(row: TenantInfo, newStatus: number) {
  if (newStatus === row.status) return
  const label = getStatusLabel(newStatus)
  try {
    await ElMessageBox.confirm(`确认将租户「${row.tenantName}」设为${label}状态？`, '状态变更', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateTenantStatus(row.id, newStatus)
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
.tenant-list {
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
