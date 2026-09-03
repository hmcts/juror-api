package uk.gov.hmcts.juror.api.juror.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.config.FeatureFlagConfigurationProperties;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.JurorServiceModImpl;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseServiceImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PublicAuthenticationServiceImplTest {
    private static final String DIGITAL_BY_DEFAULT_FEATURE_FLAG = "digital-by-default";
    private static final String JUROR_NUMBER = "123456789";

    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorServiceModImpl jurorServiceModImpl;
    @Mock
    private JurorResponseServiceImpl jurorResponseServiceImpl;
    @Mock
    private FeatureFlagConfigurationProperties featureFlags;

    @InjectMocks
    private PublicAuthenticationServiceImpl publicAuthenticationService;

    @Test
    public void authenticationJuror_dbdCourtAndDbdJuror_returnsDigitalByDefaultTrue() {
        setupAuthentication(true, true);

        PublicAuthenticationResponseDto response =
            publicAuthenticationService.authenticationJuror(createAuthenticationRequest());

        assertThat(response.isDigitalByDefault()).isTrue();
    }

    @Test
    public void authenticationJuror_dbdCourtAndNonDbdJuror_returnsDigitalByDefaultFalse() {
        setupAuthentication(false, true);

        PublicAuthenticationResponseDto response =
            publicAuthenticationService.authenticationJuror(createAuthenticationRequest());

        assertThat(response.isDigitalByDefault()).isFalse();
    }

    private void setupAuthentication(boolean jurorDigitalByDefault, boolean courtDigitalByDefault) {
        Juror juror = createJuror(jurorDigitalByDefault);
        JurorPool jurorPool = createJurorPool(juror, courtDigitalByDefault);

        when(jurorResponseServiceImpl.getCommonJurorResponseOptional(JUROR_NUMBER)).thenReturn(Optional.empty());
        when(jurorServiceModImpl.getJurorOptionalFromJurorNumber(JUROR_NUMBER)).thenReturn(Optional.of(juror));
        when(jurorPoolRepository.findByJurorJurorNumberAndStatusStatusAndIsActive(
            JUROR_NUMBER, IJurorStatus.SUMMONED, true)).thenReturn(Optional.of(jurorPool));
        when(featureFlags.isEnabled(DIGITAL_BY_DEFAULT_FEATURE_FLAG)).thenReturn(true);
    }

    private PublicAuthenticationRequestDto createAuthenticationRequest() {
        return PublicAuthenticationRequestDto.builder()
            .jurorNumber(JUROR_NUMBER)
            .lastName("Smith")
            .postcode("AB1 2CD")
            .build();
    }

    private Juror createJuror(boolean digitalByDefault) {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        juror.setFirstName("Jane");
        juror.setLastName("SMITH");
        juror.setPostcode("AB1 2CD");
        juror.setDigitalByDefault(digitalByDefault);
        return juror;
    }

    private JurorPool createJurorPool(Juror juror, boolean courtDigitalByDefault) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setDigitalByDefault(courtDigitalByDefault);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setReturnDate(LocalDate.now().plusDays(10));

        JurorPool jurorPool = new JurorPool();
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);
        jurorPool.setNextDate(LocalDate.now().plusDays(10));
        return jurorPool;
    }
}
