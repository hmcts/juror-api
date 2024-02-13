package uk.gov.hmcts.juror.api.moj.utils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.MAX_ITEMS_EXCEEDED;

@SuppressWarnings({
    "PMD.LinguisticNaming",
    "unchecked"
})
class PaginationUtilTest {

    @Test
    void positiveSinglePageOnLimit() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        doReturn(query).when(query).limit(anyLong());
        doReturn(query).when(query).offset(anyLong());
        doReturn(query).when(query).orderBy(any());

        IsPageable isPageable = mock(IsPageable.class);
        when(isPageable.getPageNumber()).thenReturn(1L);
        when(isPageable.getPageLimit()).thenReturn(5L);

        List<Tuple> tupleList =
            List.of(mock(Tuple.class), mock(Tuple.class), mock(Tuple.class), mock(Tuple.class), mock(Tuple.class));
        List<String> resultList = List.of("Result 1", "Result 2", "Result 3", "Result 4", "Result 5");
        QueryResults<Tuple> results = new QueryResults<>(
            tupleList,
            5L,
            0L,
            5L
        );
        SortMethod.HasComparableExpression comparableExpression = mock(SortMethod.HasComparableExpression.class);
        SortMethod sortMethod = mock(SortMethod.class);
        OrderSpecifier<?> order = mock(OrderSpecifier.class);
        doReturn(order).when(sortMethod).from(comparableExpression);
        doReturn(results).when(query).fetchResults();
        PaginatedList<String> list = PaginationUtil.toPaginatedList(query, isPageable,
            comparableExpression, sortMethod,
            getSupportFunction(tupleList, resultList), 500L);

        assertThat(list).isNotNull();
        assertThat(list.getCurrentPage()).isEqualTo(1);
        assertThat(list.getTotalItems()).isEqualTo(5);
        assertThat(list.getTotalPages()).isEqualTo(1);
        assertThat(list.getData()).isEqualTo(resultList);

