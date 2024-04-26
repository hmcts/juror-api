package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauBacklogCountData;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedData;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedResponses;
import uk.gov.hmcts.juror.api.bureau.domain.UserQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.util.List;

import static uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries.byAssignedAll;

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

        log.trace("Extracting backlog data");
        BureauBacklogCountData bureauBacklogCountDto = BureauBacklogCountData.builder()
            .nonUrgent(bureauBacklogCountService.getBacklogNonUrgentCount())
            .urgent(bureauBacklogCountService.getBacklogUrgentCount())
            .allReplies(bureauBacklogCountService.getBacklogAllRepliesCount())
            .build();

        log.trace("No of Bureau officers {}", bureauOfficers.size());

        return BureauOfficerAllocatedResponses.builder()
            .data(
                bureauOfficers.stream()
                    .map(user -> BureauOfficerAllocatedData
                        .staffAllocationResponseBuilder()
                        .login(user.getUsername())
                        .name(user.getName())
                        .nonUrgent(jurorResponseRepository.count(JurorResponseQueries.byAssignedNonUrgent(user)))
                        .urgent(jurorResponseRepository.count(JurorResponseQueries.byAssignedUrgent(user)))
                        .all(jurorResponseRepository.count(byAssignedAll(user)))
                        .build()).toList()
            )
            .bureauBacklogCount(bureauBacklogCountDto)
            .build();
    }
}
