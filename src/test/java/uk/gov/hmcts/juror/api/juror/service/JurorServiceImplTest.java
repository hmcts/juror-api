package uk.gov.hmcts.juror.api.juror.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.AppSettingRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.ReplyTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.ReasonableAdjustmentsRepository;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestService;
import uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link JurorServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
@SuppressWarnings("PMD.LawOfDemeter")
public class JurorServiceImplTest {

    private static final String TEST_JUROR_NUMBER = "209092530";

    @Mock
    private JurorReasonableAdjustmentRepository specialNeedRepository;

    @Mock
    private ReplyTypeRepository replyTypeRepository;
    @Mock
    private ReasonableAdjustmentsRepository reasonableAdjustmentsRepository;

    @Mock
    private JurorResponseCjsEmploymentRepositoryMod cjsEmploymentRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @Mock
    private JurorPoolRepository poolDetailsRepository;

    @Mock
    private StraightThroughProcessor straightThroughProcessor;

    @Mock
    private UrgencyService urgencyService;

    @Mock
    private AppSettingRepository appSettingRepository;

    @Mock
    private UserService mockUserService;

    @Mock
    private PoolRequestService mockUniquePoolService;

    @Mock
    private JurorNotificationService jurorNotificationService;

    @Mock
    private ResponseInspectorImpl responseInspector;

    @Mock
    private JurorService mockJurorService;

    @InjectMocks
    private JurorServiceImpl defaultService;

    @InjectMocks
    private JurorPersistenceServiceImpl jurorPersistenceService;

    private JurorPool jurorPoolDetails;

    private Juror juror;

    @Before
    public void setup() {
        juror = new Juror();
        PoolRequest poolRequest = new PoolRequest();

        jurorPoolDetails = new JurorPool();
        jurorPoolDetails.setJuror(juror);
        jurorPoolDetails.setPool(poolRequest);
        juror = jurorPoolDetails.getJuror();

        poolRequest.setPoolNumber("101");
        juror.setJurorNumber(TEST_JUROR_NUMBER);
        juror.setTitle("Dr");
        juror.setFirstName("Jane");
        juror.setLastName("CASTILLO");


        final CourtLocation testCourt = new CourtLocation();
        testCourt.setCourtAttendTime(LocalTime.of(9, 0));
        poolRequest.setCourtLocation(testCourt);
        jurorPoolDetails.getPool().setCourtLocation(testCourt);
    }

    @Test
    public void getJurorByByJurorNumber_WithJurorNumber_ReturnsJurorDetails() {

        doReturn(jurorPoolDetails).when(poolDetailsRepository).findByJurorJurorNumber(TEST_JUROR_NUMBER);

        final JurorDetailDto jurorDto = defaultService.getJurorByJurorNumber(TEST_JUROR_NUMBER);
        assertThat(jurorDto)
            .extracting("jurorNumber", "title", "firstName", "lastName")
            .contains(jurorPoolDetails.getJurorNumber(), jurorPoolDetails.getJuror().getTitle(),
                jurorPoolDetails.getJuror().getFirstName(),
                jurorPoolDetails.getJuror().getLastName());
        assertThat(jurorDto.getCourtAttendTime()).isEqualTo("09:00");
    }

    /**
     * Tests that attend time in the JUROR.UNIQUE_POOL table (if present) overrides the court attend time
     *
     * @since JDB-2042
     */
    @Test
    public void getJurorByJurorNumber_alternatePath_uniquePoolAttendTime() {
        doReturn("8am").when(mockUniquePoolService).getPoolAttendanceTime("101");
        doReturn(jurorPoolDetails).when(poolDetailsRepository).findByJurorJurorNumber(TEST_JUROR_NUMBER);

        final JurorDetailDto jurorDto = defaultService.getJurorByJurorNumber(TEST_JUROR_NUMBER);
        assertThat(jurorDto.getCourtAttendTime()).isEqualTo("8am");
    }

