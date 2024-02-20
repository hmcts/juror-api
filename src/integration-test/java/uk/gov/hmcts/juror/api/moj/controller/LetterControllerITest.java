package uk.gov.hmcts.juror.api.moj.controller;

import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.DeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.letter.LetterId;
import uk.gov.hmcts.juror.api.moj.domain.letter.RequestLetter;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.RequestLetterRepository;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
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
    @Autowired
    private ExcusalCodeRepository excusalCodeRepository;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Nested
    @DisplayName("POST /api/v1/moj/letter/request-information")
    class RequestInformation {

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
                .build(), httpHeaders, POST, uri);
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
                .build(), httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);

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
                + "Part 2 Section C, Part 2 Section C, Part 2 Section E, Part 2 Section A, Part 3 Section A/B/C, Part"
                + " 2 "
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
                .build(), httpHeaders, POST, uri);
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
                .build(), httpHeaders, POST, uri);
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
                .build(), httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be BAD_REQUEST, cannot process when no missing info")
                .isEqualTo(BAD_REQUEST);

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
                .build(), httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be BAD_REQUEST, cannot process when Signature is missing")
                .isEqualTo(BAD_REQUEST);

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
                .build(), httpHeaders, POST, uri);
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
                + "Part 2 Section C, Part 2 Section C, Part 2 Section E, Part 2 Section A, Part 3 Section A/B/C, Part"
                + " 2 "
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
                .build(), httpHeaders, POST, uri);
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
                .build(), httpHeaders, POST, uri);
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
        @Sql({"/db/mod/truncate.sql", "/db/LetterController_initPoolMemberAndResponse.sql"})
        void requestInformationBureauUserPaperMissingReplyMethod() throws Exception {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/request-information");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
                .jurorNumber("222222222")
                .missingInformation(List.of(
                    MissingInformation.BAIL))
                .build(), httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be BAD_REQUEST, cannot process when Reply Method is unspecified")
                .isEqualTo(BAD_REQUEST);

        }

        private void verifyPaperResponse(String jurorNumber, ProcessingStatus status) {
            PaperResponse paperResponse =
                jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
            assertThat(paperResponse).isNotNull();
            assertThat(paperResponse.getProcessingStatus()).isEqualTo(status);
        }

        private void verifyDigitalResponse(String jurorNumber, ProcessingStatus status) {
            DigitalResponse response = jurorResponseRepository.findByJurorNumber(jurorNumber);
            assertThat(response).isNotNull();
            assertThat(response.getProcessingStatus()).isEqualTo(status);
        }

    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/court-letter-list")
    class CourtLetterList {

        private static final String GET_LETTER_LIST_URI = "/api/v1/moj/letter/court-letter-list";

        @Nested
        @DisplayName("Deferral Granted List")
        class DeferralGranted {
            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void jurorNumberSearchExcludePrinted() {

                final String jurorNumber = "555555562";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(0);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void jurorNumberSearchIncludePrinted() {

                final String jurorNumber = "555555562";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(1);

                DeferralLetterData data = (DeferralLetterData) responseBody.get(0);
                assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
                assertThat(data.getFirstName()).isEqualTo("TEST_TWO");
                assertThat(data.getLastName()).isEqualTo("PERSON");
                assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
                assertThat(data.getStatus()).isEqualTo("Deferred");
                assertThat(data.getDeferredTo()).isEqualTo(LocalDate.now().plusDays(10));
                String reason = excusalCodeRepository.findById("A").orElse(new ExcusalCodeEntity()).getDescription();
                assertThat(data.getReason()).isEqualToIgnoringCase(reason);
                assertThat(data.getDatePrinted()).isEqualTo(LocalDate.now().minusDays(9));
                assertThat(data.getPoolNumber()).isEqualTo("415220401");
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void jurorNumberSearchBureauDeferral() {

                final String jurorNumber = "555555561";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(0);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void jurorNameSearchExcludePrinted() {

                final String jurorName = "TEST_SEVEN";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorName(jurorName)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(1);

                DeferralLetterData data = (DeferralLetterData) responseBody.get(0);
                assertThat(data.getJurorNumber()).isEqualTo("555555567");
                assertThat(data.getFirstName()).isEqualTo("TEST_SEVEN");
                assertThat(data.getLastName()).isEqualTo("PERSON");
                assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
                assertThat(data.getStatus()).isEqualTo("Deferred");
                assertThat(data.getDeferredTo()).isEqualTo(LocalDate.now().plusDays(25));
                String reason = excusalCodeRepository.findById("T").orElse(new ExcusalCodeEntity()).getDescription();
                assertThat(data.getReason()).isEqualToIgnoringCase(reason);
                assertThat(data.getDatePrinted()).isNull();
                assertThat(data.getPoolNumber()).isEqualTo("415220404");
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void jurorNameSearchIncludePrinted() {
                final String jurorName = "TEST_SEVEN";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorName(jurorName)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(2);

                List<DeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (DeferralLetterData) data)
                    .filter(data -> "555555567".equalsIgnoreCase(data.getJurorNumber()))
                    .toList();

                assertThat(dataList.size()).isEqualTo(2);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() == null).count()).isEqualTo(1);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() != null).count()).isEqualTo(1);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void jurorPostcodeSearchExcludePrinted() {

                final String postcode = "CH1 2AN";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorPostcode(postcode)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(5);

                List<DeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (DeferralLetterData) data)
                    .filter(data -> data.getPostcode().equalsIgnoreCase(postcode)
                        && data.getDatePrinted() == null)
                    .toList();

                assertThat(dataList).size().isEqualTo(5);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void jurorPostcodeSearchIncludePrinted() {

                final String postcode = "CH1 2AN";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorPostcode(postcode)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(7);

                List<DeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (DeferralLetterData) data)
                    .filter(data -> data.getPostcode().equalsIgnoreCase(postcode))
                    .toList();

                assertThat(dataList).size().isEqualTo(7);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() == null).count())
                    .as("Expect 5 of the returned records to not have a letter previously printed")
                    .isEqualTo(5);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() != null).count())
                    .as("Expect 2 of the returned records to not a letter previously printed")
                    .isEqualTo(2);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void poolNumberSearchExcludePrinted() {

                final String poolNumber = "415220401";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .poolNumber(poolNumber)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(3);

                List<DeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (DeferralLetterData) data)
                    .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber)
                        && data.getDatePrinted() == null)
                    .toList();

                assertThat(dataList).size().isEqualTo(3);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void poolNumberSearchIncludePrinted() {

                final String poolNumber = "415220401";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .poolNumber(poolNumber)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(5);

                List<DeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (DeferralLetterData) data)
                    .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber))
                    .toList();

                assertThat(dataList).size().isEqualTo(5);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() == null).count())
                    .as("Expect 3 of the returned records to not have a letter previously printed")
                    .isEqualTo(3);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() != null).count())
                    .as("Expect 2 of the returned records to not a letter previously printed")
                    .isEqualTo(2);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void invalidUser() {
                final String jurorNumber = "555555562";
                final String payload = createBureauJwt("BUREAU_USER", "400");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<String> response =
                    template.exchange(request, String.class);

                assertThat(response).isNotNull();
                assertForbiddenResponse(response, GET_LETTER_LIST_URI);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void invalidRequestBody() {
                final String jurorNumber = "555555562";
                final String poolNumber = "415220401";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .poolNumber(poolNumber)
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<String> response =
                    template.exchange(request, String.class);

                assertThat(response).isNotNull();
                assertInvalidPayload(response, new RestResponseEntityExceptionHandler.FieldError("jurorNumber",
                    "Field jurorNumber should be excluded if any of the following fields are present: "
                        + "[poolNumber]"));
            }
        }

        @Nested
        @DisplayName("Deferral Denied List")
        class DeferralDenied {

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void jurorNumberSearchExcludePrinted() {

                final String jurorNumber = "555555567";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(0);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void jurorNumberSearchIncludePrinted() {

                final String jurorNumber = "555555567";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(1);

                NonDeferralLetterData data = (NonDeferralLetterData) responseBody.get(0);
                assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
                assertThat(data.getFirstName()).isEqualTo("TEST_SEVEN");
                assertThat(data.getLastName()).isEqualTo("PERSON");
                assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
                assertThat(data.getStatus()).isEqualTo("Responded");
                assertThat(data.getDateRefused()).isEqualTo(LocalDate.now().minusDays(8));
                String reason = excusalCodeRepository.findById("A").orElse(new ExcusalCodeEntity()).getDescription();
                assertThat(data.getReason()).isEqualToIgnoringCase(reason);
                assertThat(data.getDatePrinted()).isEqualTo(LocalDate.now().minusDays(8));
                assertThat(data.getPoolNumber()).isEqualTo("415220401");
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void jurorNumberSearchBureauDeniedDeferral() {

                final String jurorNumber = "555555562";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(0);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void jurorNameSearchExcludePrinted() {

                final String jurorName = "TEST_SIX";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorName(jurorName)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(1);

                NonDeferralLetterData data = (NonDeferralLetterData) responseBody.get(0);
                assertThat(data.getJurorNumber()).isEqualTo("555555566");
                assertThat(data.getFirstName()).isEqualTo("TEST_SIX");
                assertThat(data.getLastName()).isEqualTo("PERSON");
                assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
                assertThat(data.getStatus()).isEqualTo("Deferred");
                assertThat(data.getDateRefused()).isEqualTo(LocalDate.now().minusDays(8));
                String reason = excusalCodeRepository.findById("A").orElse(new ExcusalCodeEntity()).getDescription();
                assertThat(data.getReason()).isEqualToIgnoringCase(reason);
                assertThat(data.getDatePrinted()).isNull();
                assertThat(data.getPoolNumber()).isEqualTo("415220402");
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void jurorNameSearchIncludePrinted() {
                final String jurorName = "TEST_SIX";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorName(jurorName)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(2);

                List<NonDeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (NonDeferralLetterData) data)
                    .filter(data -> "555555566".equalsIgnoreCase(data.getJurorNumber()))
                    .toList();

                assertThat(dataList).size().isEqualTo(2);

                assertThat(dataList.stream().filter(data -> data.getDatePrinted() == null).count())
                    .as("Expect 1 of the returned records to not have a letter previously printed")
                    .isEqualTo(1);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() != null).count())
                    .as("Expect 1 of the returned records to not a letter previously printed")
                    .isEqualTo(1);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void jurorPostcodeSearchExcludePrinted() {

                final String postcode = "CH1 2AN";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorPostcode(postcode)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(3);

                List<NonDeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (NonDeferralLetterData) data)
                    .filter(data -> data.getPostcode().equalsIgnoreCase(postcode)
                        && data.getDatePrinted() == null)
                    .toList();

                assertThat(dataList).size().isEqualTo(3);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void jurorPostcodeSearchIncludePrinted() {

                final String postcode = "CH1 2AN";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorPostcode(postcode)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(6);

                List<NonDeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (NonDeferralLetterData) data)
                    .filter(data -> data.getPostcode().equalsIgnoreCase(postcode))
                    .toList();

                assertThat(dataList).size().isEqualTo(6);

                assertThat(dataList.stream().filter(data -> data.getDatePrinted() == null).count())
                    .as("Expect 3 of the returned records to not have a letter previously printed")
                    .isEqualTo(3);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() != null).count())
                    .as("Expect 3 of the returned records to not a letter previously printed")
                    .isEqualTo(3);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void poolNumberSearchExcludePrinted() {

                final String poolNumber = "415220401";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .poolNumber(poolNumber)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(1);

                List<NonDeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (NonDeferralLetterData) data)
                    .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber)
                        && data.getDatePrinted() == null)
                    .toList();

                assertThat(dataList).size().isEqualTo(1);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void poolNumberSearchIncludePrinted() {

                final String poolNumber = "415220401";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .poolNumber(poolNumber)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .includePrinted(true)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<LetterListResponseDto> response =
                    template.exchange(request, LetterListResponseDto.class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK")
                    .isEqualTo(OK);
                assertThat(response.getBody()).isNotNull();
                List<?> responseBody = response.getBody().getData();
                assertThat(responseBody.size()).isEqualTo(3);

                List<NonDeferralLetterData> dataList = responseBody.stream()
                    .map(data -> (NonDeferralLetterData) data)
                    .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber))
                    .toList();

                assertThat(dataList).size().isEqualTo(3);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() == null).count())
                    .as("Expect 1 of the returned records to not have a letter previously printed")
                    .isEqualTo(1);
                assertThat(dataList.stream().filter(data -> data.getDatePrinted() != null).count())
                    .as("Expect 2 of the returned records to not a letter previously printed")
                    .isEqualTo(2);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void invalidUser() {
                final String jurorNumber = "555555566";
                final String payload = createBureauJwt("BUREAU_USER", "400");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<String> response = template.exchange(request, String.class);

                assertThat(response).isNotNull();
                assertForbiddenResponse(response, GET_LETTER_LIST_URI);
            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
            void invalidRequestBody() {
                final String jurorNumber = "555555562";
                final String poolNumber = "415220401";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create(GET_LETTER_LIST_URI);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                    .builder()
                    .jurorNumber(jurorNumber)
                    .poolNumber(poolNumber)
                    .letterType(CourtLetterType.DEFERRAL_REFUSED)
                    .build();

                RequestEntity<CourtLetterListRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<String> response =
                    template.exchange(request, String.class);

                assertThat(response).isNotNull();
                assertInvalidPayload(response, new RestResponseEntityExceptionHandler.FieldError("jurorNumber",
                    "Field jurorNumber should be excluded if any of the following fields are present: "
                        + "[poolNumber]"));
            }
        }
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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be Not Found")
                .isEqualTo(NOT_FOUND);
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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Forbidden")
                .isEqualTo(FORBIDDEN);
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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);

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

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralDeniedLetter.sql"})
        void reissueDeferralDeniedLetterListByJurorNumber() throws Exception {
            final String jurorNumber = "555555561";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_REFUSED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferralDenied(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(10);
            assertThat(data.get(0).get(0)).isEqualTo("555555561");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo("Responded");
            assertThat(data.get(0).get(5)).isEqualTo(LocalDate.now().minusDays(10).toString()); // date refused
            assertThat(data.get(0).get(6)).isEqualTo("Carer");
            assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().minusDays(10).toString()); // date printed
            assertThat(data.get(0).get(8)).isEqualTo(true);
            assertThat(data.get(0).get(9)).isEqualTo(FormCode.ENG_DEFERRALDENIED.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralDeniedLetter.sql"})
        void reissueDeferralDeniedLetterListByJurorNumberWelsh() throws Exception {
            final String jurorNumber = "555555565";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_REFUSED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferralDenied(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(10);
            assertThat(data.get(0).get(0)).isEqualTo("555555565");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo("Responded");
            assertThat(data.get(0).get(5)).isEqualTo(LocalDate.now().minusDays(6).toString()); // date refused
            assertThat(data.get(0).get(6)).isEqualTo("Moved from area");
            assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().minusDays(6).toString()); // date printed
            assertThat(data.get(0).get(8)).isEqualTo(true);
            assertThat(data.get(0).get(9)).isEqualTo(FormCode.BI_DEFERRALDENIED.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralDeniedLetter.sql"})
        void reissueDeferralDeniedLetterListByPoolNumber() throws Exception {
            final String poolNumber = "415220401";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .poolNumber(poolNumber)
                .letterType(LetterType.DEFERRAL_REFUSED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferralDenied(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).as("Expect there to be 5 letters listed in pool").isEqualTo(5);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralDeniedLetter.sql"})
        void reissueDeferralDeniedLetterListByPending() throws Exception {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .showAllQueued(true)
                .letterType(LetterType.DEFERRAL_REFUSED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

            assertThat(response.getBody()).isNotNull();
            ReissueLetterListResponseDto reissueLetterListResponseDto = objectMapper.readValue(response.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesDeferralDenied(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).as("Expect there to be 2 pending letters").isEqualTo(2);
        }

        private void verifyHeadingsAndTypesDeferrals(ReissueLetterListResponseDto reissueLetterListResponseDto) {
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

        private void verifyHeadingsAndTypesDeferralDenied(ReissueLetterListResponseDto reissueLetterListResponseDto) {
            assertThat(reissueLetterListResponseDto).isNotNull();
            List<String> headings = reissueLetterListResponseDto.getHeadings();
            assertThat(headings).isNotNull();
            assertThat(headings.size()).as("Expect there to be 10 headings").isEqualTo(10);
            assertThat(headings.get(0)).isEqualTo("Juror number");
            assertThat(headings.get(1)).isEqualTo("First name");
            assertThat(headings.get(2)).isEqualTo("Last name");
            assertThat(headings.get(3)).isEqualTo("Postcode");
            assertThat(headings.get(4)).isEqualTo("Status");
            assertThat(headings.get(5)).isEqualTo("Date refused");
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

    @Nested
    @DisplayName("POST /api/v1/moj/letter/reissue-letter")
    @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
    class ReissueLetterTests {
        final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");

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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);
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
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/LetterController_initReissueConfirmationLetter.sql"})
        @DisplayName("Reissue excusal letter - expect okay response")
        void reissueExcusalLetterHappy() throws Exception {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555564")
                    .formCode(FormCode.ENG_EXCUSAL.getCode())
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralDeniedLetter.sql"})
        void reissueDeferralDeniedLetterHappy() throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode(FormCode.ENG_DEFERRALDENIED.getCode())
                    .datePrinted(LocalDate.now().minusDays(10))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

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
            "5224AC",
            "5226A",
            "5226AC",
            "5225",
            "5225C"})
        @Sql({"/db/mod/truncate.sql", "/db/LetterController_deletePendingBureauLetter.sql"})
        void deleteLetterHappy(String formCode) throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/delete-pending-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode(formCode)
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, DELETE, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void deleteLetterUnhappyNotFound() throws Exception {
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
                httpHeaders, DELETE, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Not Found")
                .isEqualTo(NOT_FOUND);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void deleteLetterCourtUserUnhappyNoAccess() throws Exception {
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
                httpHeaders, DELETE, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Forbidden")
                .isEqualTo(FORBIDDEN);
        }
    }

    @Nested
    @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initExcusalLetter.sql"})
    @DisplayName("POST /api/v1/moj/letter/reissue-letter-list (excusal accepted and denied letters)")
    class ReissueExcusalLetter {
        static final String PRINTED_DATE_2024_01_31 = "2024-01-31";
        static final String EXCUSAL_DATE_2024_01_21 = "2024-01-21";
        static final String JUROR = "Juror";
        static final String EXCUSED = "Excused";
        static final String EXCUSED_REASON_TRAVELLING_DIFFICULTIES = "Travelling difficulties";
        static final String JUROR_555555 = "555555";
        static final URI URL = URI.create("/api/v1/moj/letter/reissue-letter-list");

        @Test
        @DisplayName("Reissue excusal letter by juror number - expect multiple records in order")
        void reissueExcusalLetterByJurorNumberMultipleRecords() throws Exception {
            setHeaders();

            ReissueLetterListRequestDto request = buildReissueLetterListRequestDto(LetterType.EXCUSAL_GRANTED);
            request.setJurorNumber(JUROR_555555 + "561");
            ResponseEntity<String> responseEntity = invokeService(request);

            assertResponseOkay(responseEntity);

            ReissueLetterListResponseDto response = objectMapper.readValue(responseEntity.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesExcusals(response, EXCUSED);
            List<List<Object>> data = response.getData();

            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(2);

            // ensure response is in correct order
            verifyResponse(data, 0, "561", "Childcare",
                EXCUSAL_DATE_2024_01_21, PRINTED_DATE_2024_01_31, FALSE, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 1, "561", "Childcare",
                EXCUSAL_DATE_2024_01_21, PRINTED_DATE_2024_01_31, TRUE, FormCode.BI_EXCUSAL.getCode(), EXCUSED);
        }

        @Test
        @DisplayName("Reissue excusal letter by juror number - expect single record")
        void reissueExcusalLetterByJurorNumberSingleRecord() throws Exception {
            setHeaders();

            ReissueLetterListRequestDto request = buildReissueLetterListRequestDto(LetterType.EXCUSAL_GRANTED);
            request.setJurorNumber(JUROR_555555 + "562");
            ResponseEntity<String> responseEntity = invokeService(request);

            assertResponseOkay(responseEntity);

            ReissueLetterListResponseDto response = objectMapper.readValue(responseEntity.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesExcusals(response, EXCUSED);
            List<List<Object>> data = response.getData();

            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);

            verifyResponse(data, 0, "562", EXCUSED_REASON_TRAVELLING_DIFFICULTIES,
                "2024-01-22", "2024-02-01", FALSE, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);
        }

        @Test
        @DisplayName("Reissue excusal letter by pool number - expect multiple records in order")
        void reissueExcusalLetterListByPoolNumber() throws Exception {
            setHeaders();

            ReissueLetterListRequestDto request = buildReissueLetterListRequestDto(LetterType.EXCUSAL_GRANTED);
            request.setPoolNumber("415220401");
            ResponseEntity<String> responseEntity = invokeService(request);

            assertResponseOkay(responseEntity);

            ReissueLetterListResponseDto response = objectMapper.readValue(responseEntity.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesExcusals(response, EXCUSED);
            List<List<Object>> data = response.getData();

            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(5);

            // data should be in order (based on search criteria)
            verifyResponse(data, 0, "562", EXCUSED_REASON_TRAVELLING_DIFFICULTIES,
                "2024-01-22", "2024-02-01", FALSE, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 1, "565", "Moved from area",
                "2024-01-22", "2024-02-01", FALSE, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 2, "567", "Ill",
                "2024-01-28", "2024-01-27", FALSE, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 3, "563", "Childcare",
                "2024-01-25", "2024-01-25", FALSE, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 4, "564", "Childcare",
                EXCUSAL_DATE_2024_01_21, "2024-01-20", FALSE, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);
        }

        @Test
        @DisplayName("Reissue excusal letter by queued flag - expect multiple records in order")
        void reissueExcusalLetterListShowAllQueued() throws Exception {
            setHeaders();

            ReissueLetterListRequestDto request = buildReissueLetterListRequestDto(LetterType.EXCUSAL_GRANTED);
            request.setShowAllQueued(true);
            ResponseEntity<String> responseEntity = invokeService(request);

            assertResponseOkay(responseEntity);

            ReissueLetterListResponseDto response = objectMapper.readValue(responseEntity.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesExcusals(response, EXCUSED);
            List<List<Object>> data = response.getData();

            assertThat(data).as("Expect response not to be null").isNotNull();
            assertThat(data.size()).as("Expect 6 letters in response").isEqualTo(6);

            int pendingCount = data.stream().map(row -> row.get(8)).filter(flag -> flag.equals(false))
                .toArray().length;
            assertThat(pendingCount).as("Expect there to be 6 queued letters").isEqualTo(6);
        }

        @Test
        @DisplayName("Reissue excusal letter with no search criteria - expect a bad request exception")
        void reissueExcusalLetterWithNoSearchCriteria() throws Exception {
            setHeaders();

            ReissueLetterListRequestDto request = ReissueLetterListRequestDto.builder().build();
            ResponseEntity<String> responseEntity = invokeService(request);

            assertThat(responseEntity.getStatusCode()).as("Expect status code " + BAD_REQUEST)
                .isEqualTo(BAD_REQUEST);
        }

        @Test
        @DisplayName("Reissue excusal letter with no records matching criteria - expect not found exception")
        void reissueExcusalLetterWithNoRecordsMatchingCriteria() throws Exception {
            setHeaders();

            ReissueLetterListRequestDto request = buildReissueLetterListRequestDto(LetterType.EXCUSAL_GRANTED);
            request.setJurorNumber(JUROR_555555 + "568"); // letter code for this juror is 5224AC
            ResponseEntity<String> responseEntity = invokeService(request);

            assertThat(responseEntity.getStatusCode()).as("Expect status code " + NOT_FOUND)
                .isEqualTo(NOT_FOUND);

            assertThat(responseEntity.getBody()).as("Expect response to have json string").isNotNull();

            assertThat(JsonPath.read(responseEntity.getBody(), "$['error']").toString())
                .as("Expect error message to equal: Not Found")
                .isEqualTo("Not Found");

            assertThat(JsonPath.read(responseEntity.getBody(), "$['message']").toString())
                .as("Expect error message to equal: No letters found for the given criteria")
                .isEqualTo("No letters found for the given criteria");

            assertThat(JsonPath.read(responseEntity.getBody(), "$['path']").toString())
                .as("Expect path to equal: " + URL)
                .isEqualTo(URL.toString());
        }

        @Test
        @DisplayName("Reissue excusal denied letter by juror number - expect single record")
        void reissueExcusalDeniedLetterByJurorNumberSingleRecord() throws Exception {
            setHeaders();

            ReissueLetterListRequestDto request = buildReissueLetterListRequestDto(LetterType.EXCUSAL_DENIED);
            request.setJurorNumber(JUROR_555555 + "569");
            ResponseEntity<String> responseEntity = invokeService(request);

            assertResponseOkay(responseEntity);

            ReissueLetterListResponseDto response = objectMapper.readValue(responseEntity.getBody(),
                ReissueLetterListResponseDto.class);

            verifyHeadingsAndTypesExcusals(response, "refused");
            List<List<Object>> data = response.getData();

            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);

            verifyResponse(data, 0, "569", EXCUSED_REASON_TRAVELLING_DIFFICULTIES,
                "2024-01-22", "2024-02-01", FALSE, FormCode.ENG_EXCUSALDENIED.getCode(),
                "Responded");
        }

        @SuppressWarnings("java:S107")
        private void verifyResponse(List<List<Object>> data, int arrayIndex,
                                    String jurorNumberPostfix, String excusedReason, String excusalDate,
                                    String printedDate, boolean extractedFlag, String formCode, String status) {

            String jurorNumber = JUROR_555555 + jurorNumberPostfix;
            String jurorFirstName = JUROR + jurorNumberPostfix;
            String jurorSurname = JUROR + jurorNumberPostfix + "Surname";

            assertThat(data.get(arrayIndex).size())
                .as("Expect the array index to contain 10 elements").isEqualTo(10);
            assertThat(data.get(arrayIndex).get(0))
                .as(String.format("Expect juror number for array index %s to be %s", arrayIndex, jurorNumber))
                .isEqualTo(jurorNumber);
            assertThat(data.get(arrayIndex).get(1))
                .as(String.format("Expect juror first name for array index %s to be %s", arrayIndex, jurorFirstName))
                .isEqualTo(jurorFirstName);
            assertThat(data.get(arrayIndex).get(2))
                .as(String.format("Expect juror surname for array index %s to be %s", arrayIndex, jurorSurname))
                .isEqualTo(jurorSurname);
            assertThat(data.get(arrayIndex).get(3))
                .as(String.format("Expect the postcode for array index %s to be CH1 2AN", arrayIndex))
                .isEqualTo("CH1 2AN");
            assertThat(data.get(arrayIndex).get(4))
                .as(String.format("Expect the status for array index %s to be %s", arrayIndex, status))
                .isEqualTo(status);
            assertThat(data.get(arrayIndex).get(5))
                .as(String.format("Expect excusal date for array index %s to be %s", arrayIndex, excusalDate))
                .isEqualTo(excusalDate);
            assertThat(data.get(arrayIndex).get(6))
                .as(String.format("Expect excusal reason for array index %s to be %s", arrayIndex, excusedReason))
                .isEqualTo(excusedReason);
            assertThat(data.get(arrayIndex).get(7))
                .as(String.format("Expect letter printed date for array index %s to be %s", arrayIndex, printedDate))
                .isEqualTo(printedDate);
            assertThat(data.get(arrayIndex).get(8))
                .as(String.format("Expect extracted flag for array index %s to be %s", arrayIndex, extractedFlag))
                .isEqualTo(extractedFlag);
            assertThat(data.get(arrayIndex).get(9))
                .as(String.format("Expect form code hidden flag for array index %s to be %s", arrayIndex, formCode))
                .isEqualTo(formCode);
        }

        private void verifyHeadingsAndTypesExcusals(ReissueLetterListResponseDto reissueLetterListResponseDto,
                                                    String excusalType) {

            assertThat(reissueLetterListResponseDto).isNotNull();
            List<String> headings = reissueLetterListResponseDto.getHeadings();
            assertThat(headings).isNotNull();
            assertThat(headings.size()).as("Expect there to be 9 headings").isEqualTo(10);
            assertThat(headings.get(0)).isEqualTo("Juror number");
            assertThat(headings.get(1)).isEqualTo("First name");
            assertThat(headings.get(2)).isEqualTo("Last name");
            assertThat(headings.get(3)).isEqualTo("Postcode");
            assertThat(headings.get(4)).isEqualTo("Status");
            assertThat(headings.get(5)).isEqualTo("Date " + excusalType.toLowerCase());
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

        private void assertResponseOkay(ResponseEntity<String> response) {
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);

            assertThat(response.getBody()).isNotNull();
        }

        private ResponseEntity<String> invokeService(ReissueLetterListRequestDto request) {
            RequestEntity<ReissueLetterListRequestDto> requestEntity =
                new RequestEntity<>(request, httpHeaders, POST, URL);

            return template.exchange(requestEntity, String.class);
        }

        private ReissueLetterListRequestDto buildReissueLetterListRequestDto(LetterType letterType) {
            return ReissueLetterListRequestDto.builder()
                .letterType(letterType)
                .build();
        }

        private void setHeaders() throws Exception {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/print-court-letter")
    class PrintCourtLetters {

        @Test
        @SneakyThrows
        @DisplayName("Invalid request - invalid bureau user")
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
        void invalidRequestBureauUser() {
            final String jurorNumber = "555555562";
            final String payload = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/print-court-letter");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto
                .builder()
                .jurorNumbers(List.of(jurorNumber))
                .letterType(CourtLetterType.DEFERRAL_GRANTED)
                .build();

            RequestEntity<PrintLettersRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<MojException.Forbidden> response = template.exchange(request,
                MojException.Forbidden.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Forbidden - endpoint is permitted for Court Users only")
                .isEqualTo(FORBIDDEN);
        }

        @Nested
        @DisplayName("Deferral Granted")
        class DeferralGranted {

            @Test
            @SneakyThrows
            @DisplayName("Reissue Deferral Letter - Happy path - English")
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void reissueDeferralLetterHappy() {
                final String jurorNumber = "555555562";
                final String payload = createBureauJwt("COURT_USER", "415");
                final URI uri = URI.create("/api/v1/moj/letter/print-court-letter");

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                PrintLettersRequestDto requestDto = PrintLettersRequestDto
                    .builder()
                    .jurorNumbers(List.of(jurorNumber))
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<PrintLettersRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                    PrintLetterDataResponseDto[].class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                    .isEqualTo(OK);

                assertThat(response.getBody()).isNotNull();
                for (PrintLetterDataResponseDto dto : response.getBody()) {
                    assertThat(dto.getCourtName())
                        .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                        .isEqualTo("The Crown Court\nat CHESTER");
                    assertThat(dto.getCourtAddressLine1())
                        .as("Expect address line 1 to be 'THE CASTLE'")
                        .isEqualTo("THE CASTLE");
                    assertThat(dto.getCourtAddressLine2())
                        .as("Expect address line 2 to be 'CHESTER'")
                        .isEqualTo("CHESTER");
                    assertThat(dto.getCourtAddressLine3())
                        .as("Expect address line 3 to be null")
                        .isNull();
                    assertThat(dto.getCourtAddressLine4())
                        .as("Expect address line 4 to be null")
                        .isNull();
                    assertThat(dto.getCourtAddressLine5())
                        .as("Expect address line 5 to be null")
                        .isNull();
                    assertThat(dto.getCourtAddressLine6())
                        .as("Expect address line 6 to be null")
                        .isNull();
                    assertThat(dto.getCourtPostCode())
                        .as("Expect post code to be 'CH1 2AN'")
                        .isEqualTo("CH1 2AN");
                    assertThat(dto.getCourtPhoneNumber())
                        .as("Expect court number to be 01244 356726")
                        .isEqualTo("01244 356726");

                    assertThat(dto.getUrl())
                        .as("Expect URL to be www.gov.uk/jury-service")
                        .isEqualTo("www.gov.uk/jury-service");
                    assertThat(dto.getSignature())
                        .as("Expect signatory to be Jury Manager")
                        .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");

                    assertThat(dto.getJurorFirstName())
                        .as("Expect first name to be TEST_TWO")
                        .isEqualTo("TEST_TWO");
                    assertThat(dto.getJurorLastName())
                        .as("Expect last name to be PERSON")
                        .isEqualTo("PERSON");
                    assertThat(dto.getJurorAddressLine1())
                        .as("Expect address line 1 to be Address Line 1")
                        .isEqualTo("Address Line 1");
                    assertThat(dto.getJurorAddressLine2())
                        .as("Expect address line 2 to be Address  Line 2")
                        .isEqualTo("Address  Line 2");
                    assertThat(dto.getJurorAddressLine3())
                        .as("Expect address line 3 to be Address Line 3")
                        .isEqualTo("Address Line 3");
                    assertThat(dto.getJurorAddressLine4())
                        .as("Expect address line 4 to be CARDIFF")
                        .isEqualTo("CARDIFF");
                    assertThat(dto.getJurorAddressLine5())
                        .as("Expect address line 5 to be Some County")
                        .isEqualTo("Some County");
                    assertThat(dto.getJurorPostcode())
                        .as("Expect post code to be CH1 2AN")
                        .isEqualTo("CH1 2AN");
                    assertThat(dto.getJurorNumber())
                        .as("Expect juror number to be " + jurorNumber)
                        .isEqualTo(jurorNumber);
                    assertThat(dto.getDeferredToDate())
                        .as("Expect Date to be " + LocalDate.now().plusDays(10))
                        .isEqualTo(LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
                    assertThat(dto.getAttendTime())
                        .as("09:00")
                        .isEqualTo(LocalTime.of(9, 0));
                    assertThat(dto.getWelsh())
                        .as("Expect welsh to be false")
                        .isFalse();
                }
            }

            @Test
            @SneakyThrows
            @DisplayName("Reissue Deferral Letter - Happy path - Welsh")
            @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralGranted.sql"})
            void reissueDeferralLetterHappyWelsh() {
                final String jurorNumber = "555555568";
                final String payload = createBureauJwt("COURT_USER", "457");
                final URI uri = URI.create("/api/v1/moj/letter/print-court-letter");

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                PrintLettersRequestDto requestDto = PrintLettersRequestDto
                    .builder()
                    .jurorNumbers(List.of(jurorNumber))
                    .letterType(CourtLetterType.DEFERRAL_GRANTED)
                    .build();

                RequestEntity<PrintLettersRequestDto> request =
                    new RequestEntity<>(requestDto, httpHeaders, POST, uri);
                ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                    PrintLetterDataResponseDto[].class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode())
                    .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                    .isEqualTo(OK);

                assertThat(response.getBody()).isNotNull();
                for (PrintLetterDataResponseDto dto : response.getBody()) {
                    assertThat(dto.getCourtName())
                        .as("Expect court name to be " + "Llys y Goron\nynAbertawe")
                        .isEqualTo("Llys y Goron\nynAbertawe");
                    assertThat(dto.getCourtAddressLine1())
                        .as("Expect address line 1 to be 'Y LLYSOEDD BARN'")
                        .isEqualTo("Y LLYSOEDD BARN");
                    assertThat(dto.getCourtAddressLine2())
                        .as("Expect address line 2 to be 'LON SAN HELEN'")
                        .isEqualTo("LON SAN HELEN");
                    assertThat(dto.getCourtAddressLine3())
                        .as("Expect address line 3 to be ABERTAWE")
                        .isEqualTo("ABERTAWE");
                    assertThat(dto.getCourtAddressLine4())
                        .as("Expect address line 4 to be null")
                        .isNull();
                    assertThat(dto.getCourtAddressLine5())
                        .as("Expect address line 5 to be null")
                        .isNull();
                    assertThat(dto.getCourtAddressLine6())
                        .as("Expect address line 6 to be null")
                        .isNull();
                    assertThat(dto.getCourtPostCode())
                        .as("Expect post code to be 'SA1 4PF'")
                        .isEqualTo("SA1 4PF");
                    assertThat(dto.getCourtPhoneNumber())
                        .as("Expect court number to be 01792 637067")
                        .isEqualTo("01792 637067");

                    assertThat(dto.getUrl())
                        .as("Expect URL to be www.gov.uk/jury-service")
                        .isEqualTo("www.gov.uk/jury-service");
                    assertThat(dto.getSignature())
                        .as("Expect signatory to be Jury Manager")
                        .isEqualTo("Jury Manager\n\nSwyddog Llys");

                    assertThat(dto.getJurorFirstName())
                        .as("Expect first name to be TEST_SEVEN")
                        .isEqualTo("TEST_SEVEN");
                    assertThat(dto.getJurorLastName())
                        .as("Expect last name to be PERSON")
                        .isEqualTo("PERSON");
                    assertThat(dto.getJurorAddressLine1())
                        .as("Expect address line 1 to be Address Line 1")
                        .isEqualTo("Address Line 1");
                    assertThat(dto.getJurorAddressLine2())
                        .as("Expect address line 2 to be Address  Line 2")
                        .isEqualTo("Address  Line 2");
                    assertThat(dto.getJurorAddressLine3())
                        .as("Expect address line 3 to be Address Line 3")
                        .isEqualTo("Address Line 3");
                    assertThat(dto.getJurorAddressLine4())
                        .as("Expect address line 4 to be CARDIFF")
                        .isEqualTo("CARDIFF");
                    assertThat(dto.getJurorAddressLine5())
                        .as("Expect address line 5 to be Some County")
                        .isEqualTo("Some County");
                    assertThat(dto.getJurorPostcode())
                        .as("Expect post code to be CH1 2AN")
                        .isEqualTo("CH1 2AN");
                    assertThat(dto.getJurorNumber())
                        .as("Expect juror number to be " + jurorNumber)
                        .isEqualTo(jurorNumber);
                    assertThat(dto.getDeferredToDate())
                        .as("Expect Date to be 25 Chwefror 2024")
                        .isEqualTo("25 Chwefror 2024");
                    assertThat(dto.getAttendTime())
                        .as("09:30")
                        .isEqualTo(LocalTime.of(9, 30));
                    assertThat(dto.getWelsh())
                        .as("Expect welsh to be true")
                        .isTrue();
                }
            }
        }
    }

    @Nested
    @DisplayName("Deferral Refused")
    class DeferralRefused {

        @Test
        @SneakyThrows
        @DisplayName("Reissue Non Deferral Letter - Happy path - English")
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
        void englishLetter() {
            final String jurorNumber = "555555566";
            final String courtOwner = "415";
            final String payload = createBureauJwt("COURT_USER", courtOwner);
            final URI uri = URI.create("/api/v1/moj/letter/print-court-letter");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto
                .builder()
                .jurorNumbers(List.of(jurorNumber))
                .letterType(CourtLetterType.DEFERRAL_REFUSED)
                .build();

            RequestEntity<PrintLettersRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                PrintLetterDataResponseDto[].class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                .isEqualTo(OK);

            assertThat(response.getBody()).isNotNull();
            for (PrintLetterDataResponseDto dto : response.getBody()) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                    .isEqualTo("The Crown Court\nat CHESTER");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'THE CASTLE'")
                    .isEqualTo("THE CASTLE");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'CHESTER'")
                    .isEqualTo("CHESTER");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be null")
                    .isNull();
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be null")
                    .isNull();
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be null")
                    .isNull();
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be null")
                    .isNull();
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'CH1 2AN'")
                    .isEqualTo("CH1 2AN");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 01244 356726")
                    .isEqualTo("01244 356726");

                assertThat(dto.getUrl())
                    .as("Expect URL to be www.gov.uk/jury-service")
                    .isEqualTo("www.gov.uk/jury-service");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be Jury Manager")
                    .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be TEST_SIX")
                    .isEqualTo("TEST_SIX");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be PERSON")
                    .isEqualTo("PERSON");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be Address Line 1")
                    .isEqualTo("Address Line 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be Address  Line 2")
                    .isEqualTo("Address  Line 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be Address Line 3")
                    .isEqualTo("Address Line 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be CARDIFF")
                    .isEqualTo("CARDIFF");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be Some County")
                    .isEqualTo("Some County");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be CH1 2AN")
                    .isEqualTo("CH1 2AN");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getAttendTime())
                    .as("Expect Attend Time to be 09:00")
                    .isEqualTo(LocalTime.of(9, 0));
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be false")
                    .isFalse();
            }
        }

        @Test
        @SneakyThrows
        @DisplayName("Reissue Non Deferral Letter - Happy path - Welsh")
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_DeferralDenied.sql"})
        void welshLetter() {
            final String jurorNumber = "555555568";
            final String payload = createBureauJwt("COURT_USER", "457");
            final URI uri = URI.create("/api/v1/moj/letter/print-court-letter");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto
                .builder()
                .jurorNumbers(List.of(jurorNumber))
                .letterType(CourtLetterType.DEFERRAL_REFUSED)
                .build();

            RequestEntity<PrintLettersRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                PrintLetterDataResponseDto[].class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                .isEqualTo(OK);

            assertThat(response.getBody()).isNotNull();
            for (PrintLetterDataResponseDto dto : response.getBody()) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "Llys y Goron\nynAbertawe")
                    .isEqualTo("Llys y Goron\nynAbertawe");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'Y LLYSOEDD BARN'")
                    .isEqualTo("Y LLYSOEDD BARN");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'LON SAN HELEN'")
                    .isEqualTo("LON SAN HELEN");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be ABERTAWE")
                    .isEqualTo("ABERTAWE");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be null")
                    .isNull();
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be null")
                    .isNull();
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be null")
                    .isNull();
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'SA1 4PF'")
                    .isEqualTo("SA1 4PF");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 01792 637067")
                    .isEqualTo("01792 637067");

                assertThat(dto.getUrl())
                    .as("Expect URL to be www.gov.uk/jury-service")
                    .isEqualTo("www.gov.uk/jury-service");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be Jury Manager")
                    .isEqualTo("Jury Manager\n\nSwyddog Llys");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be TEST_SEVEN")
                    .isEqualTo("TEST_SEVEN");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be PERSON")
                    .isEqualTo("PERSON");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be Address Line 1")
                    .isEqualTo("Address Line 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be Address  Line 2")
                    .isEqualTo("Address  Line 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be Address Line 3")
                    .isEqualTo("Address Line 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be CARDIFF")
                    .isEqualTo("CARDIFF");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be Some County")
                    .isEqualTo("Some County");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be CH1 2AN")
                    .isEqualTo("CH1 2AN");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getAttendTime())
                    .as("Expect Attend Time to be 09:30")
                    .isEqualTo(LocalTime.of(9, 30));
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be true")
                    .isTrue();
            }
        }
    }
}
