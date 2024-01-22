package uk.gov.hmcts.juror.api.validation;

import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.bureau.service.AppSettingService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
public class ResponseInspectorImpl implements ResponseInspector {
    static final int AGE_LOWER_SP_ID = 101;
    static final int AGE_UPPER_SP_ID = 100;

    /**
     * Third party reason text for a deceased response.
     */
    public static final String DECEASED = "deceased";
    private final SystemParameterRepository systemParameterRepository;
    private final PoolRepository poolRepository;
    private final WelshCourtLocationRepository welshCourtLocRepository;
    private final AppSettingService appSettingService;

    @Autowired
    public ResponseInspectorImpl(final SystemParameterRepository systemParameterRepository,
                                 final PoolRepository poolRepository,
                                 final AppSettingService appSettingService,
                                 final WelshCourtLocationRepository welshCourtLocRepository) {
        Assert.notNull(systemParameterRepository, "SystemParameterRepository cannot be null.");
        Assert.notNull(poolRepository, "PoolRepository cannot be null.");
        Assert.notNull(appSettingService, "AppSettingService cannot be null.");
        Assert.notNull(welshCourtLocRepository, "WelshCourtLocationRepository cannot be null.");
        this.systemParameterRepository = systemParameterRepository;
        this.poolRepository = poolRepository;
        this.appSettingService = appSettingService;
        this.welshCourtLocRepository = welshCourtLocRepository;
    }

