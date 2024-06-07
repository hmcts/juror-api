package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.QFinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.QFinancialAuditDetailsAppearances;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;

public class IFinancialAuditDetailsRepositoryImpl implements IFinancialAuditDetailsRepository {
    @PersistenceContext
    EntityManager entityManager;


    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public FinancialAuditDetails findLastFinancialAuditDetailsWithType(
        FinancialAuditDetails financialAuditDetails,
        FinancialAuditDetails.Type.GenericType genericType,
        SortMethod sortMethod) {
        JPAQueryFactory queryFactory = getJpaQueryFactory();
        JPAQuery<FinancialAuditDetails> query = queryFactory.select(QFinancialAuditDetails.financialAuditDetails)
            .from(QFinancialAuditDetails.financialAuditDetails)
            .join(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances)
            .on(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.financialAuditId
                .eq(financialAuditDetails.getId())
                .and(QFinancialAuditDetailsAppearances.financialAuditDetailsAppearances.locCode.eq(
                    financialAuditDetails.getLocCode()))
            )
            .where(QFinancialAuditDetails.financialAuditDetails.jurorNumber.eq(financialAuditDetails.getJurorNumber()))
            .where(QFinancialAuditDetails.financialAuditDetails.locCode.eq(financialAuditDetails.getLocCode()))
            .where(QFinancialAuditDetails.financialAuditDetails.type.in(genericType.getTypes()))
            //If more then one type is passed, then get the very first one
            .orderBy(sortMethod.from(QFinancialAuditDetails.financialAuditDetails.createdOn));
        return query.fetchFirst();
    }
}
