package uk.gov.hmcts.juror.api.juror.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;
import uk.gov.hmcts.juror.api.bureau.domain.AppSettingRepository;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCJS;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCJSRepository;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.TSpecial;
import uk.gov.hmcts.juror.api.bureau.domain.TSpecialRepository;
import uk.gov.hmcts.juror.api.bureau.service.UniquePoolService;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
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
    private BureauJurorSpecialNeedsRepository specialNeedRepository;

    @Mock
    private TSpecialRepository tSpecialRepository;

    @Mock
    private BureauJurorCJSRepository cjsEmploymentRepository;

    @Mock
    private JurorResponseRepository jurorResponseRepository;

    @Mock
    private PoolRepository poolDetailsRepository;

    @Mock
    private StraightThroughProcessor straightThroughProcessor;

    @Mock
    private UrgencyService urgencyService;

    @Mock
    private AppSettingRepository appSettingRepository;

    @Mock
    private UserService mockUserService;

    @Mock
    private UniquePoolService mockUniquePoolService;

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

    private Pool jurorPoolDetails;

    @Before
    public void setup() {

        jurorPoolDetails = new Pool();
        jurorPoolDetails.setPoolNumber("101");
        jurorPoolDetails.setJurorNumber(TEST_JUROR_NUMBER);
        jurorPoolDetails.setTitle("Dr");
        jurorPoolDetails.setFirstName("Jane");
        jurorPoolDetails.setLastName("CASTILLO");

        final CourtLocation testCourt = new CourtLocation();
        testCourt.setCourtAttendTime("9am");

        jurorPoolDetails.setCourt(testCourt);
    }

    @Test
    public void getJurorByByJurorNumber_WithJurorNumber_ReturnsJurorDetails() {

        doReturn(jurorPoolDetails).when(poolDetailsRepository).findByJurorNumber(TEST_JUROR_NUMBER);

        final JurorDetailDto jurorDto = defaultService.getJurorByJurorNumber(TEST_JUROR_NUMBER);
        assertThat(jurorDto)
            .extracting("jurorNumber", "title", "firstName", "lastName")
            .contains(jurorPoolDetails.getJurorNumber(), jurorPoolDetails.getTitle(), jurorPoolDetails.getFirstName(),
                jurorPoolDetails.getLastName());
        assertThat(jurorDto.getCourtAttendTime()).isEqualTo("9am");
    }

    /**
     * Tests that attend time in the JUROR.UNIQUE_POOL table (if present) overrides the court attend time
     *
     * @since JDB-2042
     */
    @Test
    public void getJurorByJurorNumber_alternatePath_uniquePoolAttendTime() {
        doReturn("8am").when(mockUniquePoolService).getPoolAttendanceTime("101");
        doReturn(jurorPoolDetails).when(poolDetailsRepository).findByJurorNumber(TEST_JUROR_NUMBER);

        final JurorDetailDto jurorDto = defaultService.getJurorByJurorNumber(TEST_JUROR_NUMBER);
        assertThat(jurorDto.getCourtAttendTime()).isEqualTo("8am");
    }

    @Test
    public void convertJurorResponseDtoToEntityTest() throws Exception {
        final String JUROR_NUMBER = "546547731";
        final Date DOB = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
        final String TITLE = "Dr";
        final String FIRST_NAME = "Namey";
        final String LAST_NAME = "McName";
        final String PHONE_NUMBER = "01234 123456";
        final String ALT_PHONE_NUMBER = "0812 345 6789";
        final String EMAIL = "mctest@test.com";
        final String ADDRESS_1 = "Flat 8/2";
        final String ADDRESS_2 = "84";
        final String ADDRESS_3 = "Test Street";
        final String ADDRESS_4 = "Testville";
        final String ADDRESS_5 = "Testshire";
        final String POSTCODE = "T12 3YZ";
        final boolean LIVED_CONSECUTIVELY_ANSWER = false;
        final String LIVED_CONSECUTIVELY_DETAIL = "Just moved to the UK.";
        final boolean ON_BAIL_ANSWER = true;
        final String ON_BAIL_DETAIL = "I'm out on bail.";
        final boolean CONVICTIONS_ANSWER = false;
        final String CONVICTIONS_DETAIL = "No convictions.";
        final boolean MENTAL_HEALTH_ACT_ANSWER = true;
        final String MENTAL_HEALTH_ACT_DETAIL = "I have been sectioned in the past.";
        final String CJS_EMPLOYER = "Police";
        final String CJS_EMPLOYER_DETAILS = "I am a policeman";
        final String SPECIAL_NEED_TYPE = "M";
        final String SPECIAL_NEED_DETAILS = "I have a broken leg.";
        final String SPECIAL_ARRANGEMENTS = "I cannot get up stairs easily.";
        final String DEFERRAL_REASON = "I am in hospital.";
        final String DEFERRAL_DATES = "I'm in traction for another 6 weeks.";
        final String EXCUSAL_REASON = "I am needed to solve a big case";
        final String THIRD_PARTY_FNAME = "Nameina";
        final String THIRD_PARTY_LNAME = "McName";
        final String THIRD_PARTY_REASON = "My husband gets no wifi signal in the hospital.";
        final Integer VERSION = 999;

        final JurorResponseDto dto = JurorResponseDto.realBuilder()
            .jurorNumber(JUROR_NUMBER)
            .dateOfBirth(DOB)
            .title(TITLE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .primaryPhone(PHONE_NUMBER)
            .secondaryPhone(ALT_PHONE_NUMBER)
            .emailAddress(EMAIL)
            .addressLineOne(ADDRESS_1)
            .addressLineTwo(ADDRESS_2)
            .addressLineThree(ADDRESS_3)
            .addressTown(ADDRESS_4)
            .addressCounty(ADDRESS_5)
            .addressPostcode(POSTCODE)
            .qualify(JurorResponseDto.Qualify.builder()
                .livedConsecutive(JurorResponseDto.Answerable.builder()
                    .answer(LIVED_CONSECUTIVELY_ANSWER)
                    .details(LIVED_CONSECUTIVELY_DETAIL)
                    .build())
                .onBail(JurorResponseDto.Answerable.builder()
                    .answer(ON_BAIL_ANSWER)
                    .details(ON_BAIL_DETAIL)
                    .build())
                .convicted(JurorResponseDto.Answerable.builder()
                    .answer(CONVICTIONS_ANSWER)
                    .details(CONVICTIONS_DETAIL)
                    .build())
                .mentalHealthAct(JurorResponseDto.Answerable.builder()
                    .answer(MENTAL_HEALTH_ACT_ANSWER)
                    .details(MENTAL_HEALTH_ACT_DETAIL)
                    .build())
                .build()
            )
            .cjsEmployment(Collections.singletonList(JurorResponseDto.CJSEmployment.builder()
                .cjsEmployer(CJS_EMPLOYER)
                .cjsEmployerDetails(CJS_EMPLOYER_DETAILS)
                .build()))
            .specialNeeds(Collections.singletonList(JurorResponseDto.SpecialNeed.builder()
                .assistanceType(SPECIAL_NEED_TYPE)
                .assistanceTypeDetails(SPECIAL_NEED_DETAILS)
                .build()))
            .assistanceSpecialArrangements(SPECIAL_ARRANGEMENTS)
            .thirdParty(JurorResponseDto.ThirdParty.builder()
                .thirdPartyFName(THIRD_PARTY_FNAME)
                .thirdPartyLName(THIRD_PARTY_LNAME)
                .thirdPartyReason(THIRD_PARTY_REASON).build())
            .excusal(JurorResponseDto.Excusal.builder()
                .reason(EXCUSAL_REASON)
                .build())
            .deferral(JurorResponseDto.Deferral.builder()
                .reason(DEFERRAL_REASON)
                .dates(DEFERRAL_DATES)
                .build())
            .version(VERSION)
            .build();

        // set mocks to return the saved entities when repositories are invoked, simulating receiving back the
        // attached entity.
        //when(tSpecialRepository.findOne(any(Predicate.class))).thenReturn(new TSpecial(SPECIAL_NEED_TYPE, "Spec
        // need description"));
        when(tSpecialRepository.findByCode(anyString())).thenReturn(new TSpecial(SPECIAL_NEED_TYPE,
            "Spec need " + "description"));
        // echo back the input
        when(specialNeedRepository.save(any(BureauJurorSpecialNeed.class))).thenAnswer(
            invocation -> invocation.getArgument(0));
        // echo back the input
        when(cjsEmploymentRepository.save(any(BureauJurorCJS.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        final JurorResponse entity = defaultService.convertJurorResponseDtoToEntity(dto);
        verify(specialNeedRepository).save(any(BureauJurorSpecialNeed.class));
        verify(cjsEmploymentRepository).save(any(BureauJurorCJS.class));

        assertThat(entity).isNotNull();
        assertThat(entity.getJurorNumber()).isEqualTo(JUROR_NUMBER);
        assertThat(entity.getDateOfBirth()).isEqualTo(DOB);
        assertThat(entity.getTitle()).isEqualTo(TITLE);
        assertThat(entity.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(entity.getLastName()).isEqualTo(LAST_NAME);
        assertThat(entity.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(entity.getAltPhoneNumber()).isEqualTo(ALT_PHONE_NUMBER);
        assertThat(entity.getEmail()).isEqualTo(EMAIL);
        assertThat(entity.getAddress()).isEqualTo(ADDRESS_1);
        assertThat(entity.getAddress2()).isEqualTo(ADDRESS_2);
        assertThat(entity.getAddress3()).isEqualTo(ADDRESS_3);
        assertThat(entity.getAddress4()).isEqualTo(ADDRESS_4);
        assertThat(entity.getAddress5()).isEqualTo(ADDRESS_5);
        assertThat(entity.getPostcode()).isEqualTo(POSTCODE);

        assertThat(entity.getResidency()).isEqualTo(LIVED_CONSECUTIVELY_ANSWER);
        assertThat(entity.getResidencyDetail()).isEqualTo(LIVED_CONSECUTIVELY_DETAIL);
        assertThat(entity.getBail()).isEqualTo(ON_BAIL_ANSWER);
        assertThat(entity.getBailDetails()).isEqualTo(ON_BAIL_DETAIL);
        assertThat(entity.getConvictions()).isEqualTo(CONVICTIONS_ANSWER);
        assertThat(entity.getConvictionsDetails()).isEqualTo(CONVICTIONS_DETAIL);
        assertThat(entity.getMentalHealthAct()).isEqualTo(MENTAL_HEALTH_ACT_ANSWER);
        assertThat(entity.getMentalHealthActDetails()).isEqualTo(MENTAL_HEALTH_ACT_DETAIL);

        assertThat(entity.getCjsEmployments()).hasSize(1);
        assertThat(entity.getCjsEmployments().get(0).getJurorNumber()).isEqualTo(JUROR_NUMBER);
        assertThat(entity.getCjsEmployments().get(0).getEmployer()).isEqualTo(CJS_EMPLOYER);
        assertThat(entity.getCjsEmployments().get(0).getDetails()).isEqualTo(CJS_EMPLOYER_DETAILS);

        assertThat(entity.getSpecialNeeds()).hasSize(1);
        assertThat(entity.getSpecialNeeds().get(0).getJurorNumber()).isEqualTo(JUROR_NUMBER);
        assertThat(entity.getSpecialNeeds().get(0).getSpecialNeed().getCode()).isEqualTo(SPECIAL_NEED_TYPE);
        assertThat(entity.getSpecialNeeds().get(0).getDetail()).isEqualTo(SPECIAL_NEED_DETAILS);
        assertThat(entity.getSpecialNeedsArrangements()).isEqualTo(SPECIAL_ARRANGEMENTS);

        assertThat(entity.getDeferralReason()).isEqualTo(DEFERRAL_REASON);
        assertThat(entity.getDeferralDate()).isEqualTo(DEFERRAL_DATES);

        assertThat(entity.getExcusalReason()).isEqualTo(EXCUSAL_REASON);

        assertThat(entity.getThirdPartyFName()).isEqualTo(THIRD_PARTY_FNAME);
        assertThat(entity.getThirdPartyLName()).isEqualTo(THIRD_PARTY_LNAME);
        assertThat(entity.getThirdPartyReason()).isEqualTo(THIRD_PARTY_REASON);

        assertThat(entity.getDateReceived()).isBefore(Date.from(Instant.now().plusSeconds(1)));
        assertThat(entity.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

        assertThat(entity.getVersion()).isEqualTo(VERSION);

        verify(poolDetailsRepository, times(1)).findByJurorNumber(JUROR_NUMBER);
        verify(urgencyService, times(1)).setUrgencyFlags(eq(entity), Mockito.isNull());
    }

    @Test
    public void processStraightThroughAcceptance_happyPath_processAcceptanceCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final JurorResponse jurorResponse = mock(JurorResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);


        // ensure the processing of Straight-Through Acceptance is enabled
        //given(appSettingRepository.findById(StraightThroughType.ACCEPTANCE.getDbName())).willReturn(null);
        given(appSettingRepository.findById(anyString())).willReturn(Optional.empty());

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        // check we executed the straight-through acceptance logic
        verify(straightThroughProcessor).processAcceptance(any(JurorResponse.class));
    }

    @Test
    public void processStraightThroughAcceptance_unhappyPath_processAcceptanceNotCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final JurorResponse jurorResponse = mock(JurorResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);


        // ensure the processing of Straight-Through Acceptance is disabled
        AppSetting appSettingAcceptanceTrue = new AppSetting(StraightThroughType.ACCEPTANCE.getDbName(), "TRUE");
        given(appSettingRepository.findById(StraightThroughType.ACCEPTANCE.getDbName()))
            .willReturn(any(Optional.class));

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        // check we didn't execute the straight-through acceptance logic
        verify(straightThroughProcessor, times(0)).processAcceptance(any(JurorResponse.class));
    }

    @Test
    public void processDeceasedExcusal_happyPath_processDeceasedExcusalCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final JurorResponse jurorResponse = mock(JurorResponse.class);

        given(mockJurorService.saveResponse(any(JurorResponseDto.class))).willReturn(jurorResponse);

        // ensure the processing of Straight-Through Acceptance is disabled and Deceased-Excusals is enabled
        AppSetting appSettingAcceptanceTrue = new AppSetting(StraightThroughType.ACCEPTANCE.getDbName(), "TRUE");

        given(appSettingRepository.findById(anyString()))
            .willReturn(Optional.of(appSettingAcceptanceTrue))
            .willReturn(Optional.empty());

        // process response
        Boolean success = jurorPersistenceService.persistJurorResponse(responseDto);

        assertThat(success).isTrue();
        verify(straightThroughProcessor, times(0)).processAcceptance(any(JurorResponse.class));
        verify(straightThroughProcessor).processDeceasedExcusal(any(JurorResponse.class));
        verify(straightThroughProcessor, times(0)).processAgeExcusal(any(JurorResponse.class));
    }

    @Test
    public void processDeceasedExcusal_happyPath_processDeceasedExcusalNotCalled() throws
        StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final JurorResponse jurorResponse = mock(JurorResponse.class);

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
        verify(straightThroughProcessor, times(0)).processAcceptance(any(JurorResponse.class));
        verify(straightThroughProcessor, times(0)).processDeceasedExcusal(any(JurorResponse.class));
    }

    @Test
    public void processAgeExcusal_happyPath_ageExcusalCalled() throws StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final JurorResponse jurorResponse = mock(JurorResponse.class);

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
        verify(straightThroughProcessor, times(0)).processAcceptance(any(JurorResponse.class));
        verify(straightThroughProcessor, times(0))
            .processDeceasedExcusal(any(JurorResponse.class));
        verify(straightThroughProcessor).processAgeExcusal(any(JurorResponse.class));
    }

    @Test
    public void processAgeExcusal_unhappyPath_ageExcusalNotCalled() throws StraightThroughProcessingServiceException {

        final JurorResponseDto responseDto = mock(JurorResponseDto.class);
        final JurorResponse jurorResponse = mock(JurorResponse.class);

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
        verify(straightThroughProcessor, times(0)).processAcceptance(any(JurorResponse.class));
        verify(straightThroughProcessor, times(0))
            .processDeceasedExcusal(any(JurorResponse.class));
        verify(straightThroughProcessor, times(0)).processAgeExcusal(any(JurorResponse.class));
    }
}
