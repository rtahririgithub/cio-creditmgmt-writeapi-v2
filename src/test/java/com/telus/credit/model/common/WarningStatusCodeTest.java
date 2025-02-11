package com.telus.credit.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class WarningStatusCodeTest {

    private static final String[] POSSIBLE_TYPES = new String[] {
            "UNASSIGNED",
            "VERIFIED",
            "UNVERIFIED",
            "INACTIVE",
            "IRRESOLVABLE",
            "REMOVED"
    };

    @Test
    void testExoticWarningCodes() {
        Collection<WarningStatusCode> available = Arrays.stream(WarningStatusCode.values()).collect(Collectors.toSet());

        assertEquals(Arrays.stream(POSSIBLE_TYPES).collect(Collectors.toSet()),
                available.stream().map(it -> it.name()).collect(Collectors.toSet()));
    }
}
