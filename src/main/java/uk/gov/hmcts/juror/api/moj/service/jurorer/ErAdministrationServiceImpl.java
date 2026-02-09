package uk.gov.hmcts.juror.api.moj.service.jurorer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ErAdministrationServiceImpl implements ErAdministrationService {

    private final LocalAuthorityRepository localAuthorityRepository;
    private final LaUserService laUserService;

    @Override
    @Transactional
    public void deactivateLa(DeactiveLaRequestDto requestDto) {
        log.info("Deactivating LA with code {}", requestDto.getLaCode());

        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(requestDto.getLaCode())
            .orElseThrow(() -> new MojException.BadRequest("LA with code " + requestDto.getLaCode() + " not found", null));

        if (!localAuthority.getActive()) {
            log.warn("LA with code {} is already deactivated", requestDto.getLaCode());
            throw new MojException.BadRequest("LA with code " + requestDto.getLaCode() + " is already deactivated", null);
        }

        localAuthority.setActive(false);
        localAuthority.setInactiveReason(requestDto.getReason());
        localAuthority.setUpdatedBy(SecurityUtil.getUsername());
        localAuthority.setLastUpdated(LocalDateTime.now());
        localAuthorityRepository.save(localAuthority);
        log.info("LA with code {} has been deactivated", requestDto.getLaCode());

        // deactivate the users associated with the LA
        List<LaUser> laUsers = laUserService.findUsersByLaCode(localAuthority.getLaCode());
        laUsers.forEach(user -> {
            user.setActive(false);
            laUserService.saveLaUser(user);
            log.info("Deactivating user {} associated with LA code {}", user.getUsername(), requestDto.getLaCode());

        });

    }
}
