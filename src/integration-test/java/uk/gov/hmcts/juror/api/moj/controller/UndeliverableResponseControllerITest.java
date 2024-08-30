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
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for the Undeliverable Response controller.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UndeliverableResponseControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    private static final String BASE_URL = "/api/v1/moj/undeliverable-response";


    private HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        initHeaders();
    }

    private RequestEntity<JurorNumberListDto> createRequest(String... jurorNumbers) {
        return new RequestEntity<>(new JurorNumberListDto(List.of(jurorNumbers)),
            httpHeaders, HttpMethod.PATCH, URI.create(BASE_URL));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_BureauUser() {
        String jurorNumber = "123456789";

        ResponseEntity<?> response =
            restTemplate.exchange(createRequest(jurorNumber), Void.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);

        assertUpdated(jurorNumber);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_multiple_BureauUser() {
        String jurorNumber1 = "123456789";
        String jurorNumber2 = "123456788";

        ResponseEntity<?> response =
            restTemplate.exchange(createRequest(jurorNumber1, jurorNumber2), String.class);
        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);

        // verify the status of the juror record has been updated
        assertUpdated(jurorNumber1);
        assertUpdated(jurorNumber2);

    }

    private void assertUpdated(String jurorNumber) {
        executeInTransaction(() -> {
            JurorPool jurorPool = jurorPoolRepository
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue("415", jurorNumber);
            Juror juror = jurorPool.getJuror();

            assertThat(juror.isResponded()).isFalse();
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(9);
            assertThat(jurorPool.getNextDate()).isNull(); // next date is null

            // verify the history record has been created
            LocalDate today = LocalDate.now();
            List<JurorHistory> jurorHistoryList =
                jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, today);
            assertThat(jurorHistoryList).isNotNull();
        });
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_BureauUser_jurorRecordDoesNotExist() {
        ResponseEntity<?> response =
            restTemplate.exchange(createRequest("111111111"), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_CourtUser_bureauOwnedRecord() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, getCourtJwt("415"));

        ResponseEntity<?> response =
            restTemplate.exchange(createRequest("123456789"), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_BureauUser_CourtOwnedRecord() {
        ResponseEntity<?> response =
            restTemplate.exchange(createRequest("222222224"), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private void initHeaders() {
        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, getBureauJwt());
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }
}
