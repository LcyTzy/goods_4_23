package com.zhantu.service;

import com.zhantu.model.VehicleInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class VinRuleDecoder {

    private static final Map<Character, String> YEAR_MAP = new HashMap<>();
    private static final Map<String, String> WMI_BRAND = new HashMap<>();

    static {
        YEAR_MAP.put('L', "2020"); YEAR_MAP.put('M', "2021"); YEAR_MAP.put('N', "2022");
        YEAR_MAP.put('P', "2023"); YEAR_MAP.put('R', "2024"); YEAR_MAP.put('S', "2025");
        YEAR_MAP.put('T', "2026"); YEAR_MAP.put('V', "2027"); YEAR_MAP.put('W', "2028");
        YEAR_MAP.put('X', "2029"); YEAR_MAP.put('Y', "2030");

        WMI_BRAND.put("LSV", "上汽大众");
        WMI_BRAND.put("L6T", "吉利汽车");
        WMI_BRAND.put("LBV", "华晨宝马");
        WMI_BRAND.put("LFV", "一汽-大众");
        WMI_BRAND.put("LVG", "广汽丰田");
        WMI_BRAND.put("LHG", "东风本田");
        WMI_BRAND.put("LE4", "北京奔驰");
        WMI_BRAND.put("LDC", "东风标致雪铁龙");
        WMI_BRAND.put("LFM", "一汽丰田");
        WMI_BRAND.put("LGB", "东风日产");
        WMI_BRAND.put("LFP", "一汽轿车");
        WMI_BRAND.put("LVS", "长安福特");
        WMI_BRAND.put("LSG", "上汽通用");
    }

    public VehicleInfo decodeByRule(String vin) {
        if (vin == null || vin.length() != 17) return null;

        VehicleInfo info = new VehicleInfo();
        info.setVin(vin);
        info.setSource("rule");

        String wmi = vin.substring(0, 3);
        info.setBrandName(WMI_BRAND.getOrDefault(wmi, "未知品牌"));

        char yearChar = vin.charAt(9);
        info.setYear(YEAR_MAP.getOrDefault(yearChar, "未知年份"));

        info.setRemark("基于VIN规则解析，非完整数据");
        return info;
    }
}
