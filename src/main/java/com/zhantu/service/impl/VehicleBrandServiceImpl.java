package com.zhantu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.VehicleBrand;
import com.zhantu.mapper.VehicleBrandMapper;
import com.zhantu.service.VehicleBrandService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleBrandServiceImpl extends ServiceImpl<VehicleBrandMapper, VehicleBrand> implements VehicleBrandService {

    @Override
    public List<VehicleBrand> getAllBrands() {
        LambdaQueryWrapper<VehicleBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleBrand::getStatus, 1)
               .orderByAsc(VehicleBrand::getInitial)
               .orderByAsc(VehicleBrand::getSort);
        return this.list(wrapper);
    }
}
