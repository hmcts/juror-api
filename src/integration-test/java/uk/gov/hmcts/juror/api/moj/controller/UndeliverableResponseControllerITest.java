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
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

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


    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();

    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_BureauUser() {
        String jurorNumber = "123456789";

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/undeliverable-response/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be OK")
            .isEqualTo(HttpStatus.OK);

        // verify the status of the juror record has been updated
        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        Juror juror = jurorPool.getJuror();

        assertThat(juror.isResponded()).isFalse();
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(9);
        assertThat(jurorPool.getNextDate()).isNull(); // next date is null

        // verify the history record has been created
        LocalDate today = LocalDate.now();

        List<JurorHistory> jurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, today);

        assertThat(jurorHistoryList).isNotNull();
    }


    @Test
    @Sql({"/db/mod/truncate.sql","/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_BureauUser_jurorRecordDoesNotExist() {
        String jurorNumber = "111111111";

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/undeliverable-response/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_CourtUser_bureauOwnedRecord() throws Exception {
        String jurorNumber = "123456789";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415")));

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/undeliverable-response/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/UndeliverableResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsUndeliverable_BureauUser_CourtOwnedRecord() {
        String jurorNumber = "222222224";

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.PUT,
                URI.create("/api/v1/moj/undeliverable-response/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private void initHeaders() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("BUREAU_USER")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private String initCourtsJwt(String owner, List<String> courts) throws Exception {

        return mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner(owner)
            .staff(BureauJWTPayload.Staff.builder().courts(courts).build())
            .build());
    }
}
