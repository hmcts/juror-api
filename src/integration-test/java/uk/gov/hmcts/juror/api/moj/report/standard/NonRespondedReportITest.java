package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;
import uk.gov.hmcts.juror.api.moj.report.TmpSupport;

import java.util.List;

@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/mod/reports/NonRespondedReportITest_typical.sql"
})
public class NonRespondedReportITest extends AbstractReportControllerITest {
    @Autowired
    public NonRespondedReportITest(TestRestTemplate template) {
        super(template, NonRespondedReport.class);
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
            .printResponse()
            .responseConsumer(response -> System.out.println(TmpSupport.asString(response)))
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    void positiveTypicalBureau() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerValid()
            .printResponse()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }


    @Test
    void negativeInvalidPayload() {
        StandardReportRequest request = getValidPayload();
        request.setPoolNumber(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .printResponse()
            .assertInvalidPathParam("poolNumber: must not be null");
    }

    @Test
    void negativeUnauthorised() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerInvalid()
            .printResponse()
            .assertMojForbiddenResponse("User not allowed to access this pool");
    }


    private StandardReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("total_non_responded", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total non-responded")
                    .dataType("Long")
                    .value(2)
                    .build())
                .add("court_name", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build())
                .add("service_start_date", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("LocalDate")
                    .value("2023-01-05")
                    .build())
                .add("pool_number", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value("415230103")
                    .build())
                .add("pool_type", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
                    .build()))
            .tableData(
                StandardReportResponse.TableData.builder()
                    .headings(List.of(
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
                            .id("mobile_phone")
                            .name("Mobile Phone")
                            .dataType("String")
                            .headings(null)
                            .build(),
                        StandardReportResponse.TableData.Heading.builder()
                            .id("home_phone")
                            .name("Home Phone")
                            .dataType("String")
                            .headings(null)
                            .build()))
                    .data(List.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500024")
                            .add("first_name", "John4")
                            .add("last_name", "Smith4")
                            .add("mobile_phone", "400000001")
                            .add("home_phone", "400000003"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "641500026")
                            .add("first_name", "John6")
                            .add("last_name", "Smith6")
                            .add("mobile_phone", "600000001")
                            .add("home_phone", "600000003")))
                    .build())
            .build();
    }
}
