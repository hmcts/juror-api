package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.NilPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolMemberFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CoronerPoolItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestItemDto;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.FilterPoolMember;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.Voters;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.PoolCreateException;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolDetailRepository;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.VotersRepository;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsService;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class PoolCreateServiceTest {

    @Mock
    private VotersLocPostcodeTotalsService votersLocPostcodeTotalsService;
    @Mock
    private PoolCreateServiceImpl poolCreateServiceImpl;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private ManageDeferralsService manageDeferralsService;
    @Mock
    private VotersRepository votersRepository;
    @Mock
    private CourtLocationService courtLocationService;
    @Mock
    private VotersService votersServiceImpl;
    @Mock
    private PoolHistoryRepository poolHistoryRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private PoolMemberSequenceService poolMemberSequenceService;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private GeneratePoolNumberService generatePoolNumberService;
    @Mock
    private PoolTypeRepository poolTypeRepository;
    @Mock
    private CourtLocationRepository courtLocationRepository;
    @Mock
    private GenerateCoronerPoolNumberService generateCoronerPoolNumberService;
    @Mock
    private CoronerPoolDetailRepository coronerPoolDetailRepository;
    @Mock
    private CoronerPoolRepository coronerPoolRepository;

    private MockedStatic<PaginationUtil> mockStaticPaginationUtil;
    @InjectMocks
    PoolCreateServiceImpl poolCreateService;

    @AfterEach
    public void afterEach() {
        if (mockStaticPaginationUtil != null) {
            mockStaticPaginationUtil.close();
        }
    }

    @Test
    void test_getPoolRequest_recordFound() {
        String poolNumber = "415220110";
        String owner = "415";

        Mockito.when(poolRequestRepository.findById(Mockito.any()))
            .thenReturn(Optional.of(createValidPoolRequest("415220110")));
        Mockito.when(jurorPoolRepository.findByPoolPoolNumberAndWasDeferredAndIsActive(
            Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(new ArrayList<>());

        PoolRequestItemDto poolRequestItemDto = poolCreateService.getPoolRequest(poolNumber, owner);

        assertThat(poolRequestItemDto.getPoolNumber())
            .as("Pool request pool number is returned")
            .isEqualTo("415220110");
        assertThat(poolRequestItemDto.getNoRequested())
            .as("Pool request No requested is returned")
            .isEqualTo(150);
        assertThat(poolRequestItemDto.getCourtName())
            .as("Pool request court name is returned")
            .isEqualTo("Test Court Location Name");
        assertThat(poolRequestItemDto.getLocCode())
            .as("Pool request location code is returned")
            .isEqualTo("415");
        assertThat(poolRequestItemDto.getCourtSupplied())
            .as("Pool request court supplied is returned")
            .isEqualTo(0);
        assertThat(poolRequestItemDto.getAdditionalSummons())
            .as("Pool request additional summons is returned")
            .isEqualTo(0);
    }

    @Test
    void test_getPoolRequest_noMatch() {
        String poolNumber = "415220111";
        String owner = "415";

        Mockito.when(poolRequestRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        PoolRequestItemDto poolRequestItemDto = poolCreateService.getPoolRequest(poolNumber, owner);

        assertThat(poolRequestItemDto).as("Pool request is not found")
            .isEqualTo(null);
    }

    @Test
    void getCourtCatchmentItems_invalidLocationCode() {
        final String locationCode = "100";
        final boolean isCoronersPool = false;
        assertThatExceptionOfType(PoolCreateException.CourtLocationNotFound.class)
            .isThrownBy(() -> poolCreateService.getAvailableVotersByLocation(locationCode, isCoronersPool));

        Mockito.verify(courtLocationRepository, Mockito.times(1)).findByLocCode(locationCode);
    }


    @Test
    void checkVoterUnlocked() {
        final String owner = "400";
        int citizensToSummon = 100;
        int noRequested = 100;
        PoolRequest poolrequest = createValidPoolRequest("415220110");

        PoolCreateRequestDto poolCreateRequestDto = createValidPoolCreateRequestDto();
        poolCreateRequestDto.setNoRequested(noRequested);
        poolCreateRequestDto.setCitizensToSummon(citizensToSummon);

        Mockito.when(courtLocationService.getYieldForCourtLocation(Mockito.any()))
            .thenReturn(poolrequest.getCourtLocation().getYield());
        Mockito.when(courtLocationService.getVotersLock(Mockito.any())).thenReturn(false);

        assertThatExceptionOfType(PoolCreateException.UnableToObtainVotersLock.class)
            .isThrownBy(() -> poolCreateService.lockVotersAndCreatePool(buildPayload(owner), poolCreateRequestDto));
    }

    @Test
    void createPool() throws SQLException {
        String owner = "400";
        int citizensToSummon = 1;
        int noRequested = 1;

        final BureauJwtPayload payload = buildPayload(owner);

        PoolCreateRequestDto poolCreateRequestDto = createValidPoolCreateRequestDto();
        poolCreateRequestDto.setNoRequested(noRequested);
        poolCreateRequestDto.setCitizensToSummon(citizensToSummon);
        poolCreateRequestDto.setPreviousJurorCount(0);

        List<Voters> voters = List.of(createValidVoter());
        //GET POOL MEMBER
        Mockito.when(votersServiceImpl.getVoters(Mockito.any())).thenReturn(voters);
        Mockito.when(poolMemberSequenceService.getPoolMemberSequenceNumber(Mockito.any())).thenReturn(1);
        Mockito.when(poolMemberSequenceService.leftPadInteger(1)).thenReturn("01");
        Mockito.when(poolRequestRepository.findById(Mockito.any()))
            .thenReturn(Optional.of(createValidPoolRequest("415220110")));
        Mockito.when(jurorStatusRepository.findById(Mockito.any()))
            .thenReturn(Optional.of(createValidPoolStatus()));
        Mockito.doNothing().when(votersServiceImpl).markVotersAsSelected(Mockito.any(), Mockito.any());

        //CREATE POOL MEMBER
        JurorPool jurorPool = createValidJurorPool();
        List<Juror> jurorList = List.of(jurorPool.getJuror());
        Mockito.when(jurorRepository.saveAll(Mockito.any())).thenReturn(jurorList);
        Mockito.when(jurorPoolRepository.saveAll(Mockito.any())).thenReturn(List.of(jurorPool));
        Mockito.when(poolRequestRepository.save(Mockito.any()))
            .thenReturn(createValidPoolRequest("415220110"));

        //UPDATE POOL HISTORY
        Mockito.when(poolHistoryRepository.save(Mockito.any())).thenReturn(createPoolHistory());

        //UPDATE JUROR HISTORY
        Mockito.when(jurorHistoryRepository.saveAll(Mockito.any())).thenReturn(List.of(createValidJurorHist()));

        Mockito.when(jurorHistoryRepository.saveAll(Mockito.any()))
            .thenReturn((List.of(createValidJurorHist())));

        poolCreateService.createPool(payload, poolCreateRequestDto);

        Mockito.verify(votersServiceImpl, Mockito.times(1)).getVoters(Mockito.any());
        Mockito.verify(poolMemberSequenceService, Mockito.times(1))
            .getPoolMemberSequenceNumber(poolCreateRequestDto.getPoolNumber());
        Mockito.verify(poolRequestRepository, Mockito.times(1)).findById(Mockito.any());

        Mockito.verify(votersServiceImpl, Mockito.times(1))
            .markVotersAsSelected(Mockito.any(), Mockito.any());
        Mockito.verify(poolMemberSequenceService, Mockito.times(voters.size())).leftPadInteger(1);
        Mockito.verify(jurorStatusRepository, Mockito.times(voters.size())).findById(1);
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).saveAll(Mockito.any());
        Mockito.verify(poolRequestRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(poolHistoryRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(jurorHistoryRepository, Mockito.times(1)).saveAll(Mockito.any());
    }

    @Test
    void checkYield_throw_error() {
        final String owner = "400";
        int citizensToSummon = 199;
        int noRequested = 99;
        CourtLocation courtLocation = createValidPoolRequest("415220110").getCourtLocation();

        PoolCreateRequestDto poolCreateRequestDto = createValidPoolCreateRequestDto();
        poolCreateRequestDto.setNoRequested(noRequested);
        poolCreateRequestDto.setCitizensToSummon(citizensToSummon);

        Mockito.when(courtLocationService.getYieldForCourtLocation(Mockito.any())).thenReturn(courtLocation.getYield());

        //summoned too many citizens, raise an exception
        assertThatExceptionOfType(PoolCreateException.InvalidNoOfCitizensToSummonForYield.class)
            .isThrownBy(() -> poolCreateService.lockVotersAndCreatePool(buildPayload(owner), poolCreateRequestDto));
    }

    @Test
    void test_checkForDeferrals_withCourtLocNameOnly_happy() {
        String owner = "415";
        CourtLocation courtLocation = createValidPoolRequest("415220110").getCourtLocation();
        NilPoolRequestDto nilPoolRequestDto = createValidNilPoolRequestDto();
        nilPoolRequestDto.setLocationCode(null);

        Mockito.when(courtLocationService.getCourtLocationByName(Mockito.any())).thenReturn(courtLocation);

        poolCreateService.checkForDeferrals(owner, nilPoolRequestDto);

        Mockito.verify(courtLocationService, Mockito.times(1))
            .getCourtLocationByName(nilPoolRequestDto.getLocationName());
        Mockito.verify(manageDeferralsService, Mockito.times(1))
            .getDeferralsCount(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
    }

    @Test
    void test_checkForDeferrals_withCourtLocCodeOnly_happy() {
        String owner = "415";
        CourtLocation courtLocation = createValidPoolRequest("415220110").getCourtLocation();
        NilPoolRequestDto nilPoolRequestDto = createValidNilPoolRequestDto();
        nilPoolRequestDto.setLocationName(null);

        Mockito.when(courtLocationService.getCourtLocation(Mockito.any())).thenReturn(courtLocation);

        poolCreateService.checkForDeferrals(owner, nilPoolRequestDto);

        Mockito.verify(courtLocationService, Mockito.times(1))
            .getCourtLocation(nilPoolRequestDto.getLocationCode());
        Mockito.verify(manageDeferralsService, Mockito.times(1))
            .getDeferralsCount(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
    }

    @Test
    void test_createNilPool_happy() {

        String owner = "415";
        CourtLocation courtLocation = createValidPoolRequest("415220110").getCourtLocation();
        NilPoolRequestDto nilPoolRequestDto = createValidNilPoolRequestDto();

        Mockito.when(courtLocationService.getCourtLocation(Mockito.any())).thenReturn(courtLocation);
        Mockito.doReturn(Optional.of(new PoolType("CRO", "CROWN COURT")))
            .when(poolTypeRepository).findById(Mockito.any());

        poolCreateService.createNilPool(owner, nilPoolRequestDto);

        ArgumentCaptor<PoolRequest> nilPoolArgumentCaptor = ArgumentCaptor.forClass(PoolRequest.class);

        Mockito.verify(poolTypeRepository, Mockito.times(1))
            .findById(nilPoolRequestDto.getPoolType());
        Mockito.verify(courtLocationService, Mockito.times(2))
            .getCourtLocation(nilPoolRequestDto.getLocationCode());
        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .save(nilPoolArgumentCaptor.capture());
        PoolRequest nilPool = nilPoolArgumentCaptor.getValue();

        assertThat(nilPool.getPoolNumber())
            .as("Expect pool number to be mapped from request dto")
            .isEqualTo(nilPoolRequestDto.getPoolNumber());
        assertThat(nilPool.getPoolType().getPoolType())
            .as("Expect pool type to be mapped from request dto")
            .isEqualTo(nilPoolRequestDto.getPoolType());
        assertThat(nilPool.getNumberRequested())
            .as("Expect number requested to be 0 (Nil Pool)")
            .isEqualTo(0);
        assertThat(nilPool.getTotalNoRequired())
            .as("Expect total number requested to be 0 (Nil Pool)")
            .isEqualTo(0);
        assertThat(nilPool.getReturnDate())
            .as("Expect service start date to be mapped from request dto")
            .isEqualTo(nilPoolRequestDto.getAttendanceDate());
        assertThat(nilPool.getAttendTime())
            .as("Expect attendance time to be mapped from request dto")
            .isEqualToIgnoringSeconds(LocalDateTime.of(nilPoolRequestDto.getAttendanceDate(),
                nilPoolRequestDto.getAttendanceTime()));
        assertThat(nilPool.getNewRequest())
            .as("Expect new request flag to be set to 'N' - no jurors need to be summoned")
            .isEqualTo('N');
        assertThat(nilPool.getOwner())
            .as("Expect owner to be set to bureau's location code")
            .isEqualTo("400");
        assertThat(nilPool.isNilPool())
            .as("Expect nil pool flag to be set to true")
            .isTrue();
    }

    @Test
    void testConvertNilPoolHappy() {
        String bureauOwner = "400";
        NilPoolRequestDto nilPoolRequestDto = createValidNilPoolRequestDto();
        PoolRequest poolRequest = createValidPoolRequest("4152221201");
        poolRequest.setAttendTime(LocalDateTime.of(nilPoolRequestDto.getAttendanceDate(),
            nilPoolRequestDto.getAttendanceTime()));
        poolRequest.setReturnDate(nilPoolRequestDto.getAttendanceDate());
        poolRequest.setNewRequest('N');
        poolRequest.setOwner(bureauOwner);
        CourtLocation courtLocation = poolRequest.getCourtLocation();

        Mockito.when(courtLocationService.getCourtLocation(Mockito.any())).thenReturn(courtLocation);

        String courtOwner = "415";
        poolCreateService.createNilPool(courtOwner, nilPoolRequestDto);

        BureauJwtPayload payload = buildPayload(bureauOwner);
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();

        Mockito.when(poolRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(poolRequest));
        Mockito.doReturn(Optional.of(new PoolType("CRO", "CROWN COURT"))).when(poolTypeRepository)
            .findById(Mockito.any());

        poolCreateService.convertNilPool(poolRequestDto, payload);

        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .findById(poolRequestDto.getPoolNumber());
        Mockito.verify(poolTypeRepository, Mockito.times(2))
            .findById(poolRequestDto.getPoolType());
        Mockito.verify(poolHistoryRepository, Mockito.times(1))
            .save(Mockito.any());

        ArgumentCaptor<PoolRequest> nilPoolArgumentCaptor = ArgumentCaptor.forClass(PoolRequest.class);
        Mockito.verify(poolRequestRepository, Mockito.times(2))
            .saveAndFlush(nilPoolArgumentCaptor.capture());

        PoolRequest convertedNilPool =
            nilPoolArgumentCaptor.getAllValues().stream().filter(pool -> !pool.isNilPool()).findFirst()
                .orElse(null);

        assertThat(convertedNilPool).isNotNull();

        assertThat(convertedNilPool.getPoolNumber())
            .as("Expect pool number to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolNumber());
        assertThat(convertedNilPool.getPoolType().getPoolType())
            .as("Expect pool type to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolType());
        assertThat(convertedNilPool.getNumberRequested())
            .as("Expect number requested to be mapped from request dto")
            .isEqualTo(poolRequestDto.getNumberRequested());
        assertThat(convertedNilPool.getTotalNoRequired())
            .as("Expect total number requested to be mapped from request dto")
            .isEqualTo(poolRequestDto.getNumberRequested());
        assertThat(convertedNilPool.getReturnDate())
            .as("Expect service start date to be mapped from request dto")
            .isEqualTo(poolRequestDto.getAttendanceDate());
        assertThat(convertedNilPool.getAttendTime())
            .as("Expect attendance time to be mapped from request dto")
            .isEqualToIgnoringSeconds(LocalDateTime.of(poolRequestDto.getAttendanceDate(),
                poolRequestDto.getAttendanceTime()));
        assertThat(convertedNilPool.getNewRequest())
            .as("Expect new request flag to be set to 'N'")
            .isEqualTo('N');
        assertThat(convertedNilPool.getOwner())
            .as("Expect owner to be set to bureau's location code")
            .isEqualTo("400");
        assertThat(convertedNilPool.isNilPool())
            .as("Expect nil pool flag to be set to false")
            .isFalse();
    }

    @Test
    void test_createCoronerPool_happy() {
        String owner = "400";
        String locCode = "415";
        CoronerPoolRequestDto coronerPoolRequestDto = getCoronerPoolRequestDto(locCode);

        Mockito.when(generateCoronerPoolNumberService.generateCoronerPoolNumber())
            .thenReturn("923120001");
        Mockito.when(courtLocationService.getCourtLocation(Mockito.any())).thenReturn(new CourtLocation());

        poolCreateService.createCoronerPool(owner, coronerPoolRequestDto);

        Mockito.verify(generateCoronerPoolNumberService, Mockito.times(1))
            .generateCoronerPoolNumber();
        Mockito.verify(courtLocationService, Mockito.times(1))
            .getCourtLocation(Mockito.any());
        Mockito.verify(coronerPoolRepository, Mockito.times(1)).save(Mockito.any());

    }

    @Test
    void test_createCoronerPool_TooManyRequested() {
        String owner = "400";
        String locCode = "415";
        CoronerPoolRequestDto coronerPoolRequestDto = getCoronerPoolRequestDto(locCode);
        coronerPoolRequestDto.setNoRequested(251);  // UPPER_REQUEST_LIMIT = 250

        //Requested too many jurors, raise an exception
        assertThatExceptionOfType(PoolCreateException.InvalidNoOfJurorsRequested.class)
            .isThrownBy(() -> poolCreateService.createCoronerPool(owner, coronerPoolRequestDto));

    }

    @Test
    void test_createCoronerPool_TooFewRequested() {
        String owner = "400";
        String locCode = "415";
        CoronerPoolRequestDto coronerPoolRequestDto = getCoronerPoolRequestDto(locCode);
        coronerPoolRequestDto.setNoRequested(29);  // LOWER_REQUEST_LIMIT = 30

        //Requested too few jurors, raise an exception
        assertThatExceptionOfType(PoolCreateException.InvalidNoOfJurorsRequested.class)
            .isThrownBy(() -> poolCreateService.createCoronerPool(owner, coronerPoolRequestDto));

    }

    @Test
    void test_getCoronerPool_happy() {

        CoronerPool coronerPool = getCoronerPool();


        coronerPool.setPoolNumber("923120001");
        coronerPool.setEmail("coroner.name@test.com");
        coronerPool.setPhoneNumber("0121 1235432");

        Optional<CoronerPool> coronerPoolOpt = Optional.of(coronerPool);

        Mockito.when(coronerPoolRepository.findById(Mockito.any()))
            .thenReturn(coronerPoolOpt);
        Mockito.when(coronerPoolDetailRepository.findAllByPoolNumber(Mockito.any()))
            .thenReturn(new ArrayList<>());

        CoronerPoolItemDto coronerPoolItemDto = poolCreateService.getCoronerPool("923120001");

        Mockito.verify(coronerPoolRepository, Mockito.times(1))
            .findById(Mockito.any());
        Mockito.verify(coronerPoolDetailRepository, Mockito.times(1))
            .findAllByPoolNumber(Mockito.any());

        assertThat(coronerPoolItemDto).as("Coroner Pool Item DTO is not null")
            .isNotNull();

        verifyCoronerPoolItemDto(coronerPoolItemDto);

    }

    @Test
    void testGetThinJurorPoolListPassThrough() {
        final String poolNumber = "12345678";
        final String owner = "415";
        final List<String> jurorNumbers = Arrays.asList("123456789", "987654321");

        Mockito.when(jurorPoolRepository.fetchThinPoolMembers(poolNumber, owner))
            .thenReturn(jurorNumbers);

        final List<String> callResponse = poolCreateService.getThinJurorPoolsList(poolNumber, owner);

        Mockito.verify(jurorPoolRepository, Mockito.times(1)).fetchThinPoolMembers(poolNumber, owner);
        assertThat(callResponse).isEqualTo(jurorNumbers);
    }

    @Test
    void testGetPoolMembersListBureauUserMapsFieldsProperly() {
        String bureauOwner = "400";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "BUREAU_USER");

        PoolRequest poolRequest = createValidPoolRequest("415220110");
        poolRequest.setOwner(bureauOwner);
        String poolNumber = poolRequest.getPoolNumber();

        JPAQuery<Tuple> mockData = Mockito.mock();
        Mockito.when(jurorPoolRepository.fetchFilteredPoolMembers(Mockito.any(), Mockito.any()))
            .thenReturn(mockData);

        mockStaticPaginationUtil = Mockito.mockStatic(PaginationUtil.class);

        ArgumentCaptor<Function<Tuple, FilterPoolMember>> dataMapperCaptor = ArgumentCaptor.forClass(Function.class);

        poolCreateService.getJurorPoolsList(payload, createPoolFilterQuery(poolNumber));

        mockStaticPaginationUtil.verify(() -> {
            PaginationUtil.toPaginatedList(Mockito.eq(mockData), Mockito.eq(createPoolFilterQuery(poolNumber)),
                                           Mockito.eq(PoolMemberFilterRequestQuery.SortField.JUROR_NUMBER),
                                           Mockito.eq(SortMethod.ASC), dataMapperCaptor.capture(),
                                           Mockito.eq(500L));
        });

        Function<Tuple, FilterPoolMember> dataMapper = dataMapperCaptor.getValue();

        String jurorNumber = "123456789";
        FilterPoolMember data = dataMapper.apply(createQueryResult(jurorNumber));

        assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(data.getFirstName()).isEqualTo("Test");
        assertThat(data.getLastName()).isEqualTo("Person");
        assertThat(data.getStatus()).isEqualTo("Responded");
        assertThat(data.getPostcode()).isEqualTo("CH1 2AN");
        assertThat(data.getAttendance()).isNull();
        assertThat(data.getCheckedInToday()).isNull();
        assertThat(data.getCheckedIn()).isNull();
        assertThat(data.getNextDate()).isNull();
    }

    @Test
    void testGetPoolMembersListCourtUserMapsFieldsProperly() {
        String bureauOwner = "415";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "COURT_USER");

        PoolRequest poolRequest = createValidPoolRequest("415220110");
        poolRequest.setOwner(bureauOwner);
        String poolNumber = poolRequest.getPoolNumber();

        JPAQuery<Tuple> mockData = Mockito.mock();
        Mockito.when(jurorPoolRepository.fetchFilteredPoolMembers(Mockito.any(), Mockito.any()))
            .thenReturn(mockData);

        mockStaticPaginationUtil = Mockito.mockStatic(PaginationUtil.class);

        ArgumentCaptor<Function<Tuple, FilterPoolMember>> dataMapperCaptor = ArgumentCaptor.forClass(Function.class);

        poolCreateService.getJurorPoolsList(payload, createPoolFilterQuery(poolNumber));

        mockStaticPaginationUtil.verify(() -> {
            PaginationUtil.toPaginatedList(Mockito.eq(mockData), Mockito.eq(createPoolFilterQuery(poolNumber)),
                                           Mockito.eq(PoolMemberFilterRequestQuery.SortField.JUROR_NUMBER),
                                           Mockito.eq(SortMethod.ASC), dataMapperCaptor.capture(),
                                           Mockito.eq(500L));
        });

        Function<Tuple, FilterPoolMember> dataMapper = dataMapperCaptor.getValue();

        String jurorNumber = "123456789";
        FilterPoolMember data = dataMapper.apply(createQueryResult(jurorNumber));

        assertThat(data.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(data.getFirstName()).isEqualTo("Test");
        assertThat(data.getLastName()).isEqualTo("Person");
        assertThat(data.getStatus()).isEqualTo("Responded");
        assertThat(data.getPostcode()).isNull();
        assertThat(data.getAttendance()).isEqualTo("");
        assertThat(data.getCheckedInToday()).isEqualTo(false);
        assertThat(data.getCheckedIn().truncatedTo(ChronoUnit.SECONDS))
            .isEqualTo(LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
        assertThat(data.getNextDate()).isEqualTo(LocalDate.now());
    }

    private static NilPoolRequestDto createValidNilPoolRequestDto() {
        NilPoolRequestDto nilPoolRequestDto = new NilPoolRequestDto();
        nilPoolRequestDto.setLocationCode("415");
        nilPoolRequestDto.setLocationName("CHESTER");
        nilPoolRequestDto.setAttendanceDate(LocalDate.of(2022, 12, 3));
        nilPoolRequestDto.setAttendanceTime(LocalTime.of(9, 30));
        nilPoolRequestDto.setPoolType("CRO");
        nilPoolRequestDto.setPoolNumber("415221201");
        return nilPoolRequestDto;
    }

    private static PoolRequestDto createValidPoolRequestDto() {
        PoolRequestDto poolRequestDto = new PoolRequestDto();
        poolRequestDto.setPoolNumber("4152221201");
        poolRequestDto.setLocationCode("415");
        poolRequestDto.setAttendanceDate(LocalDate.of(2022, 12, 3));
        poolRequestDto.setNumberRequested(10);
        poolRequestDto.setPoolType("CRO");
        poolRequestDto.setAttendanceTime(LocalTime.of(9, 30));
        poolRequestDto.setDeferralsUsed(0);
        return poolRequestDto;
    }

    private static CoronerPoolRequestDto getCoronerPoolRequestDto(String locCode) {
        CoronerPoolRequestDto coronerPoolRequestDto = new CoronerPoolRequestDto();
        coronerPoolRequestDto.setLocationCode(locCode);
        coronerPoolRequestDto.setName("Fname Lastname");
        coronerPoolRequestDto.setNoRequested(100);
        coronerPoolRequestDto.setRequestDate(LocalDate.now());
        coronerPoolRequestDto.setEmailAddress("coroner.name@test.com");
        coronerPoolRequestDto.setPhone("0121 1231234");
        return coronerPoolRequestDto;
    }

    private static CoronerPool getCoronerPool() {
        CoronerPool coronerPool = new CoronerPool();
        coronerPool.setPoolNumber("923120001");
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setName("Court Name");
        coronerPool.setCourtLocation(courtLocation);
        coronerPool.setNumberRequested(100);
        coronerPool.setName("Fname Lname");
        coronerPool.setServiceDate(LocalDate.now().minusDays(10));
        coronerPool.setRequestDate(LocalDate.now().minusDays(10));
        return coronerPool;
    }

    private static void verifyCoronerPoolItemDto(CoronerPoolItemDto coronerPoolItemDto) {
        assertThat(coronerPoolItemDto.getPoolNumber())
            .as("Expect Coroner Pool number to be 923120001")
            .isEqualTo("923120001");
        assertThat(coronerPoolItemDto.getCourtName())
            .as("Expect Coroner Pool court name to be Court Name")
            .isEqualTo("Court Name");
        assertThat(coronerPoolItemDto.getLocCode())
            .as("Expect Coroner Pool court location code to be 415")
            .isEqualTo("415");
        assertThat(coronerPoolItemDto.getName())
            .as("Expect Coroner Pool name of request to be Fname Lname")
            .isEqualTo("Fname Lname");
        assertThat(coronerPoolItemDto.getNoRequested())
            .as("Expect Coroner Pool number requested to be 100")
            .isEqualTo(100);
        assertThat(coronerPoolItemDto.getEmailAddress())
            .as("Expect Coroner Pool requester email to be coroner.name@test.com")
            .isEqualTo("coroner.name@test.com");
        assertThat(coronerPoolItemDto.getPhone())
            .as("Expect Coroner Pool requester phone number to be 0121 1235432")
            .isEqualTo("0121 1235432");
    }

    private BureauJwtPayload buildPayload(String owner) {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login("SOME_USER")
            .owner(owner)
            .build();
    }

    private PoolCreateRequestDto createValidPoolCreateRequestDto() {
        PoolCreateRequestDto poolCreateRequestDto = new PoolCreateRequestDto();
        poolCreateRequestDto.setPoolNumber("415221201");
        poolCreateRequestDto.setStartDate(LocalDate.of(2022, 12, 4));
        poolCreateRequestDto.setAttendTime(LocalDateTime.of(2022, 12, 4, 9, 0, 0));
        poolCreateRequestDto.setNoRequested(5);
        poolCreateRequestDto.setBureauDeferrals(0);
        poolCreateRequestDto.setNumberRequired(4);
        poolCreateRequestDto.setCitizensToSummon(8);
        poolCreateRequestDto.setCatchmentArea("415");
        List<String> postcodes = new ArrayList<>();
        postcodes.add("CH1");
        postcodes.add("CH2");
        postcodes.add("CH3");
        poolCreateRequestDto.setPostcodes(postcodes);

        return poolCreateRequestDto;
    }

    private Voters createValidVoter() {
        Voters voter = new Voters();
        voter.setTitle(null);
        voter.setFirstName("FNAMEEIGHTTHREEONE");
        voter.setLastName("LNAMEEIGHTTHREEONE");
        voter.setAddress("831 STREET NAME");
        voter.setAddress2("ANYTOWN");
        voter.setAddress3(null);
        voter.setAddress4(null);
        voter.setAddress5(null);
        voter.setAddress6(null);
        voter.setPostcode("SY2 6LU");
        voter.setJurorNumber("641500541");
        voter.setDateOfBirth(LocalDate.of(1990, 6, 1));
        voter.setLocCode("415");
        voter.setRecNumber(91);
        voter.setRegisterLett("91");
        voter.setPollNumber("91");

        return voter;
    }

    private JurorPool createValidJurorPool() {
        Juror juror = new Juror();
        juror.setJurorNumber("209092530");
        juror.setAddressLine1("16 STREET NAME");
        juror.setAddressLine2("ANYTOWN");
        juror.setAddressLine3(null);
        juror.setAddressLine4(null);
        juror.setAddressLine5(null);
        juror.setAddressLine5(null);

        PoolRequest poolRequest = createValidPoolRequest("415220110");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setPool(poolRequest);
        jurorPool.setOwner(poolRequest.getOwner());
        jurorPool.setPoolSequence("01");
        jurorPool.setStatus(jurorStatus);
        jurorPool.setUserEdtq("BUREAU_USER_1");

        jurorPool.setJuror(juror);
        return jurorPool;
    }

    private PoolHistory createPoolHistory() {
        return new PoolHistory("400", LocalDateTime.now(), HistoryCode.PHSI,
            "somelogin", "150 (New Pool Request)");
    }

    private JurorHistory createValidJurorHist() {
        JurorHistory jurorHistory = new JurorHistory();
        jurorHistory.setPoolNumber("415220110");
        jurorHistory.setCreatedBy("somelogin");
        jurorHistory.setHistoryCode(HistoryCodeMod.NUMBER_OF_SUMMONS_ISSUED);
        jurorHistory.setDateCreated(LocalDateTime.now());
        jurorHistory.setJurorNumber("209092530");
        jurorHistory.setOtherInformation("Some Excuse");
        return jurorHistory;
    }


    private PoolRequest createValidPoolRequest(String poolNumber) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setOwner("415");
        poolRequest.setAttendTime(LocalDateTime.of(2022, 10, 3, 11, 30));
        poolRequest.setReturnDate(LocalDate.of(2022, 10, 3));
        poolRequest.setNumberRequested(150);
        poolRequest.setPoolType(new PoolType("CRO", "Crown Court"));
        poolRequest.setAdditionalSummons(0);
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("Test Court Location Name");
        courtLocation.setYield(BigDecimal.valueOf(2));
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setLastUpdate(LocalDateTime.now());
        poolRequest.setJurorPools(List.of());
        return poolRequest;
    }

    private JurorStatus createValidPoolStatus() {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(1);
        jurorStatus.setStatusDesc("Summoned");
        jurorStatus.setActive(true);
        return jurorStatus;
    }


    private PoolMemberFilterRequestQuery createPoolFilterQuery(String poolNumber) {
        return PoolMemberFilterRequestQuery.builder()
            .poolNumber(poolNumber)
            .pageLimit(1)
            .pageNumber(1)
            .build();
    }

    private Tuple createQueryResult(String jurorNumber) {
        Tuple data = Mockito.mock(Tuple.class);
        Mockito.when(data.get(QJurorPool.jurorPool.juror.jurorNumber)).thenReturn(jurorNumber);
        Mockito.when(data.get(QJurorPool.jurorPool.juror.firstName)).thenReturn("Test");
        Mockito.when(data.get(QJurorPool.jurorPool.juror.lastName)).thenReturn("Person");
        Mockito.when(data.get(QJurorStatus.jurorStatus.statusDesc)).thenReturn("Responded");

        // Lenient because sometimes we don't use these mock fields and the test runner complains
        Mockito.lenient().when(data.get(QJurorPool.jurorPool.juror.postcode)).thenReturn("CH1 2AN");
        Mockito.lenient().when(data.get(JurorPoolRepository.ATTENDANCE)).thenReturn("");
        Mockito.lenient().when(data.get(JurorPoolRepository.CHECKED_IN_TODAY)).thenReturn(false);
        Mockito.lenient().when(data.get(QAppearance.appearance.timeIn)).thenReturn(LocalTime.now());
        Mockito.lenient().when(data.get(QJurorPool.jurorPool.nextDate)).thenReturn(LocalDate.now());

        return data;
    }
}
