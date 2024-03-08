package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.EditJurorRecordRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDtoTest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoliceCheckStatusDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessNameChangeRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsCommonResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorOverviewResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.BulkServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.JurorRecordService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.LawOfDemeter", "PMD.ExcessivePublicCount"})
@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = JurorRecordController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {JurorRecordController.class, BulkServiceImpl.class})
@DisplayName("Controller: /api/v1/moj/juror-record")
class JurorRecordControllerTest {

    private static final String BASE_URL = "/api/v1/moj/juror-record";
    private static final String JUROR_NUMBER = "111111111";
    private static final String LOC_CODE = "415";
    private static final String OWNER_BUREAU = "400";
    private static final String OWNER_COURT = "415";
    private static final String USERNAME_COURT = "COURT_USER";
    private static final String USERNAME_BUREAU = "BUREAU_USER";


    @MockBean
    private JurorRecordService jurorRecordService;

    @Autowired
    private BulkService bulkService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    private BureauJWTPayload bureauJwtPayload;

    private MockMvc mockMvc;

    @Before
    @BeforeEach
    public void setupMocks() {
        bureauJwtPayload = null;
        mockMvc = MockMvcBuilders
            .standaloneSetup(new JurorRecordController(jurorRecordService, bulkService))
            .setCustomArgumentResolvers(new PrincipalDetailsArgumentResolver())
            .build();
    }

    @Test
    public void test_bureauUser_getJurorOverview_happyPath() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        JurorDetailsCommonResponseDto commonDto = new JurorDetailsCommonResponseDto();
        commonDto.setJurorNumber(JUROR_NUMBER);
        commonDto.setPoliceCheck("Not Checked");
        commonDto.setNoDeferrals(0);

        JurorOverviewResponseDto dto = new JurorOverviewResponseDto();
        dto.setCommonDetails(commonDto);

        doReturn(dto).when(jurorRecordService).getJurorOverview(bureauJwtPayload, JUROR_NUMBER, LOC_CODE);

        mockMvc.perform(get("/api/v1/moj/juror-record/overview/" + JUROR_NUMBER + "/" + LOC_CODE)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(commonDto)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$['commonDetails']['jurorNumber']", CoreMatchers.is(JUROR_NUMBER)))
            .andExpect(jsonPath("$['welshLanguageRequired']", CoreMatchers.nullValue()))
            .andExpect(jsonPath("$['commonDetails']['policeCheck']", CoreMatchers.is("Not Checked")));

        verify(jurorRecordService, times(1))
            .getJurorOverview(bureauJwtPayload, JUROR_NUMBER, LOC_CODE);
    }

    /**
     * Test designed to check all fields within the returned object.
     */
    @Test
    public void test_bureauUser_getJurorOverview_happyPath_allFieldsPresent() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        LocalDate localDateCommonDto = LocalDate.of(2021, 2, 2);

        JurorDetailsCommonResponseDto commonDto = createCommonDto(localDateCommonDto, OWNER_BUREAU);
        LocalDate overviewResponseLocalDate = LocalDate.of(2021, 1, 1);
        JurorOverviewResponseDto dto = createJurorOverviewResponseDto(commonDto, overviewResponseLocalDate);

        doReturn(dto).when(jurorRecordService)
            .getJurorOverview(bureauJwtPayload, JUROR_NUMBER, LOC_CODE);

