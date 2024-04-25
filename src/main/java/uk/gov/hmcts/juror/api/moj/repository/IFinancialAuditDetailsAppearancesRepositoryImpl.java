package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;
import uk.gov.hmcts.juror.api.moj.domain.QFinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.QFinancialAuditDetailsAppearances;

import java.util.Optional;

public class IFinancialAuditDetailsAppearancesRepositoryImpl
    implements IFinancialAuditDetailsAppearancesRepository {
    @PersistenceContext
    EntityManager entityManager;


    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearances(
        FinancialAuditDetails financialAuditDetails, Appearance appearance) {
        JPAQueryFactory queryFactory = getJpaQueryFactory();
        JPAQuery<FinancialAuditDetailsAppearances> query =
            queryFactory.select(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances)
                .from(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances)
                .where(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.attendanceDate.eq(
                    appearance.getAttendanceDate()))
                .where(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.appearanceVersion.lt(
                    appearance.getVersion()))
                .join(QFinancialAuditDetails.financialAuditDetails)
                .on(QFinancialAuditDetails.financialAuditDetails.id
                    .eq(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.financialAuditId)
                    .and(QFinancialAuditDetails.financialAuditDetails.jurorNumber
                        .eq(financialAuditDetails.getJurorNumber())))
                .orderBy(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.appearanceVersion.desc());
        return Optional.ofNullable(query.fetchFirst());
    }
}
