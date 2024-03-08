package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesDto;
import uk.gov.hmcts.juror.api.moj.domain.UpdateCourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.AdministrationService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdministrationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        AdministrationController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + AdministrationControllerTest.BASE_URL)
@SuppressWarnings("PMD.ExcessiveImports")
public class AdministrationControllerTest {

    public static final String BASE_URL = "/api/v1/moj/administration";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationService administrationService;

    @MockBean
    private JurorExpenseService jurorExpenseService;

    @InjectMocks
    private AdministrationController administrationController;

    @Nested
    @DisplayName("GET " + ViewCodeAndDescriptions.URL)
    class ViewCodeAndDescriptions {
        public static final String URL = BASE_URL + "/codes/{code_type}";

        private String toUrl(CodeType codeType) {
            return toUrl(codeType.name());
        }

        private String toUrl(String codeType) {
            return URL.replace("{code_type}", codeType);
        }

        @Test
        void positiveTypical() throws Exception {
            CodeType codeType = CodeType.EXCUSAL_AND_DEFERRAL;
            doReturn(
                List.of(
                    new CodeDescriptionResponse("code1", "description1", null),
                    new CodeDescriptionResponse("code2", "description2", true))
            ).when(administrationService).viewCodeAndDescriptions(any());
            mockMvc.perform(get(toUrl(codeType)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                .andExpect(jsonPath("$[0].code", CoreMatchers.is("code1")))
                .andExpect(jsonPath("$[0].description", CoreMatchers.is("description1")))
                .andExpect(jsonPath("$[0].is_active", CoreMatchers.nullValue()))
                .andExpect(jsonPath("$[1].code", CoreMatchers.is("code2")))
                .andExpect(jsonPath("$[1].description", CoreMatchers.is("description2")))
                .andExpect(jsonPath("$[1].is_active", CoreMatchers.is(true)))
            ;


            verify(administrationService, times(1))
                .viewCodeAndDescriptions(codeType);
            verifyNoMoreInteractions(administrationService);
        }

        @Test
        void negativeInvalidCodeType() throws Exception {
            mockMvc.perform(get(toUrl("INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationService);
        }
    }

    @Nested
    @DisplayName("GET " + ViewCourtDetails.URL)
    class ViewCourtDetails {
        public static final String URL = BASE_URL + "/courts/{loc_code}";


        private String toUrl(String codeType) {
            return URL.replace("{loc_code}", codeType);
        }

        @Test
        void positiveTypical() throws Exception {
            CourtDetailsDto courtDetailsDto = new CourtDetailsDto();
            courtDetailsDto.setCourtCode(TestConstants.VALID_COURT_LOCATION);

            doReturn(courtDetailsDto).when(administrationService).viewCourt(any());
            mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.court_code", CoreMatchers.is(TestConstants.VALID_COURT_LOCATION)));

            verify(administrationService, times(1))
                .viewCourt(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(administrationService);
        }

        @Test
        void negativeInvalidLocCodeType() throws Exception {
            mockMvc.perform(get(toUrl("INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationService);
        }
    }

    @Nested
    @DisplayName("PUT " + UpdateCourtRates.URL)
    class UpdateCourtRates {
        public static final String URL = BASE_URL + "/courts/{loc_code}/rates";


        private String toUrl(String codeType) {
            return URL.replace("{loc_code}", codeType);
        }

        private CourtRates getValidPayload() {
            return CourtRates.builder()
                .publicTransportSoftLimit(new BigDecimal("1.01"))
                .taxiSoftLimit(new BigDecimal("1.02"))
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            CourtRates payload = getValidPayload();
            mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(administrationService, times(1))
                .updateCourtRates(TestConstants.VALID_COURT_LOCATION, payload);
            verifyNoMoreInteractions(administrationService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(put(toUrl(TestConstants.INVALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationService);
        }


        @Test
        void negativeInvalidPayload() throws Exception {
            CourtRates payload = getValidPayload();
            payload.setTaxiSoftLimit(new BigDecimal("-1"));
            mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationService);
        }
    }

    @Nested
    @DisplayName("PUT " + UpdateCourtDetails.URL)
    class UpdateCourtDetails {
        public static final String URL = BASE_URL + "/courts/{loc_code}";

        private String toUrl(String codeType) {
            return URL.replace("{loc_code}", codeType);
        }

        private UpdateCourtDetailsDto getValidPayload() {
            return UpdateCourtDetailsDto.builder()
                .mainPhoneNumber("0123456789")
                .defaultAttendanceTime(LocalTime.of(9, 0))
                .assemblyRoomId(3L)
                .costCentre("CSTCNR1")
                .signature("COURT1 SIGNATURE")
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            UpdateCourtDetailsDto updateCourtDetailsDto = getValidPayload();
            mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(updateCourtDetailsDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(administrationService, times(1))
                .updateCourt(TestConstants.VALID_COURT_LOCATION, updateCourtDetailsDto);
            verifyNoMoreInteractions(administrationService);
        }

        @Test
        void negativeInvalidLocCodeType() throws Exception {
            mockMvc.perform(put(toUrl(TestConstants.INVALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationService);
        }


        @Test
        void negativeInvalidPayload() throws Exception {
            UpdateCourtDetailsDto updateCourtDetailsDto = getValidPayload();
            updateCourtDetailsDto.setCostCentre(null);
            mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(updateCourtDetailsDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationService);
        }
    }

    @Nested
    @DisplayName("GET " + ViewAllCourtsDetails.URL)
    class ViewAllCourtsDetails {
        public static final String URL = BASE_URL + "/courts";


        @Test
        void positiveTypical() throws Exception {
            doReturn(List.of(mock(CourtDetailsReduced.class),
                mock(CourtDetailsReduced.class),
                mock(CourtDetailsReduced.class))).when(administrationService).viewCourts();
            mockMvc.perform(get(URL))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", CoreMatchers.is(3)));

            verify(administrationService, times(1))
                .viewCourts();
            verifyNoMoreInteractions(administrationService);
        }
    }


    @Nested
    @DisplayName("GET " + ViewExpenseDetails.URL)
    class ViewExpenseDetails {
        public static final String URL = BASE_URL + "/expenses/rates";


        @Test
        void positiveTypical() throws Exception {
            CourtDetailsDto courtDetailsDto = new CourtDetailsDto();
            courtDetailsDto.setCourtCode(TestConstants.VALID_COURT_LOCATION);

            doReturn(ExpenseRates.builder()
                .carMileageRatePerMile0Passengers(new BigDecimal("1.01"))
                .carMileageRatePerMile1Passengers(new BigDecimal("1.02"))
                .carMileageRatePerMile2OrMorePassengers(new BigDecimal("1.03"))
                .motorcycleMileageRatePerMile0Passengers(new BigDecimal("1.04"))
                .motorcycleMileageRatePerMile1Passengers(new BigDecimal("1.05"))
                .bikeRate(new BigDecimal("1.06"))
                .limitFinancialLossHalfDay(new BigDecimal("1.07"))
                .limitFinancialLossFullDay(new BigDecimal("1.08"))
                .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
                .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
                .subsistenceRateStandard(new BigDecimal("1.11"))
                .subsistenceRateLongDay(new BigDecimal("1.12"))
                .build()).when(jurorExpenseService).getCurrentExpenseRates();


            mockMvc.perform(get(URL))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(ExpenseRatesDto.builder()
                    .carMileageRatePerMile0Passengers(new BigDecimal("1.01"))
                    .carMileageRatePerMile1Passengers(new BigDecimal("1.02"))
                    .carMileageRatePerMile2OrMorePassengers(new BigDecimal("1.03"))
                    .motorcycleMileageRatePerMile0Passengers(new BigDecimal("1.04"))
                    .motorcycleMileageRatePerMile1Passengers(new BigDecimal("1.05"))
                    .bikeRate(new BigDecimal("1.06"))
                    .limitFinancialLossHalfDay(new BigDecimal("1.07"))
                    .limitFinancialLossFullDay(new BigDecimal("1.08"))
                    .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
                    .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
                    .subsistenceRateStandard(new BigDecimal("1.11"))
                    .subsistenceRateLongDay(new BigDecimal("1.12"))
                    .build())));

            verify(jurorExpenseService, times(1))
                .getCurrentExpenseRates();
            verifyNoMoreInteractions(jurorExpenseService);
            verifyNoInteractions(administrationService);
        }

    }

    @Nested
    @DisplayName("PUT " + UpdateExpenseDetails.URL)
    class UpdateExpenseDetails {
        public static final String URL = BASE_URL + "/expenses/rates";


        private ExpenseRatesDto getValidPayload() {
            return ExpenseRatesDto.builder()
                .carMileageRatePerMile0Passengers(new BigDecimal("1.01"))
                .carMileageRatePerMile1Passengers(new BigDecimal("1.02"))
                .carMileageRatePerMile2OrMorePassengers(new BigDecimal("1.03"))
                .motorcycleMileageRatePerMile0Passengers(new BigDecimal("1.04"))
                .motorcycleMileageRatePerMile1Passengers(new BigDecimal("1.05"))
                .bikeRate(new BigDecimal("1.06"))
                .limitFinancialLossHalfDay(new BigDecimal("1.07"))
                .limitFinancialLossFullDay(new BigDecimal("1.08"))
                .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
                .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
                .subsistenceRateStandard(new BigDecimal("1.11"))
                .subsistenceRateLongDay(new BigDecimal("1.12"))
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            ExpenseRatesDto payload = getValidPayload();
            mockMvc.perform(put(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(jurorExpenseService, times(1))
                .updateExpenseRates(payload);
            verifyNoMoreInteractions(jurorExpenseService);
            verifyNoInteractions(administrationService);
        }


        @Test
        void negativeInvalidPayload() throws Exception {
            ExpenseRatesDto payload = getValidPayload();
            payload.setBikeRate(null);
            mockMvc.perform(put(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationService);
        }
    }
}
