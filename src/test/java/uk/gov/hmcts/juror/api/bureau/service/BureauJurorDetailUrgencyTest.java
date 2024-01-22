package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;
import uk.gov.hmcts.juror.api.bureau.domain.AppSettingRepository;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Test the processing logic of the urgency bean.
 */
@RunWith(MockitoJUnitRunner.class)
public class BureauJurorDetailUrgencyTest {

    private static final String NON_CLOSED_STATUS = ProcessingStatus.TODO.name();
    private static final Integer URGENT_DAYS = 10;
    private static final Integer SLA_DAYS = 5;

    private BureauJurorDetail testDetail;
    private JurorResponse jurorResponse;
    private Pool poolDetails;

    private static final String URGENT_RESPONSE_DESCRIPTION = "Urgent responses are those received after " + URGENT_DAYS
        + " working days before the Friday before the Friday before the court date and are not closed.";
    private static final String SUPER_URGENT_RESPONSE_DESCRIPTION = "Super urgent responses are not closed and are " +
        "received later than the Friday before the Friday before the court date.";
    private static final String SLA_OVERDUE_DESCRIPTION = "SLA overdue responses are not closed and in the last 14 " +
        "hours of a period of " + SLA_DAYS + " working days of the response received date";

    private LocalDateTime RESPONSE_RECEIVED;
    private LocalDateTime HEARING_DATE_URGENT;
    private LocalDateTime HEARING_DATE_SUPER_URGENT;

    @Mock
    private AppSettingRepository mockAppSettingRepository;

    @InjectMocks
    private UrgencyServiceImpl urgency;

    @Before
    public void setUp() throws Exception {
        RESPONSE_RECEIVED = LocalDateTime.now();
        //set up some known static dates relative to a start point
        LocalDateTime HEARING_DATE_VALID = LocalDateTime.now().plus(35, ChronoUnit.DAYS);

        AppSetting urgency_days = new AppSetting("URGENCY_DAYS", URGENT_DAYS.toString());
        AppSetting sla_overdue_days = new AppSetting("SLA_OVERDUE_DAYS", SLA_DAYS.toString());
        given(mockAppSettingRepository.findById("URGENCY_DAYS")).willReturn(Optional.of(urgency_days));
        given(mockAppSettingRepository.findById("SLA_OVERDUE_DAYS")).willReturn(Optional.of(sla_overdue_days));

        HEARING_DATE_URGENT = urgency.fridayCutOff(HEARING_DATE_VALID).minus(10, ChronoUnit.DAYS);
        HEARING_DATE_SUPER_URGENT = urgency.fridayCutOff(HEARING_DATE_VALID);

        testDetail = new BureauJurorDetail();
        testDetail.setProcessingStatus(NON_CLOSED_STATUS);
        testDetail.setDateReceived(Date.from(RESPONSE_RECEIVED.toInstant(ZoneOffset.UTC)));
        testDetail.setHearingDate(Date.from(HEARING_DATE_VALID.toInstant(ZoneOffset.UTC)));

        jurorResponse = new JurorResponse();
        jurorResponse.setProcessingStatus(uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus.TODO);
        jurorResponse.setDateReceived(Date.from(RESPONSE_RECEIVED.toInstant(ZoneOffset.UTC)));

        poolDetails = new Pool();
        poolDetails.setHearingDate(Date.from(HEARING_DATE_VALID.toInstant(ZoneOffset.UTC)));
    }

