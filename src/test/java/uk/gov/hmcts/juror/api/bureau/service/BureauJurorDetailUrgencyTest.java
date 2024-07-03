package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.AppSettingRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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

    private ModJurorDetail testDetail;
    private DigitalResponse jurorResponse;
    private JurorPool poolDetails;

    private static final String URGENT_RESPONSE_DESCRIPTION = "Urgent responses are those received after " + URGENT_DAYS
        + " working days before the Friday before the Friday before the court date and are not closed.";
    private static final String SUPER_URGENT_RESPONSE_DESCRIPTION = "Super urgent responses are not closed and are "
        + "received later than the Friday before the Friday before the court date.";
    private static final String SLA_OVERDUE_DESCRIPTION = "SLA overdue responses are not closed and in the last 14 "
        + "hours of a period of " + SLA_DAYS + " working days of the response received date";

    private LocalDateTime responseReceived;
    private LocalDateTime hearingDateUrgent;
    private LocalDateTime hearingDateSuperUrgent;

    private static final String OWNER_IS_BUREAU = "400";

    private static final String OWNER_IS_NOT_BUREAU = "415";

    @Mock
    private AppSettingRepository mockAppSettingRepository;
    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    @InjectMocks
    private UrgencyServiceImpl urgency;

    @Before
    public void setUp() throws Exception {
        responseReceived = LocalDateTime.now();
        //set up some known static dates relative to a start point
        LocalDateTime hearingDateValid = LocalDateTime.now().plusDays(35L);

        AppSetting urgencyDays = new AppSetting("URGENCY_DAYS", URGENT_DAYS.toString());
        AppSetting slaOverdueDays = new AppSetting("SLA_OVERDUE_DAYS", SLA_DAYS.toString());
        given(mockAppSettingRepository.findById("URGENCY_DAYS")).willReturn(Optional.of(urgencyDays));
        given(mockAppSettingRepository.findById("SLA_OVERDUE_DAYS")).willReturn(Optional.of(slaOverdueDays));

        hearingDateUrgent = urgency.fridayCutOff(hearingDateValid).minusDays(10L);
        hearingDateSuperUrgent = urgency.fridayCutOff(hearingDateValid);

        testDetail = new ModJurorDetail();
        testDetail.setProcessingStatus(NON_CLOSED_STATUS);
        testDetail.setDateReceived(responseReceived.toLocalDate());
        testDetail.setHearingDate(hearingDateValid.toLocalDate());

        jurorResponse = new DigitalResponse();
        jurorResponse.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);
        jurorResponse.setDateReceived(responseReceived);

        poolDetails = new JurorPool();
        poolDetails.setNextDate(hearingDateValid.toLocalDate());
        poolDetails.setOwner(OWNER_IS_BUREAU);
    }

    @Test
    public void testUrgentFlagWithinThresholdJdb810() throws Exception {
        //given (default)

        //when
        final ModJurorDetail flaggedResponse = urgency.flagSlaOverdueForResponse(testDetail);

        //then
        assertThat(flaggedResponse)
            .isNotNull()
            .isEqualToIgnoringGivenFields(testDetail, "urgent", "superUrgent", "slaOverdue")
        ;
        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).describedAs(URGENT_RESPONSE_DESCRIPTION).isFalse();
        assertThat(flaggedResponse.getSlaOverdue()).describedAs(SLA_OVERDUE_DESCRIPTION).isFalse();
    }

    @Test
    public void testUrgentFlagUrgentThresholdJdb810() throws Exception {
        //given
        final LocalDate urgentHearingDate = (hearingDateUrgent.plusDays(1L).plusHours(1L).toLocalDate());
        testDetail.setHearingDate(urgentHearingDate);
        poolDetails.setNextDate(urgentHearingDate);
        poolDetails.setOwner(OWNER_IS_BUREAU);


        //when
        final ModJurorDetail flaggedResponse = urgency.flagSlaOverdueForResponse(testDetail);

        //then
        assertThat(flaggedResponse)
            .isNotNull()
            .isEqualToIgnoringGivenFields(testDetail, "urgent", "superUrgent", "slaOverdue")
        ;
        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).describedAs(URGENT_RESPONSE_DESCRIPTION).isTrue();
        assertThat(flaggedResponse.getSlaOverdue()).describedAs(SLA_OVERDUE_DESCRIPTION).isFalse();
    }

    @Test
    public void testUrgentFlagSlaBreachThresholdJdb810() throws Exception {
        //given
        final LocalDate slaBreachTime = LocalDate.from(
            urgency.subtractWorkingDays(responseReceived, 5)
                .plusSeconds(1L));// 4 working days after the response received
        testDetail.setDateReceived(slaBreachTime);
        poolDetails.setOwner(OWNER_IS_NOT_BUREAU);
        //when
        ModJurorDetail flaggedResponse = urgency.flagSlaOverdueForResponse(testDetail);

        //then
        assertThat(flaggedResponse)
            .isNotNull()
            .isEqualToIgnoringGivenFields(testDetail, "urgent", "superUrgent", "slaOverdue")
        ;
        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).describedAs(URGENT_RESPONSE_DESCRIPTION).isFalse();
        assertThat(flaggedResponse.getSlaOverdue()).describedAs(SLA_OVERDUE_DESCRIPTION).isTrue();
    }


    @Test
    public void testAddWorkingDaysIgnoresWeekendsJdb810() throws Exception {
        final int testDays = 9;
        final LocalDate fixedDateTime = LocalDate.of(2016, Month.DECEMBER, 31);
        LocalDate modifiedDateTime = urgency.addWorkingDays(fixedDateTime, testDays);

        assertThat(fixedDateTime.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(modifiedDateTime).isAfter(fixedDateTime.toString());
        assertThat(modifiedDateTime).isAfter(fixedDateTime.plusDays(testDays));
        assertThat(modifiedDateTime).isEqualTo(LocalDate.of(2017, Month.JANUARY, 12));
    }

    @Test
    public void testSchedulerFlag_urgent() throws Exception {

        //given
        final LocalDate hearingDateUrgent2 = (hearingDateUrgent.plusDays(1).plusHours(1L)).toLocalDate();
        poolDetails.setOwner(OWNER_IS_BUREAU);
        poolDetails.setNextDate(hearingDateUrgent2);

        assertThat(urgency.isUrgent(jurorResponse, poolDetails)).isTrue();
    }
}
