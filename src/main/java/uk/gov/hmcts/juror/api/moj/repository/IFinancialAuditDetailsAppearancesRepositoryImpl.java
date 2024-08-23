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
        return findPreviousFinancialAuditDetailsAppearancesBase(
            financialAuditDetails.getId(),
            financialAuditDetails.getLocCode(),
            financialAuditDetails.getJurorNumber(),
            appearance,
            genericType,
            excludeProvidedAuditDetails);
    }

    public Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearancesBase(
        Appearance appearance,
        FinancialAuditDetails.Type.GenericType genericType,
        boolean excludeProvidedAuditDetails) {
        return findPreviousFinancialAuditDetailsAppearancesBase(
            appearance.getFinancialAudit(),
            appearance.getLocCode(),
            appearance.getJurorNumber(),
            appearance,
            genericType,
            excludeProvidedAuditDetails);
    }


    public Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearancesBase(
        long financialAuditDetailsId,
        String locCode,
        String jurorNumber,
        Appearance appearance,
        FinancialAuditDetails.Type.GenericType genericType,
        boolean excludeProvidedAuditDetails) {
        JPAQueryFactory queryFactory = getJpaQueryFactory();

        BooleanExpression financialAuditDetailsJoinCondition = QFinancialAuditDetails.financialAuditDetails.id
            .eq(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.financialAuditId)
            .and(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances
                .locCode.eq(locCode))
            .and(QFinancialAuditDetails.financialAuditDetails.jurorNumber.eq(jurorNumber))
            .and(QFinancialAuditDetails.financialAuditDetails.locCode.eq(locCode));

        if (excludeProvidedAuditDetails) {
            financialAuditDetailsJoinCondition = financialAuditDetailsJoinCondition
                .and(QFinancialAuditDetails.financialAuditDetails.id.ne(financialAuditDetailsId));
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
