package uk.gov.hmcts.juror.api.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private JurorPoolRepository mockPoolRepository;

    @Mock
    private AppSettingService mockAppSettingService;

    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;

    @InjectMocks
    private ResponseInspectorImpl inspector;

    @Test
    public void isThirdPartyResponse() {
        final DigitalResponse jurorResponseFirstPerson = DigitalResponse.builder().thirdPartyFName(null).build();
        assertThat(inspector.isThirdPartyResponse(jurorResponseFirstPerson)).isFalse();

        final DigitalResponse jurorResponseFirstPersonEmptyName =
            DigitalResponse.builder().thirdPartyFName(EMPTY_STRING).build();
        assertThat(inspector.isThirdPartyResponse(jurorResponseFirstPersonEmptyName)).isFalse();

        final DigitalResponse jurorResponseThirdParty =
            DigitalResponse.builder().thirdPartyFName(THIRD_PARTY_FIRST_NAME).build();
        assertThat(inspector.isThirdPartyResponse(jurorResponseThirdParty)).isTrue();
    }

    @Test
    public void hasAdjustments() {
        //   final DigitalResponse jurorResponseNoAdjustments = DigitalResponse.builder().build();
        //    assertThat(inspector.hasAdjustments(jurorResponseNoAdjustments)).isFalse();

        //    final DigitalResponse jurorResponseWithAdjustments = DigitalResponse.builder()
        //        .specialNeedsArrangements("special")
        //        .specialNeeds(Collections.singletonList(BureauJurorSpecialNeed.builder()
        //            .specialNeed(new TSpecial())
        //            .build()))
        //        .build();
        //    assertThat(inspector.hasAdjustments(jurorResponseWithAdjustments)).isTrue();

        final DigitalResponse jurorResponseNoAdjustments = DigitalResponse.builder().build();
        assertThat(inspector.hasAdjustments(jurorResponseNoAdjustments)).isFalse();
        DigitalResponse jurorResponseWithAdjustments = new DigitalResponse();
        jurorResponseWithAdjustments.setReasonableAdjustmentsArrangements("special");
        // jurorResponseWithAdjustments.setReasonableAdjustments(Collections.singletonList(JurorReasonableAdjustment
        // .builder().build()));
        JurorReasonableAdjustment jurorReasonableAdjustment = new JurorReasonableAdjustment();
        //  jurorReasonableAdjustment.setReasonableAdjustment(new ReasonableAdjustments());
        //  jurorResponseWithAdjustments.setReasonableAdjustments
        //    (Collections.singletonList(jurorReasonableAdjustment.setReasonableAdjustment(new ReasonableAdjustments
        //    ())));
        //   jurorResponseWithAdjustments.setReasonableAdjustments(new ReasonableAdjustments().);
    }

    @Test
    public void isWelshLanguage() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(true);
        final DigitalResponse jurorResponseEnglishLanguageDefault = new DigitalResponse();
        jurorResponseEnglishLanguageDefault.setWelsh(null);
        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguageDefault)).isFalse();

        final DigitalResponse jurorResponseEnglishLanguage = new DigitalResponse();
        jurorResponseEnglishLanguage.setWelsh(false);
        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguage)).isFalse();

        final DigitalResponse jurorResponseWelshLanguage = new DigitalResponse();
        jurorResponseWelshLanguage.setWelsh(true);
        assertThat(inspector.isWelshLanguage(jurorResponseWelshLanguage)).isTrue();
        verify(mockAppSettingService, times(3)).isWelshEnabled();
    }

    @Test
    public void isWelshLanguage_welshLanguageSupportDisabled() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(false);

        final DigitalResponse jurorResponseEnglishLanguageDefault = new DigitalResponse();
        jurorResponseEnglishLanguageDefault.setWelsh(null);

        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguageDefault)).isFalse();


        final DigitalResponse jurorResponseEnglishLanguage = new DigitalResponse();
        jurorResponseEnglishLanguageDefault.setWelsh(null);

        assertThat(inspector.isWelshLanguage(jurorResponseEnglishLanguage)).isFalse();


        final DigitalResponse jurorResponseWelshLanguage = new DigitalResponse();
        jurorResponseEnglishLanguageDefault.setWelsh(null);

        assertThat(inspector.isWelshLanguage(jurorResponseWelshLanguage)).isFalse();
        verify(mockAppSettingService, times(3)).isWelshEnabled();
    }

    @Test
    public void activeContactEmail() {
        final DigitalResponse jurorResponseFirstPerson = new DigitalResponse();
        jurorResponseFirstPerson.setEmail(JUROR_EMAIL);
        assertThat(inspector.activeContactEmail(jurorResponseFirstPerson)).isEqualTo(JUROR_EMAIL);


        final DigitalResponse jurorResponseThirdPartyJurorDetails = new DigitalResponse();
        jurorResponseThirdPartyJurorDetails.setEmail(JUROR_EMAIL);
        jurorResponseThirdPartyJurorDetails.setJurorEmailDetails(Boolean.TRUE);
        jurorResponseThirdPartyJurorDetails.setThirdPartyFName(THIRD_PARTY_FIRST_NAME);
        jurorResponseThirdPartyJurorDetails.setEmailAddress(THIRD_PARTY_EMAIL);

        assertThat(inspector.activeContactEmail(jurorResponseThirdPartyJurorDetails)).isEqualTo(JUROR_EMAIL);


        final DigitalResponse jurorResponseThirdPartyThirdPartyDetails = new DigitalResponse();
        jurorResponseThirdPartyThirdPartyDetails.setEmail(JUROR_EMAIL);
        jurorResponseThirdPartyThirdPartyDetails.setJurorEmailDetails(Boolean.FALSE);
        jurorResponseThirdPartyThirdPartyDetails.setThirdPartyFName(THIRD_PARTY_FIRST_NAME);
        jurorResponseThirdPartyThirdPartyDetails.setEmailAddress(THIRD_PARTY_EMAIL);

        assertThat(inspector.activeContactEmail(jurorResponseThirdPartyThirdPartyDetails)).isEqualTo(THIRD_PARTY_EMAIL);
    }

    @Test
    public void responseType_firstPerson_acceptance() {
        final String jurorNumber = "123555666";

        final DigitalResponse jurorAcceptance = new DigitalResponse();
        jurorAcceptance.setJurorNumber(jurorNumber);
        jurorAcceptance.setDateOfBirth(LocalDate.of(1970, 6, 13));
        jurorAcceptance.setResidency(Boolean.TRUE);
        jurorAcceptance.setMentalHealthAct(Boolean.FALSE);
        jurorAcceptance.setBail(Boolean.FALSE);
        jurorAcceptance.setConvictions(Boolean.FALSE);
        assertThat(inspector.responseType(jurorAcceptance)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Test
    public void responseType_thirdParty_acceptance() {
        final String jurorNumber = "555888999";

        final DigitalResponse thirdPartyAcceptance = new DigitalResponse();
        thirdPartyAcceptance.setJurorNumber(jurorNumber);
        thirdPartyAcceptance.setThirdPartyFName("Bob");
        thirdPartyAcceptance.setDateOfBirth(LocalDate.of(1970, 6, 13));
        thirdPartyAcceptance.setResidency(Boolean.TRUE);
        thirdPartyAcceptance.setMentalHealthAct(Boolean.FALSE);
        thirdPartyAcceptance.setBail(Boolean.FALSE);
        thirdPartyAcceptance.setConvictions(Boolean.FALSE);

        assertThat(inspector.responseType(thirdPartyAcceptance)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyAcceptance)).isTrue();
    }

    @Test
    public void isJurorAgeDisqualified_1day_too_young() {
        final String youngJurorNumber = "123456000";
        final LocalDate hearingDate = LocalDate.of(2018, 6, 26);


        final DigitalResponse tooYoungJuror = new DigitalResponse();
        tooYoungJuror.setJurorNumber(youngJurorNumber);
        tooYoungJuror.setDateOfBirth(hearingDate.minusYears(18).plusDays(1));
        given(mockPoolRepository.findByJurorJurorNumber(anyString()))
            .willReturn(JurorPool.builder()
                .juror(Juror.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .build())
                .nextDate(hearingDate)
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isTrue();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, times(0)).findById(AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_18_exactly() {
        final String youngJurorNumber = "123456000";
        final LocalDate hearingDate = LocalDate.of(2018, 6, 26);


        final DigitalResponse tooYoungJuror = new DigitalResponse();
        tooYoungJuror.setJurorNumber(youngJurorNumber);
        tooYoungJuror.setDateOfBirth(hearingDate.minusYears(18));

        when(mockPoolRepository.findByJurorJurorNumber(anyString()))
            .thenReturn(JurorPool.builder()
                .nextDate(hearingDate)
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isFalse();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_50_years_old() {
        final String youngJurorNumber = "123456000";
        final LocalDate hearingDate = LocalDate.of(2018, 6, 26);

        final DigitalResponse tooYoungJuror = new DigitalResponse();
        tooYoungJuror.setJurorNumber(youngJurorNumber);
        tooYoungJuror.setDateOfBirth(hearingDate.minusYears(50));
        given(mockPoolRepository.findByJurorJurorNumber(anyString()))
            .willReturn(JurorPool.builder()
                .nextDate(hearingDate)
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isFalse();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_too_old_exactly_76() {
        final String youngJurorNumber = "123456000";
        final LocalDate hearingDate = LocalDate.of(2018, 6, 26);

        final DigitalResponse tooYoungJuror = new DigitalResponse();
        tooYoungJuror.setJurorNumber(youngJurorNumber);
        tooYoungJuror.setDateOfBirth(hearingDate.minusYears(76));
        given(mockPoolRepository.findByJurorJurorNumber(anyString()))
            .willReturn(JurorPool.builder()
                .juror(Juror.builder().jurorNumber(TestConstants.VALID_JUROR_NUMBER).build())
                .nextDate(hearingDate)
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isTrue();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_UPPER_SP_ID);
    }

    @Test
    public void isJurorAgeDisqualified_1_day_before_76th_birthday() {
        final String youngJurorNumber = "123456000";
        final LocalDate hearingDate = LocalDate.of(2018, 6, 26);


        final DigitalResponse tooYoungJuror = new DigitalResponse();
        tooYoungJuror.setJurorNumber(youngJurorNumber);
        tooYoungJuror.setDateOfBirth(hearingDate.minusYears(76).plusDays(1)
            .plusDays(1));

        given(mockPoolRepository.findByJurorJurorNumber(anyString()))
            .willReturn(JurorPool.builder()
                .juror(Juror.builder().jurorNumber(TestConstants.VALID_JUROR_NUMBER).build())
                .nextDate(hearingDate)
                .build());

        assertThat(inspector.isJurorAgeDisqualified(tooYoungJuror)).isFalse();

        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_LOWER_SP_ID);
        verify(mockSystemParameterRepository, atLeastOnce()).findById(AGE_UPPER_SP_ID);
    }

    @Test
    public void isIneligible_fp() {
        final String jurorNumber = "123555666";

        final DigitalResponse jurorResponseResidency = new DigitalResponse();
        jurorResponseResidency.setJurorNumber(jurorNumber);
        jurorResponseResidency.setResidency(Boolean.FALSE);

        assertThat(inspector.responseType(jurorResponseResidency)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);


        final DigitalResponse jurorResponseBail = new DigitalResponse();
        jurorResponseBail.setJurorNumber(jurorNumber);
        jurorResponseBail.setBail(Boolean.TRUE);

        assertThat(inspector.responseType(jurorResponseBail)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);


        final DigitalResponse jurorResponseMental = new DigitalResponse();
        jurorResponseMental.setJurorNumber(jurorNumber);
        jurorResponseMental.setMentalHealthAct(Boolean.TRUE);

        assertThat(inspector.responseType(jurorResponseMental)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);


        final DigitalResponse jurorResponseConvictions = new DigitalResponse();
        jurorResponseConvictions.setJurorNumber(jurorNumber);
        jurorResponseConvictions.setConvictions(Boolean.TRUE);

        assertThat(inspector.responseType(jurorResponseConvictions)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);


        final DigitalResponse jurorResponseCjs = new DigitalResponse();
        jurorResponseCjs.setJurorNumber(jurorNumber);
        //  jurorResponseCjs.setCjsEmployments(Lists.newArrayList(<JurorResponseCjsEmploymentesponseCJS> CJSEmployment)
        //          .employer("Police")

        assertThat(inspector.responseType(jurorResponseCjs)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Test
    public void isIneligible_3p() {
        final String jurorNumber = "555888999";

        final DigitalResponse thirdPartyResidency = new DigitalResponse();
        thirdPartyResidency.setJurorNumber(jurorNumber);
        thirdPartyResidency.setThirdPartyFName("Bob");
        thirdPartyResidency.setResidency(Boolean.FALSE);

        assertThat(inspector.responseType(thirdPartyResidency)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyResidency)).isTrue();


        final DigitalResponse thirdPartyBail = new DigitalResponse();
        thirdPartyBail.setJurorNumber(jurorNumber);
        thirdPartyBail.setThirdPartyFName("Bob");
        thirdPartyBail.setBail(Boolean.TRUE);
        assertThat(inspector.responseType(thirdPartyBail)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyBail)).isTrue();


        final DigitalResponse thirdPartyMental = new DigitalResponse();
        thirdPartyMental.setJurorNumber(jurorNumber);
        thirdPartyMental.setThirdPartyFName("Bob");
        thirdPartyMental.setMentalHealthAct(Boolean.TRUE);
        assertThat(inspector.responseType(thirdPartyMental)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyMental)).isTrue();


        final DigitalResponse thirdPartyConvictions = new DigitalResponse();
        thirdPartyConvictions.setJurorNumber(jurorNumber);
        thirdPartyConvictions.setThirdPartyFName("Bob");
        thirdPartyConvictions.setConvictions(Boolean.TRUE);
        assertThat(inspector.responseType(thirdPartyConvictions)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyConvictions)).isTrue();


        final DigitalResponse thirdPartyCjs = new DigitalResponse();
        thirdPartyCjs.setJurorNumber(jurorNumber);
        thirdPartyCjs.setThirdPartyFName("Bob");
        // JurorResponseCjsEmployment cjs = new JurorResponseCjsEmployment();
        // thirdPartyCjs.setCjsEmployments(Lists.newArrayList(<cjs>));
        // thirdPartyCjs.setCjsEmployments("Police") ;

        assertThat(inspector.responseType(thirdPartyCjs)).isEqualTo(NotifyTemplateType.STRAIGHT_THROUGH);
        assertThat(inspector.isThirdPartyResponse(thirdPartyCjs)).isTrue();
    }

    @Test
    public void isDeferral_fp() {
        final String jurorNumber = "123555666";

        final DigitalResponse jurorResponseFirstPersonDeferral = new DigitalResponse();
        jurorResponseFirstPersonDeferral.setJurorNumber(jurorNumber);
        jurorResponseFirstPersonDeferral.setDeferralReason("Defer me");

        assertThat(inspector.responseType(jurorResponseFirstPersonDeferral)).isEqualTo(NotifyTemplateType.DEFERRAL);
    }

    @Test
    public void isDeferral_3p() {
        final String jurorNumber = "555888999";

        final DigitalResponse thirdPartyDeferral = new DigitalResponse();
        thirdPartyDeferral.setJurorNumber(jurorNumber);
        thirdPartyDeferral.setThirdPartyFName("Bob");
        thirdPartyDeferral.setDeferralReason("Defer me");

        assertThat(inspector.responseType(thirdPartyDeferral)).isEqualTo(NotifyTemplateType.DEFERRAL);
        assertThat(inspector.isThirdPartyResponse(thirdPartyDeferral)).isTrue();
    }

    @Test
    public void isExcusal_fp() {
        final String jurorNumber = "123555666";
        final DigitalResponse jurorResponseFirstPersonExcusal = new DigitalResponse();
        jurorResponseFirstPersonExcusal.setJurorNumber(jurorNumber);
        jurorResponseFirstPersonExcusal.setExcusalReason("Excuse me");
        assertThat(inspector.responseType(jurorResponseFirstPersonExcusal)).isEqualTo(NotifyTemplateType.EXCUSAL);
    }

    @Test
    public void isExcusal_3p() {
        final String jurorNumber = "555888999";

        final DigitalResponse thirdPartyExcusal = new DigitalResponse();
        thirdPartyExcusal.setJurorNumber(jurorNumber);
        thirdPartyExcusal.setThirdPartyFName("Bob");
        thirdPartyExcusal.setExcusalReason("Excuse me");

        assertThat(inspector.responseType(thirdPartyExcusal)).isEqualTo(NotifyTemplateType.EXCUSAL);
        assertThat(inspector.isThirdPartyResponse(thirdPartyExcusal)).isTrue();
    }

    @Test
    public void isJurorDeceased() {
        final String jurorNumber = "555888999";
        final DigitalResponse thirdPartyDeceased = new DigitalResponse();
        thirdPartyDeceased.setJurorNumber(jurorNumber);
        thirdPartyDeceased.setThirdPartyFName("Bob");
        thirdPartyDeceased.setThirdPartyReason(ResponseInspectorImpl.DECEASED);
        thirdPartyDeceased.setDateOfBirth(LocalDate.of(1970, 6, 13));
        thirdPartyDeceased.setResidency(Boolean.TRUE);
        thirdPartyDeceased.setMentalHealthAct(Boolean.FALSE);
        thirdPartyDeceased.setBail(Boolean.FALSE);
        thirdPartyDeceased.setConvictions(Boolean.FALSE);

        assertThat(inspector.responseType(thirdPartyDeceased)).isEqualTo(NotifyTemplateType.EXCUSAL_DECEASED);
        assertThat(inspector.isThirdPartyResponse(thirdPartyDeceased)).isTrue();
        assertThat(inspector.isJurorDeceased(thirdPartyDeceased)).isTrue();
    }

    @Test
    public void getYoungestJurorAgeAllowed() {
        //given(mockSystemParameterRepository.findById(AGE_LOWER_SP_ID)).willReturn(SystemParameter.builder()
        //.spValue("99")
        //.build());

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
        final LocalDate hearing02_01_2018 = LocalDate.of(2018, 1, 2);
        final LocalDate oneDayBefore18 = LocalDate.of(2000, 1, 1);
        final LocalDate exactly18 = LocalDate.of(2000, 1, 2);
        final LocalDate oneDayAfter18 = LocalDate.of(2000, 1, 3);

        assertThat(inspector.getJurorAgeAtHearingDate(oneDayBefore18, hearing02_01_2018))
            .as("Juror is 18 the day before the hearing")
            .isEqualTo(18);

        assertThat(inspector.getJurorAgeAtHearingDate(exactly18, hearing02_01_2018))
            .as("Juror is 18 on the hearing date.")
            .isEqualTo(18);

        assertThat(inspector.getJurorAgeAtHearingDate(oneDayAfter18, hearing02_01_2018))
            .as("Juror is 18 one day after the hearing.")
            .isEqualTo(17);
    }

    @Test
    public void isWelshCourtTrue() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(true);
        final String jurorNumber = "443355577";
        final String title = "Mr";
        final String firstName = "Testy";
        final String lastName = "Jones";
        final String email = "testy.jones@cgi.com";

        final CourtLocation court = new CourtLocation();
        court.setLocCode("457");

        final WelshCourtLocation welshCourt = new WelshCourtLocation();
        welshCourt.setLocCode("457");

        final JurorPool pool = new JurorPool();
        Juror juror = new Juror();
        pool.setJuror(juror);
        pool.getJuror().setJurorNumber(jurorNumber);
        pool.getJuror().setTitle(title);
        pool.getJuror().setFirstName(firstName);
        pool.getJuror().setLastName(lastName);
        pool.getJuror().setEmail(email);
        pool.getJuror().setWelsh(true);
        PoolRequest poolRequest = new PoolRequest();
        pool.setPool(poolRequest);
        pool.getPool().setCourtLocation(court);

        final DigitalResponse jurorResponseEnglishLanguageDefault = new DigitalResponse();
        jurorResponseEnglishLanguageDefault.setJurorNumber(jurorNumber);
        jurorResponseEnglishLanguageDefault.setWelsh(true);

        given(mockPoolRepository.findByJurorJurorNumber(anyString())).willReturn(pool);

        given(welshCourtLocationRepository.findByLocCode(anyString())).willReturn(welshCourt);

        assertThat(inspector.isWelshCourt(jurorResponseEnglishLanguageDefault)).isTrue();

        verify(mockAppSettingService, times(1)).isWelshEnabled();
    }

    @Test
    public void isWelshCourtFalse() {
        given(mockAppSettingService.isWelshEnabled()).willReturn(true);
        final String jurorNumber = "443355578";
        final String title = "Mr";
        final String firstName = "Jones";
        final String lastName = "Testy";
        final String email = "testy.jones@cgi.com";

        final CourtLocation court = new CourtLocation();
        court.setLocCode("417");

        final JurorPool pool = new JurorPool();
        Juror juror = new Juror();
        pool.setJuror(juror);
        pool.getJuror().setJurorNumber(jurorNumber);
        pool.getJuror().setTitle(title);
        pool.getJuror().setFirstName(firstName);
        pool.getJuror().setLastName(lastName);
        pool.getJuror().setEmail(email);
        pool.getJuror().setWelsh(true);
        PoolRequest poolRequest = new PoolRequest();
        pool.setPool(poolRequest);
        pool.getPool().setCourtLocation(court);


        final DigitalResponse jurorResponseEnglishLanguageDefault = new DigitalResponse();
        jurorResponseEnglishLanguageDefault.setJurorNumber(jurorNumber);
        jurorResponseEnglishLanguageDefault.setWelsh(true);


        given(mockPoolRepository.findByJurorJurorNumber(anyString())).willReturn(pool);

        given(welshCourtLocationRepository.findByLocCode(anyString())).willReturn(null);

        assertThat(inspector.isWelshCourt(jurorResponseEnglishLanguageDefault)).isFalse();

        verify(mockAppSettingService, times(1)).isWelshEnabled();
    }
}
