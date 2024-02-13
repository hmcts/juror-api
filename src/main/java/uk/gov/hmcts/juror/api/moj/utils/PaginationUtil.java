package uk.gov.hmcts.juror.api.moj.utils;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;

import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.MAX_ITEMS_EXCEEDED;

public final class PaginationUtil {
    private PaginationUtil() {

    }

    public static <T, I> PaginatedList<T> toPaginatedList(JPQLQuery<I> query, IsPageable isPageable,
                                                          @NotNull SortMethod.HasComparableExpression defaultSortField,
                                                          @NotNull SortMethod defaultSortMethod,
                                                          Function<I, T> dataMapper,
                                                          Long maxItems) {
        PaginatedList<T> paginatedList = new PaginatedList<>();
        paginatedList.setCurrentPage(isPageable.getPageNumber());
        query.limit(isPageable.getPageLimit())
            .offset(isPageable.getPageLimit() * (isPageable.getPageNumber() - 1));


        SortMethod sortMethod = Optional.ofNullable(isPageable.getSortMethod())
            .orElse(defaultSortMethod);

        if (isPageable.getSortField() == null) {
            query.orderBy(sortMethod.from(defaultSortField));
        } else {
            query.orderBy(sortMethod.from(isPageable.getSortField()));
            if (!isPageable.getSortField().equals(defaultSortField)) {
                query.orderBy(defaultSortMethod.from(defaultSortField));
            }
        }
        QueryResults<I> results = query.fetchResults();
        paginatedList.setTotalItems(results.getTotal(), isPageable.getPageLimit());
        paginatedList.setData(results.getResults().stream().map(dataMapper).toList());
        if (maxItems != null && paginatedList.getTotalItems() > maxItems) {
            throw new MojException.BusinessRuleViolation(
                "A max of " + maxItems + " items can be returned but got " + paginatedList.getTotalItems(),
                MAX_ITEMS_EXCEEDED);
        }
        return paginatedList;
    }
}
