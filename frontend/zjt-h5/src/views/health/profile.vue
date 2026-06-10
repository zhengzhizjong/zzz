<template>
  <div class="health-profile-page">
    <!-- 体质类型 -->
    <div class="section-card">
      <div class="section-title">体质类型</div>
      <div class="section-desc">请选择您的体质类型（单选）</div>
      <van-radio-group v-model="constitutionType" direction="horizontal">
        <div class="chip-grid">
          <van-radio
            v-for="item in constitutionOptions"
            :key="item.value"
            :name="item.value"
            checked-color="#07C160"
            icon-size="0"
          >
            <div class="chip-item" :class="{ active: constitutionType === item.value }">
              {{ item.label }}
            </div>
          </van-radio>
        </div>
      </van-radio-group>
    </div>

    <!-- 过敏史 -->
    <div class="section-card">
      <div class="section-title">过敏史</div>
      <div class="section-desc">请选择您的过敏情况（可多选）</div>
      <van-checkbox-group v-model="allergiesSelected" direction="horizontal">
        <div class="chip-grid">
          <van-checkbox
            v-for="item in allergiesOptions"
            :key="item"
            :name="item"
            checked-color="#07C160"
            icon-size="0"
            @click="onAllergiesClick(item)"
          >
            <div class="chip-item" :class="{ active: allergiesSelected.includes(item) }">
              {{ item }}
            </div>
          </van-checkbox>
        </div>
      </van-checkbox-group>
      <van-field
        v-model="allergiesCustom"
        placeholder="其他过敏史请在此补充"
        border
        class="custom-field"
      />
    </div>

    <!-- 禁忌症 -->
    <div class="section-card">
      <div class="section-title">禁忌症</div>
      <div class="section-desc">请选择您的禁忌情况（可多选）</div>
      <van-checkbox-group v-model="contraindicationsSelected" direction="horizontal">
        <div class="chip-grid">
          <van-checkbox
            v-for="item in contraindicationsOptions"
            :key="item"
            :name="item"
            checked-color="#07C160"
            icon-size="0"
            @click="onContraindicationsClick(item)"
          >
            <div class="chip-item" :class="{ active: contraindicationsSelected.includes(item) }">
              {{ item }}
            </div>
          </van-checkbox>
        </div>
      </van-checkbox-group>
      <van-field
        v-model="contraindicationsCustom"
        placeholder="其他禁忌症请在此补充"
        border
        class="custom-field"
      />
    </div>

    <!-- 既往病史 -->
    <div class="section-card">
      <div class="section-title">既往病史</div>
      <div class="section-desc">请选择您的病史情况（可多选）</div>
      <van-checkbox-group v-model="medicalHistorySelected" direction="horizontal">
        <div class="chip-grid">
          <van-checkbox
            v-for="item in medicalHistoryOptions"
            :key="item"
            :name="item"
            checked-color="#07C160"
            icon-size="0"
            @click="onMedicalHistoryClick(item)"
          >
            <div class="chip-item" :class="{ active: medicalHistorySelected.includes(item) }">
              {{ item }}
            </div>
          </van-checkbox>
        </div>
      </van-checkbox-group>
      <van-field
        v-model="medicalHistoryCustom"
        placeholder="其他病史请在此补充"
        border
        class="custom-field"
      />
    </div>

    <!-- 当前用药 -->
    <div class="section-card">
      <div class="section-title">当前用药</div>
      <div class="section-desc">请选择您当前的用药情况（可多选）</div>
      <van-checkbox-group v-model="medicationSelected" direction="horizontal">
        <div class="chip-grid">
          <van-checkbox
            v-for="item in medicationOptions"
            :key="item"
            :name="item"
            checked-color="#07C160"
            icon-size="0"
            @click="onMedicationClick(item)"
          >
            <div class="chip-item" :class="{ active: medicationSelected.includes(item) }">
              {{ item }}
            </div>
          </van-checkbox>
        </div>
      </van-checkbox-group>
      <van-field
        v-model="medicationCustom"
        placeholder="其他用药情况请在此补充"
        border
        class="custom-field"
      />
    </div>

    <!-- 其他备注 -->
    <div class="section-card">
      <div class="section-title">其他备注</div>
      <van-field
        v-model="notes"
        type="textarea"
        placeholder="如有其他需要补充的健康信息，请在此填写"
        rows="3"
        maxlength="500"
        show-word-limit
        class="notes-field"
      />
    </div>

    <!-- 保存按钮 -->
    <div class="save-btn-wrap">
      <van-button
        type="success"
        block
        round
        :loading="submitting"
        loading-text="保存中..."
        @click="onSubmit"
      >
        保存健康档案
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { getHealthProfile, createHealthProfile } from '@/api/health'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 体质类型选项
const constitutionOptions = [
  { label: '平和质', value: 'pinghe' },
  { label: '气虚质', value: 'qixu' },
  { label: '阳虚质', value: 'yangxu' },
  { label: '阴虚质', value: 'yinxu' },
  { label: '痰湿质', value: 'tanshi' },
  { label: '湿热质', value: 'shire' },
  { label: '血瘀质', value: 'xueyu' },
  { label: '气郁质', value: 'qiyu' },
  { label: '特禀质', value: 'tebing' }
]