    @Test
    public void testUrgentFlag_withinThreshold_JDB_810() throws Exception {
        //given (default)

        //when
        final BureauJurorDetail flaggedResponse = urgency.flagSlaOverdueForResponse(testDetail);

        //then
        assertThat(flaggedResponse)
            .isNotNull()
            .isEqualToIgnoringGivenFields(testDetail, "urgent", "superUrgent", "slaOverdue")
        ;
        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).describedAs(URGENT_RESPONSE_DESCRIPTION).isFalse();
        assertThat(urgency.isSuperUrgent(jurorResponse, poolDetails)).describedAs(SUPER_URGENT_RESPONSE_DESCRIPTION)
            .isFalse();
        assertThat(flaggedResponse.getSlaOverdue()).describedAs(SLA_OVERDUE_DESCRIPTION).isFalse();
    }

    @Test
    public void testUrgentFlag_urgentThreshold_JDB_810() throws Exception {
        //given
        final Date urgentHearingDate = Date.from(HEARING_DATE_URGENT.toInstant(ZoneOffset.UTC)
            .plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
        testDetail.setHearingDate(urgentHearingDate);
        poolDetails.setHearingDate(urgentHearingDate);
        poolDetails.setReadOnly(Boolean.FALSE);

        //when
        final BureauJurorDetail flaggedResponse = urgency.flagSlaOverdueForResponse(testDetail);

        //then
        assertThat(flaggedResponse)
            .isNotNull()
            .isEqualToIgnoringGivenFields(testDetail, "urgent", "superUrgent", "slaOverdue")
        ;
        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).describedAs(URGENT_RESPONSE_DESCRIPTION).isTrue();
        assertThat(urgency.isSuperUrgent(jurorResponse, poolDetails)).describedAs(SUPER_URGENT_RESPONSE_DESCRIPTION)
            .isFalse();
        assertThat(flaggedResponse.getSlaOverdue()).describedAs(SLA_OVERDUE_DESCRIPTION).isFalse();
    }

    @Test
    public void testUrgentFlag_superUrgentThreshold_JDB_810() throws Exception {
        //given
        final Date dateReceivedSuperUrgent = Date.from(HEARING_DATE_SUPER_URGENT.toInstant(ZoneOffset.UTC).plus(1,
            ChronoUnit.DAYS));
        testDetail.setDateReceived(
            dateReceivedSuperUrgent
        );
        jurorResponse.setDateReceived(dateReceivedSuperUrgent);
        poolDetails.setReadOnly(Boolean.TRUE);

        //when
        BureauJurorDetail flaggedResponse = urgency.flagSlaOverdueForResponse(testDetail);

        //then
        assertThat(flaggedResponse)
            .isNotNull()
            .isEqualToIgnoringGivenFields(testDetail, "urgent", "superUrgent", "slaOverdue")
        ;
        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).describedAs(URGENT_RESPONSE_DESCRIPTION).isFalse();
        assertThat(urgency.isSuperUrgent(jurorResponse, poolDetails)).describedAs(SUPER_URGENT_RESPONSE_DESCRIPTION)
            .isTrue();
        assertThat(flaggedResponse.getSlaOverdue()).describedAs(SLA_OVERDUE_DESCRIPTION).isFalse();
    }

    @Test
    public void testUrgentFlag_slaBreachThreshold_JDB_810() throws Exception {
        //given
        final Date slaBreachTime = Date.from(
            urgency.subtractWorkingDays(RESPONSE_RECEIVED, 5)
                .plus(1, ChronoUnit.SECONDS).toInstant(ZoneOffset.UTC)
        );// 4 working days after the response received
        testDetail.setDateReceived(slaBreachTime);
        poolDetails.setReadOnly(Boolean.TRUE);
        //when
        BureauJurorDetail flaggedResponse = urgency.flagSlaOverdueForResponse(testDetail);

        //then
        assertThat(flaggedResponse)
            .isNotNull()
            .isEqualToIgnoringGivenFields(testDetail, "urgent", "superUrgent", "slaOverdue")
        ;
        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).describedAs(URGENT_RESPONSE_DESCRIPTION).isFalse();
        assertThat(urgency.isSuperUrgent(jurorResponse, poolDetails)).describedAs(SUPER_URGENT_RESPONSE_DESCRIPTION)
            .isTrue();
        assertThat(flaggedResponse.getSlaOverdue()).describedAs(SLA_OVERDUE_DESCRIPTION).isTrue();
    }


    @Test
    public void testAddWorkingDaysIgnoresWeekends_JDB_810() throws Exception {
        final Integer testDays = 9;
        final LocalDateTime fixedDateTime = LocalDateTime.of(2016, Month.DECEMBER, 31, 12, 0, 0);
        LocalDateTime modifiedDateTime = urgency.addWorkingDays(fixedDateTime, testDays);

        assertThat(fixedDateTime.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(modifiedDateTime).isAfter(fixedDateTime.toString());
        assertThat(modifiedDateTime).isAfter(fixedDateTime.plus(testDays, ChronoUnit.DAYS).toString());
        assertThat(modifiedDateTime).isEqualToIgnoringHours(LocalDateTime.of(2017, Month.JANUARY, 12, 12, 0, 0));
    }

    @Test
    public void testSchedulerFlag_urgent() throws Exception {

        //given
        final Date hearingDateUrgent = Date.from(HEARING_DATE_URGENT.toInstant(ZoneOffset.UTC)
            .plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
        poolDetails.setReadOnly(Boolean.FALSE);

        poolDetails.setHearingDate(hearingDateUrgent);

        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).isTrue();
        assertThat(urgency.isSuperUrgent(jurorResponse, poolDetails)).isFalse();

    }

    @Test
    public void testSchedulerFlag_super_urgent() throws Exception {
        //given
        LocalDateTime HEARING_DATE_VALID = LocalDateTime.now().plus(10, ChronoUnit.DAYS);
        HEARING_DATE_SUPER_URGENT = urgency.fridayCutOff(HEARING_DATE_VALID);
        poolDetails.setReadOnly(Boolean.TRUE);

        final Date dateReceivedSuperUrgent = Date.from(HEARING_DATE_SUPER_URGENT.toInstant(ZoneOffset.UTC).plus(1,
            ChronoUnit.DAYS));

        poolDetails.setHearingDate(dateReceivedSuperUrgent);

        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).isFalse();
        assertThat(urgency.isSuperUrgent(jurorResponse, poolDetails)).isTrue();

    }


}