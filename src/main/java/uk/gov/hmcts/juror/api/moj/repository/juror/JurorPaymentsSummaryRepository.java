package uk.gov.hmcts.juror.api.moj.repository.juror;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

@Repository
public class JurorPaymentsSummaryRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QReportsJurorPayments PAYMENTS = QReportsJurorPayments.reportsJurorPayments;

    public List<Tuple> fetchPaymentLogByJuror(String jurorNumber) {
        JPAQueryFactory queryFactory = getQueryFactory();

        return queryFactory.from(PAYMENTS)
            .select(
                PAYMENTS.attendanceDate,
                PAYMENTS.attendanceAudit,
                PAYMENTS.latestPaymentFAuditId,
                PAYMENTS.paymentDate,
                PAYMENTS.totalTravelDue,
                PAYMENTS.totalFinancialLossDue,
                PAYMENTS.subsistenceDue,
                PAYMENTS.smartCardDue,
                PAYMENTS.totalDue,
                PAYMENTS.totalPaid
            ).where(
                PAYMENTS.jurorNumber.eq(jurorNumber),
                PAYMENTS.locCode.eq(SecurityUtil.getLocCode())
            ).fetch();
    }

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
