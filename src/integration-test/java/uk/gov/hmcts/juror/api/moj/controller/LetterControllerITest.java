package uk.gov.hmcts.juror.api.moj.controller;

import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.internal.Failures;
import org.json.JSONObject;
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
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CertificateOfExemptionRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.DeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.ExcusalLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.FailedToAttendLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PostponeLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.WithdrawalLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JurorForExemptionListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialExemptionListDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.letter.LetterId;
import uk.gov.hmcts.juror.api.moj.domain.letter.RequestLetter;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.RequestLetterRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.PostponementLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.ShowCauseLetterListRepository;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.THURSDAY;
import static java.util.Calendar.TUESDAY;
import static java.util.Calendar.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.juror.api.TestUtil.getValuesInJsonObject;
import static uk.gov.hmcts.juror.api.TestUtils.objectMapper;
import static uk.gov.hmcts.juror.api.moj.controller.LetterControllerITest.PrintCourtLettersPostponement.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.utils.DataConversionUtil.getExceptionDetails;

/**
 * Integration tests for the API endpoints defined in LetterController.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.LawOfDemeter", "PMD.NcssCount"})
class LetterControllerITest extends AbstractIntegrationTest {
    private static final String GET_LETTER_LIST_URI = "/api/v1/moj/letter/court-letter-list";

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
    @Autowired
    private PostponementLetterListRepository postponementLetterListRepository;
    @Autowired
    private ShowCauseLetterListRepository showCauseLetterListRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

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
            assertThat(response.getStatusCode()).isEqualTo(CREATED);
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
            assertThat(response.getStatusCode()).isEqualTo(CREATED);
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
            assertThat(response.getStatusCode()).isEqualTo(CREATED);
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
            assertThat(response.getStatusCode()).isEqualTo(CREATED);
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
            assertThat(response.getStatusCode()).isEqualTo(CREATED);
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
            assertThat(response.getStatusCode()).isEqualTo(CREATED);
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
    @DisplayName("Excusal Granted : POST /api/v1/moj/letter/court-letter-list")
    class ExcusalGrantedCourtLetterList {

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedJurorNumberSearchExcludePrinted() {

            final String jurorNumber = "555555562";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(jurorNumber)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

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
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedJurorNumberSearchIncludePrinted() {

            final String jurorNumber = "555555562";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(jurorNumber)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(1);

            ExcusalLetterData data = (ExcusalLetterData) responseBody.get(0);
            assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(data.getFirstName()).isEqualTo("FNAME2");
            assertThat(data.getLastName()).isEqualTo("LNAME2");
            assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
            assertThat(data.getStatus()).isEqualTo("Excused");
            assertThat(data.getDateExcused()).isEqualTo(LocalDate.now().minusDays(5));
            assertThat(data.getReason()).isEqualToIgnoringCase(ExcusalCodeEnum.valueOf("A").getDescription());
            assertThat(data.getDatePrinted()).isEqualTo(LocalDate.now().minusDays(2));
            assertThat(data.getPoolNumber()).isEqualTo("415220502");
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedJurorNumberSearchBureauExcusal() {

            final String jurorNumber = "555555561";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(jurorNumber)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

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
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedJurorNameSearch() {

            final String jurorNumber = "555555563";
            final String jurorName = "FNAME3";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorName(jurorName)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(1);

            ExcusalLetterData data = (ExcusalLetterData) responseBody.get(0);
            assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(data.getFirstName()).isEqualTo("FNAME3");
            assertThat(data.getLastName()).isEqualTo("LNAME3");
            assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
            assertThat(data.getStatus()).isEqualTo("Excused");
            assertThat(data.getDateExcused()).isEqualTo(LocalDate.now().minusDays(5));
            assertThat(data.getReason()).isEqualToIgnoringCase(ExcusalCodeEnum.valueOf("A").getDescription());
            assertThat(data.getDatePrinted()).isNull();
            assertThat(data.getPoolNumber()).isEqualTo("415220502");
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedJurorNameSearchIncludePrinted() {
            final String jurorName = "FNAME3";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorName(jurorName)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(3);

            List<ExcusalLetterData> dataList = responseBody.stream()
                .map(data -> (ExcusalLetterData) data)
                .filter(data -> "FNAME3".equalsIgnoreCase(data.getFirstName()))
                .toList();

            assertThat(dataList).size().isEqualTo(3);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedJurorPostcodeSearch() {

            final String postcode = "CH1 2AN";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorPostcode(postcode)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(2);

            List<ExcusalLetterData> dataList = responseBody.stream()
                .map(data -> (ExcusalLetterData) data)
                .filter(data -> data.getPostcode().equalsIgnoreCase(postcode))
                .toList();

            assertThat(dataList).size().isEqualTo(2);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedJurorPostcodeSearchIncludePrinted() {

            final String postcode = "CH1 2AN";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorPostcode(postcode)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(6);

            List<ExcusalLetterData> dataList = responseBody.stream()
                .map(data -> (ExcusalLetterData) data)
                .filter(data -> data.getPostcode().equalsIgnoreCase(postcode))
                .toList();

            assertThat(dataList).size().isEqualTo(6);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedPoolNumberSearchExcludePrinted() {

            final String poolNumber = "415220502";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber(poolNumber)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(2);

            List<ExcusalLetterData> dataList = responseBody.stream()
                .map(data -> (ExcusalLetterData) data)
                .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber))
                .toList();

            assertThat(dataList).size().isEqualTo(2);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
        void courtLetterListExcusalGrantedPoolNumberSearchIncludePrinted() {

            final String poolNumber = "415220502";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber(poolNumber)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(6);

            List<ExcusalLetterData> dataList = responseBody.stream()
                .map(data -> (ExcusalLetterData) data)
                .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber))
                .toList();

            assertThat(dataList).size().isEqualTo(6);
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
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissuePostponeLetter.sql"})
        void reissuePostponeLetterListHappy() throws Exception {
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final String jurorNumber = "555555551";

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.POSTPONED)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<ReissueLetterListResponseDto> response =
                template.exchange(request, ReissueLetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

            assertThat(response.getBody().getData()).isNotNull();
            assertThat(response.getBody().getHeadings()).isNotNull();
            for (int i = 0; i < response.getBody().getHeadings().size(); i++) {
                Object value = response.getBody().getData().get(0).get(i);
                switch (response.getBody().getHeadings().get(i)) {
                    case "Juror number" -> assertThat(value).isEqualTo("555555551");
                    case "First name" -> assertThat(value).isEqualTo("FNAMEFIVEFOURZERO");
                    case "Last name" -> assertThat(value).isEqualTo("LNAMEFIVEFOURZERO");
                    case "Postcode" -> assertThat(value).isEqualTo("CH1 2AN");
                    case "Status" -> assertThat(value).isEqualTo("Postponed");
                    case "Deferred to" -> assertThat(value).isEqualTo("2024-01-01");
                    case "Reason" -> assertThat(value).isEqualTo("Postponement of service");
                    case "Date printed" -> assertThat(value).isEqualTo(
                        LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    case "hidden_extracted_flag" -> assertThat(value).isEqualTo(true);
                    case "hidden_form_code" -> assertThat(value).isEqualTo("5229");
                    default -> fail("Unexpected heading: " + response.getBody().getHeadings().get(i));
                }
            }
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

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueWithdrawalLetter.sql"})
        void reissueWithdrawalLetterListByJurorNumber() throws Exception {
            final String jurorNumber = "555555561";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.WITHDRAWAL)
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

            verifyHeadingsAndTypesWithdrawal(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(10);
            assertThat(data.get(0).get(0)).isEqualTo("555555561");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo("Disqualified");
            assertThat(data.get(0).get(5)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(6)).isEqualTo("Age");
            assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(8)).isEqualTo(true);
            assertThat(data.get(0).get(9)).isEqualTo(FormCode.ENG_WITHDRAWAL.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueWithdrawalLetter.sql"})
        void reissueWithdrawalLetterListByJurorNumberWelsh() throws Exception {
            final String jurorNumber = "555555562";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.WITHDRAWAL)
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

            verifyHeadingsAndTypesWithdrawal(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);
            assertThat(data.get(0).size()).isEqualTo(10);
            assertThat(data.get(0).get(0)).isEqualTo("555555562");
            assertThat(data.get(0).get(1)).isEqualTo("FNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(2)).isEqualTo("LNAMEFIVEFOURZERO");
            assertThat(data.get(0).get(3)).isEqualTo("CH1 2AN");
            assertThat(data.get(0).get(4)).isEqualTo("Disqualified");
            assertThat(data.get(0).get(5)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(6)).isEqualTo("Age");
            assertThat(data.get(0).get(7)).isEqualTo(LocalDate.now().minusDays(1).toString());
            assertThat(data.get(0).get(8)).isEqualTo(true);
            assertThat(data.get(0).get(9)).isEqualTo(FormCode.BI_WITHDRAWAL.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueWithdrawalLetter.sql"})
        void reissueWithdrawalLetterListShowAllPending() throws Exception {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .showAllQueued(true)
                .letterType(LetterType.WITHDRAWAL)
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

            verifyHeadingsAndTypesWithdrawal(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);

            int pendingCount = data.stream().map(row -> row.get(8)).filter(flag -> flag.equals(false))
                .toArray().length;
            assertThat(pendingCount).as("Expect there to be 3 pending rows").isEqualTo(1);
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

        private void verifyHeadingsAndTypesWithdrawal(ReissueLetterListResponseDto reissueLetterListResponseDto) {
            assertThat(reissueLetterListResponseDto).isNotNull();
            List<String> headings = reissueLetterListResponseDto.getHeadings();
            assertThat(headings).isNotNull();
            assertThat(headings.size()).as("Expect there to be 10 headings").isEqualTo(10);
            assertThat(headings.get(0)).isEqualTo("Juror number");
            assertThat(headings.get(1)).isEqualTo("First name");
            assertThat(headings.get(2)).isEqualTo("Last name");
            assertThat(headings.get(3)).isEqualTo("Postcode");
            assertThat(headings.get(4)).isEqualTo("Status");
            assertThat(headings.get(5)).isEqualTo("Date disqualified");
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

        private void verifyHeadingsAndTypesSummons(ReissueLetterListResponseDto reissueLetterListResponseDto) {
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


        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueInitialSummons.sql"})
        void reissueInitialSummonsLetterListByJurorNumber() throws Exception {
            final String jurorNumber = "555555561";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.SUMMONS)
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

            verifyHeadingsAndTypesSummons(reissueLetterListResponseDto);

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
            assertThat(data.get(0).get(6)).isEqualTo(FormCode.ENG_SUMMONS.getCode());
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueInitialSummons.sql"})
        void reissueInitialSummonsLetterListByPoolNumber() throws Exception {
            final String poolNumber = "415220401";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .poolNumber(poolNumber)
                .letterType(LetterType.SUMMONS)
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

            verifyHeadingsAndTypesSummons(reissueLetterListResponseDto);

            List<List<Object>> data = reissueLetterListResponseDto.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).as("Expect there to be 6 letters listed in pool").isEqualTo(6);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueInitialSummons.sql"})
        void reissueInitialSummonsLetterListByJurorNumberWelsh() throws Exception {
            final String jurorNumber = "555555562";
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.SUMMONS)
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

            verifyHeadingsAndTypesSummons(reissueLetterListResponseDto);

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
            assertThat(data.get(0).get(6)).isEqualTo(FormCode.BI_SUMMONS.getCode());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/reissue-letter")
    @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
    class ReissueLetterTests {
        final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initPoolReissueDeferralLetter.sql"})
        void reissueDeferralGrantedLetterHappy() {
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
        void reissueConfirmationLetterHappy() {
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
        void reissueExcusalLetterHappy() {
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
        void reissueDeferralDeniedLetterHappy() {
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

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueWithdrawalLetter.sql"})
        void reissueWithdrawalLetterHappy() {
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode(FormCode.ENG_WITHDRAWAL.getCode())
                    .datePrinted(LocalDate.now().minusDays(1))
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

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueWithdrawalLetter.sql"})
        void reissueWithdrawalListUnhappyNotFound() {
            final String jurorNumber = "995555561";
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter-list");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.WITHDRAWAL)
                .build();

            RequestEntity<ReissueLetterListRequestDto> request = new RequestEntity<>(reissueLetterListRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be Not Found")
                .isEqualTo(NOT_FOUND);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissuePostponeLetter.sql"})
        void reissuePostponeLetterHappy() {
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555551")
                    .formCode(FormCode.ENG_POSTPONE.getCode())
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            // making sure the test passes on the weekend as letters cannot be sent via weekend
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.DAY_OF_WEEK) == SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == SUNDAY) {
                assertBusinessRuleViolation(response, "Can not generate a letter on a weekend",
                    MojException.BusinessRuleViolation.ErrorCode.LETTER_CANNOT_GENERATE_ON_WEEKEND);
                return;
            }

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);

            Optional<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findByJurorNumberFormCodeAndPending(
                "555555551",
                FormCode.ENG_POSTPONE.getCode());
            assertThat(bulkPrintData).isPresent();
            assertThat(bulkPrintData.get().getExtractedFlag())
                .as("Expect extracted flag to be null")
                .isNull();

            assertThat(bulkPrintData.get().getCreationDate())
                .as("Expect creation date to be today")
                .isEqualTo(LocalDate.now());
            assertThat(bulkPrintData.get().getFormAttribute().getFormType())
                .as("Expect form attribute form code to be " + FormCode.ENG_POSTPONE.getCode());


            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case MONDAY, TUESDAY, WEDNESDAY -> assertThat(bulkPrintData.get().getDetailRec()).contains(
                    LocalDate
                        .now()
                        .plusDays(2)
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")).toUpperCase());
                case THURSDAY, FRIDAY -> assertThat(bulkPrintData.get().getDetailRec()).contains(
                    LocalDate
                        .now()
                        .plusDays(4)
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")).toUpperCase());
                default -> fail("Unexpected day of the week");
            }
        }

        private void triggerValidBureau(
            ReissueLetterRequestDto.ReissueLetterRequestData... requestBody) {
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(requestBody))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(reissueLetterRequestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isEqualTo("Letters reissued");
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initSummonsReminderLetter.sql"})
        void reissueSummonsReminderLetterHappy() throws Exception {
            triggerValidBureau(
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode(FormCode.ENG_SUMMONS_REMINDER.getCode())
                    .datePrinted(LocalDate.of(2024, 1, 31))
                    .build()
            );

            BulkPrintData bulkPrintData = bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted("555555561",
                    FormCode.ENG_SUMMONS_REMINDER.getCode(), LocalDate.now())
                .orElseThrow(() -> Failures.instance().failure("Expected record to be found in bulk print data table"));

            assertThat(bulkPrintData).isNotNull();
            assertThat(bulkPrintData.getExtractedFlag()).isNull();
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initSummonsReminderLetter.sql"})
        void issueSummonsReminderLetterIfLetterDoesNotExist() {
            final String jurorNumber = "555555570";

            // verify letter does not already exist for today's date (this is the date the letter is to be created)
            assertThat(bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(jurorNumber,
                FormCode.ENG_SUMMONS_REMINDER.getCode(), LocalDate.now()))
                .as("Existing letter should not exist for today's date ").isEmpty();

            // verify history does not already exist for today's date
            List<JurorHistory> updatedJurorHistoryList =
                jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, LocalDate.now());
            assertThat(updatedJurorHistoryList).as("History record should not exist").isEmpty();

            // invoke api
            triggerValidBureau(
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber(jurorNumber)
                    .formCode(FormCode.ENG_SUMMONS_REMINDER.getCode())
                    .datePrinted(LocalDate.of(2024, 1, 31))
                    .build()
            );

            // verify letter added
            BulkPrintData bulkPrintData = bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(jurorNumber,
                    FormCode.ENG_SUMMONS_REMINDER.getCode(), LocalDate.now())
                .orElseThrow(() -> Failures.instance().failure("Expected record to be found in bulk print data table"));

            assertThat(bulkPrintData).as("Letter should have been added in bulk print table").isNotNull();
            assertThat(bulkPrintData.getExtractedFlag()).as("Extracted flag should be null").isNull();

            // verify history added
            updatedJurorHistoryList =
                jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, LocalDate.now());
            assertThat(updatedJurorHistoryList).as("History record should have been added").isNotNull();
            assertThat(updatedJurorHistoryList.size()).as("Expect 1 history record").isEqualTo(1);
            assertThat(updatedJurorHistoryList.get(0).getJurorNumber()).as("Juror number is " + jurorNumber)
                .isEqualTo(jurorNumber);
            assertThat(updatedJurorHistoryList.get(0).getPoolNumber()).as("Pool number is 415220401")
                .isEqualTo("415220401");
            assertThat(updatedJurorHistoryList.get(0).getHistoryCode())
                .as("History code is " + HistoryCodeMod.NON_RESPONDED_LETTER)
                .isEqualTo(HistoryCodeMod.NON_RESPONDED_LETTER);
            assertThat(updatedJurorHistoryList.get(0).getCreatedBy()).as("History record created by SYSTEM")
                .isEqualTo("SYSTEM");
            assertThat(updatedJurorHistoryList.get(0).getDateCreated())
                .as("Date created is today's date").isAfterOrEqualTo(LocalDate.now().atStartOfDay());
            assertThat(updatedJurorHistoryList.get(0).getOtherInformation())
                .as("Other information is 'Reminder letter printed'")
                .isEqualTo("Reminder letter printed");
            assertThat(updatedJurorHistoryList.get(0).getOtherInformationDate())
                .as("Other Information Date is null").isNull();
            assertThat(updatedJurorHistoryList.get(0).getOtherInformationRef())
                .as("Other Information Ref is null").isNull();
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initSummonsReminderLetter.sql"})
        void issueSummonsReminderReprintAfterLetterIsCreated() {
            final String jurorNumber = "555555570";

            // invoke api
            triggerValidBureau(
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber(jurorNumber)
                    .formCode(FormCode.ENG_SUMMONS_REMINDER.getCode())
                    .datePrinted(LocalDate.of(2024, 1, 31))
                    .build()
            );

            // verify letter added
            BulkPrintData bulkPrintData = bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(jurorNumber,
                    FormCode.ENG_SUMMONS_REMINDER.getCode(), LocalDate.now())
                .orElseThrow(() -> Failures.instance().failure("Expected record to be found in bulk print data table"));

            assertThat(bulkPrintData).isNotNull();
            assertThat(bulkPrintData.getExtractedFlag()).isNull();

            // verify history added
            List<JurorHistory> updatedJurorHistoryList =
                jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, LocalDate.now());
            assertThat(updatedJurorHistoryList).isNotNull();
            assertThat(updatedJurorHistoryList.size()).isEqualTo(1);
            assertThat(updatedJurorHistoryList.get(0).getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(updatedJurorHistoryList.get(0).getPoolNumber()).isEqualTo("415220401");
            assertThat(updatedJurorHistoryList.get(0).getHistoryCode())
                .isEqualTo(HistoryCodeMod.NON_RESPONDED_LETTER);
            assertThat(updatedJurorHistoryList.get(0).getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(updatedJurorHistoryList.get(0).getDateCreated().isEqual(LocalDate.now().atStartOfDay()));
            assertThat(updatedJurorHistoryList.get(0).getOtherInformation())
                .isEqualTo("Reminder letter printed");
            assertThat(updatedJurorHistoryList.get(0).getOtherInformationDate()).isNull();
            assertThat(updatedJurorHistoryList.get(0).getOtherInformationRef()).isNull();

            // invoke api again for same juror and letter
            final URI uri = URI.create("/api/v1/moj/letter/reissue-letter");
            final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ReissueLetterRequestDto.ReissueLetterRequestData requestBody =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber(jurorNumber)
                    .formCode(FormCode.ENG_SUMMONS_REMINDER.getCode())
                    .datePrinted(LocalDate.now()) // use today's date as letter was printed today
                    .build();

            ReissueLetterRequestDto requestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(requestBody))
                .build();

            RequestEntity<ReissueLetterRequestDto> request = new RequestEntity<>(requestDto,
                httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response.getStatusCode()).as("Expect response to be BAD_REQUEST")
                .isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.getString("error")).isEqualTo("Bad Request");
            assertThat(exceptionDetails.getString("message"))
                .isEqualTo("Letter already pending reprint for juror 555555570");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/delete-pending-letter")
    class DeleteLetterTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "5229A",
            "5229AC",
            "5224",
            "5224A",
            "5224AC",
            "5224C",
            "5226A",
            "5226AC",
            "5225",
            "5225C"})
        @Sql({"/db/mod/truncate.sql", "/db/LetterController_deletePendingBureauLetter.sql"})
        void deleteLetterHappy(String formCode) {
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
        void deleteLetterUnhappyNotFound() {
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
        void deleteLetterCourtUserUnhappyNoAccess() {
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
                EXCUSAL_DATE_2024_01_21, PRINTED_DATE_2024_01_31, false, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 1, "561", "Childcare",
                EXCUSAL_DATE_2024_01_21, PRINTED_DATE_2024_01_31, true, FormCode.BI_EXCUSAL.getCode(), EXCUSED);
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
                "2024-01-22", "2024-02-01", false, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);
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
                "2024-01-22", "2024-02-01", false, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 1, "565", "Moved from area",
                "2024-01-22", "2024-02-01", false, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 2, "567", "Ill",
                "2024-01-28", "2024-01-27", false, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 3, "563", "Childcare",
                "2024-01-25", "2024-01-25", false, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);

            verifyResponse(data, 4, "564", "Childcare",
                EXCUSAL_DATE_2024_01_21, "2024-01-20", false, FormCode.ENG_EXCUSAL.getCode(), EXCUSED);
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

            ReissueLetterListRequestDto request = buildReissueLetterListRequestDto(LetterType.EXCUSAL_REFUSED);
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
                "2024-01-22", "2024-02-01", false, FormCode.ENG_EXCUSALDENIED.getCode(),
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
    @Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initSummonsReminderLetter.sql"})
    @DisplayName("POST /api/v1/moj/letter/reissue-letter-list (summons reminder)")
    class ReissueSummonsReminderLetter {

        public static final String URL = "/api/v1/moj/letter/reissue-letter-list";

        protected ReissueLetterListResponseDto triggerValid(ReissueLetterListRequestDto requestDto) throws Exception {
            final String jwt = createBureauJwt("BUREAU_USER", "400");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<ReissueLetterListResponseDto> response = template.exchange(
                new RequestEntity<>(requestDto, httpHeaders, POST, URI.create(URL)),
                ReissueLetterListResponseDto.class);
            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(OK);
            assertThat(response.getBody())
                .as("Expect no body")
                .isNotNull();
            return response.getBody();
        }

        @Test
        @DisplayName("Get Reissue summons reminder letter")
        void reissueSummonsReminderLetterByJurorNumber() throws Exception {
            ReissueLetterListResponseDto response = triggerValid(ReissueLetterListRequestDto.builder()
                .letterType(LetterType.SUMMONED_REMINDER)
                .jurorName("Juror561")
                .build());
            verifyHeadingsAndTypes(response);
            List<List<Object>> data = response.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(2);
            List<Object> data1 = data.get(0);
            assertThat(data1).hasSize(7);
            assertThat(data1.get(0)).isEqualTo("555555561");
            assertThat(data1.get(1)).isEqualTo("Juror561");
            assertThat(data1.get(2)).isEqualTo("Juror561Surname");
            assertThat(data1.get(3)).isEqualTo("CH1 2AN");
            assertThat(data1.get(4)).isEqualTo("2024-01-31");
            assertThat(data1.get(5)).isEqualTo(true);
            assertThat(data1.get(6)).isEqualTo("5228");
            List<Object> data2 = data.get(1);
            assertThat(data2).hasSize(7);
            assertThat(data2.get(0)).isEqualTo("555555561");
            assertThat(data2.get(1)).isEqualTo("Juror561");
            assertThat(data2.get(2)).isEqualTo("Juror561Surname");
            assertThat(data2.get(3)).isEqualTo("CH1 2AN");
            assertThat(data2.get(4)).isEqualTo("2024-01-31");
            assertThat(data1.get(5)).isEqualTo(true);
            assertThat(data2.get(6)).isEqualTo("5228C");
        }

        @Test
        @DisplayName("Get Reissue summons reminder letter - not created (not in bulk print table)")
        void reissueSummonsReminderLetterByJurorNumberLetterNotCreated() throws Exception {
            ReissueLetterListResponseDto response = triggerValid(ReissueLetterListRequestDto.builder()
                .letterType(LetterType.SUMMONED_REMINDER)
                .jurorName("Juror570")
                .build());
            verifyHeadingsAndTypes(response);

            List<List<Object>> data = response.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(1);

            List<Object> dataIndex0 = data.get(0);
            verifyResponse(dataIndex0, "570", null, false, null);
        }

        @Test
        @DisplayName("Get Reissue summons reminder letter - pool number (can be pending, printed, not created)")
        void reissueSummonsReminderLetterByPoolNumber() throws Exception {
            ReissueLetterListResponseDto response = triggerValid(ReissueLetterListRequestDto.builder()
                .letterType(LetterType.SUMMONED_REMINDER)
                .poolNumber("415220401")
                .build());
            verifyHeadingsAndTypes(response);

            List<List<Object>> data = response.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(7);

            List<Object> dataIndex0 = data.get(0); // not created
            verifyResponse(dataIndex0, "570", null, false, null);

            List<Object> dataIndex1 = data.get(1);
            verifyResponse(dataIndex1, "562", "2024-02-01", false, "5228");

            List<Object> dataIndex2 = data.get(2);
            verifyResponse(dataIndex2, "565", "2024-02-01", false, "5228");

            List<Object> dataIndex3 = data.get(3);
            verifyResponse(dataIndex3, "571", "2024-01-31", true, "5228C");

            List<Object> dataIndex4 = data.get(4);
            verifyResponse(dataIndex4, "567", "2024-01-27", false, "5228");

            List<Object> dataIndex5 = data.get(5);
            verifyResponse(dataIndex5, "563", "2024-01-25", false, "5228");

            List<Object> dataIndex6 = data.get(6);
            verifyResponse(dataIndex6, "564", "2024-01-20", false, "5228");
        }

        @Test
        @DisplayName("Get Reissue summons reminder letter - all queued (pending and printed in Bulk Table)")
        void reissueSummonsReminderLetterAllQueued() throws Exception {
            ReissueLetterListResponseDto response = triggerValid(ReissueLetterListRequestDto.builder()
                .letterType(LetterType.SUMMONED_REMINDER)
                .showAllQueued(true)
                .build());
            verifyHeadingsAndTypes(response);

            List<List<Object>> data = response.getData();
            assertThat(data).isNotNull();
            assertThat(data.size()).isEqualTo(8);

            List<Object> dataIndex0 = data.get(0);
            verifyResponse(dataIndex0, "562", "2024-02-01", false, "5228");

            List<Object> dataIndex1 = data.get(1);
            verifyResponse(dataIndex1, "565", "2024-02-01", false, "5228");

            List<Object> dataIndex2 = data.get(2);
            verifyResponse(dataIndex2, "561", "2024-01-31", true, "5228");

            List<Object> dataIndex3 = data.get(3);
            verifyResponse(dataIndex3, "561", "2024-01-31", true, "5228C");

            List<Object> dataIndex4 = data.get(4);
            verifyResponse(dataIndex4, "571", "2024-01-31", true, "5228C");

            List<Object> dataIndex5 = data.get(5);
            verifyResponse(dataIndex5, "567", "2024-01-27", false, "5228");

            List<Object> dataIndex6 = data.get(6);
            verifyResponse(dataIndex6, "563", "2024-01-25", false, "5228");

            List<Object> dataIndex7 = data.get(7);
            verifyResponse(dataIndex7, "564", "2024-01-20", false, "5228");
        }

        private void verifyHeadings(ReissueLetterListResponseDto reissueLetterListResponseDto) {

            assertThat(reissueLetterListResponseDto).isNotNull();
            List<String> headings = reissueLetterListResponseDto.getHeadings();
            assertThat(headings).isNotNull();
            assertThat(headings).as("Expect there to be 7 headings").hasSize(7);
            assertThat(headings.get(0)).isEqualTo("Juror number");
            assertThat(headings.get(1)).isEqualTo("First name");
            assertThat(headings.get(2)).isEqualTo("Last name");
            assertThat(headings.get(3)).isEqualTo("Postcode");
            assertThat(headings.get(4)).isEqualTo("Date printed");
            assertThat(headings.get(5)).isEqualTo("hidden_extracted_flag");
            assertThat(headings.get(6)).isEqualTo("hidden_form_code");
        }

        private void verifyTypes(ReissueLetterListResponseDto reissueLetterListResponseDto) {
            List<String> dataTypes = reissueLetterListResponseDto.getDataTypes();
            assertThat(dataTypes).isNotNull();
            assertThat(dataTypes).as("Expect there to be 7 data types").hasSize(7);
            assertThat(dataTypes.get(0)).isEqualTo("string");
            assertThat(dataTypes.get(1)).isEqualTo("string");
            assertThat(dataTypes.get(2)).isEqualTo("string");
            assertThat(dataTypes.get(3)).isEqualTo("string");
            assertThat(dataTypes.get(4)).isEqualTo("date");
            assertThat(dataTypes.get(5)).isEqualTo("boolean");
            assertThat(dataTypes.get(6)).isEqualTo("string");
        }

        private void verifyHeadingsAndTypes(ReissueLetterListResponseDto reissueLetterListResponseDto) {
            verifyHeadings(reissueLetterListResponseDto);
            verifyTypes(reissueLetterListResponseDto);
        }

        private void verifyResponse(List<Object> dataIndex, String jurorPostfix, String datePrinted,
                                    Boolean extractedFlag, String formCode) {
            assertThat(dataIndex).hasSize(7);
            assertThat(dataIndex.get(0)).isEqualTo("555555" + jurorPostfix);
            assertThat(dataIndex.get(1)).isEqualTo("Juror" + jurorPostfix);
            assertThat(dataIndex.get(2)).isEqualTo("Juror" + jurorPostfix + "Surname");
            assertThat(dataIndex.get(3)).isEqualTo("CH1 2AN");
            assertThat(dataIndex.get(4)).isEqualTo(datePrinted);
            assertThat(dataIndex.get(5)).isEqualTo(extractedFlag);
            assertThat(dataIndex.get(6)).isEqualTo(formCode);
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

    @Nested
    @DisplayName("Withdrawal : POST /api/v1/moj/letter/court-letter-list")
    class WithdrawalCourtLetterList {

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalJurorNumberSearchExcludePrinted() {

            final String jurorNumber = "555555555";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(jurorNumber)
                .letterType(CourtLetterType.WITHDRAWAL)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

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
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalJurorNumberSearchIncludePrinted() {

            final String jurorNumber = "555555555";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(jurorNumber)
                .letterType(CourtLetterType.WITHDRAWAL)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(1);

            WithdrawalLetterData data = (WithdrawalLetterData) responseBody.get(0);
            assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(data.getFirstName()).isEqualTo("FNAME1");
            assertThat(data.getLastName()).isEqualTo("LNAME1");
            assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
            assertThat(data.getStatus()).isEqualTo("Disqualified");
            assertThat(data.getDateDisqualified()).isEqualTo(LocalDate.now().minusDays(5));
            assertThat(data.getReason()).isEqualToIgnoringCase("Age");
            assertThat(data.getDatePrinted()).isEqualTo(LocalDate.now().minusDays(5));
            assertThat(data.getPoolNumber()).isEqualTo("415220502");
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalJurorNumberSearchBureauWithdrawal() {

            final String jurorNumber = "555555556";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(jurorNumber)
                .letterType(CourtLetterType.WITHDRAWAL)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

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
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalJurorNameSearch() {

            final String jurorNumber = "555555557";
            final String jurorName = "FNAME3";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorName(jurorName)
                .letterType(CourtLetterType.WITHDRAWAL)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(1);

            WithdrawalLetterData data = (WithdrawalLetterData) responseBody.get(0);
            assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(data.getFirstName()).isEqualTo("FNAME3");
            assertThat(data.getLastName()).isEqualTo("LNAME3");
            assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
            assertThat(data.getStatus()).isEqualTo("Disqualified");
            assertThat(data.getDateDisqualified()).isEqualTo(LocalDate.now().minusDays(5));
            assertThat(data.getReason()).isEqualToIgnoringCase("Age");
            assertThat(data.getDatePrinted()).isNull();
            assertThat(data.getPoolNumber()).isEqualTo("415220502");
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalJurorNameSearchIncludePrinted() {
            final String jurorName = "FNAME3";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorName(jurorName)
                .letterType(CourtLetterType.WITHDRAWAL)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(2);

            List<WithdrawalLetterData> dataList = responseBody.stream()
                .map(data -> (WithdrawalLetterData) data)
                .filter(data -> "FNAME3".equalsIgnoreCase(data.getFirstName()))
                .toList();

            assertThat(dataList).size().isEqualTo(2);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalJurorPostcodeSearch() {

            final String postcode = "CH1 2AN";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorPostcode(postcode)
                .letterType(CourtLetterType.WITHDRAWAL)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(2);

            List<WithdrawalLetterData> dataList = responseBody.stream()
                .map(data -> (WithdrawalLetterData) data)
                .filter(data -> data.getPostcode().equalsIgnoreCase(postcode))
                .toList();

            assertThat(dataList).size().isEqualTo(2);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalJurorPostcodeSearchIncludePrinted() {

            final String postcode = "CH1 2AN";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorPostcode(postcode)
                .letterType(CourtLetterType.WITHDRAWAL)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(4);

            List<WithdrawalLetterData> dataList = responseBody.stream()
                .map(data -> (WithdrawalLetterData) data)
                .filter(data -> data.getPostcode().equalsIgnoreCase(postcode))
                .toList();

            assertThat(dataList).size().isEqualTo(4);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalPoolNumberSearchExcludePrinted() {

            final String poolNumber = "415220502";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber(poolNumber)
                .letterType(CourtLetterType.WITHDRAWAL)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(2);

            List<WithdrawalLetterData> dataList = responseBody.stream()
                .map(data -> (WithdrawalLetterData) data)
                .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber))
                .toList();

            assertThat(dataList).size().isEqualTo(2);
        }

        @Test
        @SneakyThrows
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
        void courtLetterListWithdrawalPoolNumberSearchIncludePrinted() {

            final String poolNumber = "415220502";
            final String payload = createBureauJwt("COURT_USER", "415");
            final URI uri = URI.create("/api/v1/moj/letter/court-letter-list");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber(poolNumber)
                .letterType(CourtLetterType.WITHDRAWAL)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(4);

            List<WithdrawalLetterData> dataList = responseBody.stream()
                .map(data -> (WithdrawalLetterData) data)
                .filter(data -> data.getPoolNumber().equalsIgnoreCase(poolNumber))
                .toList();

            assertThat(dataList).size().isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/print-court-letter (Postponement)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Postponement.sql"})
    class PrintCourtLettersPostponement {
        static final String JUROR_NUMBER = "5555555";
        static final URI URL = URI.create("/api/v1/moj/letter/print-court-letter");

        @Test
        @SneakyThrows
        @DisplayName("Reissue Postponement Letter - Happy path - English")
        void reissuePostponementLetterHappy() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR_NUMBER + "61");
            jurorNumbers.add(JUROR_NUMBER + "62");
            jurorNumbers.add(JUROR_NUMBER + "63");

            final String payload = createBureauJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(jurorNumbers)
                .letterType(CourtLetterType.POSTPONED)
                .build();

            RequestEntity<PrintLettersRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                PrintLetterDataResponseDto[].class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as("Expect 3 letters").isEqualTo(3);

            verifyDataEnglish(data[0], "61");
            verifyDataEnglish(data[1], "62");
            verifyDataEnglish(data[2], "63");
        }

        private void verifyDataEnglish(PrintLetterDataResponseDto dto, String jurorPostfix) {
            assertThat(dto.getCourtName())
                .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(dto.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(dto.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(dto.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(dto.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(dto.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(dto.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(dto.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(dto.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(dto.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(dto.getJurorFirstName()).as("Expect first name to be Juror " + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(dto.getJurorLastName()).as("Expect last name to be JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);
            assertThat(dto.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(dto.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(dto.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(dto.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(dto.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(dto.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getJurorNumber()).as("Expect juror number to be 5555555" + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);
            assertThat(dto.getPostponedToDate())
                .as("Expect postponed date to be " + LocalDate.now().plusDays(10))
                .isEqualTo(LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            assertThat(dto.getAttendTime())
                .as("Expect attend time to be " + LocalTime.of(9, 00))
                .isEqualTo(LocalTime.of(9, 00));
        }
    }

    @Nested
    @DisplayName("Certificate of exemption")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_CertificateOfExemption.sql"})
    class CertificateOfExemption {
        static final String JUROR_NUMBER_ENGLISH = "123456789";
        static final String TRIAL_NUMBER_ENGLISH = "T10000000";
        static final String TRIAL_NUMBER_WELSH = "T10000002";


        @Nested
        @DisplayName("POST - /api/v1/moj/letter/print-certificate-of-exemption")
        class PrintCertificateOfExemption {
            static final String PRINT_CERTIFICATE_OF_EXEMPTION_URL =
                "/api/v1/moj/letter/print-certificate-of-exemption";

            protected PrintCertificateOfExemption() {

            }

            @Nested
            @DisplayName("Positive")
            class Positive {
                @Test
                @SneakyThrows
                @DisplayName("Print Certificate of exemption - Override judge name")
                void printCertificateOfExemptionOverrideJudgeName() {
                    final String payload = createBureauJwt("COURT_USER", "415");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber(TRIAL_NUMBER_ENGLISH)
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .judge("SIR DREDD")
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, POST,
                        URI.create(PRINT_CERTIFICATE_OF_EXEMPTION_URL));

                    ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                        PrintLetterDataResponseDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode())
                        .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                        .isEqualTo(OK);
                    assertThat(response.getBody()).isNotNull();

                    PrintLetterDataResponseDto[] data = response.getBody();
                    assertThat(data.length).as("Expect 1 letters").isEqualTo(1);
                    verifyLetterDataEnglish(data[0], requestDto.getJudge());
                }

                @Test
                @SneakyThrows
                @DisplayName("Print Certificate of exemption - Happy path - English")
                void printCertificateOfExemption() {
                    final String payload = createBureauJwt("COURT_USER", "415");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber(TRIAL_NUMBER_ENGLISH)
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, POST,
                        URI.create(PRINT_CERTIFICATE_OF_EXEMPTION_URL));
                    ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                        PrintLetterDataResponseDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode())
                        .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                        .isEqualTo(OK);
                    assertThat(response.getBody()).isNotNull();

                    PrintLetterDataResponseDto[] data = response.getBody();
                    assertThat(data.length).as("Expect 1 letters").isEqualTo(1);
                    verifyLetterDataEnglish(data[0], "Test judge");
                }
            }

            @Nested
            @DisplayName("Negative")
            class Negative {
                @Test
                @SneakyThrows
                @DisplayName("Print Certificate of exemption - trial not found")
                void printCertificateOfExemptionTrialNotFound() {
                    final String payload = createBureauJwt("COURT_USER", "415");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber("T1")
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, POST,
                        URI.create(PRINT_CERTIFICATE_OF_EXEMPTION_URL));
                    ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                        PrintLetterDataResponseDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode())
                        .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                        .isEqualTo(OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().length).as("Expecting results to be zero").isEqualTo(0);
                }

                @Test
                @SneakyThrows
                @DisplayName("Print Certificate of exemption - accessing wrong court information")
                void printCertificateOfExemptionNoAccessToCourtTrial() {
                    final String payload = createBureauJwt("COURT_USER", "415");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber("T10000001")
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList("987654321"))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, POST,
                        URI.create(PRINT_CERTIFICATE_OF_EXEMPTION_URL));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);

                    assertThat(response).isNotNull();
                    assertThat(response.getBody()).isNotNull();
                    assertMojForbiddenResponse(response, PRINT_CERTIFICATE_OF_EXEMPTION_URL,
                        "User does not have ownership of the supplied juror record");
                }

                @Test
                @SneakyThrows
                @DisplayName("Print Certificate of exemption - juror number incorrect")
                void printCertificateOfExemptionTrialFoundJurorNumberIncorrect() {
                    final String payload = createBureauJwt("COURT_USER", "415");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber("T10000000")
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList("1"))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request =
                        new RequestEntity<>(requestDto, httpHeaders, POST,
                            URI.create(PRINT_CERTIFICATE_OF_EXEMPTION_URL));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);

                    assertThat(response).isNotNull();
                    assertNotFound(response, PRINT_CERTIFICATE_OF_EXEMPTION_URL, "Cannot find juror number: 1");
                }

                @Test
                @SneakyThrows
                @DisplayName("Print Certificate of exemption - Unhappy path - Bureau user")
                void printCertificateOfExemptionBureauUser() {
                    final String payload = createBureauJwt("BUREAU_USER", "400");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber("T10000000")
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request =
                        new RequestEntity<>(requestDto, httpHeaders, POST,
                            URI.create(PRINT_CERTIFICATE_OF_EXEMPTION_URL));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);
                    assertForbiddenResponse(response, PRINT_CERTIFICATE_OF_EXEMPTION_URL);
                }
            }
        }

        @Nested
        @DisplayName("GET - /api/v1/moj/letter/trials-exemption-list")
        class TrialExemptionList {
            protected TrialExemptionList() {

            }

            static final String TRIAL_EXEMPTION_URL = "/api/v1/moj/letter/trials-exemption-list";

            @Nested
            @DisplayName("Positive")
            class Positive {
                @Test
                @SneakyThrows
                @DisplayName("Certificate of exemption - trials exemption list - Happy path ")
                void trialsExemptionList() {
                    final String payload = createBureauJwt("COURT_USER", "415");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    String url = TRIAL_EXEMPTION_URL + "?court_location=415";

                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(url));
                    ResponseEntity<TrialExemptionListDto[]> response = template.exchange(request,
                        TrialExemptionListDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode())
                        .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                        .isEqualTo(OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().length)
                        .as("Expect length to be 2")
                        .isEqualTo(2);
                    TrialExemptionListDto dto = response.getBody()[0];
                    assertThat(dto.getStartDate())
                        .as("Expect start date to be " + LocalDate.now())
                        .isEqualTo(LocalDate.now());
                    assertThat(dto.getJudge())
                        .as("Expect judge to be Test judge")
                        .isEqualTo("Test judge");
                    assertThat(dto.getTrialType())
                        .as("Expect trial type to be Civil")
                        .isEqualTo("Civil");
                    assertThat(dto.getTrialNumber())
                        .as("Expect trial number to be " + TRIAL_NUMBER_ENGLISH)
                        .isEqualTo(TRIAL_NUMBER_ENGLISH);
                    assertThat(dto.getDefendants())
                        .as("Expect defendants to be TEST DEFENDANT")
                        .isEqualTo("TEST DEFENDANT");
                    assertThat(dto.getEndDate())
                        .as("Expect end date to be null")
                        .isNull();

                    TrialExemptionListDto welshDto = response.getBody()[1];
                    assertThat(welshDto.getStartDate())
                        .as("Expect start date to be " + LocalDate.now())
                        .isEqualTo(LocalDate.now());
                    assertThat(welshDto.getJudge())
                        .as("Expect judge to be Test judge")
                        .isEqualTo("Test judge");
                    assertThat(welshDto.getTrialType())
                        .as("Expect trial type to be Civil")
                        .isEqualTo("Civil");
                    assertThat(welshDto.getTrialNumber())
                        .as("Expect trial number to be " + TRIAL_NUMBER_WELSH)
                        .isEqualTo(TRIAL_NUMBER_WELSH);
                    assertThat(welshDto.getDefendants())
                        .as("Expect defendants to be TEST DEFENDANT")
                        .isEqualTo("TEST DEFENDANT");
                    assertThat(welshDto.getEndDate())
                        .as("Expect end date to be null")
                        .isNull();
                }
            }

            @Nested
            @DisplayName("Negative")
            class Negative {
                @Test
                @SneakyThrows
                @DisplayName("trials exemption list - No Active trials")
                void trialsExemptionListNoActiveTrialsFound() {
                    final String payload = createBureauJwt("COURT_USER", "761");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    String url = TRIAL_EXEMPTION_URL + "?court_location=761";

                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(url));
                    ResponseEntity<TrialExemptionListDto[]> response = template.exchange(request,
                        TrialExemptionListDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody().length).as("Expect size to be zero").isEqualTo(0);
                }

                @Test
                @SneakyThrows
                @DisplayName("trials exemption list - No court location provided")
                void trialsExemptionListNoCourtLocation() {
                    final String payload = createBureauJwt("COURT_USER", "761");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(TRIAL_EXEMPTION_URL));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);

                    assertThat(response).isNotNull();
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(JsonPath.read(response.getBody(), "$['detail']").toString())
                        .as("JSON: Error message")
                        .isEqualTo("Required parameter 'court_location' is not present.");

                }

                @Test
                @SneakyThrows
                @DisplayName("trials exemption list - Invalid court location")
                void trialsExemptionListInvalidCourtLocation() {
                    final String payload = createBureauJwt("COURT_USER", "761");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(TRIAL_EXEMPTION_URL + "?court_location=761"));
                    ResponseEntity<TrialExemptionListDto[]> response = template.exchange(request,
                        TrialExemptionListDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody().length).isEqualTo(0);
                }

                @Test
                @SneakyThrows
                @DisplayName("trials exemption list - No access to court location")
                void trialsExemptionListNoAccessToCourtLocation() {
                    final String payload = createBureauJwt("COURT_USER", "761");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    String url = TRIAL_EXEMPTION_URL + "?court_location=415";

                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(url));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);

                    assertThat(response).isNotNull();
                    assertThat(response.getBody()).isNotNull();
                    assertMojForbiddenResponse(response,
                        TRIAL_EXEMPTION_URL,
                        "Current user has insufficient permission to view the trial details for the court location");
                }


                @Test
                @SneakyThrows
                @DisplayName("trial exemption list - Unhappy path - Bureau user")
                void trialsExemptionListBureauUser() {
                    final String payload = createBureauJwt("BUREAU_USER", "400");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    String url = TRIAL_EXEMPTION_URL + "?court_location=415";

                    RequestEntity<Void> request = new RequestEntity<>(null,
                        httpHeaders, GET,
                        URI.create(url));

                    ResponseEntity<String> response = template.exchange(request,
                        String.class);
                    assertForbiddenResponse(response, TRIAL_EXEMPTION_URL);
                }
            }
        }

        @Nested
        @DisplayName("GET - /api/v1/moj/letter/jurors-exemption-list")
        class JurorForExemptionList {
            static final String JUROR_EXEMPITON_URL = "/api/v1/moj/letter/jurors-exemption-list";

            @Nested
            @DisplayName("Positive")
            class Positive {

                @Test
                @SneakyThrows
                @DisplayName("Certificate of exemption - jurors exemption list - Happy path")
                void jurorsForExemptionList() {
                    final String payload = createBureauJwt("COURT_USER", "415");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(buildJurorExemptionUri("T10000000", "415")));
                    ResponseEntity<JurorForExemptionListDto[]> response = template.exchange(request,
                        JurorForExemptionListDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode())
                        .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                        .isEqualTo(OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().length).as("Expect size to be 1").isEqualTo(1);

                    JurorForExemptionListDto dto = response.getBody()[0];
                    assertThat(dto.getJurorNumber())
                        .as("Juror number")
                        .isEqualTo(JUROR_NUMBER_ENGLISH);
                    assertThat(dto.getFirstName())
                        .as("First name")
                        .isEqualTo("FNAME");
                    assertThat(dto.getLastName())
                        .as("Last name ")
                        .isEqualTo("LNAME");
                    assertThat(dto.getDateEmpanelled())
                        .as("Date empanelled")
                        .isEqualTo(LocalDate.of(2024, 2, 22));
                }
            }

            @Nested
            @DisplayName("Negative")
            class Negative {

                @Test
                @SneakyThrows
                @DisplayName("Jurors exemption list - unhappy path - no court location found in token")
                void jurorsForExemptionListNoCourtLocationFoundInToken() {
                    final String payload = createBureauJwt("COURT_USER", "415", "416");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);
                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(buildJurorExemptionUri("T10000002", "415")));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode())
                        .as("Status code")
                        .isEqualTo(FORBIDDEN);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(JsonPath.read(response.getBody(), "$['message']").toString())
                        .isEqualTo("Current user has insufficient permission to view the trial details for the court "
                            + "location");
                }

                @Test
                @SneakyThrows
                @DisplayName("Jurors exemption list - No Panel members - Unhappy path")
                void jurorsForExemptionListNoPanelMembers() {
                    final String payload = createBureauJwt("COURT_USER", "415");
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);
                    RequestEntity<Void> request = new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(buildJurorExemptionUri("T10000002", "415")));
                    ResponseEntity<JurorForExemptionListDto[]> response = template.exchange(request,
                        JurorForExemptionListDto[].class);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode())
                        .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                        .isEqualTo(OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().length).as("Expect size to be zero").isEqualTo(0);
                }

                @Test
                @SneakyThrows
                @DisplayName("Juror exemption list - Unhappy path - Bureau user")
                void jurorsForExemptionListBureauUser() {
                    final String payload = createBureauJwt("BUREAU_USER", "400");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber("T10000000")
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, GET,
                        URI.create(buildJurorExemptionUri("T10000000", "400")));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);
                    assertForbiddenResponse(response, JUROR_EXEMPITON_URL);
                }

                @Test
                @SneakyThrows
                @DisplayName("Juror exemption list - invalid court location")
                void jurorExemptionListInvalidCourtLocation() {
                    final String payload = createBureauJwt("BUREAU_USER", "999");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);
                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber("T10000000")
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, GET,
                        URI.create(buildJurorExemptionUri("T10000000", "999")));
                    ResponseEntity<JurorForExemptionListDto[]> response = template.exchange(request,
                        JurorForExemptionListDto[].class);
                    assertThat(response.getStatusCode()).isEqualTo(OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().length).isEqualTo(0);

                }

                @Test
                @SneakyThrows
                @DisplayName("Juror exemption list - no court provided")
                void jurorExemptionListNoCourtProvided() {
                    final String payload = createBureauJwt("BUREAU_USER", "415");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);
                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .trialNumber("T10000000")
                            .exemptionPeriod("indefinite")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, GET,
                        URI.create(buildJurorExemptionUri("T10000000", "")));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(JsonPath.read(response.getBody(), "$['detail']").toString())
                        .as("JSON: Error message")
                        .isEqualTo("Required parameter 'court_location' is not present.");

                }

                @Test
                @SneakyThrows
                @DisplayName("Juror exemption list - no case number provided")
                void jurorExemptionListNoCaseNumberProvided() {
                    final String payload = createBureauJwt("BUREAU_USER", "415");

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);
                    CertificateOfExemptionRequestDto requestDto =
                        CertificateOfExemptionRequestDto.builder()
                            .exemptionPeriod("indefinite")
                            .trialNumber("T10000000")
                            .jurorNumbers(Collections.singletonList(JUROR_NUMBER_ENGLISH))
                            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
                            .build();

                    RequestEntity<CertificateOfExemptionRequestDto> request = new RequestEntity<>(requestDto,
                        httpHeaders, GET,
                        URI.create(buildJurorExemptionUri("", "415")));
                    ResponseEntity<String> response = template.exchange(request,
                        String.class);
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(JsonPath.read(response.getBody(), "$['detail']").toString())
                        .as("JSON: Error message")
                        .isEqualTo("Required parameter 'case_number' is not present.");

                }
            }

            private String buildJurorExemptionUri(String caseNumber, String courtLocation) {
                String newUri = JUROR_EXEMPITON_URL;

                if (!caseNumber.isBlank()) {
                    newUri += "?case_number=" + caseNumber;
                }

                if (!courtLocation.isBlank()) {
                    newUri += newUri.contains("?")
                        ? "&court_location=" + courtLocation
                        :
                            "?court_location=" + courtLocation;
                }
                return newUri;
            }
        }

        private void verifyLetterDataEnglish(PrintLetterDataResponseDto dto, String judge) {
            assertThat(dto.getCourtName())
                .as("Court name")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(dto.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(dto.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(dto.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(dto.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(dto.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(dto.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(dto.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(dto.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(dto.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(dto.getJurorFirstName()).as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getJurorLastName()).as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
            assertThat(dto.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(dto.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(dto.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(dto.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(dto.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(dto.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getJurorNumber()).as("Expect juror number to be " + JUROR_NUMBER_ENGLISH)
                .isEqualTo(JUROR_NUMBER_ENGLISH);
            assertThat(dto.getPeriodOfExemption())
                .as("Expect period of exemption to be indefinite")
                .isEqualTo("indefinite");
            assertThat(dto.getJudgeName())
                .as("Expect judge name to be " + judge)
                .isEqualTo(judge);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/court-letter-list (Postponement)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Postponement.sql"})
    class CourtLetterListPostponement {
        static final URI URL = URI.create(GET_LETTER_LIST_URI);
        static final String JUROR_555555562 = "555555562";
        static final String COURT_USER = "COURT_USER";
        static final String OWNER_415 = "415";
        static final String RESPONSE_OK_MESSAGE = "Expect HTTP Response to be OK";

        @Test
        @SneakyThrows
        @DisplayName("Postponement letter criteria met - juror number is valid and exclude printed letters")
        void courtLetterListPostponedJurorNumberSearchExcludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_555555562)
                .letterType(CourtLetterType.POSTPONED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as(RESPONSE_OK_MESSAGE)
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(0);
        }

        @Test
        @SneakyThrows
        @DisplayName("Postponement letter criteria met - juror number and include printed")
        void courtLetterListPostponedJurorNumberSearchIncludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_555555562)
                .letterType(CourtLetterType.POSTPONED)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as(RESPONSE_OK_MESSAGE)
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(1);

            PostponeLetterData data = (PostponeLetterData) responseBody.get(0);
            assertThat(data.getJurorNumber()).isEqualTo(JUROR_555555562);
            assertThat(data.getFirstName()).isEqualTo("JurorForename62");
            assertThat(data.getLastName()).isEqualTo("JurorSurname62");
            assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
            assertThat(data.getStatus()).isEqualTo("Postponed");
            assertThat(data.getPostponedTo()).isEqualTo(LocalDate.now().plusDays(10));
            String reason = excusalCodeRepository.findById("P").orElse(new ExcusalCodeEntity()).getDescription();
            assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            assertThat(data.getDatePrinted()).isEqualTo(LocalDate.now().minusDays(9));
            assertThat(data.getPoolNumber()).isEqualTo("415220401");
        }

        @Test
        @SneakyThrows
        @DisplayName("Postponement letter criteria not met - bureau deferral")
        void courtLetterListPostponedJurorNumberSearchBureauDeferral() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber("555555569")
                .letterType(CourtLetterType.POSTPONED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as(RESPONSE_OK_MESSAGE)
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(0);
        }

        @Test
        @SneakyThrows
        @DisplayName("Postponement letter criteria met - juror forename")
        void courtLetterListPostponedJurorNameSearch() {
            final String jurorName = "JurorForename67";
            final String payload = createBureauJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorName(jurorName)
                .letterType(CourtLetterType.POSTPONED)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as(RESPONSE_OK_MESSAGE)
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(1);

            PostponeLetterData data = (PostponeLetterData) responseBody.get(0);
            assertThat(data.getJurorNumber()).isEqualTo("555555567");
            assertThat(data.getFirstName()).isEqualTo(jurorName);
            assertThat(data.getLastName()).isEqualTo("JurorSurname67");
            assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
            assertThat(data.getStatus()).isEqualTo("Postponed");
            assertThat(data.getPostponedTo()).isEqualTo(LocalDate.now().plusDays(13));
            String reason = excusalCodeRepository.findById("P").orElse(new ExcusalCodeEntity()).getDescription();
            assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            assertThat(data.getDatePrinted()).isNull();
            assertThat(data.getPoolNumber()).isEqualTo("415220401");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/court-letter-list (Show Cause)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ShowCause.sql"})
    class CourtLetterListShowCause {
        static final URI URL = URI.create(GET_LETTER_LIST_URI);
        static final String JUROR_5555555 = "5555555";
        static final String COURT_USER = "COURT_USER";
        static final String OWNER_415 = "415";
        static final String RESPONSE_OK_MESSAGE = "Expect HTTP Response to be OK";
        static final String RESPONSE_ENTITY_NOT_NULL_MESSAGE = "Response entity is not null";
        static final String RESPONSE_BODY_NOT_NULL_MESSAGE = "Response body is not null";
        static final String RESPONSE_DATA_SIZE_MESSAGE = "Response data contains %s record(s)";
        static final String DATE_PRINTED_IS_NULL_MESSAGE = "Date letter was printed should be null";

        static final String STATUS = "status";
        static final String MESSAGE = "message";
        static final String FIELD = "field";

        private static final String VALIDATION_MESSAGE_IS = "Validation message is: ";
        private static final String HTTP_STATUS_BAD_REQUEST_MESSAGE = "Expect the HTTP status to be BAD_REQUEST";
        static final String FIELDS_REQUIRED = "Field jurorNumber is required if none of the following fields are "
            + "present: [poolNumber, jurorName, jurorPostcode]";

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter - juror number and include printed letters")
        void courtLetterListShowCauseJurorNumberSearchIncludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "61")
                .includePrinted(true)
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();

            RequestEntity<CourtLetterListRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 1)).isEqualTo(1);

            FailedToAttendLetterData data = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("61", data, LocalDate.now().minusDays(10));

            assertThat(data.getDatePrinted())
                .as("Date letter was printed should be " + LocalDate.now().minusDays(1))
                .isEqualTo(LocalDate.now().minusDays(1));
        }

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter - juror number and exclude printed letters")
        void courtLetterListShowCauseJurorNumberSearchExcludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "63")
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();

            RequestEntity<CourtLetterListRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 1)).isEqualTo(1);

            FailedToAttendLetterData data = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("63", data, LocalDate.now().minusDays(10));
            assertThat(data.getDatePrinted()).as(DATE_PRINTED_IS_NULL_MESSAGE).isNull();
        }

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter - fails dto validation (mandatory fields missing)")
        void courtLetterListShowCauseIncludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();

            RequestEntity<CourtLetterListRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.get(STATUS)).isEqualTo(400);

            List<String> messageValues = getValuesInJsonObject(exceptionDetails, MESSAGE);
            List<String> fieldValues = getValuesInJsonObject(exceptionDetails, FIELD);
            assertThat(messageValues.get(0)).as(VALIDATION_MESSAGE_IS + FIELDS_REQUIRED)
                .isEqualTo(FIELDS_REQUIRED);
            assertThat(fieldValues.get(0)).as("Validated fields are jurorNumber")
                .isEqualTo("jurorNumber");
        }

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter - pool number and exclude printed letters")
        void courtLetterListShowCausePoolNumberSearchExcludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber("415220401")
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();

            RequestEntity<CourtLetterListRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 3)).isEqualTo(3);

            FailedToAttendLetterData dataIndex0 = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("63", dataIndex0, LocalDate.now().minusDays(10)); // no juror_history record exists
            assertThat(dataIndex0.getDatePrinted()).as(DATE_PRINTED_IS_NULL_MESSAGE).isNull();

            FailedToAttendLetterData dataIndex1 = (FailedToAttendLetterData) responseBody.get(1);
            verifyResponse("69", dataIndex1, LocalDate.now().minusDays(10));
            assertThat(dataIndex1.getDatePrinted()).as(DATE_PRINTED_IS_NULL_MESSAGE).isNull();

            FailedToAttendLetterData dataIndex2 = (FailedToAttendLetterData) responseBody.get(2);
            verifyResponse("70", dataIndex2, LocalDate.now().minusDays(10));
            assertThat(dataIndex2.getDatePrinted()).as(DATE_PRINTED_IS_NULL_MESSAGE).isNull();
        }

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter - pool number and include printed letters")
        void courtLetterListShowCausePoolNumber() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber("415220402")
                .includePrinted(true)
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);

            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 1)).isEqualTo(1);

            FailedToAttendLetterData data = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("65", data, LocalDate.now().minusDays(3));
        }

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter criteria not met - no_show is null (turned up for jury service")
        void courtLetterListShowCauseJurorNumberSearchAndNoShowIsNull() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "64")
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 0)).isEqualTo(0);
        }

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter criteria not met - bureau owner")
        void courtLetterListShowCauseBureauOwner() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "66")
                .letterType(CourtLetterType.SHOW_CAUSE)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 0)).isEqualTo(0);
        }

        void verifyResponse(String jurorPostfix, FailedToAttendLetterData data, LocalDate absentDate) {
            assertThat(data.getJurorNumber()).isEqualTo(JUROR_5555555 + jurorPostfix);
            assertThat(data.getFirstName()).as("First name is JurorForename" + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(data.getLastName()).as("Surname is JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);

            // date should have appeared in court (appearance.attendance_date) but was no show
            assertThat(data.getAbsentDate()).as("Absent date is " + absentDate).isEqualTo(absentDate);

            assertThat(data.getPostcode()).as("Postcode is null").isNull();
            assertThat(data.getStatus()).as("Status is null").isNull();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/moj/letter/court-letter-list/{letter_type}/{include_printed}")
    class CourtLetterListAbsentJurors {
        static final String JUROR_5555555 = "5555555";
        static final String COURT_USER = "COURT_USER";
        static final String OWNER_415 = "415";
        static final String RESPONSE_OK_MESSAGE = "Expect HTTP Response to be OK";
        static final String RESPONSE_ENTITY_NOT_NULL_MESSAGE = "Response entity is not null";
        static final String RESPONSE_BODY_NOT_NULL_MESSAGE = "Response body is not null";
        static final String RESPONSE_DATA_SIZE_MESSAGE = "Response data contains %s record(s)";

        static final LocalDate LOCAL_DATE_NOW = LocalDate.now();

        @Nested
        @DisplayName("Failed To Attend List")
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_FailedToAttend.sql"})
        class CourtLetterListFailedToAttend {
            static final URI URL = URI.create(GET_LETTER_LIST_URI + "/" + CourtLetterType.FAILED_TO_ATTEND);

            @Test
            @SneakyThrows
            @DisplayName("Failed To Attend List - include printed letters")
            void includePrinted() {
                List<?> responseBody = invokeApiHappy(true, OWNER_415, URL);
                assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 8)).isEqualTo(8);

                verifyResponse("67", (FailedToAttendLetterData) responseBody.get(0), null,
                    LOCAL_DATE_NOW.minusDays(5));
                verifyResponse("70", (FailedToAttendLetterData) responseBody.get(5),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW.minusDays(9));
                verifyResponse("63", (FailedToAttendLetterData) responseBody.get(1), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("69", (FailedToAttendLetterData) responseBody.get(2), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("70", (FailedToAttendLetterData) responseBody.get(7),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("62", (FailedToAttendLetterData) responseBody.get(3),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW);
                verifyResponse("65", (FailedToAttendLetterData) responseBody.get(4),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW.minusDays(3));
                verifyResponse("61", (FailedToAttendLetterData) responseBody.get(6),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW.minusDays(10));
            }

            @Test
            @SneakyThrows
            @DisplayName("Failed To Attend List - exclude printed letters")
            void excludePrinted() {
                List<?> responseBody = invokeApiHappy(false, OWNER_415, URL);
                assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 3)).isEqualTo(3);

                verifyResponse("67", (FailedToAttendLetterData) responseBody.get(0), null,
                    LOCAL_DATE_NOW.minusDays(5));
                verifyResponse("63", (FailedToAttendLetterData) responseBody.get(1), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("69", (FailedToAttendLetterData) responseBody.get(2), null,
                    LOCAL_DATE_NOW.minusDays(10));
            }

            @Test
            @SneakyThrows
            @DisplayName("Failed To Attend List - court owner 457")
            void courtOwner457() {
                List<?> responseBody = invokeApiHappy(true, "457", URL);
                assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 1)).isEqualTo(1);

                verifyResponse("68", (FailedToAttendLetterData) responseBody.get(0),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW);
            }
        }

        @Nested
        @DisplayName("Show Cause List")
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ShowCause.sql"})
        class CourtLetterListShowCause {

            static final URI URL = URI.create(GET_LETTER_LIST_URI + "/" + CourtLetterType.SHOW_CAUSE);

            @Test
            @SneakyThrows
            @DisplayName("Show Cause letter - include printed letters")
            void includePrinted() {
                List<?> responseBody = invokeApiHappy(true, OWNER_415, URL);
                assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 8)).isEqualTo(8);

                verifyResponse("72", (FailedToAttendLetterData) responseBody.get(0), null,
                    LOCAL_DATE_NOW.minusDays(6));
                verifyResponse("72", (FailedToAttendLetterData) responseBody.get(1), null,
                    LOCAL_DATE_NOW.minusDays(8));
                verifyResponse("63", (FailedToAttendLetterData) responseBody.get(2), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("69", (FailedToAttendLetterData) responseBody.get(3), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("70", (FailedToAttendLetterData) responseBody.get(4), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("62", (FailedToAttendLetterData) responseBody.get(5),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW);
                verifyResponse("65", (FailedToAttendLetterData) responseBody.get(6),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW.minusDays(3));
                verifyResponse("61", (FailedToAttendLetterData) responseBody.get(7),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW.minusDays(10));
            }

            @Test
            @SneakyThrows
            @DisplayName("Show Cause letter - exclude printed letters")
            void excludePrinted() {
                List<?> responseBody = invokeApiHappy(false, OWNER_415, URL);
                assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 5)).isEqualTo(5);

                verifyResponse("72", (FailedToAttendLetterData) responseBody.get(0), null,
                    LOCAL_DATE_NOW.minusDays(6));
                verifyResponse("72", (FailedToAttendLetterData) responseBody.get(1), null,
                    LOCAL_DATE_NOW.minusDays(8));
                verifyResponse("63", (FailedToAttendLetterData) responseBody.get(2), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("69", (FailedToAttendLetterData) responseBody.get(3), null,
                    LOCAL_DATE_NOW.minusDays(10));
                verifyResponse("70", (FailedToAttendLetterData) responseBody.get(4), null,
                    LOCAL_DATE_NOW.minusDays(10));
            }

            @Test
            @SneakyThrows
            @DisplayName("Show Cause letter - court owner 457")
            void courtOwner457() {
                List<?> responseBody = invokeApiHappy(true, "457", URL);
                assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 2)).isEqualTo(2);

                verifyResponse("68", (FailedToAttendLetterData) responseBody.get(0),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW);
                verifyResponse("71", (FailedToAttendLetterData) responseBody.get(1),
                    LOCAL_DATE_NOW.minusDays(1), LOCAL_DATE_NOW.minusDays(10));
            }
        }

        @Nested
        @DisplayName("Common Test Scenarios")
        @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ShowCause.sql"})
        class CourtLetterListCommonTests {
            static final URI URL = URI.create(GET_LETTER_LIST_URI + "/" + CourtLetterType.SHOW_CAUSE);

            @Test
            @SneakyThrows
            @DisplayName("Missing url path variable - includePrinted")
            void defaultIncludePrintedFlag() {
                final String payload = createBureauJwt(COURT_USER, OWNER_415);

                httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

                ResponseEntity<String> response =
                    template.exchange(new RequestEntity<Void>(httpHeaders, GET, URL), String.class);

                assertThat(response.getStatusCode()).as("Status code should be NOT_FOUND")
                    .isEqualTo(NOT_FOUND);
            }
        }

        List<?> invokeApiHappy(boolean includePrinted, String owner, URI url) {
            final String payload = createBureauJwt(COURT_USER, owner);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            ResponseEntity<LetterListResponseDto> response =
                template.exchange(new RequestEntity<Void>(httpHeaders, GET, URI.create(url + "/" + includePrinted)),
                    LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            return response.getBody().getData();
        }

        void verifyResponse(String jurorPostfix, FailedToAttendLetterData data, LocalDate datePrinted,
                            LocalDate absentDate) {
            assertThat(data.getJurorNumber()).isEqualTo(JUROR_5555555 + jurorPostfix);
            assertThat(data.getFirstName()).as("First name is JurorForename" + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(data.getLastName()).as("Surname is JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);

            // date juror should have attended for jury service (appearance.attendance_date) but was absent
            assertThat(data.getAbsentDate()).as("Absent date is " + absentDate).isEqualTo(absentDate);

            assertThat(data.getPostcode()).as("Postcode is null").isNull();
            assertThat(data.getStatus()).as("Status is null").isNull();

            assertThat(data.getDatePrinted()).as("Date letter was printed should be " + datePrinted)
                .isEqualTo(datePrinted);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/print-court-letter (Show Cause)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ShowCause.sql"})
    class PrintCourtLettersShowCauseSimpleDto {
        private static final String HTTP_STATUS_BAD_REQUEST_MESSAGE = "Expect the HTTP status to be BAD_REQUEST";
        static final URI URL = URI.create("/api/v1/moj/letter/print-court-letter");

        static final String JUROR_NUMBER = "5555555";
        static final String COURT_USER = "COURT_USER";
        static final String OWNER_415 = "415";

        static final String STATUS = "status";
        static final String MESSAGE = "message";
        static final String FIELD = "field";

        static final String MANDATORY_DATA_MISSING_FOR_LETTER_TYPE = "Mandatory data missing for letter type";
        static final String RESPONSE_OK_MESSAGE = "Expect HTTP Response to be OK";
        static final String RESPONSE_ENTITY_NOT_NULL_MESSAGE = "Response entity is not null";
        static final String RESPONSE_BODY_NOT_NULL_MESSAGE = "Response body is not null";
        static final String NUMBER_OF_LETTERS_MESSAGE = "Expect %s letters";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

        @Test
        @SneakyThrows
        @DisplayName("Reissue Show Cause letter - Happy path - English")
        void printCourtLettersShowCauseHappyEnglish() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER, OWNER_415));

            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter = new ArrayList<>();
            detailsPerLetter.add(createDetailsPerLetter("61", 10));
            detailsPerLetter.add(createDetailsPerLetter("62", 0));
            PrintLettersRequestDto request = buildPrintLettersRequestDto(detailsPerLetter);

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 2)).isEqualTo(2);

            verifyDataEnglish(data[0], request, LocalDate.now().minusDays(10), "61");
            verifyDataEnglish(data[1], request, LocalDate.now(), "62");
        }

        @Test
        @SneakyThrows
        @DisplayName("Reissue Show Cause letter - Happy path - Welsh")
        void printCourtLettersShowCauseHappyWelsh() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER, "457"));

            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter = new ArrayList<>();
            detailsPerLetter.add(createDetailsPerLetter("68", 0));
            PrintLettersRequestDto request = buildPrintLettersRequestDto(detailsPerLetter);

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 1)).isEqualTo(1);

            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            verifyDataWelsh(data[0], request, "68");
        }

        @Test
        @SneakyThrows
        @DisplayName("Reissue Show Cause letter - invalid request - missing mandatory data for letter type")
        void printCourtLettersShowCauseInvalidRequestMissingShowCauseDateAndTime() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER, OWNER_415));

            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR_NUMBER + "61");
            PrintLettersRequestDto request = PrintLettersRequestDto.builder()
                .jurorNumbers(jurorNumbers)
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();

            ResponseEntity<String> response =
                template.exchange(new RequestEntity<>(request, httpHeaders, POST, URL), String.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.get(STATUS)).isEqualTo(400);

            List<String> messageValues = getValuesInJsonObject(exceptionDetails, MESSAGE);
            List<String> fieldValues = getValuesInJsonObject(exceptionDetails, FIELD);
            assertThat(messageValues).as("Validation message is: " + MANDATORY_DATA_MISSING_FOR_LETTER_TYPE)
                .containsAnyOf(MANDATORY_DATA_MISSING_FOR_LETTER_TYPE);
            assertThat(fieldValues).as("Validated fields are showCauseTime, showCauseDate and "
                    + "detailsPerLetter")
                .containsExactlyInAnyOrder("showCauseTime", "showCauseDate", "detailsPerLetter");
        }

        @Test
        @SneakyThrows
        @DisplayName("Reissue Show Cause letter - invalid request - missing mandatory data: showCauseDate")
        void printCourtLettersShowCauseInvalidRequestMissingShowCauseDate() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("COURT_USER", "415"));

            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR_NUMBER + "61");
            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(jurorNumbers)
                .letterType(CourtLetterType.SHOW_CAUSE)
                .showCauseTime(LocalTime.now())
                .build();

            ResponseEntity<String> response =
                template.exchange(new RequestEntity<>(requestDto, httpHeaders, POST, URL), String.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.get(STATUS)).isEqualTo(400);

            List<String> messageValues = getValuesInJsonObject(exceptionDetails, MESSAGE);
            List<String> fieldValues = getValuesInJsonObject(exceptionDetails, FIELD);
            assertThat(messageValues).as("Validation message is: " + MANDATORY_DATA_MISSING_FOR_LETTER_TYPE)
                .containsAnyOf(MANDATORY_DATA_MISSING_FOR_LETTER_TYPE);
            assertThat(fieldValues).as("Validated fields is showCauseDate")
                .containsExactlyInAnyOrder("showCauseDate", "detailsPerLetter");
        }

        @Test
        @SneakyThrows
        @DisplayName("Reissue Show Cause letter - invalid request - missing mandatory data: showCauseTime")
        void printCourtLettersShowCauseInvalidRequestMissingShowCauseTime() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR_NUMBER + "61");

            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(jurorNumbers)
                .letterType(CourtLetterType.SHOW_CAUSE)
                .showCauseDate(LocalDate.now())
                .build();

            ResponseEntity<String> response =
                template.exchange(new RequestEntity<>(requestDto, httpHeaders, POST, URL), String.class);

            assertThat(response.getStatusCode()).as(HTTP_STATUS_BAD_REQUEST_MESSAGE).isEqualTo(BAD_REQUEST);

            JSONObject exceptionDetails = getExceptionDetails(response);
            assertThat(exceptionDetails.get(STATUS)).isEqualTo(400);

            List<String> messageValues = getValuesInJsonObject(exceptionDetails, MESSAGE);
            List<String> fieldValues = getValuesInJsonObject(exceptionDetails, FIELD);
            assertThat(messageValues).as("Validation message is: " + MANDATORY_DATA_MISSING_FOR_LETTER_TYPE)
                .containsAnyOf(MANDATORY_DATA_MISSING_FOR_LETTER_TYPE);
            assertThat(fieldValues).as("Validated fields are detailsPerLetter and showCauseTime ")
                .containsExactlyInAnyOrder("detailsPerLetter", "showCauseTime");
        }

        @Test
        @SneakyThrows
        @DisplayName("Show Cause letter - juror with multiple absences")
        void failedToAttendLetterJurorWithMultipleAbsences() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER, OWNER_415));

            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter = new ArrayList<>();
            detailsPerLetter.add(createDetailsPerLetter("72", 8));
            detailsPerLetter.add(createDetailsPerLetter("72", 6));
            PrintLettersRequestDto request = buildPrintLettersRequestDto(detailsPerLetter);

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 2)).isEqualTo(2);

            verifyDataEnglish(data[0], request, LocalDate.now().minusDays(8), "72");
            verifyDataEnglish(data[1], request, LocalDate.now().minusDays(6), "72");
        }

        private PrintLettersRequestDto.DetailsPerLetter createDetailsPerLetter(String jurorPostfix, int adjustDays) {
            PrintLettersRequestDto.DetailsPerLetter detailsPerLetter = new PrintLettersRequestDto.DetailsPerLetter();
            detailsPerLetter.setJurorNumber(JUROR_NUMBER + jurorPostfix);
            detailsPerLetter.setLetterDate(LocalDate.now().minusDays(adjustDays));

            return detailsPerLetter;
        }

        private PrintLettersRequestDto buildPrintLettersRequestDto(
            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter) {

            return PrintLettersRequestDto.builder()
                .showCauseDate(LocalDate.now())
                .showCauseTime(LocalTime.parse(LocalTime.now().format(formatter)))
                .detailsPerLetter(detailsPerLetter)
                .letterType(CourtLetterType.SHOW_CAUSE)
                .build();
        }

        private void verifyDataEnglish(PrintLetterDataResponseDto response,
                                       PrintLettersRequestDto request,
                                       LocalDate attendanceDate,
                                       String jurorPostfix) {
            assertThat(response.getCourtName())
                .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(response.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(response.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(response.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(response.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(response.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(response.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(response.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(response.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(response.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(response.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(response.getJurorFirstName()).as("Expect first name to be Juror " + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(response.getJurorLastName()).as("Expect last name to be JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);
            assertThat(response.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(response.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(response.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(response.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(response.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(response.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(response.getJurorNumber()).as("Expect juror number to be 5555555" + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);

            assertThat(response.getAttendanceDate())
                .as("Expect the attendance date the juror was no show for to be " + attendanceDate)
                .isEqualTo(attendanceDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            assertThat(response.getNoShowDate())
                .as("Expect show cause date to be " + request.getShowCauseDate())
                .isEqualTo(request.getShowCauseDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            assertThat(response.getNoShowTime())
                .as("Expect show cause time to be " + request.getShowCauseTime().format(formatter))
                .isEqualTo(request.getShowCauseTime());
        }

        private void verifyDataWelsh(PrintLetterDataResponseDto response,
                                     PrintLettersRequestDto request,
                                     String jurorPostfix) {
            assertThat(response.getCourtName()).as("Expect court name to be " + "Llys y Goron\nynAbertawe")
                .isEqualTo("Llys y Goron\nynAbertawe");
            assertThat(response.getCourtAddressLine1()).as("Expect address line 1 to be 'Y LLYSOEDD BARN'")
                .isEqualTo("Y LLYSOEDD BARN");
            assertThat(response.getCourtAddressLine2()).as("Expect address line 2 to be 'LON SAN HELEN'")
                .isEqualTo("LON SAN HELEN");
            assertThat(response.getCourtAddressLine3()).as("Expect address line 3 to be ABERTAWE")
                .isEqualTo("ABERTAWE");
            assertThat(response.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(response.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(response.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(response.getCourtPostCode()).as("Expect post code to be 'SA1 4PF'").isEqualTo("SA1 4PF");
            assertThat(response.getCourtPhoneNumber()).as("Expect court number to be 01792 637067")
                .isEqualTo("01792 637067");

            assertThat(response.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(response.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nSwyddog Llys");

            assertThat(response.getJurorFirstName())
                .as("Expect first name to be JurorForename" + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(response.getJurorLastName())
                .as("Expect last name to be JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);
            assertThat(response.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(response.getJurorAddressLine2()).as("Expect address line 2 to be Address Line 2")
                .isEqualTo("Address Line 2");
            assertThat(response.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(response.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(response.getJurorAddressLine5()).as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(response.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(response.getJurorNumber())
                .as("Expect juror number to be " + JUROR_NUMBER + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);
            assertThat(response.getNoShowTime()).as("Expect no show time to be " + request.getShowCauseTime())
                .isEqualTo(request.getShowCauseTime());
            assertThat(response.getWelsh()).as("Expect welsh to be true").isTrue();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/print-court-letter (Excusal granted)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalGranted.sql"})
    class PrintCourtLettersExcusalGranted {
        static final String JUROR_NUMBER = "5555555";
        static final URI URL = URI.create("/api/v1/moj/letter/print-court-letter");

        @Test
        @SneakyThrows
        @DisplayName("Reissue Excusal Granted Letter - Happy path - English")
        void reissueExcusalGrantedLetterHappy() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR_NUMBER + "62");

            final String payload = createBureauJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(jurorNumbers)
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .build();

            RequestEntity<PrintLettersRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                PrintLetterDataResponseDto[].class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as("Expect 1 letter").isEqualTo(1);

            verifyDataEnglish(data[0], "62");

        }

        private void verifyDataEnglish(PrintLetterDataResponseDto dto, String jurorPostfix) {
            assertThat(dto.getCourtName())
                .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(dto.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(dto.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(dto.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(dto.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(dto.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(dto.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(dto.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(dto.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(dto.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(dto.getJurorFirstName()).as("Expect first name to be FNAME2")
                .isEqualTo("FNAME2");
            assertThat(dto.getJurorLastName()).as("Expect last name to be LNAME2")
                .isEqualTo("LNAME2");
            assertThat(dto.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(dto.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(dto.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line3");
            assertThat(dto.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("Some Town");
            assertThat(dto.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(dto.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getJurorNumber()).as("Expect juror number to be 5555555" + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/print-court-letter (Withdrawal)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_Withdrawal.sql"})
    class PrintCourtLettersWithdrawal {
        static final String JUROR_NUMBER = "5555555";
        static final URI URL = URI.create("/api/v1/moj/letter/print-court-letter");

        @Test
        @SneakyThrows
        @DisplayName("Reissue Excusal Granted Letter - Happy path - English")
        void reissueWithdrawalLetterHappy() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR_NUMBER + "55");

            final String payload = createBureauJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(jurorNumbers)
                .letterType(CourtLetterType.WITHDRAWAL)
                .build();

            RequestEntity<PrintLettersRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                PrintLetterDataResponseDto[].class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as("Expect 1 letter").isEqualTo(1);

            verifyDataEnglish(data[0], "55");

        }

        private void verifyDataEnglish(PrintLetterDataResponseDto dto, String jurorPostfix) {
            assertThat(dto.getCourtName())
                .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(dto.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(dto.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(dto.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(dto.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(dto.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(dto.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(dto.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(dto.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(dto.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(dto.getJurorFirstName()).as("Expect first name to be FNAME1")
                .isEqualTo("FNAME1");
            assertThat(dto.getJurorLastName()).as("Expect last name to be LNAME1")
                .isEqualTo("LNAME1");
            assertThat(dto.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(dto.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(dto.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(dto.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("Some Town");
            assertThat(dto.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(dto.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getJurorNumber()).as("Expect juror number to be 5555555" + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);
        }
    }

    @Nested
    @DisplayName("POST - /api/v1/moj/letter/print-court-letter (Excusal Refusal)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_ExcusalDenied.sql"})
    class PrintCourtLetterExcusalDenied {
        static final String JUROR_NUMBER = "123456789";
        static final String WELSH_JUROR_NUMBER = "987654321";
        static final URI URL = URI.create("/api/v1/moj/letter/print-court-letter");

        @Test
        @SneakyThrows
        @DisplayName("Reissue Excusal Denied Letter - Happy path - English")
        void printCourtLetterExcusalDeniedHappyPath() {
            final String payload = createBureauJwt("COURT_USER", "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(Collections.singletonList(JUROR_NUMBER))
                .letterType(CourtLetterType.EXCUSAL_REFUSED)
                .build();

            RequestEntity<PrintLettersRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                PrintLetterDataResponseDto[].class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as("Expect 1 letter").isEqualTo(1);

            verifyEnglishLetter(data[0]);
        }

        @Test
        @SneakyThrows
        @DisplayName("Reissue Excusal Denied Letter - Happy path - Welsh")
        void printCourtLetterExcusalDeniedHappyPathWelsh() {
            final String payload = createBureauJwt("COURT_USER", "457");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(Collections.singletonList(WELSH_JUROR_NUMBER))
                .letterType(CourtLetterType.EXCUSAL_REFUSED)
                .build();

            RequestEntity<PrintLettersRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(request,
                PrintLetterDataResponseDto[].class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as("Expect HTTP Response to be OK - endpoint is permitted for Court Users only")
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as("Expect 1 letter").isEqualTo(1);

            verifyWelshLetter(data[0]);
        }

        private void verifyEnglishLetter(PrintLetterDataResponseDto dto) {
            assertThat(dto.getCourtName())
                .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(dto.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(dto.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(dto.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(dto.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(dto.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(dto.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(dto.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(dto.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(dto.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(dto.getJurorFirstName()).as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getJurorLastName()).as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
            assertThat(dto.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(dto.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(dto.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(dto.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(dto.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(dto.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(dto.getJurorNumber()).as("Expect juror number to be 123456789")
                .isEqualTo(JUROR_NUMBER);
            assertThat(dto.getCourtManager()).as("Expect the court manager to not be null").isNotNull();
            assertThat(dto.getCourtManager())
                .as("Expect the court manager to be The Court Manager")
                .isEqualTo("The Court Manager");
        }

        private void verifyWelshLetter(PrintLetterDataResponseDto dto) {
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
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getJurorLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
            assertThat(dto.getJurorAddressLine1())
                .as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(dto.getJurorAddressLine2())
                .as("Expect address line 2 to be Address Line 2")
                .isEqualTo("Address Line 2");
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
                .as("Expect juror number to be " + WELSH_JUROR_NUMBER)
                .isEqualTo(WELSH_JUROR_NUMBER);
            assertThat(dto.getWelsh())
                .as("Expect welsh to be true")
                .isTrue();
            assertThat(dto.getCourtManager())
                .as("Expect court manager to be Y Rheolwr Llys")
                .isEqualTo("Y Rheolwr Llys");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/letter/court-letter-list (Failed To Attend, aka No Show)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_FailedToAttend.sql"})
    class CourtLetterListFailedToAttend {
        static final URI URL = URI.create(GET_LETTER_LIST_URI);
        static final String JUROR_5555555 = "5555555";
        static final String COURT_USER = "COURT_USER";
        static final String OWNER_415 = "415";
        static final String RESPONSE_OK_MESSAGE = "Expect HTTP Response to be OK";
        static final String RESPONSE_ENTITY_NOT_NULL_MESSAGE = "Response entity is not null";
        static final String RESPONSE_BODY_NOT_NULL_MESSAGE = "Response body is not null";
        static final String RESPONSE_DATA_SIZE_MESSAGE = "Response data contains %s record(s)";
        static final String DATE_PRINTED_IS_NULL_MESSAGE = "Date letter was printed should be null";

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend list - juror number and include printed letters")
        void courtLetterListFailedToAttendJurorNumberSearchIncludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "61")
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode())
                .as(RESPONSE_OK_MESSAGE)
                .isEqualTo(OK);
            assertThat(response.getBody()).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).isEqualTo(1);

            FailedToAttendLetterData data = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("61", data,
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(10));

        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend list - juror number and exclude printed letters")
        void courtLetterListFailedToAttendJurorNumberSearchExcludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "63")
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .build();

            RequestEntity<CourtLetterListRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 1)).isEqualTo(1);

            FailedToAttendLetterData data = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("63", data, null, LocalDate.now().minusDays(10));
        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend list - juror number and has multiple absences")
        void courtLetterListFailedToAttendJurorNumberHasMultipleAbsences() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "70")
                .includePrinted(true)
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .build();

            RequestEntity<CourtLetterListRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 2)).isEqualTo(2);

            verifyResponse("70", (FailedToAttendLetterData) responseBody.get(0),
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(9));
            verifyResponse("70", (FailedToAttendLetterData) responseBody.get(1),
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(10));
        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend list - pool number and exclude printed letters")
        void courtLetterListFailedToAttendPoolNumberSearchExcludePrinted() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber("415220401")
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .build();

            RequestEntity<CourtLetterListRequestDto> request = new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 3)).isEqualTo(3);

            FailedToAttendLetterData dataIndex0 = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("67", dataIndex0, null, LocalDate.now().minusDays(5));
            assertThat(dataIndex0.getDatePrinted()).as(DATE_PRINTED_IS_NULL_MESSAGE).isNull();

            FailedToAttendLetterData dataIndex1 = (FailedToAttendLetterData) responseBody.get(1);
            verifyResponse("63", dataIndex1, null,
                LocalDate.now().minusDays(10));

            FailedToAttendLetterData dataIndex2 = (FailedToAttendLetterData) responseBody.get(2);
            verifyResponse("69", dataIndex2, null,
                LocalDate.now().minusDays(10));
        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend list - pool number and include printed letters")
        void courtLetterListFailedToAttendPoolNumber() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .poolNumber("415220402")
                .includePrinted(true)
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);

            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 1)).isEqualTo(1);

            FailedToAttendLetterData data = (FailedToAttendLetterData) responseBody.get(0);
            verifyResponse("65", data,
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(3));
        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend list - no_show is null (turned up for jury service")
        void courtLetterListFailedToAttendJurorNumberSearchAndNoShowIsNull() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "64")
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 0)).isEqualTo(0);
        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend list criteria not met - bureau owner")
        void courtLetterListFailedToAttendBureauOwner() {
            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto
                .builder()
                .jurorNumber(JUROR_5555555 + "66")
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .includePrinted(true)
                .build();

            RequestEntity<CourtLetterListRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, URL);
            ResponseEntity<LetterListResponseDto> response = template.exchange(request, LetterListResponseDto.class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();
            List<?> responseBody = response.getBody().getData();
            assertThat(responseBody.size()).as(String.format(RESPONSE_DATA_SIZE_MESSAGE, 0)).isEqualTo(0);
        }

        void verifyResponse(String jurorPostfix, FailedToAttendLetterData data, LocalDate dateLetterPrinted,
                            LocalDate absentDate) {
            assertThat(data.getJurorNumber()).isEqualTo(JUROR_5555555 + jurorPostfix);
            assertThat(data.getFirstName()).as("First name is JurorForename" + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(data.getLastName()).as("Surname is JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);

            assertThat(data.getPostcode()).as("Postcode is null").isNull();
            assertThat(data.getStatus()).as("Status is null").isNull();

            assertThat(data.getDatePrinted())
                .as("Date letter was printed should be " + dateLetterPrinted).isEqualTo(dateLetterPrinted);

            // date juror should have attended for jury service (appearance.attendance_date) but was no show
            assertThat(data.getAbsentDate()).as("Absent date is " + absentDate).isEqualTo(absentDate);
        }
    }

    @Nested
    @DisplayName("Certificate of Attendance")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_CertificateOfAttendance.sql"})
    class CertificateOfAttendance {

        static final URI uri = URI.create("/api/v1/moj/letter/print-court-letter");

        static final String JUROR_NUMBER = "5555555";
        static final String COURT_USER = "COURT_USER";
        static final String OWNER_415 = "415";

        static final String RESPONSE_OK_MESSAGE = "Expect HTTP Response to be OK";
        static final String RESPONSE_ENTITY_NOT_NULL_MESSAGE = "Response entity is not null";
        static final String RESPONSE_BODY_NOT_NULL_MESSAGE = "Response body is not null";
        static final String NUMBER_OF_LETTERS_MESSAGE = "Expect %s letters";

        @Test
        @SneakyThrows
        @DisplayName("Issue Certificate of Attendance Letter - Happy path - English")
        void englishLetter() {
            String jurorNumber = "555555561";

            final String payload = createBureauJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto request = PrintLettersRequestDto.builder()
                .jurorNumbers(Collections.singletonList(jurorNumber))
                .letterType(CourtLetterType.CERTIFICATE_OF_ATTENDANCE)
                .build();

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, uri);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 1)).isEqualTo(1);

            verifyDataEnglish(data[0], LocalDate.now().minusDays(10), "61");
        }

        @Test
        @SneakyThrows
        @DisplayName("Issue Certificate of Attendance Letter - Happy path - Welsh")
        void welshLetter() {
            String jurorNumber = "555555562";

            final String payload = createBureauJwt(COURT_USER, "457");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto request = PrintLettersRequestDto.builder()
                .jurorNumbers(Collections.singletonList(jurorNumber))
                .letterType(CourtLetterType.CERTIFICATE_OF_ATTENDANCE)
                .build();

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, uri);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 1)).isEqualTo(1);

            verifyDataWelsh(data[0], "62");
        }

        @Test
        @SneakyThrows
        @DisplayName("Issue Certificate of Attendance Letter - Invalid Owner - English")
        void englishLetterInvalidOwner() {
            final String jurorNumber = "555555561";
            final String bureauOwner = "400";
            final String payload = createBureauJwt("BUREAU_USER", bureauOwner);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, payload);

            PrintLettersRequestDto requestDto = PrintLettersRequestDto.builder()
                .jurorNumbers(List.of(jurorNumber))
                .letterType(CourtLetterType.CERTIFICATE_OF_ATTENDANCE)
                .build();

            RequestEntity<PrintLettersRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<String> response = template.exchange(request,
                String.class);

            assertForbiddenResponse(response, String.valueOf(uri));
        }


        void verifyDataEnglish(PrintLetterDataResponseDto response,
                               LocalDate attendanceDate,
                               String jurorPostfix) {
            assertThat(response.getCourtName())
                .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(response.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(response.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(response.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(response.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(response.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(response.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(response.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(response.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(response.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(response.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(response.getJurorFirstName()).as("Expect first name to be Juror " + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(response.getJurorLastName()).as("Expect last name to be JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);
            assertThat(response.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(response.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(response.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(response.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(response.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(response.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(response.getJurorNumber()).as("Expect juror number to be 5555555" + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);

            assertThat(response.getNonAttendance())
                .as("Expect non attendance to be Non Attendance")
                .isEqualTo("Non Attendance");
            assertThat(response.getLossOfEarnings())
                .as("Expect loss of earnings to be 40.00")
                .isEqualTo(new BigDecimal("40.00"));
            assertThat(response.getChildCare())
                .as("Expect child care to be 10.00")
                .isEqualTo(new BigDecimal("10.00"));
            assertThat(response.getMisc())
                .as("Expect misc to be 10.00")
                .isEqualTo(new BigDecimal("10.00"));

        }

        void verifyDataWelsh(PrintLetterDataResponseDto response,
                             String jurorPostfix) {
            assertThat(response.getCourtName()).as("Expect court name to be " + "Llys y Goron\nynAbertawe")
                .isEqualTo("Llys y Goron\nynAbertawe");
            assertThat(response.getCourtAddressLine1()).as("Expect address line 1 to be 'Y LLYSOEDD BARN'")
                .isEqualTo("Y LLYSOEDD BARN");
            assertThat(response.getCourtAddressLine2()).as("Expect address line 2 to be 'LON SAN HELEN'")
                .isEqualTo("LON SAN HELEN");
            assertThat(response.getCourtAddressLine3()).as("Expect address line 3 to be ABERTAWE")
                .isEqualTo("ABERTAWE");
            assertThat(response.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(response.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(response.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(response.getCourtPostCode()).as("Expect post code to be 'SA1 4PF'").isEqualTo("SA1 4PF");
            assertThat(response.getCourtPhoneNumber()).as("Expect court number to be 01792 637067")
                .isEqualTo("01792 637067");

            assertThat(response.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(response.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nSwyddog Llys");

            assertThat(response.getJurorFirstName())
                .as("Expect first name to be JurorForename" + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(response.getJurorLastName())
                .as("Expect last name to be JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);
            assertThat(response.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(response.getJurorAddressLine2()).as("Expect address line 2 to be Address Line 2")
                .isEqualTo("Address Line 2");
            assertThat(response.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(response.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(response.getJurorAddressLine5()).as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(response.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(response.getJurorNumber())
                .as("Expect juror number to be " + JUROR_NUMBER + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);

            assertThat(response.getWelsh()).as("Expect welsh to be true").isTrue();
        }

    }

    @Nested
    @DisplayName("POST - /api/v1/moj/letter/print-court-letter (Failed To Attend)")
    @Sql({"/db/mod/truncate.sql", "/db/letter/CourtLetterList_FailedToAttend.sql"})
    class PrintCourtLetterFailedToAttend {
        static final URI URL = URI.create("/api/v1/moj/letter/print-court-letter");

        static final String JUROR_NUMBER = "5555555";
        static final String COURT_USER = "COURT_USER";
        static final String OWNER_415 = "415";

        static final String RESPONSE_OK_MESSAGE = "Expect HTTP Response to be OK";
        static final String RESPONSE_ENTITY_NOT_NULL_MESSAGE = "Response entity is not null";
        static final String RESPONSE_BODY_NOT_NULL_MESSAGE = "Response body is not null";
        static final String NUMBER_OF_LETTERS_MESSAGE = "Expect %s letters";

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend letter - Happy path - English")
        void failedToAttendLetterHappyEnglish() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER, OWNER_415));

            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter = new ArrayList<>();
            detailsPerLetter.add(createDetailsPerLetter("61", 10));
            detailsPerLetter.add(createDetailsPerLetter("62", 0));
            PrintLettersRequestDto request = buildPrintLettersRequestDto(detailsPerLetter);

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 2)).isEqualTo(2);

            verifyDataEnglish(data[0], LocalDate.now().minusDays(10), "61");
            verifyDataEnglish(data[1], LocalDate.now(), "62");
        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend letter - juror with multiple absences")
        void failedToAttendLetterJurorWithMultipleAbsences() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER, OWNER_415));

            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter = new ArrayList<>();
            detailsPerLetter.add(createDetailsPerLetter("70", 9));
            detailsPerLetter.add(createDetailsPerLetter("70", 10));
            PrintLettersRequestDto request = buildPrintLettersRequestDto(detailsPerLetter);

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 2)).isEqualTo(2);

            verifyDataEnglish(data[0], LocalDate.now().minusDays(9), "70");
            verifyDataEnglish(data[1], LocalDate.now().minusDays(10), "70");
        }

        @Test
        @SneakyThrows
        @DisplayName("Failed To Attend letter - Happy path - Welsh")
        void failedToAttendLetterHappyWelsh() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER, "457"));

            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter = new ArrayList<>();
            detailsPerLetter.add(createDetailsPerLetter("68", 0));
            PrintLettersRequestDto request = buildPrintLettersRequestDto(detailsPerLetter);

            RequestEntity<PrintLettersRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders, POST, URL);
            ResponseEntity<PrintLetterDataResponseDto[]> response = template.exchange(requestEntity,
                PrintLetterDataResponseDto[].class);

            assertThat(response).as(RESPONSE_ENTITY_NOT_NULL_MESSAGE).isNotNull();
            assertThat(response.getStatusCode()).as(RESPONSE_OK_MESSAGE).isEqualTo(OK);
            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            PrintLetterDataResponseDto[] data = response.getBody();
            assertThat(data.length).as(String.format(NUMBER_OF_LETTERS_MESSAGE, 1)).isEqualTo(1);

            assertThat(response.getBody()).as(RESPONSE_BODY_NOT_NULL_MESSAGE).isNotNull();

            verifyDataWelsh(data[0], "68");
        }

        private PrintLettersRequestDto.DetailsPerLetter createDetailsPerLetter(String jurorPostfix, int adjustDays) {
            PrintLettersRequestDto.DetailsPerLetter detailsPerLetter = new PrintLettersRequestDto.DetailsPerLetter();
            detailsPerLetter.setJurorNumber(JUROR_NUMBER + jurorPostfix);
            detailsPerLetter.setLetterDate(LocalDate.now().minusDays(adjustDays));

            return detailsPerLetter;
        }

        private PrintLettersRequestDto buildPrintLettersRequestDto(
            List<PrintLettersRequestDto.DetailsPerLetter> detailsPerLetter) {

            return PrintLettersRequestDto.builder()
                .detailsPerLetter(detailsPerLetter)
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .build();
        }

        private void verifyDataEnglish(PrintLetterDataResponseDto response,
                                       LocalDate attendanceDate,
                                       String jurorPostfix) {
            assertThat(response.getCourtName())
                .as("Expect court name to be " + "The Crown Court\nat CHESTER")
                .isEqualTo("The Crown Court\nat CHESTER");
            assertThat(response.getCourtAddressLine1()).as("Expect address line 1 to be 'THE CASTLE'")
                .isEqualTo("THE CASTLE");
            assertThat(response.getCourtAddressLine2()).as("Expect address line 2 to be 'CHESTER'")
                .isEqualTo("CHESTER");
            assertThat(response.getCourtAddressLine3()).as("Expect address line 3 to be null").isNull();
            assertThat(response.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(response.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(response.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(response.getCourtPostCode()).as("Expect post code to be 'CH1 2AN'")
                .isEqualTo("CH1 2AN");
            assertThat(response.getCourtPhoneNumber()).as("Expect court number to be 01244 356726")
                .isEqualTo("01244 356726");
            assertThat(response.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(response.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nAn Officer of the Crown Court");
            assertThat(response.getJurorFirstName()).as("Expect first name to be Juror " + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(response.getJurorLastName()).as("Expect last name to be JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);
            assertThat(response.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(response.getJurorAddressLine2()).as("Expect address line 2 to be Address  Line 2")
                .isEqualTo("Address Line 2");
            assertThat(response.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(response.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(response.getJurorAddressLine5())
                .as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(response.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(response.getJurorNumber()).as("Expect juror number to be 5555555" + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);

            assertThat(response.getAttendanceDate())
                .as("Expect the attendance date the juror was no show for to be " + attendanceDate)
                .isEqualTo(attendanceDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            assertThat(response.getReplyByDate())
                .as("Expect reply by date to be " + LocalDate.now()
                    .plusDays(7)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .isEqualTo(LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        private void verifyDataWelsh(PrintLetterDataResponseDto response,
                                     String jurorPostfix) {
            assertThat(response.getCourtName()).as("Expect court name to be " + "Llys y Goron\nynAbertawe")
                .isEqualTo("Llys y Goron\nynAbertawe");
            assertThat(response.getCourtAddressLine1()).as("Expect address line 1 to be 'Y LLYSOEDD BARN'")
                .isEqualTo("Y LLYSOEDD BARN");
            assertThat(response.getCourtAddressLine2()).as("Expect address line 2 to be 'LON SAN HELEN'")
                .isEqualTo("LON SAN HELEN");
            assertThat(response.getCourtAddressLine3()).as("Expect address line 3 to be ABERTAWE")
                .isEqualTo("ABERTAWE");
            assertThat(response.getCourtAddressLine4()).as("Expect address line 4 to be null").isNull();
            assertThat(response.getCourtAddressLine5()).as("Expect address line 5 to be null").isNull();
            assertThat(response.getCourtAddressLine6()).as("Expect address line 6 to be null").isNull();
            assertThat(response.getCourtPostCode()).as("Expect post code to be 'SA1 4PF'").isEqualTo("SA1 4PF");
            assertThat(response.getCourtPhoneNumber()).as("Expect court number to be 01792 637067")
                .isEqualTo("01792 637067");

            assertThat(response.getUrl()).as("Expect URL to be www.gov.uk/jury-service")
                .isEqualTo("www.gov.uk/jury-service");
            assertThat(response.getSignature()).as("Expect signatory to be Jury Manager")
                .isEqualTo("Jury Manager\n\nSwyddog Llys");

            assertThat(response.getJurorFirstName())
                .as("Expect first name to be JurorForename" + jurorPostfix)
                .isEqualTo("JurorForename" + jurorPostfix);
            assertThat(response.getJurorLastName())
                .as("Expect last name to be JurorSurname" + jurorPostfix)
                .isEqualTo("JurorSurname" + jurorPostfix);
            assertThat(response.getJurorAddressLine1()).as("Expect address line 1 to be Address Line 1")
                .isEqualTo("Address Line 1");
            assertThat(response.getJurorAddressLine2()).as("Expect address line 2 to be Address Line 2")
                .isEqualTo("Address Line 2");
            assertThat(response.getJurorAddressLine3()).as("Expect address line 3 to be Address Line 3")
                .isEqualTo("Address Line 3");
            assertThat(response.getJurorAddressLine4()).as("Expect address line 4 to be CARDIFF")
                .isEqualTo("CARDIFF");
            assertThat(response.getJurorAddressLine5()).as("Expect address line 5 to be Some County")
                .isEqualTo("Some County");
            assertThat(response.getJurorPostcode()).as("Expect post code to be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(response.getJurorNumber())
                .as("Expect juror number to be " + JUROR_NUMBER + jurorPostfix)
                .isEqualTo(JUROR_NUMBER + jurorPostfix);

            assertThat(response.getWelsh()).as("Expect welsh to be true").isTrue();
        }
    }
}

