package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

/**
 * Normalizes blank optional email addresses at the request boundary.
 */
public class BlankEmailToNullDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = super.deserialize(parser, context);
        return value == null || value.isBlank() ? null : value;
    }
}
