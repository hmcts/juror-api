package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RevisionServiceImpl implements RevisionService {

    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;

    @Override
    public Long getLatestCourtRevisionNumber(String locCode) {
        return courtLocationRepository.getLatestRevision(locCode);
    }

    @Override
    public Long getLatestJurorRevisionNumber(String jurorNumber) {
        return jurorRepository.getLatestRevision(jurorNumber);
    }
}
