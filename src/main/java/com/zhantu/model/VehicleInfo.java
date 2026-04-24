package com.zhantu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleInfo {
    private String vin;
    private String brandName;
    private String manufacturer;
    private String seriesName;
    private String name;
    private String zws;
    private String year;
    private String saleState;
    private String price;
    private String marketPrice;
    private String powerType;
    private String oilNum;
    private String fueljetType;
    private String effluentStandard;
    private String model;
    private String marketDate;
    private String stopDate;
    private String engineModel;
    private String color;
    private String displacement;
    private String gearbox;
    private String drivenType;
    private String numberOfCarriages;
    private String maxpower;
    private String wheelbase;
    private String axesNum;
    private String size;
    private String trackFront;
    private String trackRear;
    private String fullWeight;
    private String fullWeightMax;
    private String scale;
    private String groupName;
    private String groupCode;
    private String remark;
    private String source;
    private String epc;
    private String buildDate;
    private String imageUrl;
    private String bodyType;
}
