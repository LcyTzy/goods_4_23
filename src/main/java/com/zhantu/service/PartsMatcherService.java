package com.zhantu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhantu.entity.Product;
import com.zhantu.mapper.ProductMapper;
import com.zhantu.model.VehicleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PartsMatcherService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> getMatchingParts(VehicleInfo vehicle) {
        if (vehicle == null || vehicle.getGroupCode() == null || vehicle.getGroupCode().isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getGroupCode, vehicle.getGroupCode());
        wrapper.eq(Product::getStatus, 1);
        wrapper.eq(Product::getDeleted, 0);

        return productMapper.selectList(wrapper);
    }
}
