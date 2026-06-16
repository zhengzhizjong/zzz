<template>
  <div class="login-container">
    <div class="login-card">
      <div class="card-accent"></div>
      <div class="login-header">
        <h1>忠济堂</h1>
        <p class="system-name">前台管理系统</p>
        <p class="system-desc">前台收银 · 预约管理 · 会员服务</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await userStore.login(form.phone, form.password)
    ElMessage.success('登录成功')

    // 验证角色是否匹配前台端
    const position = data.position || ''
    if (position && position !== '前台' && position !== 'receptionist') {
      const roleRedirectMap: Record<string, string> = {
        '店长': '/manager/',
        '理疗师': '/therapist/',
        'store_manager': '/manager/',
        'therapist': '/therapist/',
      }
      if (roleRedirectMap[position]) {
        ElMessage.info('该账号将跳转到对应端')
        window.location.href = roleRedirectMap[position]
        return
      }
    }

    router.push('/dashboard')
  } catch (e: any) {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  position: relative;
  width: 420px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}
.card-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background: linear-gradient(180deg, #667eea, #764ba2);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}
.login-header .system-name {
  font-size: 16px;
  color: #667eea;
  font-weight: 600;
  margin: 0 0 8px;
}
.login-header .system-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
  letter-spacing: 2px;
}
</style>
