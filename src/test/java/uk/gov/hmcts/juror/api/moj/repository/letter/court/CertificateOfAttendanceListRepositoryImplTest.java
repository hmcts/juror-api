package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.CertificateOfAttendanceLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"unchecked", "PMD.ExcessiveImports"})
class CertificateOfAttendanceListRepositoryImplTest {

    private static final String OWNER = TestConstants.VALID_COURT_LOCATION;
    private static final String JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER;
    private static final String POSTCODE = TestConstants.VALID_POSTCODE;
    private static final String POOL_NUMBER = TestConstants.VALID_POOL_NUMBER;

    private JPAQuery<Tuple> jpaQuery;
    private JPAQueryFactory queryFactory;
    private MockedConstruction<JPAQueryFactory> queryFactories;
    private JPAQuery<Appearance> appearanceQuery;

    private static final QJurorPool POOL = QJurorPool.jurorPool;
    private static final QJurorHistory HISTORY = QJurorHistory.jurorHistory;
    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final Expression<?>[] PROJECTION = {
        POOL.owner, POOL.pool.poolNumber, POOL.juror.jurorNumber, POOL.juror.firstName, POOL.juror.lastName,
        POOL.juror.completionDate, POOL.pool.returnDate, HISTORY.dateCreated,
        POOL.juror.postcode, POOL.isActive, POOL.status
    };

    private final CertificateOfAttendanceListRepositoryImpl repository =
        new CertificateOfAttendanceListRepositoryImpl();

