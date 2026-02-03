package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LocalAuthorityServiceImpl implements LocalAuthorityService {

    private final LocalAuthorityRepository localAuthorityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LocalAuthority> getAllLocalAuthorities(boolean activeOnly) {

        if (activeOnly) {
            return localAuthorityRepository.findByActiveTrueOrderByLaCode();
        } else {
            return localAuthorityRepository.findAllByOrderByLaCode();
        }
    }
}
