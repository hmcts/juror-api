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
import uk.gov.hmcts.juror.api.moj.controller.jurorer.MarkAsDeliveredResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateEmailRequestSentDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public MarkAsDeliveredResponseDto markAsDelivered(MarkAsDeliveredRequestDto request) {
        log.info("Marking initial email as delivered for {} LAs", request.getLaCodes().size());

        List<String> updated = new ArrayList<>();
        List<String> alreadySent = new ArrayList<>();
        List<MarkAsDeliveredResponseDto.MarkAsDeliveredErrorDto> errors = new ArrayList<>();

        request.getLaCodes().forEach(laCode -> {
            try {
                isValidLaCode(laCode);
                MarkDeliveredResult result = markDelivered(laCode);
                switch (result) {
                    case UPDATED -> updated.add(laCode);
                    case ALREADY_SENT -> alreadySent.add(laCode);
                    default -> throw new IllegalStateException("Unexpected result from markDelivered: " + result);
                }
            } catch (Exception e) {
                log.error("Failed to mark LA code {} as delivered: {}", laCode, e.getMessage());
                errors.add(MarkAsDeliveredResponseDto.MarkAsDeliveredErrorDto.builder()
                               .laCode(laCode)
                               .reason(e.getMessage())
                               .build());
            }
        });

        log.info("Mark as delivered complete - updated: {}, already sent: {}, errors: {}",
                 updated.size(), alreadySent.size(), errors.size());

        return MarkAsDeliveredResponseDto.builder()
            .updated(updated)
            .alreadySent(alreadySent)
            .errors(errors)
            .build();
    }

    private enum MarkDeliveredResult {
        UPDATED, ALREADY_SENT
    }

    private MarkDeliveredResult markDelivered(String laCode) {
        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(laCode)
            .orElseThrow(() -> new MojException.BadRequest("LA with code " + laCode + " not found", null));

        if (localAuthority.getEmailRequestSent() != null
            && localAuthority.getEmailRequestStatus() == EmailRequestStatus.SENT) {
            log.info("Initial email for LA code {} is already marked as delivered - skipping", laCode);
            return MarkDeliveredResult.ALREADY_SENT;
        }

        updateEmailSentStatus(localAuthority, EmailRequestStatus.SENT);
        log.info("Initial email for LA code {} has been marked as delivered", laCode);
        return MarkDeliveredResult.UPDATED;
    }

    @Override
    @Transactional
    public void updateEmailRequestSent(UpdateEmailRequestSentDto request) {
        log.info("Updating email request sent status for LA code: {} to {}",
                    request.getLaCode(), request.getEmailRequestStatus());

        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(request.getLaCode())
            .orElseThrow(() -> new MojException.BadRequest("LA with code " + request.getLaCode()
                                                               + " not found", null));

        updateEmailSentStatus(localAuthority, request.getEmailRequestStatus());

        log.info("Email request sent status updated successfully for LA code: {}", request.getLaCode());
    }

    private void updateEmailSentStatus(LocalAuthority localAuthority, EmailRequestStatus request) {
        localAuthority.setEmailRequestStatus(request);
        localAuthority.setEmailRequestSent(LocalDateTime.now());
        localAuthority.setUpdatedBy(SecurityUtil.getUsername());
        localAuthority.setLastUpdated(LocalDateTime.now());
        localAuthorityRepository.save(localAuthority);
    }

}
