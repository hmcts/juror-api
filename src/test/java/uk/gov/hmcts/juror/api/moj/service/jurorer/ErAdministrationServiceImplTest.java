package uk.gov.hmcts.juror.api.moj.service.jurorer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ActiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ErAdministrationServiceImplTest {

    @Mock
    private LocalAuthorityRepository localAuthorityRepository;

    @Mock
    private LaUserService laUserService;

    @Mock
    private DeadlineRepository deadlineRepository;


    @InjectMocks
    ErAdministrationServiceImpl erAdministrationService;

    @Test
    void deactivateLaLaCodeHappy() {

        TestUtils.setUpMockAuthentication("400", "Bureau", "1", List.of("400"));


        DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
        requestDto.setLaCode("002");
        requestDto.setReason("This is a test reason for deactivating LA2");

        LocalAuthority localAuthority = LocalAuthority.builder()
            .laCode("002")
            .laName("LA2")
            .active(true)
            .build();

        LaUser laUser1 = LaUser.builder()
            .username("user1")
            .active(true)
            .build();

        when(localAuthorityRepository.findByLaCode(requestDto.getLaCode())).thenReturn(Optional.of(localAuthority));

        when(laUserService.findUsersByLaCode(localAuthority.getLaCode())).thenReturn(List.of(laUser1));

        when(localAuthorityRepository.save(any(LocalAuthority.class))).thenReturn(localAuthority);

        doNothing().when(laUserService).saveLaUser(any(LaUser.class));

        erAdministrationService.deactivateLa(requestDto);

        verify(localAuthorityRepository, times(1))
            .findByLaCode("002");

        verify(localAuthorityRepository, times(1))
            .save(localAuthority);

        verify(laUserService, times(1))
            .findUsersByLaCode("002");

        verify(laUserService, times(1))
            .saveLaUser(laUser1);
    }

    @Test
    void deactivateLaLaCodeNotFound() {

        DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
        requestDto.setLaCode("002");
        requestDto.setReason("This is a test reason for deactivating LA2");

        when(localAuthorityRepository.findByLaCode(requestDto.getLaCode())).thenReturn(Optional.empty());

        MojException.BadRequest exception =
            assertThrows(
                MojException.BadRequest.class,
                () -> erAdministrationService.deactivateLa(requestDto),
                "Should throw an error when local authority is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("LA with code 002 not found");

        verify(localAuthorityRepository, times(1))
            .findByLaCode(requestDto.getLaCode());

        verifyNoInteractions(laUserService);

    }

    @Test
    void deactivateLaAlreadyInactive() {

        DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
        requestDto.setLaCode("002");
        requestDto.setReason("This is a test reason for deactivating LA2");

        LocalAuthority localAuthority = LocalAuthority.builder()
            .laCode("002")
            .laName("LA2")
            .active(false)
            .build();

        when(localAuthorityRepository.findByLaCode(requestDto.getLaCode())).thenReturn(Optional.of(localAuthority));

        MojException.BadRequest exception =
            assertThrows(
                MojException.BadRequest.class,
                () -> erAdministrationService.deactivateLa(requestDto),
                "Should throw an error local authority is already deactivated"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("LA with code 002 is already deactivated");

        verify(localAuthorityRepository, times(1))
            .findByLaCode(requestDto.getLaCode());

        verifyNoInteractions(laUserService);

    }

    @Test
    void activateLaLaCodeNotFound() {

        ActiveLaRequestDto activeLaRequestDto = new ActiveLaRequestDto("002");

        when(localAuthorityRepository.findByLaCode(activeLaRequestDto.getLaCode())).thenReturn(Optional.empty());

        MojException.BadRequest exception =
            assertThrows(
                MojException.BadRequest.class,
                () -> erAdministrationService.activateLa(activeLaRequestDto),
                "Should throw an error when local authority is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("LA with code 002 not found");
    }

    @Test
    void updateDeadlineDeadlineNotFound() {

        UpdateDeadlineRequestDto updateDeadlineRequestDto = new UpdateDeadlineRequestDto();
        updateDeadlineRequestDto.setDeadlineDate(LocalDate.now().plusDays(90));


        when(deadlineRepository.getCurrentDeadline())
            .thenReturn(Optional.empty());

        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                                                                  () -> erAdministrationService.updateDeadline(updateDeadlineRequestDto),
                                                                  "Expected exception to be thrown when no deadline data is found");

        assertEquals("Deadline record not found - it should always exist", exception.getMessage(),
                     "Expected exception message to be Deadline record not found - it should always exist");


       // erAdministrationService.updateDeadline(updateDeadlineRequestDto);

    }
}
