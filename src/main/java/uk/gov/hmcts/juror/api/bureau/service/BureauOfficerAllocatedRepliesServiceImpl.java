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
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.util.ArrayList;
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

        log.trace("No of Bureau Officers {}", bureauOfficers.size());
        log.trace("Backlog : non urgent {}", bureauBacklogCountService.getBacklogNonUrgentCount());
        log.trace("Backlog : urgent {}", bureauBacklogCountService.getBacklogUrgentCount());
        log.trace("Backlog : super urgent {}", bureauBacklogCountService.getBacklogSuperUrgentCount());
        log.trace("Backlog : total replies {}", bureauBacklogCountService.getBacklogAllRepliesCount());


        List<BureauOfficerAllocatedData> staffAllocatedResponseData = new ArrayList<>();

        BureauBacklogCountData bureauBacklogCountDto = BureauBacklogCountData.builder()
            .nonUrgent(bureauBacklogCountService.getBacklogNonUrgentCount())
            .urgent(bureauBacklogCountService.getBacklogUrgentCount())
            .superUrgent(bureauBacklogCountService.getBacklogSuperUrgentCount())
            .allReplies(bureauBacklogCountService.getBacklogAllRepliesCount())
            .build();

        log.trace("No of Bureau officers {}", bureauOfficers.size());

        for (User bureauOfficer : bureauOfficers) {
            final long nonUrgent =
                jurorResponseRepository.count(JurorResponseQueries.byAssignedNonUrgent(bureauOfficer));
            final long urgent = jurorResponseRepository.count(JurorResponseQueries.byAssignedUrgent(bureauOfficer));
            final long superUrgent = jurorResponseRepository.count(JurorResponseQueries.byAssignedSuperUrgent(
                bureauOfficer));
            final long allReplies = jurorResponseRepository.count(byAssignedAll(bureauOfficer));

            log.trace("Bureau officer {}, non urgent {} ", bureauOfficer.getUsername(), nonUrgent);
            log.trace("Bureau officer {},  urgent {} ", bureauOfficer.getUsername(), urgent);
            log.trace("Bureau officer {}, super urgent {} ", bureauOfficer.getUsername(), superUrgent);
            log.trace("Bureau officer {}, all replies {} ", bureauOfficer.getUsername(), allReplies);

            BureauOfficerAllocatedData staffAllocatedData = BureauOfficerAllocatedData
                .staffAllocationResponseBuilder()
                .login(bureauOfficer.getUsername())
                .name(bureauOfficer.getName())
                .nonUrgent(nonUrgent)
                .urgent(urgent)
                .superUrgent(superUrgent)
                .all(allReplies)
                .build();

            staffAllocatedResponseData.add(staffAllocatedData);

        }


        return BureauOfficerAllocatedResponses.builder().data(staffAllocatedResponseData)
            .bureauBacklogCount(bureauBacklogCountDto)
            .build();
    }
}
