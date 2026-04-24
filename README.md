# 战途汽配商城 (Auto Parts Mall)

## 项目概述

战途汽配商城是一个基于Spring Boot和Vue 3的全栈汽车配件电商平台，旨在为车主提供精准的汽车配件选购、VIN码查询配件、在线购买以及预约维修服务等一站式汽车后市场服务。

### 项目目的

1. **精准配件匹配**：通过车型选择或VIN码查询，实现汽车配件的精准匹配，避免买错配件
2. **电商交易功能**：提供完整的商品浏览、搜索、购物车、下单、支付等电商功能
3. **维修服务预约**：为用户提供在线预约汽车维修服务的功能
4. **进销存管理**：为管理员提供采购订单管理、库存变动追踪等进销存管理功能
5. **EPC电子配件目录**：集成EPC系统，支持VIN码解析和配件查询

---

## 技术栈

### 后端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 编程语言 |
| Spring Boot | 3.2.x | 后端框架 |
| Spring Security | 6.x | 安全认证 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| MySQL | 8.0 | 关系型数据库 |
| JWT | - | Token认证 |
| Lombok | - | 简化代码 |
| Swagger/OpenAPI | 3.x | API文档 |

### 前端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| Element Plus | 2.13.x | UI组件库 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.6.x | HTTP请求 |

### 项目结构

```
goods/
├── src/main/java/com/zhantu/
│   ├── controller/          # 控制器层
│   │   ├── admin/           # 管理员接口
│   │   ├── EpcController    # EPC查询接口
│   │   ├── ProductController # 商品接口
│   │   └── ...
│   ├── service/             # 服务层
│   │   └── impl/            # 服务实现
│   ├── entity/              # 实体类
│   ├── mapper/              # MyBatis Mapper
│   ├── config/              # 配置类
│   └── util/                # 工具类
│       └── VinDecoder.java  # VIN码解析工具
├── src/main/resources/
│   ├── sql/                 # 数据库脚本
│   └── application.yml      # 配置文件
├── web-admin/               # 管理后台前端
│   └── src/
│       ├── views/           # 页面组件
│       ├── api/             # API请求
│       └── router/          # 路由配置
└── web-mall/                # 用户商城前端
    └── src/
        ├── views/           # 页面组件
        ├── components/      # 公共组件
        ├── api/             # API请求
        └── router/          # 路由配置
```

---

## 功能模块

### 1. 用户端功能 (web-mall)

#### 1.1 商品浏览与搜索
- **首页展示**：轮播图、热门分类、推荐商品
- **车型选择**：品牌-车系-车型三级联动选择
- **VIN查配件**：输入17位VIN码，自动解析车型并匹配配件
- **商品列表**：支持分类筛选、价格排序、关键词搜索
- **商品详情**：商品图片、价格、规格、适用车型、用户评价

#### 1.2 购物车与订单
- **购物车管理**：添加商品、修改数量、删除商品
- **订单结算**：收货地址管理、订单确认
- **订单管理**：查看订单状态、订单详情

#### 1.3 用户中心
- **用户注册/登录**：JWT Token认证
- **个人信息**：修改密码、收货地址管理
- **收藏夹**：收藏商品管理
- **服务预约**：预约维修服务、查看预约记录

#### 1.4 预约维修服务
- **服务类型选择**：保养、维修、检测等服务类型
- **预约时间选择**：选择服务门店和预约时间
- **预约记录**：查看历史预约记录

### 2. 管理端功能 (web-admin)

#### 2.1 商品管理
- **商品列表**：分页展示、搜索筛选
- **商品编辑**：添加/编辑商品信息、上传图片
- **分类管理**：商品分类树形管理
- **上下架管理**：控制商品显示状态

#### 2.2 车型管理
- **品牌管理**：汽车品牌CRUD
- **车系管理**：按品牌管理车系
- **车型管理**：车型详细信息管理，包括VIN前缀
- **配件关联**：车型与配件的关联关系管理

#### 2.3 进销存管理
- **供应商管理**：供应商信息维护
- **采购订单**：创建采购订单、审核、入库
- **库存日志**：库存变动记录查询
- **库存预警**：低库存商品提醒

#### 2.4 订单管理
- **用户订单**：查看用户订单、发货管理
- **服务订单**：维修服务预约管理

#### 2.5 系统管理
- **用户管理**：管理员账号管理
- **角色权限**：角色和权限配置
- **Banner管理**：首页轮播图管理

### 3. EPC电子配件目录

#### 3.1 VIN码解析
- **VIN解码**：根据VIN码前3位识别品牌和产地
- **年款识别**：根据VIN码第10位识别生产年款
- **车型匹配**：通过VIN前缀模糊匹配数据库中的车型

