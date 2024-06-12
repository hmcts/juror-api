package uk.gov.hmcts.juror.api.bureau.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.repository.AppSettingRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Set the urgency flags on a {@link ModJurorDetail}.
 */
@Component
@Setter
@Getter
@Slf4j
public class UrgencyServiceImpl implements UrgencyService {
    /**
     * Processing status "closed".
     */
    private static final String CLOSED = ProcessingStatus.CLOSED.name();
    private static final String URGENCY_DAYS = "URGENCY_DAYS";
    private static final String SLA_OVERDUE_DAYS = "SLA_OVERDUE_DAYS";
    private static final String JUROR_RESPONSE_FOR_JUROR_NUMBER_HAS_INVALID_DATE_INFORMATION_ERROR = "Juror response "
        + "for Juror number {} has invalid date information!";
    private static final String JUROR_RESPONSE_FOR_JUROR_NUMBER_HAS_INVALID_DATE_INFORMATION_TRACE = "Juror response "
        + "for Juror number {} has invalid date information: {}";

    private final AppSettingRepository appSettingRepository;

    @Autowired
    public UrgencyServiceImpl(final AppSettingRepository appSettingRepository) {
        Assert.notNull(appSettingRepository, "AppSettingRepository cannot be null");
        this.appSettingRepository = appSettingRepository;
    }

    /**
     * Manages the setting of urgency level flags.
     *
     * @param details {@link ModJurorDetail}
     */
    @Override
    public List<ModJurorDetail> flagSlaOverdueFromList(List<ModJurorDetail> details) {
        final List<ModJurorDetail> processedDetails = new LinkedList<>();
        details.forEach(modJurorDetail -> processedDetails.add(flagSlaOverdueForResponse(modJurorDetail)));

        return processedDetails;
    }

    /**
     * Set the urgency levels for a single bureauJurorDetail response.
     *
     * @param modJurorDetail Fully attached response to set the urgency flags upon.
     * @throws AppSettingException Failed to find a value for the application setting in persistence.
     * @implNote CJ7820-885 for business rules on urgency thresholds
     */
    @Override
    @SuppressWarnings("Duplicates")
    public ModJurorDetail flagSlaOverdueForResponse(final ModJurorDetail modJurorDetail) {
        modJurorDetail.setSlaOverdue(slaBreached(
            ProcessingStatus.valueOf(modJurorDetail.getProcessingStatus()),
            modJurorDetail.getDateReceived()
        ));
        return modJurorDetail;
    }

    @Override
    public boolean slaBreached(
        ProcessingStatus processingStatus,
        LocalDate dateReceived
    ) {
        if (dateReceived == null) {
            return false;
        }
        //SLA period for responses in days.
        int workingDays;

        Optional<AppSetting> optSlaOverdueDays = appSettingRepository.findById(SLA_OVERDUE_DAYS);
        // check the SLA_OVERDUE_DAYS application setting
        if (optSlaOverdueDays.isPresent() && !ObjectUtils.isEmpty(optSlaOverdueDays.get().getValue())) {
            if (log.isTraceEnabled()) {
                log.trace("{}={}", SLA_OVERDUE_DAYS, optSlaOverdueDays.get().getValue());
            }
            workingDays = Integer.parseInt(optSlaOverdueDays.get().getValue());
        } else {
            log.error("Application setting {} was null or empty!", SLA_OVERDUE_DAYS);
            throw new AppSettingException("Application setting " + SLA_OVERDUE_DAYS + " was null or empty!");
        }
        // sla overdue
        final LocalDate slaBreachWindowStart = addWorkingDays(dateReceived, workingDays - 1);
        log.trace("SLA breach from: {}", slaBreachWindowStart);

        if (!ProcessingStatus.CLOSED.equals(processingStatus)
            &&                // not closed
            slaBreachWindowStart.isBefore(LocalDate.now())) {  // window start has happened
            log.debug("SLA overdue");
            return true;
        }
        log.debug("Processing complete.");
        return false;
    }

    @Override
    public LocalDateTime fridayCutOff(LocalDateTime jurySlotDay) {
        final LocalDateTime firstFridayBefore = jurySlotDay.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        final LocalDateTime cutOff = firstFridayBefore.minusWeeks(1L);
        if (log.isTraceEnabled()) {
            log.trace("First Friday before cutoff: {}", firstFridayBefore);
            log.trace("Cut off date:               {}", cutOff);
        }
        return cutOff;
    }

