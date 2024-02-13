package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RevisionServiceImpl implements RevisionService {

    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;

    @Override
    public Revision<Long, CourtLocation> getLatestCourtRevision(String locCode) {
        return courtLocationRepository.findLastChangeRevision(locCode)
            .orElseThrow(() -> new MojException.NotFound("Court: " + locCode + " not found", null));
    }

    @Override
    public Revision<Long, Juror> getLatestJurorRevision(String jurorNumber) {
        return jurorRepository.findLastChangeRevision(jurorNumber)
            .orElseThrow(() -> new MojException.NotFound("Juror: " + jurorNumber + " not found", null));
    }
}
