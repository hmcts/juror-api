package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.PoolRequestUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurySummoningMonitorReportServiceImpl implements JurySummoningMonitorReportService {
    private final JurorPoolRepository jurorPoolRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final PoolRequestRepository poolRequestRepository;

    @Override
    public JurySummoningMonitorReportResponse viewJurySummoningMonitorReport(
        JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest) {

        final boolean isSearchByPool = "POOL".equals(jurySummoningMonitorReportRequest.getSearchBy());

        JurySummoningMonitorReportResponse response = new JurySummoningMonitorReportResponse();

        setupResponseHeaders(isSearchByPool, jurySummoningMonitorReportRequest, response);

        if (isSearchByPool) {
            try {
                String result =
                    jurorPoolRepository.getJsmReportByPool(jurySummoningMonitorReportRequest.getPoolNumber());

                if (result != null && !result.isEmpty()) {
                    List<String> values = List.of(result.split(","));
                    setupByPoolFields(response, values);
                }

            } catch (Exception e) {
                log.error("Error getting jury summoning monitor report by pool", e);
                throw new MojException.InternalServerError("Error getting jury summoning monitor report by pool", e);
            }
        } else {
            // TODO implement the logic to get the report data by court once the function is available

        }
        return response;
    }

    private static void setupByPoolFields(JurySummoningMonitorReportResponse response, List<String> result) {
        response.setTotalJurorsNeeded(Integer.parseInt(result.get(0)));
        response.setBureauDeferralsIncluded(Integer.parseInt(result.get(1)));

        response.setBureauToSupply(response.getTotalJurorsNeeded() - response.getBureauDeferralsIncluded());

        // result.get(2) is not used (disqualified on selection)
        response.setInitiallySummoned(Integer.parseInt(result.get(3)));
        response.setAdditionalSummonsIssued(Integer.parseInt(result.get(4)));
        response.setReminderLettersIssued(Integer.parseInt(result.get(5)));
        response.setTotalConfirmedJurors(Integer.parseInt(result.get(6)));
        response.setExcusalsRefused(Integer.parseInt(result.get(7)));
        response.setDeferralsRefused(Integer.parseInt(result.get(8)));
        response.setDisqualifiedPoliceCheck(Integer.parseInt(result.get(9)));

        // includes disqualified on selection
        response.setDisqualifiedOther(Integer.parseInt(result.get(10)));

        // result.get(11) is not used - count of currently deferred jurors (active)
        // result.get(12) is not used - count of currently postponed jurors (active)
        response.setNonResponded(Integer.parseInt(result.get(13)));
        response.setUndeliverable(Integer.parseInt(result.get(14)));
        response.setTotalUnavailable(Integer.parseInt(result.get(15)));

        // set all the excusal reasons
        response.setMovedFromArea(Integer.parseInt(result.get(16)));
        response.setStudent(Integer.parseInt(result.get(17)));
        response.setChildcare(Integer.parseInt(result.get(18)));
        response.setDeceased(Integer.parseInt(result.get(19)));
        response.setForces(Integer.parseInt(result.get(20)));
        response.setFinancialHardship(Integer.parseInt(result.get(21)));
        response.setIll(Integer.parseInt(result.get(22)));
        response.setExcusedByBureau(Integer.parseInt(result.get(23)));
        response.setCriminalRecord(Integer.parseInt(result.get(24)));
        response.setLanguageDifficulties(Integer.parseInt(result.get(25)));
        response.setMedical(Integer.parseInt(result.get(26)));
        response.setMentalHealth(Integer.parseInt(result.get(27)));
        response.setOther(Integer.parseInt(result.get(28)));
        response.setPostponementOfService(Integer.parseInt(result.get(29)));
        response.setReligiousReasons(Integer.parseInt(result.get(30)));
        response.setRecentlyServed(Integer.parseInt(result.get(31)));
        response.setTravellingDifficulties(Integer.parseInt(result.get(32)));
        response.setWorkRelated(Integer.parseInt(result.get(33)));
        response.setCarer(Integer.parseInt(result.get(34)));
        response.setHoliday(Integer.parseInt(result.get(35)));
        response.setBereavement(Integer.parseInt(result.get(36)));
        response.setCjsEmployment(Integer.parseInt(result.get(37)));
        response.setDeferredByCourt(Integer.parseInt(result.get(38)));
        response.setPersonalEngagement(Integer.parseInt(result.get(39)));
        // result.get(40) is not used - not listed above
        // end of excusal reasons

        response.setExcused(response.getTotalExcused());

        response.setAwaitingInformation(Integer.parseInt(result.get(41)));

        // all deferrals regardless of current juror status
        response.setDeferred(Integer.parseInt(result.get(42)));
        // all postponements regardless of current juror status
        response.setPostponed(Integer.parseInt(result.get(43)));

        if (response.getTotalJurorsNeeded() - response.getBureauDeferralsIncluded() > 0) {
            response.setRatio(
                (double) response.getInitiallySummoned()
                    / (response.getTotalJurorsNeeded() - response.getBureauDeferralsIncluded()));
        } else {
            response.setRatio(0.0);
        }
    }

    private void setupResponseHeaders(boolean isSearchByPool,
                                      JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest,
                                      JurySummoningMonitorReportResponse response) {
        if (isSearchByPool) {

            String poolNumber = jurySummoningMonitorReportRequest.getPoolNumber();

            log.info("User {} viewing jury summoning monitor report by pool for pool number: {}",
                SecurityUtil.getActiveLogin(),
                poolNumber);

            PoolRequest poolRequest = PoolRequestUtils.getActivePoolRecord(poolRequestRepository, poolNumber);

            response.setHeadings(
                getSearchByPoolHeaders(poolRequest.getCourtLocation(), poolNumber, poolRequest.getPoolType(),
                    poolRequest.getReturnDate()));

        } else {
            log.info("User {} viewing jury summoning monitor report by court",
                SecurityUtil.getActiveLogin());

            if (!jurySummoningMonitorReportRequest.isAllCourts()
                && (jurySummoningMonitorReportRequest.getCourtLocCodes() == null
                || jurySummoningMonitorReportRequest.getCourtLocCodes().isEmpty())) {
                throw new MojException.BadRequest("No court locations provided", null);
            }

            response.setHeadings(getSearchByCourts(jurySummoningMonitorReportRequest.getCourtLocCodes(),
                jurySummoningMonitorReportRequest.isAllCourts(),
                jurySummoningMonitorReportRequest.getFromDate(),
                jurySummoningMonitorReportRequest.getToDate()));
        }
    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getSearchByPoolHeaders(CourtLocation court, String poolNumber, PoolType poolType,
                           LocalDate serviceStartDate) {

        String courtName = court.getName() + " (" + court.getLocCode() + ")";
        return Map.of(
            "court", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURT.getDisplayName())
                .dataType(ReportHeading.COURT.getDataType())
                .value(courtName)
                .build(),
            "pool_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.POOL_NUMBER.getDisplayName())
                .dataType(ReportHeading.POOL_NUMBER.getDataType())
                .value(poolNumber)
                .build(),
            "pool_type", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.POOL_TYPE.getDisplayName())
                .dataType(ReportHeading.POOL_TYPE.getDataType())
                .value(poolType.getDescription())
                .build(),
            "service_start_date", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.SERVICE_START_DATE.getDisplayName())
                .dataType(ReportHeading.SERVICE_START_DATE.getDataType())
                .value(serviceStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build()
        );
    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getSearchByCourts(List<String> courtsList, boolean allCourts, LocalDate reportFrom, LocalDate reportTo) {

        String courts = "All courts";
        if (!allCourts) {

            StringBuilder courtsBuilder = new StringBuilder();

            courtLocationRepository.findByLocCodeIn(courtsList).stream().forEach(c -> {
                courtsBuilder.append(c.getName()).append(" (").append(c.getLocCode()).append(')').append(", ");
            });

            // remove the trailing comma if we have any courts
            if (courtsBuilder.length() > 2) {
                courtsBuilder.deleteCharAt(courtsBuilder.length() - 2);
            }
            courts = courtsBuilder.toString().trim();
        }

        return Map.of(
            "courts", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURTS.getDisplayName())
                .dataType(ReportHeading.COURTS.getDataType())
                .value(courts)
                .build(),
            "date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_FROM.getDisplayName())
                .dataType(ReportHeading.DATE_FROM.getDataType())
                .value(reportFrom.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_TO.getDisplayName())
                .dataType(ReportHeading.DATE_TO.getDataType())
                .value(reportTo.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build()
        );
    }


    public enum ReportHeading {
        POOL_NUMBER("Pool number", String.class.getSimpleName()),
        DATE_FROM("Date from", LocalDate.class.getSimpleName()),
        DATE_TO("Date to", LocalDate.class.getSimpleName()),
        SERVICE_START_DATE("Service start date", LocalDate.class.getSimpleName()),
        REPORT_CREATED("Report created", LocalDate.class.getSimpleName()),
        TIME_CREATED("Time created", LocalDateTime.class.getSimpleName()),
        POOL_TYPE("Pool type", String.class.getSimpleName()),
        COURT("Court", String.class.getSimpleName()),
        COURTS("Courts", String.class.getSimpleName());

        private String displayName;

        private String dataType;

        ReportHeading(String displayName, String dataType) {
            this.displayName = displayName;
            this.dataType = dataType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDataType() {
            return dataType;
        }
    }
}