#### 3.2 配件查询
- **按车型查询**：根据车型ID查询适用配件
- **OE号搜索**：通过OE号或关键词搜索配件
- **适用车型展示**：商品详情页展示适用车型列表

---

## 核心功能实现

### 1. VIN码解析实现

```java
// VinDecoder.java
public static Map<String, Object> decodeVin(String vin) {
    // 1. 验证VIN码长度（必须17位）
    // 2. 提取WMI（前3位）识别品牌和产地
    // 3. 提取VDS（4-9位）识别车型特征
    // 4. 提取VIS（10-17位）识别年款
    // 5. 根据VIN前缀匹配数据库中的车型
}
```

**VIN码结构**：
- 第1位：生产国家（L=中国，J=日本，W=德国等）
- 第2-3位：制造商代码（LVG=丰田中国，LHG=本田中国，L6T=上汽通用等）
- 第4-8位：车辆特征代码
- 第9位：校验位
- 第10位：年款代码（N=2022，P=2023，R=2024等）
- 第11位：装配厂代码
- 第12-17位：生产序列号

### 2. 车型-配件关联实现

```sql
-- 车型与配件关联表
CREATE TABLE vehicle_part_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_model_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    position VARCHAR(50),  -- 配件安装位置
    INDEX idx_vehicle_model_id (vehicle_model_id),
    INDEX idx_product_id (product_id)
);
```

### 3. 库存管理实现

```sql
-- 库存变动日志表
CREATE TABLE inventory_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    change_type VARCHAR(20),  -- IN/OUT/ADJUST
    change_quantity INT,
    before_quantity INT,
    after_quantity INT,
    reference_no VARCHAR(50),
    remark VARCHAR(255),
    create_time DATETIME
);
```

### 4. 安全认证实现

- **JWT Token**：用户登录后生成JWT Token，存储在localStorage
- **Spring Security**：配置API访问权限，公开接口和受保护接口分离
- **密码加密**：使用BCrypt加密存储用户密码
- **CORS配置**：允许前后端跨域通信

---

## 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| product | 商品表 | id, name, code, oem, price, stock, brand, spec |
| product_category | 商品分类表 | id, name, parent_id, sort |
| vehicle_brand | 汽车品牌表 | id, name, logo, initial |
| vehicle_series | 汽车车系表 | id, brand_id, name |
| vehicle_model | 汽车车型表 | id, series_id, name, year, vin_prefix |
| vehicle_part_relation | 车型配件关联表 | id, vehicle_model_id, product_id, position |
| product_vehicle | 商品车型关联表 | id, product_id, vehicle_model_id |
| cart | 购物车表 | id, user_id, product_id, quantity |
| orders | 订单表 | id, user_id, total_amount, status |
| order_item | 订单明细表 | id, order_id, product_id, quantity, price |
| service_order | 服务订单表 | id, user_id, service_type_id, appointment_time |
| service_type | 服务类型表 | id, name, description, price |
| supplier | 供应商表 | id, name, contact, phone |
| purchase_order | 采购订单表 | id, supplier_id, total_amount, status |
| inventory_log | 库存日志表 | id, product_id, change_type, change_quantity |
| banner | 轮播图表 | id, title, image, url, sort |
| user | 用户表 | id, username, password, role |
| favorite | 收藏表 | id, user_id, product_id |

---

## 部署说明

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 后端启动

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE autoparts CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 执行数据库脚本
mysql -u root -p autoparts < src/main/resources/sql/schema.sql

# 3. 修改配置文件
# 编辑 src/main/resources/application.yml，配置数据库连接

# 4. 启动后端
mvn spring-boot:run
# 或
java -jar target/goods-0.0.1-SNAPSHOT.jar
```

### 前端启动

```bash
# 管理后台
cd web-admin
npm install
npm run dev

# 用户商城
cd web-mall
npm install
npm run dev
```

### 访问地址

- 用户商城：http://localhost:5173
- 管理后台：http://localhost:5174
- 后端API：http://localhost:8081
- API文档：http://localhost:8081/swagger-ui.html

---

## 项目特色

1. **精准配件匹配**：通过车型选择或VIN码查询，实现配件精准匹配
2. **完整的电商功能**：商品浏览、购物车、订单、支付等完整电商流程
3. **EPC系统集成**：支持VIN码解析和电子配件目录查询
4. **进销存管理**：采购订单、库存变动追踪、库存预警
5. **维修服务预约**：在线预约汽车维修服务
6. **前后端分离**：Vue 3 + Spring Boot现代化架构
7. **响应式设计**：适配PC和移动端浏览

---

## 开发团队

- 开发者：LcyTzy
- 项目地址：https://github.com/LcyTzy/goods_4_23

---

## 许可证

本项目仅供学习和研究使用。
