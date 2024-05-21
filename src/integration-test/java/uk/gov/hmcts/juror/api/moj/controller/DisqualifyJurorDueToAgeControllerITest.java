package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.moj.domain.IJurorStatus.RESPONDED;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorDigitalResponse;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorPaperResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DisqualifyJurorDueToAgeControllerITest extends AbstractIntegrationTest {


    @Autowired
    private TestRestTemplate template;

    @Autowired
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;

    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String URI_DISQUALIFY_JUROR = "/api/v1/moj/disqualify/juror/%s/age";
    private static final String JUROR_NUMBER_123456789 = "123456789";
    private static final String JUROR_NUMBER_987654321 = "987654321";
    private static final String JUROR_NUMBER_111111111 = "111111111";

    private static final String JUROR_NUMBER_NO_RESPONSE = "222222222";
    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String COURT_USER = "COURT_USER";


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/summonsmanagement/DisqualifyJurorControllerTestData.sql"})
    public void disqualifyJurorDueToAge_PartialDigitalResponsePresent_BureauUser() {

        //Pre-verification: Verify status of tables before the juror is disqualified
        assertDigitalDisqualifyJurorPreVerification(JUROR_NUMBER_123456789);

        //Setup data
        //Invoke service
        assertTemplateExchangeDisqualifyJuror(UserType.BUREAU, JUROR_NUMBER_123456789,
            BUREAU_USER, "400", HttpStatus.OK);

        //Post-verification: Verify tables updated
        assertDigitalDisqualifyJurorPostVerification(JUROR_NUMBER_123456789);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/summonsmanagement/DisqualifyJurorControllerTestData.sql"})
    public void disqualifyJurorDueToAge_PartialPaperResponsePresent_CourtUser() {

        //Pre-verification: Verify status of tables before the juror is disqualified
        paperDisqualifyJurorPreVerification(JUROR_NUMBER_987654321);

        //Setup data
        //Invoke service
        assertTemplateExchangeDisqualifyJuror(UserType.COURT, JUROR_NUMBER_987654321, COURT_USER, "415", HttpStatus.OK);

        //Post-verification: Verify tables updated
        assertPaperDisqualifyJurorPostVerification(COURT_USER, JUROR_NUMBER_987654321);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/summonsmanagement/DisqualifyJurorControllerTestData.sql"})
    public void disqualifyJurorDueToAge_noResponse() {

        assertThat(!jurorDigitalResponseRepository.existsById(JUROR_NUMBER_NO_RESPONSE));
        assertThat(!jurorPaperResponseRepository.existsById(JUROR_NUMBER_NO_RESPONSE));

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_NUMBER_NO_RESPONSE, true);
        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();
        assertThat(juror.isResponded()).isEqualTo(Boolean.FALSE);
        assertThat(juror.getDisqualifyDate()).isNull();
        assertThat(juror.getDisqualifyCode()).isNull();
        assertThat(juror.getUserEdtq()).isEqualTo("BUREAU_USER_1");
        assertThat(jurorPoolRecord.getNextDate()).isNotNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(IJurorStatus.SUMMONED);

        //History
        LocalDate today = LocalDate.now();
        List<JurorHistory> jurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(JUROR_NUMBER_NO_RESPONSE, today);
        assertThat(jurorHistoryList).isEmpty();

        assertTemplateExchangeDisqualifyJuror(UserType.BUREAU, JUROR_NUMBER_NO_RESPONSE,
            BUREAU_USER, "400", HttpStatus.OK);

        assertPaperDisqualifyJurorPostVerification(BUREAU_USER, JUROR_NUMBER_NO_RESPONSE);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/summonsmanagement/DisqualifyJurorControllerTestData.sql"})
    public void disqualifyJurorForbiddenException() {
        //Current location of the juror pool record is 416 - the user updating the request is 415 therefore a
        //403 Forbidden exception is thrown
        assertTemplateExchangeDisqualifyJuror(UserType.COURT, JUROR_NUMBER_111111111, COURT_USER, "415",
            HttpStatus.FORBIDDEN);
    }


    private void assertDigitalDisqualifyJurorPreVerification(String jurorNumber) {

        //Juror digital response
        DigitalResponse digitalResponse = getJurorDigitalResponse(jurorNumber,
            jurorDigitalResponseRepository);
        assertThat(digitalResponse).isNotNull();
        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);
        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();
        assertThat(juror.isResponded()).isEqualTo(Boolean.FALSE);
        assertThat(juror.getDisqualifyDate()).isNull();
        assertThat(juror.getDisqualifyCode()).isNull();
        assertThat(juror.getUserEdtq()).isEqualTo("BUREAU_USER_1");
        assertThat(jurorPoolRecord.getNextDate()).isNotNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(RESPONDED);

        //History
        LocalDate today = LocalDate.now();
        List<JurorHistory> jurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, today);
        assertThat(jurorHistoryList).isEmpty();
    }


    private void assertDigitalDisqualifyJurorPostVerification(String jurorNumber) {
        //Juror digital response
        DigitalResponse digitalResponse = getJurorDigitalResponse(jurorNumber,
            jurorDigitalResponseRepository);
        assertThat(digitalResponse).isNotNull();
        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();
        assertThat(juror.isResponded()).isEqualTo(Boolean.TRUE);
        assertThat(juror.getDisqualifyDate()).isNotNull();
        assertThat(juror.getDisqualifyCode()).isEqualTo("A");
        assertThat(jurorPoolRecord.getUserEdtq()).isEqualTo(BUREAU_USER);
        assertThat(jurorPoolRecord.getNextDate()).isNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(IJurorStatus.DISQUALIFIED);

        //History
        LocalDate updatedToday = LocalDate.now();
        List<JurorHistory> updatedJurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber,
                updatedToday);
        assertThat(updatedJurorHistoryList).isNotNull();
    }

    private void paperDisqualifyJurorPreVerification(String jurorNumber) {

        assertThat(!jurorDigitalResponseRepository.existsById(jurorNumber));

        //Juror paper response
        PaperResponse paperResponse = getJurorPaperResponse(jurorNumber,
            jurorPaperResponseRepository);
        assertThat(paperResponse).isNotNull();
        assertThat(paperResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);
        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();
        assertThat(juror.isResponded()).isEqualTo(Boolean.FALSE);
        assertThat(juror.getDisqualifyDate()).isNull();
        assertThat(juror.getDisqualifyCode()).isNull();
        assertThat(juror.getUserEdtq()).isEqualTo("COURT_USER_1");
        assertThat(jurorPoolRecord.getNextDate()).isNotNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(RESPONDED);

        //History
        LocalDate today = LocalDate.now();
        List<JurorHistory> jurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, today);
        assertThat(jurorHistoryList).isEmpty();
    }

    private void assertPaperDisqualifyJurorPostVerification(String user, String jurorNumber) {
        //Juror paper response
        PaperResponse paperResponse = getJurorPaperResponse(jurorNumber,
            jurorPaperResponseRepository);
        assertThat(paperResponse).isNotNull();
        assertThat(paperResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();
        assertThat(juror.isResponded()).isEqualTo(Boolean.TRUE);
        assertThat(juror.getDisqualifyDate()).isNotNull();
        assertThat(juror.getDisqualifyCode()).isEqualTo("A");
        assertThat(jurorPoolRecord.getUserEdtq()).isEqualTo(user);
        assertThat(jurorPoolRecord.getNextDate()).isNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(IJurorStatus.DISQUALIFIED);

        //History
        LocalDate updatedToday = LocalDate.now();
        List<JurorHistory> updatedJurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber,
                updatedToday);
        assertThat(updatedJurorHistoryList).isNotNull();
    }


    private void assertTemplateExchangeDisqualifyJuror(UserType userType,
                                                       String jurorNumber,
                                                       String username,
                                                       String owner,
                                                       HttpStatus httpStatus) {
        final URI uri = URI.create(String.format(URI_DISQUALIFY_JUROR, jurorNumber));
        HttpHeaders httpHeaders =
            initialiseHeaders(username,userType,Set.of(Role.MANAGER),owner);

        RequestEntity<String> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);
    }
}