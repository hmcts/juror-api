package uk.gov.hmcts.juror.api.bureau.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauBacklogCountData;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

@Slf4j
@Service
public class BureauBacklogCountServiceImpl implements BureauBacklogCountService {

    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;


    @Autowired
    public BureauBacklogCountServiceImpl(
        final JurorDigitalResponseRepositoryMod jurorResponseRepository) {
        Assert.notNull(jurorResponseRepository, "JurorResponseRepository cannot be null");
        this.jurorResponseRepository = jurorResponseRepository;
    }


    @Override
    public long getBacklogNonUrgentCount() {
        return jurorResponseRepository.count(JurorResponseQueries.byUnassignedTodoNonUrgent());
    }

    @Override
    public long getBacklogUrgentCount() {
        return jurorResponseRepository.count(JurorResponseQueries.byUnassignedTodoUrgent());
    }

    @Override
    public long getBacklogAwaitingInfoCount() {
        return jurorResponseRepository.count(JurorResponseQueries.byAllAwaitingInfo());
    }


    @Override
    public long getBacklogAllRepliesCount() {
        return jurorResponseRepository.count(JurorResponseQueries.byUnassignedTodo());
    }

    @Override
    public BureauBacklogCountData getBacklogResponseCount() {
        BureauBacklogCountData dto = new BureauBacklogCountData();
        dto.setNonUrgent(getBacklogNonUrgentCount());
        dto.setUrgent(getBacklogUrgentCount());
        dto.setAwaitingInfo(getBacklogAwaitingInfoCount());
        dto.setAllReplies(getBacklogAllRepliesCount());
        return dto;

    }
}
