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
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.CjsEmploymentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EligibilityDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReasonableAdjustmentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReplyTypeDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SignatureDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorPaperResponseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.SaveJurorPaperReplyResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SummonsSnapshot;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.letter.DisqualificationLetterMod;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.DisqualifyLetterModRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.SummonsSnapshotRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.StraightThroughProcessorService;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration tests for the API endpoints defined in {@link JurorPaperResponseController}.
 */
@SuppressWarnings("PMD.LawOfDemeter")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JurorPaperResponseControllerITest extends AbstractIntegrationTest {

    private static final String POOL_415220502 = "415220502";
    private static final String POOL_411220502 = "411220502";

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Autowired
    private JurorResponseCjsEmploymentRepositoryMod jurorPaperResponseCjsRepository;
    @Autowired
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private JurorResponseRepository jurorResponseRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private DisqualifyLetterModRepository disqualifyLetterRepository;
    @Autowired
    private SummonsSnapshotRepository summonsSnapshotRepository;
    @Autowired
    private WelshCourtLocationRepository welshCourtLocationRepository;
    @Autowired
    private StraightThroughProcessorService straightThroughProcessorService;

    @Autowired
    private JurorRepository jurorRepository;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_bureauUser_happyPath_notStraightThroughAcceptance() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        setThirdPartyDetails(requestDto);
        requestDto.setCjsEmployment(Collections.singletonList(buildCjsEmployment("Police Force")));
        requestDto.setReasonableAdjustments(Collections.singletonList(buildSpecialNeeds("V")));

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        verifyRequestDtoMapping(requestDto);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_bureauUser_happyPath_straightThroughAcceptance() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("111111111");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isTrue();

        verifyRequestDtoMapping(requestDto);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_courtUser_superUrgent_happyPath_notStraightThroughAcceptance() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("123456791");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        final URI uri2 = URI.create("/api/v1/moj/juror-paper-response/juror/123456791");

        RequestEntity<Void> requestEntity2 = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri2);
        ResponseEntity<JurorPaperResponseDetailDto> responseDetailDto = template.exchange(requestEntity2,
            JurorPaperResponseDetailDto.class);
        assertThat(responseDetailDto.getStatusCode()).isEqualTo(HttpStatus.OK);

        //get request for object and check super urgent set.
        JurorPaperResponseDetailDto responseDetail = responseDetailDto.getBody();
        assertThat(responseDetail).isNotNull();

        assertThat(responseDetail.getSuperUrgent()).isEqualTo(true);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_courtUser_superUrgent_happyPath_straightThroughAcceptance() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("333333333");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isTrue();

        final URI uri2 = URI.create("/api/v1/moj/juror-paper-response/juror/333333333");

        RequestEntity<Void> requestEntity2 = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri2);
        ResponseEntity<JurorPaperResponseDetailDto> responseDetailDto = template.exchange(requestEntity2,
            JurorPaperResponseDetailDto.class);
        assertThat(responseDetailDto.getStatusCode()).isEqualTo(HttpStatus.OK);

        //get request for object and check super urgent set.
        JurorPaperResponseDetailDto responseDetail = responseDetailDto.getBody();
        assertThat(responseDetail).isNotNull();

        assertThat(responseDetail.getSuperUrgent()).isEqualTo(true);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_courtUser_notSuperUrgent_happyPath() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("123456711");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        final URI uri2 = URI.create("/api/v1/moj/juror-paper-response/juror/123456711");

        RequestEntity<Void> requestEntity2 = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri2);
        ResponseEntity<JurorPaperResponseDetailDto> responseDetailDto = template.exchange(requestEntity2,
            JurorPaperResponseDetailDto.class);
        assertThat(responseDetailDto.getStatusCode()).isEqualTo(HttpStatus.OK);

        //get request for object and check super urgent set.
        JurorPaperResponseDetailDto responseDetail = responseDetailDto.getBody();
        assertThat(responseDetail).isNotNull();

        assertThat(responseDetail.getSuperUrgent()).isEqualTo(false);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_courtUser_noAccess() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "416");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("987654321");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_bureauUser_noJurorRecord() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("111111110");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_bureauUser_courtOwnedJurorRecord() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("987654321");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_courtUser_bureauOwnedJurorRecord() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("123456789");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers.sql"})
    public void respondToSummons_courtUser_paperResponseAlreadyExists() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber("222222222");

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers_ageDisqualification.sql"})
    public void respondToSummons_straightThrough_ageDisqualification_tooOld() throws Exception {
        final String jurorNumber = "555555555";
        final String courtOwner = "411";
        final String bureauJwt = createBureauJwt("COURT_USER", courtOwner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        // use a basic response where eligibility questions are unanswered
        JurorPaperResponseDto requestDto = buildBasicJurorPaperResponseDto();
        requestDto.setJurorNumber(jurorNumber);
        requestDto.setDateOfBirth(LocalDate.of(1945, 5, 3));

        RequestEntity<JurorPaperResponseDto> requestEntity =
            new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response =
            template.exchange(requestEntity, SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        PaperResponse jurorPaperResponse = getJurorPaperResponse(jurorNumber);

        verifyRequestDtoMapping_personalDetails(jurorPaperResponse, requestDto);
        verifyRequestDtoMapping_contactDetails(jurorPaperResponse, requestDto);

        Optional<JurorPool> jurorPoolOpt =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber, POOL_415220502, true);

        assertThat(jurorPoolOpt.isPresent()).isTrue();

        JurorPool jurorPool = jurorPoolOpt.get();
        validateMergedJurorRecord(jurorPool, jurorPaperResponse, IJurorStatus.DISQUALIFIED);
        verifyAgeDisqualification(jurorPool);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers_ageDisqualification.sql"})
    public void respondToSummons_straightThrough_ageDisqualification_tooYoung() throws Exception {
        final String jurorNumber = "111111111";
        final String bureauOwner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", bureauOwner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber(jurorNumber);
        requestDto.setDateOfBirth(LocalDate.of(2022, 5, 3).minusYears(17));

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        PaperResponse jurorPaperResponse = getJurorPaperResponse(jurorNumber);
        verifyRequestDtoMapping_personalDetails(jurorPaperResponse, requestDto);
        verifyRequestDtoMapping_contactDetails(jurorPaperResponse, requestDto);

        Optional<JurorPool> jurorPoolOpt =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
                POOL_411220502, true);

        assertThat(jurorPoolOpt.isPresent()).isTrue();

        JurorPool jurorPool = jurorPoolOpt.get();

        validateMergedJurorRecord(jurorPool, jurorPaperResponse, IJurorStatus.DISQUALIFIED);
        verifyAgeDisqualification(jurorPool);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers_ageDisqualification.sql"})
    public void respondToSummons_straightThrough_ageDisqualification_invalidStatus() throws Exception {
        final String jurorNumber = "222222222";
        final String bureauOwner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", bureauOwner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber(jurorNumber);
        requestDto.setDateOfBirth(LocalDate.of(2022, 5, 3).minusYears(17));

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        PaperResponse jurorPaperResponse = getJurorPaperResponse(jurorNumber);

        Optional<JurorPool> jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber, POOL_411220502, true);

        assertThat(jurorPool.isPresent()).isTrue();

        verifyStraightThrough_ageDisqualification_notProcessed(jurorPaperResponse, jurorPool.get(),
            IJurorStatus.DEFERRED);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPoolMembers_ageDisqualification.sql"})
    public void respondToSummons_straightThrough_ageDisqualification_thirdParty() throws Exception {
        final String jurorNumber = "444444444";
        final String courtOwner = "411";
        final String bureauJwt = createBureauJwt("COURT_USER", courtOwner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber(jurorNumber);
        requestDto.setDateOfBirth(LocalDate.of(2022, 5, 3).minusYears(80));
        setThirdPartyDetails(requestDto);

        RequestEntity<JurorPaperResponseDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response = template.exchange(requestEntity,
            SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        PaperResponse jurorPaperResponse = getJurorPaperResponse(jurorNumber);

        Optional<JurorPool> jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber, POOL_415220502, true);

        assertThat(jurorPool.isPresent()).isTrue();
        verifyStraightThrough_ageDisqualification_notProcessed(jurorPaperResponse, jurorPool.get(),
            IJurorStatus.SUMMONED);
    }

    @Test
    @Sql({"/db/mod/truncate.sql",
        "/db/JurorPaperResponse_initPoolMembers_ageDisqualification.sql",
        "/db/JurorPaperResponse_initDisqualifyLetter_ageDisqualification.sql"})
    public void respondToSummons_straightThrough_ageDisqualification_letterAlreadyExists() throws Exception {
        final String jurorNumber = "111111111";
        final String bureauOwner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", bureauOwner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/response");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        JurorPaperResponseDto requestDto = buildJurorPaperResponseDto();
        requestDto.setJurorNumber(jurorNumber);
        requestDto.setDateOfBirth(LocalDate.of(2022, 5, 3).minusYears(17));

        RequestEntity<JurorPaperResponseDto> requestEntity =
            new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SaveJurorPaperReplyResponseDto> response =
            template.exchange(requestEntity, SaveJurorPaperReplyResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        SaveJurorPaperReplyResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.isStraightThroughAcceptance()).isFalse();

        PaperResponse jurorPaperResponse = getJurorPaperResponse(jurorNumber);

        verifyRequestDtoMapping_personalDetails(jurorPaperResponse, requestDto);
        verifyRequestDtoMapping_contactDetails(jurorPaperResponse, requestDto);

        Optional<JurorPool> jurorPoolOpt =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber, POOL_411220502, true);

        assertThat(jurorPoolOpt.isPresent()).isTrue();
        JurorPool jurorPool = jurorPoolOpt.get();
        validateMergedJurorRecord(jurorPool, jurorPaperResponse, IJurorStatus.DISQUALIFIED);
        verifyAgeDisqualification(jurorPool);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<JurorHistory> jurorHistoryList =
            jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(jurorNumber, yesterday);
        assertThat(
            jurorHistoryList.stream().anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.DISQUALIFY_POOL_MEMBER)))
            .as("Expect history record to be created for juror disqualification")
            .isTrue();
        assertThat(
            jurorHistoryList.stream().anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.WITHDRAWAL_LETTER)))
            .as("Expect history record to be created for disqualification letter")
            .isTrue();

        Iterable<DisqualificationLetterMod> disqualifyLetterIterator = disqualifyLetterRepository.findAll();
        List<DisqualificationLetterMod> disqualificationLetters = new ArrayList<>();
        disqualifyLetterIterator.forEach(disqualificationLetters::add);

        assertThat(disqualificationLetters.size())
            .as("Expect a single disqualification letter to exist (existing record updated)")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void retrieveJurorById_bureauUser_happyPath() throws Exception {
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JurorPaperResponseDetailDto responseDetailDto = response.getBody();
        assertThat(responseDetailDto).isNotNull();

        verifyResponseDtoMapping(responseDetailDto, owner);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse_summonsSnapshot.sql"})
    public void retrieveJurorById_bureauUser_happyPath_summonsSnapshot() throws Exception {
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/222222222");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JurorPaperResponseDetailDto responseDetailDto = response.getBody();
        assertThat(responseDetailDto).isNotNull();

        verifyResponseDtoMapping(responseDetailDto, owner);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse_summonsSnapshot.sql"})
    public void retrieveJurorById_bureauUser_happyPath_summonsSnapshot_welsh() throws Exception {
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JurorPaperResponseDetailDto responseDetailDto = response.getBody();
        assertThat(responseDetailDto).isNotNull();

        verifyResponseDtoMapping(responseDetailDto, owner);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void retrieveJurorById_courtUser_happyPath() throws Exception {
        final String owner = "415";
        final String bureauJwt = createBureauJwt("COURT_USER", owner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JurorPaperResponseDetailDto responseDetailDto = response.getBody();
        assertThat(responseDetailDto).isNotNull();

        verifyResponseDtoMapping(responseDetailDto, owner);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse_summonsSnapshot.sql"})
    public void retrieveJurorById_courtUser_happyPath_summonsSnapshot() throws Exception {
        final String owner = "435";
        final String bureauJwt = createBureauJwt("COURT_USER", owner);
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/333333333");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JurorPaperResponseDetailDto responseDetailDto = response.getBody();
        assertThat(responseDetailDto).isNotNull();

        verifyResponseDtoMapping(responseDetailDto, owner);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void retrieveJurorById_courtUser_noAccess() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "416");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void retrieveJurorById_bureauUser_noResponseFound() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void retrieveJurorById_bureauUser_noPoolMember() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/222222222");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<JurorPaperResponseDetailDto> response = template.exchange(requestEntity,
            JurorPaperResponseDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_happyPath() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/111111111/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        Optional<JurorPool> jurorPoolOpt =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive("111111111", POOL_411220502, true);

        assertThat(jurorPoolOpt.isPresent()).isTrue();
        JurorPool jurorPool = jurorPoolOpt.get();

        PaperResponse summonsReplyData = jurorPaperResponseRepository.findByJurorNumber("111111111");

        validateMergedJurorRecord(jurorPool, summonsReplyData, IJurorStatus.RESPONDED);
        assertThat(jurorPool.getUserEdtq()).isEqualToIgnoringCase("BUREAU_USER");

        Juror juror = jurorPool.getJuror();
        // Summons reply was not completed by a third party so contact details should be merged
        validateMergedJurorRecordContactDetails(juror, summonsReplyData);
        // no mobile number present so numbers should be mapped to home phone and work phone only
        assertThat(juror.getAltPhoneNumber()).isNull();

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_awaitingTranslation_bureauUser_happyPath() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/111111111/AWAITING_TRANSLATION");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        PaperResponse summonsReplyData = jurorPaperResponseRepository.findByJurorNumber(
            "111111111");
        assertThat(summonsReplyData.getProcessingStatus()).isEqualTo(ProcessingStatus.AWAITING_TRANSLATION);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_firstName() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666601/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_awaitingTranslation_courtUser_happyPath() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666666/AWAITING_TRANSLATION");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        PaperResponse summonsReplyData = jurorPaperResponseRepository.findByJurorNumber("666666666");
        assertThat(summonsReplyData.getProcessingStatus()).isEqualTo(ProcessingStatus.AWAITING_TRANSLATION);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_lastName() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666602/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);


    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_dob() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666603/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_address() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666604/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_address4() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666605/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_postcode() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666606/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_bail() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666607/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_convictions() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666608/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_mentalHealthAct() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666609/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_mentalHealthCapacity() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666610/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_residency() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666611/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_missing_signature() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "411");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/666666612/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //        final String courtJwt = createBureauJwt("COURT_USER", "415");
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_eligibility_bureauUser_bureauOwner_happy() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/eligibility");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();
        // set up values different to what is in the current juror record
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(false)
            .onBail(true)
            .convicted(true)
            .mentalHealthAct(true)
            .mentalHealthCapacity(true)
            .build();
        eligibilityDetailsDto.setEligibility(eligibility);

        RequestEntity<EligibilityDetailsDto> requestEntity = new RequestEntity<>(eligibilityDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        PaperResponse jurorPaperResponseOpt = jurorPaperResponseRepository.findByJurorNumber("123456789");
        assertThat(jurorPaperResponseOpt).isNotNull();

        validateUpdatedPaperResponseEligibilityDetails(jurorPaperResponseOpt, eligibility);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_eligibility_courtUser_courtOwner_happy() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/eligibility");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();
        // set up values different to what is in the current juror record
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(false)
            .onBail(true)
            .convicted(true)
            .mentalHealthAct(true)
            .mentalHealthCapacity(true)
            .build();
        eligibilityDetailsDto.setEligibility(eligibility);

        RequestEntity<EligibilityDetailsDto> requestEntity = new RequestEntity<>(eligibilityDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        PaperResponse jurorPaperResponseOpt = jurorPaperResponseRepository.findByJurorNumber("987654321");
        assertThat(jurorPaperResponseOpt).isNotNull();

        validateUpdatedPaperResponseEligibilityDetails(jurorPaperResponseOpt, eligibility);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_eligibility_bureauUser_courtOwner() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/eligibility");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();
        // set up values different to what is in the current juror record
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(false)
            .onBail(true)
            .convicted(true)
            .mentalHealthAct(true)
            .mentalHealthCapacity(true)
            .build();
        eligibilityDetailsDto.setEligibility(eligibility);

        RequestEntity<EligibilityDetailsDto> requestEntity = new RequestEntity<>(eligibilityDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_eligibility_courtUser_noAccess_courtOwner() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "416");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/eligibility");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();
        // set up values different to what is in the current juror record
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(false)
            .onBail(true)
            .convicted(true)
            .mentalHealthAct(true)
            .mentalHealthCapacity(true)
            .build();
        eligibilityDetailsDto.setEligibility(eligibility);

        RequestEntity<EligibilityDetailsDto> requestEntity = new RequestEntity<>(eligibilityDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_eligibility_courtUser_bureauOwner() throws Exception {
        final String bureauJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/eligibility");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();
        // set up values different to what is in the current juror record
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(false)
            .onBail(true)
            .convicted(true)
            .mentalHealthAct(true)
            .mentalHealthCapacity(true)
            .build();
        eligibilityDetailsDto.setEligibility(eligibility);

        RequestEntity<EligibilityDetailsDto> requestEntity = new RequestEntity<>(eligibilityDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_bureauUser_eligibility_null() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/eligibility");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();

        eligibilityDetailsDto.setEligibility(null);

        RequestEntity<EligibilityDetailsDto> requestEntity = new RequestEntity<>(eligibilityDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_bureauUser_reasonableAdjustments_invalidAssistanceType() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.ReasonableAdjustment> reasonableAdjustmentList = new ArrayList<>();

        JurorPaperResponseDto.ReasonableAdjustment reasonableAdjustment =
            JurorPaperResponseDto.ReasonableAdjustment.builder()
                .assistanceType("Z")
                .assistanceTypeDetails("Some details on type Z")
                .build();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        reasonableAdjustmentList.add(reasonableAdjustment);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(reasonableAdjustmentList);

        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_bureauUser_reasonableAdjustments_duplicateAssistanceType() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeedList = new ArrayList<>();

        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("V")
            .assistanceTypeDetails("Some details on type V")
            .build();
        specialNeedList.add(specialNeed);
        specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("V")
            .assistanceTypeDetails("Some details on type V")
            .build();
        specialNeedList.add(specialNeed);
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(specialNeedList);

        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_reasonableAdjustments_bureauUser_bureauOwner_happy() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.ReasonableAdjustment> reasonableAdjustmentList = new ArrayList<>();
        // create a different reasonable adjustment to the one in the database
        JurorPaperResponseDto.ReasonableAdjustment reasonableAdjustment =
            JurorPaperResponseDto.ReasonableAdjustment.builder()
                .assistanceType("M")
                .assistanceTypeDetails("A number of adjustments")
                .build();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        reasonableAdjustmentList.add(reasonableAdjustment);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(reasonableAdjustmentList);

        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        List<JurorReasonableAdjustment> reasonableAdjustments = jurorReasonableAdjustmentRepository
            .findByJurorNumber("123456789");
        assertThat(reasonableAdjustments.size()).isEqualTo(1);  // we need a record to be present

        JurorReasonableAdjustment specialNeedDB = reasonableAdjustments.get(0);
        assertThat(specialNeedDB.getReasonableAdjustment().getCode()).isEqualTo(
            reasonableAdjustment.getAssistanceType());
        assertThat(specialNeedDB.getReasonableAdjustmentDetail()).isEqualTo(
            reasonableAdjustment.getAssistanceTypeDetails());

    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponseEmptyReasonableAdjustmentsBureauUserBureauOwnerHappy() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/121314151/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();

        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        List<JurorReasonableAdjustment> reasonableAdjustments = jurorReasonableAdjustmentRepository
            .findByJurorNumber("121314151");
        Juror juror = jurorRepository.findByJurorNumber("121314151");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(reasonableAdjustments.size()).isEqualTo(0);  // we need a record to be present
        assertNull(juror.getReasonableAdjustmentCode());
        assertNull(juror.getReasonableAdjustmentMessage());
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_reasonableAdjustments_courtUser_courtOwner_happy() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeedList = new ArrayList<>();
        // create a different special need to the one in the database
        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("M")
            .assistanceTypeDetails("A number of adjustments")
            .build();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        specialNeedList.add(specialNeed);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(specialNeedList);

        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        List<JurorReasonableAdjustment> reasonableAdjustmentList = jurorReasonableAdjustmentRepository
            .findByJurorNumber("987654321");
        assertThat(reasonableAdjustmentList.size()).isEqualTo(1);  // we need a record to be present

        JurorReasonableAdjustment reasonableAdjustmentDB = reasonableAdjustmentList.get(0);
        assertThat(reasonableAdjustmentDB.getReasonableAdjustment().getCode()).isEqualTo(
            specialNeed.getAssistanceType());
        assertThat(reasonableAdjustmentDB.getReasonableAdjustmentDetail()).isEqualTo(
            specialNeed.getAssistanceTypeDetails());

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_reasonableAdjustments_bureauUser_courtOwner() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeedList = new ArrayList<>();
        // create a different special need to the one in the database
        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("M")
            .assistanceTypeDetails("A number of adjustments")
            .build();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        specialNeedList.add(specialNeed);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(specialNeedList);

        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_reasonableAdjustments_courtUser_noAccess_courtOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "416");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeedList = new ArrayList<>();
        // create a different special need to the one in the database
        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("M")
            .assistanceTypeDetails("A number of adjustments")
            .build();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        specialNeedList.add(specialNeed);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(specialNeedList);
        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_reasonableAdjustments_courtUser_bureauOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/special-needs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeedList = new ArrayList<>();
        // create a different special need to the one in the database
        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("M")
            .assistanceTypeDetails("A number of adjustments")
            .build();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        specialNeedList.add(specialNeed);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(specialNeedList);

        RequestEntity<ReasonableAdjustmentDetailsDto> requestEntity = new RequestEntity<>(
            reasonableAdjustmentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_cjsEmployment_bureauUser_bureauOwner_happy() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/cjs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        // create a different CJS employer to the one in the database
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("National Crime Agency")
            .cjsEmployerDetails("Details of employment at NCA")
            .build();
        CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);

        RequestEntity<CjsEmploymentDetailsDto> requestEntity = new RequestEntity<>(cjsEmploymentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        List<JurorResponseCjsEmployment> cjsEmploymentListDB = jurorPaperResponseCjsRepository
            .findByJurorNumber("123456789");
        assertThat(cjsEmploymentListDB.size()).isEqualTo(1);  // we need a record to be present

        JurorResponseCjsEmployment cjsEmploymentDB = cjsEmploymentListDB.get(0);
        assertThat(cjsEmploymentDB.getCjsEmployer()).isEqualTo(cjsEmployment.getCjsEmployer());
        assertThat(cjsEmploymentDB.getCjsEmployerDetails()).isEqualTo(cjsEmployment.getCjsEmployerDetails());

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_cjsEmployment_courtUser_courtOwner_happy() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/cjs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        // create a different CJS employer to the one in the database
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("National Crime Agency")
            .cjsEmployerDetails("Details of employment at NCA")
            .build();
        CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);

        RequestEntity<CjsEmploymentDetailsDto> requestEntity = new RequestEntity<>(cjsEmploymentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        List<JurorResponseCjsEmployment> cjsEmploymentListDB = jurorPaperResponseCjsRepository
            .findByJurorNumber("987654321");
        assertThat(cjsEmploymentListDB.size()).isEqualTo(1);  // we need a record to be present

        JurorResponseCjsEmployment cjsEmploymentDB = cjsEmploymentListDB.get(0);
        assertThat(cjsEmploymentDB.getCjsEmployer()).isEqualTo(cjsEmployment.getCjsEmployer());
        assertThat(cjsEmploymentDB.getCjsEmployerDetails()).isEqualTo(cjsEmployment.getCjsEmployerDetails());

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_cjsEmployment_bureauUser_courtOwner() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/cjs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        // create a different CJS employer to the one in the database
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("National Crime Agency")
            .cjsEmployerDetails("Details of employment at NCA")
            .build();
        CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);

        RequestEntity<CjsEmploymentDetailsDto> requestEntity = new RequestEntity<>(cjsEmploymentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_cjsEmployment_courtUser_noAccess_courtOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "416");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/cjs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        // create a different CJS employer to the one in the database
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("National Crime Agency")
            .cjsEmployerDetails("Details of employment at NCA")
            .build();
        CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);

        RequestEntity<CjsEmploymentDetailsDto> requestEntity = new RequestEntity<>(cjsEmploymentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_cjsEmployment_courtUser_bureaOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/cjs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        // create a different CJS employer to the one in the database
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("National Crime Agency")
            .cjsEmployerDetails("Details of employment at NCA")
            .build();
        CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);

        RequestEntity<CjsEmploymentDetailsDto> requestEntity = new RequestEntity<>(cjsEmploymentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_bureauUser_cjsEmployment_invalidCjsEmployer() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/cjs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();

        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("!£$%^&*()_+")
            .cjsEmployerDetails("Details of an invalid CJS employment.")
            .build();
        CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);

        RequestEntity<CjsEmploymentDetailsDto> requestEntity = new RequestEntity<>(cjsEmploymentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_bureauUser_cjsEmployment_duplicateCjsEmployer() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/cjs");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();

        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("Police Force")
            .cjsEmployerDetails("Some test details")
            .build();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("Police Force")
            .cjsEmployerDetails("Some test details")
            .build();
        cjsEmploymentList.add(cjsEmployment);
        CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);

        RequestEntity<CjsEmploymentDetailsDto> requestEntity = new RequestEntity<>(cjsEmploymentDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_replyType_bureauUser_bureauOwner_happy() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/reply-type");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();
        // currently values are null in the database so set something non null
        replyTypeDetailsDto.setDeferral(true);
        replyTypeDetailsDto.setExcusal(false);

        RequestEntity<ReplyTypeDetailsDto> requestEntity = new RequestEntity<>(replyTypeDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber("123456789");
        assertThat(jurorPaperResponse).isNotNull();
        assertThat(jurorPaperResponse.getDeferral()).isEqualTo(replyTypeDetailsDto.getDeferral());
        assertThat(jurorPaperResponse.getExcusal()).isEqualTo(replyTypeDetailsDto.getExcusal());

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_replyType_courtUser_courtOwner_happy() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/reply-type");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();
        // currently values are null in the database so set something non null
        replyTypeDetailsDto.setDeferral(true);
        replyTypeDetailsDto.setExcusal(false);

        RequestEntity<ReplyTypeDetailsDto> requestEntity = new RequestEntity<>(replyTypeDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber("987654321");
        assertThat(jurorPaperResponse).isNotNull();
        assertThat(jurorPaperResponse.getDeferral()).isEqualTo(replyTypeDetailsDto.getDeferral());
        assertThat(jurorPaperResponse.getExcusal()).isEqualTo(replyTypeDetailsDto.getExcusal());

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_replyType_bureauUser_courtOwner() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/reply-type");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();
        // currently values are null in the database so set something non null
        replyTypeDetailsDto.setDeferral(true);
        replyTypeDetailsDto.setExcusal(false);

        RequestEntity<ReplyTypeDetailsDto> requestEntity = new RequestEntity<>(replyTypeDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_replyType_courtUser_noAccess_courtOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "416");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/reply-type");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();
        // currently values are null in the database so set something non null
        replyTypeDetailsDto.setDeferral(true);
        replyTypeDetailsDto.setExcusal(false);

        RequestEntity<ReplyTypeDetailsDto> requestEntity = new RequestEntity<>(replyTypeDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_replyType_courtUser_bureauOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/reply-type");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();
        // currently values are null in the database so set something non null
        replyTypeDetailsDto.setDeferral(true);
        replyTypeDetailsDto.setExcusal(false);

        RequestEntity<ReplyTypeDetailsDto> requestEntity = new RequestEntity<>(replyTypeDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_bureauUser_replyType_invalidReplyTypes() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/reply-type");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();

        replyTypeDetailsDto.setDeferral(true);
        replyTypeDetailsDto.setExcusal(true);

        RequestEntity<ReplyTypeDetailsDto> requestEntity = new RequestEntity<>(replyTypeDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_signature_bureauUser_bureauOwner_happy() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/signature");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        // currently value in the database is true so set to false as an update
        signatureDetailsDto.setSignature(false);

        RequestEntity<SignatureDetailsDto> requestEntity = new RequestEntity<>(signatureDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber("123456789");
        assertThat(jurorPaperResponse).isNotNull();
        assertThat(jurorPaperResponse.getSigned()).isEqualTo(signatureDetailsDto.getSignature());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_signature_courtUser_courtOwner_happy() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/signature");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        // currently value in the database is true so set to false as an update
        signatureDetailsDto.setSignature(false);

        RequestEntity<SignatureDetailsDto> requestEntity = new RequestEntity<>(signatureDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        PaperResponse jurorPaperResponse = jurorPaperResponseRepository.findByJurorNumber("987654321");
        assertThat(jurorPaperResponse).isNotNull();
        assertThat(jurorPaperResponse.getSigned()).isEqualTo(signatureDetailsDto.getSignature());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_signature_bureauUser_courtOwner() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/signature");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        // currently value in the database is true so set to false as an update
        signatureDetailsDto.setSignature(false);

        RequestEntity<SignatureDetailsDto> requestEntity = new RequestEntity<>(signatureDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_signature_courtUser_noAccess_courtOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "416");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/987654321/details/signature");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        // currently value in the database is true so set to false as an update
        signatureDetailsDto.setSignature(false);

        RequestEntity<SignatureDetailsDto> requestEntity = new RequestEntity<>(signatureDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    public void updatePaperResponse_signature_courtUser_bureauOwner() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/juror/123456789/details/signature");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        // currently value in the database is true so set to false as an update
        signatureDetailsDto.setSignature(false);

        RequestEntity<SignatureDetailsDto> requestEntity = new RequestEntity<>(signatureDetailsDto, httpHeaders,
            HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private void validateUpdatedPaperResponseEligibilityDetails(PaperResponse jurorPaperResponse,
                                                                JurorPaperResponseDto.Eligibility eligibility) {

        assertThat(jurorPaperResponse.getResidency()).isEqualTo(eligibility.getLivedConsecutive());
        assertThat(jurorPaperResponse.getMentalHealthAct()).isEqualTo(eligibility.getMentalHealthAct());
        assertThat(jurorPaperResponse.getMentalHealthCapacity()).isEqualTo(eligibility.getMentalHealthCapacity());
        assertThat(jurorPaperResponse.getBail()).isEqualTo(eligibility.getOnBail());
        assertThat(jurorPaperResponse.getConvictions()).isEqualTo(eligibility.getConvicted());

    }

    @Test
    @Sql(statements = "DELETE FROM JUROR_DIGITAL.PAPER_RESPONSE")
    public void updateJurorPaperResponseStatus_bureauUser_noResponseFound() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/999999990/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_alreadyProcessed() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/222222222/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_noJurorRecord() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/333333333/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_bureauUser_multipleJurorRecords() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/444444444/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_updatePaperResponseStatus.sql"})
    public void updateJurorPaperResponseStatus_courtUser_thirdParty() throws Exception {
        final String bureauJwt = initCourtsJwt("411", Arrays.asList("411", "774"));
        final URI uri = URI.create("/api/v1/moj/juror-paper-response/update-status/555555555/CLOSED");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        List<JurorPool> jurorPoolList =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive("555555555", true);

        assertThat(jurorPoolList.size()).isGreaterThan(0);
        JurorPool jurorPool = jurorPoolList.get(0);
        Juror juror = jurorPool.getJuror();

        PaperResponse summonsReplyData = jurorPaperResponseRepository.findByJurorNumber(
            "555555555");

        validateMergedJurorRecord(jurorPool, summonsReplyData, IJurorStatus.RESPONDED);
        assertThat(jurorPool.getUserEdtq()).isEqualToIgnoringCase("COURT_USER");

        // juror contact details are omitted from merge when completed by a third party
        assertThat(juror.getPhoneNumber()).isNull();
        assertThat(juror.getWorkPhone()).isNull();
        assertThat(juror.getAltPhoneNumber()).isNull();
        assertThat(juror.getEmail()).isNull();
    }

    private void verifyResponseDtoMapping(JurorPaperResponseDetailDto responseDetailDto, String owner) {
        PaperResponse jurorPaperResponse =
            jurorPaperResponseRepository.findByJurorNumber(responseDetailDto.getJurorNumber());
        assertThat(jurorPaperResponse).isNotNull();

        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository,
            responseDetailDto.getJurorNumber(), owner);

        assertThat(jurorPool).isNotNull();

        SummonsSnapshot summonsSnapshot = summonsSnapshotRepository.findById(jurorPool.getJurorNumber())
            .orElse(null);

        assertThat(responseDetailDto.getJurorNumber()).isEqualTo(jurorPool.getJurorNumber());
        if (summonsSnapshot != null) {
            assertThat(responseDetailDto.getPoolNumber()).isEqualTo(summonsSnapshot.getPoolNumber());
            assertThat(responseDetailDto.getServiceStartDate()).isEqualTo(summonsSnapshot.getServiceStartDate());
            assertThat(responseDetailDto.getCourtName()).isEqualTo(summonsSnapshot.getCourtLocationName());
            assertThat(responseDetailDto.isWelshCourt()).isEqualTo(
                CourtLocationUtils.isWelshCourtLocation(welshCourtLocationRepository,
                    summonsSnapshot.getCourtLocationCode()));
        } else {
            assertThat(responseDetailDto.getPoolNumber()).isEqualTo(jurorPool.getPoolNumber());
            assertThat(responseDetailDto.getServiceStartDate()).isEqualTo(jurorPool.getReturnDate());
            assertThat(responseDetailDto.getCourtName()).isEqualTo(jurorPool.getCourt().getName());
            assertThat(responseDetailDto.isWelshCourt()).isEqualTo(
                CourtLocationUtils.isWelshCourtLocation(welshCourtLocationRepository,
                    jurorPool.getCourt().getLocCode()));
        }
        assertThat(responseDetailDto.getJurorStatus()).isEqualTo(jurorPool.getStatus().getStatusDesc());

        assertThat(responseDetailDto.getTitle()).isEqualTo(jurorPaperResponse.getTitle());
        assertThat(responseDetailDto.getFirstName()).isEqualTo(jurorPaperResponse.getFirstName());
        assertThat(responseDetailDto.getLastName()).isEqualTo(jurorPaperResponse.getLastName());
        assertThat(responseDetailDto.getDateOfBirth()).isEqualTo(jurorPaperResponse.getDateOfBirth());
        assertThat(responseDetailDto.getPrimaryPhone()).isEqualTo(jurorPaperResponse.getPhoneNumber());
        assertThat(responseDetailDto.getSecondaryPhone()).isEqualTo(jurorPaperResponse.getAltPhoneNumber());
        assertThat(responseDetailDto.getEmailAddress()).isEqualTo(jurorPaperResponse.getEmail());

        assertThat(responseDetailDto.getAddressLineOne()).isEqualTo(jurorPaperResponse.getAddressLine1());
        assertThat(responseDetailDto.getAddressLineTwo()).isEqualTo(jurorPaperResponse.getAddressLine2());
        assertThat(responseDetailDto.getAddressLineThree()).isEqualTo(jurorPaperResponse.getAddressLine3());
        assertThat(responseDetailDto.getAddressTown()).isEqualTo(jurorPaperResponse.getAddressLine4());
        assertThat(responseDetailDto.getAddressCounty()).isEqualTo(jurorPaperResponse.getAddressLine5());
        assertThat(responseDetailDto.getAddressPostcode()).isEqualTo(jurorPaperResponse.getPostcode());

        List<JurorResponseCjsEmployment> cjsEmployments =
            jurorPaperResponseCjsRepository.findByJurorNumber(responseDetailDto.getJurorNumber());
        if (cjsEmployments != null && !cjsEmployments.isEmpty()) {
            JurorResponseCjsEmployment actualCjs = cjsEmployments.get(0);
            JurorPaperResponseDetailDto.CjsEmployment expectedCjs = responseDetailDto.getCjsEmployment().get(0);
            assertThat(expectedCjs.getCjsEmployer()).isEqualTo(actualCjs.getCjsEmployer());
            assertThat(expectedCjs.getCjsEmployerDetails()).isEqualTo(actualCjs.getCjsEmployerDetails());
        }

        List<JurorReasonableAdjustment> reasonableAdjustments =
            jurorReasonableAdjustmentRepository.findByJurorNumber(responseDetailDto.getJurorNumber());
        if (reasonableAdjustments != null && !reasonableAdjustments.isEmpty()) {
            JurorReasonableAdjustment actualReasonableAdjustment = reasonableAdjustments.get(0);
            JurorPaperResponseDetailDto.ReasonableAdjustment expectedSpecialNeeds =
                responseDetailDto.getReasonableAdjustments().get(0);
            assertThat(expectedSpecialNeeds.getAssistanceType()).isEqualTo(
                actualReasonableAdjustment.getReasonableAdjustment().getCode());
            assertThat(expectedSpecialNeeds.getAssistanceTypeDetails()).isEqualTo(
                actualReasonableAdjustment.getReasonableAdjustmentDetail());
        }

        assertThat(responseDetailDto.getDeferral()).isEqualTo(jurorPaperResponse.getDeferral());
        assertThat(responseDetailDto.getExcusal()).isEqualTo(jurorPaperResponse.getExcusal());

        if (jurorPaperResponse.getResidency() != null || jurorPaperResponse.getMentalHealthAct() != null
            || jurorPaperResponse.getMentalHealthCapacity() != null || jurorPaperResponse.getBail() != null
            || jurorPaperResponse.getConvictions() != null) {
            JurorPaperResponseDetailDto.Eligibility eligibility = responseDetailDto.getEligibility();
            assertThat(eligibility.getLivedConsecutive()).isEqualTo(jurorPaperResponse.getResidency());
            assertThat(eligibility.getMentalHealthAct()).isEqualTo(jurorPaperResponse.getMentalHealthAct());
            assertThat(eligibility.getMentalHealthCapacity()).isEqualTo(jurorPaperResponse.getMentalHealthCapacity());
            assertThat(eligibility.getOnBail()).isEqualTo(jurorPaperResponse.getBail());
            assertThat(eligibility.getConvicted()).isEqualTo(jurorPaperResponse.getConvictions());
        }

        assertThat(responseDetailDto.getSigned()).isEqualTo(jurorPaperResponse.getSigned());

        if (jurorPaperResponse.getRelationship() != null && !jurorPaperResponse.getRelationship().isEmpty()) {
            JurorPaperResponseDetailDto.ThirdParty thirdParty = responseDetailDto.getThirdParty();
            assertThat(thirdParty.getRelationship()).isEqualTo(jurorPaperResponse.getRelationship());
            assertThat(thirdParty.getThirdPartyReason()).isEqualTo(jurorPaperResponse.getThirdPartyReason());
        }

        assertThat(responseDetailDto.getWelsh()).isEqualTo(jurorPaperResponse.getWelsh());
        assertThat(responseDetailDto.getDateReceived()).isEqualTo(jurorPaperResponse.getDateReceived().toLocalDate());
        assertThat(responseDetailDto.getProcessingStatus()).isEqualTo(
            jurorPaperResponse.getProcessingStatus().getDescription());

        if (jurorPaperResponse.getRelationship() != null && !jurorPaperResponse.getRelationship().isEmpty()) {
            JurorPaperResponseDetailDto.ThirdParty thirdParty = responseDetailDto.getThirdParty();
            assertThat(thirdParty.getRelationship()).isEqualTo(jurorPaperResponse.getRelationship());
            assertThat(thirdParty.getThirdPartyReason()).isEqualTo(jurorPaperResponse.getThirdPartyReason());
        }

        Juror juror = jurorPool.getJuror();

        assertThat(responseDetailDto.getExistingTitle()).isEqualTo(juror.getTitle());
        assertThat(responseDetailDto.getExistingFirstName()).isEqualTo(juror.getFirstName());
        assertThat(responseDetailDto.getExistingLastName()).isEqualTo(juror.getLastName());
        assertThat(responseDetailDto.getExistingAddressLineOne()).isEqualTo(juror.getAddressLine1());
        assertThat(responseDetailDto.getExistingAddressLineTwo()).isEqualTo(juror.getAddressLine2());
        assertThat(responseDetailDto.getExistingAddressLineThree()).isEqualTo(juror.getAddressLine3());
        assertThat(responseDetailDto.getExistingAddressTown()).isEqualTo(juror.getAddressLine4());
        assertThat(responseDetailDto.getExistingAddressCounty()).isEqualTo(juror.getAddressLine5());
        assertThat(responseDetailDto.getExistingAddressPostcode()).isEqualTo(juror.getPostcode());
    }

    private PaperResponse getJurorPaperResponse(String jurorNumber) {
        PaperResponse jurorPaperResponse =
            jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        assert jurorPaperResponse != null;
        return jurorPaperResponse;
    }

    private void verifyRequestDtoMapping_personalDetails(PaperResponse jurorPaperResponse,
                                                         JurorPaperResponseDto requestDto) {
        assertThat(jurorPaperResponse.getJurorNumber()).isEqualTo(requestDto.getJurorNumber());
        assertThat(jurorPaperResponse.getDateReceived()).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));

        assertThat(jurorPaperResponse.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(jurorPaperResponse.getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(jurorPaperResponse.getLastName()).isEqualTo(requestDto.getLastName());
        assertThat(jurorPaperResponse.getDateOfBirth()).isEqualTo(requestDto.getDateOfBirth());
    }

    private void verifyRequestDtoMapping_contactDetails(PaperResponse jurorPaperResponse,
                                                        JurorPaperResponseDto requestDto) {
        assertThat(jurorPaperResponse.getAddressLine1()).isEqualTo(requestDto.getAddressLineOne());
        assertThat(jurorPaperResponse.getAddressLine2()).isEqualTo(requestDto.getAddressLineTwo());
        assertThat(jurorPaperResponse.getAddressLine3()).isEqualTo(requestDto.getAddressLineThree());
        assertThat(jurorPaperResponse.getAddressLine4()).isEqualTo(requestDto.getAddressTown());
        assertThat(jurorPaperResponse.getAddressLine5()).isEqualTo(requestDto.getAddressCounty());
        assertThat(jurorPaperResponse.getPostcode()).isEqualTo(requestDto.getAddressPostcode());

        assertThat(jurorPaperResponse.getEmail()).isEqualTo(requestDto.getEmailAddress());
        assertThat(jurorPaperResponse.getPhoneNumber()).isEqualTo(requestDto.getPrimaryPhone());
        assertThat(jurorPaperResponse.getAltPhoneNumber()).isEqualTo(requestDto.getSecondaryPhone());
    }

    private void verifyRequestDtoMapping(JurorPaperResponseDto requestDto) {
        PaperResponse jurorPaperResponse = getJurorPaperResponse(requestDto.getJurorNumber());

        verifyRequestDtoMapping_personalDetails(jurorPaperResponse, requestDto);
        verifyRequestDtoMapping_contactDetails(jurorPaperResponse, requestDto);

        JurorPaperResponseDto.ThirdParty thirdParty = requestDto.getThirdParty();
        if (requestDto.getThirdParty() != null) {
            assertThat(jurorPaperResponse.getRelationship()).isEqualTo(thirdParty.getRelationship());
            assertThat(jurorPaperResponse.getThirdPartyReason()).isEqualTo(thirdParty.getThirdPartyReason());
        } else {
            assertThat(jurorPaperResponse.getRelationship()).isNull();
            assertThat(jurorPaperResponse.getThirdPartyReason()).isNull();
        }

        JurorPaperResponseDto.Eligibility eligibility = requestDto.getEligibility();
        assertThat(jurorPaperResponse.getResidency()).isEqualTo(eligibility.getLivedConsecutive());
        assertThat(jurorPaperResponse.getMentalHealthAct()).isEqualTo(eligibility.getMentalHealthAct());
        assertThat(jurorPaperResponse.getMentalHealthCapacity()).isEqualTo(eligibility.getMentalHealthCapacity());
        assertThat(jurorPaperResponse.getBail()).isEqualTo(eligibility.getOnBail());
        assertThat(jurorPaperResponse.getConvictions()).isEqualTo(eligibility.getConvicted());

        assertThat(jurorPaperResponse.getExcusal()).isEqualTo(requestDto.getExcusal());
        assertThat(jurorPaperResponse.getDeferral()).isEqualTo(requestDto.getDeferral());
        assertThat(jurorPaperResponse.getSigned()).isEqualTo(requestDto.getSigned());

        List<JurorResponseCjsEmployment> cjsEmployments =
            jurorPaperResponseCjsRepository.findByJurorNumber(requestDto.getJurorNumber());
        if (cjsEmployments.size() == 1) {
            JurorResponseCjsEmployment actualCjsEmployment = cjsEmployments.get(0);
            JurorPaperResponseDto.CjsEmployment expectedCjsEmployment = requestDto.getCjsEmployment().get(0);
            assertThat(actualCjsEmployment.getCjsEmployer()).isEqualTo(expectedCjsEmployment.getCjsEmployer());
            assertThat(actualCjsEmployment.getCjsEmployerDetails()).isEqualTo(
                expectedCjsEmployment.getCjsEmployerDetails());
        }
        List<JurorReasonableAdjustment> specialNeeds =
            jurorReasonableAdjustmentRepository.findByJurorNumber(requestDto.getJurorNumber());
        if (specialNeeds.size() == 1) {
            JurorReasonableAdjustment actualSpecialNeed = specialNeeds.get(0);
            JurorPaperResponseDto.ReasonableAdjustment expectedSpecialNeed =
                requestDto.getReasonableAdjustments().get(0);
            assertThat(actualSpecialNeed.getReasonableAdjustment().getCode()).isEqualTo(
                expectedSpecialNeed.getAssistanceType());
            assertThat(actualSpecialNeed.getReasonableAdjustmentDetail()).isEqualTo(
                expectedSpecialNeed.getAssistanceTypeDetails());
        }
    }

    private JurorPaperResponseDto buildJurorPaperResponseDto() {
        JurorPaperResponseDto jurorPaperResponseDto = buildBasicJurorPaperResponseDto();

        setEligibilityDetails(jurorPaperResponseDto);
        jurorPaperResponseDto.setCanServeOnSummonsDate(true);
        jurorPaperResponseDto.setSigned(true);

        return jurorPaperResponseDto;
    }

    private JurorPaperResponseDto buildBasicJurorPaperResponseDto() {
        JurorPaperResponseDto jurorPaperResponseDto = new JurorPaperResponseDto();
        jurorPaperResponseDto.setJurorNumber("123456789");

        jurorPaperResponseDto.setTitle("Mr");
        jurorPaperResponseDto.setFirstName("Test");
        jurorPaperResponseDto.setLastName("Person");
        jurorPaperResponseDto.setDateOfBirth(LocalDate.now().minusYears(25));

        jurorPaperResponseDto.setPrimaryPhone("01234567890");
        jurorPaperResponseDto.setSecondaryPhone("07123456789");
        jurorPaperResponseDto.setEmailAddress("email@address.com");

        setAddressDetails(jurorPaperResponseDto);

        return jurorPaperResponseDto;
    }

    private void setAddressDetails(JurorPaperResponseDto jurorPaperResponseDto) {
        jurorPaperResponseDto.setAddressLineOne("Address Line 1");
        jurorPaperResponseDto.setAddressLineTwo("Address Line 2");
        jurorPaperResponseDto.setAddressLineThree("Address Line 3");
        jurorPaperResponseDto.setAddressTown("Some Town");
        jurorPaperResponseDto.setAddressCounty("Some County");
        jurorPaperResponseDto.setAddressPostcode("CH1 2AN");
    }

    private JurorPaperResponseDto.CjsEmployment buildCjsEmployment(String employerName) {
        return JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer(employerName)
            .cjsEmployerDetails("Some test details")
            .build();
    }

    private JurorPaperResponseDto.ReasonableAdjustment buildSpecialNeeds(String assistanceType) {
        return JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType(assistanceType)
            .assistanceTypeDetails("Some test details")
            .build();
    }

    private void setEligibilityDetails(JurorPaperResponseDto jurorPaperResponseDto) {
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(true)
            .mentalHealthAct(false)
            .mentalHealthCapacity(false)
            .onBail(false)
            .convicted(false)
            .build();

        jurorPaperResponseDto.setEligibility(eligibility);
    }

    private void setThirdPartyDetails(JurorPaperResponseDto jurorPaperResponseDto) {
        JurorPaperResponseDto.ThirdParty thirdParty = JurorPaperResponseDto.ThirdParty.builder()
            .relationship("Spouse")
            .thirdPartyReason("Some test reason")
            .build();

        jurorPaperResponseDto.setThirdParty(thirdParty);
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

    private void validateMergedJurorRecord(JurorPool jurorPool, PaperResponse summonsReplyData,
                                           long statusCode) {
        Juror juror = jurorPool.getJuror();
        assertThat(juror.getPendingTitle()).isEqualToIgnoringCase(summonsReplyData.getTitle());
        assertThat(juror.getPendingFirstName()).isEqualToIgnoringCase(summonsReplyData.getFirstName());
        assertThat(juror.getPendingLastName()).isEqualToIgnoringCase(summonsReplyData.getLastName());

        assertThat(juror.getAddressLine1()).isEqualToIgnoringCase(summonsReplyData.getAddressLine1());
        assertThat(juror.getAddressLine2()).isEqualToIgnoringCase(summonsReplyData.getAddressLine2());
        assertThat(juror.getAddressLine3()).isEqualToIgnoringCase(summonsReplyData.getAddressLine3());
        assertThat(juror.getAddressLine4()).isEqualToIgnoringCase(summonsReplyData.getAddressLine4());
        assertThat(juror.getAddressLine5()).isEqualToIgnoringCase(summonsReplyData.getAddressLine5());
        assertThat(juror.getPostcode()).isEqualToIgnoringCase(summonsReplyData.getPostcode());

        assertThat(juror.isResponded()).isTrue();
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(statusCode);

        if (summonsReplyData.getWelsh()) {
            assertThat(juror.getWelsh()).isTrue();
        } else {
            assertThat(juror.getWelsh()).isNull();
        }
    }

    private void validateMergedJurorRecordContactDetails(Juror juror, PaperResponse summonsReplyData) {
        assertThat(juror.getPhoneNumber()).isEqualToIgnoringCase(summonsReplyData.getPhoneNumber());
        assertThat(juror.getWorkPhone()).isEqualToIgnoringCase(summonsReplyData.getAltPhoneNumber());
        assertThat(juror.getEmail()).isEqualToIgnoringCase(summonsReplyData.getEmail());
    }

    private void verifyAgeDisqualification(JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();
        assertThat(juror.isResponded())
            .as("Juror record should be updated and marked as responded")
            .isTrue();
        assertThat(juror.getDisqualifyDate())
            .as("Juror record should be updated with a disqualified date")
            .isNotNull();
        assertThat(juror.getDisqualifyCode())
            .as("Juror record should be updated with a disqualification code")
            .isEqualTo(DisqualifyCode.A.toString());
        assertThat(jurorPool.getNextDate())
            .as("Juror record is no longer due to attend, expect NEXT_DATE to be null")
            .isNull();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
            juror.getJurorNumber(), yesterday);
        assertThat(
            jurorHistoryList.stream().anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.DISQUALIFY_POOL_MEMBER)))
            .as("Expect history record to be created for juror disqualification")
            .isTrue();
        assertThat(
            jurorHistoryList.stream().anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.WITHDRAWAL_LETTER)))
            .as("Expect history record to be created for disqualification letter")
            .isTrue();

        Iterable<DisqualificationLetterMod> disqualifyLetterIterator = disqualifyLetterRepository.findAll();
        List<DisqualificationLetterMod> disqualificationLetters = new ArrayList<>();
        disqualifyLetterIterator.forEach(disqualificationLetters::add);

        assertThat(disqualificationLetters.size())
            .as("Expect a single disqualification letter to exist (existing record updated)")
            .isEqualTo(1);
    }

    private void verifyStraightThrough_ageDisqualification_notProcessed(PaperResponse jurorPaperResponse,
                                                                        JurorPool jurorPool, int statusCode) {
        Juror juror = jurorPool.getJuror();
        assertThat(jurorPaperResponse.getProcessingComplete())
            .as("No automatic processing, so processing complete flag remains unset")
            .isNotEqualTo(Boolean.TRUE);
        assertThat(jurorPaperResponse.getCompletedAt())
            .as("No automatic processing, so completed date remains unset")
            .isNull();
        assertThat(jurorPaperResponse.getProcessingStatus())
            .as("No automatic processing, so processing status remains as To Do")
            .isEqualTo(ProcessingStatus.TODO);

        if (statusCode != IJurorStatus.RESPONDED) {
            assertThat(juror.isResponded())
                .as("No automatic processing, so juror record is not set to responded")
                .isFalse();
        }
        assertThat(juror.getDisqualifyDate())
            .as("No automatic processing, so disqualification date remains unset")
            .isNull();
        assertThat(juror.getDisqualifyCode())
            .as("No automatic processing, so disqualification code remains unset")
            .isNull();
        assertThat(jurorPool.getStatus().getStatus())
            .as("No automatic processing, so status remains unchanged")
            .isEqualTo(statusCode);
        assertThat(jurorPool.getNextDate())
            .as("No automatic processing, so next date remains set")
            .isNotNull();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
            juror.getJurorNumber(), yesterday);
        assertThat(
            jurorHistoryList.stream().anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.DISQUALIFY_POOL_MEMBER)))
            .as("Expect no history record to be created for juror disqualification")
            .isFalse();
        assertThat(
            jurorHistoryList.stream().anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.WITHDRAWAL_LETTER)))
            .as("Expect no history record to be created for disqualification letter")
            .isFalse();

        Iterable<DisqualificationLetterMod> disqualifyLetterIterator = disqualifyLetterRepository.findAll();
        List<DisqualificationLetterMod> disqualificationLetters = new ArrayList<>();
        disqualifyLetterIterator.forEach(disqualificationLetters::add);

        assertThat(disqualificationLetters.size())
            .as("No disqualification letter expected to be generated")
            .isEqualTo(0);
    }

}
