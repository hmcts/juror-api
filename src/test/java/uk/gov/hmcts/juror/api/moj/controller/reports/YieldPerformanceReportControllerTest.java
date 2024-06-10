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
import uk.gov.hmcts.juror.api.moj.controller.reports.request.YieldPerformanceReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.report.FinancialAuditReportService;
import uk.gov.hmcts.juror.api.moj.service.report.JurySummoningMonitorReportService;
import uk.gov.hmcts.juror.api.moj.service.report.ReportService;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;
import uk.gov.hmcts.juror.api.moj.service.report.YieldPerformanceReportService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
@DisplayName("Controller: " + YieldPerformanceReportControllerTest.BASE_URL)
class YieldPerformanceReportControllerTest {
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

    @MockBean
    private YieldPerformanceReportService yieldPerformanceReportService;

    @Nested
    @DisplayName("POST (GET) " + ViewYieldReportHappy.URL)
    class ViewYieldReportHappy {
        public static final String URL = "/yield-performance";
        private YieldPerformanceReportRequest getValidPayload() {
            return YieldPerformanceReportRequest.builder()

                .build();
        }

        private YieldPerformanceReportRequest getValidCourtPayload() {
            return YieldPerformanceReportRequest.builder()
                .courtLocCodes(List.of("415230701", "415230702"))
                .toDate(LocalDate.now())
                .fromDate(LocalDate.now().minusDays(1))
                .build();
        }

        private YieldPerformanceReportResponse getValidResponse() {
            return YieldPerformanceReportResponse.builder()
                .headings(Map.of())
                .tableData(new YieldPerformanceReportResponse.TableData())
                .build();
        }

        @Test
        void validSearchByCourt() throws Exception {
            YieldPerformanceReportRequest request = getValidCourtPayload();
            YieldPerformanceReportResponse response = getValidResponse();

            doReturn(response).when(yieldPerformanceReportService).viewYieldPerformanceReport(request);

            mockMvc.perform(post(BASE_URL + URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(response)));

            verify(yieldPerformanceReportService, times(1))
                .viewYieldPerformanceReport(request);
        }

        @Test
        void negativeSearchByCourtButNoFromDate() throws Exception {
            YieldPerformanceReportRequest request = getValidCourtPayload();
            request.setFromDate(null);
            mockMvc.perform(post(BASE_URL + URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(yieldPerformanceReportService);
        }

        @Test
        void negativeSearchByCourtButNoToDate() throws Exception {
            YieldPerformanceReportRequest request = getValidCourtPayload();
            request.setFromDate(null);
            mockMvc.perform(post(BASE_URL + URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(yieldPerformanceReportService);
        }

    }
}
