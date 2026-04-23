package com.zhantu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhantu.entity.ProductReview;

public interface ProductReviewService extends IService<ProductReview> {
    void addReview(Long userId, Long productId, Integer rating, String content, String images);
    IPage<ProductReview> getReviewPage(Long productId, Integer pageNum, Integer pageSize);
    double getAverageRating(Long productId);
}
