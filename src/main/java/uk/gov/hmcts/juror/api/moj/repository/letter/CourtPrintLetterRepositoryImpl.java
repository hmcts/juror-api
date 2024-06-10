package uk.gov.hmcts.juror.api.moj.repository.letter;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QWelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.FAILED_TO_ATTEND;
import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.POSTPONED;
import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.SHOW_CAUSE;

@Component
public class CourtPrintLetterRepositoryImpl implements CourtPrintLetterRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QWelshCourtLocation WELSH_COURT_LOCATION = QWelshCourtLocation.welshCourtLocation;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPanel PANEL = QPanel.panel;
    private static final QTrial TRIAL = QTrial.trial;

    private static final QAppearance APPEARANCE = QAppearance.appearance;

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Tuple retrievePrintInformation(String jurorNumber, CourtLetterType courtLetterType, boolean welsh,
                                          String owner) {
        return retrievePrintInformation(jurorNumber, courtLetterType, welsh, owner, null);
    }

    @Override
    public Tuple retrievePrintInformation(String jurorNumber, CourtLetterType courtLetterType, boolean welsh,
                                          String owner, String trialNumber) {
        List<Expression<?>> expressions = fetchCommonPrintData(welsh);

        buildExpressionsBasedOnLetterType(courtLetterType, expressions);

        JPAQuery<Tuple> query = selectData(expressions, welsh);

        filterDataBasedOnLetterType(courtLetterType, query, trialNumber);

        filterDataForAllLetterTypes(query, jurorNumber, owner);

        orderResultsBasedOnLetterType(query, courtLetterType);


        // multiple records can be expected if there are multiple history events for a juror - the data is sorted by
        // history date descending so fetching first will retrieve the latest record.
        return query.fetchFirst();
    }

    @Override
    // Note: letterDate is specific to a letterType e.g. attendanceDate/absentDate, excusalDate, deferralDate, etc
    public Tuple retrievePrintInformationBasedOnLetterSpecificDate(String jurorNumber, CourtLetterType letterType,
                                                                   boolean welsh,
                                                                   String owner, LocalDate letterDate) {
        List<Expression<?>> expressions = fetchCommonPrintData(welsh);

        // start building the sql expressions based on letter type
        buildExpressionsBasedOnLetterType(letterType, expressions);

        // select data from relevant tables
        JPAQuery<Tuple> query = selectData(expressions, welsh);

        // filter data based on letter type
        filterDataBasedOnLetterType(letterType, query, null);

        // filter based on date
        if (SHOW_CAUSE.equals(letterType)
            || FAILED_TO_ATTEND.equals(letterType)) {
            query.where(APPEARANCE.attendanceDate.eq(letterDate));
        }

        // filter data for all letter types
        filterDataForAllLetterTypes(query, jurorNumber, owner);

        // order results based on letter type
        orderResultsBasedOnLetterType(query, letterType);

        return query.fetchOne();
    }

    private void orderResultsBasedOnLetterType(JPAQuery<Tuple> query, CourtLetterType courtLetterType) {
        switch (courtLetterType) {
            case DEFERRAL_GRANTED, POSTPONED -> query.orderBy(JUROR_POOL.deferralDate.desc());
            case EXCUSAL_GRANTED -> query.orderBy(JUROR_POOL.juror.excusalDate.desc());
            case WITHDRAWAL -> query.orderBy(JUROR_POOL.juror.disqualifyDate.desc());
            case SHOW_CAUSE, FAILED_TO_ATTEND -> query.orderBy(APPEARANCE.attendanceDate.desc());
            case CERTIFICATE_OF_EXEMPTION -> query.orderBy(PANEL.trial.trialNumber.desc());
            case DEFERRAL_REFUSED, EXCUSAL_REFUSED, CERTIFICATE_OF_ATTENDANCE -> {
                // currently not ordered;
            }
            default -> throw new MojException.NotImplemented("letter type not implemented", null);
        }
    }

    private void filterDataForAllLetterTypes(JPAQuery<Tuple> query, String jurorNumber, String owner) {
        query.where(JUROR_POOL.juror.jurorNumber.eq(jurorNumber));
        query.where(JUROR_POOL.owner.eq(owner));
    }

    private void filterDataBasedOnLetterType(CourtLetterType courtLetterType,
                                             JPAQuery<Tuple> query, String trialNumber) {
        switch (courtLetterType) {
            case DEFERRAL_GRANTED, POSTPONED -> {
                if (POSTPONED.equals(courtLetterType)) {
                    query.join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
                        .where(JUROR_POOL.deferralCode.eq(ExcusalCodeEnum.P.getCode()));
                }
                query.where(JUROR_POOL.status.status.eq(IJurorStatus.DEFERRED))
                    .orderBy(JUROR_POOL.deferralDate.desc());
            }
            case DEFERRAL_REFUSED, EXCUSAL_REFUSED -> query.where(JUROR_POOL.isActive.eq(true));
            case EXCUSAL_GRANTED -> query
                .where(JUROR_POOL.status.status.eq(IJurorStatus.EXCUSED))
                .orderBy(JUROR_POOL.juror.excusalDate.desc());

            case WITHDRAWAL -> query
                .where(JUROR_POOL.status.status.eq(IJurorStatus.DISQUALIFIED))
                .orderBy(JUROR_POOL.juror.disqualifyDate.desc());
            case SHOW_CAUSE, FAILED_TO_ATTEND ->
                query.leftJoin(APPEARANCE).on(JUROR_POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber)
                        .and(JUROR_POOL.pool.poolNumber.eq(APPEARANCE.poolNumber)))
                    .where(APPEARANCE.noShow.isTrue())
                    .where(APPEARANCE.attendanceType.eq(AttendanceType.ABSENT))
                    .orderBy(APPEARANCE.attendanceDate.desc());
            case CERTIFICATE_OF_EXEMPTION -> query
                .join(PANEL).on(PANEL.juror.eq(JUROR)
                    .and(PANEL.trial.courtLocation.eq(POOL_REQUEST.courtLocation)))
                .where(PANEL.trial.trialNumber.eq(trialNumber))
                .orderBy(PANEL.trial.trialNumber.desc());
            case CERTIFICATE_OF_ATTENDANCE ->
                query.join(APPEARANCE).on(JUROR.jurorNumber.eq(APPEARANCE.jurorNumber)
                        .and(COURT_LOCATION.eq(APPEARANCE.courtLocation)))
                    .where(APPEARANCE.noShow.isFalse().or(APPEARANCE.noShow.isNull()))
                    .where(APPEARANCE.attendanceType.ne(AttendanceType.ABSENT))
                    .orderBy(APPEARANCE.attendanceDate.desc());

            default -> throw new MojException.NotImplemented("letter type not implemented", null);
        }
    }

    private JPAQuery<Tuple> selectData(List<Expression<?>> expressions, boolean welsh) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<Tuple> query =
            queryFactory
                .select(expressions.toArray(new Expression<?>[0]))
                .from(JUROR_POOL)
                .join(JUROR).on(JUROR.eq(JUROR_POOL.juror))
                .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
                .join(COURT_LOCATION).on(COURT_LOCATION.eq(POOL_REQUEST.courtLocation));

        if (welsh) {
            query.join(WELSH_COURT_LOCATION).on(WELSH_COURT_LOCATION.locCode.eq(POOL_REQUEST.courtLocation.locCode));
        }

        return query;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static void buildExpressionsBasedOnLetterType(CourtLetterType courtLetterType,
                                                          List<Expression<?>> expressions) {
        switch (courtLetterType) {
            case DEFERRAL_GRANTED, POSTPONED -> {
                expressions.add(JUROR_POOL.deferralDate);
                expressions.add(JUROR_POOL.deferralCode);
            }
            case EXCUSAL_GRANTED -> {
                expressions.add(JUROR.excusalCode);
                expressions.add(JUROR.excusalDate);
            }
            case WITHDRAWAL -> {
                expressions.add(JUROR.disqualifyDate);
                expressions.add(JUROR.disqualifyCode);
            }
            case DEFERRAL_REFUSED -> {
                // no additional database properties to add to the to query expressions
            }
            case EXCUSAL_REFUSED -> {
                expressions.add(JUROR_POOL.juror.excusalCode);
                expressions.add(JUROR_POOL.juror.excusalDate);
            }
            case SHOW_CAUSE -> {
                expressions.add(APPEARANCE.attendanceDate);
                expressions.add(APPEARANCE.noShow);
            }
            case FAILED_TO_ATTEND -> expressions.add(APPEARANCE.attendanceDate);
            case CERTIFICATE_OF_EXEMPTION -> {
                expressions.add(TRIAL.judge.name);
                expressions.add(TRIAL.description);
            }
            case CERTIFICATE_OF_ATTENDANCE -> {
            } // does nothing here
            default -> throw new MojException.NotImplemented("letter type not implemented", null);
        }
    }

    private List<Expression<?>> fetchCommonPrintData(boolean welsh) {
        List<Expression<?>> expressions = new ArrayList<>();

        // Court information
        if (welsh) {
            expressions.add(WELSH_COURT_LOCATION.locCourtName);
            expressions.add(WELSH_COURT_LOCATION.address1);
            expressions.add(WELSH_COURT_LOCATION.address2);
            expressions.add(WELSH_COURT_LOCATION.address3);
            expressions.add(WELSH_COURT_LOCATION.address4);
            expressions.add(WELSH_COURT_LOCATION.address5);
            expressions.add(WELSH_COURT_LOCATION.address6);

        } else {
            expressions.add(COURT_LOCATION.locCode);
            expressions.add(COURT_LOCATION.name);
            expressions.add(COURT_LOCATION.address1);
            expressions.add(COURT_LOCATION.address2);
            expressions.add(COURT_LOCATION.address3);
            expressions.add(COURT_LOCATION.address4);
            expressions.add(COURT_LOCATION.address5);
            expressions.add(COURT_LOCATION.address6);
        }

        expressions.add(COURT_LOCATION.locPhone);
        expressions.add(POOL_REQUEST.attendTime);
        expressions.add(COURT_LOCATION.signatory);
        expressions.add(COURT_LOCATION.locCode);
        expressions.add(COURT_LOCATION.postcode);

        // Juror information
        expressions.add(JUROR_POOL.juror.firstName);
        expressions.add(JUROR_POOL.juror.lastName);
        expressions.add(JUROR_POOL.juror.addressLine1);
        expressions.add(JUROR_POOL.juror.addressLine2);
        expressions.add(JUROR_POOL.juror.addressLine3);
        expressions.add(JUROR_POOL.juror.addressLine4);
        expressions.add(JUROR_POOL.juror.addressLine5);
        expressions.add(JUROR_POOL.juror.postcode);
        expressions.add(JUROR_POOL.juror.jurorNumber);
        expressions.add(POOL_REQUEST.poolNumber);
        expressions.add(POOL_REQUEST.attendTime);

        return expressions;
    }
}
