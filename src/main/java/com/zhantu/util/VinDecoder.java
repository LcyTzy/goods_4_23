package com.zhantu.util;

import java.util.HashMap;
import java.util.Map;

public class VinDecoder {

    private static final Map<String, String> BRAND_MAP = new HashMap<>();
    private static final Map<String, String> ORIGIN_MAP = new HashMap<>();

    static {
        BRAND_MAP.put("L", "China");
        BRAND_MAP.put("J", "Japan");
        BRAND_MAP.put("W", "Germany");
        BRAND_MAP.put("1", "USA");
        BRAND_MAP.put("2", "USA");
        BRAND_MAP.put("5", "USA");
        BRAND_MAP.put("S", "UK");
        BRAND_MAP.put("V", "France");
        BRAND_MAP.put("Z", "Italy");
        BRAND_MAP.put("K", "Korea");

        BRAND_MAP.put("LVG", "Toyota China");
        BRAND_MAP.put("LHG", "Honda China");
        BRAND_MAP.put("LSV", "Volkswagen China");
        BRAND_MAP.put("LBV", "BMW China");
        BRAND_MAP.put("LE4", "Mercedes-Benz China");
        BRAND_MAP.put("LDC", "Dongfeng Peugeot-Citroen");
        BRAND_MAP.put("LFM", "FAW Toyota");
        BRAND_MAP.put("LGB", "Dongfeng Nissan");
        BRAND_MAP.put("LFP", "FAW Car");
        BRAND_MAP.put("LVS", "Changan Ford");
        BRAND_MAP.put("L6T", "SAIC-GM");
        BRAND_MAP.put("LSG", "SAIC-GM");

        ORIGIN_MAP.put("L", "China");
        ORIGIN_MAP.put("J", "Japan");
        ORIGIN_MAP.put("W", "Germany");
        ORIGIN_MAP.put("1", "USA");
        ORIGIN_MAP.put("2", "USA");
        ORIGIN_MAP.put("5", "USA");
        ORIGIN_MAP.put("S", "UK");
        ORIGIN_MAP.put("V", "France");
        ORIGIN_MAP.put("Z", "Italy");
        ORIGIN_MAP.put("K", "Korea");
    }

    public static Map<String, Object> decodeVin(String vin) {
        Map<String, Object> result = new HashMap<>();
        
        if (vin == null || vin.length() != 17) {
            result.put("valid", false);
            result.put("message", "Invalid VIN length");
            return result;
        }

        vin = vin.toUpperCase();
        
        String wmi = vin.substring(0, 3);
        String vds = vin.substring(3, 9);
        String vis = vin.substring(9, 17);
        
        String origin = ORIGIN_MAP.getOrDefault(vin.substring(0, 1), "Unknown");
        String brand = BRAND_MAP.getOrDefault(wmi, BRAND_MAP.getOrDefault(vin.substring(0, 1), "Unknown"));
        
        String yearCode = vis.substring(0, 1);
        String year = decodeYear(yearCode);
        
        result.put("valid", true);
        result.put("wmi", wmi);
        result.put("vds", vds);
        result.put("vis", vis);
        result.put("origin", origin);
        result.put("brand", brand);
        result.put("year", year);
        result.put("vinPrefix", wmi);
        
        return result;
    }

    private static String decodeYear(String yearCode) {
        Map<String, String> yearMap = new HashMap<>();
        yearMap.put("A", "2010");
        yearMap.put("B", "2011");
        yearMap.put("C", "2012");
        yearMap.put("D", "2013");
        yearMap.put("E", "2014");
        yearMap.put("F", "2015");
        yearMap.put("G", "2016");
        yearMap.put("H", "2017");
        yearMap.put("J", "2018");
        yearMap.put("K", "2019");
        yearMap.put("L", "2020");
        yearMap.put("M", "2021");
        yearMap.put("N", "2022");
        yearMap.put("P", "2023");
        yearMap.put("R", "2024");
        yearMap.put("S", "2025");
        
        return yearMap.getOrDefault(yearCode, "Unknown");
    }
}
