package uk.gov.hmcts.juror.api.moj.controller;

import org.assertj.core.api.Assertions;
import org.junit.Before;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_AVAILABLE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_LIMITS;


@RunWith(SpringRunner.class)
@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PanelControllerITest extends AbstractIntegrationTest {

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
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initHeadersCourt();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    public void createPanelCourtUserAllPools() {
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
    public void createPanelCourtUserJurorsExceedLimit() {
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
    public void createPanelCourtUserNotEnoughJurors() {
        CreatePanelDto createPanelDto = makeCreatePanelDto(null);
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
    public void createPanelCourtUserAllPoolsWrongTrialNumber() {
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
    public void createPanelCourtUserSelectedPool() {
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
    public void createPanelBureauUserAllPools() {
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
    public void requestEmpanelCourtUser() {
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

        assert responseEntity.getBody() != null;

        assertThat(
                responseEntity.getBody().getTotalJurorsForEmpanel()).as("Expected total jurors to be 3")
            .isEqualTo(3);
        assertThat(
            responseEntity.getBody().getEmpanelList().size()
        ).as("Expected size to be five").isEqualTo(5);

        for (EmpanelDetailsDto dto : responseEntity.getBody().getEmpanelList()) {
            assertThat(dto.getFirstName())
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(dto.getLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
            assertThat(dto.getStatus())
                .as("Expect status to be Panelled")
                .isEqualTo("Panel");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql"})
    public void requestEmpanelBureauUser() {
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
    public void panelSummary() {
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

        Assertions
            .assertThat(responseEntity.getBody().length)
            .as("Expected length to be 13")
            .isEqualTo(13);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/Panel.sql", "/db/trial/CreatedPanel.sql"})
    public void empanelJurorsCourtUserHappyPath() {
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
    public void empanelJurorsBureauUser() {
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
    public void empanelJurorsNoResultSet() {

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
    public void availableJurors() {
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
    public void availableJurorsBureauUser() {
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


    private CreatePanelDto makeCreatePanelDto(List<String> poolNumbers) {
        CreatePanelDto dto = new CreatePanelDto();
        dto.setTrialNumber("T10000000");
        dto.setNumberRequested(13);
        dto.setPoolNumbers(Optional.ofNullable(poolNumbers));
        dto.setCourtLocationCode("415");
        return dto;
    }


    private JurorListRequestDto createEmpanelledListRequestDto() {
        final int totalJurors = 12;
        final int numberRequested = 4;

        JurorListRequestDto dto = new JurorListRequestDto();
        dto.setTrialNumber("T10000000");
        dto.setCourtLocationCode("415");
        dto.setNumberRequested(numberRequested);
        List<JurorDetailRequestDto> dtoList = new ArrayList<>();

        for (int i = 0; i < totalJurors; i++) {
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
        httpHeaders = initialiseHeaders("99", false, "COURT_USER", 89,
            "435");
    }

    private void initHeadersBureau() {
        httpHeaders = initialiseHeaders("99", false, "BUREAU_USER", 89,
            "400");
    }
}
