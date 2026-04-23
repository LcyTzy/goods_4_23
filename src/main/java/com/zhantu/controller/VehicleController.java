package com.zhantu.controller;

import com.zhantu.common.Result;
import com.zhantu.entity.VehicleBrand;
import com.zhantu.entity.VehicleModel;
import com.zhantu.entity.VehicleSeries;
import com.zhantu.service.VehicleBrandService;
import com.zhantu.service.VehicleModelService;
import com.zhantu.service.VehicleSeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车型匹配模块")
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleBrandService vehicleBrandService;
    private final VehicleSeriesService vehicleSeriesService;
    private final VehicleModelService vehicleModelService;

    @GetMapping("/brands")
    @Operation(summary = "获取所有品牌")
    public Result<List<VehicleBrand>> getBrands() {
        return Result.success(vehicleBrandService.getAllBrands());
    }

    @GetMapping("/series/{brandId}")
    @Operation(summary = "根据品牌获取车系")
    public Result<List<VehicleSeries>> getSeries(@PathVariable Long brandId) {
        return Result.success(vehicleSeriesService.getSeriesByBrandId(brandId));
    }

    @GetMapping("/models/{seriesId}")
    @Operation(summary = "根据车系获取车型")
    public Result<List<VehicleModel>> getModels(@PathVariable Long seriesId) {
        return Result.success(vehicleModelService.getModelsBySeriesId(seriesId));
    }
}
