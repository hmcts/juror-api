package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.history.Revision;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsAppearancesRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class FinancialAuditServiceImplTest {

    private FinancialAuditDetailsRepository financialAuditDetailsRepository;
    private FinancialAuditDetailsAppearancesRepository financialAuditDetailsAppearancesRepository;
    private AppearanceRepository appearanceRepository;
    private UserRepository userRepository;
    private RevisionService revisionService;
    private Clock clock;
    private FinancialAuditServiceImpl financialAuditService;

    @BeforeEach
    void beforeEach() {
        financialAuditDetailsRepository = mock(FinancialAuditDetailsRepository.class);
        financialAuditDetailsAppearancesRepository = mock(FinancialAuditDetailsAppearancesRepository.class);
        appearanceRepository = mock(AppearanceRepository.class);
        userRepository = mock(UserRepository.class);
        revisionService = mock(RevisionService.class);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        financialAuditService =
            new FinancialAuditServiceImpl(financialAuditDetailsRepository, financialAuditDetailsAppearancesRepository,
                appearanceRepository, userRepository, revisionService, clock);
    }

    @Nested
    @DisplayName("public FinancialAuditDetails createFinancialAuditDetail(String jurorNumber,\n"
        + "                                                            String courtLocationCode,\n"
        + "                                                            FinancialAuditDetails.Type type,\n"
        + "                                                            List<Appearance> appearances)")
    class CreateFinancialAuditDetail {
        private MockedStatic<SecurityUtil> securityUtilMockedStatic;
        private static final String USER_NAME = "username123";

        @BeforeEach
        void beforeEach() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin)
                .thenReturn(USER_NAME);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        void positiveTypical() {
            FinancialAuditDetails.Type type = FinancialAuditDetails.Type.APPROVED_CASH;
            Revision<Long, Juror> jurorRevision = mock(Revision.class);
            doReturn(jurorRevision).when(revisionService)
                .getLatestJurorRevision(TestConstants.VALID_JUROR_NUMBER);
            doReturn(1L).when(jurorRevision).getRequiredRevisionNumber();

            Revision<Long, CourtLocation> courtRevision = mock(Revision.class);
            doReturn(courtRevision).when(revisionService)
                .getLatestCourtRevision(TestConstants.VALID_COURT_LOCATION);
            doReturn(12L).when(courtRevision).getRequiredRevisionNumber();

            User user = mock(User.class);
            doReturn(user).when(userRepository).findByUsername(USER_NAME);
            FinancialAuditDetails auditDetails = FinancialAuditDetails.builder()
                .id(321L)
                .createdBy(user)
                .createdOn(LocalDateTime.now(clock))
                .type(type)
                .jurorRevision(1L)
                .courtLocationRevision(12L)
                .build();
            doReturn(auditDetails).when(financialAuditDetailsRepository).save(any());

            CourtLocation courtLocation = mock(CourtLocation.class);

            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            when(appearance1.getCourtLocation()).thenReturn(courtLocation);
            when(appearance1.getVersion()).thenReturn(1L);
            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            when(appearance2.getCourtLocation()).thenReturn(courtLocation);
            when(appearance2.getVersion()).thenReturn(12L);
            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            when(appearance3.getCourtLocation()).thenReturn(courtLocation);
            when(appearance3.getVersion()).thenReturn(123L);


            doAnswer(invocation -> invocation.<Appearance>getArgument(0))
                .when(appearanceRepository).saveAndFlush(any());


            FinancialAuditDetails response = financialAuditService.createFinancialAuditDetail(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_COURT_LOCATION, type,
                List.of(appearance1, appearance2, appearance3));

            assertThat(response).isEqualTo(auditDetails);
            auditDetails.setId(null);//ID is null when first save gets called
            verify(financialAuditDetailsRepository, times(1)).save(auditDetails);
            verify(financialAuditDetailsAppearancesRepository, times(1)).saveAll(
                List.of(
                    new FinancialAuditDetailsAppearances(
                       321L,
                        TestConstants.VALID_JUROR_NUMBER,
                        LocalDate.of(2023, 1, 1),
                        courtLocation,
                        1L
                    ),
                    new FinancialAuditDetailsAppearances(
                        321L,
                        TestConstants.VALID_JUROR_NUMBER,
                        LocalDate.of(2023, 1, 2),
                        courtLocation,
                        12L
                    ),
                    new FinancialAuditDetailsAppearances(
                        321L,
                        TestConstants.VALID_JUROR_NUMBER,
                        LocalDate.of(2023, 1, 3),
                        courtLocation,
                        123L
                    )
                )
            );
            verify(appearanceRepository, times(1)).saveAll(List.of(appearance1, appearance2, appearance3));
        }
    }

    @Nested
    @DisplayName("public List<FinancialAuditDetails> getFinancialAuditDetails(Appearance appearance)")
    class GetFinancialAuditDetails {
        @Test
        void positiveTypical() {
            List<FinancialAuditDetails> response = List.of(mock(FinancialAuditDetails.class));
            Appearance appearance = mock(Appearance.class);
            when(financialAuditDetailsRepository.findAllByAppearance(appearance)).thenReturn(response);
            assertThat(financialAuditService.getFinancialAuditDetails(appearance)).isEqualTo(response);
        }
    }
}
