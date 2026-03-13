package uk.gov.hmcts.juror.api.moj.service.jurorer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.EmailRequestStatus;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ActiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.MarkAsDeliveredRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateEmailRequestSentDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.LaCodeValidator.isValidLaCode;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ErAdministrationServiceImpl implements ErAdministrationService {

    private final LocalAuthorityRepository localAuthorityRepository;
    private final LaUserService laUserService;
    private final DeadlineRepository deadlineRepository;

    @Override
    @Transactional
    public void deactivateLa(DeactiveLaRequestDto requestDto) {
        log.info("Deactivating LA with code {}", requestDto.getLaCode());

        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(requestDto.getLaCode())
            .orElseThrow(() -> new MojException.BadRequest("LA with code " + requestDto.getLaCode()
                                                               + " not found", null));

        if (!localAuthority.getActive()) {
            log.warn("LA with code {} is already deactivated", requestDto.getLaCode());
            throw new MojException.BadRequest("LA with code " + requestDto.getLaCode()
                                                  + " is already deactivated", null);
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

    @Override
    @Transactional
    public void activateLa(ActiveLaRequestDto requestDto) {
        log.info("Activating LA with code {}", requestDto.getLaCode());

        isValidLaCode(requestDto.getLaCode());

        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(requestDto.getLaCode())
            .orElseThrow(() -> new MojException.BadRequest("LA with code " + requestDto.getLaCode()
                                                               + " not found", null));

        if (Boolean.TRUE.equals(localAuthority.getActive())) {
            log.warn("LA with code {} is already activated", requestDto.getLaCode());
            throw new MojException.BadRequest("LA with code " + requestDto.getLaCode()
                                                  + " is already activated", null);
        }

        localAuthority.setActive(true);
        localAuthority.setInactiveReason(null);
        localAuthority.setUpdatedBy(SecurityUtil.getUsername());
        localAuthority.setLastUpdated(LocalDateTime.now());
        localAuthorityRepository.save(localAuthority);
        log.info("LA with code {} has been activated", requestDto.getLaCode());

        // activate the users associated with the LA
        List<LaUser> laUsers = laUserService.findUsersByLaCode(localAuthority.getLaCode());
        laUsers.forEach(user -> {
            user.setActive(true);
            laUserService.saveLaUser(user);
            log.info("Activating user {} associated with LA code {}", user.getUsername(), requestDto.getLaCode());
        });


    }


    @Override
    @Transactional
    public UpdateDeadlineResponseDto updateDeadline(UpdateDeadlineRequestDto request) {
        log.info("Updating deadline date to: {}", request.getDeadlineDate());

        // Get current deadline (should always exist)
        Deadline deadline = deadlineRepository.getCurrentDeadline()
            .orElseThrow(() -> new MojException.InternalServerError(
                "Deadline record not found - it should always exist", null));

        // Get current user from JWT
        String currentUser = SecurityUtil.getActiveLogin();

        // Update deadline
        deadline.setDeadlineDate(request.getDeadlineDate());
        deadline.setUpdatedBy(currentUser);
        deadline.setLastUpdated(LocalDateTime.now());

        // Save updated deadline
        Deadline updatedDeadline = deadlineRepository.save(deadline);

        log.info("Deadline updated successfully by: {}", currentUser);

        return UpdateDeadlineResponseDto.builder()
            .deadlineDate(updatedDeadline.getDeadlineDate())
            .updatedBy(updatedDeadline.getUpdatedBy())
            .lastUpdated(updatedDeadline.getLastUpdated())
            .daysRemaining(updatedDeadline.getDaysRemaining())
            .build();
    }

    @Override
    @Transactional
    public void markAsDelivered(MarkAsDeliveredRequestDto request) {

        log.info("Marking initial email as delivered for LAs}");

        // for each LA code, find the LA and update the email request sent flag to true
        request.getLaCodes().forEach(laCode -> {
            isValidLaCode(laCode);
            markDelivered(laCode);
        });
    }

    private void markDelivered(String laCode) {
        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(laCode)
            .orElseThrow(() -> new MojException.BadRequest("LA with code " + laCode
                                                               + " not found", null));

        if (localAuthority.getEmailRequestSent() != null
            && localAuthority.getEmailRequestStatus() == EmailRequestStatus.SENT) {
            log.warn("Initial email for LA code {} is already marked as delivered", laCode);
            throw new MojException.BadRequest("Initial email for LA code " + laCode
                                                  + " is already marked as delivered", null);
        }


        localAuthority.setEmailRequestStatus(EmailRequestStatus.SENT);
        localAuthority.setEmailRequestSent(LocalDateTime.now());
        localAuthority.setUpdatedBy(SecurityUtil.getUsername());
        localAuthority.setLastUpdated(LocalDateTime.now());
        localAuthorityRepository.save(localAuthority);
        log.info("Initial email for LA code {} has been marked as delivered", laCode);
    }

}
