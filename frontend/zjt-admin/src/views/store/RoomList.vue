<template>
  <div class="room-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="门店">
          <el-select v-model="searchForm.storeId" placeholder="全部门店" clearable>
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="searchForm.roomType" placeholder="全部类型" clearable>
            <el-option
              v-for="item in ROOM_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option
              v-for="item in ROOM_STATUS_OPTIONS"
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
          <el-button type="primary" @click="handleAdd">新增房间</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="roomNo" label="房号" width="100" />
        <el-table-column prop="roomName" label="房间名称" min-width="140" />
        <el-table-column prop="roomType" label="类型" width="100" align="center">
          <template #default="{ row }">
            {{ getRoomTypeLabel(row.roomType) }}
          </template>
        </el-table-column>
        <el-table-column prop="floor" label="楼层" width="80" align="center" />
        <el-table-column prop="capacity" label="容量" width="80" align="center">
          <template #default="{ row }">
            {{ row.capacity }}人
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd: number) => handleStatusChange(row, cmd)">
              <el-button type="warning" link size="small">
                变更状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="opt in ROOM_STATUS_OPTIONS"
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
      :title="isEdit ? '编辑房间' : '新增房间'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="form.storeId" placeholder="请选择门店" style="width: 100%">
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="请输入房号" />
        </el-form-item>
        <el-form-item label="房间名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入房间名称" />
        </el-form-item>
        <el-form-item label="房间类型" prop="roomType">
          <el-select v-model="form.roomType" placeholder="请选择类型" style="width: 100%">
            <el-option
              v-for="item in ROOM_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="form.floor" :min="-5" :max="99" style="width: 100%" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="设备配置" prop="equipment">
          <el-input v-model="form.equipment" type="textarea" :rows="3" placeholder="请输入设备配置" />
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
import { ArrowDown } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getRoomList, createRoom, updateRoom, updateRoomStatus, type RoomInfo } from '@/api/store/room'
import { getStoreList } from '@/api/store/info'
import type { StoreInfo } from '@/types/store'

const ROOM_TYPE_OPTIONS = [
  { value: 1, label: '普通房' },
  { value: 2, label: 'VIP房' },
  { value: 3, label: '套房' },
]

const ROOM_STATUS_OPTIONS = [
  { value: 1, label: '空闲', type: 'success' },
  { value: 2, label: '使用中', type: 'warning' },
  { value: 3, label: '维护中', type: 'info' },
  { value: 4, label: '已停用', type: 'danger' },
]

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<RoomInfo[]>([])
const storeOptions = ref<StoreInfo[]>([])
const formVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  storeId: undefined as number | undefined,
  roomType: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  storeId: undefined as number | undefined,
  roomNo: '',
  roomName: '',
  roomType: undefined as number | undefined,
  floor: 1,
  capacity: 1,
  equipment: '',
})

const formRules: FormRules = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' }],
  roomName: [{ required: true, message: '请输入房间名称', trigger: 'blur' }],
  roomType: [{ required: true, message: '请选择房间类型', trigger: 'change' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
}

function getRoomTypeLabel(type: number): string {
  return ROOM_TYPE_OPTIONS.find(o => o.value === type)?.label || '未知'
}

function getStatusLabel(status: number): string {
  return ROOM_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return ROOM_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
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
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      storeId: searchForm.storeId,
      roomType: searchForm.roomType,
      status: searchForm.status,
    }
    const res: any = await getRoomList(params)
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
  searchForm.storeId = undefined
  searchForm.roomType = undefined
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = undefined
  Object.assign(form, {
    storeId: undefined,
    roomNo: '',
    roomName: '',
    roomType: undefined,
    floor: 1,
    capacity: 1,
    equipment: '',
  })
  formVisible.value = true
}

function handleEdit(row: RoomInfo) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    storeId: row.storeId,
    roomNo: row.roomNo,
    roomName: row.roomName,
    roomType: row.roomType,
    floor: row.floor,
    capacity: row.capacity,
    equipment: row.equipment,
  })
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && currentId.value) {
      await updateRoom(currentId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createRoom(form)
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

async function handleStatusChange(row: RoomInfo, newStatus: number) {
  if (newStatus === row.status) return
  const label = getStatusLabel(newStatus)
  try {
    await ElMessageBox.confirm(`确认将房间「${row.roomName}」设为${label}状态？`, '状态变更', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateRoomStatus(row.id, newStatus)
    ElMessage.success('状态变更成功')
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
.room-list {
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
