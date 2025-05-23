package uk.gov.hmcts.juror.api.bureau.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauBacklogCountData;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedData;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedResponses;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BureauOfficerAllocatedRepliesServiceImpl implements BureauOfficerAllocatedRepliesService {
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @Override
    @Transactional
    public BureauOfficerAllocatedResponses getBackLogData() {
        Tuple backlogStats = jurorResponseRepository.getAssignRepliesStatistics();

        log.trace("Extracting backlog data");
        BureauBacklogCountData bureauBacklogCountDto = BureauBacklogCountData.builder()
            .nonUrgent(Optional.ofNullable(backlogStats.get(Expressions.numberPath(Long.class, "nonUrgent")))
                .orElse(0L))
            .urgent(Optional.ofNullable(backlogStats.get(Expressions.numberPath(Long.class, "urgent")))
                .orElse(0L))
            .awaitingInfo(Optional.ofNullable(backlogStats.get(Expressions.numberPath(Long.class, "awaitingInfo")))
                .orElse(0L))
            .allReplies(Optional.ofNullable(backlogStats.get(Expressions.numberPath(Long.class, "allReplies")))
                .orElse(0L))
            .build();

        List<Tuple> assignedRepliesStats = jurorResponseRepository.getAssignRepliesStatisticForUsers();

        return BureauOfficerAllocatedResponses.builder()
            .data(
                assignedRepliesStats.stream()
                    .map(data ->
                        BureauOfficerAllocatedData
                            .staffAllocationResponseBuilder()
                            .login(data.get(Expressions.stringPath("login")))
                            .name(data.get(Expressions.stringPath("name")))
                            .nonUrgent(Optional.ofNullable(data.get(Expressions.numberPath(Long.class,"nonUrgent")))
                                .orElse(0L))
                            .urgent(Optional.ofNullable(data.get(Expressions.numberPath(Long.class,"urgent")))
                                .orElse(0L))
                            .awaitingInfo(
                                Optional.ofNullable(data.get(Expressions.numberPath(Long.class,"awaitingInfo")))
                                .orElse(0L))
                            .all(Optional.ofNullable(data.get(Expressions.numberPath(Long.class,"allReplies")))
                                .orElse(0L))
                            .build()
                    ).toList()
            )
            .bureauBacklogCount(bureauBacklogCountDto)
            .build();
    }
}
