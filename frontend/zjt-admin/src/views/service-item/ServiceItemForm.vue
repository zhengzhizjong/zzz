<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑服务项目' : '新增服务项目'"
    width="640px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="项目名称" prop="itemName">
        <el-input v-model="form.itemName" placeholder="请输入项目名称" maxlength="50" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option
            v-for="cat in categoryOptions"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" :controls="false" style="width: 100%" placeholder="价格" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成本价" prop="costPrice">
            <el-input-number v-model="form.costPrice" :min="0" :precision="2" :controls="false" style="width: 100%" placeholder="成本价" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="服务时长" prop="durationMinutes">
        <el-input-number v-model="form.durationMinutes" :min="1" :step="5" :controls="false" style="width: 100%" placeholder="服务时长（分钟）" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="提成方式" prop="commissionType">
            <el-select v-model="form.commissionType" placeholder="请选择" style="width: 100%">
              <el-option
                v-for="item in COMMISSION_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="提成值" prop="commissionValue">
            <el-input-number
              v-model="form.commissionValue"
              :min="0"
              :precision="2"
              :controls="false"
              style="width: 100%"
              :placeholder="form.commissionType === 2 ? '比例(%)' : '金额(元)'"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="是否套餐" prop="isPackage">
        <el-switch v-model="form.isPackage" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="标签" prop="tags">
        <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" maxlength="200" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" maxlength="500" />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :controls="false" style="width: 100%" placeholder="排序值，越小越靠前" />
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
import { getServiceItemDetail, createServiceItem, updateServiceItem } from '@/api/content/service-item'
import { COMMISSION_TYPE_OPTIONS } from '@/types/service-item'
import type { ServiceItemCreateRequest } from '@/types/service-item'

interface CategoryOption {
  id: number
  name: string
}

const props = defineProps<{
  visible: boolean
  itemId?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const categoryOptions = ref<CategoryOption[]>([])

const isEdit = computed(() => !!props.itemId)

const form = reactive<ServiceItemCreateRequest>({
  itemName: '',
  categoryId: undefined,
  price: undefined as unknown as number,
  costPrice: undefined,
  durationMinutes: undefined as unknown as number,
  commissionType: 1,
  commissionValue: undefined,
  isPackage: 0,
  tags: '',
  description: '',
  sortOrder: 0,
})

const rules: FormRules = {
  itemName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
  ],
  durationMinutes: [
    { required: true, message: '请输入服务时长', trigger: 'blur' },
  ],
}

async function fetchCategoryOptions() {
  // TODO: 替换为实际的分类API
  categoryOptions.value = [
    { id: 1, name: '推拿按摩' },
    { id: 2, name: '艾灸理疗' },
    { id: 3, name: '拔罐刮痧' },
    { id: 4, name: '足浴养生' },
    { id: 5, name: '套餐项目' },
  ]
}

watch(() => props.visible, async (val) => {
  if (val && props.itemId) {
    try {
      const res: any = await getServiceItemDetail(props.itemId)
      const data = res.data
      Object.assign(form, {
        itemName: data.itemName,
        categoryId: data.categoryId,
        price: data.price,
        costPrice: data.costPrice,
        durationMinutes: data.durationMinutes,
        commissionType: data.commissionType,
        commissionValue: data.commissionValue,
        isPackage: data.isPackage,
        tags: data.tags,
        description: data.description,
        sortOrder: data.sortOrder,
      })
    } catch {
      ElMessage.error('获取项目信息失败')
    }
  }
})

function resetForm() {
  Object.assign(form, {
    itemName: '',
    categoryId: undefined,
    price: undefined,
    costPrice: undefined,
    durationMinutes: undefined,
    commissionType: 1,
    commissionValue: undefined,
    isPackage: 0,
    tags: '',
    description: '',
    sortOrder: 0,
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
    if (isEdit.value && props.itemId) {
      await updateServiceItem(props.itemId, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createServiceItem({ ...form })
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
  fetchCategoryOptions()
})
</script>
