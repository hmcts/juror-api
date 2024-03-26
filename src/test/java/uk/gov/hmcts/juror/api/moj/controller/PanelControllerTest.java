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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.CreatePanelDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.AvailableJurorsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.trial.PanelController;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.service.trial.PanelService;
import uk.gov.hmcts.juror.api.utils.CustomArgumentResolver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.juror.api.TestUtils.asJsonString;
import static uk.gov.hmcts.juror.api.TestUtils.createJwt;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PanelController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {PanelController.class})
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class PanelControllerTest {
    private static final String BASE_URL = "/api/v1/moj/trial/panel";

    private BureauJwtPayload jwtPayload;
    private MockMvc mockMvc;

    @MockBean
    private PanelService panelService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @BeforeEach
    public void setupMocks() {
        jwtPayload = null;
        mockMvc = MockMvcBuilders
            .standaloneSetup(new PanelController(panelService))
            .setCustomArgumentResolvers(new CustomArgumentResolver())
            .build();
    }

    @Test
    void judgesForCourtLocations() throws Exception {
        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AvailableJurorsDto availableJurorsDto = new AvailableJurorsDto();
        availableJurorsDto.setAvailableJurors(1L);
        availableJurorsDto.setCourtLocation("London");
        availableJurorsDto.setCourtLocationCode("415");
        availableJurorsDto.setPoolNumber("111111111");
        availableJurorsDto.setServiceStartDate(LocalDate.now());

        List<AvailableJurorsDto> availableJurorsDtoList = new ArrayList<>();
        availableJurorsDtoList.add(availableJurorsDto);

        when(panelService.getAvailableJurors("415")).thenReturn(availableJurorsDtoList);

        Assertions.assertThatNoException().isThrownBy(() -> mockMvc.perform(get(BASE_URL + "/available-jurors"
                + "?court_location_code=415")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(availableJurorsDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void createPanel() throws Exception {
        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ArrayList<String> poolNumbers = new ArrayList<>();

        poolNumbers.add("111111111");

        CreatePanelDto createPanelDto = new CreatePanelDto();
        createPanelDto.setNumberRequested(1);
        createPanelDto.setPoolNumbers(Optional.of(poolNumbers));
        createPanelDto.setCourtLocationCode("415");
        createPanelDto.setTrialNumber("T100000025");

        when(panelService.createPanel(1,
            "T100000025",
            Optional.of(poolNumbers),
            "415",
            createJwt("415", "COURT_USER"))).thenReturn(panelListDtos());

        mockMvc.perform(post(BASE_URL + "/create-panel")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createPanelDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(panelService, times(1)).createPanel(1,
            "T100000025",
            Optional.of(poolNumbers), "415",
            createJwt("415", "COURT_USER"));
    }

    @Test
    void createPanelIncorrectUrl() throws Exception {
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        jwtPayload = createJwt("415", "COURT_USER");
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        mockMvc.perform(post(BASE_URL + "/creaate").principal(mockPrincipal))
            .andExpect(status().isNotFound());

        verify(panelService, never()).createPanel(anyInt(), any(), any(), anyString(), any());
    }

    @Test
    void createPanelIncorrectHttpMethod() throws Exception {
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        jwtPayload = createJwt("415", "COURT_USER");
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        mockMvc.perform(get(BASE_URL + "/create-panel").principal(mockPrincipal))
            .andExpect(status().isMethodNotAllowed());

        verify(panelService, never()).createPanel(anyInt(), any(), any(), anyString(), any());
    }


    @Test
    void testRequestEmpanel() throws Exception {
        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        when(panelService.requestEmpanel(1, "T100000025", "415"))
            .thenReturn(createEmpaneledJuror());

        mockMvc.perform(
                get(BASE_URL + "/request-empanel?number_requested=1&trial_number=T1&court_location_code=415").principal(
                    mockPrincipal))
            .andExpect(status().isOk());

        verify(panelService, times(1))
            .requestEmpanel(anyInt(), anyString(), anyString());
    }

    @Test
    void processEmpanelledPanelResultJuror() throws Exception {
        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        PanelResult panelResult = PanelResult.JUROR;

        JurorDetailRequestDto jurorDetailRequestDto = new JurorDetailRequestDto();
        jurorDetailRequestDto.setFirstName("FName");
        jurorDetailRequestDto.setLastName("LName");
        jurorDetailRequestDto.setJurorNumber("111111111");
        jurorDetailRequestDto.setResult(panelResult);

        List<JurorDetailRequestDto> jurorDetailRequestDtos = new ArrayList<>();
        jurorDetailRequestDtos.add(jurorDetailRequestDto);

        JurorListRequestDto dto = new JurorListRequestDto();
        dto.setJurors(jurorDetailRequestDtos);

        PanelListDto panelListDto = new PanelListDto();

        panelListDto.setFirstName("FName");
        panelListDto.setLastName("LName");
        panelListDto.setJurorNumber("111111111");
        panelListDto.setJurorStatus("Juror");

        ArrayList<PanelListDto> dtos = new ArrayList<>();

        dtos.add(panelListDto);

        when(panelService.processEmpanelled(dto, jwtPayload))
            .thenReturn(dtos);

        Assertions.assertThatNoException().isThrownBy(() ->
            mockMvc.perform(post(BASE_URL + "/process-empanelled").principal(mockPrincipal)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(dto)))
                .andExpect(status().isOk()));
    }

    @Test
    void processEmpanelledPanelResultChallenged() throws Exception {
        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        PanelResult panelResult = PanelResult.CHALLENGED;

        JurorDetailRequestDto jurorDetailRequestDto = new JurorDetailRequestDto();
        jurorDetailRequestDto.setFirstName("FName");
        jurorDetailRequestDto.setLastName("LName");
        jurorDetailRequestDto.setJurorNumber("111111111");
        jurorDetailRequestDto.setResult(panelResult);

        List<JurorDetailRequestDto> jurorDetailRequestDtos = new ArrayList<>();
        jurorDetailRequestDtos.add(jurorDetailRequestDto);

        JurorListRequestDto dto = new JurorListRequestDto();
        dto.setJurors(jurorDetailRequestDtos);


        PanelListDto panelListDto = new PanelListDto();
        panelListDto.setFirstName("FName");
        panelListDto.setLastName("LName");
        panelListDto.setJurorNumber("111111111");
        panelListDto.setJurorStatus("Juror");

        ArrayList<PanelListDto> panelListDtos = new ArrayList<>();
        panelListDtos.add(panelListDto);

        when(panelService.processEmpanelled(dto, jwtPayload))
            .thenReturn(panelListDtos);

        Assertions.assertThatNoException().isThrownBy(() ->
            mockMvc.perform(post(BASE_URL + "/process-empanelled").principal(mockPrincipal)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(dto)))
                .andExpect(status().isOk()));
    }

    @Test
    void processEmpanelledPanelResultNotUsed() throws Exception {
        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        PanelResult panelResult = PanelResult.NOT_USED;

        JurorDetailRequestDto jurorDetailRequestDto = new JurorDetailRequestDto();
        jurorDetailRequestDto.setFirstName("FName");
        jurorDetailRequestDto.setLastName("LName");
        jurorDetailRequestDto.setJurorNumber("111111111");
        jurorDetailRequestDto.setResult(panelResult);

        List<JurorDetailRequestDto> jurorDetailRequestDtos = new ArrayList<>();
        jurorDetailRequestDtos.add(jurorDetailRequestDto);

        JurorListRequestDto dto = new JurorListRequestDto();
        dto.setJurors(jurorDetailRequestDtos);

        PanelListDto panelListDto = new PanelListDto();

        panelListDto.setFirstName("FName");
        panelListDto.setLastName("LName");
        panelListDto.setJurorNumber("111111111");
        panelListDto.setJurorStatus("Juror");

        ArrayList<PanelListDto> panelListDtos = new ArrayList<>();
        panelListDtos.add(panelListDto);

        when(panelService.processEmpanelled(dto, jwtPayload))
            .thenReturn(panelListDtos);

        Assertions.assertThatNoException()
            .isThrownBy(() -> mockMvc.perform(post(BASE_URL + "/process-empanelled").principal(mockPrincipal)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(dto)))
                .andExpect(status().isOk()));
    }

    private EmpanelListDto createEmpaneledJuror() {

        EmpanelDetailsDto dto = new EmpanelDetailsDto();
        dto.setFirstName("FName");
        dto.setLastName("LName");
        dto.setJurorNumber("111111111");

        List<EmpanelDetailsDto> empanelDetailsDto = new ArrayList<>();
        empanelDetailsDto.add(dto);

        EmpanelListDto empanelListDto = new EmpanelListDto();
        empanelListDto.setEmpanelList(empanelDetailsDto);
        empanelListDto.setTotalJurorsForEmpanel(1);

        return empanelListDto;
    }

    private List<PanelListDto> panelListDtos() {
        PanelListDto panelListDto = new PanelListDto();

        panelListDto.setFirstName("FName");
        panelListDto.setLastName("LName");
        panelListDto.setJurorNumber("111111111");
        panelListDto.setJurorStatus("Responded");

        ArrayList<PanelListDto> panelListDtos = new ArrayList<>();
        panelListDtos.add(panelListDto);

        return panelListDtos;
    }
}
