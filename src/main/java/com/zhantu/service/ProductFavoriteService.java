package com.zhantu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhantu.entity.ProductFavorite;

public interface ProductFavoriteService extends IService<ProductFavorite> {
    void toggleFavorite(Long userId, Long productId);
    boolean isFavorite(Long userId, Long productId);
    IPage<ProductFavorite> getFavoritePage(Long userId, Integer pageNum, Integer pageSize);
    long getFavoriteCount(Long productId);
}
