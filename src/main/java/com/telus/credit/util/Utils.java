package com.telus.credit.util;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.telus.credit.model.TelusCharacteristic;

public interface Utils {
    static String getCharacteristicValue(List<TelusCharacteristic> characteristicList, String name) {
        try {
			if (!CollectionUtils.isEmpty(characteristicList) && !org.springframework.util.StringUtils.isEmpty(name)) {
			    return characteristicList.stream()
			            .filter(characteristic -> name.equals(characteristic.getName()))
			            .map(TelusCharacteristic::getValue)
			            .findFirst().orElse(null);
			}
			//findFirst throws nullpointer exception if there is no value.
		} catch (Exception e) {
		}
        return null;
    }
}
