package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JurorServiceModImpl implements JurorServiceMod {

    private final JurorRepository jurorRepository;

    @Override
    public Juror getJurorFromJurorNumber(String jurorNumber) {
        return getJurorOptionalFromJurorNumber(jurorNumber).orElseThrow(() -> new MojException.NotFound(
            "Juror not found: " + jurorNumber, null));
    }

    @Override
    public Optional<Juror> getJurorOptionalFromJurorNumber(String jurorNumber) {
        return jurorRepository.findById(jurorNumber);
    }
}
