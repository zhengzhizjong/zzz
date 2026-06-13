<template>
  <div class="config-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="配置键/名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 配置列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="configKey" label="配置键" width="200" />
        <el-table-column prop="configName" label="配置名称" width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑配置"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="配置键">
          <el-input :model-value="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置名称">
          <el-input :model-value="editForm.configName" disabled />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="editForm.configValue" type="textarea" :rows="4" placeholder="请输入配置值" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getConfigList, updateConfig, type ConfigInfo } from '@/api/system/config'

const loading = ref(false)
const tableData = ref<ConfigInfo[]>([])
const editDialogVisible = ref(false)
const currentConfigId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const editForm = reactive({
  configKey: '',
  configName: '',
  configValue: '',
})

const editRules: FormRules = {
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getConfigList({
      page: pagination.page,
      pageSize: pagination.pageSize,
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
  searchForm.keyword = ''
  pagination.page = 1
  fetchData()
}

function handleEdit(row: ConfigInfo) {
  currentConfigId.value = row.id
  editForm.configKey = row.configKey
  editForm.configName = row.configName
  editForm.configValue = row.configValue
  editDialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    await updateConfig(currentConfigId.value!, { configValue: editForm.configValue })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.config-list {
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
