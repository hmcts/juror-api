package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.BankHolidayDate;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.Address;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;
import uk.gov.hmcts.juror.api.moj.domain.system.HasEnabled;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@DisplayName("AdministrationServiceImpl")
@SuppressWarnings("unchecked")
class AdministrationServiceImplTest {

    private AdministrationServiceImpl administrationService;

    private CourtLocationRepository courtLocationRepository;
    private WelshCourtLocationRepository welshCourtLocationRepository;

    private CourtroomRepository courtroomRepository;

    private HolidaysRepository holidaysRepository;

    @BeforeEach
    void beforeEach() {
        courtLocationRepository = mock(CourtLocationRepository.class);
        welshCourtLocationRepository = mock(WelshCourtLocationRepository.class);
        courtroomRepository = mock(CourtroomRepository.class);
        holidaysRepository = mock(HolidaysRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        this.administrationService =
            spy(new AdministrationServiceImpl(entityManager, courtLocationRepository, welshCourtLocationRepository,
                courtroomRepository, holidaysRepository));
    }

    @Nested
    @DisplayName("public List<CodeDescriptionResponse> viewCodeAndDescriptions(CodeType codeType)")
    @SuppressWarnings("PMD.LinguisticNaming")
    class ViewCodeAndDescriptions {

        @Test
        void positiveHasValues() {
            HasCodeAndDescription<Integer> hasCodeAndDescription1 = mockHasCodeAndDescription(1, "desc1");
            HasCodeAndDescription<Integer> hasCodeAndDescription2 = mockHasCodeAndDescription(2, "desc2");
            HasCodeAndDescription<Integer> hasCodeAndDescription3 = mockHasCodeAndDescription(3, "desc3");

            HasCodeAndDescription<?>[] values =
                List.of(hasCodeAndDescription1, hasCodeAndDescription3, hasCodeAndDescription2)
                    .toArray(new HasCodeAndDescription[0]);
            CodeType codeType = mock(CodeType.class);
            doReturn(values).when(codeType).getValues();

            assertThat(administrationService.viewCodeAndDescriptions(codeType))
                .containsExactly(
                    new CodeDescriptionResponse(hasCodeAndDescription1),
                    new CodeDescriptionResponse(hasCodeAndDescription2),
                    new CodeDescriptionResponse(hasCodeAndDescription3));
        }

        @Test
        void positiveNoValuesUseQuery() {
            HasCodeAndDescription<Integer> hasCodeAndDescription1 = mockHasCodeAndDescription(1, "desc1");
            HasCodeAndDescription<Integer> hasCodeAndDescription2 = mockHasCodeAndDescription(2, "desc2");
            HasCodeAndDescription<Integer> hasCodeAndDescription3 = mockHasCodeAndDescription(3, "desc3");


            EntityPathBase<HasCodeAndDescription<Integer>> entityPathBase = mock(EntityPathBase.class);
            CodeType codeType = mock(CodeType.class);
            doReturn(null).when(codeType).getValues();
            doReturn(entityPathBase).when(codeType).getEntityPathBase();

            JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);
            doReturn(queryFactory).when(administrationService).getJpaQueryFactory();

            JPAQuery<HasCodeAndDescription<Integer>> jpaQuery =
                mock(JPAQuery.class, withSettings().defaultAnswer(RETURNS_SELF));
            doReturn(jpaQuery).when(queryFactory).select(entityPathBase);
            doReturn(List.of(hasCodeAndDescription1, hasCodeAndDescription3, hasCodeAndDescription2))
                .when(jpaQuery).fetch();


            assertThat(administrationService.viewCodeAndDescriptions(codeType))
                .containsExactly(
                    new CodeDescriptionResponse(hasCodeAndDescription1),
                    new CodeDescriptionResponse(hasCodeAndDescription2),
                    new CodeDescriptionResponse(hasCodeAndDescription3));

            verify(administrationService, times(1)).getJpaQueryFactory();
            verify(queryFactory, times(1)).select(entityPathBase);
            verify(jpaQuery, times(1)).from(entityPathBase);
            verify(jpaQuery, times(1)).fetch();
            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveNoValuesUseQuerySomeDisabled() {
            HasCodeAndDescription<Integer> hasCodeAndDescription1 = mockHasCodeAndDescription(1, "desc1", true);
            HasCodeAndDescription<Integer> hasCodeAndDescription2 = mockHasCodeAndDescription(2, "desc2", false);
            HasCodeAndDescription<Integer> hasCodeAndDescription3 = mockHasCodeAndDescription(3, "desc3", true);


            EntityPathBase<HasCodeAndDescription<Integer>> entityPathBase = mock(EntityPathBase.class);
            CodeType codeType = mock(CodeType.class);
            doReturn(null).when(codeType).getValues();
            doReturn(entityPathBase).when(codeType).getEntityPathBase();

            JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);
            doReturn(queryFactory).when(administrationService).getJpaQueryFactory();

            JPAQuery<HasCodeAndDescription<Integer>> jpaQuery =
                mock(JPAQuery.class, withSettings().defaultAnswer(RETURNS_SELF));
            doReturn(jpaQuery).when(queryFactory).select(entityPathBase);
            doReturn(List.of(hasCodeAndDescription1, hasCodeAndDescription3, hasCodeAndDescription2))
                .when(jpaQuery).fetch();


            assertThat(administrationService.viewCodeAndDescriptions(codeType))
                .as("Only enabled values should be returned")
                .containsExactly(
                    new CodeDescriptionResponse(hasCodeAndDescription1),
                    new CodeDescriptionResponse(hasCodeAndDescription3));

            verify(administrationService, times(1)).getJpaQueryFactory();
            verify(queryFactory, times(1)).select(entityPathBase);
            verify(jpaQuery, times(1)).from(entityPathBase);
            verify(jpaQuery, times(1)).fetch();
            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }


        <T extends Comparable<T>> HasCodeAndDescription<T> mockHasCodeAndDescription(T code, String desc) {
            return spy(new HasCodeAndDescription<T>() {
                @Override
                public T getCode() {
                    return code;
                }

                @Override
                public String getDescription() {
                    return desc;
                }
            });
        }

        <T extends Comparable<T>> HasCodeAndDescriptionWithEnabled<T> mockHasCodeAndDescription(T code,
                                                                                                String desc,
                                                                                                boolean enabled) {
            return spy(new HasCodeAndDescriptionWithEnabled<T>() {

                @Override
                public boolean isEnabled() {
                    return enabled;
                }

                @Override
                public T getCode() {
                    return code;
                }

                @Override
                public String getDescription() {
                    return desc;
                }
            });
        }

        interface HasCodeAndDescriptionWithEnabled<T extends Comparable<T>> extends HasCodeAndDescription<T>,
            HasEnabled {
        }
    }

