package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JurorPoolServiceImplTest {

    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;


    @InjectMocks
    private JurorPoolServiceImpl jurorPoolService;

    @Test
    void positiveGetJurorFromJurorNumber() {
        PoolRequest poolRequest = mock(PoolRequest.class);
        when(poolRequestRepository.findByPoolNumber(TestConstants.VALID_POOL_NUMBER))
            .thenReturn(Optional.of(poolRequest));
        assertThat(jurorPoolService.getPoolRequest(TestConstants.VALID_POOL_NUMBER))
            .isEqualTo(poolRequest);
        verify(poolRequestRepository, times(1))
            .findByPoolNumber(TestConstants.VALID_POOL_NUMBER);
    }

    @Test
    void negativeGetJurorFromJurorNumberNotFound() {
        when(poolRequestRepository.findByPoolNumber(TestConstants.VALID_POOL_NUMBER))
            .thenReturn(Optional.empty());

        MojException.NotFound exception =
            assertThrows(
                MojException.NotFound.class,
                () -> jurorPoolService.getPoolRequest(TestConstants.VALID_POOL_NUMBER),
                "Should throw an error when pool is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage())
            .isEqualTo("Pool not found: " + TestConstants.VALID_POOL_NUMBER);
        assertThat(exception.getCause()).isNull();

        verify(poolRequestRepository, times(1))
            .findByPoolNumber(TestConstants.VALID_JUROR_NUMBER);
    }

    @Test
    void positiveHasPoolWithLocCodeTrue() {
        List<String> locCodes = List.of(TestConstants.VALID_COURT_LOCATION, "414");
        when(jurorPoolRepository.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes))
            .thenReturn(true);

        assertThat(jurorPoolService.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes)).isTrue();

        verify(jurorPoolRepository, times(1))
            .hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes);

    }

    @Test
    void positiveHasPoolWithLocCodeFalse() {
        List<String> locCodes = List.of(TestConstants.VALID_COURT_LOCATION, "414");
        when(jurorPoolRepository.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes))
            .thenReturn(false);

        assertThat(jurorPoolService.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes)).isFalse();

        verify(jurorPoolRepository, times(1))
            .hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes);

    }

    @Nested
    @DisplayName("PaginatedList<JurorDetailsDto> search(JurorPoolSearch search)")
    class Search {

        @BeforeEach
        void beforeEach() {
            TestUtils.mockSecurityUtil(BureauJwtPayload.builder()
                .locCode(TestConstants.VALID_COURT_LOCATION)
                .owner(TestConstants.VALID_COURT_LOCATION)
                .userType(UserType.COURT)
                .build()
            );
        }

        @AfterEach
        void afterEach() {
            TestUtils.afterAll();
        }

        @Test
        @SuppressWarnings("PMD.NcssCount")
        void positiveTypical() {
            JurorDetailsDto jurorDetailsDto1 = mock(JurorDetailsDto.class);
            when(jurorDetailsDto1.getJurorNumber()).thenReturn("111111111");
            when(jurorDetailsDto1.getPoolNumber()).thenReturn("2222222222");
            when(jurorDetailsDto1.getFirstName()).thenReturn("FNAME1");
            when(jurorDetailsDto1.getLastName()).thenReturn("LNAME1");
            when(jurorDetailsDto1.getPostCode()).thenReturn("POSTCODE1");
            when(jurorDetailsDto1.getCompletionDate()).thenReturn(LocalDate.of(2023, 1, 1));

            JurorDetailsDto jurorDetailsDto2 = mock(JurorDetailsDto.class);
            when(jurorDetailsDto2.getJurorNumber()).thenReturn("111111112");
            when(jurorDetailsDto2.getPoolNumber()).thenReturn("2222222223");
            when(jurorDetailsDto2.getFirstName()).thenReturn("FNAME2");
            when(jurorDetailsDto2.getLastName()).thenReturn("LNAME2");
            when(jurorDetailsDto2.getPostCode()).thenReturn("POSTCODE2");
            when(jurorDetailsDto2.getCompletionDate()).thenReturn(LocalDate.of(2023, 1, 2));

            JurorDetailsDto jurorDetailsDto3 = mock(JurorDetailsDto.class);
            when(jurorDetailsDto3.getJurorNumber()).thenReturn("111111113");
            when(jurorDetailsDto3.getPoolNumber()).thenReturn("2222222224");
            when(jurorDetailsDto3.getFirstName()).thenReturn("FNAME3");
            when(jurorDetailsDto3.getLastName()).thenReturn("LNAME3");
            when(jurorDetailsDto3.getPostCode()).thenReturn("POSTCODE3");
            when(jurorDetailsDto3.getCompletionDate()).thenReturn(LocalDate.of(2023, 1, 3));

            JurorPoolSearch poolSearch = JurorPoolSearch.builder()
                .jurorNumber("1234")
                .build();

            PaginatedList<JurorDetailsDto> result = new PaginatedList<>();
            result.setData(List.of(jurorDetailsDto1, jurorDetailsDto2, jurorDetailsDto3));
            doReturn(result)
                .when(jurorPoolRepository)
                .findJurorPoolsBySearch(eq(poolSearch), eq("415"), any(), any(), eq(500L));

            PaginatedList<JurorDetailsDto> responses = jurorPoolService.search(poolSearch);

            assertThat(responses).isNotNull();
            List<JurorDetailsDto> data = responses.getData();
            assertThat(data).isNotNull().hasSize(3);
            JurorDetailsDto response1 = data.get(0);
            assertThat(response1).isNotNull();
            assertThat(response1.getJurorNumber()).isEqualTo("111111111");
            assertThat(response1.getPoolNumber()).isEqualTo("2222222222");
            assertThat(response1.getFirstName()).isEqualTo("FNAME1");
            assertThat(response1.getLastName()).isEqualTo("LNAME1");
            assertThat(response1.getPostCode()).isEqualTo("POSTCODE1");
            assertThat(response1.getCompletionDate()).isEqualTo(LocalDate.of(2023, 1, 1));

            JurorDetailsDto response2 = data.get(1);
            assertThat(response2).isNotNull();
            assertThat(response2.getJurorNumber()).isEqualTo("111111112");
            assertThat(response2.getPoolNumber()).isEqualTo("2222222223");
            assertThat(response2.getFirstName()).isEqualTo("FNAME2");
            assertThat(response2.getLastName()).isEqualTo("LNAME2");
            assertThat(response2.getPostCode()).isEqualTo("POSTCODE2");
            assertThat(response2.getCompletionDate()).isEqualTo(LocalDate.of(2023, 1, 2));

            JurorDetailsDto response3 = data.get(2);
            assertThat(response3).isNotNull();
            assertThat(response3.getJurorNumber()).isEqualTo("111111113");
            assertThat(response3.getPoolNumber()).isEqualTo("2222222224");
            assertThat(response3.getFirstName()).isEqualTo("FNAME3");
            assertThat(response3.getLastName()).isEqualTo("LNAME3");
            assertThat(response3.getPostCode()).isEqualTo("POSTCODE3");
            assertThat(response3.getCompletionDate()).isEqualTo(LocalDate.of(2023, 1, 3));

            verify(jurorPoolRepository, times(1))
                .findJurorPoolsBySearch(eq(poolSearch), eq("415"), any(), any(), eq(500L));
        }


        @ParameterizedTest
        @NullSource
        @EmptySource
        void negativePoolsNotFound(List<JurorDetailsDto> data) {
            JurorPoolSearch poolSearch = JurorPoolSearch.builder()
                .jurorNumber("123")
                .jurorStatus(IJurorStatus.COMPLETED)
                .build();
            PaginatedList<JurorDetailsDto> response = new PaginatedList<>();
            response.setData(data);
            doReturn(response)
                .when(jurorPoolRepository)
                .findJurorPoolsBySearch(eq(poolSearch), eq("415"), any(), any(), eq(500L));


            MojException.NotFound exception = Assertions.assertThrows(MojException.NotFound.class,
                () -> jurorPoolService.search(poolSearch),
                "Exception should be thrown");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo(
                "No juror pools found that meet your search criteria.");

            verify(jurorPoolRepository, times(1))
                .findJurorPoolsBySearch(eq(poolSearch), eq("415"), any(), any(), eq(500L));
        }
    }
}
