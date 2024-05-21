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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBureau;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageCourt;
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
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({
    "PMD.ExcessiveImports",
    "unchecked"
})
class MessageTemplateRepositoryImplTest {

    private MessageTemplateRepositoryImpl messageTemplateRepositoryImpl;
    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPanel PANEL = QPanel.panel;

    private static final QTrial TRIAL = QTrial.trial;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        queryFactory = mock(JPAQueryFactory.class);
        entityManager = mock(EntityManager.class);
        messageTemplateRepositoryImpl = spy(new MessageTemplateRepositoryImpl(entityManager));
        doReturn(queryFactory).when(messageTemplateRepositoryImpl).getQueryFactory();
    }


    @AfterEach
    void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }

    private void mockCourtUser() {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
    }

    private void mockBureauUser() {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(false);
    }

    @Test
    void positiveSimpleResponseBureauUser() {
        mockBureauUser();
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class,
            withSettings());
        MessageSearch messageSearch = mock(MessageSearch.class);

        when(queryFactory.select(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
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


        PaginatedList<? extends JurorToSendMessageBase> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, "400", true, 500L);


        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        assertThat(result.getData().get(0)).isInstanceOf(JurorToSendMessageBureau.class);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.eq(JUROR_POOL.juror));

        verify(jpaQuery, times(1)).limit(4L);
        verify(jpaQuery, times(1)).offset(0L);
        verify(jpaQuery, times(1)).orderBy(
            new OrderSpecifier<?>[]{SortMethod.ASC.from(MessageSearch.SortField.JUROR_NUMBER)});
        verify(jpaQuery, times(1)).fetchResults();

        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.isTrue());

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
        securityUtilMockedStatic.verify(SecurityUtil::isCourt, times(1));
        securityUtilMockedStatic.verifyNoMoreInteractions();
        verifyNoMoreInteractions(queryResult,
            queryFactory,
            jpaQuery);
    }

    @Test
    void positiveComplexResponseBureau() {
        mockBureauUser();
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


        PaginatedList<? extends JurorToSendMessageBase> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, "400", false, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        assertThat(result.getData().get(0)).isInstanceOf(JurorToSendMessageBureau.class);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.eq(JUROR_POOL.juror));

        verify(jpaQuery, times(1)).leftJoin(PANEL);
        verify(jpaQuery, times(1)).on(
            JUROR.eq(PANEL.juror)
                .and(PANEL.result.notIn(PanelResult.RETURNED, PanelResult.NOT_USED, PanelResult.CHALLENGED))
                .and(PANEL.trial.courtLocation.locCode.eq(JUROR_POOL.pool.courtLocation.locCode))
                .and(JUROR_POOL.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR)));

        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.isTrue());

        verify(jpaQuery, times(1)).leftJoin(PANEL);


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
                JUROR_POOL.deferralDate
            );
        verify(messageSearch, times(1))
            .apply(jpaQuery);

        verify(queryResult, times(1)).getTotal();
        verify(queryResult, times(1)).getResults();
        securityUtilMockedStatic.verify(SecurityUtil::isCourt, times(1));
        securityUtilMockedStatic.verifyNoMoreInteractions();

        verifyNoMoreInteractions(queryResult,
            queryFactory,
            jpaQuery);
    }

    @Test
    void positiveSimpleResponseCourtUserCourt() {
        mockCourtUser();
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class,
            withSettings());
        MessageSearch messageSearch = mock(MessageSearch.class);

        when(queryFactory.select(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
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


        PaginatedList<? extends JurorToSendMessageBase> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, true, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        assertThat(result.getData().get(0)).isInstanceOf(JurorToSendMessageCourt.class);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.eq(JUROR_POOL.juror));
        verify(jpaQuery, times(1))
            .where(JUROR_POOL.pool.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));
        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.isTrue());
        verify(jpaQuery, times(1))
            .where(JUROR_POOL.pool.owner.ne("400"));

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
        securityUtilMockedStatic.verify(SecurityUtil::isCourt, times(1));
        securityUtilMockedStatic.verifyNoMoreInteractions();
        verifyNoMoreInteractions(queryResult,
            queryFactory,
            jpaQuery);
    }

    @Test
    void positiveComplexResponseCourt() {
        mockCourtUser();
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


        PaginatedList<? extends JurorToSendMessageBase> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, false, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        assertThat(result.getData().get(0)).isInstanceOf(JurorToSendMessageCourt.class);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.eq(JUROR_POOL.juror));
        verify(jpaQuery, times(1))
            .where(JUROR_POOL.pool.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));
        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.isTrue());
        verify(jpaQuery, times(1))
            .where(JUROR_POOL.pool.owner.ne("400"));

        verify(jpaQuery, times(1)).leftJoin(PANEL);
        verify(jpaQuery, times(1)).on(
            JUROR.eq(PANEL.juror)
                .and(PANEL.result.notIn(PanelResult.RETURNED, PanelResult.NOT_USED, PanelResult.CHALLENGED))
                .and(PANEL.trial.courtLocation.locCode.eq(JUROR_POOL.pool.courtLocation.locCode))
                .and(JUROR_POOL.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR)));


        verify(jpaQuery, times(1)).leftJoin(PANEL);


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
                JUROR_POOL.deferralDate,
                TRIAL.trialNumber,
                JUROR.completionDate,
                JUROR_POOL.onCall,
                JUROR_POOL.nextDate
            );
        verify(messageSearch, times(1))
            .apply(jpaQuery);

        verify(queryResult, times(1)).getTotal();
        verify(queryResult, times(1)).getResults();
        securityUtilMockedStatic.verify(SecurityUtil::isCourt, times(1));
        securityUtilMockedStatic.verifyNoMoreInteractions();

        verifyNoMoreInteractions(queryResult,
            queryFactory,
            jpaQuery);
    }

    @Test
    void positiveSimpleResponseWithTrialNumber() {
        mockCourtUser();
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


        PaginatedList<? extends JurorToSendMessageBase> result = messageTemplateRepositoryImpl
            .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, true, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(3);
        assertThat(result.getData().get(0)).isInstanceOf(JurorToSendMessageCourt.class);
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.eq(JUROR_POOL.juror));
        verify(jpaQuery, times(1))
            .where(JUROR_POOL.pool.courtLocation.locCode.eq(TestConstants.VALID_COURT_LOCATION));
        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.isTrue());
        verify(jpaQuery, times(1))
            .where(JUROR_POOL.pool.owner.ne("400"));

        verify(jpaQuery, times(1)).leftJoin(PANEL);
        verify(jpaQuery, times(1)).on(
            JUROR.eq(PANEL.juror)
                .and(PANEL.result.notIn(PanelResult.RETURNED, PanelResult.NOT_USED, PanelResult.CHALLENGED))
                .and(PANEL.trial.courtLocation.locCode.eq(JUROR_POOL.pool.courtLocation.locCode))
                .and(JUROR_POOL.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR)));


        verify(jpaQuery, times(1)).leftJoin(PANEL);


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
        securityUtilMockedStatic.verify(SecurityUtil::isCourt, times(1));
        securityUtilMockedStatic.verifyNoMoreInteractions();

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
