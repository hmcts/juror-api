package uk.gov.hmcts.juror.api.bureau.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;
import uk.gov.hmcts.juror.api.bureau.domain.AppSettingRepository;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Set the urgency flags on a {@link BureauJurorDetail}.
 */
@Component
@Setter
@Slf4j
public class UrgencyServiceImpl implements UrgencyService {
    /**
     * Processing status "closed"
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
     * @param details {@link BureauJurorDetail}
     */
    @Override
    public List<BureauJurorDetail> flagSlaOverdueFromList(List<BureauJurorDetail> details) {
        final List<BureauJurorDetail> processedDetails = new LinkedList<>();
        details.forEach(bureauJurorDetail -> processedDetails.add(flagSlaOverdueForResponse(bureauJurorDetail)));

        return processedDetails;
    }

    /**
     * Set the urgency levels for a single bureauJurorDetail response.
     *
     * @param bureauJurorDetail Fully attached response to set the urgency flags upon.
     * @throws AppSettingException Failed to find a value for the application setting in persistence.
     * @implNote CJ7820-885 for business rules on urgency thresholds
     */
    @Override
    @SuppressWarnings("Duplicates")
    public BureauJurorDetail flagSlaOverdueForResponse(final BureauJurorDetail bureauJurorDetail) {
        //SLA period for responses in days.
        Integer workingDays;

//        Optional<AppSetting> optSlaOverdueDays = appSettingRepository.findById(SLA_OVERDUE_DAYS);
//        final AppSetting slaOverdueDays = optSlaOverdueDays.isPresent() ? optSlaOverdueDays.get() : null;

        final AppSetting slaOverdueDays = appSettingRepository.findById(SLA_OVERDUE_DAYS).get();
        // check the SLA_OVERDUE_DAYS application setting
        if (null != slaOverdueDays && !ObjectUtils.isEmpty(slaOverdueDays)
            && !ObjectUtils.isEmpty(slaOverdueDays.getValue())) {
            if (log.isTraceEnabled()) {
                log.trace("{}={}", SLA_OVERDUE_DAYS, slaOverdueDays.getValue());
            }

            workingDays = Integer.valueOf(slaOverdueDays.getValue());
        } else {
            log.error("Application setting {} was null or empty!", SLA_OVERDUE_DAYS);
            throw new AppSettingException("Application setting " + SLA_OVERDUE_DAYS + " was null or empty!");
        }

        if (log.isTraceEnabled()) {
            log.trace("Processing urgency for {}", bureauJurorDetail);
        }

        final String processingStatus =
            bureauJurorDetail.getProcessingStatus() != null
                ?
                bureauJurorDetail.getProcessingStatus()
                :
                    "";
        try {
            final LocalDateTime dateReceived = LocalDateTime.ofInstant(
                bureauJurorDetail.getDateReceived().toInstant(),
                ZoneId.systemDefault()
            );

            log.trace("Date received:   {}", dateReceived);

            if (bureauJurorDetail.getHearingDate() != null) {
                final LocalDateTime courtDate = LocalDateTime.ofInstant(
                    bureauJurorDetail.getHearingDate().toInstant(),
                    ZoneId.systemDefault()
                );
                log.trace("Court date:      {}", courtDate);
            }

            // sla overdue
            final LocalDateTime slaBreachWindowStart = addWorkingDays(dateReceived, workingDays - 1);
            log.trace("SLA breach from: {}", slaBreachWindowStart);

            if (!processingStatus.equalsIgnoreCase(CLOSED)
                &&                // not closed
                slaBreachWindowStart.isBefore(LocalDateTime.now())) {  // window start has happened
                bureauJurorDetail.setSlaOverdue(Boolean.TRUE);
                log.debug("SLA overdue");
            }

            log.debug("Processing complete.");
            return bureauJurorDetail;
        } catch (NullPointerException npe) {
            log.error(JUROR_RESPONSE_FOR_JUROR_NUMBER_HAS_INVALID_DATE_INFORMATION_ERROR);
            if (log.isTraceEnabled()) {
                log.trace(
                    JUROR_RESPONSE_FOR_JUROR_NUMBER_HAS_INVALID_DATE_INFORMATION_TRACE,
                    bureauJurorDetail.getJurorNumber(),
                    npe
                );
            }
            return bureauJurorDetail;
        }
    }

    @Override
    public LocalDateTime fridayCutOff(LocalDateTime jurySlotDay) {
        final LocalDateTime firstFridayBefore = jurySlotDay.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        final LocalDateTime cutOff = firstFridayBefore.minus(1, ChronoUnit.WEEKS);
        if (log.isTraceEnabled()) {
            log.trace("First Friday before cutoff: {}", firstFridayBefore);
            log.trace("Cut off date:               {}", cutOff);
        }
        return cutOff;
    }

    @Override
    public LocalDateTime addWorkingDays(final LocalDateTime date, final Integer workingDays) {
        if (log.isTraceEnabled()) {
            log.trace("Adding {} days to {}", workingDays, date);
        }

        if (workingDays < 1) {
            log.debug("Not adding {} days.", workingDays);
            return date;
        }

        LocalDateTime modifiedDate = date;
        int addedDays = 0;
        while (addedDays < workingDays) {
            modifiedDate = modifiedDate.plusDays(1);
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
            modifiedDate = modifiedDate.minusDays(1);
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

    public boolean isSuperUrgent(JurorResponse response, Pool poolDetails) {
        try {
            if (!CLOSED.equalsIgnoreCase(response.getProcessingStatus().getDescription())
                && poolDetails.getReadOnly()) {
                log.trace("isSuperUrgent: Super urgent");
                return Boolean.TRUE;
            } else {
                log.trace("isSuperUrgent: Not super urgent");
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
            return Boolean.FALSE;
        }

    }


    @SuppressWarnings("Duplicates")
    private Integer getCutOffDays() {

        Integer cutOffDays;

        Optional<AppSetting> optUrgencyDays = appSettingRepository.findById(URGENCY_DAYS);
        final AppSetting urgencyDays = optUrgencyDays.isPresent()
            ?
            optUrgencyDays.get()
            :
                null;

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
    public boolean isUrgent(JurorResponse response, Pool poolDetails) {
        //Responses not closed and received within this many <b>working days</b> are urgent.

        try {
            final LocalDateTime courtDate = LocalDateTime.ofInstant(
                poolDetails.getHearingDate().toInstant(),
                ZoneId.systemDefault()
            );
            final LocalDateTime cutOffDaysBeforeCourtDate = subtractWorkingDays(
                fridayCutOff(courtDate),
                getCutOffDays()
            );

            if (!CLOSED.equalsIgnoreCase(response.getProcessingStatus().getDescription())
                && !poolDetails.getReadOnly()
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
    public void setUrgencyFlags(JurorResponse response, Pool poolDetails) {
        response.setUrgent(isUrgent(response, poolDetails));
        response.setSuperUrgent(isSuperUrgent(response, poolDetails));
        if (log.isTraceEnabled()) {
            log.trace("Response {} Urgency flags updated: urgent={} super={}", response.getJurorNumber(),
                response.getUrgent(), response.getSuperUrgent()
            );
        }
    }


    public static class AppSettingException extends RuntimeException {
        public AppSettingException(String s) {
        }
    }
}
