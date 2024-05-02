package uk.gov.hmcts.juror.api.moj.controller;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.CurrentlyDeferredRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.BulkServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DeferralMaintenanceController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {DeferralMaintenanceController.class, RestResponseEntityExceptionHandler.class,
    BulkServiceImpl.class})
@DisplayName("Controller: " + DeferralMaintenanceControllerTest.BASE_URL)
@SuppressWarnings({"PMD.ExcessiveImports"})
class DeferralMaintenanceControllerTest {
    public static final String BASE_URL = "/api/v1/moj/deferral-maintenance";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CurrentlyDeferredRepository currentlyDeferredRepository;
    @MockBean
    private JurorPoolRepository jurorPoolRepository;
    @MockBean
    private PoolRequestRepository poolRequestRepository;

    @MockBean
    private ManageDeferralsServiceImpl deferralsService;

    @Nested
    @DisplayName("GET " + BASE_URL + "/available-pools/{locationCode}/{jurorNumber}")
    class GetDeferralOptionsForDatesAndCourtLocation {
        @Test
        @DisplayName("Valid Request Court User")
        void happyPathCourtUser() throws Exception {
            final String jurorNumber = "111111111";
            final String currentCourtLocation = "415";
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");

            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1,
                    Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            SecurityContextHolder.getContext().setAuthentication(mockPrincipal);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode(currentCourtLocation);

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            PoolRequest poolRequest = new PoolRequest();
            poolRequest.setCourtLocation(courtLocation);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(jwtPayload.getOwner());
            jurorPool.setJuror(juror);
            jurorPool.setPool(poolRequest);

            DeferralDatesRequestDto dto = new DeferralDatesRequestDto();

            dto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            DeferralOptionsDto deferralOptionsDto = new DeferralOptionsDto();
            DeferralOptionsDto.OptionSummaryDto optionSummaryDto = new DeferralOptionsDto.OptionSummaryDto();

            optionSummaryDto.setWeekCommencing(LocalDate.of(2023, 5, 30));

            deferralOptionsDto.setDeferralPoolsSummary(List.of(optionSummaryDto));

            when(deferralsService.findActivePoolsForDatesAndLocCode(eq(dto),
                eq(jurorNumber),eq(currentCourtLocation),any())).thenReturn(deferralOptionsDto);

            mockMvc.perform(post(String.format(BASE_URL + "/available-pools/415/111111111/deferral_dates"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(dto))
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deferralPoolsSummary[0].weekCommencing",
                    is("2023-05-30")));

            verify(deferralsService, times(1)).findActivePoolsForDatesAndLocCode(eq(dto),
                eq(jurorNumber),eq(currentCourtLocation),any());

        }

        @Test
        @DisplayName("Valid Request Bureau User")
        void happyPathBureauUser() throws Exception {
            final String jurorNumber = "111111111";
            final String currentCourtLocation = "400";

            BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode(currentCourtLocation);

            Juror juror = new Juror();
            juror.setJurorNumber("123456789");

            PoolRequest poolRequest = new PoolRequest();
            poolRequest.setCourtLocation(courtLocation);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(currentCourtLocation);
            jurorPool.setJuror(juror);
            jurorPool.setPool(poolRequest);



            DeferralDatesRequestDto dto = new DeferralDatesRequestDto();

            dto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            DeferralOptionsDto deferralOptionsDto = new DeferralOptionsDto();
            DeferralOptionsDto.OptionSummaryDto optionSummaryDto = new DeferralOptionsDto.OptionSummaryDto();

            optionSummaryDto.setWeekCommencing(LocalDate.of(2023, 5, 30));

            deferralOptionsDto.setDeferralPoolsSummary(List.of(optionSummaryDto));

            when(deferralsService.findActivePoolsForDatesAndLocCode(eq(dto),
                eq(jurorNumber),eq(currentCourtLocation),any())).thenReturn(deferralOptionsDto);

            mockMvc.perform(post(String.format(BASE_URL + "/available-pools/400/111111111/deferral_dates"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(dto))
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deferralPoolsSummary[0].weekCommencing",
                    is("2023-05-30")));

            verify(deferralsService, times(1)).findActivePoolsForDatesAndLocCode(eq(dto),
                eq(jurorNumber),eq(currentCourtLocation),any());
        }