    @Nested
    @DisplayName("public CourtDetailsDto viewCourt(String locCode)")
    class ViewCourt {

        @Test
        void positiveEnglish() {
            when(courtLocationRepository.findById(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.of(
                    CourtLocation.builder()
                        .locCode(TestConstants.VALID_COURT_LOCATION)
                        .name("COURT1")
                        .address1("COURT1 ADDRESS1")
                        .address2("COURT1 ADDRESS2")
                        .address3("COURT1 ADDRESS3")
                        .address4("COURT1 ADDRESS4")
                        .address5("COURT1 ADDRESS5")
                        .postcode("AB1 2CD")
                        .locPhone("0123456789")
                        .courtAttendTime(LocalTime.of(9, 0))
                        .costCentre("CSTCNR1")
                        .signatory("COURT1 SIGNATURE")
                        .assemblyRoom(
                            Courtroom.builder()
                                .roomNumber("ROOM1")
                                .build()
                        )
                        .build()
                ));
            when(welshCourtLocationRepository.findById(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.empty());

            assertThat(administrationService.viewCourt(TestConstants.VALID_COURT_LOCATION))
                .isEqualTo(CourtDetailsDto.builder()
                    .isWelsh(false)
                    .courtCode(TestConstants.VALID_COURT_LOCATION)
                    .englishCourtName("COURT1")
                    .englishAddress(
                        Address.builder()
                            .addressLine1("COURT1 ADDRESS1")
                            .addressLine2("COURT1 ADDRESS2")
                            .addressLine3("COURT1 ADDRESS3")
                            .addressLine4("COURT1 ADDRESS4")
                            .addressLine5("COURT1 ADDRESS5")
                            .postcode("AB1 2CD")
                            .build())
                    .welshCourtName(null)
                    .welshAddress(null)
                    .mainPhone("0123456789")
                    .attendanceTime(LocalTime.of(9, 0))
                    .costCentre("CSTCNR1")
                    .signature("COURT1 SIGNATURE")
                    .assemblyRoom("ROOM1")
                    .build());
            verify(courtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(courtLocationRepository);
            verify(welshCourtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(welshCourtLocationRepository);
        }

        @Test
        void positiveWelsh() {
            when(courtLocationRepository.findById(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.of(
                    CourtLocation.builder()
                        .locCode(TestConstants.VALID_COURT_LOCATION)
                        .name("COURT1")
                        .address1("COURT1 ADDRESS1")
                        .address2("COURT1 ADDRESS2")
                        .address3("COURT1 ADDRESS3")
                        .address4("COURT1 ADDRESS4")
                        .address5("COURT1 ADDRESS5")
                        .postcode("AB1 2CD")
                        .locPhone("0123456789")
                        .courtAttendTime(LocalTime.of(9, 0))
                        .costCentre("CSTCNR1")
                        .signatory("COURT1 SIGNATURE")
                        .assemblyRoom(
                            Courtroom.builder()
                                .roomNumber("ROOM1")
                                .build()
                        )
                        .build()
                ));
            when(welshCourtLocationRepository.findById(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.of(
                    WelshCourtLocation.builder()
                        .locCourtName("WELSH_COURT1")
                        .address1("WELSH_COURT1 ADDRESS1")
                        .address2("WELSH_COURT1 ADDRESS2")
                        .address3("WELSH_COURT1 ADDRESS3")
                        .address4("WELSH_COURT1 ADDRESS4")
                        .address5("WELSH_COURT1 ADDRESS5")
                        .build()
                ));

            assertThat(administrationService.viewCourt(TestConstants.VALID_COURT_LOCATION))
                .isEqualTo(CourtDetailsDto.builder()
                    .isWelsh(true)
                    .courtCode(TestConstants.VALID_COURT_LOCATION)
                    .englishCourtName("COURT1")
                    .englishAddress(
                        Address.builder()
                            .addressLine1("COURT1 ADDRESS1")
                            .addressLine2("COURT1 ADDRESS2")
                            .addressLine3("COURT1 ADDRESS3")
                            .addressLine4("COURT1 ADDRESS4")
                            .addressLine5("COURT1 ADDRESS5")
                            .postcode("AB1 2CD")
                            .build())
                    .welshCourtName("WELSH_COURT1")
                    .welshAddress(
                        Address.builder()
                            .addressLine1("WELSH_COURT1 ADDRESS1")
                            .addressLine2("WELSH_COURT1 ADDRESS2")
                            .addressLine3("WELSH_COURT1 ADDRESS3")
                            .addressLine4("WELSH_COURT1 ADDRESS4")
                            .addressLine5("WELSH_COURT1 ADDRESS5")
                            .postcode(null)
                            .build())
                    .mainPhone("0123456789")
                    .attendanceTime(LocalTime.of(9, 0))
                    .costCentre("CSTCNR1")
                    .signature("COURT1 SIGNATURE")
                    .assemblyRoom("ROOM1")
                    .build());
            verify(courtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(courtLocationRepository);
            verify(welshCourtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(welshCourtLocationRepository);

        }

        @Test
        void negativeCourtNotFound() {

            when(courtLocationRepository.findById(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.empty());
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> administrationService.viewCourt(TestConstants.VALID_COURT_LOCATION),
                    "Expected exception to be thrown when a court is not found");
            assertThat(exception.getMessage()).isEqualTo("Court not found");
            assertThat(exception.getCause()).isNull();
            verify(courtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
        }
    }

    @Nested
    @DisplayName("List<Holidays> findAllPublicHolidays()")
    class FindAllPublicHolidays {

        @Test
        void positiveTypical() {
            List<Holidays> holidays = List.of(
                mock(Holidays.class),
                mock(Holidays.class),
                mock(Holidays.class)
            );
            when(holidaysRepository.findAllByPublicHolidayAndHolidayIsGreaterThanEqual(anyBoolean(), any()))
                .thenReturn(holidays);

            assertThat(administrationService.findAllPublicHolidays()).isEqualTo(holidays);
            LocalDate startOfYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
            verify(holidaysRepository, times(1))
                .findAllByPublicHolidayAndHolidayIsGreaterThanEqual(true, startOfYear);
            verifyNoMoreInteractions(holidaysRepository);
        }
    }

    @Nested
    @DisplayName("public Map<Integer, List<BankHolidayDate>> viewBankHolidays()")
    class ViewBankHolidays {

        private Holidays mockHoliday(LocalDate date, String description) {
            return Holidays.builder()
                .holiday(date)
                .description(description)
                .build();
        }

        @Test
        void positiveTypical() {
            List<Holidays> holidays = List.of(
                mockHoliday(LocalDate.of(2021, 1, 1), "desc1"),
                mockHoliday(LocalDate.of(2021, 2, 1), "desc2"),
                mockHoliday(LocalDate.of(2022, 1, 1), "desc3"),
                mockHoliday(LocalDate.of(2021, 3, 1), "desc4"),
                mockHoliday(LocalDate.of(2023, 1, 4), "desc5")
            );
            doReturn(holidays).when(administrationService).findAllPublicHolidays();

            assertThat(administrationService.viewBankHolidays()).isEqualTo(
                Map.of(
                    2021, List.of(
                        new BankHolidayDate(LocalDate.of(2021, 1, 1), "desc1"),
                        new BankHolidayDate(LocalDate.of(2021, 2, 1), "desc2"),
                        new BankHolidayDate(LocalDate.of(2021, 3, 1), "desc4")
                    ),
                    2022, List.of(
                        new BankHolidayDate(LocalDate.of(2022, 1, 1), "desc3")
                    ),
                    2023, List.of(
                        new BankHolidayDate(LocalDate.of(2023, 1, 4), "desc5")
                    )
                )
            );
            verify(administrationService, times(1))
                .findAllPublicHolidays();
        }

        @Test
        void positiveNoneFound() {
            doReturn(List.of()).when(administrationService).findAllPublicHolidays();
            assertThat(administrationService.viewBankHolidays()).isEmpty();
            verify(administrationService, times(1))
                .findAllPublicHolidays();
        }
    }

    @Nested
    @DisplayName("public List<CourtDetailsReduced> viewCourts()")
    class ViewCourts {

        private CourtLocation mockCourtLocation(String locCode, String name, CourtType type) {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getLocCode()).thenReturn(locCode);
            when(courtLocation.getName()).thenReturn(name);
            when(courtLocation.getType()).thenReturn(type);
            return courtLocation;
        }

        @Test
        void positiveTypical() {

            doReturn(List.of(
                mockCourtLocation("LOC1", "COURT1", CourtType.MAIN),
                mockCourtLocation("LOC2", "COURT2", CourtType.SATELLITE),
                mockCourtLocation("LOC3", "COURT3", CourtType.MAIN)
            )).when(courtLocationRepository).findAll();
            assertThat(administrationService.viewCourts()).containsExactly(
                CourtDetailsReduced.builder()
                    .locCode("LOC1")
                    .courtName("COURT1")
                    .courtType(CourtType.MAIN)
                    .build(),
                CourtDetailsReduced.builder()
                    .locCode("LOC2")
                    .courtName("COURT2")
                    .courtType(CourtType.SATELLITE)
                    .build(),
                CourtDetailsReduced.builder()
                    .locCode("LOC3")
                    .courtName("COURT3")
                    .courtType(CourtType.MAIN)
                    .build()
            );
        }

        @Test
        void positiveNoDataFound() {
            doReturn(List.of()).when(courtLocationRepository).findAll();
            assertThat(administrationService.viewCourts()).isEmpty();
        }
    }

}
