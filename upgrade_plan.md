

```markdown
# 战途汽配商城 SaaS 功能升级实现文档

## 项目概述
- 当前项目：Spring Boot + Kotlin 后端，Vue 3 + Element Plus 管理后台，Vue 3 用户端，Kotlin + Jetpack Compose 安卓端。
- 目标：在现有商城基础上增加 **进销存管理**、**EPC 查询**、**预约修车** 三大模块。
- 现有基础：`product` 表中已有 `oem`, `brand`, `适用车型` 等字段，可作为 EPC 数据种子。

---

## 第一阶段：进销存管理系统

### 1. 数据库表设计（新增表）
```sql
-- 采购单主表
CREATE TABLE purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,          -- 采购单号
    supplier_id BIGINT,                            -- 供应商ID
    total_amount DECIMAL(12,2),                    -- 总金额
    status VARCHAR(20) DEFAULT 'draft',            -- 状态: draft/pending/completed/cancelled
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 采购单明细
CREATE TABLE purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(10,2),
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id)
);

-- 库存流水表
CREATE TABLE inventory_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL,              -- 变动类型: purchase_in/sale_out/adjustment
    change_quantity INT NOT NULL,                  -- 变动数量（正数为入库，负数为出库）
    before_quantity INT,                           -- 变动前库存
    after_quantity INT,                            -- 变动后库存
    related_order_no VARCHAR(64),                  -- 关联单号（采购单号或销售单号）
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2. 后端接口（新增 Controller）
**采购管理接口：**
- `POST /api/admin/purchase-order` - 创建采购单（权限：管理员）
  - 请求体：`{ supplierId, items: [{ productId, quantity, unitPrice }] }`
  - 逻辑：生成单号，保存为 draft，可后续改为 pending。
- `GET /api/admin/purchase-order` - 分页查询采购单列表
  - 参数：`page, size, status`
- `PUT /api/admin/purchase-order/{id}/confirm` - 确认入库
  - 业务逻辑：
    1. 开启事务。
    2. 遍历采购单明细，更新 `product` 表的 `stock` 字段（stock + quantity）。
    3. 为每个商品插入一条 `inventory_log`，change_type = 'purchase_in'，记录前后库存。
    4. 更新采购单状态为 completed。

**库存查询接口：**
- `GET /api/admin/inventory` - 获取所有商品库存（分页）
- `GET /api/admin/inventory/log` - 查询库存流水（可按 productId、changeType、时间范围筛选）

### 3. 管理后台前端（web-admin）
- **采购管理页面**：
  - 表单：选择供应商（新增 `supplier` 表或暂时用文本），动态添加商品行（产品搜索下拉框），填写数量、单价。
  - 列表：展示采购单号、状态、金额、时间，操作栏包含“确认入库”按钮。
  - 路由路径建议：`/purchase/list`，新建页面：`/purchase/create`。
- **库存流水页面**：
  - 路由路径：`/inventory/log`
  - 列表展示：商品名称、变动类型（显示中文）、变动数量、变动前后库存、关联单号、时间。
  - 筛选条件：商品名称搜索、变动类型下拉、日期范围选择器。

---

## 第二阶段：EPC 查询系统

### 1. 数据库表设计（新增）
```sql
-- 汽车品牌表
CREATE TABLE vehicle_brand (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    logo_url VARCHAR(255)
);

-- 车系表
CREATE TABLE vehicle_series (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    FOREIGN KEY (brand_id) REFERENCES vehicle_brand(id)
);

-- 车型表
CREATE TABLE vehicle_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    series_id BIGINT NOT NULL,
    model_name VARCHAR(100),                     -- 如 “速腾 2023款 280TSI”
    production_year VARCHAR(10),                 -- 生产年份或年款
    displacement VARCHAR(20),                    -- 排量
    vin_prefix VARCHAR(17),                      -- VIN码前缀（用于快速解析）
    FOREIGN KEY (series_id) REFERENCES vehicle_series(id)
);

-- 车型与配件关联表
CREATE TABLE vehicle_part_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_model_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    position VARCHAR(50),                        -- 配件在车上的位置标注（可选）
    FOREIGN KEY (vehicle_model_id) REFERENCES vehicle_model(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);
```
**注意：** 优先利用现有 `product` 表的 `brand` 和 `适用车型` 字段生成初始数据。例如将 `适用车型` 字符串解析为品牌-车系-年份，导入到上述新表中。

### 2. 后端接口
- **VIN 解析接口：**
  - `GET /api/epc/query-by-vin?vin=LSV...`
  - 逻辑：
    1. 实现工具类 `VinDecoder`：根据 VIN 的前 3 位识别品牌，前 8 位识别车型特征，与 `vehicle_model` 表中 `vin_prefix` 做模糊匹配。
    2. 返回匹配到的车型列表（`List<VehicleModel>`），如果只有一个则直接展示配件，否则让用户选择车型。
- **根据车型查配件：**
  - `GET /api/epc/parts?modelId=123`
  - 逻辑：联表查询 `vehicle_part_relation` 和 `product`，返回该车型下所有配件基本信息（OE号、名称、价格、库存）。
- **OE号/关键词直搜：**
  - `GET /api/epc/search?keyword=8D0...`
  - 逻辑：在 `product` 表中模糊查询 `oem` 字段或 `name` 字段，返回匹配的配件列表，并标注其适用车型（通过关联表反查）。