        verify(sortMethod, times(1)).from(comparableExpression);
        verify(query, times(1)).orderBy(order);
    }

    @Test
    void positiveSinglePageBelowLimit() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        doReturn(query).when(query).limit(anyLong());
        doReturn(query).when(query).offset(anyLong());

        IsPageable isPageable = mock(IsPageable.class);
        when(isPageable.getPageNumber()).thenReturn(1L);
        when(isPageable.getPageLimit()).thenReturn(5L);
        SortMethod pageableSortMethod = mock(SortMethod.class);
        when(isPageable.getSortMethod()).thenReturn(pageableSortMethod);
        SortMethod.HasComparableExpression pageableComparableExpression =
            mock(SortMethod.HasComparableExpression.class);

        when(isPageable.getSortField()).thenReturn(pageableComparableExpression);
        OrderSpecifier<?> pagableOrder = mock(OrderSpecifier.class);
        doReturn(pagableOrder).when(pageableSortMethod).from(pageableComparableExpression);

        List<Tuple> tupleList =
            List.of(mock(Tuple.class), mock(Tuple.class), mock(Tuple.class), mock(Tuple.class));
        List<String> resultList = List.of("Result 1", "Result 2", "Result 3", "Result 4");
        QueryResults<Tuple> results = new QueryResults<>(
            tupleList,
            5L,
            0L,
            4L
        );

        SortMethod.HasComparableExpression comparableExpression = mock(SortMethod.HasComparableExpression.class);
        SortMethod sortMethod = mock(SortMethod.class);
        OrderSpecifier<?> order = mock(OrderSpecifier.class);
        doReturn(order).when(sortMethod).from(comparableExpression);

        doReturn(results).when(query).fetchResults();
        PaginatedList<String> list = PaginationUtil.toPaginatedList(query, isPageable,
            comparableExpression, sortMethod,
            getSupportFunction(tupleList, resultList), 500L);

        assertThat(list).isNotNull();
        assertThat(list.getCurrentPage()).isEqualTo(1);
        assertThat(list.getTotalItems()).isEqualTo(4);
        assertThat(list.getTotalPages()).isEqualTo(1);
        assertThat(list.getData()).isEqualTo(resultList);

        verify(pageableSortMethod, times(1)).from(pageableComparableExpression);
        verify(query, times(1)).orderBy(pagableOrder);

        verify(sortMethod, times(1)).from(comparableExpression);
        verify(query, times(1)).orderBy(order);
    }

    @Test
    void positiveMultiPageOnLimit() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        doReturn(query).when(query).limit(anyLong());
        doReturn(query).when(query).offset(anyLong());

        IsPageable isPageable = mock(IsPageable.class);
        when(isPageable.getPageNumber()).thenReturn(2L);
        when(isPageable.getPageLimit()).thenReturn(5L);
        SortMethod pageableSortMethod = mock(SortMethod.class);
        when(isPageable.getSortMethod()).thenReturn(pageableSortMethod);
        when(isPageable.getSortField()).thenReturn(null);

        List<Tuple> tupleList =
            List.of(mock(Tuple.class), mock(Tuple.class), mock(Tuple.class), mock(Tuple.class), mock(Tuple.class));
        List<String> resultList = List.of("Result 1", "Result 2", "Result 3", "Result 4", "Result 5");
        QueryResults<Tuple> results = new QueryResults<>(
            tupleList,
            5L,
            5L,
            20L
        );
        SortMethod.HasComparableExpression comparableExpression = mock(SortMethod.HasComparableExpression.class);
        SortMethod sortMethod = mock(SortMethod.class);
        OrderSpecifier<?> order = mock(OrderSpecifier.class);
        doReturn(order).when(pageableSortMethod).from(comparableExpression);

        doReturn(results).when(query).fetchResults();
        PaginatedList<String> list = PaginationUtil.toPaginatedList(query, isPageable,
            comparableExpression, sortMethod,
            getSupportFunction(tupleList, resultList), 500L);

        assertThat(list).isNotNull();
        assertThat(list.getCurrentPage()).isEqualTo(2);
        assertThat(list.getTotalItems()).isEqualTo(20);
        assertThat(list.getTotalPages()).isEqualTo(4);
        assertThat(list.getData()).isEqualTo(resultList);
        verify(pageableSortMethod, times(1)).from(comparableExpression);
        verify(query, times(1)).orderBy(order);
    }

    @Test
    void positiveMultiPageBelowLimit() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        doReturn(query).when(query).limit(anyLong());
        doReturn(query).when(query).offset(anyLong());

        IsPageable isPageable = mock(IsPageable.class);
        when(isPageable.getPageNumber()).thenReturn(2L);
        when(isPageable.getPageLimit()).thenReturn(5L);
        SortMethod pageableSortMethod = mock(SortMethod.class);
        when(isPageable.getSortMethod()).thenReturn(pageableSortMethod);
        SortMethod.HasComparableExpression pageableComparableExpression =
            mock(SortMethod.HasComparableExpression.class);

        List<Tuple> tupleList =
            List.of(mock(Tuple.class), mock(Tuple.class), mock(Tuple.class), mock(Tuple.class));
        List<String> resultList = List.of("Result 1", "Result 2", "Result 3", "Result 4");
        QueryResults<Tuple> results = new QueryResults<>(
            tupleList,
            4L,
            5L,
            20L
        );

        OrderSpecifier<?> order = mock(OrderSpecifier.class);
        doReturn(order).when(pageableSortMethod).from(pageableComparableExpression);

        doReturn(results).when(query).fetchResults();
        PaginatedList<String> list = PaginationUtil.toPaginatedList(query, isPageable,
            pageableComparableExpression, mock(SortMethod.class),
            getSupportFunction(tupleList, resultList), 20L);

        assertThat(list).isNotNull();
        assertThat(list.getCurrentPage()).isEqualTo(2);
        assertThat(list.getTotalItems()).isEqualTo(20);
        assertThat(list.getTotalPages()).isEqualTo(4);
        assertThat(list.getData()).isEqualTo(resultList);


        verify(pageableSortMethod, times(1)).from(pageableComparableExpression);
        verify(query, times(1)).orderBy(order);
    }


    @Test
    void negativeLimitExceeded() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        doReturn(query).when(query).limit(anyLong());
        doReturn(query).when(query).offset(anyLong());

        IsPageable isPageable = mock(IsPageable.class);
        when(isPageable.getPageNumber()).thenReturn(2L);
        when(isPageable.getPageLimit()).thenReturn(5L);
        SortMethod pageableSortMethod = mock(SortMethod.class);
        when(isPageable.getSortMethod()).thenReturn(pageableSortMethod);
        SortMethod.HasComparableExpression pageableComparableExpression =
            mock(SortMethod.HasComparableExpression.class);

        List<Tuple> tupleList =
            List.of(mock(Tuple.class), mock(Tuple.class), mock(Tuple.class), mock(Tuple.class));
        List<String> resultList = List.of("Result 1", "Result 2", "Result 3", "Result 4");
        QueryResults<Tuple> results = new QueryResults<>(
            tupleList,
            4L,
            5L,
            21L
        );

        OrderSpecifier<?> order = mock(OrderSpecifier.class);
        doReturn(order).when(pageableSortMethod).from(pageableComparableExpression);

        doReturn(results).when(query).fetchResults();
        MojException.BusinessRuleViolation exception =
            assertThrows(MojException.BusinessRuleViolation.class,
                () -> PaginationUtil.toPaginatedList(query, isPageable,
                    pageableComparableExpression, mock(SortMethod.class),
                    getSupportFunction(tupleList, resultList), 20L),
                "should throw exception when items returned exceeds limit");
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(
            "A max of 20 items can be returned but got 21"
        );
        assertThat(exception.getErrorCode()).isEqualTo(MAX_ITEMS_EXCEEDED);
        assertThat(exception.getCause()).isNull();

    }

    private <T> Function<Tuple, T> getSupportFunction(List<Tuple> tuples, List<T> results) {
        Map<Tuple, T> data = new ConcurrentHashMap<>();
        for (int i = 0; i < tuples.size(); i++) {
            data.put(tuples.get(i), results.get(i));
        }
        return data::get;
    }
}