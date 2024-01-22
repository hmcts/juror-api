package uk.gov.hmcts.juror.api.moj.repository;


import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorExpenseTotals;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository implementation for the Juror Expense Totals entity.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class JurorExpenseTotalsRepositoryImpl implements IJurorExpenseTotalsRepository {


    @PersistenceContext
    EntityManager entityManager;

    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final QJurorExpenseTotals JUROR_EXPENSE_TOTALS = QJurorExpenseTotals.jurorExpenseTotals;


    @Override
    public List<JurorExpenseTotals> findUnpaidByCourtLocationCode(String locCode, Pageable pageable) {
        Querydsl querydsl = new Querydsl(entityManager, new PathBuilderFactory().create(JurorExpenseTotals.class));
        JPQLQuery<JurorExpenseTotals> query = new JPAQuery<>(entityManager);

        query.select(JUROR_EXPENSE_TOTALS)
            .from(JUROR_EXPENSE_TOTALS)
            .where(JUROR_EXPENSE_TOTALS.courtLocationCode.eq(locCode))
            .where(JUROR_EXPENSE_TOTALS.totalUnapproved.gt(0));

        return querydsl.applyPagination(pageable, query).fetch();
    }

    @Override
    public List<JurorExpenseTotals> findUnpaidByCourtLocationCodeAndAppearanceDate(String locCode, LocalDate minDate,
                                                                                   LocalDate maxDate,
                                                                                   Pageable pageable) {
        Querydsl querydsl = new Querydsl(entityManager, new PathBuilderFactory().create(JurorExpenseTotals.class));
        return querydsl.applyPagination(pageable, getJurorUnpaidExpenseTotalsForDateRange(locCode, minDate, maxDate))
            .fetch();
    }

    @Override
    public long countUnpaidByCourtLocationCodeAndAppearanceDate(String locCode, LocalDate minDate, LocalDate maxDate) {
        return getJurorUnpaidExpenseTotalsForDateRange(locCode, minDate, maxDate).fetchCount();
    }

    private JPQLQuery<JurorExpenseTotals> getJurorUnpaidExpenseTotalsForDateRange(String locCode, LocalDate minDate,
                                                                                  LocalDate maxDate) {
        JPQLQuery<JurorExpenseTotals> query = new JPAQuery<>(entityManager);
        JPQLQuery<JurorExpenseTotals> subQuery = new JPAQuery<>(entityManager);

        return query.select(JUROR_EXPENSE_TOTALS)
            .from(JUROR_EXPENSE_TOTALS)
            .where(JUROR_EXPENSE_TOTALS.courtLocationCode.eq(locCode))
            .where(JUROR_EXPENSE_TOTALS.totalUnapproved.gt(0))
            .where(JUROR_EXPENSE_TOTALS.jurorNumber.in(subQuery.select(APPEARANCE.jurorNumber)
                .distinct()
                .from(APPEARANCE)
                .where(APPEARANCE.courtLocation.locCode.eq(locCode))
                .where(APPEARANCE.attendanceDate.between(minDate, maxDate))));
    }

}