    @BeforeEach
    void beforeEach() {
        jpaQuery = Mockito.mock(JPAQuery.class);
        appearanceQuery = Mockito.mock(JPAQuery.class);
        Mockito.when(jpaQuery.from(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.leftJoin(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.on(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.where(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.where(Mockito.any(Predicate[].class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.orderBy(Mockito.any(OrderSpecifier.class))).thenReturn(jpaQuery);
        Mockito.when(appearanceQuery.where(Mockito.any(Predicate.class))).thenReturn(appearanceQuery);
        queryFactories = Mockito.mockConstruction(JPAQueryFactory.class, (factory, context) -> {
            queryFactory = factory;
            Mockito.when(factory.select(Mockito.any(Expression[].class))).thenReturn(jpaQuery);
            Mockito.when(factory.selectFrom(APPEARANCE)).thenReturn(appearanceQuery);
        });
    }

    @AfterEach
    void afterEach() {
        queryFactories.close();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void restrictsCertificatesToActiveCourtJurorsWithAttendance(boolean includePrinted) {
        final InOrder order = Mockito.inOrder(jpaQuery);
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());

        assertThat(repository.findJurorsEligibleForCertificateOfAcceptanceLetter(
            CourtLetterSearchCriteria.builder().includePrinted(includePrinted).build(),
            OWNER)).isEmpty();

        Mockito.verify(jpaQuery).where(POOL.owner.ne(SecurityUtil.BUREAU_OWNER),
            POOL.isActive.isTrue(),
            attendanceExists());
        Mockito.verify(jpaQuery).where(POOL.pool.courtLocation.locCode.eq(OWNER));
        if (!includePrinted) {
            Mockito.verify(jpaQuery).where(HISTORY.dateCreated.isNull());
        }
        Mockito.verify(jpaQuery, Mockito.times(includePrinted ? 1 : 2)).where(Mockito.any(Predicate.class));
        Mockito.verify(queryFactory).select(PROJECTION);
        Mockito.verify(jpaQuery).leftJoin(HISTORY);
        Mockito.verify(jpaQuery).on(HISTORY.jurorNumber.eq(POOL.juror.jurorNumber)
                .and(HISTORY.historyCode.eq(HistoryCodeMod.CERTIFICATE_OF_RECOGNITION)));
        if (includePrinted) {
            order.verify(jpaQuery).orderBy(
                new CaseBuilder().when(HISTORY.dateCreated.isNull()).then(0).otherwise(1).asc());
            order.verify(jpaQuery).orderBy(HISTORY.dateCreated.desc());
            order.verify(jpaQuery).orderBy(POOL.juror.jurorNumber.asc());
            Mockito.verify(jpaQuery, Mockito.times(3)).orderBy(Mockito.any(OrderSpecifier.class));
        } else {
            order.verify(jpaQuery).orderBy(HISTORY.dateCreated.desc());
            order.verify(jpaQuery).orderBy(POOL.juror.jurorNumber.asc());
            Mockito.verify(jpaQuery, Mockito.times(2)).orderBy(Mockito.any(OrderSpecifier.class));
        }
    }

    @Test
    void appliesSearchFiltersIncludingCaseInsensitivePostcode() {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());

        repository.findJurorsEligibleForCertificateOfAcceptanceLetter(CourtLetterSearchCriteria.builder()
            .jurorNumber("1234").jurorName("John Doe").postcode(" te1 4st ").poolNumber("123").build(),
            OWNER);

        Mockito.verify(jpaQuery).where(POOL.owner.ne(SecurityUtil.BUREAU_OWNER),
            POOL.isActive.isTrue(),
            attendanceExists());
        Mockito.verify(jpaQuery).where(POOL.pool.courtLocation.locCode.eq(OWNER));
        Mockito.verify(jpaQuery).where(POOL.juror.jurorNumber.startsWith("1234"));
        Mockito.verify(jpaQuery).where(
            POOL.juror.firstName.concat(" ").concat(POOL.juror.lastName).containsIgnoreCase("John Doe"));
        Mockito.verify(jpaQuery).where(POOL.juror.postcode.trim().equalsIgnoreCase("te1 4st"));
        Mockito.verify(jpaQuery).where(POOL.pool.poolNumber.startsWith("123"));
        Mockito.verify(jpaQuery).where(HISTORY.dateCreated.isNull());
        Mockito.verify(jpaQuery, Mockito.times(6)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void ignoresAbsentFilters(String value) {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());

        repository.findJurorsEligibleForCertificateOfAcceptanceLetter(
            new CourtLetterSearchCriteria(value, value, value, value, false), OWNER);

        Mockito.verify(jpaQuery).where(POOL.owner.ne(SecurityUtil.BUREAU_OWNER),
            POOL.isActive.isTrue(),
            attendanceExists());
        Mockito.verify(jpaQuery).where(POOL.pool.courtLocation.locCode.eq(OWNER));
        Mockito.verify(jpaQuery).where(HISTORY.dateCreated.isNull());
        Mockito.verify(jpaQuery, Mockito.times(2)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {false, true})
    void mapsCertificateFieldsAndHandlesNullableActiveFlag(Boolean active) {
        LocalDate start = LocalDate.of(2026, 10, 5);
        LocalDate completion = start.plusDays(10);
        final LocalDateTime printed = completion.atTime(10, 30);
        JurorStatus status = new JurorStatus();
        status.setStatus(IJurorStatus.COMPLETED);
        Tuple tuple = Mockito.mock(Tuple.class);
        Mockito.when(tuple.get(POOL.owner)).thenReturn(OWNER);
        Mockito.when(tuple.get(POOL.pool.poolNumber)).thenReturn(POOL_NUMBER);
        Mockito.when(tuple.get(POOL.juror.jurorNumber)).thenReturn(JUROR_NUMBER);
        Mockito.when(tuple.get(POOL.juror.firstName)).thenReturn("John");
        Mockito.when(tuple.get(POOL.juror.lastName)).thenReturn("Doe");
        Mockito.when(tuple.get(POOL.juror.completionDate)).thenReturn(completion);
        Mockito.when(tuple.get(POOL.pool.returnDate)).thenReturn(start);
        Mockito.when(tuple.get(HISTORY.dateCreated)).thenReturn(printed);
        Mockito.when(tuple.get(POOL.juror.postcode)).thenReturn(POSTCODE);
        Mockito.when(tuple.get(POOL.isActive)).thenReturn(active);
        Mockito.when(tuple.get(POOL.status)).thenReturn(status);

        Mockito.when(jpaQuery.fetch()).thenReturn(List.of(tuple));

        List<CertificateOfAttendanceLetterList> results =
            repository.findJurorsEligibleForCertificateOfAcceptanceLetter(
                CourtLetterSearchCriteria.builder().includePrinted(true).build(),
                OWNER);

        assertThat(results).singleElement().satisfies(letter -> {
            assertThat(letter.getOwner()).isEqualTo(OWNER);
            assertThat(letter.getPoolNumber()).isEqualTo(POOL_NUMBER);
            assertThat(letter.getJurorNumber()).isEqualTo(JUROR_NUMBER);
            assertThat(letter.getFirstName()).isEqualTo("John");
            assertThat(letter.getLastName()).isEqualTo("Doe");
            assertThat(letter.getPostcode()).isEqualTo(POSTCODE);
            assertThat(letter.getStatus()).isEqualTo(String.valueOf(IJurorStatus.COMPLETED));
            assertThat(letter.getStartDate()).isEqualTo(start);
            assertThat(letter.getCompletionDate()).isEqualTo(completion);
            assertThat(letter.getDatePrinted()).isEqualTo(printed);
            assertThat(letter.isActive()).isEqualTo(Boolean.TRUE.equals(active));
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void retrievesAttendancesForJurorAndLocationExcludingAbsences(boolean empty) {
        Appearance appearance = new Appearance();
        List<Appearance> rows = empty ? List.of() : List.of(appearance);
        Mockito.when(appearanceQuery.fetch()).thenReturn(rows);

        assertThat(repository.getAttendances(OWNER,
            JUROR_NUMBER)).isEqualTo(rows);

        Mockito.verify(queryFactory).selectFrom(APPEARANCE);
        Mockito.verify(appearanceQuery).where(APPEARANCE.jurorNumber.eq(JUROR_NUMBER)
                .and(APPEARANCE.locCode.eq(OWNER)).and(attendanceFilter()));
        Mockito.verify(appearanceQuery, Mockito.times(1)).where(Mockito.any(Predicate.class));
    }

    private BooleanExpression attendanceExists() {
        return JPAExpressions.selectOne().from(APPEARANCE).where(
            APPEARANCE.jurorNumber.eq(POOL.juror.jurorNumber)
                .and(APPEARANCE.locCode.eq(OWNER)).and(attendanceFilter())).exists();
    }

    private BooleanExpression attendanceFilter() {
        return APPEARANCE.attendanceType.ne(AttendanceType.ABSENT)
            .and(APPEARANCE.noShow.isNull().or(APPEARANCE.noShow.isFalse()));
    }
}