        mockMvc.perform(get("/api/v1/moj/juror-record/overview/" + JUROR_NUMBER + "/" + LOC_CODE)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$['commonDetails']['owner']", CoreMatchers.is("Current owner")))
            .andExpect(jsonPath("$['commonDetails']['title']", CoreMatchers.is("Miss")))
            .andExpect(jsonPath("$['commonDetails']['firstName']", CoreMatchers.is("FirstName")))
            .andExpect(jsonPath("$['commonDetails']['lastName']", CoreMatchers.is("LastName")))
            .andExpect(jsonPath("$['commonDetails']['jurorNumber']", CoreMatchers.is(JUROR_NUMBER)))
            .andExpect(jsonPath("$['commonDetails']['jurorStatus']", CoreMatchers.is("Summoned")))
            .andExpect(jsonPath("$['commonDetails']['poolNumber']", CoreMatchers.is("12345678")))
            .andExpect(jsonPath("$['commonDetails']['startDate']",
                CoreMatchers.is(localDateCommonDto.toString())))
            .andExpect(jsonPath("$['commonDetails']['courtName']", CoreMatchers.is("Chester")))
            .andExpect(jsonPath("$['commonDetails']['excusalRejected']", CoreMatchers.is("Rejected")))
            .andExpect(jsonPath("$['commonDetails']['excusalCode']", CoreMatchers.is("C")))
            .andExpect(jsonPath("$['commonDetails']['excusalDescription']", CoreMatchers.is("Childcare")))
            .andExpect(jsonPath("$['commonDetails']['deferredTo']", CoreMatchers.nullValue()))
            .andExpect(jsonPath("$['commonDetails']['noDeferrals']", CoreMatchers.is(0)))
            .andExpect(jsonPath("$['commonDetails']['deferralDate']", CoreMatchers.nullValue()))
            .andExpect(jsonPath("$['commonDetails']['policeCheck']", CoreMatchers.is("Not Checked")))
            .andExpect(jsonPath("$['commonDetails']['pendingTitle']", CoreMatchers.is("Dr")))
            .andExpect(jsonPath("$['commonDetails']['pendingFirstName']",
                CoreMatchers.is("NewFirstName")))
            .andExpect(jsonPath("$['commonDetails']['pendingLastName']",
                CoreMatchers.is("NewLastName")))
            .andExpect(jsonPath("$['replyStatus']", CoreMatchers.is("TODO")))
            .andExpect(jsonPath("$['replyDate']", CoreMatchers.is(overviewResponseLocalDate.toString())))
            .andExpect(jsonPath("$['replyMethod']", CoreMatchers.is("Paper")))
            .andExpect(jsonPath("$['specialNeed']", CoreMatchers.is("Wheelchair access")))
            .andExpect(jsonPath("$['specialNeedDescription']",
                CoreMatchers.is("Wheelchair access required")))
            .andExpect(jsonPath("$['specialNeedMessage']",
                CoreMatchers.is("Wheelchair access required")))
            .andExpect(jsonPath("$['opticReference']", CoreMatchers.is("12345678")))
            .andExpect(jsonPath("$['welshLanguageRequired']", CoreMatchers.is(true)));

        verify(jurorRecordService, times(1))
            .getJurorOverview(bureauJwtPayload, JUROR_NUMBER, LOC_CODE);
    }