    @Override
    public boolean isThirdPartyResponse(@NonNull final JurorResponse r) {
        boolean isThirdPartyResponse = !Strings.isNullOrEmpty(r.getThirdPartyFName());
        if (log.isDebugEnabled()) {
            log.debug("Third party response: {}", isThirdPartyResponse);
        }
        return isThirdPartyResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean hasAdjustments(@NonNull final JurorResponse r) {
        if ((r.getSpecialNeeds() != null && !r.getSpecialNeeds().isEmpty())
            || !Strings.isNullOrEmpty(r.getSpecialNeedsArrangements())) {
            log.debug("Response {} has reasonable adjustments", r.getJurorNumber());
            return true;
        }

        log.trace("Response {} does not have reasonable adjustments", r.getJurorNumber());
        return false;
    }

    @Override
    public boolean isWelshLanguage(@NonNull final JurorResponse r) {
        if (appSettingService.isWelshEnabled()) {
            if (BooleanUtils.isTrue(r.getWelsh())) {
                log.debug("Juror response {} is Welsh language response.", r.getJurorNumber());
                return true;
            }
        }
        return false;
    }

    @Override
    public String activeContactEmail(@NonNull final JurorResponse r) {
        if (isThirdPartyResponse(r)) {
            if (BooleanUtils.isFalse(r.getJurorEmailDetails())) {
                log.debug("Active contact email is third party.");
                return r.getEmailAddress();
            }
        }
        log.debug("Active contact email is juror.");
        return r.getEmail();
    }

    @Override
    public NotifyTemplateType responseType(@NonNull final JurorResponse r) {
        final String jurorNumber = r.getJurorNumber();

        // fail fast
        if (isJurorDeceased(r)) {
            log.debug("{} Excusal deceased", jurorNumber);
            return NotifyTemplateType.EXCUSAL_DECEASED;
        }

        if (isJurorAgeDisqualified(r)) {
            log.debug("{} Disqualification age", jurorNumber);
            return NotifyTemplateType.DISQUALIFICATION_AGE;
        }

        if (isExcusal(r)) {
            log.debug("{} Excusal", jurorNumber);
            return NotifyTemplateType.EXCUSAL;
        }
        if (isDeferral(r)) {
            log.debug("{} Deferral", jurorNumber);
            return NotifyTemplateType.DEFERRAL;
        }

        // is now a straight through since other types have been checked.
        log.warn("{} Acceptance");
        return NotifyTemplateType.STRAIGHT_THROUGH;//default value
    }

    @Override
    public boolean isJurorAgeDisqualified(final JurorResponse r) {
        try {
            final Pool p = poolRepository.findByJurorNumber(r.getJurorNumber());
            int age = getJurorAgeAtHearingDate(r.getDateOfBirth(), p.getHearingDate());
            if (log.isTraceEnabled()) {
                log.trace(
                    "Juror DOB {} at hearing date {} will be {} years old",
                    r.getDateOfBirth(),
                    p.getHearingDate(),
                    age
                );
            }
            if (age < getYoungestJurorAgeAllowed()) {
                log.info("Juror {} too young for straight through as they are younger than {} on summon date",
                    p.getJurorNumber(), getYoungestJurorAgeAllowed()
                );
                return true;
            } else if (age >= getTooOldJurorAge()) {
                log.info(
                    "Juror {} too old for straight through as they are {} or older on summon date",
                    p.getJurorNumber(),
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
    public boolean isIneligible(final JurorResponse r) {
        if (BooleanUtils.isFalse(r.getResidency())) {
            log.debug("Response {} ineligible - Residency", r.getJurorNumber());
            return true;
        }

        if (BooleanUtils.isTrue(r.getBail())) {
            log.debug("Response {} ineligible - Residency", r.getJurorNumber());
            return true;
        }

        if (BooleanUtils.isTrue(r.getMentalHealthAct())) {
            log.debug("Response {} ineligible - Mental Health Act", r.getJurorNumber());
            return true;
        }

        if (BooleanUtils.isTrue(r.getConvictions())) {
            log.debug("Response {} ineligible - Convictions", r.getJurorNumber());
            return true;
        }

        if ((r.getCjsEmployments() != null && !r.getCjsEmployments().isEmpty())) {
            log.debug("Response {} ineligible - CJS employments", r.getJurorNumber());
            return true;
        }

        log.trace("Response {} is eligible for jury service", r.getJurorNumber());
        return false;
    }

    @Override
    public boolean isDeferral(final JurorResponse r) {
        return !Strings.isNullOrEmpty(r.getDeferralReason());
    }

    @Override
    public boolean isExcusal(final JurorResponse r) {
        return !Strings.isNullOrEmpty(r.getExcusalReason());
    }

    @Override
    public boolean isJurorDeceased(final JurorResponse r) {
        return !Strings.isNullOrEmpty(r.getThirdPartyReason()) && DECEASED.equalsIgnoreCase(r.getThirdPartyReason());
    }

    @Override
    public int getJurorAgeAtHearingDate(final Date birthDate, final Date hearingDate) throws IllegalArgumentException {
        if (birthDate == null || hearingDate == null) {
            log.warn("Cannot compare null dates!");
            throw new IllegalArgumentException("Birth Date and Hearing Date cannot be null");
        }
        final LocalDate localBirthDate = birthDate.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();
        final LocalDate localHearingDate = hearingDate.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();

        return Period.between(localBirthDate, localHearingDate).getYears();
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
    public int getPoolNotification(final JurorResponse r) {
        try {
            final Pool p = poolRepository.findByJurorNumber(r.getJurorNumber());
            return p.getNotifications();
        } catch (Exception e) {
            log.error("Failed to retrieve the pool.notification value for this juror response.", e);
            return -1;
        }
    }

    @Override
    public boolean isWelshCourt(final JurorResponse r) {
        boolean courtIsWelsh = false;
        try {

            final Pool p = poolRepository.findByJurorNumber(r.getJurorNumber());
            if (isWelshLanguage(r) && welshCourtLocRepository.findByLocCode(p.getCourt().getLocCode()) != null) {
                log.debug("Court (locCode) {} is Welsh.", p.getCourt().getLocCode());
                courtIsWelsh = true;
            }
        } catch (Exception e) {
            log.error("Failed to determine the court location.", e);
        }
        return courtIsWelsh;
    }

}
