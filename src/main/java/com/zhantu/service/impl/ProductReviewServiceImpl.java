package com.zhantu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.ProductReview;
import com.zhantu.mapper.ProductReviewMapper;
import com.zhantu.service.ProductReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    @Override
    public void addReview(Long userId, Long productId, Integer rating, String content, String images) {
        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        review.setStatus(1);
        this.save(review);
    }

    @Override
    public IPage<ProductReview> getReviewPage(Long productId, Integer pageNum, Integer pageSize) {
        Page<ProductReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId)
                .eq(ProductReview::getStatus, 1)
                .orderByDesc(ProductReview::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public double getAverageRating(Long productId) {
        List<ProductReview> reviews = this.list(
                new LambdaQueryWrapper<ProductReview>()
                        .eq(ProductReview::getProductId, productId)
                        .eq(ProductReview::getStatus, 1));
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream().mapToInt(ProductReview::getRating).sum();
        return Math.round(sum / reviews.size() * 10.0) / 10.0;
    }
}
