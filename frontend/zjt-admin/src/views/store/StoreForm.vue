<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑门店' : '新增门店'"
    width="680px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="门店名称" prop="storeName">
        <el-input v-model="form.storeName" placeholder="请输入门店名称" maxlength="50" />
      </el-form-item>
      <el-form-item label="门店类型" prop="storeType">
        <el-select v-model="form.storeType" placeholder="请选择门店类型" style="width: 100%">
          <el-option
            v-for="item in STORE_TYPE_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="省份" prop="provinceCode">
            <el-input v-model="form.provinceCode" placeholder="省份编码" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="城市" prop="cityCode">
            <el-input v-model="form.cityCode" placeholder="城市编码" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="区县" prop="districtCode">
            <el-input v-model="form.districtCode" placeholder="区县编码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="详细地址" prop="address">
        <el-input v-model="form.address" placeholder="请输入详细地址" maxlength="200" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="纬度" prop="latitude">
            <el-input-number v-model="form.latitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" placeholder="纬度" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="经度" prop="longitude">
            <el-input-number v-model="form.longitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" placeholder="经度" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人" maxlength="20" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="营业开始" prop="businessStartTime">
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
          <el-form-item label="营业结束" prop="businessEndTime">
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
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getStoreDetail, createStore, updateStore } from '@/api/store/info'
import { STORE_TYPE_OPTIONS } from '@/types/store'
import type { StoreCreateRequest } from '@/types/store'

const props = defineProps<{
  visible: boolean
  storeId?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const isEdit = computed(() => !!props.storeId)

const form = reactive<StoreCreateRequest>({
  storeName: '',
  storeType: 1,
  provinceCode: '',
  cityCode: '',
  districtCode: '',
  address: '',
  latitude: undefined,
  longitude: undefined,
  contactName: '',
  contactPhone: '',
  businessStartTime: '',
  businessEndTime: '',
})

const rules: FormRules = {
  storeName: [
    { required: true, message: '请输入门店名称', trigger: 'blur' },
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

watch(() => props.visible, async (val) => {
  if (val && props.storeId) {
    try {
      const res: any = await getStoreDetail(props.storeId)
      const data = res.data
      Object.assign(form, {
        storeName: data.storeName,
        storeType: data.storeType,
        provinceCode: data.provinceCode,
        cityCode: data.cityCode,
        districtCode: data.districtCode,
        address: data.address,
        latitude: data.latitude,
        longitude: data.longitude,
        contactName: data.contactName,
        contactPhone: data.contactPhone,
        businessStartTime: data.businessStartTime,
        businessEndTime: data.businessEndTime,
      })
    } catch {
      ElMessage.error('获取门店信息失败')
    }
  }
})

function resetForm() {
  Object.assign(form, {
    storeName: '',
    storeType: 1,
    provinceCode: '',
    cityCode: '',
    districtCode: '',
    address: '',
    latitude: undefined,
    longitude: undefined,
    contactName: '',
    contactPhone: '',
    businessStartTime: '',
    businessEndTime: '',
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
    if (isEdit.value && props.storeId) {
      await updateStore(props.storeId, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createStore({ ...form })
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
</script>
