package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterReponseDto;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

/**
 * Integration tests for LetterController when digital by default is enabled.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "feature-flags.flags.digital-by-default=true")
class LetterControllerDigitalByDefaultITest extends AbstractIntegrationTest {

    private static final URI REISSUE_LETTER_URI = URI.create("/api/v1/moj/letter/reissue-letter");
    private static final URI REISSUE_LETTER_LIST_URI = URI.create("/api/v1/moj/letter/reissue-letter-list");

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtBureau("BUREAU_USER"));
    }

    @Test
    @Sql(
        scripts = {
            "/db/mod/truncate.sql",
            "/db/letter/LetterController_initReissueDbdSummons.sql"
        })
    @Sql(
        statements = "UPDATE juror_mod.court_location SET digital_by_default = false WHERE loc_code IN ('415', '774')",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void reissueDbdSummonsLetterListShowAllQueued() {
        ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
            .letters(List.of(
                reissueLetterRequestData("555555561", FormCode.ENG_DBD_SUMMONS),
                reissueLetterRequestData("555555562", FormCode.BI_DBD_SUMMONS)
            ))
            .build();

        RequestEntity<ReissueLetterRequestDto> reissueRequest = new RequestEntity<>(
            reissueLetterRequestDto, httpHeaders, POST, REISSUE_LETTER_URI);
        ResponseEntity<ReissueLetterReponseDto> reissueResponse =
            template.exchange(reissueRequest, ReissueLetterReponseDto.class);

        assertThat(reissueResponse).isNotNull();
        assertThat(reissueResponse.getStatusCode()).isEqualTo(OK);
        assertThat(reissueResponse.getBody()).isNotNull();
        assertThat(reissueResponse.getBody().getJurors()).isEmpty();

        ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
            .showAllQueued(true)
            .letterType(LetterType.SUMMONS)
            .build();

        RequestEntity<ReissueLetterListRequestDto> listRequest = new RequestEntity<>(
            reissueLetterListRequestDto, httpHeaders, POST, REISSUE_LETTER_LIST_URI);
        ResponseEntity<ReissueLetterListResponseDto> listResponse =
            template.exchange(listRequest, ReissueLetterListResponseDto.class);

        assertThat(listResponse).isNotNull();
        assertThat(listResponse.getStatusCode()).isEqualTo(OK);
        assertThat(listResponse.getBody()).isNotNull();

        ReissueLetterListResponseDto responseBody = listResponse.getBody();
        assertHeadingsAndTypes(responseBody);

        List<List<Object>> data = responseBody.getData();
        assertThat(data).hasSize(2);

        Map<String, List<Object>> rowsByJurorNumber = data.stream()
            .collect(Collectors.toMap(row -> row.get(0).toString(), Function.identity()));

        assertDbdSummonsRow(rowsByJurorNumber.get("555555561"), "415241001", "FNAMEFIVEFOURZERO",
                            "LNAMEFIVEFOURZERO", "CH1 2AN", FormCode.ENG_DBD_SUMMONS);
        assertDbdSummonsRow(rowsByJurorNumber.get("555555562"), "774241001", "FNAMEFIVEFOURONE",
                            "LNAMEFIVEFOURONE", "CF10 1AA", FormCode.BI_DBD_SUMMONS);
    }

    @Test
    @Sql(
        scripts = {
            "/db/mod/truncate.sql",
            "/db/letter/LetterController_initReissueDbdSummons.sql"
        })
    @Sql(
        statements = "UPDATE juror_mod.court_location SET digital_by_default = false WHERE loc_code IN ('415', '774')",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void reissueDbdResponseLetterListShowAllQueued() {
        ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
            .letters(List.of(
                reissueLetterRequestData("555555561", FormCode.ENG_DBD_RESPONSE),
                reissueLetterRequestData("555555562", FormCode.BI_DBD_RESPONSE)
            ))
            .build();

        RequestEntity<ReissueLetterRequestDto> reissueRequest = new RequestEntity<>(
            reissueLetterRequestDto, httpHeaders, POST, REISSUE_LETTER_URI);
        ResponseEntity<ReissueLetterReponseDto> reissueResponse =
            template.exchange(reissueRequest, ReissueLetterReponseDto.class);

        assertThat(reissueResponse).isNotNull();
        assertThat(reissueResponse.getStatusCode()).isEqualTo(OK);
        assertThat(reissueResponse.getBody()).isNotNull();
        assertThat(reissueResponse.getBody().getJurors()).isEmpty();

        ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
            .showAllQueued(true)
            .letterType(LetterType.RESPONSE)
            .build();

        RequestEntity<ReissueLetterListRequestDto> listRequest = new RequestEntity<>(
            reissueLetterListRequestDto, httpHeaders, POST, REISSUE_LETTER_LIST_URI);
        ResponseEntity<ReissueLetterListResponseDto> listResponse =
            template.exchange(listRequest, ReissueLetterListResponseDto.class);

        assertThat(listResponse).isNotNull();
        assertThat(listResponse.getStatusCode()).isEqualTo(OK);
        assertThat(listResponse.getBody()).isNotNull();

        ReissueLetterListResponseDto responseBody = listResponse.getBody();
        assertHeadingsAndTypes(responseBody);

        List<List<Object>> data = responseBody.getData();
        assertThat(data).hasSize(2);

        Map<String, List<Object>> rowsByJurorNumber = data.stream()
            .collect(Collectors.toMap(row -> row.get(0).toString(), Function.identity()));

        assertDbdSummonsRow(rowsByJurorNumber.get("555555561"), "415241001", "FNAMEFIVEFOURZERO",
                            "LNAMEFIVEFOURZERO", "CH1 2AN", FormCode.ENG_DBD_RESPONSE);
        assertDbdSummonsRow(rowsByJurorNumber.get("555555562"), "774241001", "FNAMEFIVEFOURONE",
                            "LNAMEFIVEFOURONE", "CF10 1AA", FormCode.BI_DBD_RESPONSE);
    }

    private ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData(String jurorNumber,
                                                                                      FormCode formCode) {
        return ReissueLetterRequestDto.ReissueLetterRequestData.builder()
            .jurorNumber(jurorNumber)
            .formCode(formCode.getCode())
            .datePrinted(LocalDate.now().minusDays(1))
            .build();
    }

    private void assertDbdSummonsRow(List<Object> row, String poolNumber, String firstName, String lastName,
                                     String postcode, FormCode formCode) {
        assertThat(row).isNotNull();
        assertThat(row).hasSize(9);
        assertThat(row.get(1)).isEqualTo(poolNumber);
        assertThat(row.get(3)).isEqualTo(firstName);
        assertThat(row.get(4)).isEqualTo(lastName);
        assertThat(row.get(5)).isEqualTo(postcode);
        assertThat(row.get(6)).isEqualTo(LocalDate.now().toString());
        assertThat(row.get(7)).isEqualTo(false);
        assertThat(row.get(8)).isEqualTo(formCode.getCode());
    }

    private void assertHeadingsAndTypes(ReissueLetterListResponseDto responseBody) {
        assertThat(responseBody.getHeadings()).containsExactly(
            "Juror number",
            "Pool Number",
            "Summons date",
            "First name",
            "Last name",
            "Postcode",
            "Date printed",
            "hidden_extracted_flag",
            "hidden_form_code"
        );

        assertThat(responseBody.getDataTypes()).containsExactly(
            "string",
            "string",
            "string",
            "string",
            "string",
            "string",
            "date",
            "boolean",
            "string"
        );
    }
}
