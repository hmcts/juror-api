package uk.gov.hmcts.juror.api.moj.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.exception.JurorPaperResponseException;
import uk.gov.hmcts.juror.api.moj.exception.JurorRecordException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAuditChangeService;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(SpringRunner.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
public class SummonsReplyStatusUpdateServiceImplTest {

    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;

    @Mock
    private JurorResponseAuditRepositoryMod auditRepository;
    @Mock
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;
    @Mock
    private AssignOnUpdateServiceMod assignOnUpdateService;
    @Mock
    private JurorRecordService jurorRecordService;
    @Mock
    private JurorAuditChangeService jurorAuditChangeService;

    @InjectMocks
    private SummonsReplyStatusUpdateServiceImpl summonsReplyStatusUpdateService;

    //Interface method: updateJurorResponseStatus
    @Test
    public void test_updateJurorResponseStatus_noResponseFound() {
        BureauJwtPayload payload = buildPayload();
        Mockito.doReturn(null).when(jurorPaperResponseRepository).findByJurorNumber(any());
        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> summonsReplyStatusUpdateService.updateJurorResponseStatus("123456789",
                ProcessingStatus.CLOSED, payload));

        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorReasonableAdjustmentRepository, Mockito.never()).findByJurorNumber(any());
        verify(jurorStatusRepository, Mockito.never()).findById(any());
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        verify(jurorAuditChangeService, Mockito.never()).recordPersonalDetailsHistory(anyString(),
            any(Juror.class), any(String.class), anyString());
    }

    @Test
    public void test_updateJurorResponseStatus_processingAlreadyComplete() {
        String jurorNumber = "123456789";
        PaperResponse response = new PaperResponse();
        response.setJurorNumber(jurorNumber);
        response.setProcessingComplete(true);

        final BureauJwtPayload payload = buildPayload();

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(JurorPaperResponseException.JurorPaperResponseAlreadyExists.class)
            .isThrownBy(() -> summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, payload));

        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorReasonableAdjustmentRepository, Mockito.never()).findByJurorNumber(any());
        verify(jurorStatusRepository, Mockito.never()).findById(any());
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_notClosingResponse() {
        String jurorNumber = "123456789";
        PaperResponse response = new PaperResponse();
        response.setJurorNumber(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.TODO);

        final BureauJwtPayload payload = buildPayload();

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        int invocationCounter = 1;
        List<ProcessingStatus> processStatusList = Arrays.stream(ProcessingStatus.values())
            .filter(status -> status != ProcessingStatus.CLOSED).toList();

        for (ProcessingStatus status : processStatusList) {
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                status, payload);

            verify(jurorPaperResponseRepository, times(invocationCounter)).save(any());
            invocationCounter++;

            verify(jurorReasonableAdjustmentRepository, Mockito.never()).findByJurorNumber(any());
            verify(jurorStatusRepository, Mockito.never()).findById(any());
            verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
            verify(jurorPoolRepository, Mockito.never()).save(any());
            verify(jurorAuditChangeService, Mockito.never())
                .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        }
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_happyPath() {
        final int respondedStatusCode = 2;
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.CLOSED);
        response.setPhoneNumber("07123456789");
        response.setAltPhoneNumber("01234567890");

        final BureauJwtPayload payload = buildPayload();

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Optional.of(createPoolStatus(respondedStatusCode))).when(jurorStatusRepository)
            .findById(respondedStatusCode);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
            ProcessingStatus.CLOSED, payload);

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorRepository, times(2)).save(any());
        verify(jurorPoolRepository, times(2)).save(any());
        verify(jurorPaperResponseRepository, times(1)).save(any());
        verify(jurorAuditChangeService, times(1))
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        verify(jurorStatusRepository, times(1)).findById(respondedStatusCode);
    }


    @Test
    public void test_updateJurorResponseStatus_closingResponse_noJurorAudits() throws ParseException {
        final int respondedStatusCode = 2;
        String jurorNumber = "123456789";

        JurorPool jurorPool = createJuror(jurorNumber);
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(LocalDate.of(1990, 1, 25));

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        PaperResponse response = createPaperResponseFromJurorPool(juror);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.CLOSED);
        response.setPhoneNumber("01234567890");
        response.setAltPhoneNumber("07123456789");

        final BureauJwtPayload payload = buildPayload();

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Optional.of(createPoolStatus(respondedStatusCode))).when(jurorStatusRepository)
            .findById(respondedStatusCode);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
            ProcessingStatus.CLOSED, payload);

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorRepository, times(2)).save(any());
        verify(jurorPoolRepository, times(2)).save(any());
        verify(jurorPaperResponseRepository, times(1)).save(any());
        verify(jurorAuditChangeService, times(1))
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_alreadyProcessed() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(true);
        response.setProcessingStatus(ProcessingStatus.CLOSED);

        final BureauJwtPayload payload = buildPayload();

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(JurorPaperResponseException.JurorPaperResponseAlreadyExists.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, payload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_noJurorRecord() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.CLOSED);

        BureauJwtPayload payload = buildPayload();

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, payload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_multipleJurorRecords() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.CLOSED);

        final BureauJwtPayload payload = buildPayload();

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createJuror(jurorNumber));
        jurorPools.add(createJuror(jurorNumber));

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(JurorRecordException.MultipleJurorRecordsFound.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, payload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_bureauUser_bureauOwnedJurorPool() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.TODO);

        final BureauJwtPayload payload = buildPayload();

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createJuror(jurorNumber));

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
            response.getProcessingStatus(), payload);

        verify(jurorPaperResponseRepository, times(1)).save(any());

        verify(jurorReasonableAdjustmentRepository, Mockito.never()).findByJurorNumber(any());
        verify(jurorStatusRepository, Mockito.never()).findById(any());
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_bureauUser_courtOwnedJurorPool() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.TODO);

        final BureauJwtPayload payload = buildPayload();

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("415");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, payload));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_firstName() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setFirstName("");

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_courtUser_courtOwnedJurorPool() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.TODO);

        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("415");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
            response.getProcessingStatus(), payload);

        verify(jurorPaperResponseRepository, times(1)).save(any());

        verify(jurorReasonableAdjustmentRepository, Mockito.never()).findByJurorNumber(any());
        verify(jurorStatusRepository, Mockito.never()).findById(any());
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorHistoryRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));

    }

    @Test
    public void test_updateJurorResponseStatus_courtUser_bureauOwnedJurorPool() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(false);
        response.setProcessingStatus(ProcessingStatus.TODO);

        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createJuror(jurorNumber));

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, payload));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_lastName() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setLastName("");

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_dob() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setDateOfBirth(null);

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_address() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setAddressLine1("");

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_address4() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setAddressLine4("");

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_postcode() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setPostcode("");

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_bail() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setBail(null);

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_convictions() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setConvictions(null);

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_mentalHealthAct() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setMentalHealthAct(null);

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_mentalHealthCapacity() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setMentalHealthCapacity(null);

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_residency() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setResidency(null);
        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");


        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
    }

    @Test
    public void test_updateJurorResponseStatus_closingResponse_missingMandatory_signature() {
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponseWithOnlyMandatoryFields(jurorNumber);
        response.setSigned(null);

        BureauJwtPayload courtPayload = buildPayload();
        courtPayload.setOwner("411");

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool juror = createJuror(jurorNumber);
        juror.setOwner("411");
        jurorPools.add(juror);

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(
            JurorPaperResponseException.JurorPaperResponseMissingMandatoryFields.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.CLOSED, courtPayload));

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorPoolRepository, Mockito.never()).save(any());
        verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        verify(jurorAuditChangeService, Mockito.never())
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));

    }

    //Interface methods: mergePaperResponse & mergeDigitalResponse
    @Test
    public void test_paper_mergeJurorResponse_processingCompleteStatusIsTrue() {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.TRUE);

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorPoolRepository, Mockito.never())
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorPoolRepository, Mockito.never()).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, Mockito.never()).save(any(PaperResponse.class));
    }

    @Test
    public void test_digital_mergeJurorResponse_processingCompleteStatusIsTrue() {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.TRUE);

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorPoolRepository, Mockito.never())
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorPoolRepository, Mockito.never()).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, Mockito.never()).save(any(DigitalResponse.class));
    }

    @Test
    public void test_paper_mergeJurorResponse_findActiveJurorPool_noRecordsFound() {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);

        Mockito.doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorPoolRepository, Mockito.never()).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, Mockito.never()).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
    }

    @Test
    public void test_digital_mergeJurorResponse_findActiveJurorPool_noRecordsFound() {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);

        Mockito.doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorPoolRepository, Mockito.never()).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, Mockito.never()).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
    }

    @Test
    public void test_paper_mergeJurorResponse_findActiveJurorPool_multipleRecordsFound() {
        final String auditorUsername = "test_user";
        final String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);

        List<JurorPool> multipleJurorPoolRecords = new ArrayList<>();
        multipleJurorPoolRecords.add(createJuror(jurorNumber));
        multipleJurorPoolRecords.add(createJuror(jurorNumber));

        Mockito.doReturn(multipleJurorPoolRecords).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(JurorRecordException.MultipleJurorRecordsFound.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorPoolRepository, Mockito.never()).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, Mockito.never()).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
    }

    @Test
    public void test_digital_mergeJurorResponse_findActiveJurorPool_multipleRecordsFound() {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);

        List<JurorPool> multipleJurorPoolRecords = new ArrayList<>();
        multipleJurorPoolRecords.add(createJuror(jurorNumber));
        multipleJurorPoolRecords.add(createJuror(jurorNumber));

        Mockito.doReturn(multipleJurorPoolRecords).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(JurorRecordException.MultipleJurorRecordsFound.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorPoolRepository, Mockito.never()).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, Mockito.never()).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(any());
    }

    @Test
    public void test_paper_mergeJurorResponse_mergeReasonableAdjustments_multipleReasonableAdjustments() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(createmultipleReasonableAdjustmentsPaper(jurorNumber))
            .when(jurorReasonableAdjustmentRepository)
            .findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        Juror juror = jurorCaptor.getValue();
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        assertThat(juror.getReasonableAdjustmentCode()).isEqualTo("M");

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_mergeReasonableAdjustments_multipleReasonableAdjustments() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(createMultipleReasonableAdjustmentsDigital(jurorNumber))
            .when(jurorReasonableAdjustmentRepository)
            .findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getReasonableAdjustmentCode()).isEqualTo("M");

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_mergeReasonableAdjustments_singleReasonableAdjustment() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getReasonableAdjustmentCode()).isEqualTo("V");

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_mergeReasonableAdjustments_singleReasonableAdjustment() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getReasonableAdjustmentCode()).isEqualTo("V");

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_jurorAudit_noDataChange() {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(any(), any(),
            any(), any());
    }

    @Test
    public void test_digital_mergeJurorResponse_jurorAudit_noDataChange() {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(any(), any(),
            any(), any());
    }

    @Test
    public void test_paper_mergeJurorResponse_jurorAudit_onlyDateOfBirthUpdated() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setDateOfBirth(LocalDate.of(1901, 2, 1));
        Map<String, Boolean> changedProperties = initChangedPropertiesMap(Boolean.FALSE);
        changedProperties.put("date Of Birth", Boolean.TRUE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(changedProperties).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        assertThat(jurorCaptor.getValue().getReasonableAdjustmentCode()).isEqualTo("V");

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(any(), any(),
            any(), any());
    }

    @Test
    public void test_digital_mergeJurorResponse_jurorAudit_onlyDateOfBirthUpdated() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setDateOfBirth(LocalDate.of(1901, 1, 1));
        Map<String, Boolean> changedProperties = initChangedPropertiesMap(Boolean.FALSE);
        changedProperties.put("date Of Birth", Boolean.TRUE);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(changedProperties).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        assertThat(jurorCaptor.getValue().getReasonableAdjustmentCode()).isEqualTo("V");

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(any(), any(),
            any(), any());
    }

    @Test
    public void test_paper_mergeJurorResponse_updateJurorPoolFromSummonsReply_phoneNumberRules_mainNumberIsMobile() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setPhoneNumber("07918 010101");
        paperResponse.setAltPhoneNumber("07917 020202");


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getAltPhoneNumber()).isEqualTo("07918 010101");
        assertThat(juror.getWorkPhone()).isEqualTo("07917 020202");
        assertThat(juror.getPhoneNumber()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_updateJurorPoolFromSummonsReply_phoneNumberRules_mainNumberIsMobile() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setPhoneNumber("07918 010101");
        digitalResponse.setAltPhoneNumber("07917 020202");


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getAltPhoneNumber()).isEqualTo("07918 010101");
        assertThat(juror.getWorkPhone()).isEqualTo("07917 020202");
        assertThat(juror.getPhoneNumber()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_updateJurorPoolFromSummonsReply_phoneNumberRules_mainNumberIsNotMobile() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setPhoneNumber("01908 010101");
        paperResponse.setAltPhoneNumber("07917 020202");


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getPhoneNumber()).isEqualTo("01908 010101");
        assertThat(juror.getAltPhoneNumber()).isEqualTo("07917 020202");
        assertThat(juror.getWorkPhone()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
    }

    @Test
    public void test_digital_mergeResponse_updateJurorPoolFromSummonsReply_phoneNumberRules_mainNumberIsNotMobile() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setPhoneNumber("01908 010101");
        digitalResponse.setAltPhoneNumber("07917 020202");


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getPhoneNumber()).isEqualTo("01908 010101");
        assertThat(juror.getAltPhoneNumber()).isEqualTo("07917 020202");
        assertThat(juror.getWorkPhone()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_updateJurorPoolFromSummonsReply_phoneNumberRules_noMobileNumbers() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setPhoneNumber("01908 010101");
        paperResponse.setAltPhoneNumber("01274 020202");


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getPhoneNumber()).isEqualTo("01908 010101");
        assertThat(juror.getWorkPhone()).isEqualTo("01274 020202");
        assertThat(juror.getAltPhoneNumber()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_updateJurorPoolFromSummonsReply_phoneNumberRules_noMobileNumbers() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setPhoneNumber("01908 010101");
        digitalResponse.setAltPhoneNumber("01274 020202");

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getPhoneNumber()).isEqualTo("01908 010101");
        assertThat(juror.getWorkPhone()).isEqualTo("01274 020202");
        assertThat(juror.getAltPhoneNumber()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_updateJurorPoolFromSummonsReply_thirdParty_phoneNumberRules() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setThirdPartyReason("Testing");
        paperResponse.setPhoneNumber("01908 010101");
        paperResponse.setAltPhoneNumber("01274 020202");


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getPhoneNumber()).isNull();
        assertThat(juror.getWorkPhone()).isNull();
        assertThat(juror.getAltPhoneNumber()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_updateJurorPoolFromSummonsReply_thirdParty_phoneNumberRules() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setThirdPartyReason("Testing");
        digitalResponse.setPhoneNumber("01908 010101");
        digitalResponse.setAltPhoneNumber("01274 020202");


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getPhoneNumber()).isNull();
        assertThat(juror.getWorkPhone()).isNull();
        assertThat(juror.getAltPhoneNumber()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_updateJurorPoolFromSummonsReply() throws ParseException {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final LocalDate birthDate = LocalDate.of(1990, 1, 25);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getAddressLine2()).isEqualTo("New Address Line 2");
        assertThat(juror.getAddressLine1()).isEqualTo("New Address Line 1");
        assertThat(juror.getAddressLine3()).isEqualTo("New Address Line 3");
        assertThat(juror.getAddressLine4()).isEqualTo("New Town");
        assertThat(juror.getAddressLine5()).isEqualTo("New County");
        assertThat(juror.getPostcode()).isEqualTo("WA16 0PB");
        assertThat(juror.getDateOfBirth()).isEqualTo(birthDate);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_updateJurorPoolFromSummonsReply() throws ParseException {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final LocalDate birthDate = LocalDate.of(1990, 1, 25);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getAddressLine2()).isEqualTo("New Address Line 2");
        assertThat(juror.getAddressLine1()).isEqualTo("New Address Line 1");
        assertThat(juror.getAddressLine3()).isEqualTo("New Address Line 3");
        assertThat(juror.getAddressLine4()).isEqualTo("New Town");
        assertThat(juror.getAddressLine5()).isEqualTo("New County");
        assertThat(juror.getPostcode()).isEqualTo("WA16 0PB");
        assertThat(juror.getDateOfBirth()).isEqualTo(birthDate);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_updateJurorPoolFromSummonsReply_isWelsh() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setWelsh(Boolean.TRUE);

        CourtLocation courtLocation = createCourtLocation("410", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        final WelshCourtLocation welshCourt = new WelshCourtLocation();
        welshCourt.setLocCode("457");

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(welshCourt).when(welshCourtLocationRepository).findByLocCode(anyString());
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getWelsh()).isEqualTo(Boolean.TRUE);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, times(1)).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_updateJurorPoolFromSummonsReply_isWelsh() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setWelsh(Boolean.TRUE);

        CourtLocation courtLocation = createCourtLocation("410", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        final WelshCourtLocation welshCourt = new WelshCourtLocation();
        welshCourt.setLocCode("457");

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(welshCourt).when(welshCourtLocationRepository).findByLocCode(anyString());
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getWelsh()).isEqualTo(Boolean.TRUE);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, times(1)).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_updateJurorPoolFromSummonsReply_isNotWelsh() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setWelsh(Boolean.FALSE);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getWelsh()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_updateJurorPoolFromSummonsReply_isNotWelsh() {
        final ArgumentCaptor<JurorPool> jurorPoolCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<Juror> jurorCaptor = ArgumentCaptor.forClass(Juror.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setWelsh(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorRepository, times(1)).save(jurorCaptor.capture());
        verify(jurorPoolRepository, times(1)).save(jurorPoolCaptor.capture());

        Juror juror = jurorCaptor.getValue();
        assertThat(juror.getWelsh()).isNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(juror,
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_markSummonReplyAsCompleted() {
        final ArgumentCaptor<PaperResponse> jurorPaperResponseCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setWelsh(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorPaperResponseRepository, times(1)).save(jurorPaperResponseCaptor.capture());
        assertThat(jurorPaperResponseCaptor.getValue().getProcessingComplete()).isEqualTo(Boolean.TRUE);
        assertThat(jurorPaperResponseCaptor.getValue().getCompletedAt()).isNotNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(jurorPool.getJuror(),
            paperResponse.getTitle(), paperResponse.getFirstName(), paperResponse.getLastName());
    }

    @Test
    public void test_digital_mergeJurorResponse_markSummonReplyAsCompleted() {
        final ArgumentCaptor<DigitalResponse> jurorDigitalResponseCaptor =
            ArgumentCaptor.forClass(DigitalResponse.class);
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setWelsh(Boolean.FALSE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(initChangedPropertiesMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorDigitalResponseRepository, times(1)).save(jurorDigitalResponseCaptor.capture());
        assertThat(jurorDigitalResponseCaptor.getValue().getProcessingComplete()).isEqualTo(Boolean.TRUE);
        assertThat(jurorDigitalResponseCaptor.getValue().getCompletedAt()).isNotNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, times(1)).setPendingNameChange(jurorPool.getJuror(),
            digitalResponse.getTitle(), digitalResponse.getFirstName(), digitalResponse.getLastName());
    }

    @Test
    public void test_paper_mergeJurorResponse_markSummonReplyAsCompleted_onlyTitleChanged() {
        final ArgumentCaptor<PaperResponse> jurorPaperResponseCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);

        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setWelsh(Boolean.FALSE);
        Map<String, Boolean> changedProperties = initChangedPropertiesMap(Boolean.FALSE);
        changedProperties.put("title", Boolean.TRUE);


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));
        Mockito.doReturn(changedProperties).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));
        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorPaperResponseRepository, times(1)).save(jurorPaperResponseCaptor.capture());
        assertThat(jurorPaperResponseCaptor.getValue().getProcessingComplete()).isEqualTo(Boolean.TRUE);
        assertThat(jurorPaperResponseCaptor.getValue().getCompletedAt()).isNotNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(any(), any(),
            any(), any());
    }

    @Test
    public void test_digital_mergeJurorResponse_markSummonReplyAsCompleted_onlyTitleChanged() {
        final ArgumentCaptor<DigitalResponse> jurorDigitalResponseCaptor =
            ArgumentCaptor.forClass(DigitalResponse.class);

        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setWelsh(Boolean.FALSE);
        Map<String, Boolean> changedProperties = initChangedPropertiesMap(Boolean.FALSE);
        changedProperties.put("title", Boolean.TRUE);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(null).when(jurorReasonableAdjustmentRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(changedProperties).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(AbstractJurorResponse.class));

        Mockito.doReturn(Boolean.FALSE).when(jurorAuditChangeService).hasNameChanged(anyString(), anyString(),
            anyString(), anyString());
        Mockito.doReturn(Boolean.TRUE).when(jurorAuditChangeService).hasTitleChanged(anyString(), anyString());

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorDigitalResponseRepository, times(1)).save(jurorDigitalResponseCaptor.capture());
        assertThat(jurorDigitalResponseCaptor.getValue().getProcessingComplete()).isEqualTo(Boolean.TRUE);
        assertThat(jurorDigitalResponseCaptor.getValue().getCompletedAt()).isNotNull();

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
    }

    @Test
    public void test_paper_mergeJurorResponse_auditChangeHistory_DateOfBirthChanged() throws ParseException {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        PaperResponse paperResponse = createPaperResponse(jurorNumber);
        paperResponse.setProcessingComplete(Boolean.FALSE);
        paperResponse.setWelsh(Boolean.FALSE);
        paperResponse.setTitle(null);
        paperResponse.setFirstName("Test");
        paperResponse.setLastName("Person");
        paperResponse.setAddressLine1("Address Line 1");
        paperResponse.setAddressLine2("Address Line 2");
        paperResponse.setAddressLine3("");
        paperResponse.setAddressLine4("Address Town");
        paperResponse.setAddressLine5("Address County");
        paperResponse.setPostcode("CH1 2AN");
        paperResponse.setDateOfBirth(LocalDate.of(1993, 1, 1));


        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsPaper(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any(PaperResponse.class));

        summonsReplyStatusUpdateService.mergePaperResponse(paperResponse, auditorUsername);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPaperResponseRepository, times(1)).save(any(PaperResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
    }

    @Test
    public void test_digital_mergeJurorResponse_auditChangeHistory_DateOfBirthChanged() throws ParseException {
        final String auditorUsername = "test_user";
        String jurorNumber = "123456789";

        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);
        digitalResponse.setProcessingComplete(Boolean.FALSE);
        digitalResponse.setWelsh(Boolean.FALSE);
        digitalResponse.setTitle(null);
        digitalResponse.setFirstName("Test");
        digitalResponse.setLastName("Person");
        digitalResponse.setAddressLine1("Address Line 1");
        digitalResponse.setAddressLine2("Address Line 2");
        digitalResponse.setAddressLine3("");
        digitalResponse.setAddressLine4("Address Town");
        digitalResponse.setAddressLine5("Address County");
        digitalResponse.setPostcode("CH1 2AN");
        digitalResponse.setDateOfBirth(LocalDate.of(1993, 1, 1));

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Map<String, Boolean> changedProperties = new HashMap<>();
        changedProperties.put("date Of Birth", Boolean.TRUE);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(createReasonableAdjustmentsDigital(jurorNumber)))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any(DigitalResponse.class));
        Mockito.doReturn(changedProperties).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class),
                any(AbstractJurorResponse.class));

        summonsReplyStatusUpdateService.mergeDigitalResponse(digitalResponse, auditorUsername);

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorRepository, times(1)).save(any(Juror.class));
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorDigitalResponseRepository, times(1)).save(any(DigitalResponse.class));
        verify(welshCourtLocationRepository, Mockito.never()).findByLocCode(anyString());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(anyString());
        verify(jurorRecordService, Mockito.never()).setPendingNameChange(any(), any(),
            any(), any());
    }

    @Test
    public void test_updateJurorResponseStatus_processingAlreadyComplete_NonRespondedJuror() {

        final int respondedStatusCode = 2;
        String jurorNumber = "123456789";
        PaperResponse response = createPaperResponse(jurorNumber);
        response.setProcessingComplete(true);
        response.setProcessingStatus(ProcessingStatus.CLOSED);
        response.setPhoneNumber("07123456789");
        response.setAltPhoneNumber("01234567890");

        JurorStatus respondedStatus = new JurorStatus();
        respondedStatus.setStatus(respondedStatusCode);

        final BureauJwtPayload payload = buildPayload();

        Mockito.doReturn(response).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Mockito.doReturn(Optional.of(respondedStatus)).when(jurorStatusRepository).findById(IJurorStatus.RESPONDED);

        CourtLocation courtLocation = createCourtLocation("415", "CHESTER", "09:15");
        JurorPool jurorPool = createJuror(jurorNumber);
        jurorPool.setStatus(createPoolStatus(IJurorStatus.EXCUSED));
        PoolRequest poolRequest = jurorPool.getPool();
        poolRequest.setCourtLocation(courtLocation);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
            ProcessingStatus.CLOSED, payload);

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumber);
        verify(jurorRepository, times(1)).save(any());
        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorStatusRepository, times(1)).findById(respondedStatusCode);
        verify(jurorHistoryRepository, times(1)).save(any());

        verify(jurorPaperResponseRepository, times(0)).save(any());
        verifyNoInteractions(jurorAuditChangeService);
    }


    private CourtLocation createCourtLocation(String locationCode, String name, String attendanceTime) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locationCode);
        courtLocation.setName(name);
        courtLocation.setCourtAttendTime(LocalTime.parse(attendanceTime));

        return courtLocation;
    }

    private JurorPool createJuror(String jurorNumber) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("Test");
        juror.setLastName("Person");
        juror.setAddressLine1("Address Line 1");
        juror.setAddressLine2("Address Line 2");
        juror.setAddressLine4("Address Town");
        juror.setAddressLine5("Address County");
        juror.setPostcode("CH1 2AN");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        return jurorPool;
    }

    private JurorStatus createPoolStatus(int statusCode) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(statusCode);
        return jurorStatus;
    }

    private PaperResponse createPaperResponse(String jurorNumber) {
        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setJurorNumber(jurorNumber);
        jurorPaperResponse.setTitle("Mr");
        jurorPaperResponse.setFirstName("Example");
        jurorPaperResponse.setLastName("Juror");
        jurorPaperResponse.setAddressLine1("New Address Line 1");
        jurorPaperResponse.setAddressLine2("New Address Line 2");
        jurorPaperResponse.setAddressLine3("New Address Line 3");
        jurorPaperResponse.setAddressLine4("New Town");
        jurorPaperResponse.setAddressLine5("New County");
        jurorPaperResponse.setPostcode("WA16 0PB");
        jurorPaperResponse.setDateOfBirth(LocalDate.of(1990, 1, 25));
        jurorPaperResponse.setBail(Boolean.FALSE);
        jurorPaperResponse.setConvictions(Boolean.FALSE);
        jurorPaperResponse.setMentalHealthAct(Boolean.FALSE);
        jurorPaperResponse.setMentalHealthCapacity(Boolean.FALSE);
        jurorPaperResponse.setResidency(Boolean.TRUE);
        jurorPaperResponse.setSigned(Boolean.TRUE);
        return jurorPaperResponse;
    }

    private DigitalResponse createDigitalResponse(String jurorNumber) {
        DigitalResponse jurorDigitalResponse = new DigitalResponse();
        jurorDigitalResponse.setJurorNumber(jurorNumber);
        jurorDigitalResponse.setTitle("Mr");
        jurorDigitalResponse.setFirstName("Example");
        jurorDigitalResponse.setLastName("Juror");
        jurorDigitalResponse.setAddressLine1("New Address Line 1");
        jurorDigitalResponse.setAddressLine2("New Address Line 2");
        jurorDigitalResponse.setAddressLine3("New Address Line 3");
        jurorDigitalResponse.setAddressLine4("New Town");
        jurorDigitalResponse.setAddressLine5("New County");
        jurorDigitalResponse.setPostcode("WA16 0PB");
        jurorDigitalResponse.setDateOfBirth(LocalDate.of(1990, 1, 25));
        jurorDigitalResponse.setBail(Boolean.FALSE);
        jurorDigitalResponse.setConvictions(Boolean.FALSE);
        jurorDigitalResponse.setMentalHealthAct(Boolean.FALSE);
        jurorDigitalResponse.setResidency(Boolean.TRUE);
        return jurorDigitalResponse;
    }

    private PaperResponse createPaperResponseWithOnlyMandatoryFields(String jurorNumber) {
        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setJurorNumber(jurorNumber);
        jurorPaperResponse.setFirstName("Example");
        jurorPaperResponse.setLastName("Juror");
        jurorPaperResponse.setAddressLine1("New Address Line 1");
        jurorPaperResponse.setAddressLine4("New Town");
        jurorPaperResponse.setPostcode("WA16 0PB");
        jurorPaperResponse.setDateOfBirth(LocalDate.of(1990, 1, 25));
        jurorPaperResponse.setBail(Boolean.FALSE);
        jurorPaperResponse.setConvictions(Boolean.FALSE);
        jurorPaperResponse.setMentalHealthAct(Boolean.FALSE);
        jurorPaperResponse.setMentalHealthCapacity(Boolean.FALSE);
        jurorPaperResponse.setResidency(Boolean.TRUE);
        jurorPaperResponse.setSigned(Boolean.TRUE);
        return jurorPaperResponse;
    }

    private PaperResponse createPaperResponseFromJurorPool(Juror juror) {
        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setJurorNumber(juror.getJurorNumber());
        jurorPaperResponse.setTitle(juror.getTitle());
        jurorPaperResponse.setFirstName(juror.getFirstName());
        jurorPaperResponse.setLastName(juror.getLastName());
        jurorPaperResponse.setAddressLine1(juror.getAddressLine1());
        jurorPaperResponse.setAddressLine2(juror.getAddressLine2());
        jurorPaperResponse.setAddressLine3(juror.getAddressLine3());
        jurorPaperResponse.setAddressLine4(juror.getAddressLine4());
        jurorPaperResponse.setAddressLine5(juror.getAddressLine5());
        jurorPaperResponse.setPostcode(juror.getPostcode());
        jurorPaperResponse.setDateOfBirth(juror.getDateOfBirth());

        jurorPaperResponse.setBail(Boolean.FALSE);
        jurorPaperResponse.setConvictions(Boolean.FALSE);
        jurorPaperResponse.setMentalHealthAct(Boolean.FALSE);
        jurorPaperResponse.setMentalHealthCapacity(Boolean.FALSE);
        jurorPaperResponse.setResidency(Boolean.TRUE);
        jurorPaperResponse.setSigned(Boolean.TRUE);

        return jurorPaperResponse;
    }

    private JurorReasonableAdjustment createReasonableAdjustmentsPaper(String jurorNumber) {
        JurorReasonableAdjustment reasonableAdjustment = new JurorReasonableAdjustment();
        reasonableAdjustment.setJurorNumber(jurorNumber);
        reasonableAdjustment.setReasonableAdjustment(new ReasonableAdjustments("V", "VISUAL IMPAIRMENT"));
        return reasonableAdjustment;
    }

    private List<JurorReasonableAdjustment> createmultipleReasonableAdjustmentsPaper(String jurorNumber) {
        List<JurorReasonableAdjustment> reasonableAdjustments = new ArrayList<>();

        JurorReasonableAdjustment reasonableAdjustment1 = new JurorReasonableAdjustment();
        reasonableAdjustment1.setJurorNumber(jurorNumber);
        reasonableAdjustment1.setReasonableAdjustment(new ReasonableAdjustments("V", "VISUAL IMPAIRMENT"));
        reasonableAdjustments.add(reasonableAdjustment1);

        JurorReasonableAdjustment reasonableAdjustment2 = new JurorReasonableAdjustment();
        reasonableAdjustment2.setJurorNumber(jurorNumber);
        reasonableAdjustment2.setReasonableAdjustment(new ReasonableAdjustments("D", "DIET"));
        reasonableAdjustments.add(reasonableAdjustment2);

        return reasonableAdjustments;
    }

    private JurorReasonableAdjustment createReasonableAdjustmentsDigital(String jurorNumber) {
        JurorReasonableAdjustment reasonableAdjustment = new JurorReasonableAdjustment();
        reasonableAdjustment.setJurorNumber(jurorNumber);
        reasonableAdjustment.setReasonableAdjustment(new ReasonableAdjustments("V", "VISUAL IMPAIRMENT"));
        return reasonableAdjustment;
    }

    private List<JurorReasonableAdjustment> createMultipleReasonableAdjustmentsDigital(String jurorNumber) {
        List<JurorReasonableAdjustment> reasonableAdjustments = new ArrayList<>();

        JurorReasonableAdjustment reasonableAdjustment1 = new JurorReasonableAdjustment();
        reasonableAdjustment1.setJurorNumber(jurorNumber);
        reasonableAdjustment1.setReasonableAdjustment(new ReasonableAdjustments("V", "VISUAL IMPAIRMENT"));
        reasonableAdjustments.add(reasonableAdjustment1);

        JurorReasonableAdjustment reasonableAdjustment2 = new JurorReasonableAdjustment();
        reasonableAdjustment2.setJurorNumber(jurorNumber);
        reasonableAdjustment2.setReasonableAdjustment(new ReasonableAdjustments("D", "DIET"));
        reasonableAdjustments.add(reasonableAdjustment2);

        return reasonableAdjustments;
    }

    private BureauJwtPayload buildPayload() {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login("test_user")
            .owner("400")
            .build();
    }

    private Map<String, Boolean> initChangedPropertiesMap(Boolean defaultValue) {
        HashMap<String, Boolean> changedProperties = new HashMap<>();
        changedProperties.put("title", defaultValue);
        changedProperties.put("date Of Birth", defaultValue);
        changedProperties.put("address", defaultValue);
        changedProperties.put("postcode", defaultValue);
        return changedProperties;
    }

}
