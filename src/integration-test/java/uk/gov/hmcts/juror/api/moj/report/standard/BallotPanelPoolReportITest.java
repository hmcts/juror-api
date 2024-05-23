package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public class BallotPanelPoolReportITest  extends AbstractStandardReportControllerITest {
    @Autowired
    public BallotPanelPoolReportITest(TestRestTemplate template) {
        super(template, BallotPanelPoolReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
                                 .poolNumber("415230103")
                                 .build());
    }
    @Test
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .assertEquals(getTypicalResponse());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
    void negativeInvalidPayload() {
        StandardReportRequest request = getValidPayload();
        request.setPoolNumber(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("poolNumber: must not be null");
    }
    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")//False positive
    void negativeUnauthorised() {
        testBuilder()
            .jwt("")
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this pool");
    }

    private StandardReportResponse getTypicalResponse(){
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                          .add("report Created", StandardReportResponse.DataTypeValue.builder()
                              .displayName(null)
                              .dataType("LocalDateTime")
                              .value(LocalDateTime.now())
                              .build()))
            .tableData(
                StandardReportResponse.TableData.<List<LinkedHashMap<String, Object>>>builder()
                    .headings(
                        List.of(
                            StandardReportResponse.TableData.Heading.builder()
                                .id("juror_number")
                                .name("Juror Number")
                                .dataType("String")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("first_name")
                                .name("First Name")
                                .dataType("String")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("last_name")
                                .name("Last Name")
                                .dataType("String")
                                .headings(null)
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("juror_postcode")
                                .name("Postcode")
                                .dataType("String")
                                .headings(null)
                                .build()))
                    .data(List.of())
                    .build())
            .build();
    }
}
