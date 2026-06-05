<template>
  <div class="timeslot-config">
    <el-card shadow="never">
      <template #header>
        <span class="card-title">时段配置</span>
      </template>

      <!-- 门店选择 -->
      <el-form :inline="true" class="store-select-form">
        <el-form-item label="选择门店">
          <el-select v-model="selectedStoreId" placeholder="请选择门店" @change="handleStoreChange">
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 配置表单 -->
      <el-form
        v-if="selectedStoreId"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="config-form"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="营业开始时间" prop="businessStartTime">
              <el-time-select
                v-model="form.businessStartTime"
                :max-time="form.businessEndTime"
                placeholder="开始时间"
                start="06:00"
                step="00:30"
                end="23:00"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业结束时间" prop="businessEndTime">
              <el-time-select
                v-model="form.businessEndTime"
                :min-time="form.businessStartTime"
                placeholder="结束时间"
                start="06:00"
                step="00:30"
                end="23:30"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="时段间隔(分钟)" prop="slotDurationMinutes">
              <el-select v-model="form.slotDurationMinutes" placeholder="请选择时段间隔" style="width: 100%">
                <el-option :value="15" label="15分钟" />
                <el-option :value="30" label="30分钟" />
                <el-option :value="45" label="45分钟" />
                <el-option :value="60" label="60分钟" />
                <el-option :value="90" label="90分钟" />
                <el-option :value="120" label="120分钟" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="休息开始时间" prop="restStartTime">
              <el-time-select
                v-model="form.restStartTime"
                :max-time="form.restEndTime"
                placeholder="休息开始（可选）"
                start="06:00"
                step="00:30"
                end="23:00"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="休息结束时间" prop="restEndTime">
              <el-time-select
                v-model="form.restEndTime"
                :min-time="form.restStartTime"
                placeholder="休息结束（可选）"
                start="06:00"
                step="00:30"
                end="23:30"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="saveLoading" @click="handleSave">保存配置</el-button>
        </el-form-item>
      </el-form>

      <!-- 时段预览 -->
      <div v-if="previewSlots.length > 0" class="slot-preview">
        <el-divider content-position="left">时段预览</el-divider>
        <div class="slot-grid">
          <div
            v-for="slot in previewSlots"
            :key="slot.startTime"
            class="slot-item"
            :class="{ 'slot-rest': slot.isRest }"
          >
            <span class="slot-time">{{ slot.startTime }} - {{ slot.endTime }}</span>
            <el-tag v-if="slot.isRest" type="info" size="small">休息</el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTimeSlotConfig, createTimeSlotConfig, updateTimeSlotConfig } from '@/api/store/timeslot'
import { getStoreList } from '@/api/store/info'
import type { StoreInfo } from '@/types/store'

interface SlotPreview {
  startTime: string
  endTime: string
  isRest: boolean
}

const formRef = ref<FormInstance>()
const saveLoading = ref(false)
const selectedStoreId = ref<number | undefined>(undefined)
const storeOptions = ref<StoreInfo[]>([])
const configId = ref<number | undefined>(undefined)

const form = reactive({
  businessStartTime: '',
  businessEndTime: '',
  slotDurationMinutes: 30,
  restStartTime: '',
  restEndTime: '',
})

const rules: FormRules = {
  businessStartTime: [
    { required: true, message: '请选择营业开始时间', trigger: 'change' },
  ],
  businessEndTime: [
    { required: true, message: '请选择营业结束时间', trigger: 'change' },
  ],
  slotDurationMinutes: [
    { required: true, message: '请选择时段间隔', trigger: 'change' },
  ],
}

const previewSlots = computed<SlotPreview[]>(() => {
  if (!form.businessStartTime || !form.businessEndTime || !form.slotDurationMinutes) {
    return []
  }

  const slots: SlotPreview[] = []
  const startMinutes = timeToMinutes(form.businessStartTime)
  const endMinutes = timeToMinutes(form.businessEndTime)
  const duration = form.slotDurationMinutes

  if (startMinutes >= endMinutes) return []

  const restStart = form.restStartTime ? timeToMinutes(form.restStartTime) : -1
  const restEnd = form.restEndTime ? timeToMinutes(form.restEndTime) : -1

  let current = startMinutes
  while (current + duration <= endMinutes) {
    const slotStart = current
    const slotEnd = current + duration
    const isRest = restStart >= 0 && restEnd > restStart && slotStart >= restStart && slotEnd <= restEnd

    slots.push({
      startTime: minutesToTime(slotStart),
      endTime: minutesToTime(slotEnd),
      isRest,
    })
    current = slotEnd
  }

  return slots
})

function timeToMinutes(time: string): number {
  const [h, m] = time.split(':').map(Number)
  return h * 60 + m
}

function minutesToTime(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

async function fetchStoreOptions() {
  try {
    const res: any = await getStoreList({ page: 1, pageSize: 999 })
    storeOptions.value = res.data.list || []
  } catch {
    storeOptions.value = []
  }
}

async function handleStoreChange(storeId: number) {
  configId.value = undefined
  Object.assign(form, {
    businessStartTime: '',
    businessEndTime: '',
    slotDurationMinutes: 30,
    restStartTime: '',
    restEndTime: '',
  })

  if (!storeId) return

  try {
    const res: any = await getTimeSlotConfig(storeId)
    if (res.data) {
      configId.value = res.data.id
      Object.assign(form, {
        businessStartTime: res.data.businessStartTime,
        businessEndTime: res.data.businessEndTime,
        slotDurationMinutes: res.data.slotDurationMinutes,
        restStartTime: res.data.restStartTime || '',
        restEndTime: res.data.restEndTime || '',
      })
    }
  } catch {
    // 无配置，使用默认值
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!selectedStoreId.value) {
    ElMessage.warning('请先选择门店')
    return
  }

  saveLoading.value = true
  try {
    const data = {
      storeId: selectedStoreId.value,
      businessStartTime: form.businessStartTime,
      businessEndTime: form.businessEndTime,
      slotDurationMinutes: form.slotDurationMinutes,
      restStartTime: form.restStartTime || undefined,
      restEndTime: form.restEndTime || undefined,
    }

    if (configId.value) {
      await updateTimeSlotConfig(configId.value, data)
    } else {
      const res: any = await createTimeSlotConfig(data)
      if (res.data?.id) {
        configId.value = res.data.id
      }
    }
    ElMessage.success('保存成功')
  } catch {
    // 请求失败已在拦截器中处理
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  fetchStoreOptions()
})
</script>

<style scoped lang="scss">
.timeslot-config {
  padding: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.store-select-form {
  margin-bottom: 20px;
}

.config-form {
  max-width: 700px;
}

.slot-preview {
  margin-top: 20px;
}

.slot-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.slot-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  background: #fff;

  &.slot-rest {
    background: #f5f7fa;
    border-color: #e4e7ed;
    color: #909399;
  }
}

.slot-time {
  white-space: nowrap;
}
</style>
