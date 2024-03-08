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
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.Address;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.UpdateCourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;
import uk.gov.hmcts.juror.api.moj.domain.system.HasEnabled;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
@SuppressWarnings({
    "unchecked",
    "PMD.ExcessiveImports"
})
class AdministrationServiceImplTest {

    private AdministrationServiceImpl administrationService;

    private CourtLocationRepository courtLocationRepository;
    private WelshCourtLocationRepository welshCourtLocationRepository;
    private CourtroomRepository courtroomRepository;

    @BeforeEach
    void beforeEach() {
        courtLocationRepository = mock(CourtLocationRepository.class);
        welshCourtLocationRepository = mock(WelshCourtLocationRepository.class);
        courtroomRepository = mock(CourtroomRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        this.administrationService =
            spy(new AdministrationServiceImpl(entityManager, courtLocationRepository,
                welshCourtLocationRepository, courtroomRepository));
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
    @DisplayName("CourtLocation getCourtLocation(String locCode)")
    class GetCourtLocation {

        @Test
        void positiveFound() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocationRepository.findById(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.of(courtLocation));
            assertThat(administrationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION))
                .isEqualTo(courtLocation);

            verify(courtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
        }

        @Test
        void negativeNotFound() {
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
    @DisplayName("public CourtDetailsDto viewCourt(String locCode)")
    class ViewCourt {

        @Test
        void positiveEnglish() {
            doReturn(CourtLocation.builder()
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
                .build()).when(administrationService)
                .getCourtLocation(TestConstants.VALID_COURT_LOCATION);
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
            verify(administrationService, times(1)).getCourtLocation(TestConstants.VALID_COURT_LOCATION);
            verify(welshCourtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(welshCourtLocationRepository);
        }

        @Test
        void positiveWelsh() {
            doReturn(CourtLocation.builder()
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
                .build()).when(administrationService).getCourtLocation(TestConstants.VALID_COURT_LOCATION);
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
            verify(administrationService, times(1)).getCourtLocation(TestConstants.VALID_COURT_LOCATION);
            verify(welshCourtLocationRepository, times(1)).findById(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(welshCourtLocationRepository);

        }
    }

    @Nested
    @DisplayName("public void updateCourt(String locCode, UpdateCourtDetailsDto updateCourtDetailsDto)")
    class UpdateCourt {
        @Test
        void positiveTypical() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(courtLocation).when(administrationService).getCourtLocation(TestConstants.VALID_COURT_LOCATION);

            Courtroom courtroom = mock(Courtroom.class);
            when(courtroomRepository.findById(3L)).thenReturn(Optional.of(courtroom));

            administrationService.updateCourt(TestConstants.VALID_COURT_LOCATION,
                UpdateCourtDetailsDto.builder()
                    .mainPhoneNumber("0123456789")
                    .defaultAttendanceTime(LocalTime.of(9, 0))
                    .assemblyRoomId(3L)
                    .costCentre("CSTCNR1")
                    .signature("COURT1 SIGNATURE")
                    .build());


            verify(courtLocation, times(1)).setLocPhone("0123456789");
            verify(courtLocation, times(1)).setCourtAttendTime(LocalTime.of(9, 0));
            verify(courtLocation, times(1)).setCostCentre("CSTCNR1");
            verify(courtLocation, times(1)).setSignatory("COURT1 SIGNATURE");
            verify(courtLocation, times(1)).setAssemblyRoom(courtroom);
            verify(courtLocationRepository, times(1)).save(courtLocation);
            verifyNoMoreInteractions(courtLocation, courtLocationRepository);
        }
    }

    @Nested
    @DisplayName("Courtroom getCourtRoom(Long assemblyRoomId)")
    class GetCourtRoom {

        @Test
        void positiveFound() {
            final Long id = 133L;
            Courtroom courtRoom = mock(Courtroom.class);
            when(courtroomRepository.findById(id))
                .thenReturn(Optional.of(courtRoom));
            assertThat(administrationService.getCourtRoom(id))
                .isEqualTo(courtRoom);

            verify(courtroomRepository, times(1)).findById(id);
        }

        @Test
        void negativeNotFound() {
            final Long id = 133L;
            when(courtroomRepository.findById(id))
                .thenReturn(Optional.empty());
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> administrationService.getCourtRoom(id),
                    "Expected exception to be thrown when a courtRoom is not found");
            assertThat(exception.getMessage()).isEqualTo("Courtroom not found");
            assertThat(exception.getCause()).isNull();
            verify(courtroomRepository, times(1)).findById(id);
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

    @Nested
    @DisplayName("public void updateCourtRates(String courtCode, CourtRates courtRates)")
    class UpdateCourtRates {

        @Test
        void positiveTypical() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(courtLocation).when(administrationService).getCourtLocation(TestConstants.VALID_COURT_LOCATION);
            administrationService.updateCourtRates(TestConstants.VALID_COURT_LOCATION,
                CourtRates.builder()
                    .taxiSoftLimit(new BigDecimal("1.23"))
                    .publicTransportSoftLimit(new BigDecimal("4.56"))
                    .build());

            verify(courtLocation, times(1)).setPublicTransportSoftLimit(new BigDecimal("4.56"));
            verify(courtLocation, times(1)).setTaxiSoftLimit(new BigDecimal("1.23"));
            verify(administrationService, times(1)).getCourtLocation(TestConstants.VALID_COURT_LOCATION);
            verify(courtLocationRepository, times(1)).save(courtLocation);
            verifyNoMoreInteractions(courtLocation, courtLocationRepository);
        }
    }
}
