package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.DeferralDecision;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the API endpoints defined in {@link ExcusalResponseController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.TooManyMethods")
public class DeferralRequestControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JurorPaperResponseRepositoryMod paperResponseRepository;
    @Autowired
    private JurorRepository jurorRepository;

    private HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        initHeaders();

    }

    private void initHeaders() throws Exception {
        final String bureauJwt = createJwtBureau("BUREAU_USER");

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void deny_Deferral_happyPath_bureauUser() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createDeferralDecisionDto(jurorNumber, deferralReason);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void deny_Deferral_unhappyPath_deferred_before() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createDeferralDecisionDto(jurorNumber, deferralReason);
        requestDto.setAllowMultipleDeferrals(false);
        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void deny_Deferral_happyPath_courtUser() {
        String jurorNumber = "123456789";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createDeferralDecisionDto(jurorNumber, deferralReason);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtCourt("COURT_USER", Set.of(), "415"));

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void refuseDeferralRequest_bureauUser_courtOwner() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createDeferralDecisionDto(jurorNumber, deferralReason);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtCourt("COURT_USER", Set.of(), "415"));
        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void refuseDeferralRequest_courtUser_bureauOwner() {
        String jurorNumber = "123456789";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createDeferralDecisionDto(jurorNumber, deferralReason);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void refuseDeferralRequest_jurorRecordNotFound() {
        String jurorNumber = "945209589";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createDeferralDecisionDto(jurorNumber, deferralReason);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void grant_Deferral_happyPath_bureauUser() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createGrantDeferralDecisionDto(jurorNumber, deferralReason);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createPoolAndResponse.sql"})
    public void grantDeferralHappyPathBureauUserOpenPaperResponseNoDob() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createGrantDeferralDecisionDto(jurorNumber, deferralReason);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);

        // check if paper response record has been closed
        PaperResponse paperResponse = paperResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(paperResponse).isNotNull()
            .as("Expect the paper response to be closed")
            .extracting(PaperResponse::getProcessingStatus)
            .isEqualTo(ProcessingStatus.CLOSED);

        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        assertThat(juror).isNotNull()
            .as("Expect the juror to keep the DOB in juror record")
            .extracting(Juror::getDateOfBirth)
            .isEqualTo(LocalDate.of(1989, 3, 31));

    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createPoolAndResponse.sql"})
    @Sql(statements = "update juror_mod.juror set dob = null where juror_number = '987654321'")
    @Sql(statements = "update juror_mod.juror_response set date_of_birth = '2020-05-17' "
        + "where juror_number = '987654321'")
    public void grantDeferralHappyPathBureauUserOpenPaperResponseWithDob() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createGrantDeferralDecisionDto(jurorNumber, deferralReason);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);

        // check if paper response record has been closed
        PaperResponse paperResponse = paperResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(paperResponse).isNotNull()
            .as("Expect the paper response to be closed")
            .extracting(PaperResponse::getProcessingStatus)
            .isEqualTo(ProcessingStatus.CLOSED);

        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        assertThat(juror).isNotNull()
            .as("Expect the juror to have updated DOB in juror record")
            .extracting(Juror::getDateOfBirth)
            .isEqualTo(LocalDate.of(2020, 5, 17));

    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createPoolAndResponse.sql"})
    @Sql(statements = "update juror_mod.juror_response set date_of_birth = '2020-05-17' "
        + "where juror_number = '987654321'")
    public void grantDeferralHappyPathBureauUserOpenPaperResponseDobNotOverwritten() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createGrantDeferralDecisionDto(jurorNumber, deferralReason);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);

        // check if paper response record has been closed
        PaperResponse paperResponse = paperResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(paperResponse).isNotNull()
            .as("Expect the paper response to be closed")
            .extracting(PaperResponse::getProcessingStatus)
            .isEqualTo(ProcessingStatus.CLOSED);

        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        assertThat(juror).isNotNull()
            .as("Expect the juror to keep DOB in juror record")
            .extracting(Juror::getDateOfBirth)
            .isEqualTo(LocalDate.of(1989, 3, 31));

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void grant_Deferral_happyPath_courtUser() {
        String jurorNumber = "123456789";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createDeferralDecisionDto(jurorNumber, deferralReason);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtCourt("COURT_USER", Set.of(), "415"));

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void grant_Deferral_unhappyPath_deferred_before() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createGrantDeferralDecisionDto(jurorNumber, deferralReason);
        requestDto.setAllowMultipleDeferrals(false);
        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void grant_Deferral_unhappyPath_appearances_exist() {
        String jurorNumber = "222222222";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createGrantDeferralDecisionDto(jurorNumber, deferralReason);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtCourt("COURT_USER", Set.of(), "415"));

        requestDto.setAllowMultipleDeferrals(false);
        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                                      URI.create("/api/v1/moj/deferral-response/juror/"
                                                                     + jurorNumber)),
                                  DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralRequestController_createInitialPoolRecords.sql"})
    public void grant_Deferral_Unhappy_MissingDate() {
        String jurorNumber = "987654321";
        String deferralReason = "B";

        DeferralRequestDto requestDto = createGrantDeferralDecisionDto(jurorNumber, deferralReason);
        requestDto.setDeferralDate(null);

        ResponseEntity<DeferralRequestDto> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/deferral-response/juror/" + jurorNumber)), DeferralRequestDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be BAD_REQUEST")
            .isEqualTo(HttpStatus.BAD_REQUEST);

    }

    private DeferralRequestDto createDeferralDecisionDto(String jurorNumber,
                                                         String deferralReason) {
        DeferralRequestDto deferralRequestDto = new DeferralRequestDto();
        deferralRequestDto.setJurorNumber(jurorNumber);
        deferralRequestDto.setDeferralReason(deferralReason);
        deferralRequestDto.setDeferralDecision(DeferralDecision.REFUSE);
        deferralRequestDto.setAllowMultipleDeferrals(true);
        return deferralRequestDto;
    }

    private DeferralRequestDto createGrantDeferralDecisionDto(String jurorNumber, String deferralReason) {
        DeferralRequestDto deferralRequestDto = new DeferralRequestDto();
        deferralRequestDto.setJurorNumber(jurorNumber);
        deferralRequestDto.setDeferralReason(deferralReason);
        deferralRequestDto.setDeferralDecision(DeferralDecision.GRANT);
        LocalDate deferralDate = LocalDate.now().plusDays(10);
        deferralRequestDto.setDeferralDate(deferralDate);
        deferralRequestDto.setAllowMultipleDeferrals(true);
        return deferralRequestDto;
    }
}
