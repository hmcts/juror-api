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
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.report.FinancialAuditReportService;
import uk.gov.hmcts.juror.api.moj.service.report.ReportService;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;

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
@DisplayName("Controller: " + ReportControllerTest.BASE_URL)
@SuppressWarnings("PMD.LawOfDemeter")
class ReportControllerTest {
    public static final String BASE_URL = "/api/v1/moj/reports";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private FinancialAuditReportService financialAuditReportService;

    @MockBean
    private UtilisationReportService utilisationReportService;

    @Nested
    @DisplayName("POST (GET) " + ViewReportStandard.URL)
    class ViewReportStandard {
        public static final String URL = BASE_URL + "/standard";

        private StandardReportRequest getValidPayload() {
            return StandardReportRequest.builder()
                .reportType("ReportType")
                .build();
        }

        private StandardReportResponse getValidResponse() {
            return StandardReportResponse.builder()
                .headings(Map.of("total_deferred", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total deferred")
                    .dataType("Long")
                    .value(7)
                    .build()))
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            StandardReportRequest request = getValidPayload();
            StandardReportResponse response = getValidResponse();
            doReturn(response).when(reportService).viewStandardReport(request);

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(response)));

            verify(reportService, times(1))
                .viewStandardReport(request);
            verifyNoMoreInteractions(reportService);
        }


        @Test
        void negativeInvalidPayload() throws Exception {
            StandardReportRequest request = getValidPayload();
            request.setReportType(null);
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(reportService);
        }

    }
}
