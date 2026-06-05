<template>
  <div class="employee-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="工号/姓名/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="全部部门" clearable>
            <el-option
              v-for="dept in DEPARTMENT_OPTIONS"
              :key="dept"
              :label="dept"
              :value="dept"
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增员工</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="employeeNo" label="工号" width="110" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="入职时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="danger"
              link
              size="small"
              @click="handleToggleStatus(row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              @click="handleToggleStatus(row, 1)"
            >
              启用
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="currentEmployeeId ? '编辑员工' : '新增员工'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="form.employeeNo" placeholder="请输入工号" :disabled="!!currentEmployeeId" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
            <el-option
              v-for="dept in DEPARTMENT_OPTIONS"
              :key="dept"
              :label="dept"
              :value="dept"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入职位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getEmployeeList, createEmployee, updateEmployee, updateEmployeeStatus, type EmployeeInfo } from '@/api/user/employee'

const DEPARTMENT_OPTIONS = ['管理部', '运营部', '技术部', '客服部', '市场部', '财务部']
const STATUS_OPTIONS = [
  { value: 1, label: '在职', type: 'success' },
  { value: 0, label: '离职', type: 'danger' },
]

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<EmployeeInfo[]>([])
const formVisible = ref(false)
const currentEmployeeId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  keyword: '',
  department: undefined as string | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  employeeNo: '',
  name: '',
  phone: '',
  department: '',
  position: '',
})

const formRules: FormRules = {
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
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
    const res: any = await getEmployeeList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      department: searchForm.department,
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
  searchForm.department = undefined
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

function resetForm() {
  form.employeeNo = ''
  form.name = ''
  form.phone = ''
  form.department = ''
  form.position = ''
}

function handleAdd() {
  currentEmployeeId.value = undefined
  resetForm()
  formVisible.value = true
}

function handleEdit(row: EmployeeInfo) {
  currentEmployeeId.value = row.id
  form.employeeNo = row.employeeNo
  form.name = row.name
  form.phone = row.phone
  form.department = row.department
  form.position = row.position
  formVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (currentEmployeeId.value) {
      await updateEmployee(currentEmployeeId.value, {
        name: form.name,
        phone: form.phone,
        department: form.department,
        position: form.position,
      })
      ElMessage.success('更新成功')
    } else {
      await createEmployee({
        employeeNo: form.employeeNo,
        name: form.name,
        phone: form.phone,
        department: form.department,
        position: form.position,
      })
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row: EmployeeInfo, status: number) {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${action}员工「${row.name}」？`, `${action}确认`, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateEmployeeStatus(row.id, status)
    ElMessage.success(`${action}成功`)
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
.employee-list {
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
