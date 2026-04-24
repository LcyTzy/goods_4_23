package com.zhantu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleInfo17Vin {

    private String fullVin;
    private String modelYearFromVin;
    private String epc;
    private String epcCn;
    private String brand;
    private String gonggaoNo;
    private String matchingMode;
    private String madeInCn;
    private String buildDate;
    private List<ModelItem17> modelList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelItem17 {
        private String modelYear;
        private String modelDetail;
        private String modelDetailEn;
        private String factory;
        private String brand;
        private String series;
        private String model;
        private String salesVersion;
        private String cc;
        private String engineNo;
        private String airIntake;
        private String fuelType;
        private String transmissionDetail;
        private String gearNum;
        private String drivingMode;
        private String doorNum;
        private String seatNum;
        private String bodyType;
        private String price;
        private String priceUnit;
        private String effluentStandard;
        private String kw;
        private String imgAddress;
        private String chassisCode;
    }
}
