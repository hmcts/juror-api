package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

@SuppressWarnings("PMD.TooManyMethods")
class JurorResponseServiceImplTest {
    private JurorPoolRepository jurorPoolRepository;
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    private StraightThroughProcessorService straightThroughProcessorService;
    private UserRepository userRepository;
    private SummonsReplyMergeService mergeService;
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    private JurorResponseServiceImpl jurorResponseService;

    @BeforeEach
    void setUpMocks() {

        jurorPoolRepository = Mockito.mock(JurorPoolRepository.class);
        jurorPaperResponseRepository = Mockito.mock(JurorPaperResponseRepositoryMod.class);
        jurorDigitalResponseRepository = Mockito.mock(JurorDigitalResponseRepositoryMod.class);
        straightThroughProcessorService = Mockito.mock(StraightThroughProcessorService.class);
        JurorCommonResponseRepositoryMod jurorCommonResponseRepository =
            Mockito.mock(JurorCommonResponseRepositoryMod.class);
        mergeService = Mockito.mock(SummonsReplyMergeService.class);
        jurorResponseAuditRepository = Mockito.mock(JurorResponseAuditRepositoryMod.class);
        this.jurorResponseService = spy(new JurorResponseServiceImpl(jurorPoolRepository,
            jurorPaperResponseRepository,
            jurorDigitalResponseRepository,
            straightThroughProcessorService,
            jurorCommonResponseRepository,
            jurorResponseAuditRepository,
            userRepository,
            mergeService
        ));

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());
        Mockito.doReturn(new DigitalResponse()).when(jurorDigitalResponseRepository).findByJurorNumber(any());

        Mockito.doReturn(Collections.singletonList(createTestJurorPool("400")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive("123456789", true);
        Mockito.doReturn(Collections.singletonList(createTestJurorPool("415")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive("987654321", true);

        Mockito.doReturn(Collections.singletonList(createTestJurorPool("400")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("123456789", true);
        Mockito.doReturn(Collections.singletonList(createTestJurorPool("415")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("987654321", true);

        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));

        Mockito.doReturn(false).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));
        Mockito.doReturn(false).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(DigitalResponse.class),
                any(LocalDate.class), any(JurorPool.class));
        Mockito.doNothing().when(straightThroughProcessorService)
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
        PoolRequestRepository poolRequestRepository = Mockito.mock(PoolRequestRepository.class);
        Mockito.when(poolRequestRepository.findByPoolNumber(any()))
            .thenReturn(Optional.of(mockPoolRequest("12345678", "415")));
    }

    @Test
    void closeOpenResponseRecordClosesOpenDigitalResponse() {
        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setJurorNumber("123456789");
        digitalResponse.setProcessingComplete(false);

        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber("123456789");

        boolean responseClosed = jurorResponseService.closeOpenResponseRecord("123456789", "BUREAU_USER");

        Assertions.assertThat(responseClosed).isTrue();
        Assertions.assertThat(digitalResponse.getProcessingComplete()).isTrue();
        Assertions.assertThat(digitalResponse.getCompletedAt()).isNotNull();
        Mockito.verify(mergeService).mergeDigitalResponse(digitalResponse, "BUREAU_USER");
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
    }

    @Test
    void closeOpenResponseRecordClosesOpenPaperResponseWhenNoDigitalResponseExists() {
        PaperResponse paperResponse = new PaperResponse();
        paperResponse.setJurorNumber("123456789");
        paperResponse.setProcessingComplete(false);

        Mockito.doReturn(null).when(jurorDigitalResponseRepository).findByJurorNumber("123456789");
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber("123456789");

        boolean responseClosed = jurorResponseService.closeOpenResponseRecord("123456789", "BUREAU_USER");

        Assertions.assertThat(responseClosed).isTrue();
        Assertions.assertThat(paperResponse.getProcessingComplete()).isTrue();
        Assertions.assertThat(paperResponse.getCompletedAt()).isNotNull();
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
        Mockito.verify(mergeService).mergePaperResponse(paperResponse, "BUREAU_USER");
    }

    @Test
    void closeOpenResponseRecordDoesNotCloseAlreadyCompletedDigitalResponse() {
        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setJurorNumber("123456789");
        digitalResponse.setProcessingComplete(true);

        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber("123456789");
        Mockito.doReturn(null).when(jurorPaperResponseRepository).findByJurorNumber("123456789");

        boolean responseClosed = jurorResponseService.closeOpenResponseRecord("123456789", "BUREAU_USER");

        Assertions.assertThat(responseClosed).isFalse();
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
    }

    //Tests related to method updateJurorPersonalDetails()
    @Test
    void testUpdatePaperResponsePersonalDetailsBureauUserBureauOwnerHappy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            1, 1, 0,
            0, 0,
            0, 0, 0);
    }


