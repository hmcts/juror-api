package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
public class JurorPoolQueries {
    private static final int SENT_T_COURT_COMMS_SENT = 9;
    private static final int INFO_COMMS_NOT_SENT = 0;

    private static final String OWNER_IS_BUREAU = "400";

    private static final String deceased = "D";
    private static final Boolean Y = true;
    private static final Boolean N = false;

    private JurorPoolQueries(){

    }


    private static final QJurorPool jurorDetail = QJurorPool.jurorPool;

    /**
     *
     * Query to match Responded Juror instances
     *
     */


    public static BooleanExpression respondedStatus() {
        return jurorDetail.jurorPool.status.status.eq(IJurorStatus.RESPONDED);
    }


    /**
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
     *
     *Matches on all records owned by bureau
     */
    public static BooleanExpression jurorRecordWithBureau(){
        return jurorDetail.owner.eq(OWNER_IS_BUREAU);
    }

    /**
     * Matches on all records not with bureau.
     */
    public static BooleanExpression jurorRecordNotWithBureau(){
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
     *
     * @return
     */

    public static BooleanExpression bureauToCourtTransferDate() {

        LocalDate currentDateAtSixPm = LocalDateTime.now().plusHours(18L).toLocalDate();
        log.info("Bureau To Court Transfer Date {}", jurorDetail.juror.bureauTransferDate);
        log.info("Current Date at 6pm {}", currentDateAtSixPm);
        return jurorDetail.juror.bureauTransferDate.after(
            LocalDateTime.now().plusHours(18L).toLocalDate());
    }


    public static BooleanExpression courtDateWithin4Wks() {

        //hearingDate(next_date) between now and now+28 days.
        return jurorDetail.jurorPool.nextDate.between(
            LocalDateTime.now().toLocalDate(),
            LocalDateTime.now().plusDays(28L).toLocalDate());
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
    return jurorDetail.jurorPool.nextDate.after(LocalDateTime.now().toLocalDate())
        .and(respondedStatus())
        .and(jurorRecordWithBureau())
        .and(sentToCourtCommsNotSent())
        .and(bureauToCourtTransferDate());
    }


    /**
     * Identify all Pool Records for Informational Comms (4wks comms).
     */
    public static BooleanExpression awaitingInfoComms() {

        return courtDateWithin4Wks()
            .and(respondedStatus())
            .and(jurorRecordWithBureau())
            .and(jurorRecordNotWithBureau())
            .and(infoCommsNotSent())
            .and(emailIsPresent())
            .and(isPoliceChecked());

    }

    /**
     * Query to match where SERVICE_COMP_COMMS_STATUS EQUALS NULL.
     *
     * @return
     */
    public static BooleanExpression serviceCompCommsStatus() {
        return jurorDetail.juror.serviceCompCommsStatus.isNull();
    }


    /**
     * Query to match Excused PoolCourt instances.
     */
    public static BooleanExpression excusedStatus() {
        return jurorDetail.jurorPool.status.status.eq(IJurorStatus.EXCUSED);
    }

    /**
     * Query to match Excused Juror instances.
     */
    public static BooleanExpression excusedCode() {
        return jurorDetail.juror.excusalCode.ne(deceased).or(jurorDetail.juror.excusalCode.isNull());
    }

    /**
     * Query to match Responded Welsh Juror instances.
     */
    public static BooleanExpression respondedWelsh() {
        return jurorDetail.juror.welsh.eq(Y);
    }

    /**
     * Query to match not Responded Welsh PoolCourt instances.
     */
    public static BooleanExpression notRespondedWelsh() {
        return jurorDetail.juror.welsh.eq(N).or(jurorDetail.juror.welsh.isNull());
    }


    /**
     * Query to match  Date_EXCUS between now and now minus excusal date parameter.
     */

    public static BooleanExpression excusalDateBetweenSysdateExcusalParameter() {

        return jurorDetail.juror.excusalDate.between(
            LocalDateTime.now().minusDays(3L).toLocalDate(),
            LocalDateTime.now().toLocalDate());
    }

    /**
     * Query COMPLETION_DATE between now and now minus SERVICE COMPLETION PARAMETER.
     */
    public static BooleanExpression completionDateBetweenSysdateCompletionParameter() {
        return jurorDetail.juror.completionDate.between(LocalDateTime.now().minusDays(2L).toLocalDate(),LocalDateTime.now().toLocalDate());
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
        return excusalDateBetweenSysdateExcusalParameter().and(excusedStatus().and(serviceCompCommsStatus().and(
            excusedCode()
                .and(notRespondedWelsh()))));
    }

    /**
     * Identify all juror Records for Service Completed  Comms.
     */

    public static BooleanExpression recordsForServiceCompletedComms() {
        return completionDateBetweenSysdateCompletionParameter().and(respondedStatus().and(serviceCompCommsStatus().and(
                completionDateNotNull())
                                                                                               .and(notRespondedWelsh())));


    }

    /**
     * Identify all Juror Records for Excusal Comms in Welsh.
     */
    public static BooleanExpression welshRecordsForExcusalComms() {
        return excusalDateBetweenSysdateExcusalParameter().and(excusedStatus().and(serviceCompCommsStatus()
                                                                                       .and(excusedCode().and(
                                                                                           respondedWelsh()))));
    }

    /**
     * Identify all Juror Records for Service Completed Comms in Welsh.
     */
    public static BooleanExpression welshRecordsForServiceCompletedComms() {
        return completionDateBetweenSysdateCompletionParameter().and(respondedStatus().and(serviceCompCommsStatus().and(
            completionDateNotNull()).and(respondedWelsh())));
    }


}




