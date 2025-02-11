package com.telus.credit.capi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.capi.model.CreditCheckChangeEvent;
import com.telus.credit.model.AccountInfo;

class CreditCheckChangeEventMapperTest {

    private ObjectMapper objectMapper = new ObjectMapper();

//    @Test
//    void testMapCustomerToPatchIndividual() throws IOException {
//        CreditCheckChangeEvent event = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("capi/capievent-individual.json"), CreditCheckChangeEvent.class);
//        CustomerToPatch customerToPatch = CreditCheckChangeEventMapper.fromEvent(event);
//        CustomerToPatch expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("capi/customer-to-patch-individual.json"), CustomerToPatch.class);
//        assertThat(customerToPatch).usingRecursiveComparison().isEqualTo(expected);
//    }
//
//    @Test
//    void testMapCustomerToPatchOrganization() throws IOException {
//        CreditCheckChangeEvent event = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("capi/capievent-org.json"), CreditCheckChangeEvent.class);
//        CustomerToPatch customerToPatch = CreditCheckChangeEventMapper.fromEvent(event);
//        CustomerToPatch expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("capi/customer-to-patch-org.json"), CustomerToPatch.class);
//        assertThat(customerToPatch).usingRecursiveComparison().isEqualTo(expected);
//    }

    @Test
    void testMapAccountInfo() throws IOException {
        CreditCheckChangeEvent event = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("capi/capievent-individual.json"), CreditCheckChangeEvent.class);
        AccountInfo accountInfo = CreditCheckChangeEventMapper.fromEvent(event.getAccount());
        AccountInfo expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("capi/accountinfo.json"), AccountInfo.class);
        assertThat(accountInfo).usingRecursiveComparison().isEqualTo(expected);
    }

//    @ParameterizedTest
//    @ValueSource(strings = {
//            "B/A", "B/F", "B/R" , "B/X"
//    })
//    void testMapOrganization(String accountTypeSubType) {
//        String[] parts = accountTypeSubType.split("/");
//        CreditCheckChangeEvent event = new CreditCheckChangeEvent();
//        event.setAuditInfo(new AuditInfo());
//        Account acc = new Account();
//        event.setAccount(acc);
//
//        acc.setAccountType(parts[0]);
//        acc.setAccountSubType(parts[1]);
//
//        CustomerToPatch customerToPatch = CreditCheckChangeEventMapper.fromEvent(event);
//        assertEquals(PartyType.ORGANIZATION.getType(), customerToPatch.getEngagedParty().getAtReferredType());
//    }

//    @ParameterizedTest
//    @ValueSource(strings = {
//            "I/B", "I/Y"
//    })
//    void testMapIndividual(String accountTypeSubType) {
//        // Individuals are anything not organization, event with these values (exceptions)
//        String[] parts = accountTypeSubType.split("/");
//        CreditCheckChangeEvent event = new CreditCheckChangeEvent();
//        event.setAuditInfo(new AuditInfo());
//        Account acc = new Account();
//        event.setAccount(acc);
//
//        acc.setAccountType(parts[0]);
//        acc.setAccountSubType(parts[1]);
//
//        assertEquals("Individual", CreditCheckChangeEventMapper.fromEvent(event).getEngagedParty().getAtReferredType());
//    }
}