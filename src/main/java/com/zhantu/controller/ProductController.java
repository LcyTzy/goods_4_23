package com.zhantu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhantu.common.Result;
import com.zhantu.entity.Category;
import com.zhantu.entity.Product;
import com.zhantu.entity.VehicleModel;
import com.zhantu.entity.VehiclePartRelation;
import com.zhantu.service.CategoryService;
import com.zhantu.service.ProductService;
import com.zhantu.service.VehicleModelService;
import com.zhantu.service.VehiclePartRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "商品模块")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final VehiclePartRelationService vehiclePartRelationService;
    private final VehicleModelService vehicleModelService;

    @GetMapping("/category/tree")
    @Operation(summary = "获取分类树")
    public Result<List<Category>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询商品")
    public Result<IPage<Product>> getProductPage(@RequestParam(required = false) Long categoryId,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String brand,
                                                   @RequestParam(required = false) Long vehicleModelId,
                                                   @RequestParam(required = false) String sortBy,
                                                   @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productService.getProductPage(categoryId, keyword, brand, vehicleModelId, sortBy, sortOrder, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Product product = productService.getById(id);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("product", product);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehiclePartRelation> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehiclePartRelation::getProductId, id);
        List<VehiclePartRelation> productRelations = vehiclePartRelationService.list(wrapper);
        
        List<Map<String, Object>> applicableVehicles = productRelations.stream().map(r -> {
            VehicleModel model = vehicleModelService.getById(r.getVehicleModelId());
            if (model != null) {
                Map<String, Object> vehicleInfo = new java.util.HashMap<>();
                vehicleInfo.put("id", model.getId());
                vehicleInfo.put("name", model.getName());
                vehicleInfo.put("year", model.getYear());
                vehicleInfo.put("displacement", model.getDisplacement());
                vehicleInfo.put("position", r.getPosition());
                return vehicleInfo;
            }
            return null;
        }).filter(v -> v != null).collect(Collectors.toList());
        
        result.put("applicableVehicles", applicableVehicles);
        return Result.success(result);
    }
}
