package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
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
        return findPreviousFinancialAuditDetailsAppearancesBase(financialAuditDetails, appearance, null, false);
    }

    @Override
    @SuppressWarnings("LineLength")
    public Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearancesWithGenericTypeExcludingProvidedAuditDetails(
        FinancialAuditDetails.Type.GenericType genericType, FinancialAuditDetails financialAuditDetails,
        Appearance appearance) {
        return findPreviousFinancialAuditDetailsAppearancesBase(financialAuditDetails, appearance, genericType, true);
    }

    public Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearancesBase(
        FinancialAuditDetails financialAuditDetails,
        Appearance appearance,
        FinancialAuditDetails.Type.GenericType genericType,
        boolean excludeProvidedAuditDetails) {
        JPAQueryFactory queryFactory = getJpaQueryFactory();

        BooleanExpression financialAuditDetailsJoinCondition = QFinancialAuditDetails.financialAuditDetails.id
            .eq(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.financialAuditId)
            .and(QFinancialAuditDetails.financialAuditDetails.locCode.eq(QFinancialAuditDetails.financialAuditDetails.locCode))
            .and(QFinancialAuditDetails.financialAuditDetails.jurorNumber.eq(financialAuditDetails.getJurorNumber()))
            .and(QFinancialAuditDetails.financialAuditDetails.locCode.eq(financialAuditDetails.getLocCode()));

        if (excludeProvidedAuditDetails) {
            financialAuditDetailsJoinCondition = financialAuditDetailsJoinCondition
                .and(QFinancialAuditDetails.financialAuditDetails.id.ne(financialAuditDetails.getId()));
        }
        if (genericType != null) {
            financialAuditDetailsJoinCondition = financialAuditDetailsJoinCondition
                .and(QFinancialAuditDetails.financialAuditDetails.type.in(genericType.getTypes()));
        }

        JPAQuery<FinancialAuditDetailsAppearances> query =
            queryFactory.select(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances)
                .from(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances)
                .where(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.attendanceDate.eq(
                    appearance.getAttendanceDate()))
                .where(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.appearanceVersion.lt(
                    appearance.getVersion()))
                .join(QFinancialAuditDetails.financialAuditDetails)
                .on(financialAuditDetailsJoinCondition)
                .orderBy(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.appearanceVersion.desc());
        return Optional.ofNullable(query.fetchFirst());
    }
}
