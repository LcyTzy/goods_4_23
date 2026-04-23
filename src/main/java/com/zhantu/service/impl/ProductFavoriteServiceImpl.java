package com.zhantu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.ProductFavorite;
import com.zhantu.mapper.ProductFavoriteMapper;
import com.zhantu.service.ProductFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class ProductFavoriteServiceImpl extends ServiceImpl<ProductFavoriteMapper, ProductFavorite> implements ProductFavoriteService {

    @Override
    public void toggleFavorite(Long userId, Long productId) {
        ProductFavorite existing = this.getOne(
                new LambdaQueryWrapper<ProductFavorite>()
                        .eq(ProductFavorite::getUserId, userId)
                        .eq(ProductFavorite::getProductId, productId));
        if (existing != null) {
            this.removeById(existing.getId());
        } else {
            ProductFavorite favorite = new ProductFavorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            this.save(favorite);
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        return this.count(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId)) > 0;
    }

    @Override
    public IPage<ProductFavorite> getFavoritePage(Long userId, Integer pageNum, Integer pageSize) {
        Page<ProductFavorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductFavorite::getUserId, userId)
                .orderByDesc(ProductFavorite::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public long getFavoriteCount(Long productId) {
        return this.count(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getProductId, productId));
    }
}
