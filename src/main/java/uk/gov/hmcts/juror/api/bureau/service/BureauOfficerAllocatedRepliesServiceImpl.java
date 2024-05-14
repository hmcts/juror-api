package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
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
            .nonUrgent(backlogStats.get(0, Integer.class).longValue())
            .urgent(backlogStats.get(1, Integer.class).longValue())
            .allReplies(backlogStats.get(2, Integer.class).longValue())
            .build();

        log.trace("No of Bureau officers {}", bureauOfficers.size());
        List<Tuple> assignedRepliesStats = jurorResponseRepository.getAssignRepliesStatisticForUsers();

        return BureauOfficerAllocatedResponses.builder()
            .data(
                assignedRepliesStats.stream()
                    .map(data ->
                        BureauOfficerAllocatedData
                            .staffAllocationResponseBuilder()
                            .login(data.get(0, String.class))
                            .name(data.get(1, String.class))
                            .nonUrgent(data.get(2, Integer.class).longValue())
                            .urgent(data.get(3, Integer.class).longValue())
                            .all(data.get(4, Integer.class).longValue())
                            .build()
                    ).toList()
            )
            .bureauBacklogCount(bureauBacklogCountDto)
            .build();
    }
}
