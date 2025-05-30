package uk.gov.hmcts.juror.api.moj.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.trial.PanelService;
import uk.gov.hmcts.juror.api.moj.service.trial.TrialService;
import uk.gov.hmcts.juror.api.utils.CustomArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static java.time.LocalDate.now;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.juror.api.TestUtils.asJsonString;
import static uk.gov.hmcts.juror.api.TestUtils.createJwt;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TrialController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {TrialController.class})
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class TrialControllerTest {
    private static final String BASE_URL = "/api/v1/moj/trial";

    private BureauJwtPayload jwtPayload;
    private MockMvc mockMvc;

    @MockBean
    private TrialService trialService;

    @MockBean
    private PanelService panelService;

    @MockBean
    private JurorAppearanceService jurorAppearanceService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @BeforeEach
    public void setupMocks() {
        jwtPayload = null;
        mockMvc = MockMvcBuilders
            .standaloneSetup(new TrialController(trialService,jurorAppearanceService))
            .setCustomArgumentResolvers(new CustomArgumentResolver())
            .build();
    }

    @Test
    void createTrial() throws Exception {
        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        when(trialService.createTrial(jwtPayload, createTrialDto())).thenReturn(createTrialSummaryDto());

        mockMvc.perform(post(BASE_URL + "/create")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createTrialDto())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").value(notNullValue()))
            .andExpect(jsonPath("$.trial_number").value("TEST000001"))
            .andExpect(jsonPath("$.defendants").value("Joe, Jo, Jon"))
            .andExpect(jsonPath("$.trial_type").value("CRI"))
            .andExpect(jsonPath("$.judge.id").value(21L))
            .andExpect(jsonPath("$.judge.code").value("1234"))
            .andExpect(jsonPath("$.judge.description").value("Test Judge"))
            .andExpect(jsonPath("$.courtroom.id").value(66L))
            .andExpect(jsonPath("$.courtroom.owner").value("415"))
            .andExpect(jsonPath("$.courtroom.room_number").value("1"))
            .andExpect(jsonPath("$.courtroom.description").value("Large room for 100 guests"))
            .andExpect(jsonPath("$.protected").value("true"))
            .andExpect(jsonPath("$.is_active").value("true"))
            .andExpect(jsonPath("$.court_room_location_name").value("TEST COURTROOM LOCATION"));

        verify(trialService, times(1)).createTrial(jwtPayload, createTrialDto());
    }

    @Test
    void createTrialIncorrectUrl() throws Exception {
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        jwtPayload = createJwt("415", "COURT_USER");
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        mockMvc.perform(post(BASE_URL + "/creaate").principal(mockPrincipal))
            .andExpect(status().isNotFound());

        verify(trialService, never()).createTrial(any(), any());
    }

    @Test
    void createTrialIncorrectHttpMethod() throws Exception {
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        jwtPayload = createJwt("415", "COURT_USER");
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        mockMvc.perform(get(BASE_URL + "/create").principal(mockPrincipal))
            .andExpect(status().isMethodNotAllowed());

        verify(trialService, never()).createTrial(any(), any());
    }

    @Test
    void testGetTrials() throws Exception {
        final String methodUrl = "/list";

        TrialSearch trialSearch = TrialSearch.builder()
            .pageNumber(1)
            .pageLimit(25)
            .build();

        jwtPayload = createJwt("415", "COURT_USER");
        PaginatedList<TrialListDto> result = new PaginatedList<>();
        result.setTotalItems(10L);

        when(trialService.getTrials(trialSearch)).thenReturn(result);

        mockMvc.perform(post(BASE_URL + methodUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(trialSearch)))
            .andExpect(status().isOk())
            .andDo(MockMvcResultHandlers.print())
            .andExpect(content().json(asJsonString(result)));

        verify(trialService, times(1))
            .getTrials(trialSearch);
    }

    @Test
    void testGetTrialSummary() throws Exception {
        final String methodUrl = "/summary?trial_number=TEST000001&location_code=415";

        jwtPayload = createJwt("415", "COURT_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        when(trialService.getTrialSummary(jwtPayload, "TEST000001", "415"))
            .thenReturn(createTrialSummaryDto());

        mockMvc.perform(get(BASE_URL + methodUrl).principal(mockPrincipal))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").value(notNullValue()))
            .andExpect(jsonPath("$.trial_number").value("TEST000001"))
            .andExpect(jsonPath("$.defendants").value("Joe, Jo, Jon"))
            .andExpect(jsonPath("$.trial_type").value("CRI"))
            .andExpect(jsonPath("$.judge.id").value(21L))
            .andExpect(jsonPath("$.judge.code").value("1234"))
            .andExpect(jsonPath("$.judge.description").value("Test Judge"))
            .andExpect(jsonPath("$.courtroom.id").value(66L))
            .andExpect(jsonPath("$.courtroom.owner").value("415"))
            .andExpect(jsonPath("$.courtroom.room_number").value("1"))
            .andExpect(jsonPath("$.courtroom.description").value("Large room for 100 guests"))
            .andExpect(jsonPath("$.protected").value("true"))
            .andExpect(jsonPath("$.is_active").value("true"))
            .andExpect(jsonPath("$.court_room_location_name").value("TEST COURTROOM LOCATION"));

        verify(trialService, times(1)).getTrialSummary(jwtPayload,
            "TEST000001", "415");
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void testReturnJury() {
        final String methodUrl = "/return-jury?trial_number=T10000000&location_code=415";

        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        Assertions.assertThatNoException().isThrownBy(() -> mockMvc.perform(post(BASE_URL + methodUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createReturnJuryDto()))
                .principal(mockPrincipal))
            .andExpect(status().isOk()));
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void testReturnJuryEmptyTimes() throws Exception {
        final String methodUrl = "/return-jury?trial_number=T10000000&location_code=415";

        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ReturnJuryDto requestBody = createReturnJuryDto();
        requestBody.setCheckIn("");
        requestBody.setCheckOut("");

        mockMvc.perform(post(BASE_URL + methodUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody))
                .principal(mockPrincipal))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert"//False positive
    })
    void testReturnPanel() {
        final String methodUrl = "/return-panel?trial_number=T10000000&location_code=415";

        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        Assertions.assertThatNoException().isThrownBy(() -> mockMvc.perform(post(BASE_URL + methodUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createReturnPanelDto())).principal(mockPrincipal))
            .andExpect(status().isOk()));
    }

    @Test
    void testEndTrial() {
        final String methodUrl = "/end-trial";

        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        Assertions.assertThatNoException().isThrownBy(() -> mockMvc.perform(patch(BASE_URL + methodUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createEndTrialDto()))
                .principal(mockPrincipal))
            .andExpect(status().isOk()));
    }

    private TrialSummaryDto createTrialSummaryDto() {
        CourtroomsDto courtroomsDto = new CourtroomsDto();
        courtroomsDto.setId(66L);
        courtroomsDto.setOwner("415");
        courtroomsDto.setRoomNumber("1");
        courtroomsDto.setDescription("Large room for 100 guests");

        JudgeDto judgeDto = new JudgeDto();
        judgeDto.setId(21L);
        judgeDto.setCode("1234");
        judgeDto.setDescription("Test Judge");

        return TrialSummaryDto.builder()
            .trialNumber("TEST000001")
            .defendants("Joe, Jo, Jon")
            .trialType(valueOf(TrialType.CRI))
            .judge(judgeDto)
            .courtroomsDto(courtroomsDto)
            .protectedTrial(Boolean.TRUE)
            .isActive(Boolean.TRUE)
            .courtRoomLocationName("TEST COURTROOM LOCATION")
            .build();
    }

    private TrialDto createTrialDto() {
        TrialDto trialDto = new TrialDto();
        trialDto.setCaseNumber("TEST000001");
        trialDto.setTrialType(TrialType.CRI);
        trialDto.setDefendant("Joe, Jo, Jon");
        trialDto.setStartDate(now().plusMonths(1));
        trialDto.setJudgeId(21L);
        trialDto.setCourtLocation("415");
        trialDto.setCourtroomId(66L);
        trialDto.setProtectedTrial(Boolean.TRUE);

        return trialDto;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private ReturnJuryDto createReturnJuryDto() {
        ReturnJuryDto dto = new ReturnJuryDto();

        dto.setCheckIn("09:00");
        dto.setCheckIn("09:15");

        dto.setCompleted(false);

        List<JurorDetailRequestDto> jurorDetailRequestDtos = new ArrayList<>();

        for (int i = 0;
             i < 10;
             i++) {
            JurorDetailRequestDto detailRequestDto = new JurorDetailRequestDto();
            detailRequestDto.setFirstName("FNAME");
            detailRequestDto.setLastName("LNAME");
            detailRequestDto.setJurorNumber(String.format("11111111%d", i));
            detailRequestDto.setResult(PanelResult.JUROR);
            jurorDetailRequestDtos.add(detailRequestDto);
        }
        dto.setJurors(jurorDetailRequestDtos);

        return dto;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<JurorDetailRequestDto> createReturnPanelDto() {
        List<JurorDetailRequestDto> jurorDetailRequestDtos = new ArrayList<>();

        for (int i = 0;
             i < 10;
             i++) {
            JurorDetailRequestDto detailRequestDto = new JurorDetailRequestDto();
            detailRequestDto.setFirstName("FNAME");
            detailRequestDto.setLastName("LNAME");
            detailRequestDto.setJurorNumber(String.format("11111111%d", i));
            detailRequestDto.setResult(PanelResult.JUROR);
            jurorDetailRequestDtos.add(detailRequestDto);
        }
        return jurorDetailRequestDtos;
    }

    private EndTrialDto createEndTrialDto() {
        EndTrialDto dto = new EndTrialDto();
        dto.setTrialEndDate(now());
        dto.setTrialNumber("T10000000");
        dto.setLocationCode("415");
        return dto;
    }
}
