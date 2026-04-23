package com.zhantu.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.*;
import com.zhantu.mapper.OrdersMapper;
import com.zhantu.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final PointsLogService pointsLogService;

    @Override
    @Transactional
    public Long createOrder(Long userId, List<Long> cartIds, String receiverName, String receiverPhone, String receiverAddress, String remark, Long userCouponId) {
        List<Cart> carts = cartService.listByIds(cartIds);
        if (carts.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : carts) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new RuntimeException("商品不存在或已下架: " + cart.getProductId());
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (userCouponId != null) {
            UserCoupon userCoupon = userCouponService.getById(userCouponId);
            if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
                throw new RuntimeException("优惠券不存在");
            }
            if (userCoupon.getStatus() != 0) {
                throw new RuntimeException("优惠券不可用");
            }
            
            Coupon coupon = couponService.getById(userCoupon.getCouponId());
            if (coupon == null) {
                throw new RuntimeException("优惠券信息异常");
            }
            if (totalAmount.compareTo(coupon.getMinAmount()) < 0) {
                throw new RuntimeException("订单金额不满足优惠券使用条件");
            }
            
            if (coupon.getType() == 1) {
                couponDiscount = coupon.getDiscountAmount();
            } else if (coupon.getType() == 2) {
                couponDiscount = totalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate())).setScale(2, RoundingMode.HALF_UP);
            }
            
            if (couponDiscount.compareTo(totalAmount) > 0) {
                couponDiscount = totalAmount;
            }
        }

        BigDecimal payAmount = totalAmount.subtract(couponDiscount);

        Orders order = new Orders();
        order.setOrderNo(IdUtil.fastSimpleUUID());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setCouponDiscount(couponDiscount);
        order.setPayAmount(payAmount);
        order.setUserCouponId(userCouponId);
        order.setStatus(0);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);
        this.save(order);

        if (userCouponId != null) {
            userCouponService.useCoupon(userId, userCouponId, order.getId());
        }

        for (Cart cart : carts) {
            Product product = productService.getById(cart.getProductId());
            
            boolean updated = productService.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, product.getId())
                    .ge(Product::getStock, cart.getQuantity())
                    .setSql("stock = stock - " + cart.getQuantity()));
            
            if (!updated) {
                throw new RuntimeException("库存不足，下单失败: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductCode(product.getCode());
            orderItem.setProductImage(product.getImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            orderItemService.save(orderItem);
        }

        cartService.removeByIds(cartIds);
        return order.getId();
    }

    @Override
    @Transactional
    public void payOrder(Long userId, Long orderId) {
        Orders order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常");
        }
        order.setStatus(1);
        order.setPayTime(java.time.LocalDateTime.now());
        this.updateById(order);

        int points = order.getPayAmount().intValue();
        pointsLogService.addPoints(userId, points, "order", "订单支付获得积分", orderId);
    }

    @Override
    public IPage<Orders> getOrderPage(Long userId, Integer pageNum, Integer pageSize) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId).orderByDesc(Orders::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Orders getOrderDetail(Long userId, Long orderId) {
        Orders order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        order.setItems(items);
        return order;
    }
}
