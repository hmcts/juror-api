package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauBacklogCountData;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedData;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedResponses;
import uk.gov.hmcts.juror.api.bureau.domain.UserQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BureauOfficerAllocatedRepliesServiceImpl implements BureauOfficerAllocatedRepliesService {
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    private final BureauBacklogCountService bureauBacklogCountService;
    private final UserRepository userRepository;

    @Override
    public BureauOfficerAllocatedResponses getBackLogData() {
        final List<User> bureauOfficers = Lists.newLinkedList(userRepository
            .findAll(UserQueries.activeBureauOfficers()));

        Tuple backlogStats = jurorResponseRepository.getAssignRepliesStatistics();

        log.trace("Extracting backlog data");
        BureauBacklogCountData bureauBacklogCountDto = BureauBacklogCountData.builder()
            .nonUrgent(backlogStats.get(Expressions.numberPath(Integer.class, "nonUrgent")).longValue())
            .urgent(backlogStats.get(Expressions.numberPath(Integer.class,"urgent")).longValue())
            .allReplies(backlogStats.get(Expressions.numberPath(Integer.class,"allReplies")).longValue())
            .build();

        log.trace("No of Bureau officers {}", bureauOfficers.size());
        List<Tuple> assignedRepliesStats = jurorResponseRepository.getAssignRepliesStatisticForUsers();

        return BureauOfficerAllocatedResponses.builder()
            .data(
                assignedRepliesStats.stream()
                    .map(data ->
                        BureauOfficerAllocatedData
                            .staffAllocationResponseBuilder()
                            .login(data.get(Expressions.stringPath("login")))
                            .name(data.get(Expressions.stringPath("name")))
                            .nonUrgent(data.get(Expressions.numberPath(Integer.class,"nonUrgent")).longValue())
                            .urgent(data.get(Expressions.numberPath(Integer.class,"urgent")).longValue())
                            .all(data.get(Expressions.numberPath(Integer.class,"allReplies")).longValue())
                            .build()
                    ).toList()
            )
            .bureauBacklogCount(bureauBacklogCountDto)
            .build();
    }
}
