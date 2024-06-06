package uk.gov.hmcts.juror.api.moj.controller;

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
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.audit.JurorAuditService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuditController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        AuditController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + AuthenticationControllerTest.BASE_URL)
public class AuditControllerTest {

    public static final String BASE_URL = "/api/v1/moj/audit";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JurorAuditService jurorAuditService;
    @InjectMocks
    private AuditController auditController;

    @Nested
    @DisplayName("GET " + AuditControllerTest.GetAllPoolAuditsForDay.URL)
    class GetAllPoolAuditsForDay {
        public static final String URL = BASE_URL + "/{date}/pool";

        private String toUrl(LocalDate date) {
            return URL.replace("{date}", DateTimeFormatter.ISO_DATE.format(date));
        }

        @Test
        void positiveTypical() throws Exception {
            List<String> audits = List.of("P123", "P456", "789");
            LocalDate date = LocalDate.of(2024, 1, 1);
            doReturn(audits).when(jurorAuditService).getAllPoolAuditsForDay(date);

            mockMvc.perform(get(toUrl(date))
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(audits)));

            verify(jurorAuditService, times(1))
                .getAllPoolAuditsForDay(date);
            verifyNoMoreInteractions(jurorAuditService);
        }

        @Test
        void negativeInvalidDate() throws Exception {
            mockMvc.perform(get(URL.replace("{date}", "INVALID"))
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(jurorAuditService);
        }
    }
}
