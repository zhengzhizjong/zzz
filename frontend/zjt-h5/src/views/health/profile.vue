<template>
  <div class="health-profile-page">
    <!-- 体质辨识结果卡片 -->
    <div class="constitution-card">
      <div class="constitution-header">
        <span class="constitution-title">体质辨识结果</span>
        <van-tag :color="constitutionColor" size="large" text-color="#fff">{{ constitutionName }}</van-tag>
      </div>
      <div class="constitution-desc" v-if="constitutionType">
        <span>根据AI问诊分析，您的体质类型为{{ constitutionName }}</span>
      </div>
      <div class="constitution-desc" v-else>
        <span>暂未进行体质辨识，点击下方按钮开始AI问诊</span>
      </div>
    </div>

    <!-- 健康信息表单 -->
    <div class="form-card">
      <div class="form-title">健康信息</div>

      <van-cell-group inset>
        <van-field
          v-model="allergies"
          label="过敏史"
          type="textarea"
          placeholder='请输入过敏史，如无请填"无"'
          rows="2"
          maxlength="500"
          show-word-limit
        />
        <van-field
          v-model="contraindications"
          label="禁忌症"
          type="textarea"
          placeholder='请输入禁忌症，如无请填"无"'
          rows="2"
          maxlength="500"
          show-word-limit
        />
        <van-field
          v-model="medicalHistory"
          label="病史"
          type="textarea"
          placeholder="请输入既往病史"
          rows="2"
          maxlength="500"
          show-word-limit
        />
        <van-field
          v-model="medication"
          label="用药情况"
          type="textarea"
          placeholder="请输入当前用药情况"
          rows="2"
          maxlength="500"
          show-word-limit
        />
        <van-field
          v-model="notes"
          label="备注"
          type="textarea"
          placeholder="其他需要备注的健康信息"
          rows="2"
          maxlength="500"
          show-word-limit
        />
      </van-cell-group>
    </div>

    <!-- 保存按钮 -->
    <div class="save-btn-wrap">
      <van-button
        type="primary"
        block
        round
        :loading="submitting"
        loading-text="保存中..."
        @click="onSubmit"
      >
        保存
      </van-button>
    </div>

    <!-- AI体质辨识按钮 -->
    <div class="ai-btn-wrap">
      <van-button block round plain type="success" @click="onGoAiDiagnosis">
        AI体质辨识
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getHealthProfile, updateHealthProfile } from '@/api/user'

const CONSTITUTION_MAP: Record<string, { name: string; color: string }> = {
  pinghe: { name: '平和质', color: '#07C160' },
  qixu: { name: '气虚质', color: '#E6A23C' },
  yangxu: { name: '阳虚质', color: '#F56C6C' },
  yinxu: { name: '阴虚质', color: '#909399' },
  tanshi: { name: '痰湿质', color: '#409EFF' },
  shire: { name: '湿热质', color: '#FA5151' },
  xueyu: { name: '血瘀质', color: '#9B59B6' },
  qiyu: { name: '气郁质', color: '#1ABC9C' },
  tebing: { name: '特禀质', color: '#E67E22' }
}

const constitutionType = ref('')
const constitutionName = ref('未辨识')
const constitutionColor = ref('#909399')

const allergies = ref('')
const contraindications = ref('')
const medicalHistory = ref('')
const medication = ref('')
const notes = ref('')
const submitting = ref(false)

onMounted(() => {
  loadHealthProfile()
})

async function loadHealthProfile() {
  try {
    const res: any = await getHealthProfile()
    const data = res.data || {}
    constitutionType.value = data.constitutionType || ''
    const info = CONSTITUTION_MAP[data.constitutionType] || {}
    constitutionName.value = info.name || data.constitutionName || '未辨识'
    constitutionColor.value = info.color || '#909399'
    allergies.value = data.allergies || ''
    contraindications.value = data.contraindications || ''
    medicalHistory.value = data.medicalHistory || ''
    medication.value = data.medication || ''
    notes.value = data.notes || ''
  } catch {}
}

async function onSubmit() {
  if (submitting.value) return
  submitting.value = true
  try {
    await updateHealthProfile({
      allergies: allergies.value,
      contraindications: contraindications.value,
      medicalHistory: medicalHistory.value,
      medication: medication.value,
      notes: notes.value
    })
    showToast('保存成功')
  } catch {} finally {
    submitting.value = false
  }
}

function onGoAiDiagnosis() {
  // TODO: AI体质辨识页面
  showToast('AI体质辨识功能开发中')
}
</script>

<style scoped lang="scss">
.health-profile-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.constitution-card {
  margin: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;

  .constitution-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .constitution-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .constitution-desc {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
  }
}

.form-card {
  margin: 12px;

  .form-title {
    padding: 14px 16px 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.save-btn-wrap {
  padding: 20px 16px 8px;
}

.ai-btn-wrap {
  padding: 0 16px 30px;
}
</style>
