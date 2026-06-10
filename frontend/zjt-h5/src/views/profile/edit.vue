<template>
  <div class="profile-edit-page">
    <!-- 头像 -->
    <div class="avatar-section" @click="onChooseAvatar">
      <van-image class="avatar" round width="72" height="72" :src="avatarUrl || ''" fit="cover">
        <template #error><div class="avatar-placeholder">👤</div></template>
      </van-image>
      <div class="avatar-edit-hint">
        <span>点击更换头像</span>
      </div>
    </div>

    <!-- 表单 -->
    <div class="form-card">
      <van-cell-group inset>
        <van-field v-model="name" label="姓名" placeholder="请输入姓名" />
        <van-field v-model="phone" label="手机号" readonly />
        <van-field
          v-model="genderText"
          is-link
          readonly
          label="性别"
          placeholder="请选择性别"
          @click="showGenderPicker = true"
        />
        <van-field
          v-model="birthday"
          is-link
          readonly
          label="生日"
          placeholder="请选择生日"
          @click="showBirthdayPicker = true"
        />
        <van-field v-model="memberNo" label="会员编号" readonly />
      </van-cell-group>
    </div>

    <!-- 性别选择 -->
    <van-popup v-model:show="showGenderPicker" round position="bottom">
      <van-picker
        :columns="genderOptions"
        @confirm="onGenderConfirm"
        @cancel="showGenderPicker = false"
      />
    </van-popup>

    <!-- 生日选择 -->
    <van-popup v-model:show="showBirthdayPicker" round position="bottom">
      <van-date-picker
        v-model="birthdayDate"
        :min-date="new Date(1900, 0, 1)"
        :max-date="new Date()"
        @confirm="onBirthdayConfirm"
        @cancel="showBirthdayPicker = false"
      />
    </van-popup>

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getProfile, updateProfile } from '@/api/user'

const router = useRouter()

const name = ref('')
const gender = ref(0)
const genderText = ref('未设置')
const birthday = ref('')
const phone = ref('')
const avatarUrl = ref('')
const memberNo = ref('')
const submitting = ref(false)

const showGenderPicker = ref(false)
const showBirthdayPicker = ref(false)
const genderOptions = [{ text: '未设置' }, { text: '男' }, { text: '女' }]
const birthdayDate = ref(['2020', '01', '01'])

onMounted(() => {
  loadProfile()
})

async function loadProfile() {
  try {
    const res: any = await getProfile()
    // 后端返回 {member: {...}, level: {...}}
    const raw = res.data || {}
    const data = raw.member || raw
    name.value = data.name || data.realName || data.nickname || ''
    gender.value = data.gender ?? 0
    genderText.value = genderOptions[data.gender]?.text || '未设置'
    birthday.value = data.birthday || ''
    phone.value = data.phone || ''
    avatarUrl.value = data.avatarUrl || data.avatar || ''
    memberNo.value = data.memberNo || ''
    if (data.birthday) {
      birthdayDate.value = data.birthday.split('-')
    }
  } catch {}
}

function onChooseAvatar() {
  // H5环境使用文件选择
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (ev) => {
        avatarUrl.value = ev.target?.result as string
      }
      reader.readAsDataURL(file)
    }
  }
  input.click()
}

function onGenderConfirm({ selectedOptions }: any) {
  const val = selectedOptions[0]?.text || '未设置'
  genderText.value = val
  gender.value = genderOptions.findIndex(o => o.text === val)
  showGenderPicker.value = false
}

function onBirthdayConfirm({ selectedValues }: any) {
  birthday.value = selectedValues.join('-')
  showBirthdayPicker.value = false
}

async function onSubmit() {
  if (submitting.value) return
  if (!name.value.trim()) {
    showToast('请输入姓名')
    return
  }

  submitting.value = true
  try {
    await updateProfile({
      name: name.value.trim(),
      gender: gender.value,
      birthday: birthday.value,
      avatar: avatarUrl.value
    })
    showToast('保存成功')
    setTimeout(() => router.back(), 1500)
  } catch {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-edit-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0 20px;
  background: #fff;

  .avatar-placeholder {
    width: 72px;
    height: 72px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0f0f0;
    border-radius: 50%;
    font-size: 32px;
  }

  .avatar-edit-hint {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}

.form-card {
  margin-top: 12px;
}

.save-btn-wrap {
  padding: 30px 16px;
}
</style>
