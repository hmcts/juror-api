package uk.gov.hmcts.juror.api.bureau.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsLetterServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsSentToCourtServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsWeeklyInfoServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorDashboardSmartSurveyImportImpl;
import uk.gov.hmcts.juror.api.bureau.service.ScheduledService;
import uk.gov.hmcts.juror.api.juror.service.ExcusedCompletedCourtCommsServiceImpl;
import uk.gov.hmcts.juror.api.juror.service.MessagesServiceImpl;


@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BureauBatchProcessFactory {
    private final JurorCommsLetterServiceImpl jurorCommsLetterService;
    private final JurorCommsWeeklyInfoServiceImpl jurorCommsWeeklyInfoService;
    private final JurorCommsSentToCourtServiceImpl jurorCommsSentToCourtService;
    private final MessagesServiceImpl messagesService;
    private final ExcusedCompletedCourtCommsServiceImpl excusedCompletedCourtCommsService;
    private final JurorDashboardSmartSurveyImportImpl jurorDashboardSmartSurveyImport;
    private final UrgentSuperUrgentStatusScheduler urgentSuperUrgentStatusScheduler;

    /**
     * Retrieves the correct batch processer Instance based on a given string value.(job identifier)
     *
     * @param job Name of batch process to run.
     * @return BureauProcessService instance representing the job to execute.
     */
    public ScheduledService getBatchProcessService(String job) {
        return switch (job) {
            case "letterComms" -> jurorCommsLetterService;
            case "weeklyComms" -> jurorCommsWeeklyInfoService;
            case "sentToCourtComms" -> jurorCommsSentToCourtService;
            case "courtComms" -> messagesService;
            case "excusalCompletedServiceCourtComms" -> excusedCompletedCourtCommsService;
            case "smartSurveyImport" -> jurorDashboardSmartSurveyImport;
            case "urgentSuperUrgentStatus" -> urgentSuperUrgentStatusScheduler;
            default -> null;
        };
    }
}
