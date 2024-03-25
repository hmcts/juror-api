package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.utils.CustomArgumentResolver;
import uk.gov.hmcts.juror.api.utils.CustomArgumentResolverBureau;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.juror.api.TestUtils.asJsonString;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = JurorManagementController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {JurorManagementController.class})
class JurorManagementControllerTest {
    private static final String BASE_URL = "/api/v1/moj/juror-management/attendance";

    private MockMvc mockMvc;

    @MockBean
    private JurorAppearanceService jurorAppearanceService;
    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @Before
    @BeforeEach
    public void setupMocks() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new JurorManagementController(jurorAppearanceService))
            .setCustomArgumentResolvers(new CustomArgumentResolver())
            .build();
    }

    @Test
    void addAttendanceDayHappyPath() throws Exception {
        BureauJWTPayload payload = TestUtils.createJwt("415", "COURT_USER");
        AddAttendanceDayDto request = buildAddAttendanceDayDto();


        mockMvc.perform(post("/api/v1/moj/juror-management/add-attendance-day")
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated());

        verify(jurorAppearanceService, times(1)).addAttendanceDay(payload, request);
    }

    @Test
    void retrieveAttendanceDetailsOkay() throws Exception {
        List<String> jurors = new ArrayList<>();
        jurors.add("111111111");
        RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(jurors);

        when(jurorAppearanceService.retrieveAttendanceDetails(null, request))
            .thenReturn(buildAttendanceDetailsResponse());

        mockMvc.perform(get(BASE_URL)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isOk());

        verify(jurorAppearanceService, times(1)).retrieveAttendanceDetails(any(), any());
    }

    @Test
    void retrieveAttendanceDetailsForbiddenBureauUser() throws Exception {
        buildMockMvcBureau();

        RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);

        when(jurorAppearanceService.retrieveAttendanceDetails(null, request))
            .thenReturn(buildAttendanceDetailsResponse());

        MvcResult mvcResult = mockMvc.perform(get(BASE_URL)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isForbidden())
            .andReturn();

        MojException.Forbidden ex = (MojException.Forbidden) mvcResult.getResolvedException();
        assert ex != null;
        assertThat(ex.getMessage()).isEqualTo("Bureau users are not allowed to use this service");

        verify(jurorAppearanceService, never()).retrieveAttendanceDetails(any(), any());
    }

    @Test
    void retrieveAttendanceDetailsIncorrectUrl() throws Exception {
        RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);

        mockMvc.perform(get("/api/v1/moj/juror-management/attendances")
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isNotFound());

        verify(jurorAppearanceService, never()).retrieveAttendanceDetails(any(), any());
    }

    @Test
    void retrieveAttendanceDetailsIncorrectHttpMethod() throws Exception {
        RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);

        mockMvc.perform(put(BASE_URL)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isMethodNotAllowed());

        verify(jurorAppearanceService, never()).retrieveAttendanceDetails(any(), any());
    }

    @Test
    void updateAttendanceIsOkay() throws Exception {
        UpdateAttendanceDto request = buildUpdateAttendanceDto(null);

        when(jurorAppearanceService.updateAttendance(null, request))
            .thenReturn(buildAttendanceDetailsResponse());

        mockMvc.perform(patch(BASE_URL)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isOk());

        verify(jurorAppearanceService, times(1)).updateAttendance(any(), any());
    }

    @Test
    void updateAttendanceMissingAttendanceDate() throws Exception {
        UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
        request.getCommonData().setAttendanceDate(null);

        when(jurorAppearanceService.updateAttendance(null, request))
            .thenReturn(buildAttendanceDetailsResponse());

        mockMvc.perform(patch(BASE_URL)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isBadRequest());

        verify(jurorAppearanceService, never()).updateAttendance(any(), any());
    }

    @Test
    void deleteAttendanceDetailsIsOkay() throws Exception {
        UpdateAttendanceDto request = buildUpdateAttendanceDto(null);

        when(jurorAppearanceService.deleteAttendance(null, request))
            .thenReturn(buildAttendanceDetailsResponse());

        mockMvc.perform(delete(BASE_URL)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isOk());

        verify(jurorAppearanceService, times(1)).deleteAttendance(any(), any());
    }

    @Test
    void deleteAttendanceDetailsMissingStatusValue() throws Exception {
        UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
        request.getCommonData().setStatus(null);

        when(jurorAppearanceService.deleteAttendance(null, request))
            .thenReturn(buildAttendanceDetailsResponse());

        mockMvc.perform(delete(BASE_URL)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isBadRequest());

        verify(jurorAppearanceService, never()).deleteAttendance(any(), any());
    }

    @Test
    void addNonAttendanceOkay() throws Exception {
        final String url = "/api/v1/moj/juror-management/non-attendance";
        JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
            .jurorNumber("111111111")
            .nonAttendanceDate(now())
            .poolNumber("415230101")
            .locationCode("415")
            .build();

        mockMvc.perform(post(url)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated());

        ArgumentCaptor<JurorNonAttendanceDto> requestCaptor = ArgumentCaptor.forClass(JurorNonAttendanceDto.class);

        verify(jurorAppearanceService, times(1)).addNonAttendance(requestCaptor.capture());

        JurorNonAttendanceDto capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getJurorNumber()).isEqualTo(request.getJurorNumber());
        assertThat(capturedRequest.getNonAttendanceDate()).isEqualTo(request.getNonAttendanceDate());
        assertThat(capturedRequest.getPoolNumber()).isEqualTo(request.getPoolNumber());
        assertThat(capturedRequest.getLocationCode()).isEqualTo(request.getLocationCode());

    }

    @Test
    void addNonAttendanceUnhappyJurorNumberMissing() throws Exception {
        final String url = "/api/v1/moj/juror-management/non-attendance";
        JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
            .nonAttendanceDate(now())
            .poolNumber("415230101")
            .locationCode("415")
            .build();

        mockMvc.perform(post(url)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorAppearanceService);
    }

    @Test
    void addNonAttendanceIncorrectHttpMethodPut() throws Exception {
        final String url = "/api/v1/moj/juror-management/non-attendance";
        JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
            .nonAttendanceDate(now())
            .jurorNumber("111111111")
            .locationCode("415")
            .build();

        mockMvc.perform(put(url)
                .principal(mock(BureauJwtAuthentication.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(jurorAppearanceService);
    }

    private UpdateAttendanceDto buildUpdateAttendanceDto(List<String> jurors) {
        UpdateAttendanceDto.CommonData commonData = new UpdateAttendanceDto.CommonData();
        commonData.setStatus(UpdateAttendanceStatus.CHECK_OUT);
        commonData.setAttendanceDate(now().minusDays(2));
        commonData.setLocationCode("415");
        commonData.setSingleJuror(Boolean.FALSE);

        UpdateAttendanceDto request = new UpdateAttendanceDto();
        request.setCommonData(commonData);
        request.setJuror(jurors);

        return request;
    }

    private AttendanceDetailsResponse buildAttendanceDetailsResponse() {
        return AttendanceDetailsResponse.builder()
            .summary(new AttendanceDetailsResponse.Summary())
            .details(new ArrayList<>())
            .build();
    }

    private RetrieveAttendanceDetailsDto buildRetrieveAttendanceDetailsDto(List<String> jurors) {
        RetrieveAttendanceDetailsDto.CommonData commonData = new RetrieveAttendanceDetailsDto.CommonData();
        commonData.setAttendanceDate(now().minusDays(2));
        commonData.setLocationCode("415");
        commonData.setTag(RetrieveAttendanceDetailsTag.JUROR_NUMBER);

        return RetrieveAttendanceDetailsDto.builder()
            .commonData(commonData)
            .juror(jurors)
            .build();
    }

    AddAttendanceDayDto buildAddAttendanceDayDto() {
        AddAttendanceDayDto dto = new AddAttendanceDayDto();
        dto.setAttendanceDate(LocalDate.now());
        dto.setJurorNumber("123456789");
        dto.setPoolNumber("123456789");
        dto.setLocationCode("415");
        dto.setCheckInTime(LocalTime.of(9, 30));
        dto.setCheckOutTime(LocalTime.of(17, 30));
        return dto;
    }

    private void buildMockMvcBureau() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new JurorManagementController(jurorAppearanceService))
            .setCustomArgumentResolvers(new CustomArgumentResolverBureau())
            .build();
    }
}
