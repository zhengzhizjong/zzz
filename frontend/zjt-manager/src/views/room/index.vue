<template>
  <div class="room-page">
    <!-- 顶部操作 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="房间类型">
          <el-select v-model="filters.roomType" placeholder="全部" clearable style="width: 140px;">
            <el-option label="普通" :value="1" />
            <el-option label="双人" :value="2" />
            <el-option label="VIP" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px;">
            <el-option label="空闲" :value="0" />
            <el-option label="使用中" :value="1" />
            <el-option label="维护中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="success" @click="handleAdd">新增房间</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房间列表 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="roomList" stripe v-loading="loading">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="roomNo" label="房间号" width="120" />
        <el-table-column prop="name" label="房间名" width="150" />
        <el-table-column prop="roomType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="roomTypeTag(row.roomType || row.type)" size="small">{{ roomTypeLabel(row.roomType || row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status !== 0"
              type="success"
              link
              size="small"
              @click="handleChangeStatus(row, 0)"
            >设为空闲</el-button>
            <el-button
              v-if="row.status !== 2"
              type="warning"
              link
              size="small"
              @click="handleChangeStatus(row, 2)"
            >设为维护</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑房间' : '新增房间'" width="480px">
      <el-form :model="roomForm" label-width="80px">
        <el-form-item label="房间号" required>
          <el-input v-model="roomForm.roomNo" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="房间名" required>
          <el-input v-model="roomForm.name" placeholder="请输入房间名" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="roomForm.roomType" placeholder="请选择类型" style="width: 100%;">
            <el-option label="普通" :value="1" />
            <el-option label="双人" :value="2" />
            <el-option label="VIP" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roomForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getRoomList, createRoom, updateRoom, updateRoomStatus } from '@/api/room'

const userStore = useUserStore()
const loading = ref(false)
const saveLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const roomList = ref<Array<Record<string, any>>>([])

const filters = reactive({ roomType: '' as number | string, status: '' as number | string })
const roomForm = reactive({
  id: null as number | null,
  roomNo: '',
  name: '',
  roomType: 1 as number,
  description: ''
})

function roomTypeLabel(type: number | string) {
  const map: Record<number, string> = { 1: '普通', 2: '双人', 3: 'VIP' }
  return map[type as number] || type
}

function roomTypeTag(type: number | string) {
  const map: Record<number, string> = { 1: 'info', 2: '', 3: 'warning' }
  return map[type as number] || ''
}

function statusLabel(status: number) {
  const map: Record<number, string> = { 0: '空闲', 1: '使用中', 2: '维护中' }
  return map[status] || '未知'
}

function statusTag(status: number) {
  const map: Record<number, string> = { 0: 'success', 1: 'primary', 2: 'warning' }
  return map[status] || 'info'
}

function resetFilters() {
  filters.roomType = ''
  filters.status = ''
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = { storeId: userStore.storeId }
    if (filters.roomType) params.roomType = filters.roomType
    if (filters.status !== '') params.status = filters.status
    const res: any = await getRoomList(params)
    roomList.value = res.data?.list || res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  roomForm.id = null
  roomForm.roomNo = ''
  roomForm.name = ''
  roomForm.roomType = 1
  roomForm.description = ''
  dialogVisible.value = true
}

function handleEdit(row: Record<string, any>) {
  isEdit.value = true
  roomForm.id = row.id
  roomForm.roomNo = row.roomNo
  roomForm.name = row.name
  roomForm.roomType = row.roomType || row.type || 1
  roomForm.description = row.description || ''
  dialogVisible.value = true
}

async function handleSave() {
  if (!roomForm.roomNo || !roomForm.name || !roomForm.roomType) {
    ElMessage.warning('请填写必要信息')
    return
  }
  saveLoading.value = true
  try {
    const data = {
      storeId: userStore.storeId,
      roomNo: roomForm.roomNo,
      name: roomForm.name,
      roomType: roomForm.roomType,
      description: roomForm.description
    }
    if (isEdit.value && roomForm.id) {
      await updateRoom(roomForm.id, data)
      ElMessage.success('编辑成功')
    } else {
      await createRoom(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function handleChangeStatus(row: Record<string, any>, status: number) {
  const label = statusLabel(status)
  await ElMessageBox.confirm(`确定将房间 ${row.name} 设为${label}吗？`, '状态变更确认')
  try {
    await updateRoomStatus(row.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch {
    ElMessage.error('状态更新失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.room-page {
  padding: 0;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
</style>
