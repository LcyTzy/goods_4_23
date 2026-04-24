```markdown
# 战途汽配商城 - 集成17VIN API（与探数API并行）

## 📋 背景说明
- 项目已集成**探数API**作为VIN查询数据源
- 现在需要**新增17VIN API**作为第二个数据源（中国市场数据更全、支持配件OE号查询）
- 要求两个数据源并存，互不影响，后续可扩展更多数据源

## 🔑 第一步：17VIN API 认证机制（关键！）

### 1.1 账号信息
```
用户名：13194717525
密码：sd6yh78ju
```

### 1.2 Token生成算法
17VIN不使用传统的用户名密码认证，而是通过**动态Token**机制：

**算法公式**：
```
MD5(MD5(username) + MD5(password) + url_parameters)
```

**示例**：VIN码 `LFMGJE720DS070251`
- `url_parameters` = `/?vin=LFMGJE720DS070251`
- `token` = `MD5(MD5('13194717525') + MD5('sd6yh78ju') + '/?vin=LFMGJE720DS070251')`
- 计算结果：`3ee62eb744b069a5084b40e3dcb9a7a5`

**请求格式**：
```
http://api.17vin.com:8080/?vin=LFMGJE720DS070251&user=13194717525&token=3ee62eb744b069a5084b40e3dcb9a7a5
```

> ⚠️ **重要**：每次请求的 `url_parameters` 不同，token都不同。POST请求也需要将参数拼成GET格式再计算token。

### 1.3 环境变量配置
```bash
export VIN_17_USER=13194717525
export VIN_17_PASSWORD=sd6yh78ju
```

### 1.4 application.yml 补充
```yaml
vin:
  api17:
    user: ${VIN_17_USER:}
    password: ${VIN_17_PASSWORD:}
```

---

## 🧩 第二步：后端实现

### 2.1 策略模式设计（与探数API并行）

新增一个接口 `VinDecoder.java`，探数(`TanshuVinService`)和17VIN(`Api17VinService`)都实现它：

```java
package com.zhantu.service;

import com.zhantu.model.VehicleInfo;

public interface VinDecoder {
    /**
     * 通过VIN码查询车辆信息
     * @return VehicleInfo 或 null
     */
    VehicleInfo decodeVin(String vin);

