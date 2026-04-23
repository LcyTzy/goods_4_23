package com.zhantu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhantu.entity.Orders;

import java.util.List;

public interface OrdersService extends IService<Orders> {
    Long createOrder(Long userId, List<Long> cartIds, String receiverName, String receiverPhone, String receiverAddress, String remark, Long userCouponId);
    void payOrder(Long userId, Long orderId);
    IPage<Orders> getOrderPage(Long userId, Integer pageNum, Integer pageSize);
    Orders getOrderDetail(Long userId, Long orderId);
}
