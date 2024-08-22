package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorThirdParty;
import uk.gov.hmcts.juror.api.moj.repository.juror.JurorThirdPartyRepository;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;

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
        jurorThirdParty.setFirstName(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyFirstName()));
        jurorThirdParty.setLastName(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyLastName()));
        jurorThirdParty.setRelationship(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyRelationship()));
        jurorThirdParty.setMainPhone(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyMainPhone()));
        jurorThirdParty.setOtherPhone(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyOtherPhone()));
        jurorThirdParty.setEmailAddress(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyEmailAddress()));
        jurorThirdParty.setReason(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyReason()));
        jurorThirdParty.setOtherReason(DataUtils.nullIfBlank(thirdPartyUpdate.getThirdPartyOtherReason()));
        jurorThirdParty.setContactJurorByEmail(thirdPartyUpdate.isContactJurorByEmail());
        jurorThirdParty.setContactJurorByPhone(thirdPartyUpdate.isContactJurorByPhone());

        jurorThirdPartyRepository.save(jurorThirdParty);
    }

    private JurorThirdParty getOrCreateJurorThirdParty(String jurorNumber) {
        return jurorThirdPartyRepository.findById(jurorNumber)
            .orElseGet(() -> new JurorThirdParty(jurorNumber));
    }
}
