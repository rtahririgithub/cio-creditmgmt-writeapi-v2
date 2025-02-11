package com.telus.credit.model.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.crypto.service.CryptoService;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.model.TelusCreditProfile;

import io.micrometer.core.instrument.util.IOUtils;

@ExtendWith(MockitoExtension.class)
public class TelusCreditProfileModelMapperTest {
    private static final String POPULATE_METHOD_CD_VALUE = "TestMethodCode";
    private static final String CPROFL_FORMAT_CD_VALUE = "TestFormatCode";
    private static final String COMMENT_TXT_VALUE = "No Comments";
    private static final Boolean BYPASS_MATCH_IND_VALUE = Boolean.FALSE;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @InjectMocks
    private TelusCreditProfileModelMapper telusCreditProfileModelMapper;

    @Mock
    private CryptoService cryptoService;

    @Test
    public void testToCreditProfileEntity_withCharacteristics_atCreditProfileLevel() throws JsonProcessingException {
        TelusCreditProfile creditProfile = getTelusCreditProfile("credit-profile-with-characteristics-at-credit-profile-level.json");
        telusCreditProfileModelMapper.setEncryptionService(cryptoService);
        CreditProfileEntity creditProfileEntity = TelusCreditProfileModelMapper.toCreditProfileEntity(creditProfile);
        assertThat(creditProfileEntity).isNotNull();
        assertThat(creditProfileEntity.getPopulateMethodCd()).isEqualTo(POPULATE_METHOD_CD_VALUE);
        assertThat(creditProfileEntity.getCproflFormatCd()).isEqualTo(CPROFL_FORMAT_CD_VALUE);
        assertThat(creditProfileEntity.getCommentTxt()).isEqualTo(COMMENT_TXT_VALUE);
        assertThat(creditProfileEntity.getBusLastUpdtTs()).isNotNull();
        assertThat(creditProfileEntity.getBypassMatchInd()).isEqualTo(BYPASS_MATCH_IND_VALUE);
    }


    @Test
    public void testToCreditProfileEntity_withCharacteristics_atCreditProfileAndPartyLevel_individual() throws JsonProcessingException {
        TelusCreditProfile creditProfile = getTelusCreditProfile("credit-profile-with-characteristics.json");
        telusCreditProfileModelMapper.setEncryptionService(cryptoService);
        CreditProfileEntity creditProfileEntity = TelusCreditProfileModelMapper.toCreditProfileEntity(creditProfile);
        assertThat(creditProfileEntity).isNotNull();
        assertThat(creditProfileEntity.getPopulateMethodCd()).isEqualTo(POPULATE_METHOD_CD_VALUE);
        assertThat(creditProfileEntity.getCproflFormatCd()).isEqualTo(CPROFL_FORMAT_CD_VALUE);
        assertThat(creditProfileEntity.getCommentTxt()).isEqualTo(COMMENT_TXT_VALUE);
        assertThat(creditProfileEntity.getBusLastUpdtTs()).isNotNull();
        assertThat(creditProfileEntity.getBypassMatchInd()).isEqualTo(BYPASS_MATCH_IND_VALUE);
    }

    @Test
    public void testToCreditProfileEntity_withoutCharacteristics() throws JsonProcessingException {
        TelusCreditProfile creditProfile = getTelusCreditProfile("credit-profile-without-characteristics.json");
        telusCreditProfileModelMapper.setEncryptionService(cryptoService);
        CreditProfileEntity creditProfileEntity = TelusCreditProfileModelMapper.toCreditProfileEntity(creditProfile);
        assertThat(creditProfileEntity).isNotNull();
        assertThat(creditProfileEntity.getPopulateMethodCd()).isNull();
        assertThat(creditProfileEntity.getCproflFormatCd()).isNull();
        assertThat(creditProfileEntity.getCommentTxt()).isNull();
        assertThat(creditProfileEntity.getBusLastUpdtTs()).isNull();
        assertThat(creditProfileEntity.getBypassMatchInd()).isNull();
    }

    private TelusCreditProfile getTelusCreditProfile(String file) throws JsonProcessingException {
        String payload = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8);
        return MAPPER.readValue(payload, TelusCreditProfile.class);
    }

}
