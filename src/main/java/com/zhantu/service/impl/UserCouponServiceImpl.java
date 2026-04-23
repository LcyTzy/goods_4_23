package com.zhantu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.UserCoupon;
import com.zhantu.mapper.UserCouponMapper;
import com.zhantu.service.UserCouponService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    @Override
    public IPage<UserCoupon> getUserCoupons(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        
        wrapper.orderByDesc(UserCoupon::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void useCoupon(Long userId, Long userCouponId, Long orderId) {
        UserCoupon userCoupon = this.getById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new RuntimeException("优惠券不存在");
        }
        if (userCoupon.getStatus() != 0) {
            throw new RuntimeException("优惠券不可用");
        }
        
        userCoupon.setStatus(1);
        userCoupon.setUseTime(LocalDateTime.now());
        userCoupon.setOrderId(orderId);
        this.updateById(userCoupon);
    }
}
