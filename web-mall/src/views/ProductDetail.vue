<template>
  <div class="product-detail" v-loading="loading">
    <div class="detail-card" v-if="product">
      <div class="detail-main">
        <div class="product-gallery">
          <div class="main-image">
            <el-icon :size="80" color="#ccc"><Goods /></el-icon>
          </div>
        </div>
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-sub">{{ product.subName }}</p>
          <div class="price-section">
            <span class="price-label">战途价</span>
            <span class="price">¥{{ product.price }}</span>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">商品编号</span>
              <span>{{ product.code }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">品牌</span>
              <span>{{ product.brand || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">规格</span>
              <span>{{ product.spec || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">单位</span>
              <span>{{ product.unit || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">库存</span>
              <span>{{ product.stock }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">收藏数</span>
              <span>{{ product.favoriteCount || 0 }}</span>
            </div>
          </div>
          <div class="action-section">
            <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
            <el-button type="primary" size="large" @click="handleAddToCart" style="margin-left: 16px">
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button type="danger" size="large" @click="handleBuyNow" style="margin-left: 16px">
              立即购买
            </el-button>
            <el-button :type="isFavorite ? 'warning' : 'default'" size="large" @click="handleToggleFavorite" style="margin-left: 16px">
              <el-icon><Star /></el-icon>
              {{ isFavorite ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="detail-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content">
              <h3>{{ product.name }}</h3>
              <p>副标题: {{ product.subName }}</p>
              <p>适用系列: {{ product.series || '-' }}</p>
              <p>OEM编号: {{ product.oem || '-' }}</p>
              <p>规格: {{ product.spec || '-' }}</p>
              <p>品牌: {{ product.brand || '-' }}</p>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`适用车型 (${applicableVehicles.length})`" name="vehicles" v-if="applicableVehicles.length > 0">
            <div class="vehicle-list">
              <el-table :data="applicableVehicles" stripe>
                <el-table-column prop="name" label="车型名称" />
                <el-table-column prop="year" label="年款" width="100" />
                <el-table-column prop="displacement" label="排量" width="100" />
                <el-table-column prop="position" label="安装位置" width="120" />
              </el-table>
            </div>
          </el-tab-pane>
          <el-tab-pane label="商品评价" name="review">
            <ProductReview :productId="product.id" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { toggleFavorite, checkFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'
import ProductReview from '@/components/ProductReview.vue'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const loading = ref(false)
const quantity = ref(1)
const activeTab = ref('detail')
const isFavorite = ref(false)
const applicableVehicles = ref([])

const handleAddToCart = async () => {
  const token = localStorage.getItem('userToken')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
}

const handleBuyNow = async () => {
  const token = localStorage.getItem('userToken')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    router.push('/checkout')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleToggleFavorite = async () => {
  const token = localStorage.getItem('userToken')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await toggleFavorite(product.value.id)
    isFavorite.value = !isFavorite.value
    ElMessage.success(isFavorite.value ? '已收藏' : '已取消收藏')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const loadProduct = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data.product
    applicableVehicles.value = res.data.applicableVehicles || []
    const token = localStorage.getItem('userToken')
    if (token) {
      try {
        const favRes = await checkFavorite(product.value.id)
        isFavorite.value = favRes.data.favorite
      } catch (e) {
        console.error('检查收藏状态失败', e)
      }
    }
  } catch (e) {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadProduct)
</script>

<style scoped>
.product-detail {
  padding-bottom: 40px;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.detail-main {
  display: flex;
  gap: 40px;
  padding: 30px;
}

.product-gallery {
  flex: 0 0 400px;
}

.main-image {
  width: 400px;
  height: 400px;
  background: #f8f8f8;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.product-sub {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

.price-section {
  background: #fff5f0;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-label {
  font-size: 14px;
  color: #ff6600;
}

.price {
  font-size: 32px;
  font-weight: bold;
  color: #ff6600;
}

.info-list {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  width: 80px;
  color: #999;
  flex-shrink: 0;
}

.action-section {
  display: flex;
  align-items: center;
}

.detail-tabs {
  border-top: 1px solid #eee;
  padding: 20px 30px;
}

.detail-content {
  padding: 20px 0;
}

.detail-content h3 {
  margin-bottom: 12px;
}

.detail-content p {
  margin: 8px 0;
  color: #666;
}

.vehicle-list {
  padding: 10px 0;
}
</style>
