package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.enumeration.CommunicationChannel;
import uk.gov.hmcts.juror.api.moj.enumeration.DigitalByDefaultEmailTemplate;
import uk.gov.hmcts.juror.api.moj.enumeration.EmailStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.juror.api.TestUtils.OBJECT_MAPPER;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "feature-flags.flags.digital-by-default=true")
@DisplayName("Digital by default letter controller")
class LetterDigitalByDefaultControllerITest extends AbstractIntegrationTest {

    private static final URI REISSUE_LETTER_URI = URI.create("/api/v1/moj/letter/reissue-letter");
    private static final URI REISSUE_LETTER_LIST_URI = URI.create("/api/v1/moj/letter/reissue-letter-list");

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql({
        "/db/mod/truncate.sql",
        "/db/letter/LetterController_initPoolReissueDeferralLetter.sql",
        "/db/letter/LetterController_updateReissueDeferralLetterDigitalByDefault.sql"
    })
    void reissueDeferralGrantedLetterListByJurorNumberSentByEmail() throws Exception {
        final String jurorNumber = "555555565";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtBureau("BUREAU_USER"));

        ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
            .jurorNumber(jurorNumber)
            .letterType(LetterType.DEFERRAL_GRANTED)
            .build();

        RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(
            reissueLetterListRequestDto, httpHeaders, POST, REISSUE_LETTER_LIST_URI);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be OK")
            .isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();

        ReissueLetterListResponseDto responseDto = OBJECT_MAPPER.readValue(
            response.getBody(), ReissueLetterListResponseDto.class);

        assertThat(responseDto.getHeadings()).containsExactly(
            "Juror number",
            "First name",
            "Last name",
            "Postcode",
            "Status",
            "Deferred to",
            "Reason",
            "Date printed",
            "hidden_extracted_flag",
            "hidden_form_code",
            "Original sent by",
            "Current preference",
            "hidden_email_status"
        );
        assertThat(responseDto.getDataTypes()).containsExactly(
            "string",
            "string",
            "string",
            "string",
            "string",
            "date",
            "string",
            "date",
            "boolean",
            "string",
            "string",
            "string",
            "string"
        );

        List<List<Object>> data = responseDto.getData();
        assertThat(data).hasSize(1);
        assertThat(data.get(0)).hasSize(13);
        assertThat(data.get(0).get(0)).isEqualTo(jurorNumber);
        assertThat(data.get(0).get(4)).isEqualTo("Deferred");
        assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().toString());
        assertThat(data.get(0).get(8)).isEqualTo(true);
        assertThat(data.get(0).get(9)).isEqualTo(FormCode.ENG_DEFERRAL.getCode());
        assertThat(data.get(0).get(10)).isEqualTo("EMAIL");
        assertThat(data.get(0).get(11)).isEqualTo("EMAIL");
        assertThat(data.get(0).get(12)).isEqualTo("PENDING");
    }

    @Test
    @Sql({
        "/db/mod/truncate.sql",
        "/db/letter/LetterController_initPoolReissueDeferralLetter.sql",
        "/db/letter/LetterController_updateReissueDeferralLetterDigitalByDefault.sql"
    })
    void reissueDeferralGrantedLetterQueuesEmailForDigitalByDefaultJuror() {
        final String jurorNumber = "555555565";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtBureau("BUREAU_USER"));

        ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
            ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                .jurorNumber(jurorNumber)
                .formCode(FormCode.ENG_DEFERRAL.getCode())
                .datePrinted(LocalDate.now())
                .build();
        ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
            .letters(List.of(reissueLetterRequestData))
            .build();

        RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(
            reissueLetterRequestDto, httpHeaders, POST, REISSUE_LETTER_URI);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be OK")
            .isEqualTo(OK);

        executeInTransaction(() -> {
            List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findByJurorNo(jurorNumber);
            assertThat(bulkPrintData).hasSize(2);

            BulkPrintData emailData = bulkPrintData.stream()
                .filter(data -> DigitalByDefaultEmailTemplate.DEFERRAL_GRANTED_ENGLISH.getTemplateName()
                    .equals(data.getNotifyTemplateName()))
                .findFirst()
                .orElseThrow();

            assertThat(emailData.getFormAttribute().getFormType()).isEqualTo(FormCode.ENG_DEFERRAL.getCode());
            assertThat(emailData.isExtractedFlag()).isTrue();
            assertThat(emailData.isDigitalComms()).isTrue();
            assertThat(emailData.getDetailRec()).isEqualTo("N/A");
            assertThat(emailData.getCommunicationChannel()).isEqualTo(CommunicationChannel.EMAIL);
            assertThat(emailData.getEmailStatus()).isEqualTo(EmailStatus.PENDING);
        });
    }
}