    /**
     * 数据源名称
     */
    String getSourceName();
}
```

### 2.2 Token生成工具类 `Token17Util.java`

```java
package com.zhantu.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Token17Util {

    /**
     * 生成17VIN API所需的动态token
     * @param username 用户名
     * @param password 密码
     * @param urlParameters url参数字符串，如 "/?vin=LFMGJE720DS070251"
     * @return MD5 token
     */
    public static String generateToken(String username, String password, String urlParameters) {
        return md5(md5(username) + md5(password) + urlParameters);
    }

    /**
     * MD5哈希（小写32位）
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
```

### 2.3 17VIN车辆信息实体类 `VehicleInfo17Vin.java`

> 根据API实际返回JSON结构定义，数据来源：https://api.17vin.com:8443/?vin=LFMGJE720DS070251&user=13194717525

```java
package com.zhantu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleInfo17Vin {

    // ---------- 顶层字段 ----------
    private String fullVin;                 // 完整VIN
    private String modelYearFromVin;        // VIN解析年份
    private String epc;                     // EPC品牌代号（如toyota）
    private String epcCn;                   // EPC品牌中文名
    private String brand;                   // 品牌
    private String gonggaoNo;               // 公告号
    private String matchingMode;            // 匹配模式（exact_match）
    private String madeInCn;                // 产地（中文）
    private String buildDate;               // 制造日期（yyyyMM）

    // ---------- 车型列表（model_list是一个数组，取第一个作为主车型）----------
    private List<ModelItem17> modelList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelItem17 {
        private String modelYear;           // 年款
        private String modelDetail;         // 车型详情（中文全称）
        private String modelDetailEn;       // 车型详情（英文）
        private String factory;             // 厂商
        private String brand;               // 品牌
        private String series;              // 车系
        private String model;               // 车型
        private String salesVersion;        // 销售版本
        private String cc;                  // 排量（如2.0L）
        private String engineNo;            // 发动机型号
        private String airIntake;           // 进气方式
        private String fuelType;            // 燃料类型
        private String transmissionDetail;  // 变速箱详情
        private String gearNum;             // 档位数
        private String drivingMode;         // 驱动方式
        private String doorNum;             // 车门数
        private String seatNum;             // 座位数
        private String bodyType;            // 车身类型
        private String price;               // 指导价
        private String priceUnit;           // 价格单位
        private String effluentStandard;    // 排放标准
        private String kw;                  // 功率（kW）
        private String imgAddress;          // 图片地址（逗号分隔多张）
        private String chassisCode;         // 底盘号
    }
}
```

### 2.4 17VIN服务实现 `Api17VinService.java`

```java
package com.zhantu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhantu.model.VehicleInfo;
import com.zhantu.model.VehicleInfo17Vin;
import com.zhantu.util.Token17Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class Api17VinService implements VinDecoder {

    @Value("${vin.api17.user}")
    private String username;

    @Value("${vin.api17.password}")
    private String password;

    @Autowired
    private VinCacheService cacheService;

    private static final String API_URL_PREFIX = "http://api.17vin.com:8080/";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public VehicleInfo decodeVin(String vin) {
        // 1. 查Redis缓存（key带前缀区分数据源）
        VehicleInfo cached = cacheService.get("17vin:" + vin);
        if (cached != null) {
            return cached;
        }

        // 2. 构建url_parameters并生成token
        String urlParameters = "/?vin=" + vin;
        String token = Token17Util.generateToken(username, password, urlParameters);

        // 3. 请求API
        String url = API_URL_PREFIX + "?vin=" + vin + "&user=" + username + "&token=" + token;

        try {
            String responseJson = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(responseJson);

            if (rootNode.path("code").asInt() == 1) {
                JsonNode dataNode = rootNode.path("data");

                // 4. 解析为17VIN模型
                VehicleInfo17Vin info17 = objectMapper.treeToValue(dataNode, VehicleInfo17Vin.class);

                // 5. 转换为通用VehicleInfo
                VehicleInfo vehicleInfo = convertToCommon(info17, vin);
                vehicleInfo.setSource("17vin");

                // 6. 存入缓存
                cacheService.put("17vin:" + vin, vehicleInfo);
                return vehicleInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getSourceName() {
        return "17vin";
    }

    /**
     * 将17VIN模型转换为通用VehicleInfo模型
     */
    private VehicleInfo convertToCommon(VehicleInfo17Vin info17, String vin) {
        VehicleInfo info = new VehicleInfo();
        info.setVin(vin);
        info.setBrandName(info17.getBrand());
        info.setManufacturer(info17.getMadeInCn());
        info.setYear(info17.getModelYearFromVin());
        info.setModel(info17.getGonggaoNo());
        info.setBuildDate(info17.getBuildDate());

        // 取model_list第一条作为主要车型信息
        if (info17.getModelList() != null && !info17.getModelList().isEmpty()) {
            VehicleInfo17Vin.ModelItem17 model = info17.getModelList().get(0);

            info.setName(model.getModelDetail());
            info.setManufacturer(model.getFactory());
            info.setBrandName(model.getBrand());
            info.setSeriesName(model.getSeries());
            info.setYear(model.getModelYear());
            info.setDisplacement(model.getCc());
            info.setEngineModel(model.getEngineNo());
            info.setPowerType(model.getFuelType());
            info.setGearbox(model.getTransmissionDetail());
            info.setDrivenType(model.getDrivingMode());
            info.setBodyType(model.getBodyType());
            info.setZws(model.getSeatNum());
            info.setPrice(model.getPrice());
            info.setEffluentStandard(model.getEffluentStandard());
            info.setMaxpower(model.getKw());
            info.setImageUrl(model.getImgAddress());
            info.setRemark(model.getSalesVersion());
        }

        return info;
    }
}
```

### 2.5 修改探数服务 `TanshuVinService.java`，实现统一接口

```java
@Service
public class TanshuVinService implements VinDecoder {

    // ... 保留原有代码 ...

    @Override
    public String getSourceName() {
        return "tanshu";
    }
}
```

### 2.6 创建数据源管理器 `VinDecoderManager.java`

```java
package com.zhantu.service;

