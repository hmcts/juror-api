package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportControllerITest;
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;

import java.util.List;

@Sql ({"/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/administration/createUsers.sql",
    "/db/reports/oncall.sql"
})
public class OnCallReportITest extends AbstractStandardReportControllerITest {

    @Autowired
    OnCallReportITest(TestRestTemplate testRestTemplate) {
        super(testRestTemplate, OnCallReport.class);
    }

    @Override
    protected String getValidJwt() {
        return getCourtJwt("415");
    }

    @Override
    protected StandardReportRequest getValidPayload() {
        return addReportType(StandardReportRequest.builder()
            .reportType("OnCallReport")
            .poolNumber("416220901")
            .build());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void positiveTypicalCourt() {
        testBuilder()
            .triggerValid()
            .responseConsumer(this::verifyAndRemoveReportCreated)
            .assertEquals(getTypicalResponse());
    }

    @Test
    void negativeInvalidPayloadMissingPoolNumber() {
        StandardReportRequest request = getValidPayload();
        request.setPoolNumber(null);
        testBuilder()
            .payload(addReportType(request))
            .triggerInvalid()
            .assertInvalidPathParam("poolNumber: must not be null");
    }

    @Test
    void negativeUnauthorisedBureauUser() {
        testBuilder()
            .jwt(getBureauJwt())
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this report");
    }

    @Test
    void negativeUnauthorisedUserDoesNotOwnCourtLocation() {
        testBuilder()
            .jwt(getCourtJwt("414"))
            .triggerInvalid()
            .assertMojForbiddenResponse("User not allowed to access this pool");
    }



    AbstractReportResponse getTypicalResponse() {
        return StandardReportResponse.builder()
            .headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()
                .add("pool_number", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value("416220901")
                    .build())
                .add("pool_type", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
                    .build())
                .add("service_start_date", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("LocalDate")
                    .value("2023-05-30")
                    .build())
                .add("total_on_call", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total on call")
                    .dataType("Long")
                    .value(3L)
                    .build())
                .add("court_name", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build()))
            .tableData(
                StandardReportResponse.TableData.<StandardTableData>builder()
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
                    .data(StandardTableData.of(
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000001")
                            .add("first_name", "FIRSTNAMEONE")
                            .add("last_name", "LASTNAMEONE")
                            .add("mobile_phone", "44776-301-1110")
                            .add("home_phone", "44776-301-2222"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000002")
                            .add("first_name", "FIRSTNAMETWO")
                            .add("last_name", "LASTNAMETWO")
                            .add("mobile_phone", "44776-301-1111")
                            .add("home_phone", "44776-301-1110"),
                        new ReportLinkedMap<String, Object>()
                            .add("juror_number", "415000004")
                            .add("first_name", "FIRSTNAMEFOUR")
                            .add("last_name", "LASTNAMEFOUR")
                            .add("mobile_phone", "44776-301-1110")
                            .add("home_phone", "44141101-1112")))
                    .build())
            .build();
    }
}