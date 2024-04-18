package uk.gov.hmcts.juror.api.moj.controller;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.utils.CustomPageImpl;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.juror.api.TestUtils.staffBuilder;
import static uk.gov.hmcts.juror.api.utils.DataConversionUtil.getExceptionDetails;

/**
 * Integration tests for the Trial controller.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/db/mod/truncate.sql", "/db/trial/Trial.sql"})
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
public class TrialControllerITest extends AbstractIntegrationTest {
    private static final String ASSERT_POST_IS_SUCCESSFUL = "Expect the HTTP POST request to be successful.";
    private static final String URL_CREATE = "/api/v1/moj/trial/create";

    private static final String COURT_USER = "COURT_USER";
    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String URL_EDIT = "/api/v1/moj/trial/edit";

    private HttpHeaders httpHeaders;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    TrialRepository trialRepository;

    @Autowired
    PanelRepository panelRepository;

    @Autowired
    AppearanceRepository appearanceRepository;

    @Autowired
    JurorHistoryRepository jurorHistoryRepository;

    @Autowired
    JurorPoolRepository jurorPoolRepository;

    @Override
    @Before
    public void setUp() throws Exception {
        //Nothing to set up for the time being
    }

    @Test
    public void createTrialHappy() {
        initialiseHeader(singletonList("415"), "415", COURT_USER);
        TrialDto trialRequest = createTrialRequest();

        long countBefore = trialRepository.count();

        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, POST,
                URI.create(URL_CREATE)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(OK);

        long countAfter = trialRepository.count();
        assertThat(countAfter)
            .as("Expect number of trial records to increase by 1")
            .isEqualTo(countBefore + 1);

        TrialSummaryDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(requireNonNull(responseBody).getTrialNumber())
            .as("Expect trial number to be TEST00001")
            .isEqualTo("TEST00001");

        assertThat(requireNonNull(responseBody).getDefendants())
            .as("Expect trial defendent to be Joe, John, Betty")
            .isEqualTo("Joe, John, Betty");

        assertThat(requireNonNull(responseBody).getTrialType())
            .as("Expect trial type to be CIV")
            .isEqualTo("Civil");

        assertThat(requireNonNull(responseBody).getTrialStartDate())
            .as("Expect trial start date to be 1 month in the future")
            .isEqualTo(LocalDate.now().plusMonths(1));

        assertThat(requireNonNull(responseBody).getProtectedTrial())
            .as("Expect protected trial to be false")
            .isEqualTo(Boolean.FALSE);

        assertThat(requireNonNull(responseBody).getIsActive())
            .as("Expect is active to be true")
            .isEqualTo(Boolean.TRUE);

        JudgeDto judge = responseBody.getJudge();
        assertThat(requireNonNull(judge).getId())
            .as("Expect judge id to be 21 (type long)")
            .isEqualTo(21L);

        assertThat(requireNonNull(judge).getCode())
            .as("Expect judge code to be 1234")
            .isEqualTo("1234");

        assertThat(requireNonNull(judge).getDescription())
            .as("Expect judge description to be Test Judge")
            .isEqualTo("Test judge");

        CourtroomsDto courtrooms = responseBody.getCourtroomsDto();
        assertThat(requireNonNull(courtrooms).getId())
            .as("Expect courtroom id to be 66 (type long)")
            .isEqualTo(66L);

        assertThat(requireNonNull(courtrooms).getOwner())
            .as("Expect courtroom owner to be 415")
            .isEqualTo("415");

        assertThat(requireNonNull(courtrooms).getRoomNumber())
            .as("Expect courtroom room number to be 1")
            .isEqualTo("1");

        assertThat(requireNonNull(courtrooms).getDescription())
            .as("Expect courtroom description to be")
            .isEqualTo("large room fits 100 people");
    }

    @Test
    public void createTrialCaseNumberIsNotSaved() {
        initialiseHeader(singletonList("415"), "415", COURT_USER);
        TrialDto trialRequest1 = createTrialRequest();

        long countBefore = trialRepository.count();

        restTemplate.exchange(new RequestEntity<>(trialRequest1, httpHeaders, POST,
            URI.create(URL_CREATE)), TrialSummaryDto.class);

        TrialDto trialRequest2 = createTrialRequest();
        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest2, httpHeaders, POST,
                URI.create(URL_CREATE)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as("Expect the POST request to be unsuccessful - "
            + "bad request sending duplicate case number for the same location").isEqualTo(BAD_REQUEST);

        long countAfter = trialRepository.count();
        assertThat(countAfter)
            .as("Expect number of trial records increase by 1")
            .isEqualTo(countBefore + 1);
    }

    @Test
    public void createTrialMissingCaseNumber() {
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        TrialDto trialRequest = createTrialRequest();
        trialRequest.setCaseNumber("");
        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, POST,
                URI.create(URL_CREATE)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createTrialTooLongDefendantsString() {
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        TrialDto trialRequest = createTrialRequest();
        trialRequest.setDefendant("This, is, a, very, long, list, of, defendants, et al");
        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, POST,
                URI.create(URL_CREATE)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createTrialDuplicateCaseNumberButDifferentCourtLocIsSaved() {
        initialiseHeader(singletonList("415"), "415", COURT_USER);
        TrialDto trialRequest1 = createTrialRequest();

        final long countBefore = trialRepository.count();

        restTemplate.exchange(new RequestEntity<>(trialRequest1, httpHeaders, POST,
            URI.create(URL_CREATE)), TrialSummaryDto.class);

        TrialDto trialRequest2 = createTrialRequest();
        trialRequest2.setCourtLocation("462");
        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest2, httpHeaders, POST,
                URI.create(URL_CREATE)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(OK);

        long countAfter = trialRepository.count();
        assertThat(countAfter)
            .as("Expect number of trial records to increase by 2")
            .isEqualTo(countBefore + 2);
    }

    @Test
    public void createTrialBureauUserForbidden() {
        initialiseHeader(singletonList("400"), "400", BUREAU_USER);

        TrialDto trialRequest = createTrialRequest();
        trialRequest.setCourtLocation("462");
        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, POST,
                URI.create(URL_CREATE)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(FORBIDDEN);
    }

    @Test
    public void createTrialMultiCourtAccess() {
        List<String> courts = new ArrayList<>();
        courts.add("415");
        courts.add("462");
        initialiseHeader(courts, "415", COURT_USER);

        final long countBefore = trialRepository.count();

        TrialDto trialRequest1 = createTrialRequest();
        restTemplate.exchange(new RequestEntity<>(trialRequest1, httpHeaders, POST,
            URI.create(URL_CREATE)), TrialSummaryDto.class);

        //Create second trial
        TrialDto trialRequest2 = createTrialRequest();
        trialRequest2.setCourtLocation("462");
        trialRequest2.setProtectedTrial(Boolean.TRUE);

        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest2, httpHeaders, POST,
                URI.create(URL_CREATE)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(OK);

        long countAfter = trialRepository.count();
        assertThat(countAfter)
            .as("Expect number of trials created to increase by 2 (one for each court location)")
            .isEqualTo(countBefore + 2);
    }

    @Test
    public void testGetTrialsDescOrder() {
        initialiseHeader(singletonList("415"), "415", COURT_USER);
        final String urlGetTrials = "/api/v1/moj/trial/list?"
            + "page_number=0&"
            + "sort_by=trialNumber&"
            + "sort_order=desc&"
            + "is_active=false";

        ResponseEntity<CustomPageImpl<TrialListDto>> responseEntity = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET, URI.create(urlGetTrials)),
            new ParameterizedTypeReference<>() {
            });

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful.")
            .isEqualTo(OK);

        CustomPageImpl<TrialListDto> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(requireNonNull(responseBody.stream().count()))
            .as("Expect the response to return a list of 13 trials")
            .isEqualTo(13);

        assertThat(responseBody.getContent())
            .as("Expect owner to be the same for all courtrooms")
            .extracting(TrialListDto::getTrialNumber)
            .containsExactly("T100000025",
                "T100000023",
                "T100000021",
                "T100000019",
                "T100000017",
                "T100000015",
                "T100000013",
                "T100000011",
                "T100000009",
                "T100000007",
                "T100000005",
                "T100000003",
                "T100000001");
    }

    @Test
    public void testGetTrialsBureauUserForbidden() {
        initialiseHeader(singletonList("400"), "400", BUREAU_USER);

        final String urlGetTrials = "/api/v1/moj/trial/list?"
            + "page_number=0&"
            + "sort_by=trialNumber&"
            + "sort_order=desc&"
            + "is_active=false";

        ResponseEntity<TrialListDto> responseEntity = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET, URI.create(urlGetTrials)), TrialListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the request to get a list of trials to fail for Bureau users.")
            .isEqualTo(FORBIDDEN);
    }

    @Test
    public void testGetTrialSummary() {
        final String urlGetTrialSummary = "/api/v1/moj/trial/summary?"
            + "trial_number=T100000023&"
            + "location_code=415";

        initialiseHeader(singletonList("415"), "415", COURT_USER);

        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                URI.create(urlGetTrialSummary)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect request to get trial summary is successful")
            .isEqualTo(OK);

        TrialSummaryDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(requireNonNull(responseBody).getTrialNumber())
            .as("Expect trial number to be T100000023")
            .isEqualTo("T100000023");

        assertThat(requireNonNull(responseBody).getDefendants())
            .as("Expect trial defendent to be TEST DEFENDANT")
            .isEqualTo("TEST DEFENDANT");

        assertThat(requireNonNull(responseBody).getTrialType())
            .as("Expect trial type to be CIV")
            .isEqualTo("Civil");

        assertThat(requireNonNull(responseBody).getProtectedTrial())
            .as("Expect protected trial to be false")
            .isEqualTo(Boolean.FALSE);

        assertThat(requireNonNull(responseBody).getTrialEndDate())
            .as("Expect end date to be null")
            .isNull();

        assertThat(requireNonNull(responseBody).getIsActive())
            .as("Expect is active to be true")
            .isEqualTo(Boolean.TRUE);

        JudgeDto judge = responseBody.getJudge();
        assertThat(requireNonNull(judge).getId())
            .as("Expect judge id to be 22 (type long)")
            .isEqualTo(22L);

        assertThat(requireNonNull(judge).getCode())
            .as("Expect judge code to be 4321")
            .isEqualTo("4321");

        assertThat(requireNonNull(judge).getDescription())
            .as("Expect judge description to be Test Judge")
            .isEqualTo("Judge Test");

        CourtroomsDto courtrooms = responseBody.getCourtroomsDto();
        assertThat(requireNonNull(courtrooms).getId())
            .as("Expect courtroom id to be 66 (type long)")
            .isEqualTo(66L);

        assertThat(requireNonNull(courtrooms).getOwner())
            .as("Expect courtroom owner to be 415")
            .isEqualTo("415");

        assertThat(requireNonNull(courtrooms).getRoomNumber())
            .as("Expect courtroom room number to be 1")
            .isEqualTo("1");

        assertThat(requireNonNull(courtrooms).getDescription())
            .as("Expect courtroom description to be")
            .isEqualTo("large room fits 100 people");

    }

    @Test
    public void testGetTrialSummaryInactiveTrial() {
        final String urlGetTrialSummary = "/api/v1/moj/trial/summary?"
            + "trial_number=T100000024&"
            + "location_code=462";

        initialiseHeader(singletonList("462"), "462", COURT_USER);

        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                URI.create(urlGetTrialSummary)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect request to get trial summary is successful")
            .isEqualTo(OK);

        TrialSummaryDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(requireNonNull(responseBody).getTrialNumber())
            .as("Expect trial number to be T100000024")
            .isEqualTo("T100000024");

        assertThat(requireNonNull(responseBody).getDefendants())
            .as("Expect trial defendent to be TEST DEFENDANT")
            .isEqualTo("TEST DEFENDANT");

        assertThat(requireNonNull(responseBody).getTrialType())
            .as("Expect trial type to be CIV")
            .isEqualTo("Civil");

        assertThat(requireNonNull(responseBody).getProtectedTrial())
            .as("Expect protected trial to be false")
            .isEqualTo(Boolean.FALSE);

        assertThat(requireNonNull(responseBody).getTrialEndDate())
            .as("Expect end date to be today's date")
            .isEqualTo(LocalDate.now());

        assertThat(requireNonNull(responseBody).getIsActive())
            .as("Expect is active to be false")
            .isEqualTo(Boolean.FALSE);
    }

    @Test
    public void testGetTrialSummaryBureauUserForbidden() {
        final String urlGetTrialSummary = "/api/v1/moj/trial/summary?"
            + "trial_number=T100000023&"
            + "location_code=415";

        initialiseHeader(singletonList("400"), "400", BUREAU_USER);

        TrialDto trialRequest = createTrialRequest();
        trialRequest.setCourtLocation("462");
        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, GET,
                URI.create(urlGetTrialSummary)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Bureau users are forbidden from getting trial summary").isEqualTo(FORBIDDEN);
    }

    @Test
    public void testGetTrialSummaryNotFoundException() {
        final String urlGetTrialSummary = "/api/v1/moj/trial/summary?"
            + "trial_number=123&"
            + "location_code=415";

        initialiseHeader(singletonList("415"), "415", COURT_USER);

        TrialDto trialRequest = createTrialRequest();
        trialRequest.setCourtLocation("415");
        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, GET,
                URI.create(urlGetTrialSummary)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect a Not Found exception if trial details cannot be found for the search criteria")
            .isEqualTo(NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/ReturnJuryPanel.sql"})
    public void testReturnPanel() {
        final String url = "/api/v1/moj/trial/return-panel?"
            + "trial_number=T10000000&"
            + "location_code=415";

        List<JurorDetailRequestDto> dto = createJurorDetailRequestDto(1, 13);
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, POST,
                URI.create(url)), Void.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(OK);

        List<Panel> panelList = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(
            "T10000000", "415");

        for (Panel panel : panelList) {
            assertThat(panel.getResult()).as("Expect result to be Returned")
                .isEqualTo(PanelResult.RETURNED);
            assertThat(panel.getJurorPool().getStatus().getStatus()).as(
                "Expect status to be Responded (Juror in waiting)").isEqualTo(IJurorStatus.RESPONDED);
            assertThat(
                jurorHistoryRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()).size())
                .as("Expect one history item for juror " + panel.getJurorPool().getJurorNumber())
                .isEqualTo(1);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/ReturnJuryPanel.sql"})
    public void testReturnJuryConfirmAttendance() {
        final String url = "/api/v1/moj/trial/return-jury?"
            + "trial_number=T10000001&"
            + "location_code=415";

        ReturnJuryDto dto = createReturnJuryDto(false, "09:00", "10:00");
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, POST,
                URI.create(url)), Void.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(OK);

        List<Panel> panelList = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(
            "T10000001", "415");

        for (Panel panel : panelList) {
            assertThat(panel.getResult()).as("Expect result to be Returned")
                .isEqualTo(PanelResult.RETURNED);
            assertThat(panel.getJurorPool().getStatus().getStatus()).as(
                "Expect status to be Responded (Juror in waiting)").isEqualTo(IJurorStatus.RESPONDED);
            assertThat(
                jurorHistoryRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()).size())
                .as("Expect one history item for juror " + panel.getJurorPool().getJurorNumber())
                .isEqualTo(1);
            assertThat(panel.isCompleted()).as("Expected panel completed status to be true").isTrue();

            Appearance appearance =
                appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorPool().getJurorNumber(),
                    LocalDate.now()).orElseThrow(() ->
                    new MojException.NotFound("No appearance record found", null));

            assertThat(appearance.getTimeIn()).as("Expect time in to not be null").isNotNull();
            assertThat(appearance.getTimeIn()).as("Expect time in to be 09:00").isEqualTo(LocalTime.parse(
                "09:00"));

            assertThat(appearance.getTimeOut()).as("Expect time out to not be null").isNotNull();
            assertThat(appearance.getTimeOut()).as("Expect time out to be 10:00").isEqualTo(LocalTime.parse(
                "10:00"));

            assertThat(appearance.getSatOnJury()).isTrue();
            assertThat(appearance.getAppearanceStage())
                .as("Expect appearance stage to be EXPENSE_ENTERED")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/ReturnJuryPanel.sql"})
    public void testReturnJuryNoConfirmAttendance() {
        final String url = "/api/v1/moj/trial/return-jury?"
            + "trial_number=T10000001&"
            + "location_code=415";

        ReturnJuryDto dto = createReturnJuryDto(false, "09:30", "");
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, POST,
                URI.create(url)), Void.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(OK);

        List<Panel> panelList = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(
            "T10000001", "415");

        for (Panel panel : panelList) {
            assertThat(panel.getResult()).as("Expect result to be Returned")
                .isEqualTo(PanelResult.RETURNED);
            assertThat(panel.getJurorPool().getStatus().getStatus()).as(
                "Expect status to be Responded").isEqualTo(IJurorStatus.RESPONDED);
            assertThat(
                jurorHistoryRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()).size())
                .as("Expect one history item for juror " + panel.getJurorPool().getJurorNumber())
                .isEqualTo(1);

            Appearance appearance =
                appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorPool().getJurorNumber(),
                    LocalDate.now()).orElseThrow(() ->
                    new MojException.NotFound("No appearance record found", null));
            assertThat(appearance.getTimeIn()).as("Expect time in to be null").isEqualTo(LocalTime.of(9, 30));
            assertThat(appearance.getTimeOut()).as("Expect time out to be null").isNull();
            assertThat(panel.isCompleted()).as("Expected panel completed status to be true").isTrue();
            assertThat(appearance.getAppearanceStage())
                .as("Expect appearance stage to be CHECKED_IN")
                .isEqualTo(AppearanceStage.CHECKED_IN);
            assertThat(appearance.getSatOnJury()).isTrue();
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/ReturnJuryPanel.sql"})
    public void testReturnJuryConfirmAttendanceAndCompleteService() {
        final String url = "/api/v1/moj/trial/return-jury?"
            + "trial_number=T10000001&"
            + "location_code=415";

        ReturnJuryDto dto = createReturnJuryDto(true, "09:00", "10:00");
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, POST,
                URI.create(url)), Void.class);

        assertThat(responseEntity.getStatusCode()).as(ASSERT_POST_IS_SUCCESSFUL).isEqualTo(OK);

        List<Panel> panelList = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(
            "T10000001", "415");

        for (Panel panel : panelList) {
            assertThat(panel.getResult()).as("Expect result to be Returned")
                .isEqualTo(PanelResult.RETURNED);
            assertThat(
                jurorHistoryRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()).size())
                .as("Expect one history item for juror " + panel.getJurorPool().getJurorNumber())
                .isEqualTo(2);
            assertThat(panel.isCompleted()).as("Expect completed status to be true").isTrue();
            assertThat(panel.getJurorPool().getStatus().getStatus()).as("Expect status to be COMPLETED")
                .isEqualTo(IJurorStatus.COMPLETED);
            assertThat(panel.getJurorPool().getJuror().getCompletionDate()).as(
                "Expect completion date to be " + LocalDate.now()).isEqualTo(LocalDate.now());

            Appearance appearance =
                appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorPool().getJurorNumber(),
                    LocalDate.now()).orElseThrow(() ->
                    new MojException.NotFound("No appearance record found", null));

            assertThat(appearance.getTimeIn()).as("Expect time in to not be null").isNotNull();
            assertThat(appearance.getTimeIn()).as("Expect time in to be 09:00").isEqualTo(LocalTime.parse(
                "09:00"));

            assertThat(appearance.getTimeOut()).as("Expect time out to not be null").isNotNull();
            assertThat(appearance.getTimeOut()).as("Expect time out to be 10:00").isEqualTo(LocalTime.parse(
                "10:00"));

            assertThat(appearance.getSatOnJury()).isTrue();
            assertThat(appearance.getAppearanceStage())
                .as("Expect appearance stage to be EXPENSE_ENTERED")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);

            assertThat(panel.getJurorPool().getJuror().getCompletionDate()).as("Expect completion date to not be "
                + "null").isNotNull();
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/EndTrial.sql"})
    public void testEndTrialHappyPath() {
        final String url = "/api/v1/moj/trial/end-trial";
        final String trialNumber = "T10000002";
        final String locationCode = "415";
        EndTrialDto dto = createEndTrialDto();
        dto.setTrialNumber(trialNumber);
        initialiseHeader(singletonList(locationCode), locationCode, COURT_USER);

        ResponseEntity<Void> responseEntity =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, PATCH,
                URI.create(url)), Void.class);

        assertThat(responseEntity.getStatusCode()).as("Expect status code to be 200 (ok)").isEqualTo(OK);

        Trial trial = trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber, locationCode).get();
        assertThat(trial.getTrialEndDate()).as("Expect trial end date to not be null").isNotNull();
        assertThat(trial.getTrialEndDate()).as("Expect trial end date to equal " + dto.getTrialEndDate())
            .isEqualTo(dto.getTrialEndDate());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/EndTrial.sql"})
    public void testEndTrialJuryMembersStillInTrial() {
        final String url = "/api/v1/moj/trial/end-trial";
        EndTrialDto dto = createEndTrialDto();
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        ResponseEntity<String> responseEntity =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, PATCH,
                URI.create(url)), String.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect status code to be 422 (ok)")
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        assertThat(responseEntity.getBody()).as("Expect response to have json string").isNotNull();

        JSONObject exceptionDetails = getExceptionDetails(responseEntity);
        String errorMessage = exceptionDetails.getString("message");
        String code = exceptionDetails.getString("code");

        assertThat(errorMessage).as("Expect error message to not be null").isNotNull();
        assertThat(errorMessage)
            .as("Expect error message to equal: Cannot end trial, trial still has members")
            .isEqualTo("Cannot end trial, trial still has members");
        assertThat(code).as("Expect code to not be null").isNotNull();
        assertThat(code).as("Expect code to be TRIAL_HAS_MEMBERS").isEqualTo("TRIAL_HAS_MEMBERS");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/trial/EndTrial.sql"})
    public void testEndTrialPanelMembersStillInTrial() {
        final String url = "/api/v1/moj/trial/end-trial";
        EndTrialDto dto = createEndTrialDto();
        dto.setTrialNumber("T10000001");
        initialiseHeader(singletonList("415"), "415", COURT_USER);

        ResponseEntity<String> responseEntity =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, PATCH,
                URI.create(url)), String.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect status code to be 422 (ok)")
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        JSONObject exceptionDetails = getExceptionDetails(responseEntity);
        String errorMessage = exceptionDetails.getString("message");
        String code = exceptionDetails.getString("code");


        assertThat(errorMessage).as("Expect error message to not be null").isNotNull();
        assertThat(errorMessage)
            .as("Expect error message to equal: Cannot end trial, trial still has members")
            .isEqualTo("Cannot end trial, trial still has members");
        assertThat(code).as("Expect code to not be null").isNotNull();
        assertThat(code).as("Expect code to be TRIAL_HAS_MEMBERS").isEqualTo("TRIAL_HAS_MEMBERS");
    }

    @Test
    public void editTrialHappy() {
        initialiseHeader(singletonList("416"), "416", COURT_USER);
        TrialDto trialRequest = editTrialRequest();

        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, PATCH,
                URI.create(URL_EDIT)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as("Expect HTTP Response to be OK").isEqualTo(OK);

        TrialSummaryDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(requireNonNull(responseBody).getTrialNumber())
            .as("Expect trial number to be TEST000012")
            .isEqualTo("TEST000012");

        assertThat(requireNonNull(responseBody).getDefendants())
            .as("Expect trial defendent to be Peter and David")
            .isEqualTo("Peter and David");

        assertThat(requireNonNull(responseBody).getTrialType())
            .as("Expect trial type to be CRI")
            .isEqualTo("Criminal");

        assertThat(requireNonNull(responseBody).getTrialStartDate())
            .as("Expect start date of trial to be 10 days in future")
            .isEqualTo(LocalDate.now().plusDays(10));

        assertThat(requireNonNull(responseBody).getProtectedTrial())
            .as("Expect protected trial to be true")
            .isEqualTo(Boolean.TRUE);

        assertThat(requireNonNull(responseBody).getIsActive())
            .as("Expect is active to be true")
            .isEqualTo(Boolean.TRUE);

        JudgeDto judge = responseBody.getJudge();
        assertThat(requireNonNull(judge).getId())
            .as("Expect judge id to be 24 (type long)")
            .isEqualTo(24L);

        assertThat(requireNonNull(judge).getCode())
            .as("Expect judge code to be 4323")
            .isEqualTo("4323");

        assertThat(requireNonNull(judge).getDescription())
            .as("Expect judge description to be Judge Test3")
            .isEqualTo("Judge Test3");

        CourtroomsDto courtrooms = responseBody.getCourtroomsDto();
        assertThat(requireNonNull(courtrooms).getId())
            .as("Expect courtroom id to be 69 (type long)")
            .isEqualTo(69L);

        assertThat(requireNonNull(courtrooms).getOwner())
            .as("Expect courtroom owner to be 416")
            .isEqualTo("416");

        assertThat(requireNonNull(courtrooms).getRoomNumber())
            .as("Expect courtroom room number to be 4")
            .isEqualTo("4");

        assertThat(requireNonNull(courtrooms).getDescription())
            .as("Expect courtroom description to be")
            .isEqualTo("large room fits 102 people");
    }

    @Test
    public void editTrialWrongCourtUser() {
        initialiseHeader(singletonList("415"), "415", COURT_USER);
        TrialDto trialRequest = editTrialRequest();

        ResponseEntity<TrialSummaryDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(trialRequest, httpHeaders, PATCH,
                URI.create(URL_EDIT)), TrialSummaryDto.class);

        assertThat(responseEntity.getStatusCode()).as("Expect HTTP Response to be Forbidden")
            .isEqualTo(FORBIDDEN);
    }

    private void initialiseHeader(List<String> courts, String owner, String loginUserType) {
        BureauJwtPayload.Staff staff = createStaff(courts, "MsCourt");

        httpHeaders = initialiseHeaders("99", false, loginUserType, 89,
            owner, staff);
    }

    private BureauJwtPayload.Staff createStaff(List<String> courts, String staffName) {
        return staffBuilder(staffName, 1, courts);
    }

    private TrialDto createTrialRequest() {
        return TrialDto.builder()
            .caseNumber("TEST00001")
            .trialType(TrialType.CIV)
            .defendant("Joe, John, Betty")
            .startDate(LocalDate.now().plusMonths(1))
            .judgeId(21L)
            .courtLocation("415")
            .courtroomId(66L)
            .protectedTrial(Boolean.FALSE)
            .build();
    }

    private TrialDto editTrialRequest() {
        return TrialDto.builder()
            .caseNumber("TEST000012")
            .trialType(TrialType.CRI)
            .defendant("Peter and David")
            .startDate(LocalDate.now().plusDays(10))
            .judgeId(24L)
            .courtLocation("416")
            .courtroomId(69L)
            .protectedTrial(Boolean.TRUE)
            .build();
    }

    private ReturnJuryDto createReturnJuryDto(boolean completeServiceFlag, String checkIn, String checkOut) {
        ReturnJuryDto dto = new ReturnJuryDto();

        dto.setCheckIn(checkIn);
        dto.setCheckOut(checkOut);
        dto.setCompleted(completeServiceFlag);

        final int jurorNumberStart = 14;
        final int jurorNumberEnd = 17;

        dto.setJurors(createJurorDetailRequestDto(jurorNumberStart, jurorNumberEnd));

        return dto;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")

    private List<JurorDetailRequestDto> createJurorDetailRequestDto(final int jurorNumberStart,
                                                                    final int jurorNumberEnd) {
        List<JurorDetailRequestDto> dtoList = new ArrayList<>();
        for (int i = jurorNumberStart;
             i <= jurorNumberEnd;
             i++) {
            JurorDetailRequestDto detailRequestDto = new JurorDetailRequestDto();
            detailRequestDto.setFirstName("FNAME");
            detailRequestDto.setLastName("LNAME");
            detailRequestDto.setJurorNumber(String.format("4150000%02d", i));
            detailRequestDto.setResult(PanelResult.JUROR);
            dtoList.add(detailRequestDto);
        }
        return dtoList;
    }

    private EndTrialDto createEndTrialDto() {
        EndTrialDto dto = new EndTrialDto();
        dto.setTrialEndDate(LocalDate.now());
        dto.setTrialNumber("T10000000");
        dto.setLocationCode("415");
        return dto;
    }
}
