<template>
  <div class="product-list-page">
    <div class="filter-bar">
      <div class="filter-row" v-if="selectedVehicle">
        <span class="filter-label">当前车型:</span>
        <span class="vehicle-tag">
          {{ selectedVehicle.brand.name }} {{ selectedVehicle.series.name }} {{ selectedVehicle.model.name }}
          <el-icon @click="clearVehicle" style="cursor: pointer; margin-left: 8px;"><Close /></el-icon>
        </span>
      </div>
      <div class="filter-row">
        <span class="filter-label">分类:</span>
        <el-radio-group v-model="filters.categoryId" @change="handleFilterChange">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-row">
        <span class="filter-label">排序:</span>
        <el-radio-group v-model="filters.sortBy" @change="handleFilterChange">
          <el-radio-button value="default">默认排序</el-radio-button>
          <el-radio-button value="price-asc">价格↑</el-radio-button>
          <el-radio-button value="price-desc">价格↓</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索商品/OEM号/品牌" style="width: 300px" clearable @keyup.enter="handleFilterChange">
          <template #append>
            <el-button @click="handleFilterChange">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="product-grid" v-loading="loading">
      <div v-for="product in products" :key="product.id" class="product-card" @click="goToDetail(product.id)">
        <div class="product-image">
          <el-icon :size="48" color="#ccc"><Goods /></el-icon>
        </div>
        <div class="product-info">
          <h4 class="product-name">{{ product.name }}</h4>
          <p class="product-sub">{{ product.subName }}</p>
          <div class="product-meta">
            <span class="price">¥{{ product.price }}</span>
            <span class="brand">{{ product.brand }}</span>
          </div>
          <div class="product-bottom">
            <span class="stock">库存: {{ product.stock }}</span>
            <span class="favorite">收藏: {{ product.favoriteCount || 0 }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="total"
      :page-sizes="[12, 24, 48]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 20px; justify-content: center"
      @current-change="loadProducts"
      @size-change="loadProducts"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCategoryTree, getProductPage } from '@/api/product'

const router = useRouter()
const route = useRoute()
const categories = ref([])
const products = ref([])
const loading = ref(false)
const total = ref(0)
const selectedVehicle = ref(null)

const filters = reactive({
  categoryId: null,
  keyword: '',
  sortBy: 'default',
  vehicleModelId: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 12
})

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const clearVehicle = () => {
  selectedVehicle.value = null
  filters.vehicleModelId = null
  localStorage.removeItem('selectedVehicle')
  handleFilterChange()
}

const handleFilterChange = () => {
  pagination.pageNum = 1
  loadProducts()
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: filters.keyword || undefined,
      categoryId: filters.categoryId || undefined,
      vehicleModelId: filters.vehicleModelId || undefined
    }
    if (filters.sortBy === 'price-asc') {
      params.sortBy = 'price'
      params.sortOrder = 'asc'
    } else if (filters.sortBy === 'price-desc') {
      params.sortBy = 'price'
      params.sortOrder = 'desc'
    }
    const res = await getProductPage(params)
    products.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error('加载商品失败', e)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

onMounted(() => {
  const vehicleStr = localStorage.getItem('selectedVehicle')
  if (vehicleStr) {
    selectedVehicle.value = JSON.parse(vehicleStr)
    filters.vehicleModelId = selectedVehicle.value.model.id
  }
  if (route.query.categoryId) {
    filters.categoryId = parseInt(route.query.categoryId)
  }
  if (route.query.keyword) {
    filters.keyword = route.query.keyword
  }
  if (route.query.vehicleModelId) {
    filters.vehicleModelId = parseInt(route.query.vehicleModelId)
  }
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.product-list-page {
  padding-bottom: 40px;
}

.filter-bar {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-weight: 600;
  color: #666;
  white-space: nowrap;
}

.vehicle-tag {
  background: #fff5f0;
  color: #ff6600;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  min-height: 200px;
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

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #ff6600;
}

.brand {
  font-size: 12px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stock {
  font-size: 12px;
  color: #999;
}

.favorite {
  font-size: 12px;
  color: #999;
}
</style>
