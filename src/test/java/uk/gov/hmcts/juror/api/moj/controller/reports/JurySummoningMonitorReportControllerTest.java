package uk.gov.hmcts.juror.api.moj.controller.reports;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.report.FinancialAuditReportService;
import uk.gov.hmcts.juror.api.moj.service.report.JurySummoningMonitorReportService;
import uk.gov.hmcts.juror.api.moj.service.report.ReportService;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReportController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        ReportController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + JurySummoningMonitorReportControllerTest.BASE_URL)
class JurySummoningMonitorReportControllerTest {
    public static final String BASE_URL = "/api/v1/moj/reports";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private FinancialAuditReportService financialAuditReportService;

    @MockBean
    private UtilisationReportService utilisationReportService;

    @MockBean
    private JurySummoningMonitorReportService jurySummoningMonitorReportService;

    @Nested
    @DisplayName("POST (GET) " + ViewReportStandard.URL)
    class ViewReportStandard {
        public static final String URL = BASE_URL + "/jury-summoning-monitor";

        private JurySummoningMonitorReportRequest getValidPayload() {
            return JurySummoningMonitorReportRequest.builder()
                .searchBy("POOL")
                .poolNumber("415230701")
                .build();
        }

        private JurySummoningMonitorReportRequest getValidCourtPayload() {
            return JurySummoningMonitorReportRequest.builder()
                .searchBy("COURT")
                .courtLocCodes(List.of("415230701", "415230702"))
                .toDate(LocalDate.now())
                .fromDate(LocalDate.now().minusDays(1))
                .build();
        }

        private JurySummoningMonitorReportResponse getValidResponse() {
            return JurySummoningMonitorReportResponse.builder()
                .headings(Map.of())
                .totalJurorsNeeded(100)
                .totalConfirmedJurors(50)
                .additionalSummonsIssued(40)
                .totalUnavailable(10)
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            JurySummoningMonitorReportRequest request = getValidPayload();
            JurySummoningMonitorReportResponse response = getValidResponse();
            doReturn(response).when(jurySummoningMonitorReportService).viewJurySummoningMonitorReport(request);

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(response)));

            verify(jurySummoningMonitorReportService, times(1))
                .viewJurySummoningMonitorReport(request);
            verifyNoMoreInteractions(reportService);
        }

        @Test
        void negativeSearchByNull() throws Exception {
            JurySummoningMonitorReportRequest request = getValidPayload();
            request.setSearchBy(null);
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(jurySummoningMonitorReportService);
        }

        @Test
        void negativeSearchByPoolButNoPoolNumber() throws Exception {
            JurySummoningMonitorReportRequest request = getValidPayload();
            request.setPoolNumber(null);
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(jurySummoningMonitorReportService);
        }

        @Test
        void validSearchByCourt() throws Exception {
            JurySummoningMonitorReportRequest request = getValidCourtPayload();
            JurySummoningMonitorReportResponse response = getValidResponse();
            request.setPoolNumber(null);
            doReturn(response).when(jurySummoningMonitorReportService).viewJurySummoningMonitorReport(request);

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(response)));

            verify(jurySummoningMonitorReportService, times(1))
                .viewJurySummoningMonitorReport(request);
            verifyNoMoreInteractions(reportService);
        }

        @Test
        void negativeSearchByCourtButNoFromDate() throws Exception {
            JurySummoningMonitorReportRequest request = getValidCourtPayload();
            request.setFromDate(null);
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(jurySummoningMonitorReportService);
        }

        @Test
        void negativeSearchByCourtButNoToDate() throws Exception {
            JurySummoningMonitorReportRequest request = getValidCourtPayload();
            request.setFromDate(null);
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(jurySummoningMonitorReportService);
        }

    }
}
