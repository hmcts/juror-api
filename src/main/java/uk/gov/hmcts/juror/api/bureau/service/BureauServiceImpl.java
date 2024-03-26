package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseOverviewDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetailQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.QModJurorDetail;
import uk.gov.hmcts.juror.api.moj.repository.JurorDetailRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementation of Bureau service for bureau data access operations.
 */
@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BureauServiceImpl implements BureauService {
    private static final String TODO = "todo";
    private static final String PENDING = "pending";
    private static final String COMPLETED = "completed";
    private final JurorDetailRepositoryMod bureauJurorDetailRepository;
    private final UrgencyService urgencyCalculator;
    private final BureauTransformsService bureauTransformsService;
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;


    @Override
    @Transactional(readOnly = true)
    public BureauJurorDetailDto getDetailsByJurorNumber(final String jurorNumber) {
        final Optional<ModJurorDetail> jurorDetails = bureauJurorDetailRepository.findOne(
            QModJurorDetail.modJurorDetail.jurorNumber.eq(jurorNumber));

        if (jurorDetails.isEmpty()) {
            log.error("Could not find juror response for {}", jurorNumber);
            throw new JurorResponseNotFoundException("Failed to find juror response!");
        }

        return mapJurorDetailsToDto(jurorDetails.get());
    }

    @Override
    public BureauJurorDetailDto mapJurorDetailsToDto(ModJurorDetail jurorDetails) {
        // touch the collections to load lazy relationships within the transaction
        jurorDetails.getPhoneLogs().size();
        jurorDetails.getCjsEmployments().size();
        jurorDetails.getReasonableAdjustments().size();

        if (log.isDebugEnabled()) {
            log.debug("Found {}", jurorDetails);
        }

        final ModJurorDetail slaFlaggedJurorDetails = urgencyCalculator.flagSlaOverdueForResponse(jurorDetails);
        final BureauJurorDetailDto responseDto = new BureauJurorDetailDto(slaFlaggedJurorDetails);

        if (log.isTraceEnabled()) {
            log.trace("Converted to {}", responseDto);
        }

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseSummaryWrapper getDetailsByProcessingStatus(String category) {

        List<ModJurorDetail> detailsByStatus = Lists.newLinkedList(bureauJurorDetailRepository.findAll(
            BureauJurorDetailQueries.byStatus(queryableStatusList(category)),
            BureauJurorDetailQueries.dateReceivedAscending()
        ));
        List<ModJurorDetail> enrichedDetails = urgencyCalculator.flagSlaOverdueFromList(detailsByStatus);
        List<BureauResponseSummaryDto> filteredResponses =
            enrichedDetails.stream().map(bureauTransformsService::detailToDto).collect(
                Collectors.toCollection(LinkedList::new));
        BureauResponseSummaryWrapper wrapper = new BureauResponseSummaryWrapper();

        wrapper.setResponses(filteredResponses);
        wrapper.setTodoCount(bureauJurorDetailRepository.count(BureauJurorDetailQueries.byStatus(queryableStatusList(
            TODO))));
        wrapper.setRepliesPendingCount(bureauJurorDetailRepository.count(BureauJurorDetailQueries.byStatus(
            queryableStatusList(PENDING))));
        wrapper.setCompletedCount(bureauJurorDetailRepository.count(BureauJurorDetailQueries.byStatus(
            queryableStatusList(COMPLETED))));

        return wrapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseSummaryWrapper getTodo(String staffLogin) {

        log.debug("Getting todo responses assigned to {}", staffLogin);
        final BureauResponseSummaryWrapper wrapper = bureauTransformsService.prepareOutput(getInDisplayOrder(
            BureauJurorDetailQueries.byAssignmentAndProcessingStatus(staffLogin, queryableStatusList(TODO))));
        wrapper.setTodoCount((long) wrapper.getResponses().size());
        wrapper.setRepliesPendingCount(
            bureauJurorDetailRepository.count(BureauJurorDetailQueries.byAssignmentAndProcessingStatus(
                staffLogin,
                queryableStatusList(PENDING)
            )));
        wrapper.setCompletedCount(bureauJurorDetailRepository.count(BureauJurorDetailQueries.byCompletedAt(
            staffLogin,
            startOfToday(),
            endOfToday()
        )));

        return wrapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseSummaryWrapper getPending(String staffLogin) {
        log.debug("Getting pending responses assigned to {}", staffLogin);
        final BureauResponseSummaryWrapper wrapper = bureauTransformsService.prepareOutput(getInDisplayOrder(
            BureauJurorDetailQueries.byAssignmentAndProcessingStatus(staffLogin, queryableStatusList(PENDING))));
        wrapper.setRepliesPendingCount((long) wrapper.getResponses().size());
        wrapper.setTodoCount(bureauJurorDetailRepository.count(BureauJurorDetailQueries.byAssignmentAndProcessingStatus(
            staffLogin,
            queryableStatusList(TODO)
        )));
        wrapper.setCompletedCount(bureauJurorDetailRepository.count(BureauJurorDetailQueries.byCompletedAt(
            staffLogin,
            startOfToday(),
            endOfToday()
        )));
        return wrapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseSummaryWrapper getCompletedToday(String staffLogin) {
        log.debug("Getting responses assigned to {} which were marked as complete today", staffLogin);
        final BureauResponseSummaryWrapper wrapper = bureauTransformsService.prepareOutput(getInDisplayOrder(
            BureauJurorDetailQueries.byCompletedAt(staffLogin, startOfToday(), endOfToday())));
        wrapper.setCompletedCount((long) wrapper.getResponses().size());
        wrapper.setTodoCount(bureauJurorDetailRepository.count(BureauJurorDetailQueries.byAssignmentAndProcessingStatus(
            staffLogin,
            queryableStatusList(TODO)
        )));
        wrapper.setRepliesPendingCount(
            bureauJurorDetailRepository.count(BureauJurorDetailQueries.byAssignmentAndProcessingStatus(
                staffLogin,
                queryableStatusList(PENDING)
            )));
        return wrapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseOverviewDto getOverview(String staffLogin) {
        log.debug("Getting totals of different response types assigned to {}", staffLogin);

        final List<ProcessingStatus> urgentStatuses = Arrays.asList(
            ProcessingStatus.AWAITING_CONTACT,
            ProcessingStatus.AWAITING_COURT_REPLY,
            ProcessingStatus.AWAITING_TRANSLATION,
            ProcessingStatus.TODO
        );

        final List<ProcessingStatus> pendingStatuses = Arrays.asList(
            ProcessingStatus.AWAITING_CONTACT,
            ProcessingStatus.AWAITING_COURT_REPLY,
            ProcessingStatus.AWAITING_TRANSLATION
        );

        final List<ProcessingStatus> todoStatus = Collections.singletonList(ProcessingStatus.TODO);

        BureauResponseOverviewDto overviewDto = new BureauResponseOverviewDto();
        overviewDto.setUrgentsCount(
            jurorResponseRepository.count(JurorResponseQueries.byAssignmentAndProcessingStatusAndUrgency(
                staffLogin,
                urgentStatuses,
                true
            )));
        overviewDto.setPendingCount(
            jurorResponseRepository.count(JurorResponseQueries.byAssignmentAndProcessingStatusAndUrgency(
                staffLogin,
                pendingStatuses,
                false
            )));
        overviewDto.setTodoCount(
            jurorResponseRepository.count(JurorResponseQueries.byAssignmentAndProcessingStatusAndUrgency(
                staffLogin,
                todoStatus,
                false
            )));

        return overviewDto;
    }

    /**
     * Gets entities in the order required by the AC.
     *
     * @param query query to run
     * @return matching entities
     * @since JDB-2142
     */
    private Iterable<ModJurorDetail> getInDisplayOrder(Predicate query) {
        return bureauJurorDetailRepository.findAll(query, BureauJurorDetailQueries.dateReceivedAscending());
    }


    private LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime endOfToday() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }

    private List<String> queryableStatusList(final String category) {
        List<String> statuses = Stream.of(ProcessingStatus.values()).map(Enum::name).collect(Collectors.toList());
        if (category != null) {
            switch (category.toLowerCase().trim()) {
                case TODO:
                    statuses = Collections.singletonList(ProcessingStatus.TODO.name());
                    break;
                case PENDING:
                    statuses = Arrays.asList(
                        ProcessingStatus.AWAITING_CONTACT.name(),
                        ProcessingStatus.AWAITING_TRANSLATION.name(),
                        ProcessingStatus.AWAITING_COURT_REPLY.name()
                    );
                    break;
                case COMPLETED:
                    statuses = Collections.singletonList(ProcessingStatus.CLOSED.name());
                    break;
                default:
                    log.warn("No category filter matched '{}'", category);
            }
        }
        return statuses;
    }

}
