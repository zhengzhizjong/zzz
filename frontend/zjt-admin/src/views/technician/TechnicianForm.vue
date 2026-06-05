<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑技师' : '新增技师'"
    width="560px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
      <el-form-item label="员工" prop="employeeId">
        <el-input-number v-model="form.employeeId" :min="1" :controls="false" style="width: 100%" placeholder="请输入员工ID" />
      </el-form-item>
      <el-form-item label="技能等级" prop="skillLevel">
        <el-select v-model="form.skillLevel" placeholder="请选择技能等级" style="width: 100%">
          <el-option
            v-for="item in SKILL_LEVEL_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="擅长项目" prop="skilledItems">
        <el-input v-model="form.skilledItems" type="textarea" :rows="3" placeholder="请输入擅长项目，多个项目用逗号分隔" />
      </el-form-item>
      <el-form-item label="默认排班" prop="defaultSchedule">
        <el-input v-model="form.defaultSchedule" placeholder="请输入默认排班" maxlength="100" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTechnicianDetail, createTechnician, updateTechnician } from '@/api/store/technician'
import { getStoreList } from '@/api/store/info'
import { SKILL_LEVEL_OPTIONS } from '@/types/technician'
import type { TechnicianCreateRequest } from '@/types/technician'
import type { StoreInfo } from '@/types/store'

const props = defineProps<{
  visible: boolean
  technicianId?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const storeOptions = ref<StoreInfo[]>([])

const isEdit = computed(() => !!props.technicianId)

const form = reactive<TechnicianCreateRequest>({
  storeId: undefined as unknown as number,
  employeeId: undefined as unknown as number,
  skillLevel: 1,
  skilledItems: '',
  defaultSchedule: '',
})

const rules: FormRules = {
  storeId: [
    { required: true, message: '请选择门店', trigger: 'change' },
  ],
  employeeId: [
    { required: true, message: '请输入员工ID', trigger: 'blur' },
  ],
}

async function fetchStoreOptions() {
  try {
    const res: any = await getStoreList({ page: 1, pageSize: 999 })
    storeOptions.value = res.data.list || []
  } catch {
    storeOptions.value = []
  }
}

watch(() => props.visible, async (val) => {
  if (val && props.technicianId) {
    try {
      const res: any = await getTechnicianDetail(props.technicianId)
      const data = res.data
      Object.assign(form, {
        storeId: data.storeId,
        employeeId: data.employeeId,
        skillLevel: data.skillLevel,
        skilledItems: data.skilledItems,
        defaultSchedule: data.defaultSchedule,
      })
    } catch {
      ElMessage.error('获取技师信息失败')
    }
  }
})

function resetForm() {
  Object.assign(form, {
    storeId: undefined,
    employeeId: undefined,
    skillLevel: 1,
    skilledItems: '',
    defaultSchedule: '',
  })
  formRef.value?.resetFields()
}

function handleClose() {
  resetForm()
  emit('update:visible', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value && props.technicianId) {
      await updateTechnician(props.technicianId, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createTechnician({ ...form })
      ElMessage.success('创建成功')
    }
    emit('success')
    handleClose()
  } catch {
    // 请求失败已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchStoreOptions()
})
</script>