    @Override
    public LocalDate addWorkingDays(final LocalDate date, final Integer workingDays) {
        if (log.isTraceEnabled()) {
            log.trace("Adding {} days to {}", workingDays, date);
        }

        if (workingDays < 1) {
            log.debug("Not adding {} days.", workingDays);
            return date;
        }

        LocalDate modifiedDate = date;
        int addedDays = 0;
        while (addedDays < workingDays) {
            modifiedDate = modifiedDate.plusDays(1L);
            if (!(modifiedDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || modifiedDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                addedDays++;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("{} plus {} working days is {}", date, workingDays, modifiedDate);
        }
        return modifiedDate;
    }

    @Override
    public LocalDateTime subtractWorkingDays(final LocalDateTime date, final Integer workingDays) {
        if (log.isTraceEnabled()) {
            log.trace("Subtracting {} days from {}", workingDays, date);
        }

        if (workingDays < 1) {
            log.debug("Not subtracting {} days.", workingDays);
            return date;
        }

        int subtractedDays = 0;
        LocalDateTime modifiedDate = date;
        while (subtractedDays < workingDays) {
            modifiedDate = modifiedDate.minusDays(1L);
            if (!(modifiedDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || modifiedDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                subtractedDays++;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("{} minus {} working days is {}", date, workingDays, modifiedDate);
        }
        return modifiedDate;
    }


    @SuppressWarnings("Duplicates")
    private Integer getCutOffDays() {

        Integer cutOffDays;

        Optional<AppSetting> optUrgencyDays = appSettingRepository.findById(URGENCY_DAYS);
        final AppSetting urgencyDays = optUrgencyDays.orElse(null);

        // check the URGENCY_DAYS application setting
        if (null != urgencyDays && !ObjectUtils.isEmpty(urgencyDays)
            && !ObjectUtils.isEmpty(urgencyDays.getValue())) {
            if (log.isTraceEnabled()) {
                log.trace("{}={}", URGENCY_DAYS, urgencyDays.getValue());
            }

            cutOffDays = Integer.valueOf(urgencyDays.getValue());
        } else {
            log.error("Application setting {} was null or empty!", URGENCY_DAYS);
            throw new AppSettingException("Application setting " + URGENCY_DAYS + " was null or empty!");
        }

        return cutOffDays;
    }


    @SuppressWarnings("Duplicates")
    @Override
    public boolean isUrgent(AbstractJurorResponse response, JurorPool jurorDetails) {
        //Responses not closed and received within this many <b>working days</b> are urgent.

        try {
            final LocalDateTime courtDate = jurorDetails.getNextDate().atStartOfDay();

            final LocalDateTime cutOffDaysBeforeCourtDate = subtractWorkingDays(
                fridayCutOff(courtDate),
                getCutOffDays()
            );

            if (!CLOSED.equalsIgnoreCase(response.getProcessingStatus().getDescription())
                && SecurityUtil.BUREAU_OWNER.equalsIgnoreCase(jurorDetails.getOwner())
                && LocalDateTime.now().isAfter(cutOffDaysBeforeCourtDate)
            ) {
                log.trace("Urgent");
                return Boolean.TRUE;
            } else {
                log.trace("Not urgent");
                return Boolean.FALSE;
            }

        } catch (NullPointerException npe) {
            log.error(JUROR_RESPONSE_FOR_JUROR_NUMBER_HAS_INVALID_DATE_INFORMATION_ERROR);
            if (log.isTraceEnabled()) {
                log.trace(
                    JUROR_RESPONSE_FOR_JUROR_NUMBER_HAS_INVALID_DATE_INFORMATION_TRACE,
                    response.getJurorNumber(),
                    npe
                );
            }
            return false;
        }
    }


    @Override
    public void setUrgencyFlags(AbstractJurorResponse response, JurorPool jurorDetails) {
        response.setUrgent(isUrgent(response, jurorDetails));
        if (log.isTraceEnabled()) {
            log.trace("Response {} Urgency flags updated: urgent={}",
                response.getJurorNumber(),
                response.isUrgent()
            );
        }
    }


    public static class AppSettingException extends RuntimeException {
        public AppSettingException(String message) {
            super(message);
        }
    }
}
