package uk.gov.hmcts.juror.api.bureau.scheduler;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsLetterServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsSentToCourtServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsWeeklyInfoServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.JurorDashboardSmartSurveyImportImpl;
import uk.gov.hmcts.juror.api.bureau.service.ScheduledService;
import uk.gov.hmcts.juror.api.juror.service.ExcusedCompletedCourtCommsServiceImpl;
import uk.gov.hmcts.juror.api.juror.service.MessagesServiceImpl;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Tests for {@link uk.gov.hmcts.juror.api.bureau.scheduler.BureauBatchProcessFactory}.
*/

@ExtendWith(MockitoExtension.class)
class BureauBatchProcessFactoryTest {

    @Mock
    private JurorCommsLetterServiceImpl jurorCommsLetterService;
    @Mock
    private JurorCommsWeeklyInfoServiceImpl jurorCommsWeeklyInfoService;
    @Mock
    private JurorCommsSentToCourtServiceImpl jurorCommsSentToCourtService;
    @Mock
    private MessagesServiceImpl messagesService;
    @Mock
    private ExcusedCompletedCourtCommsServiceImpl excusedCompletedCourtCommsService;
    @Mock
    private JurorDashboardSmartSurveyImportImpl jurorDashboardSmartSurveyImport;
    @Mock
    private UrgentSuperUrgentStatusScheduler urgentSuperUrgentStatusScheduler;

    @InjectMocks
    private BureauBatchProcessFactory bureauBatchProcessFactory;


    @Test
    void retrieveNullProcessServiceDoesNotExist() {
        String jobToRun = "letterComms2";
        final ScheduledService batchProcessService = bureauBatchProcessFactory.getBatchProcessService(jobToRun);
        assertThat(batchProcessService).isNull();
    }

    public static Stream<Arguments> retrieveValidProcessServiceArguments() {
        return Stream.of(
            arguments("letterComms", JurorCommsLetterServiceImpl.class),
            arguments("weeklyComms", JurorCommsWeeklyInfoServiceImpl.class),
            arguments("sentToCourtComms", JurorCommsSentToCourtServiceImpl.class),
            arguments("courtComms", MessagesServiceImpl.class),
            arguments("excusalCompletedServiceCourtComms", ExcusedCompletedCourtCommsServiceImpl.class),
            arguments("smartSurveyImport", JurorDashboardSmartSurveyImportImpl.class),
            arguments("urgentSuperUrgentStatus", UrgentSuperUrgentStatusScheduler.class)
        );
    }

    @ParameterizedTest
    @MethodSource("retrieveValidProcessServiceArguments")
    void retrieveValidProcessService(String jobToRun, Class<?> expectedClass) {
        final ScheduledService batchProcessService = bureauBatchProcessFactory.getBatchProcessService(jobToRun);
        assertThat(batchProcessService).isInstanceOf(expectedClass);
    }
}
