package uk.gov.hmcts.juror.api.moj.controller;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.letter.LetterId;
import uk.gov.hmcts.juror.api.moj.domain.letter.RequestLetter;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.RequestLetterRepository;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.TestUtils.objectMapper;

/**
 * Integration tests for the API endpoints defined in LetterController.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.LawOfDemeter"})
class LetterControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Autowired
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Autowired
    private RequestLetterRepository requestLetterRepository;
    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauPaperUserHappyPath() throws Exception {
        final String jurorNumber = "222222222";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 2 Section D, Part 2 Section A";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        List<BulkPrintData> bulkPrintDataList = bulkPrintDataRepository.findAll();
        List<BulkPrintData> bulkPrintDataByJuror = bulkPrintDataList.stream()
            .filter(item -> jurorNumber.equals(item.getJurorNo())).toList();
        assertThat(bulkPrintDataByJuror.toArray().length).isEqualTo(1);
        assertThat(bulkPrintDataByJuror.stream().findFirst().get().getDetailRec().substring(361, 571))
            .isEqualTo(StringUtils.rightPad(expectedRequestString, 210, " ").toUpperCase())
            .as("Expect the request letter string to be queued for all info");

        verifyPaperResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationCourtUserForbidden() throws Exception {
        final String jurorNumber = "222222222";
        final String courtOwner = "415";
        final String courtJwt = createBureauJwt("COURT_USER", courtOwner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verifyPaperResponse(jurorNumber, ProcessingStatus.TODO);
    }

    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauUserPaperAllCategoriesMissing() throws Exception {
        final String jurorNumber = "222222222";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 1 Date of Birth, Part 1 Telephone No., Part 2 Section B, "
            + "Part 2 Section C, Part 2 Section C, Part 2 Section E, Part 2 Section A, Part 3 Section A/B/C, Part 2 "
            + "Section D, Part 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyPaperResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauUserPaperAllCategoriesMissingWelsh() throws Exception {
        final String jurorNumber = "222222223";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Rhan 1 Dyddiad Geni, Rhan 1 Rhif FFon., Rhan 2 Adran B, "
            + "Rhan 2 Adran C, Rhan 2 Adran C, Rhan 2 Adran E, Rhan 2 Adran A, Rhan 3 Adran A/B/C, Rhan 2 Adran D, "
            + "Rhan"
            + " 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyPaperResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauUserPaperEmptyMissingInformation() throws Exception {
        final String jurorNumber = "222222222";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(new ArrayList<>())
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be BAD_REQUEST, cannot process when no missing info")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyPaperResponse(jurorNumber, ProcessingStatus.TODO);
    }

    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauUserPaperMissingSignature() throws Exception {
        final String jurorNumber = "222222222";
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber("222222222")
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(List.of(
                MissingInformation.SIGNATURE))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be BAD_REQUEST, cannot process when Signature is missing")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyPaperResponse(jurorNumber, ProcessingStatus.TODO);
    }


    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauUserDigitalHappyPath() throws Exception {
        final String jurorNumber = "111111000";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 2 Section D, Part 2 Section A";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.DIGITAL)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        requestLetterOpt.ifPresent(requestLetter -> assertThat(requestLetter.getRequiredInformation())
            .as("Expect the request letter string to be queued for all info")
            .isEqualTo(expectedRequestString));

        verifyDigitalResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }


    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauUserDigitalAllCategoriesMissing() throws Exception {
        final String jurorNumber = "111111000";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 1 Date of Birth, Part 1 Telephone No., Part 2 Section B, "
            + "Part 2 Section C, Part 2 Section C, Part 2 Section E, Part 2 Section A, Part 3 Section A/B/C, Part 2 "
            + "Section D, Part 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.DIGITAL)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyDigitalResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    void requestInformationBureauUserDigitalAllCategoriesMissingWelsh() throws Exception {
        final String jurorNumber = "111111001";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Rhan 1 Dyddiad Geni, Rhan 1 Rhif FFon., Rhan 2 Adran B, "
            + "Rhan 2 Adran C, Rhan 2 Adran C, Rhan 2 Adran E, Rhan 2 Adran A, Rhan 3 Adran A/B/C, Rhan 2 Adran D, "
            + "Rhan" + " 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.DIGITAL)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyDigitalResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Test
    void requestInformationBureauUserPaperMissingReplyMethod() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber("222222222")
            .missingInformation(List.of(
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be BAD_REQUEST, cannot process when Reply Method is unspecified")
            .isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/reissue-letter-list")
    class ReissueLetterListTests {

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void reissueDeferralGrantedLetterListByJurorNumber() throws Exception {
            final String jurorNumber = "555555561";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferrals(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(10);
            assertThat(data.get(0).get(0)).isEqualTo("555555561");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo("Deferred");
            assertThat(data.get(0).get(5)).isEqualTo(LocalDate.now().plusDays(10).toString());
            assertThat(data.get(0).get(6)).isEqualTo("Moved from area");
            assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(8)).isEqualTo(true);
            assertThat(data.get(0).get(9)).isEqualTo(FormCode.ENG_DEFERRAL.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void reissueDeferralGrantedLetterListByJurorNumberBiLingual() throws Exception {
            final String jurorNumber = "555555567";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferrals(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(2);  // deferred twice
            assertThat(data.get(0).size()).isEqualTo(10);
            assertThat(data.get(0).get(0)).isEqualTo("555555567");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo("Deferred");
            assertThat(data.get(0).get(5)).isEqualTo(LocalDate.now().plusDays(20).toString());
            assertThat(data.get(0).get(6)).isEqualTo("Childcare");
            assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(8)).isEqualTo(true);
            assertThat(data.get(0).get(9)).isEqualTo(FormCode.BI_DEFERRAL.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void reissueDeferralGrantedLetterListByPoolNumber() throws Exception {
            final String poolNumber = "415220401";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .poolNumber(poolNumber)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferrals(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(5);

            // verify the order of the rows - pending should be first, then sorted by date printed descending
            assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().toString());
            assertThat(data.get(0).get(8)).as("Expect extracted flag to be false").isEqualTo(false);
            assertThat(data.get(1).get(7)).isEqualTo(LocalDate.now().toString());
            assertThat(data.get(1).get(8)).as("Expect extracted flag to be false").isEqualTo(false);
            assertThat(data.get(2).get(7)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(2).get(8)).as("Expect extracted flag to be true").isEqualTo(true);
            assertThat(data.get(3).get(7)).isEqualTo(LocalDate.now().minusDays(4).toString());
            assertThat(data.get(4).get(7)).isEqualTo(LocalDate.now().minusDays(8).toString());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void reissueDeferralGrantedLetterListShowAllPending() throws Exception {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .showAllQueued(true)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferrals(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(3);

            int pendingCount = data.stream().map(row -> row.get(8)).filter(flag -> flag.equals(false))
                .toArray().length;
            assertThat(pendingCount).as("Expect there to be 3 pending rows").isEqualTo(3);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void reissueDeferralGrantedLetterListUnhappyNotFound() throws Exception {
            final String jurorNumber = "995555561";
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Not Found")
                .isEqualTo(HttpStatus.NOT_FOUND);

        }

        @Test
        void reissueListCourtUnhappyNoAccess() throws Exception {
            final String jurorNumber = "555555561";
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");
            final String bureauJwt = createBureauJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Forbidden")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueConfirmationLetter.sql"})
        void reissueConfirmationLetterListByJurorNumber() throws Exception {
            final String jurorNumber = "555555561";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.CONFIRMATION)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesConfirmation(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(7);
            assertThat(data.get(0).get(0)).isEqualTo("555555561");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(5)).isEqualTo(true);
            assertThat(data.get(0).get(6)).isEqualTo(FormCode.ENG_CONFIRMATION.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueConfirmationLetter.sql"})
        void reissueConfirmationLetterListByJurorNumberWelsh() throws Exception {
            final String jurorNumber = "555555562";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.CONFIRMATION)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesConfirmation(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(7);
            assertThat(data.get(0).get(0)).isEqualTo("555555562");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(5)).isEqualTo(true);
            assertThat(data.get(0).get(6)).isEqualTo(FormCode.BI_CONFIRMATION.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueConfirmationLetter.sql"})
        void reissueConfirmationLetterListByPoolNumber() throws Exception {
            final String poolNumber = "415220504";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .poolNumber(poolNumber)
                .letterType(LetterType.CONFIRMATION)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesConfirmation(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(3);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueConfirmationLetter.sql"})
        void reissueConfirmationLetterListByPending() throws Exception {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .showAllQueued(true)
                .letterType(LetterType.CONFIRMATION)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesConfirmation(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(7);
            assertThat(data.get(0).get(0)).isEqualTo("555555563");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo(LocalDate.now().toString());
            assertThat(data.get(0).get(5)).isEqualTo(false);
            assertThat(data.get(0).get(6)).isEqualTo(FormCode.BI_CONFIRMATION.getCode());
        }

        private void verifyHeadingsAndTypesConfirmation(ReissueLetterListResponseDto reissueLetterListResponseDto) {
            assertThat(reissueLetterListResponseDto).isNotNull();
            List<String> headings = reissueLetterListResponseDto.getHeadings();
            assertThat(headings).isNotNull();
            assertThat(headings.size()).as("Expect there to be 7 headings").isEqualTo(7);
            assertThat(headings.get(0)).isEqualTo("Juror number");
            assertThat(headings.get(1)).isEqualTo("First name");
            assertThat(headings.get(2)).isEqualTo("Last name");
            assertThat(headings.get(3)).isEqualTo("Postcode");
            assertThat(headings.get(4)).isEqualTo("Date printed");
            assertThat(headings.get(5)).isEqualTo("hidden_extracted_flag");
            assertThat(headings.get(6)).isEqualTo("hidden_form_code");

            List<String> dataTypes = reissueLetterListResponseDto.getDataTypes();
            assertThat(dataTypes).isNotNull();
            assertThat(dataTypes.size()).as("Expect there to be 7 data types").isEqualTo(7);
            assertThat(dataTypes.get(0)).isEqualTo("string");
            assertThat(dataTypes.get(1)).isEqualTo("string");
            assertThat(dataTypes.get(2)).isEqualTo("string");
            assertThat(dataTypes.get(3)).isEqualTo("string");
            assertThat(dataTypes.get(4)).isEqualTo("date");
            assertThat(dataTypes.get(5)).isEqualTo("boolean");
            assertThat(dataTypes.get(6)).isEqualTo("string");
        }

    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/reissue-letter")
    class ReissueLetterTests {

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void reissueDeferralGrantedLetterHappy() throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode(FormCode.ENG_DEFERRAL.getCode())
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueConfirmationLetter.sql"})
        void reissueConfirmationLetterHappy() throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode(FormCode.ENG_CONFIRMATION.getCode())
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, HttpMethod.POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/delete-pending-letter")
    class DeleteLetterTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "5229A",
            "5229AC",
            "5224A",
            "5224AC"})
        @Sql({"/db/mod/truncate.sql", "/db/LetterController_deletePendingBureauLetter.sql"})
        void deleteLetterHappy(String formCode) throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/delete-pending-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            final ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode(formCode)
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            final ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, HttpMethod.DELETE, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(HttpStatus.OK);

        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void deleteDeferralLetterUnhappyNotFound() throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/delete-pending-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            final ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("995555561")
                    .formCode(FormCode.ENG_DEFERRAL.getCode())
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            final ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, HttpMethod.DELETE, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Not Found")
                .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void deleteDeferralLetterCourtUserUnhappyNoAccess() throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/delete-pending-letter");
            final String bureauJwt = createBureauJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("995555561")
                    .formCode(FormCode.ENG_DEFERRAL.getCode())
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, HttpMethod.DELETE, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Forbidden")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    private void verifyPaperResponse(String jurorNumber, ProcessingStatus status) {
        PaperResponse paperResponse =
            jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(paperResponse).isNotNull();
        Assertions.assertThat(paperResponse.getProcessingStatus()).isEqualTo(status);
    }

    private void verifyDigitalResponse(String jurorNumber, ProcessingStatus status) {
        DigitalResponse response = jurorResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(response).isNotNull();
        Assertions.assertThat(response.getProcessingStatus()).isEqualTo(status);
    }

    private static void verifyHeadingsAndTypesDeferrals(ReissueLetterListResponseDto reissueLetterListResponseDto) {
        assertThat(reissueLetterListResponseDto).isNotNull();
        List<String> headings = reissueLetterListResponseDto.getHeadings();
        assertThat(headings).isNotNull();
        assertThat(headings.size()).as("Expect there to be 10 headings").isEqualTo(10);
        assertThat(headings.get(0)).isEqualTo("Juror number");
        assertThat(headings.get(1)).isEqualTo("First name");
        assertThat(headings.get(2)).isEqualTo("Last name");
        assertThat(headings.get(3)).isEqualTo("Postcode");
        assertThat(headings.get(4)).isEqualTo("Status");
        assertThat(headings.get(5)).isEqualTo("Deferred to");
        assertThat(headings.get(6)).isEqualTo("Reason");
        assertThat(headings.get(7)).isEqualTo("Date printed");
        assertThat(headings.get(8)).isEqualTo("hidden_extracted_flag");
        assertThat(headings.get(9)).isEqualTo("hidden_form_code");

        List<String> dataTypes = reissueLetterListResponseDto.getDataTypes();
        assertThat(dataTypes).isNotNull();
        assertThat(dataTypes.size()).as("Expect there to be 10 data types").isEqualTo(10);
        assertThat(dataTypes.get(0)).isEqualTo("string");
        assertThat(dataTypes.get(1)).isEqualTo("string");
        assertThat(dataTypes.get(2)).isEqualTo("string");
        assertThat(dataTypes.get(3)).isEqualTo("string");
        assertThat(dataTypes.get(4)).isEqualTo("string");
        assertThat(dataTypes.get(5)).isEqualTo("date");
        assertThat(dataTypes.get(6)).isEqualTo("string");
        assertThat(dataTypes.get(7)).isEqualTo("date");
        assertThat(dataTypes.get(8)).isEqualTo("boolean");
        assertThat(dataTypes.get(9)).isEqualTo("string");
    }
}
