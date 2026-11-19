package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EmailRequestDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    static Stream<Arguments> emailFields() {
        return Stream.of(
            Arguments.of(JurorCreateRequestDto.class, "email_address"),
            Arguments.of(JurorManualCreationRequestDto.class, "email_address"),
            Arguments.of(JurorPersonalDetailsDto.class, "emailAddress"),
            Arguments.of(JurorPaperResponseDto.class, "emailAddress"),
            Arguments.of(JurorPaperResponseDto.ThirdParty.class, "thirdPartyEmail"),
            Arguments.of(JurorThirdPartyDto.class, "email_address"),
            Arguments.of(JurorResponseDto.class, "email_address"),
            Arguments.of(JurorResponseDto.ThirdParty.class, "emailAddress"),
            Arguments.of(ResponseUpdateController.FirstPersonJurorDetailsDto.class, "email_address"),
            Arguments.of(ResponseUpdateController.ThirdPartyJurorDetailsDto.class, "email_address"),
            Arguments.of(ResponseUpdateController.ThirdPartyJurorDetailsDto.class, "third_party_email")
        );
    }

    @ParameterizedTest
    @MethodSource("emailFields")
    void blankAndNullEmailsBecomeNull(Class<?> requestType, String property) throws Exception {
        for (String email : new String[]{"", " ", "\t\r\n", null}) {
            String json = objectMapper.createObjectNode().put(property, email).toString();
            Object request = objectMapper.readValue(json, requestType);
            JsonNode result = objectMapper.valueToTree(request);

            assertThat(result.path(property).isNull() || result.path(property).isMissingNode())
                .as("%s.%s should normalize blank email", requestType.getSimpleName(), property).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("emailFields")
    void nonBlankEmailsRemainUnchanged(Class<?> requestType, String property) throws Exception {
        for (String email : new String[]{"Juror@example.com", "invalid-email", " juror@example.com "}) {
            String json = objectMapper.createObjectNode().put(property, email).toString();
            Object request = objectMapper.readValue(json, requestType);
            JsonNode result = objectMapper.valueToTree(request);

            assertThat(result.path(property).asText()).isEqualTo(email);
        }
    }
}
