<template>
  <div class="home-page">
    <div class="vehicle-match-section">
      <div class="vehicle-card">
        <div class="vehicle-card-content">
          <div class="vehicle-info">
            <h3>按车型选购</h3>
            <p v-if="selectedVehicle">
              当前车型：{{ selectedVehicle.brand.name }} {{ selectedVehicle.series.name }} {{ selectedVehicle.model.name }}
            </p>
            <p v-else>选择您的车型，精准匹配配件</p>
          </div>
          <el-button type="primary" size="large" @click="showVehicleSelector = true">
            {{ selectedVehicle ? '重新选择' : '选择车型' }}
          </el-button>
        </div>
      </div>
    </div>

    <div class="banner-section">
      <el-carousel height="360px" :interval="4000" v-loading="bannerLoading">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div class="banner-item" @click="handleBannerClick(banner)">
            <el-image :src="banner.image" fit="cover" style="width: 100%; height: 100%" />
            <div class="banner-overlay">
              <h2>{{ banner.title }}</h2>
            </div>
          </div>
        </el-carousel-item>
        <el-carousel-item v-if="banners.length === 0">
          <div class="banner-item banner-placeholder">
            <h2>欢迎来到战途汽配商城</h2>
            <p>品质配件 极速配送</p>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="category-section">
      <h3 class="section-title">商品分类</h3>
      <div class="category-grid">
        <div v-for="cat in categories" :key="cat.id" class="category-card" @click="goToCategory(cat.id)">
          <div class="category-icon">{{ categoryIcons[cat.name] || '🔧' }}</div>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <div class="hot-section">
      <h3 class="section-title">
        <span>热门商品</span>
        <el-button type="primary" link @click="$router.push('/products')">查看更多 ></el-button>
      </h3>
      <div class="product-grid">
        <div v-for="product in hotProducts" :key="product.id" class="product-card" @click="goToDetail(product.id)">
          <div class="product-image">
            <el-image v-if="product.image" :src="product.image" fit="cover" style="width: 100%; height: 100%" />
            <el-icon v-else :size="48" color="#ccc"><Goods /></el-icon>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-sub">{{ product.subName }}</p>
            <div class="product-price">
              <span class="price">¥{{ product.price }}</span>
              <span class="sales">已售 {{ product.sales || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <VehicleSelector v-model="showVehicleSelector" @confirm="handleVehicleConfirm" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryTree, getProductPage } from '@/api/product'
import { getBanners } from '@/api/banner'
import VehicleSelector from '@/components/VehicleSelector.vue'

const router = useRouter()
const categories = ref([])
const hotProducts = ref([])
const banners = ref([])
const bannerLoading = ref(false)
const showVehicleSelector = ref(false)
const selectedVehicle = ref(null)

const categoryIcons = {
  '机油': '🛢️',
  '滤清器': '🔧',
  '刹车片': '🛑',
  '火花塞': '⚡',
  '雨刮器': '🌧️',
  '蓄电池': '🔋',
  '防冻液': '❄️',
  '变速箱油': '⚙️',
  '辅助油液': '💧'
}

const handleVehicleConfirm = (vehicle) => {
  selectedVehicle.value = vehicle
  localStorage.setItem('selectedVehicle', JSON.stringify(vehicle))
  router.push({ path: '/products', query: { vehicleModelId: vehicle.model.id } })
}

const handleBannerClick = (banner) => {
  if (banner.link) {
    router.push(banner.link)
  }
}

const goToCategory = (id) => {
  router.push({ path: '/products', query: { categoryId: id } })
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const loadData = async () => {
  const vehicleStr = localStorage.getItem('selectedVehicle')
  if (vehicleStr) {
    selectedVehicle.value = JSON.parse(vehicleStr)
  }
  
  try {
    bannerLoading.value = true
    const bannerRes = await getBanners()
    banners.value = bannerRes.data || []
  } catch (e) {
    console.error('加载轮播图失败', e)
  } finally {
    bannerLoading.value = false
  }
  
  try {
    const catRes = await getCategoryTree()
    categories.value = catRes.data
  } catch (e) {
    console.error('加载分类失败', e)
  }
  
  try {
    const prodRes = await getProductPage({ pageNum: 1, pageSize: 8, sortBy: 'sales' })
    hotProducts.value = prodRes.data.records
  } catch (e) {
    console.error('加载商品失败', e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

.vehicle-match-section {
  margin-bottom: 24px;
}

.vehicle-card {
  background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
  border-radius: 12px;
  padding: 24px;
  color: #fff;
}

.vehicle-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vehicle-info h3 {
  font-size: 20px;
  margin-bottom: 8px;
}

.vehicle-info p {
  font-size: 14px;
  opacity: 0.9;
}

.banner-section {
  margin-bottom: 30px;
  border-radius: 12px;
  overflow: hidden;
}

.banner-item {
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  position: relative;
}

.banner-item .banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  padding: 40px 20px 20px;
}

.banner-item h2 {
  font-size: 36px;
  margin-bottom: 12px;
}

.banner-item p {
  font-size: 18px;
  opacity: 0.9;
}

.banner-placeholder {
  background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
}

.section-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid #ff6600;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-section {
  margin-bottom: 40px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.12);
}

.category-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.hot-section {
  margin-bottom: 40px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.12);
}

.product-image {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f8f8;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-sub {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #ff6600;
}

.sales {
  font-size: 12px;
  color: #999;
}
</style>
