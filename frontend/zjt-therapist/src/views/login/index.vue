<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo">忠济堂</div>
      <div class="subtitle">技师端</div>
      <div class="system-desc">签到签退 · 业绩查看 · 推广管理</div>
    </div>

    <van-form @submit="handleLogin" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          maxlength="11"
          :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1\d{10}$/, message: '手机号格式不正确' }]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          type="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="login-btn-wrapper">
        <van-button type="primary" block native-type="submit" :loading="loading" loading-text="登录中...">
          登录
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({
  phone: '',
  password: ''
})
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await userStore.doLogin(form.value.phone, form.value.password)
    showToast('登录成功')
    const redirect = (route.query.redirect as string) || '/workspace'
    router.push(redirect)
  } catch {
    showToast('登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #07C160 0%, #06ad56 40%, #f5f5f5 70%);
  padding: 0 20px;
  position: relative;
}

.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50vh;
  background: linear-gradient(180deg, #07C160 0%, #06ad56 100%);
  z-index: 0;
}

.login-header {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 80px 0 40px;
  color: #fff;
}

.logo {
  font-size: 36px;
  font-weight: bold;
  letter-spacing: 4px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.subtitle {
  font-size: 18px;
  margin-top: 8px;
  opacity: 0.9;
  font-weight: 500;
}

.system-desc {
  font-size: 13px;
  margin-top: 12px;
  opacity: 0.75;
  letter-spacing: 3px;
}

.login-form {
  position: relative;
  z-index: 1;
  margin-top: 20px;
}

.login-btn-wrapper {
  margin: 24px 16px;
}

.login-btn-wrapper .van-button--primary {
  background: #07C160;
  border-color: #07C160;
}
</style>
