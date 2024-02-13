package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessage;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.messages.DataType;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessagePlaceholders;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports",
    "unchecked"
})
class MessageTemplateRepositoryImplTest {

    private MessageTemplateRepositoryImpl messageTemplateRepositoryImpl;
    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPanel JUROR_TRIAL = QPanel.panel;

    private static final QTrial TRIAL = QTrial.trial;

    @BeforeEach
    void beforeEach() {
        queryFactory = mock(JPAQueryFactory.class);
        entityManager = mock(EntityManager.class);
        messageTemplateRepositoryImpl = spy(new MessageTemplateRepositoryImpl(entityManager));
        doReturn(queryFactory).when(messageTemplateRepositoryImpl).getQueryFactory();
    }


    @Test
    void positiveSimpleResponse() {
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class);
        MessageSearch messageSearch = mock(MessageSearch.class);

        when(queryFactory.select(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);

        doNothing().when(messageSearch).apply(jpaQuery);

        when(messageSearch.getPageNumber()).thenReturn(1L);
        when(messageSearch.getPageLimit()).thenReturn(4L);

        QueryResults<Tuple> queryResult = mock(QueryResults.class);
        doReturn(queryResult).when(jpaQuery).fetchResults();
        doReturn(3L).when(queryResult).getTotal();

        Tuple result1 = mock(Tuple.class);
        Tuple result2 = mock(Tuple.class);
        Tuple result3 = mock(Tuple.class);
        doReturn(List.of(result1, result2, result3)).when(queryResult).getResults();


        PaginatedList<JurorToSendMessage> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, true, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        verify(jpaQuery, times(1)).where(JUROR_POOL.pool.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));

        verify(jpaQuery, times(1)).limit(4L);
        verify(jpaQuery, times(1)).offset(0L);
        verify(jpaQuery, times(1)).orderBy(
            new OrderSpecifier<?>[]{SortMethod.ASC.from(MessageSearch.SortField.JUROR_NUMBER)});
        verify(jpaQuery, times(1)).fetchResults();


        verify(queryFactory, times(1))
            .select(
                JUROR.jurorNumber,
                JUROR_POOL.pool.poolNumber,
                JUROR.email,
                JUROR.phoneNumber,
                JUROR.welsh
            );
        verify(messageSearch, times(1))
            .apply(jpaQuery);

        verify(queryResult, times(1)).getTotal();
        verify(queryResult, times(1)).getResults();

