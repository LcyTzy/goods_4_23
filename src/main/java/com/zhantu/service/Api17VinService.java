package com.zhantu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhantu.model.VehicleInfo;
import com.zhantu.model.VehicleInfo17Vin;
import com.zhantu.util.Token17Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Api17VinService implements VinDecoder {

    @Value("${vin.api17.user}")
    private String username;

    @Value("${vin.api17.password}")
    private String password;

    @Autowired
    private VinCacheService cacheService;

    private static final String API_URL_PREFIX = "http://api.17vin.com:8080/";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public VehicleInfo decodeVin(String vin) {
        VehicleInfo cached = cacheService.get("17vin:" + vin);
        if (cached != null) {
            return cached;
        }

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }

        String urlParameters = "/?vin=" + vin;
        String token = Token17Util.generateToken(username, password, urlParameters);

        String url = API_URL_PREFIX + "?vin=" + vin + "&user=" + username + "&token=" + token;

        try {
            String responseJson = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(responseJson);

            if (rootNode.path("code").asInt() == 1) {
                JsonNode dataNode = rootNode.path("data");
                VehicleInfo17Vin info17 = objectMapper.treeToValue(dataNode, VehicleInfo17Vin.class);
                VehicleInfo vehicleInfo = convertToCommon(info17, vin);
                vehicleInfo.setSource("17vin");
                cacheService.put("17vin:" + vin, vehicleInfo);
                return vehicleInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getSourceName() {
        return "17vin";
    }

    private VehicleInfo convertToCommon(VehicleInfo17Vin info17, String vin) {
        VehicleInfo info = new VehicleInfo();
        info.setVin(vin);
        info.setBrandName(info17.getBrand());
        info.setManufacturer(info17.getMadeInCn());
        info.setYear(info17.getModelYearFromVin());
        info.setModel(info17.getGonggaoNo());
        info.setBuildDate(info17.getBuildDate());
        info.setEpc(info17.getEpc());

        if (info17.getModelList() != null && !info17.getModelList().isEmpty()) {
            VehicleInfo17Vin.ModelItem17 model = info17.getModelList().get(0);
            info.setName(model.getModelDetail());
            info.setManufacturer(model.getFactory());
            info.setBrandName(model.getBrand());
            info.setSeriesName(model.getSeries());
            info.setYear(model.getModelYear());
            info.setDisplacement(model.getCc());
            info.setEngineModel(model.getEngineNo());
            info.setPowerType(model.getFuelType());
            info.setGearbox(model.getTransmissionDetail());
            info.setDrivenType(model.getDrivingMode());
            info.setBodyType(model.getBodyType());
            info.setZws(model.getSeatNum());
            info.setPrice(model.getPrice());
            info.setEffluentStandard(model.getEffluentStandard());
            info.setMaxpower(model.getKw());
            info.setImageUrl(model.getImgAddress());
            info.setRemark(model.getSalesVersion());
        }

        return info;
    }
}
