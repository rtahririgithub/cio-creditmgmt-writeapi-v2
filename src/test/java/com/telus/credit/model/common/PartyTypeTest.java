package com.telus.credit.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class PartyTypeTest {

    private static final String[] POSSIBLE_TYPES_DESC = new String[] {
            "Individual",
            "Organization"
    };

    @Test
    void testExoticPartyTypes() {
        assertEquals(Arrays.stream(POSSIBLE_TYPES_DESC).map(String::toLowerCase).collect(Collectors.toSet()),
                Arrays.stream(PartyType.values()).map(it -> it.getType().toLowerCase()).collect(Collectors.toSet()));
    }
}
