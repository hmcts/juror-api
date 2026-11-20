package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class EditJurorRecordRequestDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\r\n"})
    void blankEmailDeserializesToNull(String email) throws Exception {
        String json = objectMapper.createObjectNode().put("email_address", email).toString();
        EditJurorRecordRequestDto request = objectMapper.readValue(json, EditJurorRecordRequestDto.class);

        assertThat(request.getEmailAddress()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"email_address\":null}"})
    void missingOrNullEmailRemainsNull(String json) throws Exception {
        assertThat(objectMapper.readValue(json, EditJurorRecordRequestDto.class).getEmailAddress()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Juror@example.com", "invalid-email", " juror@example.com "})
    void nonBlankEmailRemainsUnchangedForValidation(String email) throws Exception {
        String json = objectMapper.createObjectNode().put("email_address", email).toString();

        assertThat(objectMapper.readValue(json, EditJurorRecordRequestDto.class).getEmailAddress()).isEqualTo(email);
    }

    @Test
    void otherBlankFieldsRemainUnchanged() throws Exception {
        EditJurorRecordRequestDto request = objectMapper.readValue(
            "{\"email_address\":\"\",\"address_line_two\":\"\",\"primary_phone\":\" \"}",
            EditJurorRecordRequestDto.class);

        assertThat(request.getEmailAddress()).isNull();
        assertThat(request.getAddressLineTwo()).isEmpty();
        assertThat(request.getPrimaryPhone()).isEqualTo(" ");
    }
}
