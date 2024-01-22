package uk.gov.hmcts.juror.api.juror.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * QueryDSL queries for {@link Pool}.
 *
 * @since JDB-283
 */

@Slf4j
public class PoolQueries {
    private static final int SENT_T_COURT_COMMS_SENT = 9;
    private static final int INFO_COMMS_NOT_SENT = 0;
    private static final boolean READ_ONLY = true;
    private static final String POLICE_CHECK_PASS = String.valueOf('P');

    private PoolQueries() {

    }

    private static final QPool poolDetail = QPool.pool;


    /**
     * Query to match Responded Pool instances.
     */
    public static BooleanExpression repondedStatus() {
        return poolDetail.status.eq(IPoolStatus.RESPONDED);
    }

    /**
     * Matches Pool instances where notification flag indicates, sentToCourt comms has not been sent.
     */
    public static BooleanExpression sentToCourtCommsNotSent() {
        return poolDetail.notifications.ne(SENT_T_COURT_COMMS_SENT);
    }

    /**
     * Matches Pool instances where notification flag indicates, weekly info_comms has not been sent.
     */
    public static BooleanExpression infoCommsNotSent() {
        return poolDetail.notifications.eq(INFO_COMMS_NOT_SENT);
    }

    /**
     * Matches on all Read Only.
     */
    public static BooleanExpression readOnly() {
        return poolDetail.readOnly.eq(READ_ONLY);
    }

    /**
     * Matches on all Not Read Only.
     */
    public static BooleanExpression notReadOnly() {
        return poolDetail.readOnly.ne(READ_ONLY);
    }

    /**
     * Query to match instance where an email exists.
     */
    public static BooleanExpression emailIsPresent() {
        return poolDetail.email.isNotNull();
    }

    /**
     * Query to match all records where bureau_to_court_transfer date is between 6pm and midnight.
     *
     * @return
     */

    public static BooleanExpression bureauToCourtTransferDate() {

        Date currentDateAtSixPm = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(
            ChronoUnit.DAYS).plus(18, ChronoUnit.HOURS));
        log.info("Bureau To Court Transfer Date {}", poolDetail.transferDate);
        log.info("Current Date at 6pm {}", currentDateAtSixPm);
        return poolDetail.transferDate.after(
            Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(
                ChronoUnit.DAYS).plus(18, ChronoUnit.HOURS)));
    }


    public static BooleanExpression courtDateWithin4Wks() {

        //hearingDate(next_date) between now and now+28 days.
        return poolDetail.hearingDate.between(
            Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.DAYS)),
            Date.from(Instant.now()
                .plus(28, ChronoUnit.DAYS)
                .atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.DAYS))
        );
    }

    /**
     * Query to match instance where police checked or not based on given parameter.
     */
    public static BooleanExpression isPoliceChecked() {
        return poolDetail.policeCheck.eq(POLICE_CHECK_PASS);
    }

    /**
     * Identify all Pool Records for which a sent To Comms needs to be sent.
     */
    public static BooleanExpression awaitingSentToCourtComms() {
        return poolDetail.hearingDate.after(
                Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(
                    ChronoUnit.DAYS)))
            .and(repondedStatus())
            .and(readOnly())
            .and(sentToCourtCommsNotSent())
            .and(bureauToCourtTransferDate());
    }


    /**
     * Identify all Pool Records for Informational Comms (4wks comms).
     */
    public static BooleanExpression awaitingInfoComms() {

        //pool view holds owner '400' only so not required as a condition.
        return courtDateWithin4Wks()
            .and(repondedStatus())
            .and(notReadOnly())
            .and(infoCommsNotSent())
            .and(emailIsPresent())
            .and(isPoliceChecked());

    }


}
