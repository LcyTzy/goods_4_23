package com.zhantu.controller;

import com.zhantu.model.VehicleInfo;
import com.zhantu.service.PartsMatcherService;
import com.zhantu.service.VinDecoderManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleController {

    @Autowired
    private VinDecoderManager decoderManager;

    @Autowired
    private PartsMatcherService partsMatcherService;

    @GetMapping("/decode")
    public ResponseEntity<?> decodeVin(@RequestParam String vin) {
        if (vin == null || vin.trim().length() != 17) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "VIN码必须为17位"));
        }

        VehicleInfo vehicleInfo = decoderManager.decode(vin.trim().toUpperCase());
        if (vehicleInfo == null) {
            return ResponseEntity.ok(Map.of("code", 404, "message", "未查询到该车辆信息"));
        }

        List<?> parts = partsMatcherService.getMatchingParts(vehicleInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("vehicle", vehicleInfo);
        result.put("matchedParts", parts);
        return ResponseEntity.ok(result);
    }
}
