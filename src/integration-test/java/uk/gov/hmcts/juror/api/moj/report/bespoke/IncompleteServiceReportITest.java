package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql({
    "/db/mod/truncate.sql",
    "/db/mod/reports/IncompleteServiceReportITest_typical.sql"
})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: /api/v1/moj/reports/incomplete-service")
@SuppressWarnings("PMD.LooseCoupling")
class IncompleteServiceReportITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate template;
    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders() throws Exception {
        List<String> staffCourts = Collections.singletonList("415");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner("415")
            .staff(TestUtils.staffBuilder("staffName", 1, staffCourts))
            .build());
        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Test
    void positiveTypicalCourt() {

        final URI uri = URI.create("/api/v1/moj/reports/standard");

        ResponseEntity<String> response = template.exchange(
            new RequestEntity<>(
                StandardReportRequest.builder()
                    .reportType("IncompleteServiceReport")
                    .date(LocalDate.now())
                    .locCode("415")
                    .build(),
                httpHeaders, HttpMethod.POST, uri), String.class);

        System.out.println("TMP: " + response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

//        verifyStandardResponse(responseBody);

    }

    private void verifyStandardResponse(LinkedHashMap<String, Object> responseBody) {
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("headings")).isNotNull();
        assertThat(responseBody.get("table_data")).isNotNull();
        assertThat(responseBody.get("headings")).isInstanceOf(LinkedHashMap.class);
        assertThat(responseBody.get("table_data")).isInstanceOf(LinkedHashMap.class);

        LinkedHashMap<String, Object> headings = validateHeadings(responseBody);

        LinkedHashMap<String, Object> tableDataMap = (LinkedHashMap<String, Object>) responseBody.get("table_data");
        assertThat(headings).isNotNull();
        assertThat(headings).hasSize(4);

        List<LinkedHashMap<String, Object>> tableHeadings = (List<LinkedHashMap<String, Object>>) tableDataMap.get(
            "headings");
        List<LinkedHashMap<String, Object>> tableData = (List<LinkedHashMap<String, Object>>) tableDataMap.get("data");

        assertThat(tableHeadings).isNotNull();
        assertThat(tableData).isNotNull();

        assertThat(tableHeadings.size()).isEqualTo(5);
        validateTableHeadings(tableHeadings);

        assertThat(tableData).hasSize(3);
        validateTableData(tableData);
    }

    @NotNull
    private LinkedHashMap<String, Object> validateHeadings(
        LinkedHashMap<String, Object> responseBody) {
        LinkedHashMap<String, Object> headings = (LinkedHashMap<String, Object>) responseBody.get("headings");
        assertThat(headings).isNotNull();
        assertThat(headings).hasSize(4);

        assertThat(headings.get("court_name")).isNotNull();
        LinkedHashMap<String, Object> courtName = (LinkedHashMap<String, Object>) headings.get("court_name");
        assertThat(courtName.get("displayName")).isEqualTo("Court Name");
        assertThat(courtName.get("dataType")).isEqualTo("String");
        assertThat(courtName.get("value")).isEqualTo("Chester (415)");

        assertThat(headings.get("cut_off_date")).isNotNull();
        LinkedHashMap<String, Object> cutOffDate = (LinkedHashMap<String, Object>) headings.get("cut_off_date");
        assertThat(cutOffDate.get("displayName")).isEqualTo("Cut-off Date");
        assertThat(cutOffDate.get("dataType")).isEqualTo("LocalDate");
        assertThat(cutOffDate.get("value")).isEqualTo(LocalDate.now().toString());

        assertThat(headings.get("total_incomplete_service")).isNotNull();
        LinkedHashMap<String, Object> totalIncompleteService = (LinkedHashMap<String, Object>) headings.get(
            "total_incomplete_service");
        assertThat(totalIncompleteService.get("displayName")).isEqualTo("Total Incomplete Service");
        assertThat(totalIncompleteService.get("dataType")).isEqualTo("Integer");
        assertThat(totalIncompleteService.get("value")).isEqualTo(3);

        assertThat(headings.get("report_created")).isNotNull();
        LinkedHashMap<String, Object> reportCreated = (LinkedHashMap<String, Object>) headings.get("report_created");
        assertThat(reportCreated.get("displayName")).isEqualTo("Report Created");
        assertThat(reportCreated.get("dataType")).isEqualTo("LocalDateTime");
        List<Integer> createdDateList = (List<Integer>) reportCreated.get("value");

        assertThat(createdDateList).hasSize(7);
        assertThat(createdDateList.get(0)).isEqualTo(LocalDate.now().getYear());
        assertThat(createdDateList.get(1)).isEqualTo(LocalDate.now().getMonthValue());
        assertThat(createdDateList.get(2)).isEqualTo(LocalDate.now().getDayOfMonth());
        assertThat(createdDateList.get(3)).isBetween(0, 23); // hour
        assertThat(createdDateList.get(4)).isBetween(0, 59); // minute
        assertThat(createdDateList.get(5)).isBetween(0, 59); // seconds
        return headings;
    }

    private void validateTableData(List<LinkedHashMap<String, Object>> tableData) {
        LinkedHashMap<String, Object> jurorOne = tableData.get(0);
        assertThat(jurorOne.get("juror_number")).isEqualTo("641500003");
        assertThat(jurorOne.get("first_name")).isEqualTo("FNAMETHREE");
        assertThat(jurorOne.get("last_name")).isEqualTo("LNAMETHREE");
        assertThat(jurorOne.get("pool_number")).isEqualTo("415240601");
        assertThat(jurorOne.get("next_attendance_date")).isEqualTo(LocalDate.now().minusDays(1).toString());

        LinkedHashMap<String, Object> jurorTwo = tableData.get(1);
        assertThat(jurorTwo.get("juror_number")).isEqualTo("641500011");
        assertThat(jurorTwo.get("first_name")).isEqualTo("FNAMEONEONE");
        assertThat(jurorTwo.get("last_name")).isEqualTo("LNAMEONEONE");
        assertThat(jurorTwo.get("pool_number")).isEqualTo("415240601");
        assertThat(jurorTwo.get("next_attendance_date")).isEqualTo(LocalDate.now().minusDays(1).toString());

        LinkedHashMap<String, Object> jurorThree = tableData.get(2);
        assertThat(jurorThree.get("juror_number")).isEqualTo("641500021");
        assertThat(jurorThree.get("first_name")).isEqualTo("FNAMETWOONE");
        assertThat(jurorThree.get("last_name")).isEqualTo("LNAMETWOONE");
        assertThat(jurorThree.get("pool_number")).isEqualTo("415240601");
        assertThat(jurorThree.get("next_attendance_date")).isEqualTo(LocalDate.now().minusDays(1).toString());
    }

    private void validateTableHeadings(List<LinkedHashMap<String, Object>> tableHeadings) {
        LinkedHashMap<String, Object> jurorNumber = tableHeadings.get(0);
        assertThat(jurorNumber.get("id")).isEqualTo("juror_number");
        assertThat(jurorNumber.get("name")).isEqualTo("Juror Number");
        assertThat(jurorNumber.get("dataType")).isEqualTo("String");

        LinkedHashMap<String, Object> firstName = tableHeadings.get(1);
        assertThat(firstName.get("id")).isEqualTo("first_name");
        assertThat(firstName.get("name")).isEqualTo("First Name");
        assertThat(firstName.get("dataType")).isEqualTo("String");

        LinkedHashMap<String, Object> lastName = tableHeadings.get(2);
        assertThat(lastName.get("id")).isEqualTo("last_name");
        assertThat(lastName.get("name")).isEqualTo("Last Name");
        assertThat(lastName.get("dataType")).isEqualTo("String");

        LinkedHashMap<String, Object> poolNumber = tableHeadings.get(3);
        assertThat(poolNumber.get("id")).isEqualTo("pool_number");
        assertThat(poolNumber.get("name")).isEqualTo("Pool Number");
        assertThat(poolNumber.get("dataType")).isEqualTo("String");

        LinkedHashMap<String, Object> nextAttendanceDate = tableHeadings.get(4);
        assertThat(nextAttendanceDate.get("id")).isEqualTo("next_attendance_date");
        assertThat(nextAttendanceDate.get("name")).isEqualTo("Next attendance date");
        assertThat(nextAttendanceDate.get("dataType")).isEqualTo("LocalDate");
    }

    @Test
    void negativeInvalidRequest() {
        final URI uri = URI.create("/api/v1/moj/reports/incomplete-service?location=sfd&cut-off-date=21-12-12");
        ResponseEntity<Object> response = template.exchange(
            new RequestEntity<>(httpHeaders, HttpMethod.GET, uri), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void negativeUnauthorised() {

        final String bureauJwt = createBureauJwt(BUREAU_USER, "400");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/moj/reports/incomplete-service?location=415&cut-off-date="
            + LocalDate.now());

        ResponseEntity<Object> response = template.exchange(
            new RequestEntity<>(httpHeaders, HttpMethod.GET, uri), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}
