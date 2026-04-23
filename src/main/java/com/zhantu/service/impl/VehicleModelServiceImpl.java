package com.zhantu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.VehicleModel;
import com.zhantu.mapper.VehicleModelMapper;
import com.zhantu.service.VehicleModelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleModelServiceImpl extends ServiceImpl<VehicleModelMapper, VehicleModel> implements VehicleModelService {

    @Override
    public List<VehicleModel> getModelsBySeriesId(Long seriesId) {
        LambdaQueryWrapper<VehicleModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleModel::getSeriesId, seriesId)
               .eq(VehicleModel::getStatus, 1)
               .orderByAsc(VehicleModel::getSort);
        return this.list(wrapper);
    }
}
