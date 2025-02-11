package com.telus.credit.common.serializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Print BigDecimal number in format like 54234.10 (###.##)
 */
public class CustomDecimalSerializer extends StdSerializer<BigDecimal> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String PATTERN = "%.2f";

    public CustomDecimalSerializer() {
        this(null);
    }

    public CustomDecimalSerializer(Class<BigDecimal> t) {
        super(t);
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            jsonGenerator.writeNull();
            return;
        }

        String stringVal = String.format(Locale.US, PATTERN, value);
        jsonGenerator.writeNumber(stringVal);
    }
}
