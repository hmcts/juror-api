package uk.gov.hmcts.juror.api.moj.domain.messages;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorSearch;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.TooManyMethods",
    "unchecked"
})
class MessageSearchTest {


    @Test
    void positiveApplyPoolNumberOnly() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .build();

        messageSearch.apply(query);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.startsWith(TestConstants.VALID_POOL_NUMBER));

        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyTrialNumberOnly() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .trialNumber(TestConstants.VALID_TRIAL_NUMBER)
            .build();

        messageSearch.apply(query);

        verify(query, times(1))
            .where(QTrial.trial.trialNumber.eq(TestConstants.VALID_TRIAL_NUMBER));

        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyNextDueAtCourtOnly() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        LocalDate localDate = LocalDate.now();
        MessageSearch messageSearch = MessageSearch.builder()
            .nextDueAtCourt(localDate)
            .build();

        messageSearch.apply(query);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.nextDate.eq(localDate));

        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyDateDeferredToOnly() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        LocalDate localDate = LocalDate.now();
        MessageSearch messageSearch = MessageSearch.builder()
            .dateDeferredTo(localDate)
            .build();

        messageSearch.apply(query);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.deferralDate.eq(localDate));

        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveOnlyDeferralsInCourt() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .onlyDeferralsInCourt(TestConstants.VALID_COURT_LOCATION)
            .build();

        messageSearch.apply(query);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));
        verify(query, times(1))
            .where(MessageSearch.Filter.SHOW_ONLY_DEFERRED.getExpression());
        verifyNoMoreInteractions(query);
    }


    @Test
    void positiveApplyJurorSearchNull() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .jurorSearch(null)
            .build();
        messageSearch.applyJurorSearch(query, messageSearch.getJurorSearch());
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyJurorSearchJurorNameOnly() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .jurorSearch(JurorSearch.builder()
                .jurorName("ABC a")
                .build())
            .build();

        messageSearch.applyJurorSearch(query, messageSearch.getJurorSearch());

        verify(query, times(1))
            .where(QJuror.juror.firstName.concat(" ").concat(QJuror.juror.lastName).toLowerCase()
                .contains("abc a"));
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyJurorSearchJurorNumberOnly() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .jurorSearch(JurorSearch.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .build())
            .build();

        messageSearch.applyJurorSearch(query, messageSearch.getJurorSearch());

        verify(query, times(1))
            .where(QJuror.juror.jurorNumber.startsWith(TestConstants.VALID_JUROR_NUMBER));
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyJurorSearchPostcodeOnly() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .jurorSearch(JurorSearch.builder()
                .postcode(TestConstants.VALID_POSTCODE)
                .build())
            .build();

        messageSearch.applyJurorSearch(query, messageSearch.getJurorSearch());

        verify(query, times(1))
            .where(QJuror.juror.postcode
                .eq(TestConstants.VALID_POSTCODE));
        verifyNoMoreInteractions(query);
    }


    @Test
    void positiveApplyMessageFilterNull() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .filters(null)
            .build();
        messageSearch.applyMessageFilter(query, messageSearch.getFilters());
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyMessageFilterOnlyOr() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .filters(List.of(
                MessageSearch.Filter.INCLUDE_COMPLETED,
                MessageSearch.Filter.INCLUDE_ON_CALL,
                MessageSearch.Filter.INCLUDE_TRANSFERRED
            ))
            .build();

        when(query.where(any())).thenReturn(query);

        messageSearch.applyMessageFilter(query, messageSearch.getFilters());

        verify(query, times(1))
            .where(MessageSearch.Filter.INCLUDE_COMPLETED.getExpression()
                .or(MessageSearch.Filter.INCLUDE_ON_CALL.getExpression())
                .or(MessageSearch.Filter.INCLUDE_TRANSFERRED.getExpression()));

        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyMessageFilterOnlyOrSingle() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .filters(List.of(
                MessageSearch.Filter.INCLUDE_COMPLETED
            ))
            .build();

        when(query.where(any())).thenReturn(query);

        messageSearch.applyMessageFilter(query, messageSearch.getFilters());

        verify(query, times(1))
            .where(MessageSearch.Filter.INCLUDE_COMPLETED.getExpression());

        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyMessageFilterOnlyAnd() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .filters(List.of(
                MessageSearch.Filter.SHOW_ONLY_RESPONDED,
                MessageSearch.Filter.SHOW_ONLY_ON_CALL,
                MessageSearch.Filter.SHOW_ONLY_FAILED_TO_ATTEND
            ))
            .build();

        when(query.where(any())).thenReturn(query);

        messageSearch.applyMessageFilter(query, messageSearch.getFilters());

        verify(query, times(1))
            .where(MessageSearch.Filter.SHOW_ONLY_RESPONDED.getExpression()
                .and(MessageSearch.Filter.SHOW_ONLY_ON_CALL.getExpression())
                .and(MessageSearch.Filter.SHOW_ONLY_FAILED_TO_ATTEND.getExpression()));
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyMessageFilterOnlyAndSingle() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .filters(List.of(
                MessageSearch.Filter.SHOW_ONLY_RESPONDED
            ))
            .build();

        when(query.where(any())).thenReturn(query);

        messageSearch.applyMessageFilter(query, messageSearch.getFilters());

        verify(query, times(1))
            .where(MessageSearch.Filter.SHOW_ONLY_RESPONDED.getExpression());
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveApplyMessageFilterBothOrAndAnd() {
        JPQLQuery<Tuple> query = mock(JPQLQuery.class);
        MessageSearch messageSearch = MessageSearch.builder()
            .filters(List.of(
                MessageSearch.Filter.SHOW_ONLY_RESPONDED,
                MessageSearch.Filter.SHOW_ONLY_ON_CALL,
                MessageSearch.Filter.SHOW_ONLY_FAILED_TO_ATTEND,
                MessageSearch.Filter.INCLUDE_COMPLETED,
                MessageSearch.Filter.INCLUDE_ON_CALL,
                MessageSearch.Filter.INCLUDE_TRANSFERRED
            ))
            .build();

        when(query.where(any())).thenReturn(query);

        messageSearch.applyMessageFilter(query, messageSearch.getFilters());


        verify(query, times(1))
            .where(MessageSearch.Filter.INCLUDE_COMPLETED.getExpression()
                .or(MessageSearch.Filter.INCLUDE_ON_CALL.getExpression())
                .or(MessageSearch.Filter.INCLUDE_TRANSFERRED.getExpression()));

        verify(query, times(1))
            .where(MessageSearch.Filter.SHOW_ONLY_RESPONDED.getExpression()
                .and(MessageSearch.Filter.SHOW_ONLY_ON_CALL.getExpression())
                .and(MessageSearch.Filter.SHOW_ONLY_FAILED_TO_ATTEND.getExpression()));
        verifyNoMoreInteractions(query);

    }

    @TestFactory
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    Stream<DynamicTest> sortFieldTests() {
        return Stream.of(
            sortFieldTest(MessageSearch.SortField.JUROR_NUMBER, QJuror.juror.jurorNumber),
            sortFieldTest(MessageSearch.SortField.FIRST_NAME, QJuror.juror.firstName),
            sortFieldTest(MessageSearch.SortField.LAST_NAME, QJuror.juror.lastName),
            sortFieldTest(MessageSearch.SortField.EMAIL, QJuror.juror.email),
            sortFieldTest(MessageSearch.SortField.PHONE, QJuror.juror.phoneNumber),
            sortFieldTest(MessageSearch.SortField.POOL_NUMBER,
                Expressions.asComparable(QJurorPool.jurorPool.pool.poolNumber)),
            sortFieldTest(MessageSearch.SortField.STATUS, QJurorPool.jurorPool.status.status),
            sortFieldTest(MessageSearch.SortField.TRIAL_NUMBER, QTrial.trial.trialNumber),
            sortFieldTest(MessageSearch.SortField.ON_CALL, QJurorPool.jurorPool.onCall),
            sortFieldTest(MessageSearch.SortField.NEXT_DUE_AT_COURT_DATE, QJurorPool.jurorPool.nextDate),
            sortFieldTest(MessageSearch.SortField.DATE_DEFERRED_TO, QJurorPool.jurorPool.deferralDate),
            sortFieldTest(MessageSearch.SortField.COMPLETION_DATE, QJuror.juror.completionDate),
            sortFieldTest(MessageSearch.SortField.WELSH_LANGUAGE, QJuror.juror.welsh)
        );
    }

    DynamicTest sortFieldTest(MessageSearch.SortField sortField, ComparableExpressionBase<?> expected) {
        return DynamicTest.dynamicTest(sortField + " - Returns correct expression",
            () -> {
                assertThat(sortField).isNotNull();
                assertThat(sortField.getComparableExpression()).isEqualTo(expected);
            });
    }

    @TestFactory
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    Stream<DynamicTest> filterTests() {
        return Stream.of(
            filterTest(MessageSearch.Filter.INCLUDE_ON_CALL, true, QJurorPool.jurorPool.onCall.eq(true)),
            filterTest(MessageSearch.Filter.INCLUDE_FAILED_TO_ATTEND, true,
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.FAILED_TO_ATTEND)),
            filterTest(MessageSearch.Filter.INCLUDE_DEFERRED, true,
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)),
            filterTest(MessageSearch.Filter.INCLUDE_JURORS_AND_PANELLED, true,
                QJurorPool.jurorPool.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR)),
            filterTest(MessageSearch.Filter.INCLUDE_COMPLETED, true,
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.COMPLETED)),
            filterTest(MessageSearch.Filter.INCLUDE_TRANSFERRED, true,
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.TRANSFERRED)),
            filterTest(MessageSearch.Filter.INCLUDE_DISQUALIFIED_AND_EXCUSED, true,
                QJurorPool.jurorPool.status.status.in(IJurorStatus.DISQUALIFIED,
                    IJurorStatus.EXCUSED)),
            filterTest(MessageSearch.Filter.SHOW_ONLY_ON_CALL, false, QJurorPool.jurorPool.onCall.eq(true)),
            filterTest(MessageSearch.Filter.SHOW_ONLY_FAILED_TO_ATTEND, false,
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.FAILED_TO_ATTEND)),
            filterTest(MessageSearch.Filter.SHOW_ONLY_DEFERRED, false,
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)),
            filterTest(MessageSearch.Filter.SHOW_ONLY_RESPONDED, false,
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED))
        );
    }

    DynamicTest filterTest(MessageSearch.Filter filter, boolean isInclude, BooleanExpression expected) {
        return DynamicTest.dynamicTest(filter + " - Returns correct expression and include",
            () -> {
                assertThat(filter).isNotNull();
                assertThat(filter.isInclude()).isEqualTo(isInclude);
                assertThat(filter.getExpression()).isEqualTo(expected);
            });
    }
}