// 过敏史选项
const allergiesOptions = ['花粉', '海鲜', '药物(青霉素等)', '尘螨', '食物(牛奶/鸡蛋等)', '金属', '化妆品', '无']
// 禁忌症选项
const contraindicationsOptions = ['高血压', '心脏病', '糖尿病', '皮肤病', '传染病', '骨质疏松', '出血性疾病', '孕期/经期', '无']
// 既往病史选项
const medicalHistoryOptions = ['颈椎病', '腰椎病', '关节炎', '失眠', '胃病', '头痛/偏头痛', '手术史', '骨折史', '无']
// 当前用药选项
const medicationOptions = ['降压药', '降糖药', '抗凝药', '激素类', '中药', '止痛药', '无']

const constitutionType = ref('')

const allergiesSelected = ref<string[]>([])
const allergiesCustom = ref('')

const contraindicationsSelected = ref<string[]>([])
const contraindicationsCustom = ref('')

const medicalHistorySelected = ref<string[]>([])
const medicalHistoryCustom = ref('')

const medicationSelected = ref<string[]>([])
const medicationCustom = ref('')

const notes = ref('')
const submitting = ref(false)

onMounted(() => {
  if (!userStore.isLogin || !userStore.userInfo?.id) {
    router.replace({ path: '/login', query: { redirect: '/health/profile' } })
    return
  }
  loadHealthProfile()
})

async function loadHealthProfile() {
  try {
    const memberId = userStore.userInfo!.id
    const res: any = await getHealthProfile(memberId)
    const data = res.data || {}

    constitutionType.value = data.constitutionType || ''

    allergiesSelected.value = parseList(data.allergies)
    allergiesCustom.value = extractCustom(allergiesSelected.value, allergiesOptions)

    contraindicationsSelected.value = parseList(data.contraindications)
    contraindicationsCustom.value = extractCustom(contraindicationsSelected.value, contraindicationsOptions)

    medicalHistorySelected.value = parseList(data.medicalHistory)
    medicalHistoryCustom.value = extractCustom(medicalHistorySelected.value, medicalHistoryOptions)

    medicationSelected.value = parseList(data.medication)
    medicationCustom.value = extractCustom(medicationSelected.value, medicationOptions)

    notes.value = data.notes || ''
  } catch {
    // 首次进入无数据，保持空表单
  }
}

/** 将逗号分隔字符串解析为数组 */
function parseList(str: string): string[] {
  if (!str) return []
  return str.split(',').map(s => s.trim()).filter(Boolean)
}

/** 从已选列表中提取不在预设选项中的项，拼接为自定义文本 */
function extractCustom(selected: string[], options: string[]): string {
  const customItems = selected.filter(s => !options.includes(s))
  return customItems.join(',')
}

/** 处理"无"互斥逻辑：选"无"时清除其他选项，选其他时清除"无" */
function handleNoneToggle(selected: string[], item: string): string[] {
  if (item === '无') {
    // 选中"无"则只保留"无"
    return ['无']
  }
  // 选中其他项时移除"无"
  return selected.filter(s => s !== '无')
}

function onAllergiesClick(item: string) {
  // van-checkbox-group 的 v-model 在 click 时还未更新，使用 setTimeout 延迟处理
  setTimeout(() => {
    allergiesSelected.value = handleNoneToggle(allergiesSelected.value, item)
  }, 0)
}

function onContraindicationsClick(item: string) {
  setTimeout(() => {
    contraindicationsSelected.value = handleNoneToggle(contraindicationsSelected.value, item)
  }, 0)
}

function onMedicalHistoryClick(item: string) {
  setTimeout(() => {
    medicalHistorySelected.value = handleNoneToggle(medicalHistorySelected.value, item)
  }, 0)
}

function onMedicationClick(item: string) {
  setTimeout(() => {
    medicationSelected.value = handleNoneToggle(medicationSelected.value, item)
  }, 0)
}

/** 合并预设选项和自定义输入为逗号分隔字符串 */
function mergeField(selected: string[], custom: string): string {
  const presetItems = selected.filter(s => s !== '无')
  const customItems = custom
    ? custom.split(',').map(s => s.trim()).filter(Boolean)
    : []
  const merged = [...presetItems, ...customItems]
  if (merged.length === 0 && selected.includes('无')) {
    return '无'
  }
  return merged.join(',')
}

async function onSubmit() {
  if (submitting.value) return
  submitting.value = true
  try {
    await createHealthProfile({
      memberId: userStore.userInfo!.id,
      constitutionType: constitutionType.value,
      allergies: mergeField(allergiesSelected.value, allergiesCustom.value),
      contraindications: mergeField(contraindicationsSelected.value, contraindicationsCustom.value),
      medicalHistory: mergeField(medicalHistorySelected.value, medicalHistoryCustom.value),
      medication: mergeField(medicationSelected.value, medicationCustom.value),
      notes: notes.value
    })
    showToast('保存成功')
  } catch {
    // request.ts 已处理错误提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.health-profile-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 30px;
}

.section-card {
  margin: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }

  .section-desc {
    font-size: 12px;
    color: #909399;
    margin-bottom: 12px;
  }
}

.chip-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  :deep(.van-radio),
  :deep(.van-checkbox) {
    margin: 0;
  }
}

.chip-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  color: #606266;
  background: #f5f5f5;
  border: 1px solid #e4e7ed;
  transition: all 0.2s;
  white-space: nowrap;

  &.active {
    color: #07C160;
    background: rgba(7, 193, 96, 0.08);
    border-color: #07C160;
    font-weight: 500;
  }
}

.custom-field {
  margin-top: 12px;

  :deep(.van-field__control) {
    font-size: 14px;
  }
}

.notes-field {
  margin-top: 0;

  :deep(.van-field__control) {
    font-size: 14px;
  }
}

.save-btn-wrap {
  padding: 20px 16px 30px;
}
</style>