        @Test
        @DisplayName("Invalid Request No Location Code")
        void unHappyPathInvalidJurorNumber() throws Exception {
            final String jurorNumber = "111111111";
            String locationCode = " 415";
            BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");

            jwtPayload.setStaff(
                TestUtils.staffBuilder("Bureau User", 1,
                    Collections.singletonList("400")));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode(locationCode);

            Juror juror = new Juror();
            juror.setJurorNumber("123456789");

            PoolRequest poolRequest = new PoolRequest();
            poolRequest.setCourtLocation(courtLocation);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(locationCode);
            jurorPool.setJuror(juror);
            jurorPool.setPool(poolRequest);

            DeferralDatesRequestDto dto = new DeferralDatesRequestDto();

            dto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActive(jurorNumber, true);

            mockMvc.perform(post(String.format(BASE_URL + "/available-pools/123456788/111111111/deferral_dates"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(poolRequestRepository, never()).findActivePoolsForDateRange(any(), any(), any(), any(), anyBoolean());

            verify(currentlyDeferredRepository, never()).count(any(BooleanExpression.class));
        }

        @Test
        @DisplayName("Invalid Request No Location Code")
        void unHappyPathInvalidLocationCode() throws Exception {
            final String jurorNumber = "111111111";
            String locationCode = " 415";
            BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");

            jwtPayload.setStaff(
                TestUtils.staffBuilder("Bureau User", 1,
                    Collections.singletonList("400")));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode(locationCode);

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            PoolRequest poolRequest = new PoolRequest();
            poolRequest.setCourtLocation(courtLocation);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner("400");
            jurorPool.setJuror(juror);
            jurorPool.setPool(poolRequest);


            DeferralDatesRequestDto dto = new DeferralDatesRequestDto();

            dto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActive(jurorNumber, true);

            mockMvc.perform(post(String.format(BASE_URL + "/available-pools/null/111111111/deferral_dates"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(poolRequestRepository, never()).findActivePoolsForDateRange(any(), any(), any(), any(), anyBoolean());

            verify(currentlyDeferredRepository, never()).count(any(BooleanExpression.class));
        }

        @Test
        @DisplayName("Invalid Request No Location Code")
        void unHappyPathInvalidUrl() throws Exception {

            BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");

            jwtPayload.setStaff(
                TestUtils.staffBuilder("Bureau User", 1,
                    Collections.singletonList("400")));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            mockMvc.perform(post(String.format(BASE_URL + "/available-pools/null/111111111/deferral_dates"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(poolRequestRepository, never()).findActivePoolsForDateRange(any(), any(), any(), any(), anyBoolean());

            verify(currentlyDeferredRepository, never()).count(any(BooleanExpression.class));
        }
    }

    @Nested
    @DisplayName("POST Process juror postponement")
    class ProcessJurorPostponement {
        static final String URL = "/api/v1/moj/deferral-maintenance/juror/postpone";
        BureauJwtPayload jwtPayload;

        @Test
        @DisplayName("Process juror postponement - happy path")
        void happyPath() throws Exception {
            BureauJwtAuthentication mockPrincipal = getBureauJwtAuthentication();
            ProcessJurorPostponementRequestDto request = getProcessJurorPostponementRequestDto();
            DeferralResponseDto response = getDeferralResponseDto();

            when(deferralsService.processJurorPostponement(jwtPayload, request)).thenReturn(response);

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request))
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

            verify(deferralsService, times(1)).processJurorPostponement(any(), any());
        }

        @Test
        @DisplayName("Process juror postponement - invalid url")
        void invalidUrl() throws Exception {
            BureauJwtAuthentication mockPrincipal = getBureauJwtAuthentication();
            ProcessJurorPostponementRequestDto request = getProcessJurorPostponementRequestDto();
            DeferralResponseDto response = getDeferralResponseDto();

            when(deferralsService.processJurorPostponement(jwtPayload, request)).thenReturn(response);

            mockMvc.perform(post(URL + "/123456789")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request))
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

            verify(deferralsService, never()).processJurorPostponement(any(), any());
        }

        @Test
        @DisplayName("Process juror postponement - empty juror number list")
        void invalidPayloadEmptyJurorNumbers() throws Exception {
            BureauJwtAuthentication mockPrincipal = getBureauJwtAuthentication();
            ProcessJurorPostponementRequestDto request = getProcessJurorPostponementRequestDto();
            request.setJurorNumbers(new ArrayList<>());

            DeferralResponseDto response = getDeferralResponseDto();

            when(deferralsService.processJurorPostponement(jwtPayload, request)).thenReturn(response);

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request))
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(deferralsService, never()).processJurorPostponement(any(), any());
        }

        @Test
        @DisplayName("Process juror postponement - null juror numbers")
        void invalidPayloadNullJurorNumbers() throws Exception {
            BureauJwtAuthentication mockPrincipal = getBureauJwtAuthentication();
            ProcessJurorPostponementRequestDto request = getProcessJurorPostponementRequestDto();
            request.setJurorNumbers(null);

            DeferralResponseDto response = getDeferralResponseDto();

            when(deferralsService.processJurorPostponement(jwtPayload, request)).thenReturn(response);

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request))
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(deferralsService, never()).processJurorPostponement(any(), any());
        }

        @Test
        @DisplayName("Process juror postponement - invalid Http method")
        void invalidPayloadHttpMethod() throws Exception {
            BureauJwtAuthentication mockPrincipal = getBureauJwtAuthentication();
            ProcessJurorPostponementRequestDto request = getProcessJurorPostponementRequestDto();
            DeferralResponseDto response = getDeferralResponseDto();

            when(deferralsService.processJurorPostponement(jwtPayload, request)).thenReturn(response);

            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request))
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isMethodNotAllowed());

            verify(deferralsService, never()).processJurorPostponement(any(), any());
        }

        private BureauJwtAuthentication getBureauJwtAuthentication() {
            jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);
            return mockPrincipal;
        }

        private static ProcessJurorPostponementRequestDto getProcessJurorPostponementRequestDto() {
            ProcessJurorPostponementRequestDto request = new ProcessJurorPostponementRequestDto();
            request.setDeferralDate(LocalDate.now().plusDays(10));
            request.setPoolNumber("999999999");
            request.setExcusalReasonCode("P");
            request.setJurorNumbers(Collections.singletonList("999999999"));
            return request;
        }

        private static DeferralResponseDto getDeferralResponseDto() {
            return DeferralResponseDto.builder()
                .countJurorsPostponed(1)
                .build();
        }
    }
}