import com.zhantu.model.VehicleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VinDecoderManager {

    @Autowired
    private List<VinDecoder> decoders;  // Spring会自动注入所有VinDecoder实现

    /**
     * 依次尝试所有数据源，哪个先成功就返回哪个
     */
    public VehicleInfo decode(String vin) {
        for (VinDecoder decoder : decoders) {
            VehicleInfo result = decoder.decodeVin(vin);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * 返回数据源名称
     */
    public String getActiveSource(String vin) {
        VehicleInfo result = decode(vin);
        return result != null ? result.getSource() : "unknown";
    }
}
```

### 2.7 修改控制器 `VehicleController.java`

```java
package com.zhantu.controller;

import com.zhantu.model.VehicleInfo;
import com.zhantu.service.PartsMatcherService;
import com.zhantu.service.VinDecoderManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleController {

    @Autowired
    private VinDecoderManager decoderManager;   // 改为使用管理器

    @Autowired
    private PartsMatcherService partsMatcherService;

    @GetMapping("/decode")
    public ResponseEntity<?> decodeVin(@RequestParam String vin,
                                        @RequestParam(required = false, defaultValue = "auto") String source) {
        if (vin == null || vin.trim().length() != 17) {
            return ResponseEntity.badRequest().body(Map.of("error", "VIN码必须为17位"));
        }

        VehicleInfo vehicleInfo = decoderManager.decode(vin.trim().toUpperCase());
        if (vehicleInfo == null) {
            return ResponseEntity.notFound().build();
        }

        // 匹配配件
        List<?> parts = partsMatcherService.getMatchingParts(vehicleInfo);

        // 组装返回
        Map<String, Object> result = new HashMap<>();
        result.put("vehicle", vehicleInfo);
        result.put("matchedParts", parts);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增：获取全部配件OE号（17VIN独有功能）
     */
    @GetMapping("/oe-numbers")
    public ResponseEntity<?> getOeNumbers(@RequestParam String vin) {
        // 仅17VIN支持此功能，调用17VIN的配件接口
        // 接口文档：https://www.17vin.com/doc/5109.html
        // http://api.17vin.com:8080/{epc}?action=all_part_number&vin={vin}&user={user}&token={token}

        if (vin == null || vin.trim().length() != 17) {
            return ResponseEntity.badRequest().body(Map.of("error", "VIN码必须为17位"));
        }

        // 先查询车辆信息获取epc品牌代号
        VehicleInfo vehicle = decoderManager.decode(vin);
        if (vehicle == null || vehicle.getEpc() == null) {
            return ResponseEntity.notFound().build();
        }

        // 调用5109接口获取所有配件OE号（需要17VIN的token）
        // 此处由Api17VinService直接处理，省略具体实现
        return ResponseEntity.ok(Map.of("message", "请参考5109接口文档实现"));
    }
}
```

### 2.8 扩展 `VehicleInfo.java`，补充17VIN特有字段

在现有 `VehicleInfo.java` 中增加以下字段：
```java
// 17VIN特有字段
private String epc;            // EPC品牌代号（如toyota），用于后续配件查询
private String buildDate;      // 制造日期（yyyyMM）
private String imageUrl;       // 车辆图片地址
private String effluentStandard; // 排放标准
```

---

## 🧪 第三步：测试验证

### 3.1 Token计算验证
使用以下Java代码验证token是否正确：
```java
public static void main(String[] args) {
    String token = Token17Util.generateToken(
        "13194717525",
        "sd6yh78ju",
        "/?vin=LFMGJE720DS070251"
    );
    System.out.println("Token: " + token);
    // 应输出：3ee62eb744b069a5084b40e3dcb9a7a5
}
```

### 3.2 API调用验证
```bash
curl "http://api.17vin.com:8080/?vin=LFMGJE720DS070251&user=13194717525&token=3ee62eb744b069a5084b40e3dcb9a7a5"
```

### 3.3 集成测试
1. 启动Redis
2. 启动后端服务
3. 调用 `/api/vehicle/decode?vin=LFMGJE720DS070251`
4. 检查返回的 `source` 字段是否为 `"17vin"`

---

## ⚠️ 注意事项

### 安全警告
- **绝对不要**将17VIN的用户名密码硬编码在代码中，必须通过环境变量注入
- `application.yml` 中的敏感信息应通过 `${环境变量}` 引用

### Token计算注意
- 每次请求的 `url_parameters` 不同，token都不同
- POST请求也需将参数拼接成GET格式再计算token
- 原始密码不出现在请求URL中，只用于计算token

### API费用
- 17VIN按接口调用次数收费，建议配合Redis缓存（成功结果缓存24小时）
- 接口未查到不扣费

### 数据源优先级
- `VinDecoderManager` 按Spring Bean加载顺序依次尝试
- 可通过 `@Order` 注解调整数据源优先级

---

## 📊 17VIN vs 探数API 对比

| 特性 | 探数API | 17VIN API |
|:---|:---|:---|
| 认证方式 | Key参数 | 动态Token（MD5） |
| 数据覆盖 | 中国市场 | 中国市场（更全） |
| 配件OE号 | 不支持 | 支持（5109接口） |
| 图片地址 | 无 | 有（imgAddress） |
| EPC配件目录 | 无 | 支持多级目录查询 |
| 费用 | 按量计费 | 按量计费 |
| 集成难度 | 简单 | 中等（需计算token） |

---

## 📁 需要新建/修改的文件清单

### 新建文件
- `src/main/java/com/zhantu/util/Token17Util.java` - Token工具类
- `src/main/java/com/zhantu/model/VehicleInfo17Vin.java` - 17VIN数据模型
- `src/main/java/com/zhantu/service/Api17VinService.java` - 17VIN服务
- `src/main/java/com/zhantu/service/VinDecoder.java` - 统一接口
- `src/main/java/com/zhantu/service/VinDecoderManager.java` - 数据源管理器

### 修改文件
- `src/main/java/com/zhantu/model/VehicleInfo.java` - 添加epc/buildDate/imageUrl/effluentStandard字段
- `src/main/java/com/zhantu/service/TanshuVinService.java` - 实现VinDecoder接口
- `src/main/java/com/zhantu/controller/VehicleController.java` - 使用VinDecoderManager
- `src/main/resources/application.yml` - 添加17VIN的user/password配置

---

完成以上步骤后，你的汽配商城将同时拥有**探数API**和**17VIN API**两个数据源，系统会自动选择可用的数据源，并为后续接入更多数据源（如CarsXE、NHTSA）打下基础。
```