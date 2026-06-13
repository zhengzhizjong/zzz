<template>
  <div class="login-container">
    <div class="login-brand">
      <div class="brand-content">
        <h1 class="brand-title">忠济堂</h1>
        <h2 class="brand-subtitle">连锁管理系统</h2>
        <p class="brand-desc">管理后台 · 全局管控</p>
        <div class="brand-features">
          <div class="feature-item">多门店管理</div>
          <div class="feature-item">数据洞察</div>
          <div class="feature-item">权限管控</div>
        </div>
      </div>
    </div>
    <div class="login-form-area">
      <div class="login-card">
        <h2 class="login-title">管理后台登录</h2>
        <p class="login-desc">管理员账号登录</p>
        <el-form :model="loginForm" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="loginForm.phone" placeholder="请输入手机号" prefix-icon="Phone" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" style="width: 100%" size="large" @click="handleLogin">登 录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

const loginForm = reactive({
  phone: '',
  password: ''
})

async function handleLogin() {
  if (!loginForm.phone || !loginForm.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(loginForm.phone, loginForm.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    ElMessage.error('登录失败，请检查手机号和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.login-brand {
  flex: 0 0 60%;
  background: linear-gradient(135deg, #1a1a2e 0%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.03);
    top: -100px;
    right: -100px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.02);
    bottom: -50px;
    left: -50px;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: 8px;
}

.brand-subtitle {
  font-size: 24px;
  font-weight: 400;
  margin: 0 0 16px;
  opacity: 0.9;
}

.brand-desc {
  font-size: 16px;
  opacity: 0.7;
  margin: 0 0 40px;
}

.brand-features {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.feature-item {
  padding: 8px 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 14px;
  opacity: 0.8;
}

.login-form-area {
  flex: 0 0 40%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.login-card {
  width: 340px;
}

.login-title {
  font-size: 24px;
  color: #1a1a2e;
  margin: 0 0 8px;
  text-align: center;
}

.login-desc {
  font-size: 14px;
  color: #909399;
  text-align: center;
  margin: 0 0 32px;
}
</style>
