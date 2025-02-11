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

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class JurorResponseServiceImplTest {
    private JurorPoolRepository jurorPoolRepository;
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    private StraightThroughProcessorService straightThroughProcessorService;
    private UserRepository userRepository;

    private JurorResponseServiceImpl jurorResponseService;

    @BeforeEach
    void setUpMocks() {

        jurorPaperResponseRepository = Mockito.mock(JurorPaperResponseRepositoryMod.class);
        jurorPoolRepository = Mockito.mock(JurorPoolRepository.class);
        jurorPaperResponseRepository = Mockito.mock(JurorPaperResponseRepositoryMod.class);
        jurorDigitalResponseRepository = Mockito.mock(JurorDigitalResponseRepositoryMod.class);
        straightThroughProcessorService = Mockito.mock(StraightThroughProcessorService.class);
        JurorCommonResponseRepositoryMod jurorCommonResponseRepository =
            Mockito.mock(JurorCommonResponseRepositoryMod.class);
        SummonsReplyMergeService mergeService = Mockito.mock(SummonsReplyMergeService.class);
        JurorResponseAuditRepositoryMod jurorResponseAuditRepository =
            Mockito.mock(JurorResponseAuditRepositoryMod.class);
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
            .findByJurorNumber(Mockito.any());
        Mockito.doReturn(new DigitalResponse()).when(jurorDigitalResponseRepository).findByJurorNumber(Mockito.any());

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

        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(Mockito.any(PaperResponse.class));
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(Mockito.any(DigitalResponse.class));

        Mockito.doReturn(false).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(Mockito.any(PaperResponse.class),
                Mockito.any(LocalDate.class), Mockito.any(JurorPool.class));
        Mockito.doReturn(false).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(Mockito.any(DigitalResponse.class),
                Mockito.any(LocalDate.class), Mockito.any(JurorPool.class));
        Mockito.doNothing().when(straightThroughProcessorService)
            .processAgeDisqualification(Mockito.any(PaperResponse.class), Mockito.any(LocalDate.class),
                Mockito.any(JurorPool.class), Mockito.any());
        PoolRequestRepository poolRequestRepository = Mockito.mock(PoolRequestRepository.class);
        Mockito.when(poolRequestRepository.findByPoolNumber(any()))
            .thenReturn(Optional.of(mockPoolRequest("12345678", "415")));
    }

    //Tests related to method updateJurorPersonalDetails()
    @Test
    void testUpdatePaperResponse_personalDetails_bureauUser_bureauOwner_happy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            1, 1, 0,
            0, 0,
            0, 0, 0);
    }


    @Test
    void testUpdateDigitalResponsePersonalDetails_bureauUser_bureauOwner_happy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            0, 0, 0,
            0, 0,
            0, 1, 0);
    }

    @Test
    void testUpdatePaperResponsePersonalDetails_courtUser_courtOwner_happy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.PAPER);

        BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321");

        assertPaperResponsePersonalDetailsMockitoVerification(1, 0,
            1, 1, 0,
            0, 0,
            0, 0, 0);
    }

    @Test
    void testUpdateDigitalResponsePersonalDetails_courtUser_courtOwner_happy() {
        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);

        BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "987654321");

        assertPaperResponsePersonalDetailsMockitoVerification(1, 0,
            0, 0, 0,
            0, 0, 0, 1, 0);
    }

    @Test
    void testUpdatePaperResponsePersonalDetails_bureauUser_courtOwnerUserNotAuthorised() {
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
    void testUpdateDigitalResponsePersonalDetails_bureauUser_courtOwnerUserNotAuthorised() {
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
    void testUpdatePaperResponsePersonalDetails_courtUser_courtOwner_noAccess() {
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
    void testUpdateDigitalResponsePersonalDetails_courtUser_courtOwner_noAccess() {
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
            .isValidForStraightThroughAgeDisqualification(Mockito.any(PaperResponse.class),
                Mockito.any(LocalDate.class), Mockito.any(JurorPool.class));

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
            .isValidForStraightThroughAgeDisqualification(Mockito.any(DigitalResponse.class),
                Mockito.any(LocalDate.class), Mockito.any(JurorPool.class));

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
            .isValidForStraightThroughAgeDisqualification(Mockito.any(PaperResponse.class),
                Mockito.any(LocalDate.class), Mockito.any(JurorPool.class));

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
            .isValidForStraightThroughAgeDisqualification(Mockito.any(DigitalResponse.class),
                Mockito.any(LocalDate.class), Mockito.any(JurorPool.class));

        JurorPersonalDetailsDto personalDetailsDto = buildJurorPersonalDetailsDto(ReplyMethod.DIGITAL);
        personalDetailsDto.setDateOfBirth(LocalDate.now().minusYears(80));

        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER", "99");
        jurorResponseService.updateJurorPersonalDetails(payload, personalDetailsDto, "123456789");

        assertPaperResponsePersonalDetailsMockitoVerification(0, 1,
            0, 0, 0,
            1, 0,
            1, 1, 1);
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
            .findByJurorJurorNumberAndIsActive(Mockito.any(), Mockito.anyBoolean());
        Mockito.verify(jurorPoolRepository, Mockito.times(jurorPoolRepoFindOrdered))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(Mockito.any(), Mockito.anyBoolean());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(jurorPaperResponseRepoFind))
            .findByJurorNumber(Mockito.any(String.class));
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(jurorPaperResponseRepoSave))
            .save(Mockito.any(PaperResponse.class));

        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorServiceIsValidPaper))
            .isValidForStraightThroughAgeDisqualification(Mockito.any(PaperResponse.class),
                Mockito.any(LocalDate.class),
                Mockito.any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorServiceIsValidDigital))
            .isValidForStraightThroughAgeDisqualification(Mockito.any(DigitalResponse.class),
                Mockito.any(LocalDate.class),
                Mockito.any(JurorPool.class));

        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorProcessAgeDisqPaper))
            .processAgeDisqualification(Mockito.any(PaperResponse.class), Mockito.any(LocalDate.class),
                Mockito.any(JurorPool.class), Mockito.any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(straightThroughProcessorProcessAgeDisqDigital))
            .processAgeDisqualification(Mockito.any(DigitalResponse.class),
                Mockito.any(JurorPool.class), Mockito.any());

        Mockito.verify(jurorDigitalResponseRepository, Mockito.times(jurorDigitalResponseRepositoryFind))
            .findByJurorNumber(Mockito.any(String.class));
        Mockito.verify(jurorDigitalResponseRepository, Mockito.times(jurorDigitalResponseRepositorySave))
            .save(Mockito.any(DigitalResponse.class));
    }
}
