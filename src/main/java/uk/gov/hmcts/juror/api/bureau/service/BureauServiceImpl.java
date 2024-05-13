package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
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
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauYourWorkCounts;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetailQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.CombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QCombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorDetailRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementation of Bureau service for bureau data access operations.
 */
@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.LawOfDemeter")
public class BureauServiceImpl implements BureauService {
    private static final String TODO = "todo";
    private static final String PENDING = "pending";
    private static final String COMPLETED = "completed";
    private final JurorDetailRepositoryMod bureauJurorDetailRepository;
    private final JurorCommonResponseRepositoryMod jurorCommonResponseRepositoryMod;
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

        wrapper.setTodoCount(
            jurorCommonResponseRepositoryMod.countByProcessingStatusIn(JurorCommonResponseRepositoryMod.TODO_STATUS));

        wrapper.setRepliesPendingCount(
            jurorCommonResponseRepositoryMod.countByProcessingStatusIn(
                JurorCommonResponseRepositoryMod.PENDING_STATUS));
        wrapper.setCompletedCount(
            jurorCommonResponseRepositoryMod.countByProcessingStatusIn(
                JurorCommonResponseRepositoryMod.COMPLETE_STATUS));

        return wrapper;
    }

    private Map<ProcessingStatus, Long> getJurorResponseCounts(String username) {
        return jurorCommonResponseRepositoryMod.getJurorResponseCounts(
            QCombinedJurorResponse.combinedJurorResponse.staff.username.eq(username));
    }

    @Override
    @Transactional(readOnly = true)
    public BureauYourWorkCounts getCounts(String staffLogin) {
        Map<ProcessingStatus, Long> countMap = getJurorResponseCounts(staffLogin);

        long todoCount = getTodoCount(countMap);
        return BureauYourWorkCounts.builder()
            .todoCount(todoCount)
            .workCount(todoCount + getPendingCount(countMap))
            .build();
    }

    long getTodoCount(Map<ProcessingStatus, Long> countMap) {
        return combineMapCounts(countMap, JurorCommonResponseRepositoryMod.TODO_STATUS);
    }

    long getPendingCount(Map<ProcessingStatus, Long> countMap) {
        return combineMapCounts(countMap, JurorCommonResponseRepositoryMod.PENDING_STATUS);
    }


    long getCompleteCount(String staffLogin, LocalDateTime start, LocalDateTime end) {
        return combineMapCounts(jurorCommonResponseRepositoryMod.getJurorResponseCounts(
                QCombinedJurorResponse.combinedJurorResponse.staff.username.eq(staffLogin),
                QCombinedJurorResponse.combinedJurorResponse.completedAt.between(start, end)),
            JurorCommonResponseRepositoryMod.COMPLETE_STATUS);
    }

    long combineMapCounts(Map<ProcessingStatus, Long> countMap, Collection<ProcessingStatus> processingStatuses) {
        return processingStatuses.stream()
            .mapToLong(value -> countMap.getOrDefault(value, 0L))
            .sum();
    }

    private BureauResponseSummaryWrapper getBureauResponseSummaryWrapperFromUsernameAndStatus(
        String staffLogin,
        Collection<ProcessingStatus> processingStatus,
        Predicate... predicates) {

        List<Tuple> tuples = jurorCommonResponseRepositoryMod
            .getJurorResponseDetailsByUsernameAndStatus(staffLogin, processingStatus, predicates);
        BureauResponseSummaryWrapper wrapper = BureauResponseSummaryWrapper.builder()
            .responses(
                tuples.stream()
                    .map(tuple -> {
                        CombinedJurorResponse jurorResponseRepository1 =
                            tuple.get(QCombinedJurorResponse.combinedJurorResponse);
                        Juror juror = tuple.get(QJuror.juror);
                        JurorPool jurorPool = tuple.get(QJurorPool.jurorPool);
                        PoolRequest pool = tuple.get(QPoolRequest.poolRequest);
                        return bureauTransformsService.detailToDto(jurorResponseRepository1, juror, jurorPool, pool);
                    }).toList()
            ).build();

        Map<ProcessingStatus, Long> countMap = getJurorResponseCounts(staffLogin);
        wrapper.setCompletedCount(getCompleteCount(staffLogin, startOfToday(), endOfToday()));
        wrapper.setTodoCount(getTodoCount(countMap));
        wrapper.setRepliesPendingCount(getPendingCount(countMap));
        return wrapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseSummaryWrapper getTodo(String staffLogin) {
        log.debug("Getting todo responses assigned to {}", staffLogin);
        return getBureauResponseSummaryWrapperFromUsernameAndStatus(
            staffLogin,
            JurorCommonResponseRepositoryMod.TODO_STATUS
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseSummaryWrapper getPending(String staffLogin) {
        log.debug("Getting pending responses assigned to {}", staffLogin);
        return getBureauResponseSummaryWrapperFromUsernameAndStatus(
            staffLogin,
            JurorCommonResponseRepositoryMod.PENDING_STATUS
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BureauResponseSummaryWrapper getCompletedToday(String staffLogin) {
        log.debug("Getting responses assigned to {} which were marked as complete today", staffLogin);
        return getBureauResponseSummaryWrapperFromUsernameAndStatus(
            staffLogin,
            JurorCommonResponseRepositoryMod.COMPLETE_STATUS,
            QCombinedJurorResponse.combinedJurorResponse.completedAt.between(startOfToday(), endOfToday())
        );
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
                    statuses = JurorCommonResponseRepositoryMod.TODO_STATUS
                        .stream().map(Enum::name).collect(Collectors.toList());
                    break;
                case PENDING:
                    statuses = JurorCommonResponseRepositoryMod.PENDING_STATUS
                        .stream().map(Enum::name).collect(Collectors.toList());
                    break;
                case COMPLETED:
                    statuses = JurorCommonResponseRepositoryMod.COMPLETE_STATUS
                        .stream().map(Enum::name).collect(Collectors.toList());
                    break;
                default:
                    log.warn("No category filter matched '{}'", category);
            }
        }
        return statuses;
    }

}
