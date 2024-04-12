package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.CreatePanelDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NO_PANEL_EXIST;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_AVAILABLE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_LIMITS;


@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PanelControllerITest extends AbstractIntegrationTest {

    static final String COURT_USER_NAME = "court_user";
    static final String CHESTER_LOC_CODE = "415";

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Autowired
    private PanelRepository panelRepository;

    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    @Autowired
    private AppearanceRepository appearanceRepository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE,
            CHESTER_LOC_CODE, "462", "767"));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void createPanelCourtUserAllPools() {
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(makeCreatePanelDto(null), httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/create-panel"));

        ResponseEntity<PanelListDto[]> responseEntity =
            restTemplate.exchange(requestEntity, PanelListDto[].class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be ok")
            .isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.getBody()).isNotNull();

        assertThat(responseEntity.getBody().length)
            .as("Expected Length to be 13")
            .isEqualTo(13);

        for (PanelListDto dto : responseEntity.getBody()) {
            assertThat(dto.getJurorStatus())
                .as("Expect the status to be panelled")
                .isEqualTo("Panelled");
            assertThat(dto.getFirstName())
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void createPanelCourtUserJurorsExceedLimit() {
        CreatePanelDto createPanelDto = makeCreatePanelDto(null);
        createPanelDto.setNumberRequested(1001);
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/create-panel"));

        ResponseEntity<String> response =
            restTemplate.exchange(requestEntity, String.class);

        assertBusinessRuleViolation(response, "Cannot create panel - "
                + "Number requested must be between 1 and 1000",
            NUMBER_OF_JURORS_EXCEEDS_LIMITS);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void createPanelCourtUserNotEnoughJurors() {
        CreatePanelDto createPanelDto = makeCreatePanelDto(new ArrayList<>());
        createPanelDto.setNumberRequested(45);
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/create-panel"));

        ResponseEntity<String> response =
            restTemplate.exchange(requestEntity, String.class);

        assertBusinessRuleViolation(response, "Cannot create panel - Not enough jurors available",
            NUMBER_OF_JURORS_EXCEEDS_AVAILABLE);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void createPanelCourtUserAllPoolsWrongTrialNumber() {
        CreatePanelDto dto = makeCreatePanelDto(null);
        dto.setTrialNumber("T2");
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/create-panel"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be not found")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void createPanelCourtUserSelectedPool() {
        List<String> poolNumbers = new ArrayList<>();
        poolNumbers.add("415231104");

        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(makeCreatePanelDto(poolNumbers), httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/create-panel"));

        ResponseEntity<PanelListDto[]> responseEntity =
            restTemplate.exchange(requestEntity, PanelListDto[].class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be ok")
            .isEqualTo(HttpStatus.OK);

        assert responseEntity.getBody() != null;

        assertThat(responseEntity.getBody().length)
            .as("Expected Length to be 13")
            .isEqualTo(13);

        for (PanelListDto dto : responseEntity.getBody()) {
            assertThat(dto.getJurorStatus())
                .as("Expect the status to be panelled")
                .isEqualTo("Panelled");
            assertThat(dto.getFirstName())
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void createPanelBureauUserAllPools() {
        initHeadersBureau();
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(makeCreatePanelDto(null), httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/create-panel"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void requestEmpanelCourtUser() {
        initHeadersCourt();
        RequestEntity<Void> requestEntity =
            new RequestEntity<>(
                httpHeaders,
                HttpMethod.GET,
                URI.create("/api/v1/moj/trial/panel/request-empanel?number_requested=3&trial_number=T10000001"
                    + "&court_location_code=415"));

        ResponseEntity<EmpanelListDto> responseEntity =
            restTemplate.exchange(requestEntity, EmpanelListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be ok")
            .isEqualTo(HttpStatus.OK);

        EmpanelListDto responseBody = responseEntity.getBody();
        assert responseBody != null;

        assertThat(responseBody.getTotalJurorsForEmpanel())
            .as("Expected total jurors to be 3")
            .isEqualTo(3);
        assertThat(responseBody.getEmpanelList().stream().filter(list ->
            "Responded".equalsIgnoreCase(list.getStatus())))
            .as("Expected size to be three (responded)")
            .hasSize(3);
        assertThat(responseBody.getEmpanelList().stream().filter(list ->
            "Panel".equalsIgnoreCase(list.getStatus())))
            .as("Expected size to be one (panel)")
            .hasSize(1);
        assertThat(responseBody.getEmpanelList().stream().filter(list ->
            "Juror".equalsIgnoreCase(list.getStatus())))
            .as("Expected size to be one (juror)")
            .hasSize(1);

        for (EmpanelDetailsDto dto : responseBody.getEmpanelList()) {
            assertThat(dto.getFirstName())
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void requestEmpanelBureauUser() {
        initHeadersBureau();
        RequestEntity<Void> requestEntity =
            new RequestEntity<>(
                httpHeaders,
                HttpMethod.GET,
                URI.create("/api/v1/moj/trial/panel/request-empanel?number_requested=3&trial_number=T100000000"
                    + "&court_location_code=415"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void panelSummary() {
        RequestEntity<PanelListDto[]> requestEntity =
            new RequestEntity<>(
                httpHeaders,
                HttpMethod.GET,
                URI.create("/api/v1/moj/trial/panel/list?trial_number=T10000000&court_location_code=415"));

        ResponseEntity<PanelListDto[]> responseEntity =
            restTemplate.exchange(requestEntity, PanelListDto[].class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be ok")
            .isEqualTo(HttpStatus.OK);

        assert responseEntity.getBody() != null;

        assertThat(responseEntity.getBody().length)
            .as("Expected length to be 13")
            .isEqualTo(13);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void empanelJurorsCourtUserHappyPath() {
        JurorListRequestDto dto = createEmpanelledListRequestDto();
        RequestEntity<JurorListRequestDto> requestEntity =
            new RequestEntity<>(dto,
                httpHeaders,
                HttpMethod.POST,
                URI.create("/api/v1/moj/trial/panel/process-empanelled"));

        ResponseEntity<PanelListDto[]> responseEntity =
            restTemplate.exchange(requestEntity, PanelListDto[].class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be ok")
            .isEqualTo(HttpStatus.OK);

        assert responseEntity.getBody() != null;
        assertThat(responseEntity.getBody().length).as("Expected length to be 4").isEqualTo(4);
        for (PanelListDto panelListDto : responseEntity.getBody()) {
            assertThat(panelListDto.getJurorStatus())
                .as("Expect the status to be Juror")
                .isEqualTo("Juror");
        }

        List<Panel> panel = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(dto.getTrialNumber(),
            dto.getCourtLocationCode());

        panel = panel.stream().filter(p -> p.getResult() != null).collect(Collectors.toList());

        int jurorCount = 0;
        int notUsedCount = 0;
        int challengedCount = 0;

        for (Panel panelMember : panel) {
            assertThat(panelMember.getResult()).as("Result should not be null").isNotNull();
            switch (panelMember.getResult()) {
                case NOT_USED -> {
                    validateNotUsedChallengedHistory(panelMember);
                    notUsedCount++;
                }
                case CHALLENGED -> {
                    validateNotUsedChallengedHistory(panelMember);
                    challengedCount++;
                }
                case JUROR -> {
                    List<JurorHistory> jurorHistories =
                        jurorHistoryRepository.findByJurorNumber(panelMember.getJurorPool().getJurorNumber());
                    assertThat(jurorHistories.size()).as("Expected history items to be one").isEqualTo(1);
                    assertThat(jurorHistories.get(0).getHistoryCode().getCode()).as(
                            "Expected history code to be TADD")
                        .isEqualTo("TADD");
                    jurorCount++;
                }
                default -> throw new IllegalStateException();
            }
        }

        assertThat(jurorCount).as("Expected total jurors on a jury to be 4").isEqualTo(4);
        assertThat(notUsedCount).as("Expected total not used jurors to be 4").isEqualTo(4);
        assertThat(challengedCount).as("Expected total challenged jurors to be 4").isEqualTo(4);

    }

    private void validateNotUsedChallengedHistory(Panel panelMember) {
        List<JurorHistory> jurorHistories =
            jurorHistoryRepository.findByJurorNumber(panelMember.getJurorPool().getJurorNumber());
        assertThat(jurorHistories.size()).as("Expected history items to be one").isEqualTo(1);
        assertThat(jurorHistories.get(0).getHistoryCode().getCode()).as(
                "Expected history code to be VRET")
            .isEqualTo("VRET");
        Appearance appearance =
            appearanceRepository.findByJurorNumber(panelMember.getJurorPool().getJurorNumber());
        assertThat(appearance.getPoolNumber())
            .as("Expected value to be the current juror's pool number")
            .isEqualTo(panelMember.getJurorPool().getPoolNumber());
        assertThat(appearance.getTrialNumber())
            .as("Expected trial number value to be null")
            .isNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void empanelJurorsBureauUser() {
        initHeadersBureau();
        RequestEntity<JurorListRequestDto> requestEntity =
            new RequestEntity<>(
                createEmpanelledListRequestDto(),
                httpHeaders,
                HttpMethod.POST,
                URI.create("/api/v1/moj/trial/panel/process-empanelled"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void empanelJurorsNoResultSet() {

        JurorListRequestDto dto = createEmpanelledListRequestDto();
        dto.getJurors().get(0).setResult(null);

        RequestEntity<JurorListRequestDto> requestEntity =
            new RequestEntity<>(
                dto,
                httpHeaders,
                HttpMethod.POST,
                URI.create("/api/v1/moj/trial/panel/process-empanelled"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be bad request")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void availableJurors() {
        RequestEntity<Void> requestEntity = new RequestEntity<>(
            httpHeaders,
            HttpMethod.GET,
            URI.create("/api/v1/moj/trial/panel/available-jurors?court_location_code=415")
        );

        ResponseEntity<PanelListDto[]> responseEntity =
            restTemplate.exchange(requestEntity, PanelListDto[].class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be ok")
            .isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void availableJurorsBureauUser() {
        initHeadersBureau();
        RequestEntity<Void> requestEntity = new RequestEntity<>(
            httpHeaders,
            HttpMethod.GET,
            URI.create("/api/v1/moj/trial/panel/available-jurors?court_location_code=415")
        );

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expected status code to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);


    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void addPanelMembersNoPool() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        CreatePanelDto createPanelDto = makeCreatePanelDto(null);
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<PanelListDto[]> responseEntity =
            restTemplate.exchange(requestEntity, PanelListDto[].class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
            .isNotNull();

        PanelListDto[] panelListDtos = responseEntity.getBody();
        assertThat(panelListDtos.length).as("Total added members").isEqualTo(13);

        for (PanelListDto dto : responseEntity.getBody()) {
            assertThat(dto.getJurorStatus())
                .as("Expect the status to be panelled")
                .isEqualTo("Panelled");
            assertThat(dto.getFirstName())
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
        }

        assertThat(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(createPanelDto.getTrialNumber(),
            createPanelDto.getCourtLocationCode()).size()).as("Total members").isEqualTo(26);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void addPanelMembersPoolProvided() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);

        final int numberRequested = 2;
        CreatePanelDto createPanelDto = makeCreatePanelDto(Collections.singletonList("415231105"));
        createPanelDto.setNumberRequested(numberRequested);

        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto,
            httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<PanelListDto[]> responseEntity =
            restTemplate.exchange(requestEntity, PanelListDto[].class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
            .isNotNull();

        PanelListDto[] panelListDtos = responseEntity.getBody();
        assert panelListDtos != null;

        assertThat(panelListDtos.length).as("Total added members").isEqualTo(numberRequested);

        for (PanelListDto dto : responseEntity.getBody()) {
            assertThat(dto.getJurorStatus())
                .as("Expect the status to be panelled")
                .isEqualTo("Panelled");
            assertThat(dto.getFirstName())
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
        }

        assertThat(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(createPanelDto.getTrialNumber(),
            createPanelDto.getCourtLocationCode()).size()).as("Total members").isEqualTo(15);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void addPanelMembersBureauUser() {
        initHeadersBureau();

        final int numberRequested = 2;
        CreatePanelDto createPanelDto = makeCreatePanelDto(Collections.singletonList(
            "415231105"));
        createPanelDto.setNumberRequested(numberRequested);

        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto,
            httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void addPanelMembersNoTrialNumber() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        CreatePanelDto createPanelDto = makeCreatePanelDto(null);
        createPanelDto.setTrialNumber("");
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void addPanelMembersNoCourtLocation() {
        CreatePanelDto createPanelDto = makeCreatePanelDto(null);
        createPanelDto.setCourtLocationCode("");
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(requestEntity, Void.class);


        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void addPanelMembersZeroNumberRequested() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        CreatePanelDto createPanelDto = makeCreatePanelDto(null);
        createPanelDto.setNumberRequested(0);
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<String> responseEntity =
            restTemplate.exchange(requestEntity, String.class);

        assertBusinessRuleViolation(responseEntity,
            "Cannot add panel members - Number requested must be between 1 and 1000",
            NUMBER_OF_JURORS_EXCEEDS_LIMITS);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void addPanelMembersOverOneThousandNumbersRequested() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        CreatePanelDto createPanelDto = makeCreatePanelDto(null);
        createPanelDto.setNumberRequested(1001);
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<String> responseEntity =
            restTemplate.exchange(requestEntity, String.class);

        assertBusinessRuleViolation(responseEntity,
            "Cannot add panel members - Number requested must be between 1 and 1000",
            NUMBER_OF_JURORS_EXCEEDS_LIMITS);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    void addPanelMembersNoPanelCreated() {

        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE, CHESTER_LOC_CODE);
        CreatePanelDto createPanelDto = makeCreatePanelDto(new ArrayList<>());
        createPanelDto.setTrialNumber("T10000002");
        RequestEntity<CreatePanelDto> requestEntity = new RequestEntity<>(createPanelDto, httpHeaders,
            HttpMethod.POST, URI.create("/api/v1/moj/trial/panel/add-panel-members"));

        ResponseEntity<String> responseEntity =
            restTemplate.exchange(requestEntity, String.class);

        assertBusinessRuleViolation(responseEntity, "Cannot add panel members - panel has not been created for trial",
            NO_PANEL_EXIST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void panelCreationStatusPanelExists() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        RequestEntity<Void> requestEntity = new RequestEntity<>(
            httpHeaders,
            HttpMethod.GET,
            URI.create("/api/v1/moj/trial/panel/status?trial_number=T10000001&court_location_code=415")
        );

        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(requestEntity, Boolean.class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
            .isNotNull();
        assertThat(responseEntity.getBody())
            .as("Panel creation flag")
            .isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void panelCreationStatusPanelDoesNotExists() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        RequestEntity<Void> requestEntity = new RequestEntity<>(
            httpHeaders,
            HttpMethod.GET,
            URI.create("/api/v1/moj/trial/panel/status?trial_number=T10000002&court_location_code=415")
        );

        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(requestEntity, Boolean.class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
            .isNotNull();
        assertThat(responseEntity.getBody())
            .as("Panel creation flag")
            .isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void panelCreationStatusPanelNoTrialNumberProvided() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        RequestEntity<Void> requestEntity = new RequestEntity<>(
            httpHeaders,
            HttpMethod.GET,
            URI.create("/api/v1/moj/trial/panel/status?trial_number=&court_location_code=415")
        );

        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(requestEntity, Boolean.class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
            .isNotNull();
        assertThat(responseEntity.getBody())
            .as("Panel creation flag")
            .isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void panelCreationStatusPanelNoCourtLocationCodeProvided() {
        createBureauJwt(COURT_USER_NAME, CHESTER_LOC_CODE);
        RequestEntity<Void> requestEntity = new RequestEntity<>(
            httpHeaders,
            HttpMethod.GET,
            URI.create("/api/v1/moj/trial/panel/status?trial_number=T10000002&court_location_code=")
        );

        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(requestEntity, Boolean.class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
            .isNotNull();
        assertThat(responseEntity.getBody())
            .as("Panel creation flag")
            .isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    void panelCreationStatusBureauUser() {
        initHeadersBureau();

        RequestEntity<Void> requestEntity = new RequestEntity<>(
            httpHeaders,
            HttpMethod.GET,
            URI.create("/api/v1/moj/trial/panel/status?trial_number=T10000002&court_location_code=415")
        );

        ResponseEntity<Void> responseEntity = restTemplate.exchange(requestEntity, Void.class);

        assertThat(responseEntity.getStatusCode())
            .as("HTTP status")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }


    private CreatePanelDto makeCreatePanelDto(List<String> poolNumbers) {
        CreatePanelDto dto = new CreatePanelDto();
        dto.setTrialNumber("T10000000");
        dto.setNumberRequested(13);
        dto.setPoolNumbers(poolNumbers);
        dto.setCourtLocationCode(CHESTER_LOC_CODE);
        return dto;
    }


    private JurorListRequestDto createEmpanelledListRequestDto() {
        final int totalJurors = 12;
        final int numberRequested = 4;

        JurorListRequestDto dto = new JurorListRequestDto();
        dto.setTrialNumber("T10000000");
        dto.setCourtLocationCode(CHESTER_LOC_CODE);
        dto.setNumberRequested(numberRequested);
        List<JurorDetailRequestDto> dtoList = new ArrayList<>();

        for (int i = 0;
             i < totalJurors;
             i++) {
            JurorDetailRequestDto detailDto = createEmpanelDetailDto(i + 1);
            if (i <= 3) {
                detailDto.setResult(PanelResult.JUROR);
            } else if (i <= 7) {
                detailDto.setResult(PanelResult.CHALLENGED);
            } else {
                detailDto.setResult(PanelResult.NOT_USED);
            }
            dtoList.add(detailDto);
        }

        dto.setJurors(dtoList);
        return dto;
    }

    private JurorDetailRequestDto createEmpanelDetailDto(int index) {
        JurorDetailRequestDto detailDto = new JurorDetailRequestDto();
        detailDto.setFirstName("FNAME");
        detailDto.setLastName("LNAME");
        detailDto.setJurorNumber("4150000%02d".formatted(index));
        return detailDto;
    }

    private void initHeadersCourt() {
        httpHeaders = initialiseHeaders("99", false, COURT_USER_NAME, 89,
            CHESTER_LOC_CODE);
    }

    private void initHeadersBureau() {
        httpHeaders = initialiseHeaders("99", false, "BUREAU_USER", 89,
            "400");
    }
}
