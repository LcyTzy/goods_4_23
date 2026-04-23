package com.zhantu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhantu.common.Result;
import com.zhantu.entity.Category;
import com.zhantu.entity.Orders;
import com.zhantu.entity.Product;
import com.zhantu.entity.User;
import com.zhantu.service.CategoryService;
import com.zhantu.service.OrderItemService;
import com.zhantu.service.OrdersService;
import com.zhantu.service.ProductService;
import com.zhantu.service.UserService;
import com.zhantu.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理后台模块")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrdersService ordersService;
    private final OrderItemService orderItemService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<String> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        User user = userService.lambdaQuery().eq(User::getPhone, username).one();
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("无管理员权限");
        }
        return Result.success(jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole()));
    }

    @GetMapping("/product/page")
    @Operation(summary = "商品分页查询")
    public Result<IPage<Product>> getProductPage(@RequestParam(required = false) Long categoryId,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productService.getAdminProductPage(categoryId, keyword, null, pageNum, pageSize));
    }

    @PostMapping("/product")
    @Operation(summary = "新增商品")
    public Result<Void> addProduct(@RequestBody Product product) {
        productService.save(product);
        return Result.success();
    }

    @PutMapping("/product")
    @Operation(summary = "编辑商品")
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/product/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    @PutMapping("/product/status/{id}")
    @Operation(summary = "商品上下架")
    public Result<Void> updateProductStatus(@PathVariable Long id,
                                             @RequestParam Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productService.updateById(product);
        return Result.success();
    }

    @PutMapping("/product/stock/{id}")
    @Operation(summary = "调整库存")
    public Result<Void> updateStock(@PathVariable Long id,
                                     @RequestParam Integer stock) {
        Product product = new Product();
        product.setId(id);
        product.setStock(stock);
        productService.updateById(product);
        return Result.success();
    }

    @GetMapping("/category/tree")
    @Operation(summary = "分类树")
    public Result<List<Category>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @PostMapping("/category")
    @Operation(summary = "新增分类")
    public Result<Void> addCategory(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping("/category")
    @Operation(summary = "编辑分类")
    public Result<Void> updateCategory(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }

    @GetMapping("/order/page")
    @Operation(summary = "订单分页查询")
    public Result<IPage<Orders>> getOrderPage(@RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Orders> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return Result.success(ordersService.page(page, wrapper));
    }

    @GetMapping("/order/{id}")
    @Operation(summary = "订单详情")
    public Result<Orders> getOrderDetail(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        java.util.List<com.zhantu.entity.OrderItem> items = orderItemService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zhantu.entity.OrderItem>()
                        .eq(com.zhantu.entity.OrderItem::getOrderId, id));
        order.setItems(items);
        return Result.success(order);
    }

    @PutMapping("/order/{id}/status")
    @Operation(summary = "更新订单状态")
    public Result<Void> updateOrderStatus(@PathVariable Long id,
                                           @RequestBody Map<String, Object> params) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        Integer status = (Integer) params.get("status");
        order.setStatus(status);
        ordersService.updateById(order);
        return Result.success();
    }

    @GetMapping("/user/page")
    @Operation(summary = "用户分页查询")
    public Result<IPage<User>> getUserPage(@RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getPhone, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userService.page(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PutMapping("/user/status/{id}")
    @Operation(summary = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Integer> params) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(params.get("status"));
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/user/reset-password/{id}")
    @Operation(summary = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "数据统计")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("productCount", productService.count());
        stats.put("userCount", userService.count());
        stats.put("orderCount", ordersService.count());
        stats.put("categoryCount", categoryService.count());
        return Result.success(stats);
    }
}
