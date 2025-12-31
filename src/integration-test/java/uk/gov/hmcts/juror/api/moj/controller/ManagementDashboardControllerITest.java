package uk.gov.hmcts.juror.api.moj.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.ExpenseLimitsReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.IncompleteServiceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.SmsMessagesReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.WeekendAttendanceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.messages.Message;
import uk.gov.hmcts.juror.api.moj.repository.MessageRepository;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

/**
 * Integration tests for the Management Dashboard controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ManagementDashboardControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MessageRepository messageRepository;

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        final BureauJwtPayload bureauJwtPayload = TestUtils.getJwtPayloadSuperUser("415", "Chester");

        final String bureauJwt = mintBureauJwt(bureauJwtPayload);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/mod/ManagementDashboardOverdueUtilITest_typical.sql"})
    void overdueUtilisationReportHappy() {

        ResponseEntity<OverdueUtilisationReportResponseDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/management-dashboard/overdue-utilisation")),
                OverdueUtilisationReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        OverdueUtilisationReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull(); // no actual data to test against

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/mod/ManagementDashboardIncompleteServiceReportITest_typical.sql"})
    void incompleteServiceReportHappy() {

        ResponseEntity<IncompleteServiceReportResponseDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/management-dashboard/incomplete-service")),
            IncompleteServiceReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        IncompleteServiceReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getRecords()).isNotNull();
        assertThat(responseBody.getRecords().size()).isEqualTo(2);

        IncompleteServiceReportResponseDto.IncompleteServiceRecord incompleteServiceRecord =
                                                                                responseBody.getRecords().get(0);
        assertThat(incompleteServiceRecord.getCourt()).isEqualTo("CHESTER (415)");
        assertThat(incompleteServiceRecord.getNumberOfIncompleteServices()).isEqualTo(11);

        incompleteServiceRecord =
                responseBody.getRecords().get(1);
        assertThat(incompleteServiceRecord.getCourt()).isEqualTo("IPSWICH (426)");
        assertThat(incompleteServiceRecord.getNumberOfIncompleteServices()).isEqualTo(10);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/mod/reports/WeekendAttendanceReportITest_typical.sql"})
    void weekendAttendanceReportHappy() {

        ResponseEntity<WeekendAttendanceReportResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET,
                        URI.create("/api/v1/moj/management-dashboard/weekend-attendance")),
                WeekendAttendanceReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        WeekendAttendanceReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/mod/ExpenseLimitsReportITest_typical.sql"})
    void expenseLimitsReportHappy() {

        ResponseEntity<ExpenseLimitsReportResponseDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/management-dashboard/expense-limits")),
            ExpenseLimitsReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        ExpenseLimitsReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<ExpenseLimitsReportResponseDto.ExpenseLimitsRecord> records = responseBody.getRecords();
        assertThat(records).isNotNull();
        assertThat(records.size()).isEqualTo(10);

        ExpenseLimitsReportResponseDto.ExpenseLimitsRecord expenseLimitsRecord = records.get(0);
        assertThat(expenseLimitsRecord.getCourtLocationNameAndCode()).isEqualTo("BOURNEMOUTH (406)");
        assertThat(expenseLimitsRecord.getType()).isEqualTo("Public Transport");
        assertThat(expenseLimitsRecord.getOldLimit()).isEqualTo(10.0);
        assertThat(expenseLimitsRecord.getNewLimit()).isEqualTo(5.0);
        assertThat(expenseLimitsRecord.getChangedBy()).isEqualTo("kggf.kggfww");

        expenseLimitsRecord = records.get(1);
        assertThat(expenseLimitsRecord.getCourtLocationNameAndCode()).isEqualTo("Chelmsford  (414)");
        assertThat(expenseLimitsRecord.getType()).isEqualTo("Taxi");
        assertThat(expenseLimitsRecord.getOldLimit()).isEqualTo(40.0);
        assertThat(expenseLimitsRecord.getNewLimit()).isEqualTo(75.0);
        assertThat(expenseLimitsRecord.getChangedBy()).isEqualTo("test.sur");

        expenseLimitsRecord = records.get(9);
        assertThat(expenseLimitsRecord.getCourtLocationNameAndCode()).isEqualTo("SALISBURY (480)");
        assertThat(expenseLimitsRecord.getType()).isEqualTo("Taxi");
        assertThat(expenseLimitsRecord.getOldLimit()).isEqualTo(0.0);
        assertThat(expenseLimitsRecord.getNewLimit()).isEqualTo(10.0);
        assertThat(expenseLimitsRecord.getChangedBy()).isEqualTo("dsfedf.test");
    }

    @Test
    @Sql({"/db/mod/truncate.sql",  "/db/mod/ManagementDashboardSmsMessages_typical.sql"})
    void smsMessagesReportHappy() {

        setupMessageData();

        ResponseEntity<SmsMessagesReportResponseDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/management-dashboard/sms-messages")),
            SmsMessagesReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        SmsMessagesReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<SmsMessagesReportResponseDto.SmsMessagesRecord> records = responseBody.getRecords();
        assertThat(records).isNotNull();
        assertThat(records.size()).isEqualTo(3);

        assertThat(responseBody.getTotalMessagesSent()).isEqualTo(71);

        SmsMessagesReportResponseDto.SmsMessagesRecord smsMessagesRecord = records.get(0);
        assertThat(smsMessagesRecord.getCourtLocationNameAndCode()).isEqualTo("CHESTER (415)");
        assertThat(smsMessagesRecord.getMessagesSent()).isEqualTo(55);
        smsMessagesRecord = records.get(1);
        assertThat(smsMessagesRecord.getCourtLocationNameAndCode()).isEqualTo("COVENTRY (417)");
        assertThat(smsMessagesRecord.getMessagesSent()).isEqualTo(9);
        smsMessagesRecord = records.get(2);
        assertThat(smsMessagesRecord.getCourtLocationNameAndCode()).isEqualTo("AYLESBURY (401)");
        assertThat(smsMessagesRecord.getMessagesSent()).isEqualTo(7);

    }

    private void setupMessageData() {
        Iterable<Message> messages = messageRepository.findAll();
        messageRepository.deleteAll();

        // determine the date to set the message file datetime to be within the financial year
        if (LocalDate.now().getMonth().getValue() >= 4) {
            // current date is after 1st April, so set to 1st April this year
            for (Message message : messages) {
                message.setFileDatetime(LocalDate.now().withMonth(4).withDayOfMonth(1)
                                            .atTime(LocalTime.of(9,0)));
            }
        } else {
            // current date is before 1st April, so set to 1st April last year
            for (Message message : messages) {
                message.setFileDatetime(LocalDate.now().minusYears(1).withMonth(4).withDayOfMonth(1)
                                            .atTime(LocalTime.of(9,0)));
            }
        }

        messageRepository.saveAll(messages);

    }

}
