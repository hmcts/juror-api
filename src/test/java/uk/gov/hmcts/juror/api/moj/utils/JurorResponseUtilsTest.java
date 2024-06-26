package uk.gov.hmcts.juror.api.moj.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorPaperResponseDetailDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;

@RunWith(SpringRunner.class)
public class JurorResponseUtilsTest {
    private static final String JUROR_NUMBER_123456789 = "123456789";

    @Mock
    JurorPaperResponseRepositoryMod jurorPaperResponseRepositoryMod;

    @Mock
    JurorPoolRepository jurorPoolRepository;

    @Test
    public void test_createMinimalPaperSummonsRecord() {
        String disqualifiedComment = "Disqualified due to age.";
        Juror mockJuror = createMockJuror(JUROR_NUMBER_123456789);
        LocalDateTime mockLocalDate = LocalDateTime.now();
        PaperResponse mockPaperResponse = createMockPaperResponse(mockJuror, mockLocalDate, disqualifiedComment);

        Mockito.doReturn(mockPaperResponse).when(jurorPaperResponseRepositoryMod)
            .findByJurorNumber(JUROR_NUMBER_123456789);

        PaperResponse actualPaperResponse = JurorResponseUtils.createMinimalPaperSummonsRecord(mockJuror,
            disqualifiedComment);

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorResponseUtils.createMinimalPaperSummonsRecord(mockJuror, disqualifiedComment));
        assertThat(actualPaperResponse.getJurorNumber()).isEqualTo(mockPaperResponse.getJurorNumber());
        assertThat(actualPaperResponse.getJurorNumber()).isEqualTo(mockPaperResponse.getJurorNumber());
        assertThat(actualPaperResponse.getTitle()).isEqualTo(mockPaperResponse.getTitle());
        assertThat(actualPaperResponse.getFirstName()).isEqualTo(mockPaperResponse.getFirstName());
        assertThat(actualPaperResponse.getLastName()).isEqualTo(mockPaperResponse.getLastName());
        assertThat(actualPaperResponse.getDateOfBirth()).isEqualTo(mockPaperResponse.getDateOfBirth());
        assertThat(actualPaperResponse.getAddressLine1()).isEqualTo(mockPaperResponse.getAddressLine1());
        assertThat(actualPaperResponse.getAddressLine2()).isEqualTo(mockPaperResponse.getAddressLine2());
        assertThat(actualPaperResponse.getAddressLine3()).isEqualTo(mockPaperResponse.getAddressLine3());
        assertThat(actualPaperResponse.getAddressLine4()).isEqualTo(mockPaperResponse.getAddressLine4());
        assertThat(actualPaperResponse.getAddressLine5()).isEqualTo(mockPaperResponse.getAddressLine5());
        assertThat(actualPaperResponse.getPostcode()).isEqualTo(mockPaperResponse.getPostcode());
        assertThat(actualPaperResponse.getThirdPartyReason()).isEqualTo(mockPaperResponse.getThirdPartyReason());
        assertThat(actualPaperResponse.getProcessingComplete()).isEqualTo(mockPaperResponse.getProcessingComplete());
        assertThat(actualPaperResponse.getCompletedAt()).isCloseTo(mockPaperResponse.getCompletedAt(),
            within(10, ChronoUnit.SECONDS));
    }

    @Test
    public void updateCurrentOwnerInResponseDto() {
        JurorPaperResponseDetailDto responseDto = new JurorPaperResponseDetailDto();
        responseDto.setJurorNumber(JUROR_NUMBER_123456789);

        JurorPool jurorPoolOne = createJurorPool("111111111", "457", LocalDateTime.now().minusDays(3),
            2);
        JurorPool jurorPoolThree = createJurorPool("222222222", "400", LocalDateTime.now().minusDays(5),
            10);
        Mockito.doReturn(Arrays.asList(jurorPoolOne, jurorPoolThree)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(JUROR_NUMBER_123456789, true);

        JurorResponseUtils.updateCurrentOwnerInResponseDto(jurorPoolRepository, responseDto);

        assertThat(responseDto.getCurrentOwner()).isEqualTo("457");
    }

    @Test
    public void updateCurrentOwnerInResponseDtoJurorPoolIsEmpty() {
        JurorPaperResponseDetailDto responseDto = new JurorPaperResponseDetailDto();
        responseDto.setJurorNumber(JUROR_NUMBER_123456789);

        Mockito.doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(JUROR_NUMBER_123456789, true);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            JurorResponseUtils.updateCurrentOwnerInResponseDto(jurorPoolRepository, responseDto));
    }

    private JurorPool createJurorPool(String poolNumber, String owner, LocalDateTime dateCreated, int status) {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER_123456789);

        PoolRequest pool = new PoolRequest();
        pool.setPoolNumber(poolNumber);
        pool.setDateCreated(dateCreated);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(status);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setJuror(juror);
        jurorPool.setPool(pool);
        jurorPool.setStatus(jurorStatus);

        return jurorPool;
    }

    private PaperResponse createMockPaperResponse(Juror juror, LocalDateTime mockLocalDate,
                                                  String disqualifiedComment) {
        PaperResponse mockPaperResponse = new PaperResponse();
        mockPaperResponse.setJurorNumber(juror.getJurorNumber());
        mockPaperResponse.setDateReceived(mockLocalDate);
        mockPaperResponse.setTitle(juror.getTitle());
        mockPaperResponse.setFirstName(juror.getFirstName());
        mockPaperResponse.setLastName(juror.getLastName());
        mockPaperResponse.setDateOfBirth(juror.getDateOfBirth());
        mockPaperResponse.setThirdPartyReason(disqualifiedComment);
        mockPaperResponse.setProcessingComplete(true);
        mockPaperResponse.setCompletedAt(mockLocalDate);
        mockPaperResponse.setAddressLine1("1 ANY STREET");
        mockPaperResponse.setAddressLine2("ANY TOWN");
        mockPaperResponse.setAddressLine3("ANYWHERE");
        mockPaperResponse.setAddressLine4("ADDRESS4");
        mockPaperResponse.setAddressLine5("ADDRESS5");
        mockPaperResponse.setPostcode("CH1 2AN");
        return mockPaperResponse;
    }

    private Juror createMockJuror(String jurorNumber) {
        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setTitle(null);
        juror.setFirstName("FNAMEONE");
        juror.setLastName("LNAMEONE");
        juror.setDateOfBirth(LocalDate.of(2020, 1, 1));
        setMockJurorAddress(juror);
        return juror;
    }

    private Juror setMockJurorAddress(Juror juror) {
        juror.setAddressLine1("1 ANY STREET");
        juror.setAddressLine2("ANY TOWN");
        juror.setAddressLine3("ANYWHERE");
        juror.setAddressLine4("ADDRESS4");
        juror.setAddressLine5("ADDRESS5");
        juror.setPostcode("CH1 2AN");
        return juror;
    }
}