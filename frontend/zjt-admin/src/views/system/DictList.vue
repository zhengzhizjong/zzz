<template>
  <div class="dict-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="字典编码/名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAddDict">新增字典</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 字典列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="dictCode" label="字典编码" width="180" />
        <el-table-column prop="dictName" label="字典名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewItems(row)">字典项</el-button>
            <el-button type="warning" link size="small" @click="handleEditDict(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteDict(row)">删除</el-button>
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

    <!-- 新增/编辑字典弹窗 -->
    <el-dialog
      v-model="dictDialogVisible"
      :title="isEditDict ? '编辑字典' : '新增字典'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="90px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="dictForm.dictCode" :disabled="isEditDict" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="dictForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="dictForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDict">确认</el-button>
      </template>
    </el-dialog>

    <!-- 字典项管理弹窗 -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="`字典项管理 - ${currentDictName}`"
      width="700px"
      destroy-on-close
    >
      <div style="margin-bottom: 12px">
        <el-button type="primary" size="small" @click="handleAddItem">新增字典项</el-button>
      </div>
      <el-table :data="dictItems" v-loading="itemLoading" stripe border size="small">
        <el-table-column prop="itemCode" label="编码" width="120" />
        <el-table-column prop="itemName" label="名称" width="120" />
        <el-table-column prop="itemValue" label="值" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditItem(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 新增/编辑字典项弹窗 -->
      <el-dialog
        v-model="itemFormVisible"
        :title="isEditItem ? '编辑字典项' : '新增字典项'"
        width="450px"
        append-to-body
        destroy-on-close
      >
        <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="80px">
          <el-form-item label="编码" prop="itemCode">
            <el-input v-model="itemForm.itemCode" placeholder="请输入字典项编码" />
          </el-form-item>
          <el-form-item label="名称" prop="itemName">
            <el-input v-model="itemForm.itemName" placeholder="请输入字典项名称" />
          </el-form-item>
          <el-form-item label="值" prop="itemValue">
            <el-input v-model="itemForm.itemValue" placeholder="请输入字典项值" />
          </el-form-item>
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="itemForm.sortOrder" :min="0" :max="9999" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="itemFormVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveItem">确认</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDictList, createDict, updateDict, deleteDict,
  getDictItems, createDictItem, updateDictItem, deleteDictItem,
  type DictInfo, type DictItemInfo,
} from '@/api/system/config'

const loading = ref(false)
const tableData = ref<DictInfo[]>([])
const dictDialogVisible = ref(false)
const isEditDict = ref(false)
const currentDictId = ref<number | undefined>(undefined)
const dictFormRef = ref<FormInstance>()

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const dictForm = reactive({
  dictCode: '',
  dictName: '',
  description: '',
})

const dictRules: FormRules = {
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
}

// 字典项相关
const itemDialogVisible = ref(false)
const itemFormVisible = ref(false)
const itemLoading = ref(false)
const isEditItem = ref(false)
const currentItem = ref<DictItemInfo | undefined>(undefined)
const currentDictIdForItems = ref<number>(0)
const currentDictName = ref('')
const dictItems = ref<DictItemInfo[]>([])
const itemFormRef = ref<FormInstance>()

const itemForm = reactive({
  itemCode: '',
  itemName: '',
  itemValue: '',
  sortOrder: 0,
})

const itemRules: FormRules = {
  itemCode: [{ required: true, message: '请输入字典项编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入字典项名称', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getDictList({
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

function handleAddDict() {
  isEditDict.value = false
  currentDictId.value = undefined
  dictForm.dictCode = ''
  dictForm.dictName = ''
  dictForm.description = ''
  dictDialogVisible.value = true
}

function handleEditDict(row: DictInfo) {
  isEditDict.value = true
  currentDictId.value = row.id
  dictForm.dictCode = row.dictCode
  dictForm.dictName = row.dictName
  dictForm.description = row.description
  dictDialogVisible.value = true
}

async function handleSaveDict() {
  if (!dictFormRef.value) return
  await dictFormRef.value.validate()
  try {
    if (isEditDict.value && currentDictId.value) {
      await updateDict(currentDictId.value, {
        dictName: dictForm.dictName,
        description: dictForm.description,
      })
      ElMessage.success('编辑成功')
    } else {
      await createDict({
        dictCode: dictForm.dictCode,
        dictName: dictForm.dictName,
        description: dictForm.description,
      })
      ElMessage.success('新增成功')
    }
    dictDialogVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleDeleteDict(row: DictInfo) {
  try {
    await ElMessageBox.confirm(`确认删除字典「${row.dictName}」？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

// 字典项管理
async function handleViewItems(row: DictInfo) {
  currentDictIdForItems.value = row.id
  currentDictName.value = row.dictName
  itemDialogVisible.value = true
  await fetchDictItems()
}

async function fetchDictItems() {
  itemLoading.value = true
  try {
    const res: any = await getDictItems(currentDictIdForItems.value)
    dictItems.value = res.data || []
  } catch {
    dictItems.value = []
  } finally {
    itemLoading.value = false
  }
}

function handleAddItem() {
  isEditItem.value = false
  currentItem.value = undefined
  itemForm.itemCode = ''
  itemForm.itemName = ''
  itemForm.itemValue = ''
  itemForm.sortOrder = 0
  itemFormVisible.value = true
}

function handleEditItem(row: DictItemInfo) {
  isEditItem.value = true
  currentItem.value = row
  itemForm.itemCode = row.itemCode
  itemForm.itemName = row.itemName
  itemForm.itemValue = row.itemValue
  itemForm.sortOrder = row.sortOrder
  itemFormVisible.value = true
}

async function handleSaveItem() {
  if (!itemFormRef.value) return
  await itemFormRef.value.validate()
  try {
    if (isEditItem.value && currentItem.value) {
      await updateDictItem(currentItem.value.id, {
        itemCode: itemForm.itemCode,
        itemName: itemForm.itemName,
        itemValue: itemForm.itemValue,
        sortOrder: itemForm.sortOrder,
      })
      ElMessage.success('编辑成功')
    } else {
      await createDictItem(currentDictIdForItems.value, {
        itemCode: itemForm.itemCode,
        itemName: itemForm.itemName,
        itemValue: itemForm.itemValue,
        sortOrder: itemForm.sortOrder,
      })
      ElMessage.success('新增成功')
    }
    itemFormVisible.value = false
    fetchDictItems()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleDeleteItem(row: DictItemInfo) {
  try {
    await ElMessageBox.confirm(`确认删除字典项「${row.itemName}」？`, '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteDictItem(row.id)
    ElMessage.success('删除成功')
    fetchDictItems()
  } catch {
    // 用户取消或请求失败
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.dict-list {
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
