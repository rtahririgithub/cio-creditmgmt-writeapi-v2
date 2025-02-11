package com.telus.credit.dao.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * This extends PartyIdentificationEntity and contains all the Identification characteristic and hashes,
 * however they are not persisted when PartyIdentificationExEntity persisted. Need to persist them via
 * corresponding DAOs
 */
public class PartyIdentificationExEntity extends PartyIdentificationEntity {

    private List<IdentificationCharEntity> characteristic = new ArrayList<>();

    private List<IdentificationCharHashEntity> hashedCharacteristic = new ArrayList<>();

    public List<IdentificationCharEntity> getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(List<IdentificationCharEntity> characteristicList) {
        this.characteristic = characteristicList;
    }

    public List<IdentificationCharHashEntity> getHashedCharacteristics() {
        return hashedCharacteristic;
    }

    public void setHashedCharacteristics(List<IdentificationCharHashEntity> hashedCharacteristics) {
        this.hashedCharacteristic = hashedCharacteristics;
    }
}
