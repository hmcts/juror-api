package uk.gov.hmcts.juror.api.validation;

import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseInspectorImpl implements ResponseInspector {
    static final int AGE_LOWER_SP_ID = 101;
    static final int AGE_UPPER_SP_ID = 100;

    /**
     * Third party reason text for a deceased response.
     */
    public static final String DECEASED = "deceased";
    private final SystemParameterRepository systemParameterRepository;

    private final JurorRepository jurorRepository;

    private final WelshCourtLocationRepository welshCourtLocRepository;
    private final AppSettingService appSettingService;
    private final JurorPoolService jurorPoolService;

    @Override
    public boolean isThirdPartyResponse(@NonNull final DigitalResponse response) {
        boolean isThirdPartyResponse = !Strings.isNullOrEmpty(response.getThirdPartyFName());
        if (log.isDebugEnabled()) {
            log.debug("Third party response: {}", isThirdPartyResponse);
        }
        return isThirdPartyResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean hasAdjustments(@NonNull final DigitalResponse response) {
        if ((response.getReasonableAdjustments() != null && !response.getReasonableAdjustments().isEmpty())
            || !Strings.isNullOrEmpty(response.getReasonableAdjustmentsArrangements())) {
            log.debug("Response {} has reasonable adjustments", response.getJurorNumber());
            return true;
        }

        log.trace("Response {} does not have reasonable adjustments", response.getJurorNumber());
        return false;
    }

    @Override
    public boolean isWelshLanguage(@NonNull final DigitalResponse response) {
        if (appSettingService.isWelshEnabled()) {
            if (BooleanUtils.isTrue(response.getWelsh())) {
                log.debug("Juror response {} is Welsh language response.", response.getJurorNumber());
                return true;
            }
        }
        return false;
    }

    @Override
    public String activeContactEmail(@NonNull final DigitalResponse response) {
        if (isThirdPartyResponse(response)) {
            if (BooleanUtils.isFalse(response.getJurorEmailDetails())) {
                log.debug("Active contact email is third party.");
                return response.getEmailAddress();
            }
        }
        log.debug("Active contact email is juroresponse.");
        return response.getEmail();
    }

    @Override
    public NotifyTemplateType responseType(@NonNull final DigitalResponse response) {
        final String jurorNumber = response.getJurorNumber();

        // fail fast
        if (isJurorDeceased(response)) {
            log.debug("{} Excusal deceased", jurorNumber);
            return NotifyTemplateType.EXCUSAL_DECEASED;
        }

        if (isJurorAgeDisqualified(response)) {
            log.debug("{} Disqualification age", jurorNumber);
            return NotifyTemplateType.DISQUALIFICATION_AGE;
        }

        if (isExcusal(response)) {
            log.debug("{} Excusal", jurorNumber);
            return NotifyTemplateType.EXCUSAL;
        }
        if (isDeferral(response)) {
            log.debug("{} Deferral", jurorNumber);
            return NotifyTemplateType.DEFERRAL;
        }

        return NotifyTemplateType.STRAIGHT_THROUGH;//default value
    }

    @Override
    public boolean isJurorAgeDisqualified(final DigitalResponse response) {
        try {
            final JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(response.getJurorNumber());
            int age = getJurorAgeAtHearingDate(response.getDateOfBirth(), jurorPool.getNextDate());
            if (log.isTraceEnabled()) {
                log.trace(
                    "Juror DOB {} at hearing date {} will be {} years old",
                    response.getDateOfBirth(),
                    jurorPool.getNextDate(),
                    age
                );
            }
            if (age < getYoungestJurorAgeAllowed()) {
                log.info("Juror {} too young for straight through as they are younger than {} on summon date",
                    jurorPool.getJurorNumber(), getYoungestJurorAgeAllowed()
                );
                return true;
            } else if (age >= getTooOldJurorAge()) {
                log.info(
                    "Juror {} too old for straight through as they are {} or older on summon date",
                    jurorPool.getJurorNumber(),
                    getTooOldJurorAge()
                );
                return true;
            } else {
                log.debug("Juror age within valid range.");
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to calculate juror age at hearing date: {}", e);
            return false;
        }

    }

    @Override
    public boolean isIneligible(final DigitalResponse response) {
        if (BooleanUtils.isFalse(response.getResidency())) {
            log.debug("Response {} ineligible - Residency", response.getJurorNumber());
            return true;
        }

        if (BooleanUtils.isTrue(response.getBail())) {
            log.debug("Response {} ineligible - Residency", response.getJurorNumber());
            return true;
        }

        if (BooleanUtils.isTrue(response.getMentalHealthAct())) {
            log.debug("Response {} ineligible - Mental Health Act", response.getJurorNumber());
            return true;
        }

        if (BooleanUtils.isTrue(response.getConvictions())) {
            log.debug("Response {} ineligible - Convictions", response.getJurorNumber());
            return true;
        }

        if ((response.getCjsEmployments() != null && !response.getCjsEmployments().isEmpty())) {
            log.debug("Response {} ineligible - CJS employments", response.getJurorNumber());
            return true;
        }

        log.trace("Response {} is eligible for jury service", response.getJurorNumber());
        return false;
    }

    @Override
    public boolean isDeferral(final DigitalResponse response) {
        return !Strings.isNullOrEmpty(response.getDeferralReason());
    }

    @Override
    public boolean isExcusal(final DigitalResponse response) {
        return !Strings.isNullOrEmpty(response.getExcusalReason());
    }

    @Override
    public boolean isJurorDeceased(final DigitalResponse response) {
        return !Strings.isNullOrEmpty(response.getThirdPartyReason())
            && DECEASED.equalsIgnoreCase(response.getThirdPartyReason());
    }

    @Override
    public int getJurorAgeAtHearingDate(final LocalDate birthDate,
                                        final LocalDate hearingDate) throws IllegalArgumentException {
        if (birthDate == null || hearingDate == null) {
            log.warn("Cannot compare null dates!");
            throw new IllegalArgumentException("Birth Date and Hearing Date cannot be null");
        }
        return Period.between(birthDate, hearingDate).getYears();
    }

    @Override
    public int getYoungestJurorAgeAllowed() {
        Optional<SystemParameter> optygJurAgeAlwParam = systemParameterRepository.findById(AGE_LOWER_SP_ID);
        final SystemParameter youngestJurorAgeAllowedParameter = optygJurAgeAlwParam.isPresent()
            ?
            optygJurAgeAlwParam.get()
            :
                null;
        int youngestJurorAgeAllowed = 18;
        if (youngestJurorAgeAllowedParameter != null) {
            try {
                youngestJurorAgeAllowed = Integer.parseInt(youngestJurorAgeAllowedParameter.getSpValue());
            } catch (Exception e) {
                log.error("Failed to parse lower age constraint parameter from database: Using default {}",
                    youngestJurorAgeAllowed, e
                );
            }
        }
        return youngestJurorAgeAllowed;
    }

    @Override
    public int getTooOldJurorAge() {
        Optional<SystemParameter> optoldJurAgeParam = systemParameterRepository.findById(AGE_UPPER_SP_ID);
        final SystemParameter tooOldJurorAgeParameter = optoldJurAgeParam.isPresent()
            ?
            optoldJurAgeParam.get()
            :
                null;
        int tooOldJurorAge = 76;
        if (tooOldJurorAgeParameter != null) {
            try {
                tooOldJurorAge = Integer.parseInt(tooOldJurorAgeParameter.getSpValue());
            } catch (Exception e) {
                log.error("Failed to parse upper age constraint parameter from database: Using default {}",
                    tooOldJurorAge, e
                );
            }
        }
        return tooOldJurorAge;
    }

    @Override
    public int getPoolNotification(final DigitalResponse response) {
        try {
            return jurorRepository.findByJurorNumber(response.getJurorNumber()).getNotifications();
        } catch (Exception exception) {
            log.error("Failed to retrieve the pool.notification value for this juror response.", exception);
            return -1;
        }
    }

    @Override
    public boolean isWelshCourt(final DigitalResponse response) {
        boolean courtIsWelsh = false;
        try {
            final JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(response.getJurorNumber());
            if (isWelshLanguage(response)
                && welshCourtLocRepository.findByLocCode(jurorPool.getCourt().getLocCode()) != null) {
                log.debug("Court (locCode) {} is Welsh.", jurorPool.getCourt().getLocCode());
                courtIsWelsh = true;
            }
        } catch (Exception e) {
            log.error("Failed to determine the court location.", e);
        }
        return courtIsWelsh;
    }

}
