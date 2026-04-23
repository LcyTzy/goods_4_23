package com.zhantu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhantu.common.Result;
import com.zhantu.entity.ProductFavorite;
import com.zhantu.service.ProductFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "商品收藏模块")
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class ProductFavoriteController {

    private final ProductFavoriteService productFavoriteService;

    @PostMapping("/toggle")
    @Operation(summary = "切换收藏状态")
    public Result<Void> toggleFavorite(HttpServletRequest request,
                                        @RequestBody Map<String, Long> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = params.get("productId");
        productFavoriteService.toggleFavorite(userId, productId);
        return Result.success();
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否已收藏")
    public Result<Map<String, Boolean>> checkFavorite(HttpServletRequest request,
                                                        @RequestParam Long productId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean favorite = productFavoriteService.isFavorite(userId, productId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorite", favorite);
        return Result.success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "收藏列表")
    public Result<IPage<ProductFavorite>> getFavoritePage(HttpServletRequest request,
                                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(productFavoriteService.getFavoritePage(userId, pageNum, pageSize));
    }

    @GetMapping("/count/{productId}")
    @Operation(summary = "商品收藏数")
    public Result<Map<String, Long>> getFavoriteCount(@PathVariable Long productId) {
        long count = productFavoriteService.getFavoriteCount(productId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
}
