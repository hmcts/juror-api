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
import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationHolidaysServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdministrationHolidaysController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        AdministrationHolidaysController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + AdministrationHolidaysControllerTest.BASE_URL)
public class AdministrationHolidaysControllerTest {

    public static final String BASE_URL = "/api/v1/moj/administration";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationHolidaysServiceImpl administrationHolidaysService;

    @InjectMocks
    private AdministrationHolidaysController administrationHolidaysController;

    @Nested
    @DisplayName("GET " + ViewBankHolidays.URL)
    class ViewBankHolidays {
        public static final String URL = BASE_URL + "/bank-holidays";

        @Test
        void positiveTypical() throws Exception {
            Map<Integer, List<HolidayDate>> response = Map.of(
                2024, List.of(new HolidayDate(LocalDate.now(), "Some bank holiday"))
            );

            doReturn(response).when(administrationHolidaysService).viewBankHolidays();
            mockMvc.perform(get(URL))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.2024.[0].description", CoreMatchers.is("Some bank holiday")));

            verify(administrationHolidaysService, times(1))
                .viewBankHolidays();
            verifyNoMoreInteractions(administrationHolidaysService);
        }
    }


    @Nested
    @DisplayName("GET " + ViewNonSittingDays.URL)
    class ViewNonSittingDays {
        public static final String URL = BASE_URL + "/non-sitting-days/{loc_code}";


        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        @Test
        void positiveTypical() throws Exception {
            List<HolidayDate> response = List.of(new HolidayDate(LocalDate.now(), "Some bank holiday"));

            doReturn(response).when(administrationHolidaysService).viewNonSittingDays(any());
            mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description", CoreMatchers.is("Some bank holiday")));

            verify(administrationHolidaysService, times(1))
                .viewNonSittingDays(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(administrationHolidaysService);
        }

        @Test
        void negativeInvalidLocCodeType() throws Exception {
            mockMvc.perform(get(toUrl("INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationHolidaysService);
        }
    }

    @Nested
    @DisplayName("DELETE " + DeleteNonSittingDays.URL)
    class DeleteNonSittingDays {
        public static final String URL = BASE_URL + "/non-sitting-days/{loc_code}/{date}";


        private String toUrl(String locCode, LocalDate date) {
            return toUrl(locCode, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        private String toUrl(String locCode, String date) {
            return URL.replace("{loc_code}", locCode)
                .replace("{date}", date);
        }

        @Test
        void positiveTypical() throws Exception {
            mockMvc.perform(delete(toUrl(TestConstants.VALID_COURT_LOCATION, LocalDate.of(2022, 1, 1))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(administrationHolidaysService, times(1))
                .deleteNonSittingDays(TestConstants.VALID_COURT_LOCATION, LocalDate.of(2022, 1, 1));
            verifyNoMoreInteractions(administrationHolidaysService);
        }

        @Test
        void negativeInvalidLocCodeType() throws Exception {
            mockMvc.perform(delete(toUrl("INVALID", LocalDate.now())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationHolidaysService);
        }

        @Test
        void negativeInvalidDate() throws Exception {
            mockMvc.perform(delete(toUrl(TestConstants.VALID_COURT_LOCATION, "INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationHolidaysService);
        }
    }

    @Nested
    @DisplayName("POST " + AddNonSittingDays.URL)
    class AddNonSittingDays {
        public static final String URL = BASE_URL + "/non-sitting-days/{loc_code}";

        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        private HolidayDate getValidPayload() {
            return new HolidayDate(LocalDate.now(), "Some bank holiday");
        }

        @Test
        void positiveTypical() throws Exception {
            HolidayDate holidayDate = getValidPayload();

            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(holidayDate)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(administrationHolidaysService, times(1))
                .addNonSittingDays(TestConstants.VALID_COURT_LOCATION, holidayDate);
            verifyNoMoreInteractions(administrationHolidaysService);
        }

        @Test
        void negativeInvalidLocCodeType() throws Exception {
            mockMvc.perform(post(toUrl(TestConstants.INVALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationHolidaysService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            HolidayDate holidayDate = getValidPayload();
            holidayDate.setDate(null);
            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(holidayDate)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationHolidaysService);
        }
    }

}
