package uk.gov.hmcts.juror.api.juror.domain;


import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;


/**
 * QueryDSL queries for {@link PoolCourt}.
 *
 */
@Slf4j
public class PoolCourtQueries {

    private static final String deceased = "D";
    private static final String yes = "Y";
    private static final String no = "N";


    private PoolCourtQueries() {

    }

    private static final QPoolCourt poolDetail = QPoolCourt.poolCourt;

    /**
     * Query to match where SERVICE_COMP_COMMS_STATUS EQUALS NULL.
     *
     */
    public static BooleanExpression serviceCompCommsStatus() {
        return poolDetail.serviceCompCommsStatus.isNull();
    }

    /**
     * Query to match Responded PoolCourt instances.
     */
    public static BooleanExpression repondedStatus() {
        return poolDetail.status.eq(IPoolStatus.RESPONDED);
    }

    /**
     * Query to match Excused PoolCourt instances.
     */
    public static BooleanExpression excusedStatus() {
        return poolDetail.status.eq(IPoolStatus.EXCUSED);
    }

    /**
     * Query to match Excused PoolCourt instances.
     */
    public static BooleanExpression excusedCode() {
        return poolDetail.excusalCode.ne(deceased).or(poolDetail.excusalCode.isNull());
    }

    /**
     * Query to match Responded Welsh PoolCourt instances.
     */
    public static BooleanExpression respondedWelsh() {
        return poolDetail.welsh.eq(yes);
    }

    /**
     * Query to match not Responded Welsh PoolCourt instances.
     */
    public static BooleanExpression notRespondedWelsh() {
        return poolDetail.welsh.eq(no).or(poolDetail.welsh.isNull());
    }


    /**
     * Query to match  Date_EXCUS between now and now minus excusal date parameter.
     */

    public static BooleanExpression excusalDateBetweenSysdateExcusalParameter() {

        return poolDetail.excusalDate.between(
            Date.from(Instant.now().minus(3, ChronoUnit.DAYS)),
            Date.from(Instant.now())
        );
    }

    /**
     * Query COMPLETION_DATE between now and now minus SERVICE COMPLETION PARAMETER.
     */
    public static BooleanExpression completionDateBetweenSysdateCompletionParameter() {


        return poolDetail.completionDate.between(Date.from(Instant.now().minus(2, ChronoUnit.DAYS)
                .atZone(ZoneId.systemDefault()).toInstant().truncatedTo(
                    ChronoUnit.DAYS)),
            Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.DAYS)));
    }

    /**
     * Query on COMPLETION_DATE is not null.
     */
    public static BooleanExpression completionDateNotNull() {
        return poolDetail.completionDate.isNotNull();
    }

    /**
     * Identify all Pool Records for Excusal Comms.
     */

    public static BooleanExpression recordsForExcusalComms() {
        return excusalDateBetweenSysdateExcusalParameter().and(excusedStatus().and(serviceCompCommsStatus().and(
            excusedCode()
                .and(notRespondedWelsh()))));
    }

    /**
     * Identify all Pool Records for Service Completed  Comms.
     */

    public static BooleanExpression recordsForServiceCompletedComms() {
        return completionDateBetweenSysdateCompletionParameter().and(repondedStatus().and(serviceCompCommsStatus().and(
                completionDateNotNull())
            .and(notRespondedWelsh())));


    }

    /**
     * Identify all Pool Records for Excusal Comms in Welsh.
     */
    public static BooleanExpression welshRecordsForExcusalComms() {
        return excusalDateBetweenSysdateExcusalParameter().and(excusedStatus().and(serviceCompCommsStatus()
            .and(excusedCode().and(
                respondedWelsh()))));
    }

    /**
     * Identify all Pool Records for Service Completed Comms in Welsh.
     */
    public static BooleanExpression welshRecordsForServiceCompletedComms() {
        return completionDateBetweenSysdateCompletionParameter().and(repondedStatus().and(serviceCompCommsStatus().and(
                completionDateNotNull())
            .and(respondedWelsh())));
    }


}





