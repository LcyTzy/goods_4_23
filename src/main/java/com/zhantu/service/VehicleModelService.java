package com.zhantu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhantu.entity.VehicleModel;

import java.util.List;

public interface VehicleModelService extends IService<VehicleModel> {
    List<VehicleModel> getModelsBySeriesId(Long seriesId);
}
