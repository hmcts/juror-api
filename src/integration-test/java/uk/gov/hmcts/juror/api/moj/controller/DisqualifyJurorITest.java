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
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorDigitalResponse;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorPaperResponse;

/**
 * Integration tests for the API endpoints defined in DisqualifyJurorController.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DisqualifyJurorITest extends AbstractIntegrationTest {

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

    private HttpHeaders httpHeaders;

    private static final String URI_DISQUALIFY_JUROR = "/api/v1/moj/disqualify/juror/%s";
    private static final String JUROR_NUMBER_123456789 = "123456789";
    private static final String JUROR_NUMBER_987654321 = "987654321";
    private static final String JUROR_NUMBER_111111111 = "111111111";
    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String COURT_USER = "COURT_USER";

    //Tests related to controller method: disqualifyReasons()
    @Test
    public void disqualifyReasons() {
        DisqualifyReasonsDto disqualifyReasonsDto = templateExchangeDisqualifyReasons(BUREAU_USER, "400",
            HttpStatus.OK);

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().size()).isEqualTo(10);
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(0).getCode()).isEqualTo("A");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(0).getDescription()).isEqualTo("Age");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(1).getCode()).isEqualTo("B");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(1).getDescription()).isEqualTo("Bail");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(2).getCode()).isEqualTo("C");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(2).getDescription()).isEqualTo("Conviction");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(3).getCode()).isEqualTo("D");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(3).getDescription())
            .isEqualTo("Judicial Disqualification");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(4).getCode()).isEqualTo("E");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(4).getDescription())
            .isEqualTo("Electronic Police Check Failure");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(5).getCode()).isEqualTo("J");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(5).getDescription())
            .isEqualTo("Involved in Justice Administration or a Member of the Clergy");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(6).getCode()).isEqualTo("M");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(6).getDescription())
            .isEqualTo("Suffering From a Mental Disorder");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(7).getCode()).isEqualTo("N");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(7).getDescription())
            .isEqualTo("Mental Capacity Act");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(8).getCode()).isEqualTo("O");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(8).getDescription())
            .isEqualTo("Mental Health Act");

        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(9).getCode()).isEqualTo("R");
        assertThat(disqualifyReasonsDto.getDisqualifyReasons().get(9).getDescription()).isEqualTo("Residency");
    }

    //Tests related to controller method: disqualifyJuror()
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/summonsmanagement/DisqualifyJurorControllerTestData.sql"})
    public void disqualifyJurorDigitalResponseBureauUserHappy() {

        //Pre-verification: Verify status of tables before the juror is disqualified
        digitalDisqualifyJurorPreVerification();

        //Setup data
        DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorDigitalDto();

        //Invoke service
        templateExchangeDisqualifyJuror(disqualifyJurorDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", HttpStatus.OK);

        //Post-verification: Verify tables updated
        digitalDisqualifyJurorPostVerification();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/summonsmanagement/DisqualifyJurorControllerTestData.sql"})
    public void disqualifyPaperResponseCourtUserHappy() {

        //Pre-verification: Verify status of tables before the juror is disqualified
        paperDisqualifyJurorPreVerification();

        //Setup data
        DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorPaperDto();

        //Invoke service
        templateExchangeDisqualifyJuror(disqualifyJurorDto, JUROR_NUMBER_987654321, COURT_USER, "415", HttpStatus.OK);

        //Post-verification: Verify tables updated
        paperDisqualifyJurorPostVerification();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/summonsmanagement/DisqualifyJurorControllerTestData.sql"})
    public void disqualifyJurorForbiddenException() {
        //Current location of the juror pool record is 416 - the user updating the request is 415 therefore a
        //403 Forbidden exception is thrown
        DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorPaperDto();

        templateExchangeDisqualifyJuror(disqualifyJurorDto, JUROR_NUMBER_111111111, COURT_USER, "415",
            HttpStatus.FORBIDDEN);
    }

    private void digitalDisqualifyJurorPostVerification() {
        //Juror digital response
        DigitalResponse digitalResponse = getJurorDigitalResponse(JUROR_NUMBER_123456789,
            jurorDigitalResponseRepository);
        assertThat(digitalResponse).isNotNull();
        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_NUMBER_123456789, true);

        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();
        assertThat(juror.isResponded()).isEqualTo(Boolean.TRUE);
        assertThat(juror.getDisqualifyDate()).isNotNull();
        assertThat(juror.getDisqualifyCode()).isEqualTo("M");
        assertThat(jurorPoolRecord.getUserEdtq()).isEqualTo(BUREAU_USER);
        assertThat(jurorPoolRecord.getNextDate()).isNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(IJurorStatus.DISQUALIFIED);

        //History
        LocalDate updatedToday = LocalDate.now();
        List<JurorHistory> updatedJurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(JUROR_NUMBER_123456789,
                updatedToday);
        assertThat(updatedJurorHistoryList).isNotNull();
    }

    private void paperDisqualifyJurorPostVerification() {
        //Juror paper response
        PaperResponse paperResponse = getJurorPaperResponse(JUROR_NUMBER_987654321,
            jurorPaperResponseRepository);
        assertThat(paperResponse).isNotNull();
        assertThat(paperResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_NUMBER_987654321, true);

        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();

        assertThat(juror.isResponded()).isEqualTo(Boolean.TRUE);
        assertThat(juror.getDisqualifyDate()).isNotNull();
        assertThat(juror.getDisqualifyCode()).isEqualTo("C");
        assertThat(jurorPoolRecord.getUserEdtq()).isEqualTo(COURT_USER);
        assertThat(jurorPoolRecord.getNextDate()).isNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(IJurorStatus.DISQUALIFIED);

        //History
        LocalDate updatedToday = LocalDate.now();
        List<JurorHistory> updatedJurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(JUROR_NUMBER_987654321,
                updatedToday);
        assertThat(updatedJurorHistoryList).isNotNull();
    }

    private void digitalDisqualifyJurorPreVerification() {
        //Juror digital response
        DigitalResponse digitalResponse = getJurorDigitalResponse(JUROR_NUMBER_123456789,
            jurorDigitalResponseRepository);
        assertThat(digitalResponse).isNotNull();
        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_NUMBER_123456789, true);
        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();

        assertThat(juror.isResponded()).isEqualTo(Boolean.FALSE);
        assertThat(juror.getDisqualifyDate()).isNull();
        assertThat(juror.getDisqualifyCode()).isNull();
        assertThat(juror.getUserEdtq()).isEqualTo("BUREAU_USER_1");
        assertThat(jurorPoolRecord.getNextDate()).isNotNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(IJurorStatus.RESPONDED);

        //History
        LocalDate today = LocalDate.now();
        List<JurorHistory> jurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(JUROR_NUMBER_123456789, today);
        assertThat(jurorHistoryList).isEmpty();
    }

    private void paperDisqualifyJurorPreVerification() {
        //Juror paper response
        PaperResponse paperResponse = getJurorPaperResponse(JUROR_NUMBER_987654321,
            jurorPaperResponseRepository);
        assertThat(paperResponse).isNotNull();
        assertThat(paperResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

        //Pool (juror) record
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_NUMBER_987654321, true);
        assertThat(jurorPools.size()).isGreaterThan(0);

        JurorPool jurorPoolRecord = jurorPools.get(0);
        Juror juror = jurorPoolRecord.getJuror();

        assertThat(juror.isResponded()).isEqualTo(Boolean.FALSE);
        assertThat(juror.getDisqualifyDate()).isNull();
        assertThat(juror.getDisqualifyCode()).isNull();
        assertThat(juror.getUserEdtq()).isEqualTo("COURT_USER_1");
        assertThat(jurorPoolRecord.getNextDate()).isNotNull();
        assertThat(jurorPoolRecord.getStatus().getStatus()).isEqualTo(IJurorStatus.RESPONDED);

        //History
        LocalDate today = LocalDate.now();
        List<JurorHistory> jurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(JUROR_NUMBER_987654321, today);
        assertThat(jurorHistoryList).isEmpty();
    }

    private void templateExchangeDisqualifyJuror(DisqualifyJurorDto disqualifyJurorDto,
                                                 String jurorNumber,
                                                 String userType,
                                                 String owner,
                                                 HttpStatus httpStatus) {
        final URI uri = URI.create(String.format(URI_DISQUALIFY_JUROR, jurorNumber));
        httpHeaders = initialiseHeaders("1", false, userType, 89, owner);

        RequestEntity<DisqualifyJurorDto> requestEntity = new RequestEntity<>(disqualifyJurorDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);
    }

    private DisqualifyReasonsDto templateExchangeDisqualifyReasons(String userType, String owner,
                                                                   HttpStatus httpStatus) {
        final URI uri = URI.create("/api/v1/moj/disqualify/reasons");
        httpHeaders = initialiseHeaders("1", false, userType, 89, owner);

        RequestEntity<DisqualifyReasonsDto> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<DisqualifyReasonsDto> response = template.exchange(requestEntity, DisqualifyReasonsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);

        return response.getBody();
    }

    private DisqualifyJurorDto createDisqualifyJurorDigitalDto() {
        return DisqualifyJurorDto.builder()
            .code(DisqualifyCodeEnum.N)
            .replyMethod(ReplyMethod.DIGITAL)
            .build();
    }

    private DisqualifyJurorDto createDisqualifyJurorPaperDto() {
        return DisqualifyJurorDto.builder()
            .code(DisqualifyCodeEnum.C)
            .replyMethod(ReplyMethod.PAPER)
            .build();
    }
}
