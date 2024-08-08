package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IReissueLetterRepositoryImplTest {

    private static final String JUROR_NUMBER = "123456789";
    private static final String FORM_CODE = "2994A";
    private IReissueLetterRepositoryImpl reissueLetterRepositoryImpl;
    private JPAQueryFactory queryFactory;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QBulkPrintData BULK_PRINT_DATA = QBulkPrintData.bulkPrintData;

    private static final QJurorHistory JUROR_HISTORY = QJurorHistory.jurorHistory;

    @BeforeEach
    void beforeEach() {
        queryFactory = mock(JPAQueryFactory.class);
        reissueLetterRepositoryImpl = spy(new IReissueLetterRepositoryImpl());
        doReturn(queryFactory).when(reissueLetterRepositoryImpl).getQueryFactory();
    }

    @Test
    void positiveExcusalDeniedLetterProcessed() {
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectDistinct(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.leftJoin(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);

        Tuple result1 = mock(Tuple.class);
        Tuple result2 = mock(Tuple.class);
        Tuple result3 = mock(Tuple.class);
        when(jpaQuery.fetch()).thenReturn(List.of(result1, result2, result3));

        ReissueLetterListRequestDto request = ReissueLetterListRequestDto.builder()
            .letterType(LetterType.EXCUSAL_REFUSED)
            .jurorNumber(JUROR_NUMBER)
            .build();

        Consumer<JPAQuery<Tuple>> queryConsumer = tupleJPAQuery -> tupleJPAQuery
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED))
            .where(QJuror.juror.excusalRejected.eq("Y"));

        List<Tuple> tuples = reissueLetterRepositoryImpl.findLetters(request, queryConsumer);

        assertThat(tuples).isNotNull();
        assertThat(tuples.size()).isEqualTo(3);

        verify(queryFactory, times(1))
            .selectDistinct(
                JUROR.jurorNumber.as("juror_number"),
                JUROR.firstName.as("first_name"),
                JUROR.lastName.as("last_name"),
                JUROR.postcode.as("postcode"),
                JUROR_POOL.status.statusDesc.as("status"),
                JUROR.excusalDate.as("date_refused"),
                JUROR.excusalCode.as("excusal_code"),
                BULK_PRINT_DATA.creationDate.as("date_printed"),
                BULK_PRINT_DATA.extractedFlag.as("extracted_flag"),
                BULK_PRINT_DATA.formAttribute.formType.as("form_code")
            );

        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        verify(jpaQuery, times(0)).join(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).leftJoin(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(BULK_PRINT_DATA.jurorNo));
        verify(jpaQuery, times(1))
            .where(BULK_PRINT_DATA.formAttribute.formType.in(List.of("5226", "5226C")));
        verify(jpaQuery, times(1))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED));
        verify(jpaQuery, times(1)).where(QJuror.juror.excusalRejected.eq("Y"));
        verify(jpaQuery, times(1)).where(JUROR.jurorNumber.eq(JUROR_NUMBER));
        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.eq(true));
        verify(jpaQuery, times(1)).where(JUROR_POOL.owner.eq(SecurityUtil.BUREAU_OWNER));
        verify(jpaQuery, times(1)).orderBy(BULK_PRINT_DATA.creationDate.desc());
        verify(jpaQuery, times(1)).orderBy(JUROR.jurorNumber.asc());
        verify(jpaQuery, times(1)).fetch();
    }

    @Test
    void positiveDeferralDeniedLetterProcessed() {
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectDistinct(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.leftJoin(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);

        Tuple result1 = mock(Tuple.class);
        Tuple result2 = mock(Tuple.class);
        when(jpaQuery.fetch()).thenReturn(List.of(result1, result2));

        ReissueLetterListRequestDto request = ReissueLetterListRequestDto.builder()
            .letterType(LetterType.DEFERRAL_REFUSED)
            .poolNumber("123456789")
            .build();

        Consumer<JPAQuery<Tuple>> queryConsumer = tupleJPAQuery ->
            tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED));

        List<Tuple> tuples = reissueLetterRepositoryImpl.findLetters(request, queryConsumer);

        assertThat(tuples).isNotNull();
        assertThat(tuples.size()).isEqualTo(2);

        verify(queryFactory, times(1))
            .selectDistinct(
                JUROR.jurorNumber.as("juror_number"),
                JUROR.firstName.as("first_name"),
                JUROR.lastName.as("last_name"),
                JUROR.postcode.as("postcode"),
                JUROR_POOL.status.statusDesc.as("status"),
                JUROR_HISTORY.dateCreated.as("date_refused"),
                JUROR_POOL.deferralCode.as("deferral_code"),
                BULK_PRINT_DATA.creationDate.as("date_printed"),
                BULK_PRINT_DATA.extractedFlag.as("extracted_flag"),
                BULK_PRINT_DATA.formAttribute.formType.as("form_code")
            );
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        verify(jpaQuery, times(0)).join(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).leftJoin(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(BULK_PRINT_DATA.jurorNo));
        verify(jpaQuery, times(1)).join(QJurorHistory.jurorHistory);
        verify(jpaQuery, times(1))
            .on(JUROR.jurorNumber.eq(QJurorHistory.jurorHistory.jurorNumber));
        verify(jpaQuery, times(1))
            .where(QJurorHistory.jurorHistory.poolNumber.eq(JUROR_POOL.pool.poolNumber));
        verify(jpaQuery, times(1))
            .where(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.NON_DEFERRED_LETTER));
        verify(jpaQuery, times(1))
            .where(BULK_PRINT_DATA.formAttribute.formType.in(List.of("5226A", "5226AC")));
        verify(jpaQuery, times(1))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED));
        verify(jpaQuery, times(1)).where(JUROR_POOL.pool.poolNumber.eq(request.getPoolNumber()));
        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.eq(true));
        verify(jpaQuery, times(1)).where(JUROR_POOL.owner.eq(SecurityUtil.BUREAU_OWNER));
        verify(jpaQuery, times(1)).orderBy(BULK_PRINT_DATA.creationDate.desc());
        verify(jpaQuery, times(1)).orderBy(JUROR.jurorNumber.asc());
        verify(jpaQuery, times(1)).fetch();
    }

    @Test
    void testFindByJurorNumberFormCodeDatePrinted() {
        JPAQuery<BulkPrintData> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectFrom(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetchOne()).thenReturn(BulkPrintData.builder().build());

        LocalDate datePrinted = LocalDate.now();

        Optional<BulkPrintData> bulkPrintData =
            reissueLetterRepositoryImpl.findByJurorNumberFormCodeDatePrinted(JUROR_NUMBER, FORM_CODE,
                datePrinted);

        assertThat(bulkPrintData).isNotNull();
        postverifyFindOne(jpaQuery, JUROR_NUMBER, FORM_CODE, BULK_PRINT_DATA.creationDate.eq(datePrinted));

    }

    @Test
    void testFindByJurorNumberFormCodeDatePrintedNotFound() {
        JPAQuery<BulkPrintData> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectFrom(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetchOne()).thenReturn(null);

        LocalDate datePrinted = LocalDate.now();

        Optional<BulkPrintData> bulkPrintData =
            reissueLetterRepositoryImpl.findByJurorNumberFormCodeDatePrinted(JUROR_NUMBER, FORM_CODE,
                datePrinted);

        assertThat(bulkPrintData).isEmpty();
        postverifyFindOne(jpaQuery, JUROR_NUMBER, FORM_CODE, BULK_PRINT_DATA.creationDate.eq(datePrinted));
    }

    @Test
    void testFindByJurorNumberFormCodeAndPending() {
        JPAQuery<BulkPrintData> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectFrom(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetchOne()).thenReturn(BulkPrintData.builder().build());

        Optional<BulkPrintData> bulkPrintData =
            reissueLetterRepositoryImpl.findByJurorNumberFormCodeAndPending(JUROR_NUMBER, FORM_CODE);

        assertThat(bulkPrintData).isNotNull();
        postverifyFindOne(jpaQuery, JUROR_NUMBER, FORM_CODE, BULK_PRINT_DATA.extractedFlag.isNull()
            .or(BULK_PRINT_DATA.extractedFlag.eq(false)));
    }

    @Test
    void testFindByJurorNumberFormCodeAndPendingNotFound() {
        JPAQuery<BulkPrintData> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectFrom(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetchOne()).thenReturn(null);

        Optional<BulkPrintData> bulkPrintData =
            reissueLetterRepositoryImpl.findByJurorNumberFormCodeAndPending(JUROR_NUMBER, FORM_CODE);

        assertThat(bulkPrintData).isEmpty();
        postverifyFindOne(jpaQuery, JUROR_NUMBER, FORM_CODE, BULK_PRINT_DATA.extractedFlag.isNull()
            .or(BULK_PRINT_DATA.extractedFlag.eq(false)));
    }

    private void postverifyFindOne(JPAQuery<BulkPrintData> jpaQuery, String jurorNumber, String formCode,
                                   BooleanExpression extractedFlag) {
        verify(queryFactory, times(1))
            .selectFrom(BULK_PRINT_DATA);

        verify(jpaQuery, times(1)).where(BULK_PRINT_DATA.jurorNo.eq(jurorNumber));
        verify(jpaQuery, times(1)).where(BULK_PRINT_DATA.formAttribute.formType.eq(formCode));
        verify(jpaQuery, times(1)).where(extractedFlag);
    }

    @Test
    void positiveSummonsRemindersLetterProcessedJurorNameFilter() {
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectDistinct(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.leftJoin(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);

        Tuple result1 = mock(Tuple.class);
        Tuple result2 = mock(Tuple.class);
        Tuple result3 = mock(Tuple.class);
        when(jpaQuery.fetch()).thenReturn(List.of(result1, result2, result3));

        ReissueLetterListRequestDto request = ReissueLetterListRequestDto.builder()
            .letterType(LetterType.SUMMONED_REMINDER)
            .jurorName("John Doe")
            .build();

        Consumer<JPAQuery<Tuple>> queryConsumer = tupleJPAQuery ->
            tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)
                .and(QJuror.juror.responded.eq(false)));

        List<Tuple> tuples = reissueLetterRepositoryImpl.findLetters(request, queryConsumer);

        assertThat(tuples).isNotNull();
        assertThat(tuples.size()).isEqualTo(3);

        verify(queryFactory, times(1))
            .selectDistinct(
                JUROR.jurorNumber.as("juror_number"),
                JUROR.firstName.as("first_name"),
                JUROR.lastName.as("last_name"),
                JUROR.postcode.as("postcode"),
                BULK_PRINT_DATA.creationDate.as("date_printed"),
                BULK_PRINT_DATA.extractedFlag.as("extracted_flag"),
                BULK_PRINT_DATA.formAttribute.formType.as("form_code")
            );
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        verify(jpaQuery, times(0)).join(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).leftJoin(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(BULK_PRINT_DATA.jurorNo)
            .and(BULK_PRINT_DATA.formAttribute.formType
                .in(List.of("5228", "5228C"))));
        verify(jpaQuery, times(1))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)
            .and(QJuror.juror.responded.eq(false)));
        verify(jpaQuery, times(1))
            .where(QJuror.juror.firstName.concat(" ").concat(QJuror.juror.lastName).toLowerCase()
            .likeIgnoreCase("%john doe%"));
        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.eq(true));
        verify(jpaQuery, times(1)).where(JUROR_POOL.owner.eq(SecurityUtil.BUREAU_OWNER));
        verify(jpaQuery, times(1)).orderBy(BULK_PRINT_DATA.creationDate.desc());
        verify(jpaQuery, times(1)).orderBy(JUROR.jurorNumber.asc());
        verify(jpaQuery, times(1)).fetch();
    }

    @Test
    void positiveSummonsRemindersLetterProcessedPostcodeFilter() {
        JPAQuery<Tuple> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectDistinct(any(Expression[].class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.join(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.leftJoin(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);

        Tuple result1 = mock(Tuple.class);
        Tuple result2 = mock(Tuple.class);
        Tuple result3 = mock(Tuple.class);
        when(jpaQuery.fetch()).thenReturn(List.of(result1, result2, result3));

        ReissueLetterListRequestDto request = ReissueLetterListRequestDto.builder()
            .letterType(LetterType.SUMMONED_REMINDER)
            .jurorPostcode("TS1 1ST")
            .build();

        Consumer<JPAQuery<Tuple>> queryConsumer =
            tupleJPAQuery -> tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)
                .and(QJuror.juror.responded.eq(false)));

        List<Tuple> tuples = reissueLetterRepositoryImpl.findLetters(request, queryConsumer);

        assertThat(tuples).isNotNull();
        assertThat(tuples.size()).isEqualTo(3);

        verify(queryFactory, times(1))
            .selectDistinct(
                JUROR.jurorNumber.as("juror_number"),
                JUROR.firstName.as("first_name"),
                JUROR.lastName.as("last_name"),
                JUROR.postcode.as("postcode"),
                BULK_PRINT_DATA.creationDate.as("date_printed"),
                BULK_PRINT_DATA.extractedFlag.as("extracted_flag"),
                BULK_PRINT_DATA.formAttribute.formType.as("form_code")
            );
        verify(jpaQuery, times(1)).from(JUROR);
        verify(jpaQuery, times(1)).join(JUROR_POOL);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        verify(jpaQuery, times(0)).join(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).leftJoin(BULK_PRINT_DATA);
        verify(jpaQuery, times(1)).on(JUROR.jurorNumber.eq(BULK_PRINT_DATA.jurorNo)
            .and(BULK_PRINT_DATA.formAttribute.formType
                .in(List.of("5228", "5228C"))));
        verify(jpaQuery, times(1)).where(QJurorPool.jurorPool.status.status
            .eq(IJurorStatus.SUMMONED)
            .and(QJuror.juror.responded.eq(false)));
        verify(jpaQuery, times(1)).where(QJuror.juror.postcode
            .eq("TS1 1ST"));
        verify(jpaQuery, times(1)).where(JUROR_POOL.isActive.eq(true));
        verify(jpaQuery, times(1)).where(JUROR_POOL.owner.eq(SecurityUtil.BUREAU_OWNER));
        verify(jpaQuery, times(1)).orderBy(BULK_PRINT_DATA.creationDate.desc());
        verify(jpaQuery, times(1)).orderBy(JUROR.jurorNumber.asc());
        verify(jpaQuery, times(1)).fetch();
    }

}
