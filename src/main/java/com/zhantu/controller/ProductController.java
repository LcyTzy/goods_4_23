package com.zhantu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhantu.common.Result;
import com.zhantu.entity.Category;
import com.zhantu.entity.Product;
import com.zhantu.service.CategoryService;
import com.zhantu.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品模块")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final CategoryService categoryService;
    private final ProductService productService;

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
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }
}
