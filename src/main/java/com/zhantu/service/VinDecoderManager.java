package com.zhantu.service;

import com.zhantu.model.VehicleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VinDecoderManager {

    @Autowired
    private List<VinDecoder> decoders;

    public VehicleInfo decode(String vin) {
        for (VinDecoder decoder : decoders) {
            VehicleInfo result = decoder.decodeVin(vin);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public String getActiveSource(String vin) {
        VehicleInfo result = decode(vin);
        return result != null ? result.getSource() : "unknown";
    }
}
