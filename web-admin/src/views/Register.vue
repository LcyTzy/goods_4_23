<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">战途汽配商城 - 用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入姓名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="Phone" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" prefix-icon="Key" size="large" style="flex: 1" @keyup.enter="handleRegister" />
            <img :src="captchaImage" class="captcha-image" @click="refreshCaptcha" alt="验证码" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<el-link type="primary" @click="goToLogin">去登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')

const form = reactive({
  nickname: '',
  phone: '',
  password: '',
  captcha: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.data.image
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    console.error('注册失败', error)
    refreshCaptcha()
    form.captcha = ''
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.register-form {
  margin-top: 20px;
}

.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.register-btn {
  width: 100%;
}

.login-link {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}
</style>
