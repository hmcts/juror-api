package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JurorPoolQueries {
    private static final int SENT_T_COURT_COMMS_SENT = 9;
    private static final int INFO_COMMS_NOT_SENT = 0;

    private static final String OWNER_IS_BUREAU = "400";

    private static final String deceased = "D";

    private static final List<String> locCodes = Arrays.asList("459", "468");


    private JurorPoolQueries() {

    }

    private static final QJurorPool jurorDetail = QJurorPool.jurorPool;

    /**
     * Query to match Responded Juror instances.
    */
    public static BooleanExpression respondedStatus() {
        return jurorDetail.status.status.eq(IJurorStatus.RESPONDED);
    }

    public static BooleanExpression completedStatus() {
        return jurorDetail.status.status.eq(IJurorStatus.COMPLETED);
    }

    /**
    * ø
    * Matches Juror instances where notification flag indicates, sentToCourt comms has not been sent.
    */
    public static BooleanExpression sentToCourtCommsNotSent() {
        return jurorDetail.juror.notifications.ne(SENT_T_COURT_COMMS_SENT);
    }

    /**
    * Matches Juror instances where notification flag indicates, weekly info_comms has not been sent.
    */
    public static BooleanExpression infoCommsNotSent() {
        return jurorDetail.juror.notifications.eq(INFO_COMMS_NOT_SENT);
    }

    /**
    * Matches on all records owned by bureau.
    */
    public static BooleanExpression jurorRecordWithBureau() {
        return jurorDetail.owner.eq(OWNER_IS_BUREAU);
    }

    public static BooleanExpression jurorRecordNotWithBureau() {
        return jurorDetail.owner.ne(OWNER_IS_BUREAU);
    }

    /**
    * Query to match instance where an email exists.
    */
    public static BooleanExpression emailIsPresent() {
        return jurorDetail.juror.email.isNotNull();
    }

    /**
    * Query to match all records where bureau_to_court_transfer date is between 6pm and midnight.
    */
    public static BooleanExpression bureauToCourtTransferDate() {
        LocalDate currentDate = LocalDate.now();
        log.info("Bureau To Court Transfer Date {}", jurorDetail.juror.bureauTransferDate);
        log.info("Current Date at 6pm {}", currentDate);
        return jurorDetail
        .juror
        .bureauTransferDate
        .after(currentDate)
        .or(jurorDetail.juror.bureauTransferDate.eq(currentDate));
    }

    public static BooleanExpression courtDateWithin4Wks() {

        // hearingDate(next_date) between now and now+28 days.
        return jurorDetail.nextDate.between(LocalDate.now(), LocalDate.now().plusDays(28L));
    }

    /**
    * Query to match instance where police checked or not based on given parameter.
    */
    public static BooleanExpression isPoliceChecked() {
        return jurorDetail.juror.policeCheck.eq(PoliceCheck.ELIGIBLE);
    }

    /**
    * Identify all Pool Records for which a sent To Comms needs to be sent.
    */
    public static BooleanExpression awaitingSentToCourtComms() {
        return jurorDetail
        .nextDate
        .after(LocalDate.now())
        .and(respondedStatus())
        .and(jurorRecordNotWithBureau())
        .and(sentToCourtCommsNotSent())
        .and(bureauToCourtTransferDate())
        .and(locCodeNotIn(locCodes));

    }

    /**
    * Identify all Pool Records for which a sent To Comms needs to be sent for a certain locCode.
    */
    public static BooleanExpression awaitingSentToCourtCommsTemporaryCourt() {
        return jurorDetail
        .nextDate
        .after(LocalDate.now())
        .and(respondedStatus())
        .and(jurorRecordNotWithBureau())
        .and(sentToCourtCommsNotSent())
        .and(bureauToCourtTransferDate())
        .and(locCodeIn(locCodes));
    }

    /**
    * Identify all Pool Records for Informational Comms (4wks comms).
    */
    public static BooleanExpression awaitingInfoComms() {

        return courtDateWithin4Wks()
        .and(respondedStatus())
        .and(jurorRecordWithBureau())
        .and(infoCommsNotSent())
        .and(emailIsPresent())
        .and(isPoliceChecked())
        .and(locCodeNotIn(locCodes));
    }

    /**
     * Awaiting info comms for a specific court location.
     */
    public static BooleanExpression awaitingInfoCommsTemporaryCourt() {
        return courtDateWithin4Wks()
        .and(respondedStatus())
        .and(jurorRecordWithBureau())
        .and(infoCommsNotSent())
        .and(emailIsPresent())
        .and(isPoliceChecked())
        .and(locCodeIn(locCodes));
    }

    /**
    * Query to match where SERVICE_COMP_COMMS_STATUS EQUALS NULL.
    */
    public static BooleanExpression serviceCompCommsStatus() {
        return jurorDetail.juror.serviceCompCommsStatus.isNull();
    }

    /**
    * Query to match Excused PoolCourt instances.
    */
    public static BooleanExpression excusedStatus() {
        return jurorDetail.status.status.eq(IJurorStatus.EXCUSED);
    }

    /**
    * Query to match Excused Juror instances.
    */
    public static BooleanExpression excusedCode() {
        return jurorDetail.juror.excusalCode.ne(deceased).or(jurorDetail.juror.excusalCode.isNull());
    }

    /**
    * Query to match  Date_EXCUS between now and now minus excusal date parameter.
    */
    public static BooleanExpression excusalDateBetweenSysdateExcusalParameter() {

        return jurorDetail.juror.excusalDate.between(
           LocalDateTime.now().minusDays(3L).toLocalDate(), LocalDate.now());
    }

    /**
    * Query COMPLETION_DATE between now and now minus SERVICE COMPLETION PARAMETER.
    */
    public static BooleanExpression completionDateBetweenSysdateCompletionParameter() {
        return jurorDetail.juror.completionDate.between(LocalDate.now().minusDays(2L), LocalDate.now());
    }

    /**
    * Query on COMPLETION_DATE is not null.
    */
    public static BooleanExpression completionDateNotNull() {
        return jurorDetail.juror.completionDate.isNotNull();
    }

    /**
    * Identify all juror Records for Excusal Comms.
    */
    public static BooleanExpression recordsForExcusalComms() {
        return excusalDateBetweenSysdateExcusalParameter()
         .and(excusedStatus())
         .and(serviceCompCommsStatus())
         .and(excusedCode());
    }

    /**
    * Identify all juror Records for Service Completed  Comms.
    */
    public static BooleanExpression recordsForServiceCompletedComms() {
        return completionDateBetweenSysdateCompletionParameter()
         .and(completedStatus())
         .and(serviceCompCommsStatus())
         .and(completionDateNotNull());
    }


    /**
    * Query to match instance where juror locCode is equal to '459','468'.
    */
    public static BooleanExpression locCodeIn(List<String> locCodes) {
        return jurorDetail.pool.courtLocation.locCode.in(locCodes);
    }
    /**
     * Query to match instance where juror locCode is not equal to '459','468'.
     */
    public static BooleanExpression locCodeNotIn(List<String> locCodes) {
        return jurorDetail.pool.courtLocation.locCode.notIn(locCodes);


    }

}


