package uk.gov.hmcts.juror.api.moj.repository.letter;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QWelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"unchecked", "PMD.ExcessiveImports", "PMD.TooManyMethods"})
class CourtPrintLetterRepositoryImplTest {

    private static final String OWNER = TestConstants.VALID_COURT_LOCATION;
    private static final String JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER;
    private JPAQuery<Tuple> jpaQuery;
    private JPAQueryFactory queryFactory;
    private MockedConstruction<JPAQueryFactory> queryFactories;

    private static final QJurorPool POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPoolRequest REQUEST = QPoolRequest.poolRequest;
    private static final QCourtLocation COURT = QCourtLocation.courtLocation;
    private static final QWelshCourtLocation WELSH = QWelshCourtLocation.welshCourtLocation;
    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final QPanel PANEL = QPanel.panel;
    private static final String TRIAL_NUMBER = TestConstants.VALID_TRIAL_NUMBER;

    private final CourtPrintLetterRepositoryImpl repository = new CourtPrintLetterRepositoryImpl();

    @BeforeEach
    void beforeEach() {
        jpaQuery = Mockito.mock(JPAQuery.class);
        Mockito.when(jpaQuery.from(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.join(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.leftJoin(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.on(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.where(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.orderBy(Mockito.any(OrderSpecifier.class))).thenReturn(jpaQuery);
        queryFactories = Mockito.mockConstruction(JPAQueryFactory.class, (factory, context) -> {
            queryFactory = factory;
            Mockito.when(factory.select(Mockito.any(Expression[].class))).thenReturn(jpaQuery);
        });
    }

    @AfterEach
    void afterEach() {
        queryFactories.close();
    }

    @ParameterizedTest
    @MethodSource("letterTypesAndLanguages")
    void retrievesLatestPrintDataForEveryLetterTypeAndLanguage(CourtLetterType type, boolean welsh) {
        Tuple row = Mockito.mock(Tuple.class);
        Mockito.when(jpaQuery.fetchFirst()).thenReturn(row);

        assertThat(repository.retrievePrintInformation(JUROR_NUMBER,
            type, welsh, OWNER, TRIAL_NUMBER)).isSameAs(row);

        Mockito.verify(jpaQuery).fetchFirst();
        Mockito.verify(jpaQuery, Mockito.never()).fetchOne();
        Mockito.verify(queryFactory).select(expectedProjection(type, welsh).toArray(new Expression<?>[0]));
        verifyLetterFilters(type);
        Mockito.verify(jpaQuery).where(POOL.juror.jurorNumber.eq(JUROR_NUMBER));
        Mockito.verify(jpaQuery).where(POOL.owner.eq(OWNER));
        verifyLetterOrder(type);
        Mockito.verify(jpaQuery).join(JUROR);
        Mockito.verify(jpaQuery).on(JUROR.eq(POOL.juror));
        Mockito.verify(jpaQuery, Mockito.times(type == CourtLetterType.POSTPONED ? 2 : 1)).join(REQUEST);
        Mockito.verify(jpaQuery, Mockito.times(type == CourtLetterType.POSTPONED ? 2 : 1))
            .on(REQUEST.eq(POOL.pool));
        Mockito.verify(jpaQuery).join(COURT);
        Mockito.verify(jpaQuery).on(COURT.eq(REQUEST.courtLocation));
        if (welsh) {
            Mockito.verify(jpaQuery).join(WELSH);
            Mockito.verify(jpaQuery).on(WELSH.locCode.eq(REQUEST.courtLocation.locCode));
        } else {
            Mockito.verify(jpaQuery, Mockito.never()).join(WELSH);
        }
        verifyLetterSpecificJoins(type);
    }

    @ParameterizedTest
    @EnumSource(value = CourtLetterType.class, names = "CERTIFICATE_OF_EXEMPTION", mode = EnumSource.Mode.EXCLUDE)
    void dateSpecificLookupAddsAttendanceDateOnlyForAbsenceLetters(CourtLetterType type) {
        LocalDate date = LocalDate.of(2026, 10, 12);
        Tuple row = Mockito.mock(Tuple.class);
        Mockito.when(jpaQuery.fetchOne()).thenReturn(row);

        assertThat(repository.retrievePrintInformationBasedOnLetterSpecificDate(
            JUROR_NUMBER, type, false, OWNER, date))
            .isSameAs(row);

        verifyLetterFilters(type);
        if (type == CourtLetterType.SHOW_CAUSE || type == CourtLetterType.FAILED_TO_ATTEND) {
            Mockito.verify(jpaQuery).where(APPEARANCE.attendanceDate.eq(date));
        } else {
            Mockito.verify(jpaQuery, Mockito.never()).where(APPEARANCE.attendanceDate.eq(date));
        }
        Mockito.verify(jpaQuery).where(POOL.juror.jurorNumber.eq(JUROR_NUMBER));
        Mockito.verify(jpaQuery).where(POOL.owner.eq(OWNER));
        Mockito.verify(jpaQuery).fetchOne();
        Mockito.verify(jpaQuery, Mockito.never()).fetchFirst();
    }

    @Test
    void overloadWithoutTrialNumberRetrievesLatestLetter() {
        Mockito.when(jpaQuery.fetchFirst()).thenReturn(null);

        assertThat(repository.retrievePrintInformation(JUROR_NUMBER,
            CourtLetterType.DEFERRAL_GRANTED, false, OWNER)).isNull();

        verifyLetterFilters(CourtLetterType.DEFERRAL_GRANTED);
        Mockito.verify(jpaQuery).where(POOL.juror.jurorNumber.eq(JUROR_NUMBER));
        Mockito.verify(jpaQuery).where(POOL.owner.eq(OWNER));
        Mockito.verify(jpaQuery).fetchFirst();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void returnsNullWhenNoPrintDataMatches(boolean dateSpecific) {
        Mockito.when(jpaQuery.fetchFirst()).thenReturn(null);
        Mockito.when(jpaQuery.fetchOne()).thenReturn(null);

        Tuple result = dateSpecific ? repository.retrievePrintInformationBasedOnLetterSpecificDate(
            JUROR_NUMBER, CourtLetterType.SHOW_CAUSE, true,
            OWNER, LocalDate.of(2026, 10, 12))
            : repository.retrievePrintInformation(JUROR_NUMBER,
                CourtLetterType.SHOW_CAUSE, true, OWNER);

        assertThat(result).isNull();
    }

    private static Stream<Arguments> letterTypesAndLanguages() {
        return Arrays.stream(CourtLetterType.values()).flatMap(type ->
            Stream.of(Arguments.of(type, false), Arguments.of(type, true)));
    }

    // Enumerate the expected data for every letter type; Checkstyle requires the defensive default.
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExhaustiveSwitchHasDefault"})
    private void verifyLetterFilters(CourtLetterType type) {
        switch (type) {
            case DEFERRAL_GRANTED, POSTPONED -> {
                if (type == CourtLetterType.POSTPONED) {
                    Mockito.verify(jpaQuery).where(POOL.deferralCode.eq(ExcusalCodeEnum.P.getCode()));
                }
                Mockito.verify(jpaQuery).where(POOL.status.status.eq(IJurorStatus.DEFERRED));
            }
            case DEFERRAL_REFUSED, EXCUSAL_REFUSED -> Mockito.verify(jpaQuery).where(POOL.isActive.eq(true));
            case EXCUSAL_GRANTED -> Mockito.verify(jpaQuery).where(POOL.status.status.eq(IJurorStatus.EXCUSED));
            case WITHDRAWAL -> Mockito.verify(jpaQuery).where(POOL.status.status.eq(IJurorStatus.DISQUALIFIED));
            case SHOW_CAUSE, FAILED_TO_ATTEND -> {
                Mockito.verify(jpaQuery).where(APPEARANCE.noShow.isTrue());
                Mockito.verify(jpaQuery).where(APPEARANCE.attendanceType.eq(AttendanceType.ABSENT));
            }
            case CERTIFICATE_OF_EXEMPTION -> Mockito.verify(jpaQuery).where(PANEL.trial.trialNumber.eq(TRIAL_NUMBER));
            case CERTIFICATE_OF_ATTENDANCE -> {
                Mockito.verify(jpaQuery).where(APPEARANCE.noShow.isFalse().or(APPEARANCE.noShow.isNull()));
                Mockito.verify(jpaQuery).where(APPEARANCE.attendanceType.ne(AttendanceType.ABSENT));
            }
            default -> throw new IllegalArgumentException("Unexpected letter type: " + type);
        }
    }

    private void verifyLetterSpecificJoins(CourtLetterType type) {
        switch (type) {
            case SHOW_CAUSE, FAILED_TO_ATTEND -> {
                Mockito.verify(jpaQuery).leftJoin(APPEARANCE);
                Mockito.verify(jpaQuery).on(POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber)
                    .and(POOL.pool.poolNumber.eq(APPEARANCE.poolNumber)));
            }
            case CERTIFICATE_OF_ATTENDANCE -> {
                Mockito.verify(jpaQuery).join(APPEARANCE);
                Mockito.verify(jpaQuery).on(JUROR.jurorNumber.eq(APPEARANCE.jurorNumber)
                    .and(COURT.eq(APPEARANCE.courtLocation)));
            }
            case CERTIFICATE_OF_EXEMPTION -> {
                Mockito.verify(jpaQuery).join(PANEL);
                Mockito.verify(jpaQuery).on(PANEL.juror.eq(JUROR)
                    .and(PANEL.trial.courtLocation.eq(REQUEST.courtLocation)));
            }
            default -> {
                Mockito.verify(jpaQuery, Mockito.never()).join(APPEARANCE);
                Mockito.verify(jpaQuery, Mockito.never()).leftJoin(APPEARANCE);
                Mockito.verify(jpaQuery, Mockito.never()).join(PANEL);
            }
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExhaustiveSwitchHasDefault"})
    private void verifyLetterOrder(CourtLetterType type) {
        switch (type) {
            case DEFERRAL_GRANTED, POSTPONED -> Mockito.verify(jpaQuery, Mockito.times(2))
                .orderBy(POOL.deferralDate.desc());
            case EXCUSAL_GRANTED -> Mockito.verify(jpaQuery, Mockito.times(2)).orderBy(POOL.juror.excusalDate.desc());
            case WITHDRAWAL -> Mockito.verify(jpaQuery, Mockito.times(2)).orderBy(POOL.juror.disqualifyDate.desc());
            case SHOW_CAUSE, FAILED_TO_ATTEND -> Mockito.verify(jpaQuery, Mockito.times(2))
                .orderBy(APPEARANCE.attendanceDate.desc());
            case CERTIFICATE_OF_EXEMPTION -> Mockito.verify(jpaQuery, Mockito.times(2))
                .orderBy(PANEL.trial.trialNumber.desc());
            case CERTIFICATE_OF_ATTENDANCE -> Mockito.verify(jpaQuery).orderBy(APPEARANCE.attendanceDate.desc());
            case DEFERRAL_REFUSED, EXCUSAL_REFUSED -> Mockito.verify(jpaQuery, Mockito.never())
                .orderBy(Mockito.any(OrderSpecifier.class));
            default -> throw new IllegalArgumentException("Unexpected letter type: " + type);
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExhaustiveSwitchHasDefault"})
    private List<Expression<?>> expectedProjection(CourtLetterType type, boolean welsh) {
        List<Expression<?>> expressions = new ArrayList<>();
        if (welsh) {
            expressions.addAll(List.of(WELSH.locCourtName, WELSH.address1, WELSH.address2, WELSH.address3,
                WELSH.address4, WELSH.address5, WELSH.address6));
        } else {
            expressions.addAll(List.of(COURT.locCode, COURT.name, COURT.address1, COURT.address2, COURT.address3,
                COURT.address4, COURT.address5, COURT.address6));
        }
        expressions.addAll(List.of(COURT.locPhone, REQUEST.attendTime, COURT.signatory, COURT.locCode,
            COURT.postcode, COURT.courtAttendTime, POOL.juror.firstName, POOL.juror.lastName,
            POOL.juror.addressLine1, POOL.juror.addressLine2, POOL.juror.addressLine3, POOL.juror.addressLine4,
            POOL.juror.addressLine5, POOL.juror.postcode, POOL.juror.jurorNumber,
            REQUEST.poolNumber, REQUEST.attendTime));
        switch (type) {
            case DEFERRAL_GRANTED, POSTPONED -> expressions.addAll(List.of(POOL.deferralDate, POOL.deferralCode));
            case EXCUSAL_GRANTED -> expressions.addAll(List.of(JUROR.excusalCode, JUROR.excusalDate));
            case EXCUSAL_REFUSED -> expressions.addAll(List.of(POOL.juror.excusalCode, POOL.juror.excusalDate));
            case WITHDRAWAL -> expressions.addAll(List.of(JUROR.disqualifyDate, JUROR.disqualifyCode));
            case SHOW_CAUSE -> expressions.addAll(List.of(APPEARANCE.attendanceDate, APPEARANCE.noShow));
            case FAILED_TO_ATTEND -> expressions.add(APPEARANCE.attendanceDate);
            case CERTIFICATE_OF_EXEMPTION -> expressions.addAll(
                List.of(QTrial.trial.judge.name, QTrial.trial.description));
            case DEFERRAL_REFUSED, CERTIFICATE_OF_ATTENDANCE -> {
            }
            default -> throw new IllegalArgumentException("Unexpected letter type: " + type);
        }
        return expressions;
    }
}
