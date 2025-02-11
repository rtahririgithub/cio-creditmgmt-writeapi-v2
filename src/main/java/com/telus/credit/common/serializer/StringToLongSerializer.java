package com.telus.credit.common.serializer;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Remove double quote for Long values. Ignore if input value is encrypted
 */
public class StringToLongSerializer extends StdSerializer<String> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(StringToLongSerializer.class);

    public StringToLongSerializer() {
        this(null);
    }

    public StringToLongSerializer(Class<String> t) {
        super(t);
    }

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String val = StringUtils.trimToNull(s);
        if (s == null) {
            jsonGenerator.writeNull();
            return;
        }
        try {
            long l = Long.parseLong(val);
            jsonGenerator.writeNumber(l);
        } catch (NumberFormatException e) {
            LOGGER.trace("Invalid Long value {}", s);
            jsonGenerator.writeString(s);
        }
    }
}
