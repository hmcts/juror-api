package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorThirdParty;
import uk.gov.hmcts.juror.api.moj.repository.juror.JurorThirdPartyRepository;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JurorThirdPartyServiceImpl implements JurorThirdPartyService {
    private final JurorThirdPartyRepository jurorThirdPartyRepository;

    @Override
    @Transactional
    public void deleteThirdParty(Juror juror) {
        jurorThirdPartyRepository.deleteById(juror.getJurorNumber());
        juror.setThirdParty(null);
    }

    @Override
    @Transactional
    public void createOrUpdateThirdParty(Juror juror, ThirdPartyUpdateDto thirdPartyUpdate) {
        JurorThirdParty jurorThirdParty = getOrCreateJurorThirdParty(juror.getJurorNumber());
        jurorThirdParty.setFirstName(thirdPartyUpdate.getFirstName());
        jurorThirdParty.setLastName(thirdPartyUpdate.getLastName());
        jurorThirdParty.setRelationship(thirdPartyUpdate.getRelationship());
        jurorThirdParty.setMainPhone(thirdPartyUpdate.getMainPhone());
        jurorThirdParty.setOtherPhone(thirdPartyUpdate.getOtherPhone());
        jurorThirdParty.setEmailAddress(thirdPartyUpdate.getEmailAddress());
        jurorThirdParty.setReason(thirdPartyUpdate.getReason());
        jurorThirdParty.setOtherReason(thirdPartyUpdate.getOtherReason());
        jurorThirdParty.setContactJurorByEmail(thirdPartyUpdate.isContactJurorByEmail());
        jurorThirdParty.setContactJurorByPhone(thirdPartyUpdate.isContactJurorByPhone());

        jurorThirdPartyRepository.save(jurorThirdParty);
    }

    private JurorThirdParty getOrCreateJurorThirdParty(String jurorNumber) {
        return jurorThirdPartyRepository.findById(jurorNumber)
            .orElseGet(() -> new JurorThirdParty(jurorNumber));
    }
}
