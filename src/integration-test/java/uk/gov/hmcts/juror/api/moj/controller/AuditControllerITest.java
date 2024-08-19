package uk.gov.hmcts.juror.api.moj.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AuditControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
public class AuditControllerITest {
    public static final String BASE_URL = "/api/v1/moj/audit";
    private final TestRestTemplate template;


    @Nested
    @DisplayName("GET  " + AuditControllerITest.GetAllPoolAuditsForDay.URL)
    @Sql({
        "/db/mod/truncate.sql",
        "/db/administration/createJudges.sql",
        "/db/administration/createCourtRooms.sql",
        "/db/mod/reports/PoolAttendanceAuditReportITest_typical.sql"
    })
    class GetAllPoolAuditsForDay extends AbstractControllerIntegrationTest<Void, List<String>> {
        public static final String URL = BASE_URL + "/{date}/pool";

        protected GetAllPoolAuditsForDay() {
            super(HttpMethod.GET, template, HttpStatus.OK);
        }

        private String toUrl(LocalDate date) {
            return toUrl(DateTimeFormatter.ISO_DATE.format(date));
        }

        private String toUrl(String date) {
            return URL.replace("{date}", date);
        }

        @Override
        protected String getValidUrl() {
            return toUrl(LocalDate.of(2024, 1, 1));
        }

        @Override
        protected String getValidJwt() {
            return createJwt("test_court_primary", "415",
                UserType.COURT, Set.of(), "415");
        }

        @Override
        protected Void getValidPayload() {
            return null;
        }

        @Test
        void positiveTypical() {
            testBuilder()
                .triggerValid()
                .assertEquals(List.of("P1234", "P12345678"));
        }

        @Test
        void positiveNotFound() {
            testBuilder()
                .url(toUrl(LocalDate.of(2023, 1, 1)))
                .triggerValid()
                .assertEquals(List.of());
        }

        @Test
        void negativeIsBureau() {
            testBuilder()
                .jwt(getBureauJwt())
                .triggerInvalid()
                .assertForbiddenResponse();
        }

        @Test
        void negativeInvalidDateFormat() {
            testBuilder()
                .url(toUrl("INVALID"))
                .triggerInvalid()
                .assertInvalidPathParam("INVALID is the incorrect data type or is not in the expected format (date)");
        }
    }
}
