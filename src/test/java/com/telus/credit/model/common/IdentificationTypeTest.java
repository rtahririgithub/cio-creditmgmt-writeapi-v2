package com.telus.credit.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class IdentificationTypeTest {

	/*
	 * private static final String[] POSSIBLE_TYPES_DESC = new String[] {
	 * "qst registration number for Quebec", "nova scotia joint stocks registry",
	 * "cra business number", "business registration number", "health card",
	 * "Drivers License", "credit card", "social insurance number",
	 * "provincial card", "passport" };
	 */

    private static final String[] POSSIBLE_TYPES = new String[] {
            "PSP",
            "PRV",
            "SIN",
            "CC",
            "DL",
            "HC",
            "BIC",
            "CRA",
            "NSJ",
            "QST"
    };

    @Test
    void testExoticIdentifications() {
        Collection<IdentificationType> expected = Arrays.stream(POSSIBLE_TYPES).map(IdentificationType::getIdentificationType).collect(Collectors.toSet());
        Collection<IdentificationType> available = Arrays.stream(IdentificationType.values())
                // try to get identification Type from name value
                .map(t->t.name().toLowerCase())
                .map(IdentificationType::getIdentificationType)
                .collect(Collectors.toSet());

        assertEquals(expected, available);

        assertEquals(Arrays.stream(POSSIBLE_TYPES).collect(Collectors.toSet()),
                available.stream().map(it -> it.name()).collect(Collectors.toSet()));
    }
}
