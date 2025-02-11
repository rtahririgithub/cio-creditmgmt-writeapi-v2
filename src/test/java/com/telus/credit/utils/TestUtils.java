package com.telus.credit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {
    private TestUtils() {
        //
    }

    public static void compareObject(Object o, String jsonPath, String... ignoredFields) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        objectMapper.setDateFormat(df);
        Object expected = objectMapper.readValue(TestUtils.class.getClassLoader().getResourceAsStream(jsonPath), o.getClass());
        assertThat(expected).usingRecursiveComparison().ignoringFields(ignoredFields).isEqualTo(o);
    }
}