    @Test
    public void convertJurorResponseDtoToEntityTest() throws Exception {
        final String jurorNumber = "546547731";
        final LocalDate dob = LocalDateTime.now().toLocalDate();
        final String title = "Dr";
        final String firstName = "Namey";
        final String lastName = "McName";
        final String phoneNumber = "01234 123456";
        final String altPhoneNumber = "0812 345 6789";
        final String email = "mctest@test.com";
        final String address1 = "Flat 8/2";
        final String address2 = "84";
        final String address3 = "Test Street";
        final String address4 = "Testville";
        final String address5 = "Testshire";
        final String postcode = "T12 3YZ";
        final boolean livedConsecutivelyAnswer = false;
        final String livedConsecutivelyDetail = "Just moved to the UK.";
        final boolean onBailAnswer = true;
        final String onBailDetail = "I'm out on bail.";
        final boolean convictionsAnswer = false;
        final String convictionsDetail = "No convictions.";
        final boolean mentalHealthActAnswer = true;
        final String mentalHealthActDetail = "I have been sectioned in the past.";
        final String cjsEmployer = "Police";
        final String cjsEmployerDetails = "I am a policeman";
        final String specialNeedType = "M";
        final String specialNeedDetail = "I have a broken leg.";
        final String specialArrangements = "I cannot get up stairs easily.";
        final String deferralReason = "I am in hospital.";
        final String deferralDates = "I'm in traction for another 6 weeks.";
        final String excusalReason = "I am needed to solve a big case";
        final String thirdPartyFname = "Nameina";
        final String thirdPartyLname = "McName";
        final String thirdPartyReason = "My husband gets no wifi signal in the hospital.";
        final Integer version = 999;

        final JurorResponseDto dto = JurorResponseDto.realBuilder()
            .jurorNumber(jurorNumber)
            .dateOfBirth(dob)
            .title(title)
            .firstName(firstName)
            .lastName(lastName)
            .primaryPhone(phoneNumber)
            .secondaryPhone(altPhoneNumber)
            .emailAddress(email)
            .addressLineOne(address1)
            .addressLineTwo(address2)
            .addressLineThree(address3)
            .addressTown(address4)
            .addressCounty(address5)
            .addressPostcode(postcode)
            .qualify(JurorResponseDto.Qualify.builder()
                .livedConsecutive(JurorResponseDto.Answerable.builder()
                    .answer(livedConsecutivelyAnswer)
                    .details(livedConsecutivelyDetail)
                    .build())
                .onBail(JurorResponseDto.Answerable.builder()
                    .answer(onBailAnswer)
                    .details(onBailDetail)
                    .build())
                .convicted(JurorResponseDto.Answerable.builder()
                    .answer(convictionsAnswer)
                    .details(convictionsDetail)
                    .build())
                .mentalHealthAct(JurorResponseDto.Answerable.builder()
                    .answer(mentalHealthActAnswer)
                    .details(mentalHealthActDetail)
                    .build())
                .build()
            )
            .cjsEmployment(Collections.singletonList(JurorResponseDto.CJSEmployment.builder()
                .cjsEmployer(cjsEmployer)
                .cjsEmployerDetails(cjsEmployerDetails)
                .build()))
            .reasonableAdjustments(Collections.singletonList(JurorResponseDto.ReasonableAdjustment.builder()
                .assistanceType(specialNeedType)
                .assistanceTypeDetails(specialNeedDetail)
                .build()))
            .assistanceSpecialArrangements(specialArrangements)
            .thirdParty(JurorResponseDto.ThirdParty.builder()
                .thirdPartyFName(thirdPartyFname)
                .thirdPartyLName(thirdPartyLname)
                .thirdPartyReason(thirdPartyReason).build())
            .excusal(JurorResponseDto.Excusal.builder()
                .reason(excusalReason)
                .build())
            .deferral(JurorResponseDto.Deferral.builder()
                .reason(deferralReason)
                .dates(deferralDates)
                .build())
            .version(version)
            .replyMethod(ReplyMethod.DIGITAL)
            .build();

        // set mocks to return the saved entities when repositories are invoked, simulating receiving back the
        // attached entity.
        //when(tSpecialRepository.findOne(any(Predicate.class))).thenReturn(new TSpecial(SPECIAL_NEED_TYPE, "Spec
        // need description"));
        when(reasonableAdjustmentsRepository.findByCode(anyString()))
            .thenReturn(new ReasonableAdjustments(specialNeedType, "Spec need " + "description"));

        when(replyTypeRepository.findById(anyString())).thenReturn(
            Optional.of(new ReplyType("DIGITAL", "Digital")));


        final DigitalResponse entity = defaultService.convertJurorResponseDtoToEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(entity.getDateOfBirth()).isEqualTo(dob);
        assertThat(entity.getTitle()).isEqualTo(title);
        assertThat(entity.getFirstName()).isEqualTo(firstName);
        assertThat(entity.getLastName()).isEqualTo(lastName);
        assertThat(entity.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(entity.getAltPhoneNumber()).isEqualTo(altPhoneNumber);
        assertThat(entity.getEmail()).isEqualTo(email);
        assertThat(entity.getAddressLine1()).isEqualTo(address1);
        assertThat(entity.getAddressLine2()).isEqualTo(address2);
        assertThat(entity.getAddressLine3()).isEqualTo(address3);
        assertThat(entity.getAddressLine4()).isEqualTo(address4);
        assertThat(entity.getAddressLine5()).isEqualTo(address5);
        assertThat(entity.getPostcode()).isEqualTo(postcode);

        assertThat(entity.getResidency()).isEqualTo(livedConsecutivelyAnswer);
        assertThat(entity.getResidencyDetail()).isEqualTo(livedConsecutivelyDetail);
        assertThat(entity.getBail()).isEqualTo(onBailAnswer);
        assertThat(entity.getBailDetails()).isEqualTo(onBailDetail);
        assertThat(entity.getConvictions()).isEqualTo(convictionsAnswer);
        assertThat(entity.getConvictionsDetails()).isEqualTo(convictionsDetail);
        assertThat(entity.getMentalHealthAct()).isEqualTo(mentalHealthActAnswer);
        assertThat(entity.getMentalHealthActDetails()).isEqualTo(mentalHealthActDetail);

        assertThat(entity.getCjsEmployments()).hasSize(1);
        assertThat(entity.getCjsEmployments().get(0).getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(entity.getCjsEmployments().get(0).getCjsEmployer()).isEqualTo(cjsEmployer);
        assertThat(entity.getCjsEmployments().get(0).getCjsEmployerDetails()).isEqualTo(cjsEmployerDetails);

        assertThat(entity.getReasonableAdjustments()).hasSize(1);
        assertThat(entity.getReasonableAdjustments().get(0).getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(entity.getReasonableAdjustments().get(0).getReasonableAdjustment().getCode())
            .isEqualTo(specialNeedType);
        assertThat(entity.getReasonableAdjustments().get(0).getReasonableAdjustmentDetail())
            .isEqualTo(specialNeedDetail);
        assertThat(entity.getReasonableAdjustmentsArrangements()).isEqualTo(specialArrangements);

        assertThat(entity.getDeferralReason()).isEqualTo(deferralReason);
        assertThat(entity.getDeferralDate()).isEqualTo(deferralDates);

        assertThat(entity.getExcusalReason()).isEqualTo(excusalReason);

        assertThat(entity.getThirdPartyFName()).isEqualTo(thirdPartyFname);
        assertThat(entity.getThirdPartyLName()).isEqualTo(thirdPartyLname);
        assertThat(entity.getThirdPartyReason()).isEqualTo(thirdPartyReason);

        assertThat(entity.getDateReceived()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(entity.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

        assertThat(entity.getVersion()).isEqualTo(version);


        verify(poolDetailsRepository, times(1)).findByJurorJurorNumber(jurorNumber);
        verify(urgencyService, times(1)).setUrgencyFlags(eq(entity), Mockito.isNull());
    }

    @Test
    public void processStraightThroughAcceptance_happyPath_processAcceptanceCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final DigitalResponse jurorResponse = mock(DigitalResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);


        // ensure the processing of Straight-Through Acceptance is enabled
        //given(appSettingRepository.findById(StraightThroughType.ACCEPTANCE.getDbName())).willReturn(null);
        given(appSettingRepository.findById(anyString())).willReturn(Optional.empty());

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        // check we executed the straight-through acceptance logic
        verify(straightThroughProcessor).processAcceptance(any(DigitalResponse.class));
    }

    @Test
    public void processStraightThroughAcceptance_unhappyPath_processAcceptanceNotCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final DigitalResponse jurorResponse = mock(DigitalResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);


        // ensure the processing of Straight-Through Acceptance is disabled
        AppSetting appSettingAcceptanceTrue = new AppSetting(StraightThroughType.ACCEPTANCE.getDbName(), "TRUE");
        given(appSettingRepository.findById(StraightThroughType.ACCEPTANCE.getDbName()))
            .willReturn(any(Optional.class));

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        // check we didn't execute the straight-through acceptance logic
        verify(straightThroughProcessor, times(0)).processAcceptance(any(DigitalResponse.class));
    }

    @Test
    public void processDeceasedExcusal_happyPath_processDeceasedExcusalCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final DigitalResponse jurorResponse = mock(DigitalResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);

        // ensure the processing of Straight-Through Acceptance is disabled and Deceased-Excusals is enabled
        AppSetting appSettingAcceptanceTrue = new AppSetting(StraightThroughType.ACCEPTANCE.getDbName(), "TRUE");

        given(appSettingRepository.findById(anyString()))
            .willReturn(Optional.of(appSettingAcceptanceTrue))
            .willReturn(Optional.empty());

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        verify(straightThroughProcessor, times(0)).processAcceptance(any(DigitalResponse.class));
        verify(straightThroughProcessor).processDeceasedExcusal(any(DigitalResponse.class));
        verify(straightThroughProcessor, times(0)).processAgeExcusal(any(DigitalResponse.class));
    }

    @Test
    public void processDeceasedExcusal_happyPath_processDeceasedExcusalNotCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final DigitalResponse jurorResponse = mock(DigitalResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);


        // ensure the processing of Straight-Through Acceptance and Deceased-Excusals are disabled
        AppSetting appSettingAcceptanceTrue = new AppSetting(StraightThroughType.ACCEPTANCE.getDbName(), "TRUE");
        AppSetting appSettingDeceasedExcusalTrue = new AppSetting(StraightThroughType.DECEASED_EXCUSAL.getDbName(),
            "TRUE");

        // return TRUE the first two DB checks, then null for age excusal check after
        given(appSettingRepository.findById(anyString()))
            .willReturn(Optional.of(appSettingAcceptanceTrue))
            .willReturn(Optional.of(appSettingDeceasedExcusalTrue))
            .willReturn(Optional.empty());

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        // check we didn't execute the deceased-excusal logic
        verify(straightThroughProcessor, times(0)).processAcceptance(any(DigitalResponse.class));
        verify(straightThroughProcessor, times(0)).processDeceasedExcusal(any(DigitalResponse.class));
    }

    @Test
    public void processAgeExcusal_happyPath_ageExcusalCalled() throws StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final DigitalResponse jurorResponse = mock(DigitalResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);


        // ensure the processing of age-excusal is enabled
        AppSetting appSettingAcceptanceTrue = new AppSetting(StraightThroughType.ACCEPTANCE.getDbName(), "TRUE");
        AppSetting appSettingDeceasedExcusalTrue = new AppSetting(StraightThroughType.DECEASED_EXCUSAL.getDbName(),
            "TRUE");

        // return TRUE the first two DB checks, then null for age excusal check after
        given(appSettingRepository.findById(anyString()))
            .willReturn(Optional.of(appSettingAcceptanceTrue))
            .willReturn(Optional.of(appSettingDeceasedExcusalTrue))
            .willReturn(Optional.empty());

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        // check we executed the age-excusal logic
        verify(straightThroughProcessor, times(0)).processAcceptance(any(DigitalResponse.class));
        verify(straightThroughProcessor, times(0))
            .processDeceasedExcusal(any(DigitalResponse.class));
        verify(straightThroughProcessor).processAgeExcusal(any(DigitalResponse.class));
    }

