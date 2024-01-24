package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;

import java.time.Clock;
import java.util.Date;

//TODO rename once old letter services are deleted
//TODO Migrate to use new letter approach
//TODO Test this class -- No test classes have been made for this as this will change when letter queues are refactored
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LetterServiceModImpl implements LetterServiceMod {

    private final DisqualificationLetterRepository disqualificationLetterRepository;
    private final Clock clock;

    @Override
    public void createDisqualificationLetter(String jurorNumber, String disqualifyCode) {
        DisqualificationLetter disqualificationLetter = new DisqualificationLetter();
        disqualificationLetter.setOwner("400");
        disqualificationLetter.setJurorNumber(jurorNumber);
        disqualificationLetter.setDisqCode(disqualifyCode);
        disqualificationLetter.setDateDisq(Date.from(clock.instant()));
        disqualificationLetterRepository.save(disqualificationLetter);
    }
}
