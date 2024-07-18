package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.ExcuseDeniedLetterRepository;
import uk.gov.hmcts.juror.api.moj.repository.ExcuseLetterRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision.REFUSE;

/**
 * Integration tests for the API endpoints defined in {@link ExcusalResponseController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class ExcusalResponseControllerITest extends AbstractIntegrationTest {

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Autowired
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JurorStatusRepository jurorStatusRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private ExcuseDeniedLetterRepository excusalDeniedLetterRepository;
    @Autowired
    private ExcuseLetterRepository excusalLetterRepository;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;

    private HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void refuseExcusalRequest_paperResponse_bureauUser_bureauOwner() throws Exception {
        final String jurorNumber = "123456789";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        validatePaperResponseExcusal(jurorPaperResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateRefuseExcusal(jurorPool, excusalDecisionDto, login);
        validateExcusalDeniedLetter();
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void refuseExcusalRequest_paperResponse_courtUser_courtOwner() throws Exception {
        final String jurorNumber = "987654321";
        final String login = "COURT_USER";
        final String courtJwt = createJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PaperResponse jurorPaperResponse =
            jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        validatePaperResponseExcusal(jurorPaperResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateRefuseExcusal(jurorPool, excusalDecisionDto, login);

        Iterable<BulkPrintData> bulkPrintDataIterable = bulkPrintDataRepository.findAll();
        List<BulkPrintData> bulkPrintData = new ArrayList<>();
        bulkPrintDataIterable.forEach((data) -> {
            if (data.getFormAttribute().getFormType().equals(FormCode.BI_EXCUSALDENIED.getCode())
                || data.getFormAttribute().getFormType().equals(FormCode.ENG_EXCUSALDENIED.getCode())) {
                bulkPrintData.add(data);
            }
        });

        // there should be zero letters as this is a court journey
        assertThat(bulkPrintData.size())
            .as("Expect zero letters to be queued in the bulk print table")
            .isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void grantExcusalRequest_paperResponse_bureauUser_bureauOwner() throws Exception {
        final String jurorNumber = "123456789";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PaperResponse jurorPaperResponse =
            jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        validatePaperResponseExcusal(jurorPaperResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateExcusal(jurorPool, excusalDecisionDto, login);

        validateExcusalLetter(excusalDecisionDto.getExcusalReasonCode());
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void grantExcusalRequest_paperResponse_courtUser_courtOwner() throws Exception {
        final String jurorNumber = "987654321";
        final String login = "COURT_USER";
        final String courtJwt = createJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PaperResponse jurorPaperResponse =
            jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        validatePaperResponseExcusal(jurorPaperResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateExcusal(jurorPool, excusalDecisionDto, login);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initDigitalResponse.sql"})
    public void refuseExcusalRequest_digitalResponse_bureauUser_bureauOwner() throws Exception {
        final String jurorNumber = "111222333";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        validateDigitalResponseExcusal(jurorResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateRefuseExcusal(jurorPool, excusalDecisionDto, login);
        validateExcusalDeniedLetter();
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initDigitalResponse.sql"})
    public void refuseExcusalRequest_digitalResponse_courtUser_courtOwner() throws Exception {
        final String jurorNumber = "333222111";
        final String login = "COURT_USER";
        final String courtJwt = createJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        validateDigitalResponseExcusal(jurorResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateRefuseExcusal(jurorPool, excusalDecisionDto, login);
        Iterable<BulkPrintData> bulkPrintDataIterable = bulkPrintDataRepository.findAll();
        List<BulkPrintData> bulkPrintData = new ArrayList<>();
        bulkPrintDataIterable.forEach((data) -> {
            if (data.getFormAttribute().getFormType().equals(FormCode.BI_EXCUSALDENIED.getCode())
                || data.getFormAttribute().getFormType().equals(FormCode.ENG_EXCUSALDENIED.getCode())) {
                bulkPrintData.add(data);
            }
        });

        // there should be zero letters as this is a court journey
        assertThat(bulkPrintData.size())
            .as("Expect zero letters to be queued in the bulk print table")
            .isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ExcusalResponse_initDigitalResponse.sql"})
    public void grantExcusalRequest_digitalResponse_bureauUser_bureauOwner() throws Exception {
        final String jurorNumber = "111222333";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        validateDigitalResponseExcusal(jurorResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateExcusal(jurorPool, excusalDecisionDto, login);
        validateExcusalLetter(excusalDecisionDto.getExcusalReasonCode());
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initDigitalResponse.sql"})
    public void grantExcusalRequest_digitalResponse_courtUser_courtOwner() throws Exception {
        final String jurorNumber = "333222111";
        final String login = "COURT_USER";
        final String courtJwt = createJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        validateDigitalResponseExcusal(jurorResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateExcusal(jurorPool, excusalDecisionDto, login);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void grantExcusalRequest_excusalCodeDeceased() throws Exception {
        final String jurorNumber = "123456789";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);
        excusalDecisionDto.setExcusalReasonCode(ExcusalCodeEnum.D.getCode());

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        validatePaperResponseExcusal(jurorPaperResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateExcusal(jurorPool, excusalDecisionDto, login);
        validateExcusalLetter(excusalDecisionDto.getExcusalReasonCode());
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void excusalRequest_bureauUser_courtOwner() throws Exception {
        final String jurorNumber = "987654321";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void excusalRequest_courtUser_bureauOwner() throws Exception {
        final String jurorNumber = "123456789";
        final String login = "COURT_USER";
        final String courtJwt = createJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void excusalRequest_paperResponse_alreadyProcessed() throws Exception {
        final String jurorNumber = "111111111";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setReplyMethod(ReplyMethod.PAPER);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);
        excusalDecisionDto.setExcusalReasonCode(ExcusalCodeEnum.D.getCode());

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        validatePaperResponseExcusal(jurorPaperResponse, login);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateExcusal(jurorPool, excusalDecisionDto, login);
        validateExcusalLetter(excusalDecisionDto.getExcusalReasonCode());
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initDigitalResponse.sql"})
    public void excusalRequest_digitalResponse_alreadyProcessed() throws Exception {
        final String jurorNumber = "222222222";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);
        excusalDecisionDto.setExcusalReasonCode(ExcusalCodeEnum.D.getCode());

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void excusalRequest_invalidExcusalCode() throws Exception {
        final String jurorNumber = "123456789";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();
        excusalDecisionDto.setExcusalReasonCode("?");

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/ExcusalResponse_initPaperResponse.sql"})
    public void excusalRequest_paperResponse_doesNotExist() throws Exception {
        final String jurorNumber = "222222222";
        final String login = "BUREAU_USER";
        final String bureauJwt = createJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/excusal-response/juror/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ExcusalDecisionDto excusalDecisionDto = createExcusalDecisionDto();

        RequestEntity<ExcusalDecisionDto> requestEntity = new RequestEntity<>(excusalDecisionDto, httpHeaders,
            HttpMethod.PUT, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        validateExcusal(jurorPool, excusalDecisionDto, login);
        validateExcusalLetter(excusalDecisionDto.getExcusalReasonCode());
    }

    private void validatePaperResponseExcusal(PaperResponse jurorPaperResponse, String login) {
        assertThat(jurorPaperResponse.getProcessingStatus())
            .as("Paper response should be marked as closed")
            .isEqualTo(ProcessingStatus.CLOSED);
        assertThat(jurorPaperResponse.getStaff())
            .as("Current user should be assigned to the response (dependent on test data)")
            .isEqualTo(userRepository.findByUsername(login));
    }

    private void validateDigitalResponseExcusal(DigitalResponse jurorDigitalResponse, String login) {
        assertThat(jurorDigitalResponse.getProcessingStatus())
            .as("Digital response should be marked as closed")
            .isEqualTo(ProcessingStatus.CLOSED);
        assertThat(jurorDigitalResponse.getStaff())
            .as("Current user should be assigned to the response (dependent on test data)")
            .isEqualTo(userRepository.findByUsername(login));
    }

    private void validateRefuseExcusal(JurorPool jurorPool, ExcusalDecisionDto excusalDecisionDto, String login) {
        Juror juror = jurorPool.getJuror();
        assertThat(juror.isResponded())
            .as("Juror record should be updated and marked as responded")
            .isTrue();
        assertThat(juror.getExcusalDate())
            .as("Juror record should be updated with an excusal date")
            .isNotNull();
        assertThat(juror.getExcusalCode())
            .as("Juror record should be update with an excusal code")
            .isEqualTo(excusalDecisionDto.getExcusalReasonCode());
        assertThat(juror.getUserEdtq())
            .as("Current user should be recorded in juror record as last to make changes")
            .isEqualTo(login);
        assertThat(juror.getExcusalRejected())
            .as("Juror record should be updated to show excusal was rejected")
            .isEqualTo("Y");
        assertThat(jurorPool.getStatus().getStatus())
            .as("Juror record should be updated to show they have responded")
            .isEqualTo(IJurorStatus.RESPONDED);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<JurorHistory> jurorHistory = jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
            jurorPool.getJurorNumber(), yesterday);
        assertThat(jurorHistory.stream()
            .anyMatch(jurorHist -> jurorHist.getHistoryCode().equals(HistoryCodeMod.RESPONDED_POSITIVELY)))
            .as("Expect history record to be created to show juror now responded")
            .isTrue();
    }

    private void validateExcusalDeniedLetter() {
        Iterable<BulkPrintData> bulkPrintDataIterable = bulkPrintDataRepository.findAll();
        List<BulkPrintData> bulkPrintData = new ArrayList<>();
        bulkPrintDataIterable.forEach((data) -> {
            if (data.getFormAttribute().getFormType().equals(FormCode.BI_EXCUSALDENIED.getCode())
                || data.getFormAttribute().getFormType().equals(FormCode.ENG_EXCUSALDENIED.getCode())) {
                bulkPrintData.add(data);
            }
        })
        ;

        assertThat(bulkPrintData.size())
            .as("Expect a single excusal denied letter to exist")
            .isEqualTo(1);
    }

    private void validateExcusal(JurorPool jurorPool, ExcusalDecisionDto excusalDecisionDto, String login) {
        Juror juror = jurorPool.getJuror();
        assertThat(juror.isResponded())
            .as("Juror record should be updated and marked as responded")
            .isTrue();
        assertThat(juror.getExcusalDate())
            .as("Juror record should be updated with an excusal date")
            .isNotNull();
        assertThat(juror.getExcusalCode())
            .as("Juror record should be update with an excusal code")
            .isEqualTo(excusalDecisionDto.getExcusalReasonCode());
        assertThat(juror.getUserEdtq())
            .as("Current user should be recorded in juror record as last to make changes")
            .isEqualTo(login);
        if (excusalDecisionDto.getExcusalDecision().equals(REFUSE)) {
            assertThat(jurorPool.getStatus().getStatus())
                .as("Juror record should be updated to show they are responded")
                .isEqualTo(IJurorStatus.RESPONDED);
        } else {
            assertThat(jurorPool.getStatus().getStatus())
                .as("Juror record should be updated to show they are excused")
                .isEqualTo(IJurorStatus.EXCUSED);
        }
        assertThat(jurorPool.getNextDate())
            .as("Next date should be set to null as Juror has been excused")
            .isNull();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
            juror.getJurorNumber(), yesterday);
        assertThat(jurorHistoryList.stream()
            .anyMatch(jurorHistory -> jurorHistory.getHistoryCode().equals(HistoryCodeMod.EXCUSE_POOL_MEMBER)))
            .as("Expect history record to be created for excusal refusal")
            .isTrue();
    }

    private void validateExcusalLetter(String excusalCode) {
        Iterable<BulkPrintData> excusalLetterIterable = bulkPrintDataRepository.findAll();
        List<BulkPrintData> excusalLetters = new ArrayList<>();
        excusalLetterIterable.forEach(excusalLetters::add);

        if (ExcusalCodeEnum.D.getCode().equals(excusalCode)) {
            assertThat(excusalLetters.size())
                .as("Expect no excusal letter for deceased jurors")
                .isEqualTo(0);
        } else {
            assertThat(excusalLetters.size())
                .as("Expect a single excusal letter to exist")
                .isEqualTo(1);
        }
    }

    private ExcusalDecisionDto createExcusalDecisionDto() {
        ExcusalDecisionDto excusalDecisionDto = new ExcusalDecisionDto();
        excusalDecisionDto.setExcusalReasonCode("A");
        excusalDecisionDto.setExcusalDecision(REFUSE);
        excusalDecisionDto.setReplyMethod(ReplyMethod.PAPER);
        return excusalDecisionDto;
    }

}
