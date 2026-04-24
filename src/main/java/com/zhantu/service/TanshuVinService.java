package com.zhantu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhantu.model.VehicleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TanshuVinService implements VinDecoder {

    @Value("${vin.tanshu.api-key}")
    private String tanshuApiKey;

    @Autowired
    private VinCacheService cacheService;

    @Autowired
    private VinRuleDecoder ruleDecoder;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public VehicleInfo decodeVin(String vin) {
        VehicleInfo cached = cacheService.get("tanshu:" + vin);
        if (cached != null) {
            return cached;
        }

        VehicleInfo info = queryApi(vin);
        if (info != null) {
            info.setSource("tanshu");
        } else {
            info = ruleDecoder.decodeByRule(vin);
            if (info != null) {
                info.setSource("rule");
            }
        }

        if (info != null) {
            cacheService.put("tanshu:" + vin, info);
        }
        return info;
    }

    @Override
    public String getSourceName() {
        return "tanshu";
    }

    private VehicleInfo queryApi(String vin) {
        if (tanshuApiKey == null || tanshuApiKey.isEmpty()) {
            return null;
        }

        String url = "https://api.tanshuapi.com/api/vin_v4/v1/index"
                + "?key=" + tanshuApiKey
                + "&vin=" + vin;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                if (rootNode.path("code").asInt() == 1) {
                    JsonNode dataNode = rootNode.path("data");
                    return objectMapper.treeToValue(dataNode, VehicleInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