        verifyNoMoreInteractions(queryResult,
            queryFactory,
            jpaQuery);
    }

    @Test
    void positiveComplexResponse() {
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class);
        MessageSearch messageSearch = mock(MessageSearch.class);

        when(queryFactory.select(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.leftJoin(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate[].class))).thenReturn(jpaQuery);

        doNothing().when(messageSearch).apply(jpaQuery);

        when(messageSearch.getPageNumber()).thenReturn(1L);
        when(messageSearch.getPageLimit()).thenReturn(4L);

        QueryResults<Tuple> queryResult = mock(QueryResults.class);
        doReturn(queryResult).when(jpaQuery).fetchResults();
        doReturn(3L).when(queryResult).getTotal();

        Tuple result1 = mock(Tuple.class);
        Tuple result2 = mock(Tuple.class);
        Tuple result3 = mock(Tuple.class);
        doReturn(List.of(result1, result2, result3)).when(queryResult).getResults();


        PaginatedList<JurorToSendMessage> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, false, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        verify(jpaQuery, times(1)).where(JUROR_POOL.pool.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));

        verify(jpaQuery, times(1)).leftJoin(JUROR_TRIAL);
        verify(jpaQuery, times(1)).on(
            JUROR_POOL.eq(JUROR_TRIAL.jurorPool),
            JUROR_TRIAL.result.in(PanelResult.JUROR),
            TRIAL.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION),
            JUROR_POOL.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR));


        verify(jpaQuery, times(1)).leftJoin(JUROR_TRIAL);


        verify(jpaQuery, times(1)).limit(4L);
        verify(jpaQuery, times(1)).offset(0L);
        verify(jpaQuery, times(1)).orderBy(
            new OrderSpecifier<?>[]{SortMethod.ASC.from(MessageSearch.SortField.JUROR_NUMBER)});
        verify(jpaQuery, times(1)).fetchResults();


        verify(queryFactory, times(1))
            .select(
                JUROR.jurorNumber,
                JUROR_POOL.pool.poolNumber,
                JUROR.email,
                JUROR.phoneNumber,
                JUROR.welsh,
                JUROR.firstName,
                JUROR.lastName,
                JUROR_POOL.status,
                TRIAL.trialNumber,
                JUROR_POOL.onCall,
                JUROR_POOL.nextDate,
                JUROR_POOL.deferralDate,
                JUROR.completionDate
            );
        verify(messageSearch, times(1))
            .apply(jpaQuery);

        verify(queryResult, times(1)).getTotal();
        verify(queryResult, times(1)).getResults();

        verifyNoMoreInteractions(queryResult,
            queryFactory,
            jpaQuery);
    }

    @Test
    void positiveSimpleResponseWithTrialNumber() {
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class);
        MessageSearch messageSearch = mock(MessageSearch.class);

        when(messageSearch.getTrialNumber()).thenReturn(TestConstants.VALID_TRIAL_NUMBER);
        when(queryFactory.select(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.leftJoin(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate[].class))).thenReturn(jpaQuery);

        doNothing().when(messageSearch).apply(jpaQuery);

        when(messageSearch.getPageNumber()).thenReturn(1L);
        when(messageSearch.getPageLimit()).thenReturn(4L);

        QueryResults<Tuple> queryResult = mock(QueryResults.class);
        doReturn(queryResult).when(jpaQuery).fetchResults();
        doReturn(3L).when(queryResult).getTotal();

        Tuple result1 = mock(Tuple.class);
        Tuple result2 = mock(Tuple.class);
        Tuple result3 = mock(Tuple.class);
        doReturn(List.of(result1, result2, result3)).when(queryResult).getResults();


        PaginatedList<JurorToSendMessage> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, true, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        verify(jpaQuery, times(1)).where(JUROR_POOL.pool.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));

        verify(jpaQuery, times(1)).leftJoin(JUROR_TRIAL);
        verify(jpaQuery, times(1)).on(
            JUROR_POOL.eq(JUROR_TRIAL.jurorPool),
            JUROR_TRIAL.result.in(PanelResult.JUROR),
            TRIAL.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION),
            JUROR_POOL.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR));


        verify(jpaQuery, times(1)).leftJoin(JUROR_TRIAL);


        verify(jpaQuery, times(1)).limit(4L);
        verify(jpaQuery, times(1)).offset(0L);
        verify(jpaQuery, times(1)).orderBy(
            new OrderSpecifier<?>[]{SortMethod.ASC.from(MessageSearch.SortField.JUROR_NUMBER)});
        verify(jpaQuery, times(1)).fetchResults();


        verify(queryFactory, times(1))
            .select(
                JUROR.jurorNumber,
                JUROR_POOL.pool.poolNumber,
                JUROR.email,
                JUROR.phoneNumber,
                JUROR.welsh
            );
        verify(messageSearch, times(1))
            .apply(jpaQuery);

        verify(queryResult, times(1)).getTotal();
        verify(queryResult, times(1)).getResults();

        verifyNoMoreInteractions(queryResult,
            queryFactory,
            jpaQuery);
    }

    @Test
    void positiveGetDefaultValue() {
        MessagePlaceholders messagePlaceholders = new MessagePlaceholders(
            "PLACEHOLDERNAME1",
            "SOURCETABLENAME1",
            "COLUMNNAME1",
            "Display name",
            DataType.NONE,
            "Desc",
            true,
            "",
            ""
        );
        Query query = mock(Query.class);

        doReturn(query).when(entityManager).createNativeQuery(anyString());

        final String response = "Test response";
        doReturn(response).when(query).getSingleResult();

        assertThat(messageTemplateRepositoryImpl
            .getDefaultValue(messagePlaceholders, TestConstants.VALID_COURT_LOCATION))
            .isEqualTo(response);

        verify(entityManager, times(1))
            .createNativeQuery("SELECT COLUMNNAME1 from juror_mod.SOURCETABLENAME1 where loc_code=?");
        verify(query, times(1))
            .setParameter(1, TestConstants.VALID_COURT_LOCATION);
        verify(query, times(1)).getSingleResult();
        verifyNoMoreInteractions(entityManager, query);
    }
}