    @Test
    public void processAgeExcusal_unhappyPath_ageExcusalNotCalled() throws StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final DigitalResponse jurorResponse = mock(DigitalResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);


        // ensure the processing of age excusal is disabled
        AppSetting appSettingAcceptanceTrue = new AppSetting(StraightThroughType.ACCEPTANCE.getDbName(), "TRUE");
        AppSetting appSettingDeceasedExcusalTrue = new AppSetting(StraightThroughType.DECEASED_EXCUSAL.getDbName(),
            "TRUE");
        AppSetting appSettingAgeExcusalTrue = new AppSetting(StraightThroughType.AGE_EXCUSAL.getDbName(), "TRUE");

        // return TRUE for all DB checks on the straight-through switches
        given(appSettingRepository.findById(anyString()))
            .willReturn(Optional.of(appSettingAcceptanceTrue))
            .willReturn(Optional.of(appSettingDeceasedExcusalTrue))
            .willReturn(Optional.of(appSettingAgeExcusalTrue));

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        // check we executed the age-excusal logic
        verify(straightThroughProcessor, times(0)).processAcceptance(any(DigitalResponse.class));
        verify(straightThroughProcessor, times(0))
            .processDeceasedExcusal(any(DigitalResponse.class));
        verify(straightThroughProcessor, times(0)).processAgeExcusal(any(DigitalResponse.class));
    }
}