    @Test
    public void test_courtUser_getJurorOverview_happyPath() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_COURT, USERNAME_COURT);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        JurorDetailsCommonResponseDto commonDto = new JurorDetailsCommonResponseDto();
        commonDto.setJurorNumber(JUROR_NUMBER);

        JurorOverviewResponseDto dto = new JurorOverviewResponseDto();
        dto.setCommonDetails(commonDto);
        dto.setWelshLanguageRequired(true);

        doReturn(dto).when(jurorRecordService)
            .getJurorOverview(bureauJwtPayload, JUROR_NUMBER, LOC_CODE);

        mockMvc.perform(get("/api/v1/moj/juror-record/overview/" + JUROR_NUMBER + "/" + LOC_CODE)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(commonDto)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$['welshLanguageRequired']", CoreMatchers.is(true)))
            .andExpect(jsonPath("$['commonDetails']['jurorNumber']", CoreMatchers.is(JUROR_NUMBER)))
            .andExpect(jsonPath("$['commonDetails']['policeCheck']", CoreMatchers.nullValue()));

        verify(jurorRecordService, times(1))
            .getJurorOverview(bureauJwtPayload, JUROR_NUMBER, LOC_CODE);
    }

    public class PrincipalDetailsArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(BureauJWTPayload.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return bureauJwtPayload;
        }
    }

    @Test
    public void test_fixJurorName_courtUser_happyPath() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_COURT, USERNAME_COURT);
        bureauJwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(bureauJwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().is2xxSuccessful());

        verify(jurorRecordService, times(1))
            .fixErrorInJurorName(any(BureauJWTPayload.class), Mockito.anyString(),
                any(JurorNameDetailsDto.class));
    }

    @Test
    public void test_fixJurorName_bureauUser_teamLeader_happyPath() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_COURT, USERNAME_COURT);
        bureauJwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(1).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(bureauJwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().is2xxSuccessful());

        verify(jurorRecordService, times(1))
            .fixErrorInJurorName(any(BureauJWTPayload.class), Mockito.anyString(),
                any(JurorNameDetailsDto.class));
    }

    @Test
    public void test_fixJurorName_bureauUser_insufficientRank() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        bureauJwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(bureauJwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isForbidden());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }


    @ParameterizedTest
    @ValueSource(strings = {"MrMrsMsMiss", "Mr|"})
    void testFixJurorNameBureauUserInvalidTitle(String title) throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        bureauJwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(1).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto(title, "Test", "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(bureauJwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "FirstNameTooLongFirst", "Test|"})
    void testFixJurorNameBureauUserFirstNameInvalid(String firstName) throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(1).build());

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", firstName, "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(jwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "LastNameLastNameLastN", "Person|"})
    void testFixJurorNameBureauUserLastNameEmpty(String lastName) throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt(OWNER_COURT, USERNAME_COURT);
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", lastName);

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(jwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }


    // TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void testFixJurorNameBureauUserInvalidRequestJurorNumberTooShort() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(1).build());

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(jwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }

    //TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void testFixJurorNameBureauUserInvalidRequestJurorNumberTooLong() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt(OWNER_COURT, USERNAME_COURT);
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(jwtPayload, "11111111111111111111", dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + "11111111111111111111")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }

    // TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void testFixJurorNameBureauUserInvalidRequestJurorNumberAlphaNumeric() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(1).build());

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "11111111L";

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        doNothing().when(jurorRecordService)
            .fixErrorInJurorName(jwtPayload, jurorNumber, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }

    @Test
    public void testFixJurorNameBureauUserInvalidRequestJurorNumberNull() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt(OWNER_COURT, USERNAME_COURT);
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        mockMvc.perform(patch("/api/v1/moj/juror-record/fix-name/")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isNotFound());

        verify(jurorRecordService, Mockito.never()).fixErrorInJurorName(any(BureauJWTPayload.class),
            Mockito.anyString(), any(JurorNameDetailsDto.class));
    }


    @Test
    public void testProcessNameChangeApprovalCourtUserHappyPath() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_COURT, USERNAME_COURT);
        bureauJwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.APPROVE,
            "Marriage certificate and passport");

        doNothing().when(jurorRecordService)
            .processPendingNameChange(bureauJwtPayload, JUROR_NUMBER, dto);

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().is2xxSuccessful());

        verify(jurorRecordService, times(1))
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    @Test
    public void testProcessNameChangeApprovalBureauUser() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        bureauJwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.APPROVE,
            "Marriage certificate and passport");

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isForbidden());

        verify(jurorRecordService, Mockito.never())
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    @Test
    public void testProcessNameChangeApprovalInvalidRequestBodyMissingApprovalDecision() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(null,
            "Marriage certificate and passport");

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    @Test
    public void testProcessNameChangeApprovalInvalidRequestBodyMissingNotes() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, null);

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    @Test
    public void test_processNameChangeApproval_invalidJurorNumber_missing() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT,
            "Their name has not been legally changed");

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isNotFound());

        verify(jurorRecordService, Mockito.never())
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    // TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void test_processNameChangeApproval_invalidJurorNumber_tooShort() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "11111111";

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, "test notes");

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    //TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void test_processNameChangeApproval_invalidJurorNumber_tooLong() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "1111111111";

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, "test notes");

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    //TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void test_processNameChangeApproval_invalidJurorNumber_alphaNumeric() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "11111111L";

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, "test notes");

        mockMvc.perform(patch("/api/v1/moj/juror-record/change-name/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .processPendingNameChange(any(BureauJWTPayload.class), Mockito.anyString(),
                any(ProcessNameChangeRequestDto.class));
    }

    @Test
    public void test_editJurorDetails_happyPath() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();

        doNothing().when(jurorRecordService).editJurorDetails(any(), any(), any());

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isNoContent());

        verify(jurorRecordService, times(1))
            .editJurorDetails(any(), any(), any());
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_title_invalidPipe() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setTitle("|Title|");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "First|Name"})
    void testEditJurorDetailsInvalidFirstName(String firstName) throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setFirstName(firstName);

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Last|Name"})
    void testEditJurorDetailsInvalidLastName(String lastName) throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setLastName(lastName);

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Address|Line"})
    void testEditJurorDetailsInvalidAddressLine1(String addressLine1) throws Exception {
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressLineOne(addressLine1);

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_lastName_invalidPipe() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "111111111";

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setLastName("Last|Name");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, jurorNumber);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_addressLine1_missing() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "111111111";

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressLineOne("");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, jurorNumber);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_addressLine1_invalidPipe() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(0).build());
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "111111111";

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressLineOne("Address|Line");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, jurorNumber);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_addressLine2_invalidPipe() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressLineTwo("Address|Line");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_addressLine3_invalidPipe() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressLineThree("Address|Line");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_addressLine4_invalidPipe() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressTown("Some|Town");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_addressLine5_invalidPipe() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressCounty("Some|County");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_postcode_invalidFormat() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setAddressPostcode("AB1CD");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_dob_tooOld() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setDateOfBirth(LocalDate.now().minusYears(125));

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_dob_tooYoung() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setDateOfBirth(LocalDate.now());

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_primaryPhone_invalidFormat() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setPrimaryPhone("987654");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_secondaryPhone_invalidFormat() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setSecondaryPhone("987654");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_emailAddress_invalidFormat() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setEmailAddress("some.email");

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_pendingTitle_tooLong() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setPendingTitle(TestUtils.buildStringToLength(11));

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_pendingFirstName_tooLong() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setPendingFirstName(TestUtils.buildStringToLength(21));

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    @Test
    public void test_editJurorDetails_invalidRequestDto_pendingLastName_tooLong() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();
        dto.setPendingLastName(TestUtils.buildStringToLength(21));

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, JUROR_NUMBER);
    }

    // Todo
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void test_editJurorDetails_invalidJurorNumber_tooShort() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "11111111";

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, jurorNumber);
    }

    // TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void test_editJurorDetails_invalidJurorNumber_tooLong() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "1111111111";

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, jurorNumber);
    }

    // TODO
    @Test
    @Ignore("PathVariables are not validating, need a solution to fix these tests. One solution is to create a custom"
        + " type to represent the variable and apply validation - like we do for DTOs")
    public void test_editJurorDetails_invalidJurorNumber_alphaNumeric() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        final String jurorNumber = "11111111L";

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, jurorNumber);
    }

    @Test
    public void test_editJurorDetails_invalidJurorNumber_missing() throws Exception {
        BureauJWTPayload jwtPayload = createPayload(OWNER_COURT, USERNAME_COURT, 0);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        EditJurorRecordRequestDto dto = createEditJurorRecordRequestDto();

        mockMvc.perform(patch("/api/v1/moj/juror-record/edit-juror/")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isNotFound());

        verify(jurorRecordService, Mockito.never())
            .editJurorDetails(jwtPayload, dto, null);
    }

    private EditJurorRecordRequestDto createEditJurorRecordRequestDto() {
        EditJurorRecordRequestDto editJurorRecordRequestDto = new EditJurorRecordRequestDto();

        editJurorRecordRequestDto.setFirstName("First");
        editJurorRecordRequestDto.setLastName("Last");
        editJurorRecordRequestDto.setAddressLineOne("Address Line One");
        editJurorRecordRequestDto.setAddressTown(null);
        editJurorRecordRequestDto.setAddressPostcode("M24 4BP");
        editJurorRecordRequestDto.setDateOfBirth(LocalDate.of(2002, 2, 1));
        editJurorRecordRequestDto.setWelshLanguageRequired(false);

        return editJurorRecordRequestDto;
    }

    private JurorDetailsCommonResponseDto createCommonDto(LocalDate localDate, String currentOwner) {
        JurorDetailsCommonResponseDto commonDto = new JurorDetailsCommonResponseDto();
        commonDto.setOwner(currentOwner);
        commonDto.setTitle("Miss");
        commonDto.setFirstName("FirstName");
        commonDto.setLastName("LastName");
        commonDto.setJurorNumber(JUROR_NUMBER);
        commonDto.setJurorStatus("Summoned");
        commonDto.setPoolNumber("12345678");
        commonDto.setStartDate(localDate);
        commonDto.setCourtName("Chester");
        commonDto.setExcusalRejected("Rejected");
        commonDto.setExcusalCode("C");
        commonDto.setExcusalDescription("Childcare");
        commonDto.setDeferredTo(null);
        commonDto.setNoDeferrals(0);
        commonDto.setDeferralDate(null);
        commonDto.setPoliceCheck("Not Checked");
        commonDto.setPendingTitle("Dr");
        commonDto.setPendingFirstName("NewFirstName");
        commonDto.setPendingLastName("NewLastName");
        return commonDto;
    }

    private JurorOverviewResponseDto createJurorOverviewResponseDto(JurorDetailsCommonResponseDto commonDto,
                                                                    LocalDate localDate) {
        JurorOverviewResponseDto dto = new JurorOverviewResponseDto();
        dto.setReplyStatus("TODO");
        dto.setReplyDate(localDate);
        dto.setReplyMethod("Paper");
        dto.setSpecialNeed("Wheelchair access");
        dto.setSpecialNeedDescription("Wheelchair access required");
        dto.setSpecialNeedMessage("Wheelchair access required");
        dto.setOpticReference("12345678");
        dto.setCommonDetails(commonDto);
        dto.setWelshLanguageRequired(true);
        return dto;
    }

    private BureauJWTPayload createPayload(String owner, String username, int rank) {
        BureauJWTPayload jwtPayload = TestUtils.createJwt(owner, username);
        jwtPayload.setStaff(BureauJWTPayload.Staff.builder().rank(rank).build());
        return jwtPayload;
    }


    @Nested
    @DisplayName(UpdatePncCheckStatus.URL)
    class UpdatePncCheckStatus extends AbstractControllerTest<PoliceCheckStatusDto, Void> {
        private static final String URL = BASE_URL + "/pnc/{jurorNumber}";
        private static final BureauJwtAuthentication MOCK_PRINCIPAL = mock(BureauJwtAuthentication.class);

        public UpdatePncCheckStatus() {
            super(HttpMethod.PATCH, URL, MOCK_PRINCIPAL);
            bureauJwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
            when(MOCK_PRINCIPAL.getPrincipal()).thenReturn(bureauJwtPayload);
        }

        @BeforeEach
        void beforeEach() {
            this.setMockMvc(mockMvc);
        }

        @ParameterizedTest(name = "Police check status update should get processed when given a value of {0}")
        @EnumSource(PoliceCheck.class)
        @DisplayName("Police check update valid")
        void positiveAccepted(PoliceCheck policeCheck) throws Exception {
            PoliceCheckStatusDto policeCheckStatusDto = new PoliceCheckStatusDto(policeCheck);
            send(policeCheckStatusDto, HttpStatus.ACCEPTED, TestConstants.VALID_JUROR_NUMBER);
            verify(jurorRecordService, times(1))
                .updatePncStatus(TestConstants.VALID_JUROR_NUMBER, policeCheck);
            verifyNoMoreInteractions(jurorRecordService);
        }

        @org.junit.jupiter.api.Test
        void negativeNullStatus() throws Exception {
            PoliceCheckStatusDto policeCheckStatusDto = new PoliceCheckStatusDto(null);
            send(policeCheckStatusDto, HttpStatus.BAD_REQUEST, TestConstants.VALID_JUROR_NUMBER);
            verifyNoInteractions(jurorRecordService);
        }

        @org.junit.jupiter.api.Test
        @Disabled("Known issue ticket raised: JM-5010")
        void negativeInvalidJurorNumber() throws Exception {
            PoliceCheckStatusDto policeCheckStatusDto = new PoliceCheckStatusDto(PoliceCheck.ELIGIBLE);
            send(policeCheckStatusDto, HttpStatus.BAD_REQUEST, TestConstants.INVALID_JUROR_NUMBER);
            verifyNoInteractions(jurorRecordService);
        }
    }

    @Nested
    @DisplayName(UpdatePncCheckStatus.URL)
    class UpdateJurorToFailedToAttend extends AbstractControllerTest<JurorNumberAndPoolNumberDto, Void> {
        private static final String URL = BASE_URL + "/failed-to-attend";
        private static final BureauJwtAuthentication MOCK_PRINCIPAL = mock(BureauJwtAuthentication.class);

        public UpdateJurorToFailedToAttend() {
            super(HttpMethod.PATCH, URL, MOCK_PRINCIPAL);
            bureauJwtPayload = TestUtils.createJwt("415", "COURT_USER");
            when(MOCK_PRINCIPAL.getPrincipal()).thenReturn(bureauJwtPayload);
        }

        @BeforeEach
        void beforeEach() {
            this.setMockMvc(mockMvc);
        }

        @org.junit.jupiter.api.Test
        void typical() throws Exception {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();
            dto.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            dto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);

            send(dto, HttpStatus.ACCEPTED);
            verify(jurorRecordService, times(1))
                .updateJurorToFailedToAttend(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER);
            verifyNoMoreInteractions(jurorRecordService);
        }

        @org.junit.jupiter.api.Test
        void negativeInvalidJurorNumber() throws Exception {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();
            dto.setJurorNumber(TestConstants.INVALID_JUROR_NUMBER);
            dto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);

            send(dto, HttpStatus.BAD_REQUEST);
            verifyNoInteractions(jurorRecordService);
        }

        @org.junit.jupiter.api.Test
        void negativeInvalidPoolNumber() throws Exception {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();
            dto.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            dto.setPoolNumber(TestConstants.INVALID_POOL_NUMBER);

            send(dto, HttpStatus.BAD_REQUEST);
            verifyNoInteractions(jurorRecordService);
        }
    }

    @Nested
    @DisplayName(UndoUpdateJurorToFailedToAttend.URL)
    class UndoUpdateJurorToFailedToAttend extends AbstractControllerTest<JurorNumberAndPoolNumberDto, Void> {
        private static final String URL = BASE_URL + "/failed-to-attend/undo";
        private static final BureauJwtAuthentication MOCK_PRINCIPAL = mock(BureauJwtAuthentication.class);

        public UndoUpdateJurorToFailedToAttend() {
            super(HttpMethod.PATCH, URL, MOCK_PRINCIPAL);
            bureauJwtPayload = TestUtils.createJwt("415", "COURT_USER", "9");
            when(MOCK_PRINCIPAL.getPrincipal()).thenReturn(bureauJwtPayload);
        }

        @BeforeEach
        void beforeEach() {
            this.setMockMvc(mockMvc);
        }

        @org.junit.jupiter.api.Test
        void typical() throws Exception {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();
            dto.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            dto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);

            send(dto, HttpStatus.ACCEPTED);
            verify(jurorRecordService, times(1))
                .undoUpdateJurorToFailedToAttend(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER);
            verifyNoMoreInteractions(jurorRecordService);
        }

        @org.junit.jupiter.api.Test
        void negativeInvalidJurorNumber() throws Exception {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();

            dto.setJurorNumber(TestConstants.INVALID_JUROR_NUMBER);
            dto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);

            send(dto, HttpStatus.BAD_REQUEST);
            verifyNoInteractions(jurorRecordService);
        }

        @org.junit.jupiter.api.Test
        void negativeInvalidPoolNumber() throws Exception {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();
            dto.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            dto.setPoolNumber(TestConstants.INVALID_POOL_NUMBER);

            send(dto, HttpStatus.BAD_REQUEST);
            verifyNoInteractions(jurorRecordService);
        }
    }

    @Nested
    @DisplayName(CreateJurorRecord.URL)
    class CreateJurorRecord extends AbstractControllerTest<JurorCreateRequestDto, Void> {
        private static final String URL = BASE_URL + "/create-juror";
        private static final BureauJwtAuthentication MOCK_PRINCIPAL = mock(BureauJwtAuthentication.class);

        public CreateJurorRecord() {
            super(HttpMethod.POST, URL, MOCK_PRINCIPAL);
        }

        @BeforeEach
        void beforeEach() {
            bureauJwtPayload = TestUtils.createJwt("415", "COURT_USER");
            when(MOCK_PRINCIPAL.getPrincipal()).thenReturn(bureauJwtPayload);
            this.setMockMvc(mockMvc);
        }

        @org.junit.jupiter.api.Test
        void typical() throws Exception {
            JurorCreateRequestDto dto = JurorCreateRequestDtoTest.createValidJurorCreateRequestDto();

            send(dto, HttpStatus.CREATED);
            verify(jurorRecordService, times(1))
                .createJurorRecord(bureauJwtPayload, dto);
            verifyNoMoreInteractions(jurorRecordService);
        }

        @org.junit.jupiter.api.Test
        void negativeInvalidPayload() throws Exception {
            JurorCreateRequestDto dto = JurorCreateRequestDtoTest.createValidJurorCreateRequestDto();
            dto.setFirstName(null);
            send(dto, HttpStatus.BAD_REQUEST);
            verifyNoInteractions(jurorRecordService);
        }
    }

    @Nested
    @DisplayName("POST " + GetJurorDetailsBulkFilterable.URL)
    class GetJurorDetailsBulkFilterable extends AbstractControllerTest<List<FilterableJurorDetailsRequestDto>,
        List<FilterableJurorDetailsResponseDto>> {
        private static final String URL = BASE_URL + "/details";
        private static final BureauJwtAuthentication MOCK_PRINCIPAL = mock(BureauJwtAuthentication.class);

        protected GetJurorDetailsBulkFilterable() {
            super(HttpMethod.POST, URL, MOCK_PRINCIPAL);
            bureauJwtPayload = TestUtils.createJwt("415", "COURT_USER");
            when(MOCK_PRINCIPAL.getPrincipal()).thenReturn(bureauJwtPayload);
        }

        @BeforeEach
        void beforeEach() {
            this.setMockMvc(mockMvc);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @org.junit.jupiter.api.Test
            void typical() throws Exception {
                FilterableJurorDetailsRequestDto dto1 = new FilterableJurorDetailsRequestDto();
                dto1.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                dto1.setInclude(List.of(FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS));

                FilterableJurorDetailsRequestDto dto2 = new FilterableJurorDetailsRequestDto();
                dto2.setJurorNumber("223456789");
                dto2.setInclude(List.of(FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS));

                send(List.of(dto1, dto2), HttpStatus.OK);

                verify(jurorRecordService, times(1))
                    .getJurorDetails(dto1);
                verify(jurorRecordService, times(1))
                    .getJurorDetails(dto2);
                verifyNoMoreInteractions(jurorRecordService);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            @org.junit.jupiter.api.Test
            @DisplayName("Not Found")
            void notFound() throws Exception {
                FilterableJurorDetailsRequestDto payload = new FilterableJurorDetailsRequestDto();
                payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload.setInclude(List.of(FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS));

                MojException.NotFound exception =
                    new MojException.NotFound("Juror not found: JurorNumber: " + TestConstants.VALID_JUROR_NUMBER
                        + " Revision: null", null);
                when(jurorRecordService.getJurorDetails(payload))
                    .thenThrow(exception);

                send(List.of(payload), HttpStatus.NOT_FOUND);

                verify(jurorRecordService, times(1))
                    .getJurorDetails(payload);
                verifyNoMoreInteractions(jurorRecordService);
            }
        }
    }

    @Nested
    @DisplayName("POST" + GetJurorBankDetails.URL)
    class GetJurorBankDetails extends JurorBankDetailsDto {
        public static final String JUROR_NUMBER = "123456789";
        private static final String URL = BASE_URL + "/{juror_number}/bank-details";

        @org.junit.jupiter.api.Test
        void juryOfficerGetJurorBankDetailsHappyPath() throws Exception {
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            JurorBankDetailsDto jurorBankDetailsDto = JurorBankDetailsDto.builder()
                .bankAccountNumber("12345678")
                .sortCode("115578")
                .accountHolderName("Mr Fname Lname")
                .addressLineOne("Address Line 1")
                .addressLineTwo("Address Line 1")
                .addressLineThree("Address Line 1")
                .addressLineFour("Address Line 1")
                .addressLineFive("Address Line 1")
                .postCode("M24 4BP")
                .notes("Some notes")
                .build();

            doReturn(jurorBankDetailsDto).when(jurorRecordService).getJurorBankDetails(JUROR_NUMBER);

            mockMvc.perform(get(URL.replace("{juror_number}", JUROR_NUMBER))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(jurorBankDetailsDto)));

            verify(jurorRecordService, times(1)).getJurorBankDetails(JUROR_NUMBER);
        }

        //TODO: Fix path variable validation - JM-5010
        @Disabled("Need to fix path variable validation")
        @org.junit.jupiter.api.Test
        void juryOfficerGetJurorBankDetailsJurorNumberTooLong() throws Exception {
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            mockMvc.perform(get(URL.replace("{juror_number}", TestConstants.INVALID_JUROR_NUMBER))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andExpect(status().isBadRequest());

            verifyNoInteractions(jurorRecordService);
        }

        // TODO: Pending refactoring of the principal validation
        @Disabled("Need to fix the principle validation")
        @org.junit.jupiter.api.Test
        void bureauUserGetJurorBankDetailsDoesNotHaveAccess() throws Exception {
            BureauJWTPayload jwtPayload = TestUtils.createJwt(SecurityUtil.BUREAU_OWNER, "BUREAU_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Bureau User", 1,
                Collections.singletonList(SecurityUtil.BUREAU_OWNER)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            mockMvc.perform(get(URL.replace("{juror_number}", JUROR_NUMBER))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andExpect(status().isForbidden());

            verifyNoInteractions(jurorRecordService);
        }
    }

    @DisplayName("PATCH editBankDetails(RequestBankDetailsDto)")
    @Nested
    class EditBankDetails {
        @org.junit.jupiter.api.Test
        void happyPathEditBankDetails() throws Exception {
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSortCode("115578");
            dto.setAccountNumber("87654321");
            dto.setAccountHolderName("Mr Fname Lname");

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            doNothing().when(jurorRecordService).editJurorsBankDetails(dto);

            mockMvc.perform(patch(String.format(BASE_URL + "/update-bank-details"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isOk());

            verify(jurorRecordService, times(1)).editJurorsBankDetails(dto);
        }

        @org.junit.jupiter.api.Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void invalidAccountNumber() throws Exception {
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSortCode("115578");
            dto.setAccountNumber("987654321");
            dto.setAccountHolderName("Mr Fname Lname");

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            mockMvc.perform(patch(String.format(BASE_URL + "/update-bank-details"))
                    .content(TestUtils.asJsonString(dto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        }

        @org.junit.jupiter.api.Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void wrongSortCode() throws Exception {
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSortCode("11557812356");
            dto.setAccountNumber("87654321");
            dto.setAccountHolderName("Mr Fname Lname");

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            mockMvc.perform(patch(String.format(BASE_URL + "/update-bank-details"))
                    .content(TestUtils.asJsonString(dto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        }

        @org.junit.jupiter.api.Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void wrongAccountName() throws Exception {
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSortCode("112233");
            dto.setAccountNumber("87654321");
            dto.setAccountHolderName("Mr Fname Lname extra");

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            mockMvc.perform(patch(String.format(BASE_URL + "/update-bank-details"))
                    .content(TestUtils.asJsonString(dto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        }
    }
}