### 3. 前端实现
**用户端（Web/Android）：**
- 在搜索栏旁增加 **“VIN查配件”** 入口（图标或文字按钮）。
- 点击后弹出输入框，填入 17 位 VIN 码，提交后：
  - 若后台返回多个车型，展示车型选择列表（品牌、车系、年款、排量）。
  - 用户选择车型后，跳转到配件列表页（或直接展示在当前页面），配件可像普通商品一样加入购物车。
- 商品详情页显示 **“适用车型”** 信息，来源于 `vehicle_part_relation` 关联。

**管理后台（web-admin）：**
- **车型数据管理页面** `/epc/vehicle-manage`：
  - 三级联动维护：品牌 -> 车系 -> 车型。
  - 支持搜索、新增、编辑、删除。
- **配件车型关联页面** `/epc/part-relation`：
  - 左侧为车型树，右侧为配件列表。
  - 可拖拽或勾选配件与车型建立关联，也可批量导入。

---

## 第三阶段：预约修车服务

### 1. 数据库表设计
```sql
-- 服务商品扩展：直接在 product 表增加类型字段（type: 'physical'/'service'），或新增 service_sku 表。
-- 简便做法：在 product 表增加 category 为 “维修服务”，创建服务类商品。

-- 服务预约单表
CREATE TABLE service_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    service_product_id BIGINT NOT NULL,           -- 关联服务商品
    appointment_time DATETIME NOT NULL,           -- 预约时间
    status VARCHAR(20) DEFAULT 'pending',         -- pending/confirmed/completed/cancelled
    remark TEXT,                                  -- 客户备注（如车辆信息）
    related_sale_order_id BIGINT,                 -- 关联的配件销售订单ID（可选）
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2. 后端接口
- **用户端：**
  - `POST /api/service-order` - 创建预约
    - 请求体：`{ serviceProductId, appointmentTime, remark, relatedSaleOrderId }`
    - 逻辑：创建服务单，状态为 pending，并返回预约成功信息。
  - `GET /api/service-order/my` - 用户查看自己的预约列表
- **管理后台：**
  - `GET /api/admin/service-order` - 分页查看所有预约，支持按时间、状态筛选
  - `PUT /api/admin/service-order/{id}/confirm` - 确认预约，状态改为 confirmed
  - `PUT /api/admin/service-order/{id}/complete` - 标记完成，状态改为 completed

### 3. 前端实现
**用户端：**
- **下单流程增强**：当购物车中有“维修服务”类商品时，结算页面要求选择预约时间（日期时间选择器）。
- **独立预约页面**：在用户中心新增“我的预约”，可查看预约状态。
- **智能关联提示**：用户购买了机油+机滤+“换机油服务”后，订单详情页提示“您已购买安装服务，可一键预约”。

**管理后台：**
- **服务预约管理页面** `/service/appointment`
  - 日历视图（可选）展示每日预约情况。
  - 列表视图按时间排序，操作列包含“确认”、“完成”按钮。
  - 支持筛选日期范围和服务状态。

---

## 技术规范与注意事项
1. **复用现有基础**：所有新增接口统一使用 `Result<T>` 返回格式，异常处理沿用 `GlobalExceptionHandler`。
2. **权限控制**：管理后台接口使用 `@PreAuthorize("hasRole('ADMIN')")` 注解。
3. **API 文档**：每个新接口添加 Swagger 注解（`@Operation`），确保 `springdoc-openapi` 自动生成文档。
4. **事务处理**：涉及库存变更的入库、出库操作必须使用 `@Transactional`。
5. **初始EPC数据生成**：编写一个一次性脚本或后台功能，扫描现有 `product` 表，尝试解析 `适用车型` 字段，将 `品牌-车系-年份` 拆分后写入 `vehicle_brand`, `vehicle_series`, `vehicle_model` 表并建立关联。
6. **安卓端同步**：Android 项目（Kotlin/Compose）同步实现 EPC 查询和预约页面，使用 `ViewModel` + `StateFlow` 管理网络请求状态。

---

## 文件结构建议
在现有项目基础上新增以下包/目录：

**后端：**
```
com.example.goods.controller.admin.PurchaseOrderController
com.example.goods.controller.admin.InventoryController
com.example.goods.controller.epc.EpcController
com.example.goods.controller.service.ServiceOrderController
com.example.goods.entity.PurchaseOrder
com.example.goods.entity.PurchaseOrderItem
com.example.goods.entity.InventoryLog
com.example.goods.entity.VehicleBrand
com.example.goods.entity.VehicleSeries
com.example.goods.entity.VehicleModel
com.example.goods.entity.VehiclePartRelation
com.example.goods.entity.ServiceOrder
com.example.goods.repository.*   (JPA/MyBatis 仓库)
com.example.goods.service.PurchaseService
com.example.goods.service.EpcService
com.example.goods.service.ServiceOrderService
com.example.goods.util.VinDecoder
```

**前端 web-admin:**
```
src/views/purchase/List.vue
src/views/purchase/Create.vue
src/views/inventory/Log.vue
src/views/epc/VehicleManage.vue
src/views/epc/PartRelation.vue
src/views/service/Appointment.vue
```

**前端 user-web:**
```
增加 “VIN查询” 组件，预约选择组件，服务订单页面。
```

请根据以上文档依次进行代码实现，优先完成第一阶段，再扩展第二、三阶段。
```
