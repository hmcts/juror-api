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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.MarkAsDeceasedDto;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the API endpoints defined in DeceasedResponseController }.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.ExcessiveImports")
public class DeceasedResponseControllerITest extends AbstractIntegrationTest {

    private HttpHeaders httpHeaders;

    private static final String PAPER_RESPONSE_EXISTS_TEXT = "A Paper summons reply exists.";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private ContactLogRepository contactLogRepository;
    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Autowired
    private JurorRepository jurorRepository;

    @Before
    public void setUp() throws Exception {
        initHeaders();

    }

    private void initHeaders() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("BUREAU_USER")
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private String initCourtsJwt(String owner, List<String> courts) throws Exception {

        return mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("COURT_USER")
            .owner(owner)
            .staff(BureauJwtPayload.Staff.builder().courts(courts).build())
            .build());
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/DeceasedResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsDeceased_BureauUser_paperResponseExists() {
        String jurorNumber = "222222224";
        Boolean paperResponseExists = true;

        MarkAsDeceasedDto requestDto = createMarkAsDeceasedDto(jurorNumber, paperResponseExists);

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/deceased-response/excuse-deceased-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be OK")
            .isEqualTo(HttpStatus.OK);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, jurorNumber, courtLocation);
        Juror juror = jurorPool.getJuror();

        // verify the status of the juror record has been updated
        assertThat(juror.isResponded()).isTrue();
        assertThat(juror.getExcusalDate()).isEqualTo(LocalDate.now()); // should be set to current date
        assertThat(juror.getExcusalCode()).isEqualTo("D"); // deceased code
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(5); // excused status
        assertThat(jurorPool.getNextDate()).isNull(); // next date is null

        // verify the history record has been created
        List<JurorHistory> jurorHistList = jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
            jurorNumber, LocalDate.now());

        assertThat(jurorHistList).isNotNull();

        // verify the contact log has been created
        List<ContactLog> contactLog = contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLog).isNotEmpty();
        assertThat(contactLog.get(0).getNotes()).contains(PAPER_RESPONSE_EXISTS_TEXT);

        // verify the paper response has been created
        PaperResponse jurorPaperResponse = DataUtils.getJurorPaperResponse(jurorNumber, jurorPaperResponseRepository);
        assertThat(jurorPaperResponse).isNotNull();

    }


    @Test
    @Sql({"/db/mod/truncate.sql","/db/DeceasedResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsDeceased_BureauUser_paperResponseDoesNotExists() {
        final String jurorNumber = "222222225";

        final Boolean paperResponseExists = false;

        MarkAsDeceasedDto requestDto = createMarkAsDeceasedDto(jurorNumber, paperResponseExists);

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/deceased-response/excuse-deceased-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be OK")
            .isEqualTo(HttpStatus.OK);

        // verify the status of the juror record has been updated

        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, jurorNumber, courtLocation);

        assertThat(juror.isResponded()).isTrue();
        assertThat(juror.getExcusalDate()).isEqualTo(LocalDate.now()); // should be set to current date
        assertThat(juror.getExcusalCode()).isEqualTo("D"); // deceased code
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(5); // excused status
        assertThat(jurorPool.getNextDate()).isNull(); // next date is null

        // verify the history record has been created
        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
            jurorNumber, LocalDate.now());
        assertThat(jurorHistoryList).isNotNull();

        // verify the contact log has been created
        List<ContactLog> contactLog = contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLog).isNotEmpty();
        assertThat(contactLog.get(0).getNotes()).doesNotContain(PAPER_RESPONSE_EXISTS_TEXT);

    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/DeceasedResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsDeceased_BureauUser_jurorRecordDoesNotExist() {
        String jurorNumber = "123456789";

        Boolean paperResponseExists = false;

        MarkAsDeceasedDto requestDto = createMarkAsDeceasedDto(jurorNumber, paperResponseExists);

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/deceased-response/excuse-deceased-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/DeceasedResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsDeceased_CourtUser_bureauOwnedRecord() throws Exception {
        String jurorNumber = "222222225";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415")));


        Boolean paperResponseExists = false;

        MarkAsDeceasedDto requestDto = createMarkAsDeceasedDto(jurorNumber, paperResponseExists);

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/deceased-response/excuse-deceased-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/DeceasedResponseController_createInitialPoolRecords.sql"})
    public void markJurorAsDeceased_BureauUser_CourtOwnedRecord() {
        String jurorNumber = "222222226";

        Boolean paperResponseExists = false;

        MarkAsDeceasedDto requestDto = createMarkAsDeceasedDto(jurorNumber, paperResponseExists);

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/deceased-response/excuse-deceased-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private MarkAsDeceasedDto createMarkAsDeceasedDto(String jurorNumber, Boolean exists) {
        MarkAsDeceasedDto requestDto = new MarkAsDeceasedDto();

        requestDto.setJurorNumber(jurorNumber);
        requestDto.setDeceasedComment("Brother phoned in to say deceased.");
        requestDto.setPaperResponseExists(exists);

        return requestDto;
    }

}
