<template>
  <div class="role-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="角色名称/编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleAssignPermission(row)">权限</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
      :title="currentRoleId ? '编辑角色' : '新增角色'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码" :disabled="!!currentRoleId" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配弹窗 -->
    <el-dialog
      v-model="permissionVisible"
      title="权限分配"
      width="500px"
      destroy-on-close
    >
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="currentPermissionIds"
        :props="{ label: 'label', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionLoading" @click="handlePermissionSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, assignPermissions, getPermissionTree, type RoleInfo, type PermissionNode } from '@/api/user/role'

const loading = ref(false)
const submitLoading = ref(false)
const permissionLoading = ref(false)
const tableData = ref<RoleInfo[]>([])
const formVisible = ref(false)
const permissionVisible = ref(false)
const currentRoleId = ref<number | undefined>(undefined)
const currentPermissionIds = ref<number[]>([])
const formRef = ref<FormInstance>()
const treeRef = ref<any>()
const permissionTree = ref<PermissionNode[]>([])

const searchForm = reactive({
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  name: '',
  code: '',
  description: '',
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getRoleList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
    })
    // 后端返回data是数组，不是分页格式
    const list = Array.isArray(res.data) ? res.data : (res.data?.list || [])
    tableData.value = list
    if (res.data?.pagination) {
      pagination.total = res.data.pagination.total
    } else {
      pagination.total = list.length
    }
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
  pagination.page = 1
  fetchData()
}

function resetForm() {
  form.name = ''
  form.code = ''
  form.description = ''
}

function handleAdd() {
  currentRoleId.value = undefined
  resetForm()
  formVisible.value = true
}

function handleEdit(row: RoleInfo) {
  currentRoleId.value = row.id
  form.name = row.roleName
  form.code = row.roleCode
  form.description = row.description
  formVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (currentRoleId.value) {
      await updateRole(currentRoleId.value, {
        name: form.name,
        code: form.code,
        description: form.description,
      })
      ElMessage.success('更新成功')
    } else {
      await createRole({
        name: form.name,
        code: form.code,
        description: form.description,
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

async function handleDelete(row: RoleInfo) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？删除后不可恢复`, '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

async function handleAssignPermission(row: RoleInfo) {
  currentRoleId.value = row.id
  currentPermissionIds.value = row.permissions || []
  try {
    const res: any = await getPermissionTree()
    permissionTree.value = res.data || []
  } catch {
    permissionTree.value = []
  }
  permissionVisible.value = true
}

async function handlePermissionSubmit() {
  if (!currentRoleId.value) return
  const checkedKeys = treeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = treeRef.value?.getHalfCheckedKeys() || []
  const permissionIds = [...checkedKeys, ...halfCheckedKeys]

  permissionLoading.value = true
  try {
    await assignPermissions(currentRoleId.value, permissionIds)
    ElMessage.success('权限分配成功')
    permissionVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    permissionLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.role-list {
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
