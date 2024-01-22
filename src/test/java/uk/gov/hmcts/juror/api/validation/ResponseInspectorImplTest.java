package uk.gov.hmcts.juror.api.validation;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCJS;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.bureau.domain.TSpecial;
import uk.gov.hmcts.juror.api.bureau.service.AppSettingService;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl.AGE_LOWER_SP_ID;
import static uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl.AGE_UPPER_SP_ID;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResponseInspectorImplTest {
    private static final String JUROR_EMAIL = "juror@test.com";
    private static final String THIRD_PARTY_FIRST_NAME = "Thirdy";
    private static final String THIRD_PARTY_EMAIL = "third@test.com";
    private static final String EMPTY_STRING = "";

    @Mock
    private SystemParameterRepository mockSystemParameterRepository;

    @Mock
    private PoolRepository mockPoolRepository;

    @Mock
    private AppSettingService mockAppSettingService;

    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;

    @InjectMocks
    private ResponseInspectorImpl inspector;

    @Test
    public void isThirdPartyResponse() {
        final JurorResponse jurorResponseFirstPerson = JurorResponse.builder().thirdPartyFName(null).build();
        assertThat(inspector.isThirdPartyResponse(jurorResponseFirstPerson)).isFalse();

        final JurorResponse jurorResponseFirstPersonEmptyName =
            JurorResponse.builder().thirdPartyFName(EMPTY_STRING).build();
        assertThat(inspector.isThirdPartyResponse(jurorResponseFirstPersonEmptyName)).isFalse();

        final JurorResponse jurorResponseThirdParty =
            JurorResponse.builder().thirdPartyFName(THIRD_PARTY_FIRST_NAME).build();
        assertThat(inspector.isThirdPartyResponse(jurorResponseThirdParty)).isTrue();
    }

    @Test
    public void hasAdjustments() {
        final JurorResponse jurorResponseNoAdjustments = JurorResponse.builder().build();
        assertThat(inspector.hasAdjustments(jurorResponseNoAdjustments)).isFalse();

        final JurorResponse jurorResponseWithAdjustments = JurorResponse.builder()
            .specialNeedsArrangements("special")
            .specialNeeds(Collections.singletonList(BureauJurorSpecialNeed.builder()
                .specialNeed(new TSpecial())
                .build()))
            .build();
        assertThat(inspector.hasAdjustments(jurorResponseWithAdjustments)).isTrue();
    }

    @Test
    public void isWelshLanguage() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(true);
        final JurorResponse jurorResponseEnglishLanguageDefault = JurorResponse.builder()
            .welsh(null)
            .build();
        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguageDefault)).isFalse();

        final JurorResponse jurorResponseEnglishLanguage = JurorResponse.builder()
            .welsh(false)
            .build();
        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguage)).isFalse();

        final JurorResponse jurorResponseWelshLanguage = JurorResponse.builder()
            .welsh(true)
            .build();
        assertThat(inspector.isWelshLanguage(jurorResponseWelshLanguage)).isTrue();
        verify(mockAppSettingService, times(3)).isWelshEnabled();
    }

    @Test
    public void isWelshLanguage_welshLanguageSupportDisabled() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(false);
        final JurorResponse jurorResponseEnglishLanguageDefault = JurorResponse.builder()
            .welsh(null)
            .build();
        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguageDefault)).isFalse();

        final JurorResponse jurorResponseEnglishLanguage = JurorResponse.builder()
            .welsh(false)
            .build();
        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguage)).isFalse();

        final JurorResponse jurorResponseWelshLanguage = JurorResponse.builder()
            .welsh(true)
            .build();
        assertThat(inspector.isWelshLanguage(jurorResponseWelshLanguage)).isFalse();
        verify(mockAppSettingService, times(3)).isWelshEnabled();
    }

    @Test
    public void activeContactEmail() {
        final JurorResponse jurorResponseFirstPerson = JurorResponse.builder().email(JUROR_EMAIL).build();
        assertThat(inspector.activeContactEmail(jurorResponseFirstPerson)).isEqualTo(JUROR_EMAIL);

        final JurorResponse jurorResponseThirdPartyJurorDetails = JurorResponse.builder()
            .email(JUROR_EMAIL)
            .jurorEmailDetails(Boolean.TRUE)
            .thirdPartyFName(THIRD_PARTY_FIRST_NAME)
            .emailAddress(THIRD_PARTY_EMAIL)
            .build();
        assertThat(inspector.activeContactEmail(jurorResponseThirdPartyJurorDetails)).isEqualTo(JUROR_EMAIL);

        final JurorResponse jurorResponseThirdPartyThirdPartyDetails = JurorResponse.builder()
            .email(JUROR_EMAIL)
            .jurorEmailDetails(Boolean.FALSE)
            .thirdPartyFName(THIRD_PARTY_FIRST_NAME)
            .emailAddress(THIRD_PARTY_EMAIL)
            .build();
        assertThat(inspector.activeContactEmail(jurorResponseThirdPartyThirdPartyDetails)).isEqualTo(THIRD_PARTY_EMAIL);
    }

    @Test
    public void responseType_firstPerson_acceptance() {
        final String JUROR_NUMBER = "123555666";
        final JurorResponse jurorAcceptance = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .dateOfBirth(Date.from(LocalDate.of(1970, 6, 13).atStartOfDay().toInstant(ZoneOffset.UTC)))
            .residency(Boolean.TRUE)
            .mentalHealthAct(Boolean.FALSE)
            .bail(Boolean.FALSE)
            .convictions(Boolean.FALSE)
            .build();
        assertThat(inspector.responseType(jurorAcceptance)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Test
    public void responseType_thirdParty_acceptance() {
        final String JUROR_NUMBER = "555888999";
        final JurorResponse thirdPartyAcceptance = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .dateOfBirth(Date.from(LocalDate.of(1970, 6, 13).atStartOfDay().toInstant(ZoneOffset.UTC)))
            .residency(Boolean.TRUE)
            .mentalHealthAct(Boolean.FALSE)
            .bail(Boolean.FALSE)
            .convictions(Boolean.FALSE)
            .build();
        assertThat(inspector.responseType(thirdPartyAcceptance)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyAcceptance)).isTrue();
    }

    @Test
    public void isJurorAgeDisqualified_1day_too_young() {
        final String YOUNG_JUROR_NUMBER = "123456000";
        final LocalDate HEARING_DATE = LocalDate.of(2018, 6, 26);

        final JurorResponse tooYoungJuror = JurorResponse.builder()
            .jurorNumber(YOUNG_JUROR_NUMBER)
            .dateOfBirth(
                Date.from(
                    HEARING_DATE.minusYears(18)
                        .plusDays(1)
                        .atStartOfDay().toInstant(ZoneOffset.UTC))
            ).build();
        given(mockPoolRepository.findByJurorNumber(anyString()))
            .willReturn(Pool.builder()
                .jurorNumber(YOUNG_JUROR_NUMBER)
                .hearingDate(Date.from(HEARING_DATE.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isTrue();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, times(0)).findById(ResponseInspectorImpl.AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_18_exactly() {
        final String YOUNG_JUROR_NUMBER = "123456000";
        final LocalDate HEARING_DATE = LocalDate.of(2018, 6, 26);

        final JurorResponse tooYoungJuror = JurorResponse.builder()
            .jurorNumber(YOUNG_JUROR_NUMBER)
            .dateOfBirth(
                Date.from(
                    HEARING_DATE.minusYears(18)
                        .atStartOfDay().toInstant(ZoneOffset.UTC))
            ).build();
        given(mockPoolRepository.findByJurorNumber(anyString()))
            .willReturn(Pool.builder()
                .jurorNumber(YOUNG_JUROR_NUMBER)
                .hearingDate(Date.from(HEARING_DATE.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isFalse();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(ResponseInspectorImpl.AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_50_years_old() {
        final String YOUNG_JUROR_NUMBER = "123456000";
        final LocalDate HEARING_DATE = LocalDate.of(2018, 6, 26);

        final JurorResponse tooYoungJuror = JurorResponse.builder()
            .jurorNumber(YOUNG_JUROR_NUMBER)
            .dateOfBirth(
                Date.from(
                    HEARING_DATE.minusYears(50)
                        .atStartOfDay().toInstant(ZoneOffset.UTC))
            ).build();
        given(mockPoolRepository.findByJurorNumber(anyString()))
            .willReturn(Pool.builder()
                .jurorNumber(YOUNG_JUROR_NUMBER)
                .hearingDate(Date.from(HEARING_DATE.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isFalse();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(ResponseInspectorImpl.AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_too_old_exactly_76() {
        final String YOUNG_JUROR_NUMBER = "123456000";
        final LocalDate HEARING_DATE = LocalDate.of(2018, 6, 26);

        final JurorResponse tooYoungJuror = JurorResponse.builder()
            .jurorNumber(YOUNG_JUROR_NUMBER)
            .dateOfBirth(
                Date.from(
                    HEARING_DATE.minusYears(76)
                        .atStartOfDay().toInstant(ZoneOffset.UTC))
            ).build();
        given(mockPoolRepository.findByJurorNumber(anyString()))
            .willReturn(Pool.builder()
                .jurorNumber(YOUNG_JUROR_NUMBER)
                .hearingDate(Date.from(HEARING_DATE.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isTrue();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(ResponseInspectorImpl.AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_1_day_before_76th_birthday() {
        final String YOUNG_JUROR_NUMBER = "123456000";
        final LocalDate HEARING_DATE = LocalDate.of(2018, 6, 26);

        final JurorResponse tooYoungJuror = JurorResponse.builder()
            .jurorNumber(YOUNG_JUROR_NUMBER)
            .dateOfBirth(
                Date.from(
                    HEARING_DATE.minusYears(76)
                        .plusDays(1)
                        .atStartOfDay().toInstant(ZoneOffset.UTC))
            ).build();
        given(mockPoolRepository.findByJurorNumber(anyString()))
            .willReturn(Pool.builder()
                .jurorNumber(YOUNG_JUROR_NUMBER)
                .hearingDate(Date.from(HEARING_DATE.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isFalse();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(ResponseInspectorImpl.AGE_UPPER_SP_ID);
    }

    @Test
    public void isIneligible_fp() {
        final String JUROR_NUMBER = "123555666";
        final JurorResponse jurorResponseResidency = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .residency(Boolean.FALSE)
            .build();
        assertThat(inspector.responseType(jurorResponseResidency)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);

        final JurorResponse jurorResponseBail = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .bail(Boolean.TRUE)
            .build();
        assertThat(inspector.responseType(jurorResponseBail)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);

        final JurorResponse jurorResponseMental = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .mentalHealthAct(Boolean.TRUE)
            .build();
        assertThat(inspector.responseType(jurorResponseMental)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);

        final JurorResponse jurorResponseConvictions = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .convictions(Boolean.TRUE)
            .build();
        assertThat(inspector.responseType(jurorResponseConvictions)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);

        final JurorResponse jurorResponseCjs = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .cjsEmployments(Lists.newArrayList(BureauJurorCJS.builder()
                .employer("Police")
                .build()))
            .build();
        assertThat(inspector.responseType(jurorResponseCjs)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Test
    public void isIneligible_3p() {
        final String JUROR_NUMBER = "555888999";
        final JurorResponse thirdPartyResidency = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .residency(Boolean.FALSE)
            .build();
        assertThat(inspector.responseType(thirdPartyResidency)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyResidency)).isTrue();

        final JurorResponse thirdPartyBail = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .bail(Boolean.TRUE)
            .build();
        assertThat(inspector.responseType(thirdPartyBail)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyBail)).isTrue();

        final JurorResponse thirdPartyMental = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .mentalHealthAct(Boolean.TRUE)
            .build();
        assertThat(inspector.responseType(thirdPartyMental)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyMental)).isTrue();

        final JurorResponse thirdPartyConvictions = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .convictions(Boolean.TRUE)
            .build();
        assertThat(inspector.responseType(thirdPartyConvictions)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyConvictions)).isTrue();

        final JurorResponse thirdPartyCjs = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .cjsEmployments(Lists.newArrayList(BureauJurorCJS.builder()
                .employer("Police")
                .build()))
            .build();
        assertThat(inspector.responseType(thirdPartyCjs)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyCjs)).isTrue();
    }

    @Test
    public void isDeferral_fp() {
        final String JUROR_NUMBER = "123555666";
        final JurorResponse jurorResponseFirstPersonDeferral = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .deferralReason("Defer me")
            .build();
        assertThat(inspector.responseType(jurorResponseFirstPersonDeferral)).isEqualTo(NotifyTemplateType.DEFERRAL);
    }

    @Test
    public void isDeferral_3p() {
        final String JUROR_NUMBER = "555888999";
        final JurorResponse thirdPartyDeferral = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .deferralReason("Defer me")
            .build();
        assertThat(inspector.responseType(thirdPartyDeferral)).isEqualTo(NotifyTemplateType.DEFERRAL);
        assertThat(inspector.isThirdPartyResponse(thirdPartyDeferral)).isTrue();
    }

    @Test
    public void isExcusal_fp() {
        final String JUROR_NUMBER = "123555666";

        final JurorResponse jurorResponseFirstPersonExcusal = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .excusalReason("Excuse me")
            .build();
        assertThat(inspector.responseType(jurorResponseFirstPersonExcusal)).isEqualTo(NotifyTemplateType.EXCUSAL);
    }

    @Test
    public void isExcusal_3p() {
        final String JUROR_NUMBER = "555888999";

        final JurorResponse thirdPartyExcusal = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .excusalReason("Excuse me")
            .build();
        assertThat(inspector.responseType(thirdPartyExcusal)).isEqualTo(NotifyTemplateType.EXCUSAL);
        assertThat(inspector.isThirdPartyResponse(thirdPartyExcusal)).isTrue();
    }

    @Test
    public void isJurorDeceased() {
        final String JUROR_NUMBER = "555888999";
        final JurorResponse thirdPartyDeceased = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .thirdPartyFName("Bob")
            .thirdPartyReason(ResponseInspectorImpl.DECEASED)
            .dateOfBirth(Date.from(LocalDate.of(1970, 6, 13).atStartOfDay().toInstant(ZoneOffset.UTC)))
            .residency(Boolean.TRUE)
            .mentalHealthAct(Boolean.FALSE)
            .bail(Boolean.FALSE)
            .convictions(Boolean.FALSE)
            .build();
        assertThat(inspector.responseType(thirdPartyDeceased)).isEqualTo(NotifyTemplateType.EXCUSAL_DECEASED);
        assertThat(inspector.isThirdPartyResponse(thirdPartyDeceased)).isTrue();
        assertThat(inspector.isJurorDeceased(thirdPartyDeceased)).isTrue();
    }

    @Test
    public void getYoungestJurorAgeAllowed() {
//        given(mockSystemParameterRepository.findById(AGE_LOWER_SP_ID)).willReturn(SystemParameter.builder()
//                .spValue("99")
//                .build());

        given(mockSystemParameterRepository.findById(AGE_LOWER_SP_ID)).willReturn(Optional.of(SystemParameter.builder()
            .spValue("99")
            .build()));

        //given(mockSystemParameterRepository.findById(AGE_LOWER_SP_ID)).willReturn(any(Optional.class));
        assertThat(inspector.getYoungestJurorAgeAllowed()).isEqualTo(99);
        verify(mockSystemParameterRepository).findById(AGE_LOWER_SP_ID);
    }

    @Test
    public void getTooOldJurorAge() {
        given(mockSystemParameterRepository.findById(AGE_UPPER_SP_ID)).willReturn(Optional.of(SystemParameter.builder()
            .spValue("1234")
            .build()));

        assertThat(inspector.getTooOldJurorAge()).isEqualTo(1234);
        verify(mockSystemParameterRepository).findById(AGE_UPPER_SP_ID);
    }

    @Test
    public void getJurorAgeAtHearingDate() {
        final Date HEARING_02_01_2018 = Date.from(LocalDate.of(2018, 1, 2).atStartOfDay().toInstant(ZoneOffset.UTC));
        final Date ONE_DAY_BEFORE_18 = Date.from(LocalDate.of(2000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
        final Date EXACTLY_18 = Date.from(LocalDate.of(2000, 1, 2).atStartOfDay().toInstant(ZoneOffset.UTC));
        final Date ONE_DAY_AFTER_18 = Date.from(LocalDate.of(2000, 1, 3).atStartOfDay().toInstant(ZoneOffset.UTC));

        assertThat(inspector.getJurorAgeAtHearingDate(ONE_DAY_BEFORE_18, HEARING_02_01_2018))
            .as("Juror is 18 the day before the hearing")
            .isEqualTo(18);

        assertThat(inspector.getJurorAgeAtHearingDate(EXACTLY_18, HEARING_02_01_2018))
            .as("Juror is 18 on the hearing date.")
            .isEqualTo(18);

        assertThat(inspector.getJurorAgeAtHearingDate(ONE_DAY_AFTER_18, HEARING_02_01_2018))
            .as("Juror is 18 one day after the hearing.")
            .isEqualTo(17);
    }

    @Test
    public void isWelshCourtTrue() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(true);
        final String JUROR_NUMBER = "443355577";
        final String TITLE = "Mr";
        final String FIRST_NAME = "Testy";
        final String LAST_NAME = "Jones";
        final String EMAIL = "testy.jones@cgi.com";

        final CourtLocation court = new CourtLocation();
        court.setLocCode("457");

        final WelshCourtLocation welshCourt = new WelshCourtLocation();
        welshCourt.setLocCode("457");

        final Pool pool = Pool.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(TITLE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .email(EMAIL)
            .welsh(true)
            .court(court)
            .build();

        final JurorResponse jurorResponseEnglishLanguageDefault = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .welsh(true)
            .build();

        given(mockPoolRepository.findByJurorNumber(anyString())).willReturn(pool);

        given(welshCourtLocationRepository.findByLocCode(anyString())).willReturn(welshCourt);

        assertThat(inspector.isWelshCourt(jurorResponseEnglishLanguageDefault)).isTrue();

        verify(mockAppSettingService, times(1)).isWelshEnabled();
    }

    @Test
    public void isWelshCourtFalse() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(true);
        final String JUROR_NUMBER = "443355578";
        final String TITLE = "Mr";
        final String FIRST_NAME = "Jones";
        final String LAST_NAME = "Testy";
        final String EMAIL = "testy.jones@cgi.com";

        final CourtLocation court = new CourtLocation();
        court.setLocCode("417");

        final Pool pool = Pool.builder()
            .jurorNumber(JUROR_NUMBER)
            .title(TITLE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .email(EMAIL)
            .welsh(true)
            .court(court)
            .build();

        final JurorResponse jurorResponseEnglishLanguageDefault = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .welsh(true)
            .build();

        given(mockPoolRepository.findByJurorNumber(anyString())).willReturn(pool);

        given(welshCourtLocationRepository.findByLocCode(anyString())).willReturn(null);

        assertThat(inspector.isWelshCourt(jurorResponseEnglishLanguageDefault)).isFalse();

        verify(mockAppSettingService, times(1)).isWelshEnabled();
    }
}