    @Test
    void testUpdateDigitalResponsePersonalDetailsBureauUserBureauOwnerHappy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            0, 0, 0,
            0, 0,
            0, 1, 0);
    }

    @Test
    void testUpdatePaperResponsePersonalDetailsCourtUserCourtOwnerHappy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);

        BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321");

        assertPaperResponsePersonalDetailsMockitoVerification(1, 0,
            1, 1, 0,
            0, 0,
            0, 0, 0);
    }

    @Test
    void testUpdateDigitalResponsePersonalDetailsCourtUserCourtOwnerHappy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);

        BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321");

        assertPaperResponsePersonalDetailsMockitoVerification(1, 0,
            0, 0, 0,
            0, 0, 0, 1, 0);
    }

    @Test
    void testUpdatePaperResponsePersonalDetailsBureauUserCourtOwnerUserNotAuthorised() {
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321"));

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            0, 0, 0,
            0, 0,
            0, 0, 0);
    }

    @Test
    void testUpdateDigitalResponsePersonalDetailsBureauUserCourtOwnerUserNotAuthorised() {
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321"));

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            0, 0, 0,
            0, 0,
            0, 0, 0);
    }

    @Test
    void testUpdatePaperResponsePersonalDetailsCourtUserCourtOwnerNoAccess() {
        BureauJwtPayload payload = TestUtils.createJwt("416", "SOME_USER", "99");
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321"));

        assertPaperResponsePersonalDetailsMockitoVerification(1, 0,
            0, 0, 0,
            0, 0,
            0, 0, 0);
    }

    @Test
    void testUpdateDigitalResponsePersonalDetailsCourtUserCourtOwnerNoAccess() {
        BureauJwtPayload payload = TestUtils.createJwt("416", "SOME_USER", "99");

        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321"));

        assertPaperResponsePersonalDetailsMockitoVerification(1, 0,
            0, 0, 0,
            0, 0,
            0, 0, 0);
    }

    @Test
    void testUpdatePaperResponsePersonalDetailsDateOfBirthValidAge() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        personalDetailsDto.setDateOfBirth(LocalDate.now().minusYears(30));

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            1, 1, 1,
            0, 0,
            0, 0, 0);
    }

    @Test
    void testUpdateDigitalResponsePersonalDetailsDateOfBirthValidAge() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);
        personalDetailsDto.setDateOfBirth(LocalDate.now().minusYears(30));

        BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321");

        assertPaperResponsePersonalDetailsMockitoVerification(1, 0,
            0, 0, 0,
            1, 0,
            0, 1, 1);
    }

    @Test
    void testUpdatePaperResponsePersonalDetailsDateOfBirthTooYoung() {
        Mockito.doReturn(true).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));

        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        personalDetailsDto.setDateOfBirth(LocalDate.now().minusYears(17));

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            1, 1, 1,
            0, 1,
            0, 0, 0);
    }

    @Test
    void testUpdateDigitalResponsePersonalDetailsDateOfBirthTooYoung() {
        Mockito.doReturn(true).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(DigitalResponse.class),
                any(LocalDate.class), any(JurorPool.class));

        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);
        personalDetailsDto.setDateOfBirth(LocalDate.now().minusYears(17));

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            0, 0, 0,
            1, 0,
            1, 1, 1);
    }

    @Test
    void testUpdatePaperResponsePersonalDetailsDateOfBirthTooOld() {
        Mockito.doReturn(true).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));

        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        personalDetailsDto.setDateOfBirth(LocalDate.now().minusYears(80));

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            1, 1, 1,
            0, 1,
            0, 0, 0);
    }

    @Test
    void testUpdateDigitalResponsePersonalDetailsDateOfBirthTooOld() {

        Mockito.doReturn(true).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(DigitalResponse.class),
                any(LocalDate.class), any(JurorPool.class));

        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);
        personalDetailsDto.setDateOfBirth(LocalDate.now().minusYears(80));

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            0, 0, 0,
            1, 0,
            1, 1, 1);
    }

    @Test
    void updatesPaperPersonalAddressAndContactDetails() {
        PaperResponse response = new PaperResponse();
        response.setJurorNumber("123456789");
        response.setFirstName("Original");
        response.setLastName("Name");
        response.setAddressLine1("1 Original Street");
        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber("123456789");

        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        request.setTitle("Dr");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddressLineOne("1 Updated Street");
        request.setAddressLineTwo("Updated Village");
        request.setAddressLineThree("Updated District");
        request.setAddressTown("Updated Town");
        request.setAddressCounty("Updated County");
        request.setAddressPostcode("MK5 6LA");
        request.setPrimaryPhone("07700900123");
        request.setSecondaryPhone("07700900456");
        request.setEmailAddress("juror@example.com");
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789");

        Assertions.assertThat(response.getTitle()).isEqualTo("Dr");
        Assertions.assertThat(response.getFirstName()).isEqualTo("FName");
        Assertions.assertThat(response.getLastName()).isEqualTo("LName");
        Assertions.assertThat(response.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        Assertions.assertThat(response.getAddressLine1()).isEqualTo("1 Updated Street");
        Assertions.assertThat(response.getAddressLine2()).isEqualTo("Updated Village");
        Assertions.assertThat(response.getAddressLine3()).isEqualTo("Updated District");
        Assertions.assertThat(response.getAddressLine4()).isEqualTo("Updated Town");
        Assertions.assertThat(response.getAddressLine5()).isEqualTo("Updated County");
        Assertions.assertThat(response.getPostcode()).isEqualTo("MK5 6LA");
        Assertions.assertThat(response.getPhoneNumber()).isEqualTo("07700900123");
        Assertions.assertThat(response.getAltPhoneNumber()).isEqualTo("07700900456");
        Assertions.assertThat(response.getEmail()).isEqualTo("juror@example.com");
        Mockito.verify(jurorPaperResponseRepository).save(Mockito.same(response));
        Mockito.verifyNoInteractions(jurorDigitalResponseRepository);
    }

    @Test
    void updatesPaperThirdPartyDetails() {
        PaperResponse response = new PaperResponse();
        response.setJurorNumber("123456789");
        response.setThirdPartyFName("Original");
        response.setRelationship("Original relationship");
        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        request.setThirdParty(JurorPaperResponseDto.ThirdParty.builder()
            .thirdPartyFName("Other")
            .thirdPartyLName("Person")
            .mainPhone("07700900789")
            .otherPhone("07700900987")
            .emailAddress("third.party@example.com")
            .relationship("Friend")
            .thirdPartyReason("Other")
            .thirdPartyOtherReason("Needs assistance")
            .useJurorPhoneDetails(false)
            .useJurorEmailDetails(false)
            .build());
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789");

        Assertions.assertThat(response.getThirdPartyFName()).isEqualTo("Other");
        Assertions.assertThat(response.getThirdPartyLName()).isEqualTo("Person");
        Assertions.assertThat(response.getMainPhone()).isEqualTo("07700900789");
        Assertions.assertThat(response.getOtherPhone()).isEqualTo("07700900987");
        Assertions.assertThat(response.getEmailAddress()).isEqualTo("third.party@example.com");
        Assertions.assertThat(response.getRelationship()).isEqualTo("Friend");
        Assertions.assertThat(response.getThirdPartyReason()).isEqualTo("Other");
        Assertions.assertThat(response.getThirdPartyOtherReason()).isEqualTo("Needs assistance");
        Assertions.assertThat(response.getJurorPhoneDetails()).isFalse();
        Assertions.assertThat(response.getJurorEmailDetails()).isFalse();
        Mockito.verify(jurorPaperResponseRepository).save(Mockito.same(response));
        Mockito.verifyNoInteractions(jurorDigitalResponseRepository);
    }

    @Test
    void updatesDigitalDateOfBirthAndPreservesOtherDetails() {
        DigitalResponse response = new DigitalResponse();
        response.setJurorNumber("123456789");
        response.setDateOfBirth(LocalDate.of(1990, 1, 1));
        response.setTitle("Mr");
        response.setFirstName("Original");
        response.setLastName("Person");
        response.setAddressLine1("1 Original Street");
        response.setPostcode("MK5 6LA");
        response.setPhoneNumber("07700900123");
        response.setAltPhoneNumber("07700900456");
        response.setEmail("original@example.com");
        response.setThirdPartyFName("Original contact");
        response.setRelationship("Friend");
        Mockito.doReturn(response).when(jurorDigitalResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);
        request.setDateOfBirth(LocalDate.of(1991, 2, 2));
        request.setTitle("Dr");
        request.setAddressLineOne("2 Updated Street");
        request.setAddressPostcode("SW1A 1AA");
        request.setPrimaryPhone("07700900789");
        request.setSecondaryPhone("07700900987");
        request.setEmailAddress("updated@example.com");
        request.setThirdParty(JurorPaperResponseDto.ThirdParty.builder()
            .thirdPartyFName("Updated contact").relationship("Sibling").build());
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789");

        Assertions.assertThat(response.getDateOfBirth()).isEqualTo(LocalDate.of(1991, 2, 2));
        Assertions.assertThat(response.getTitle()).isEqualTo("Mr");
        Assertions.assertThat(response.getFirstName()).isEqualTo("Original");
        Assertions.assertThat(response.getLastName()).isEqualTo("Person");
        Assertions.assertThat(response.getAddressLine1()).isEqualTo("1 Original Street");
        Assertions.assertThat(response.getPostcode()).isEqualTo("MK5 6LA");
        Assertions.assertThat(response.getPhoneNumber()).isEqualTo("07700900123");
        Assertions.assertThat(response.getAltPhoneNumber()).isEqualTo("07700900456");
        Assertions.assertThat(response.getEmail()).isEqualTo("original@example.com");
        Assertions.assertThat(response.getThirdPartyFName()).isEqualTo("Original contact");
        Assertions.assertThat(response.getRelationship()).isEqualTo("Friend");
        Mockito.verify(jurorDigitalResponseRepository).save(Mockito.same(response));
        Mockito.verifyNoInteractions(jurorPaperResponseRepository);
    }

    @Test
    void doesNotSavePaperResponseWhenDetailsAreUnchanged() {
        PaperResponse response = new PaperResponse();
        response.setJurorNumber("123456789");
        response.setFirstName("FName");
        response.setLastName("LName");
        response.setAddressLine1("1 Test Street");
        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        request.setAddressLineOne("1 Test Street");
        request.setThirdParty(null);
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789");

        Assertions.assertThat(response.getFirstName()).isEqualTo("FName");
        Assertions.assertThat(response.getLastName()).isEqualTo("LName");
        Assertions.assertThat(response.getAddressLine1()).isEqualTo("1 Test Street");
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any(PaperResponse.class));
        Mockito.verifyNoInteractions(jurorDigitalResponseRepository, straightThroughProcessorService);
    }

    @Test
    void doesNotSaveDigitalResponseWhenOnlyIgnoredDetailsChange() {
        DigitalResponse response = new DigitalResponse();
        response.setJurorNumber("123456789");
        response.setFirstName("Original");
        response.setAddressLine1("1 Original Street");
        Mockito.doReturn(response).when(jurorDigitalResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);
        request.setAddressLineOne("2 Updated Street");
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789");

        Assertions.assertThat(response.getFirstName()).isEqualTo("Original");
        Assertions.assertThat(response.getAddressLine1()).isEqualTo("1 Original Street");
        Mockito.verify(jurorDigitalResponseRepository, Mockito.never()).save(any(DigitalResponse.class));
        Mockito.verifyNoInteractions(jurorPaperResponseRepository, straightThroughProcessorService);
    }

    @Test
    void preservesPaperThirdPartyDetailsWhenRequestHasNoThirdParty() {
        PaperResponse response = new PaperResponse();
        response.setJurorNumber("123456789");
        response.setFirstName("FName");
        response.setLastName("LName");
        response.setThirdPartyFName("Other");
        response.setEmailAddress("third.party@example.com");
        response.setRelationship("Friend");
        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        request.setThirdParty(null);
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789");

        Assertions.assertThat(response.getThirdPartyFName()).isEqualTo("Other");
        Assertions.assertThat(response.getEmailAddress()).isEqualTo("third.party@example.com");
        Assertions.assertThat(response.getRelationship()).isEqualTo("Friend");
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any(PaperResponse.class));
        Mockito.verifyNoInteractions(jurorDigitalResponseRepository);
    }

    @Test
    void clearsPaperOptionalContactDetailsWhenRequestValuesAreNull() {
        PaperResponse response = new PaperResponse();
        response.setJurorNumber("123456789");
        response.setFirstName("FName");
        response.setLastName("LName");
        response.setPhoneNumber("07700900123");
        response.setAltPhoneNumber("07700900456");
        response.setEmail("original@example.com");
        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        request.setThirdParty(null);
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789");

        Assertions.assertThat(response.getPhoneNumber()).isNull();
        Assertions.assertThat(response.getAltPhoneNumber()).isNull();
        Assertions.assertThat(response.getEmail()).isNull();
        Assertions.assertThat(response.getFirstName()).isEqualTo("FName");
        Assertions.assertThat(response.getLastName()).isEqualTo("LName");
        Mockito.verify(jurorPaperResponseRepository).save(Mockito.same(response));
        Mockito.verifyNoInteractions(jurorDigitalResponseRepository);
    }

    @Test
    void rejectsMissingPaperResponseWithoutSavingOrProcessing() {
        Mockito.doReturn(null).when(jurorPaperResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789"));

        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any(PaperResponse.class));
        Mockito.verifyNoInteractions(jurorDigitalResponseRepository, straightThroughProcessorService);
    }

    @Test
    void rejectsMissingDigitalResponseWithoutSavingOrProcessing() {
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).findByJurorNumber("123456789");
        JurorPersonalDetailsDto request = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorResponseService.updateJurorPersonalDetails(payload, request, "123456789"));

        Mockito.verify(jurorDigitalResponseRepository, Mockito.never()).save(any(DigitalResponse.class));
        Mockito.verifyNoInteractions(jurorPaperResponseRepository, straightThroughProcessorService);
    }

    private JurorPool createTestJurorPool(String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setName("Chester");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setReturnDate(LocalDate.now().plusWeeks(8));

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);
        jurorStatus.setStatusDesc("Responded");

        Juror juror = new Juror();
        juror.setTitle("Dr");
        juror.setFirstName("Test");
        juror.setLastName("Person");
        juror.setAddressLine1("Address Line One");
        juror.setAddressLine2("Address Line Two");
        juror.setAddressLine3("Address Line Three");
        juror.setAddressLine4("Town");
        juror.setAddressLine5("County");
        juror.setPostcode("PO19 1SX");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private PoolRequest mockPoolRequest(String poolNumber, String owner) {
        PoolRequest mockPoolRequest = new PoolRequest();
        mockPoolRequest.setPoolNumber(poolNumber);
        mockPoolRequest.setOwner(owner);
        mockPoolRequest.setReturnDate(LocalDate.now().plusWeeks(3));
        return mockPoolRequest;
    }

    private JurorPersonalDetailsDto buildJurorPersonalDetailsDto(ReplyMethod method) {

        JurorPersonalDetailsDto jurorPersonalDetailsDto = new JurorPersonalDetailsDto();
        jurorPersonalDetailsDto.setReplyMethod(method);
        jurorPersonalDetailsDto.setFirstName("FName");
        jurorPersonalDetailsDto.setLastName("LName");
        JurorPaperResponseDto.ThirdParty thirdParty = JurorPaperResponseDto.ThirdParty.builder()
            .relationship("relationship")
            .thirdPartyReason("The reason text")
            .build();
        jurorPersonalDetailsDto.setThirdParty(thirdParty);
        return jurorPersonalDetailsDto;
    }

    private void assertPaperResponsePersonalDetailsMockitoVerification(
        int jurorPoolRepoFind,
        int jurorPoolRepoFindOrdered,
        int jurorPaperResponseRepoFind,
        int jurorPaperResponseRepoSave,
        int straightThroughProcessorServiceIsValidPaper,
        int straightThroughProcessorServiceIsValidDigital,
        int straightThroughProcessorProcessAgeDisqPaper,
        int straightThroughProcessorProcessAgeDisqDigital,
        int jurorDigitalResponseRepositoryFind,
        int jurorDigitalResponseRepositorySave) {
        Mockito.verify(jurorPoolRepository, Mockito.times(jurorPoolRepoFind))
            .findByJurorJurorNumberAndIsActive(any(), Mockito.anyBoolean());
        Mockito.verify(jurorPoolRepository, Mockito.times(jurorPoolRepoFindOrdered))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(jurorPaperResponseRepoFind))
            .findByJurorNumber(any(String.class));
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(jurorPaperResponseRepoSave))
            .save(any(PaperResponse.class));

        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorServiceIsValidPaper))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class),
                any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorServiceIsValidDigital))
            .isValidForStraightThroughAgeDisqualification(any(DigitalResponse.class),
                any(LocalDate.class),
                any(JurorPool.class));

        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorProcessAgeDisqPaper))
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorProcessAgeDisqDigital))
            .processAgeDisqualification(any(DigitalResponse.class),
                any(JurorPool.class), any());

        Mockito.verify(jurorDigitalResponseRepository, Mockito.times(jurorDigitalResponseRepositoryFind))
            .findByJurorNumber(any(String.class));
        Mockito.verify(jurorDigitalResponseRepository, Mockito.times(jurorDigitalResponseRepositorySave))
            .save(any(DigitalResponse.class));
    }
}
