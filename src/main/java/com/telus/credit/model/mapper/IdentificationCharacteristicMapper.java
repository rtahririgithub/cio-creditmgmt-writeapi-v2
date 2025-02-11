package com.telus.credit.model.mapper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.crypto.service.HashService;
import com.telus.credit.dao.entity.IdentificationCharEntity;
import com.telus.credit.dao.entity.IdentificationCharHashEntity;
import com.telus.credit.exceptions.CryptoException;

@Component
public class IdentificationCharacteristicMapper {

    private static final String HASH_TYPE_FULL = "FULL";

    private static CryptoService cryptoService;
    private static HashService hashService;

    @Autowired
    public void setCryptoService(CryptoService cryptoService) {
        IdentificationCharacteristicMapper.cryptoService = cryptoService;
    }

    @Autowired
    public void setHashService(HashService hashService) {
        IdentificationCharacteristicMapper.hashService = hashService;
    }

    /**
     * As identification attributes are dynamic, each attribute will be mapped into one entity.
     * The value stored in IdentificationCharEntity will be encrypted if it appears in encryptedAttrs,
     * Then hash value will be stored in IdentificationCharHashEntity for searching function
     *
     * @return DAO entity
     */
    public static void mapAttribute(String attrName, String attrValue, List<String> encryptedAttrs,
                             List<IdentificationCharEntity> characteristicEntities,
                             List<IdentificationCharHashEntity> characteristicHashEntities) {

        if (encryptedAttrs.contains(attrName)) {
            characteristicHashEntities.add(new IdentificationCharHashEntity()
                    .hashType(HASH_TYPE_FULL)
                    .key(attrName).value(StringUtils.isNotBlank(attrValue) ? hashService.sha512CaseInsensitive(attrValue) : StringUtils.EMPTY));
        }
        try {
            characteristicEntities.add(new IdentificationCharEntity()
                    .key(attrName)
                    .value(encryptedAttrs.contains(attrName) && StringUtils.isNotBlank(attrValue) ? cryptoService.encrypt(attrValue) : toEmpty(attrValue)));
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    private static String toEmpty(String value) {
        return value == null ? StringUtils.EMPTY : value;
    }
}
