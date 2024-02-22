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
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.POSTPONED;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class CourtPrintLetterRepositoryImpl implements CourtPrintLetterRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QWelshCourtLocation WELSH_COURT_LOCATION = QWelshCourtLocation.welshCourtLocation;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QJuror JUROR = QJuror.juror;

    @Override
    public Tuple retrievePrintInformation(String jurorNumber, CourtLetterType courtLetterType, boolean welsh,
                                          String owner) {
        List<Expression<?>> expressions = fetchCommonPrintData(welsh);

        // Add switch/if statements for separate letter types e.g. certificate of attendance
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
            default -> throw new MojException.NotImplemented("letter type not implemented", null);
        }

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

        switch (courtLetterType) {
            case DEFERRAL_GRANTED, POSTPONED -> {
                if (POSTPONED.equals(courtLetterType)) {
                    query.join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
                        .where(JUROR_POOL.deferralCode.eq(ExcusalCodeEnum.P.getCode()));
                }
                query.where(JUROR_POOL.status.status.eq(IJurorStatus.DEFERRED))
                    .orderBy(JUROR_POOL.deferralDate.desc());
            }
            case DEFERRAL_REFUSED, EXCUSAL_REFUSED -> query
                .where(JUROR_POOL.isActive.eq(true));
            case EXCUSAL_GRANTED -> query
                .where(JUROR_POOL.status.status.eq(IJurorStatus.EXCUSED))
                .orderBy(JUROR_POOL.juror.excusalDate.desc());
            case WITHDRAWAL -> query
                .where(JUROR_POOL.status.status.eq(IJurorStatus.DISQUALIFIED))
                .orderBy(JUROR_POOL.juror.disqualifyDate.desc());
            default -> throw new MojException.NotImplemented("letter type not implemented", null);
        }
        query.where(JUROR_POOL.juror.jurorNumber.eq(jurorNumber));
        query.where(JUROR_POOL.owner.eq(owner));

        return query.fetchOne();
    }

    private List<Expression<?>> fetchCommonPrintData(boolean welsh) {
        List<Expression<?>> expressions = new ArrayList<>();

        // Court information
        if (!welsh) {
            expressions.add(COURT_LOCATION.locCode);
            expressions.add(COURT_LOCATION.name);
            expressions.add(COURT_LOCATION.address1);
            expressions.add(COURT_LOCATION.address2);
            expressions.add(COURT_LOCATION.address3);
            expressions.add(COURT_LOCATION.address4);
            expressions.add(COURT_LOCATION.address5);
            expressions.add(COURT_LOCATION.address6);
        } else {
            expressions.add(WELSH_COURT_LOCATION.locCourtName);
            expressions.add(WELSH_COURT_LOCATION.address1);
            expressions.add(WELSH_COURT_LOCATION.address2);
            expressions.add(WELSH_COURT_LOCATION.address3);
            expressions.add(WELSH_COURT_LOCATION.address4);
            expressions.add(WELSH_COURT_LOCATION.address5);
            expressions.add(WELSH_COURT_LOCATION.address6);
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
