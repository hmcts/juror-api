package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.ConfirmAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.ModifyConfirmedAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsOnTrialResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.UnconfirmedJurorDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.UnconfirmedJurorResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusGroup;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.service.AppearanceCreationService;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;
import uk.gov.hmcts.juror.api.moj.service.trial.PanelService;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.APPEARANCE_RECORD_BEFORE_SERVICE_START_DATE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.ATTENDANCE_RECORD_ALREADY_EXISTS;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CANNOT_PROCESS_EMPANELLED_JUROR;
import static uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils.getNextWorkingDay;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.isEmptyOrNull;
import static uk.gov.hmcts.juror.api.moj.utils.JurorUtils.checkOwnershipForCurrentUser;
import static uk.gov.hmcts.juror.api.moj.utils.JurorUtils.getActiveJurorRecord;
import static uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils.unboxOptionalRecord;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.GodClass", "PMD.CyclomaticComplexity"})
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class JurorAppearanceServiceImpl implements JurorAppearanceService {
    private final TrialRepository trialRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final AppearanceRepository appearanceRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;
    private final JurorExpenseService jurorExpenseService;
    private final JurorHistoryServiceImpl jurorHistoryService;
    private final PanelRepository panelRepository;
    private final JurorPoolService jurorPoolService;
    private final AppearanceCreationService appearanceCreationService;
    private final PanelService panelService;

    @Override
    @Transactional
    public void addAttendanceDay(BureauJwtPayload payload, AddAttendanceDayDto dto) {

        //check that the appearance date is not in the future
        checkAttendanceDateIsNotAFutureDate(dto.getAttendanceDate());
        final String jurorNumber = dto.getJurorNumber();

        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(
            jurorNumber, dto.getPoolNumber());

        if (jurorPool == null) {
            throw new MojException.NotFound("No valid juror pool found", null);
        }
        SecurityUtil.validateIsLocCode(dto.getLocationCode());

        if (!jurorPool.getOwner().equals(payload.getOwner())) {
            throw new MojException.Forbidden("Invalid access to juror pool", null);
        }

        final boolean isCompleted = jurorPool.getStatus().getStatus() == IJurorStatus.COMPLETED;

        CourtLocation courtLocation = courtLocationRepository.findByLocCode(dto.getLocationCode()).orElseThrow(
            () -> new MojException.NotFound("Court location not found", null));

        JurorAppearanceDto appearanceDto = dto.getJurorAppearanceDto();
        if (hasAttendance(dto.getLocationCode(), jurorNumber, appearanceDto.getAttendanceDate())) {
            throw new MojException.BusinessRuleViolation("Attendance record already exists",
                ATTENDANCE_RECORD_ALREADY_EXISTS);
        }
        processAppearance(payload, appearanceDto, true, isCompleted);

        // Read the newly created appearance record
        AppearanceId appearanceId = new AppearanceId(jurorNumber, dto.getAttendanceDate(), courtLocation);
        Appearance appearance = appearanceRepository.findById(appearanceId).orElseThrow(
            () -> new MojException.InternalServerError("Error adding attendance record for juror " + jurorNumber,
                null));

        final String poolAttendancePrefix = "P";
        final String poolAttendanceNumber = getAttendanceAuditNumber(poolAttendancePrefix);

        appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
        realignAttendanceType(appearance);
        appearance.setAttendanceAuditNumber(poolAttendanceNumber);

        jurorHistoryService.createPoolAttendanceHistory(jurorPool, appearance);
        jurorExpenseService.applyDefaultExpenses(List.of(appearance));
        appearanceRepository.saveAndFlush(appearance);
    }

    private boolean hasAttendance(String locCode, String jurorNumber, LocalDate attendanceDate) {
        return appearanceRepository.findByLocCodeAndJurorNumberAndAttendanceDate(locCode, jurorNumber, attendanceDate)
            .isPresent();
    }

    @Override
    @Transactional
    public JurorAppearanceResponseDto.JurorAppearanceResponseData processAppearance(
        BureauJwtPayload payload, JurorAppearanceDto jurorAppearanceDto) {
        return processAppearance(payload, jurorAppearanceDto, false, false);
    }

    JurorAppearanceResponseDto.JurorAppearanceResponseData processAppearance(
        BureauJwtPayload payload, JurorAppearanceDto jurorAppearanceDto,
        boolean allowBothCheckInAndOut, boolean isCompleted) {

        final String jurorNumber = jurorAppearanceDto.getJurorNumber();
        final String locCode = jurorAppearanceDto.getLocationCode();
        final LocalDate appearanceDate = jurorAppearanceDto.getAttendanceDate();
        final AppearanceStage appearanceStage = jurorAppearanceDto.getAppearanceStage();

        // validate the request to ensure the time and appearance stage combination are valid
        validateTimeAndAppearanceStage(jurorAppearanceDto.getCheckInTime(), jurorAppearanceDto.getCheckOutTime(),
            appearanceStage, allowBothCheckInAndOut);

        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode, payload.getOwner(),
            courtLocationRepository);

        // validate the juror record exists and user has ownership of the record
        Juror juror = getActiveJurorRecord(jurorRepository, jurorNumber);
        checkOwnershipForCurrentUser(juror, payload.getOwner());

        // check juror status to make sure they can be checked in
        final JurorPool jurorPool = validateJurorStatus(juror, isCompleted);

        // check if juror has been empanelled
        if (panelService.isEmpanelledJuror(jurorNumber, locCode, appearanceDate)) {
            throw new MojException.BusinessRuleViolation("Juror has been empanelled: " + jurorNumber,
                                                         CANNOT_PROCESS_EMPANELLED_JUROR);
        }

        Optional<Appearance> appearanceOpt = getAppearance(jurorNumber, appearanceDate, locCode);
        Appearance appearance;

        if (appearanceOpt.isPresent()) {
            appearance = appearanceOpt.get();
            // validate the current record and the new appearance stage
            validateAppearanceStage(jurorNumber, appearanceStage, appearance);
        } else {
            appearance = appearanceCreationService.createAppearance(
                jurorNumber,
                appearanceDate,
                courtLocation,
                jurorPool.getPool().getPoolNumber(),
                false
            );
        }

        if (appearanceStage == AppearanceStage.CHECKED_IN || allowBothCheckInAndOut) {
            appearance.setTimeIn(jurorAppearanceDto.getCheckInTime());
        }

        if (appearanceStage == AppearanceStage.CHECKED_OUT || allowBothCheckInAndOut) {
            appearance.setTimeOut(jurorAppearanceDto.getCheckOutTime());
        }

        appearance.setAppearanceStage(appearanceStage);
        realignAttendanceType(appearance);
        // update the juror next date and clear on call flag in case it is set
        jurorPool.setNextDate(getNextWorkingDay(locCode));

        jurorPool.setOnCall(false);
        jurorPoolRepository.saveAndFlush(jurorPool);

        // now read the data back and send to front end
        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList =
            appearanceRepository.getAppearanceRecords(locCode, appearanceDate, jurorNumber,
                JurorStatusGroup.ALL);

        if (appearanceDataList.size() != 1) {
            throw new MojException.InternalServerError("Error checking in juror " + jurorNumber, null);
        }

        return appearanceDataList.get(0);
    }


    private void checkAttendanceDateIsNotAFutureDate(LocalDate attendanceDate) {
        if (attendanceDate.isAfter(LocalDate.now())) {
            log.error("Requested date is in the future.");
            throw new MojException.BadRequest("Requested date is in the future.",
                null);
        }
    }

    @Override
    public JurorAppearanceResponseDto getAppearanceRecords(String locCode, LocalDate date,
                                                           BureauJwtPayload payload, JurorStatusGroup group) {

        CourtLocationUtils.validateAccessToCourtLocation(locCode, payload.getOwner(), courtLocationRepository);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList =
            appearanceRepository.getAppearanceRecords(locCode, date, null, group);

        return new JurorAppearanceResponseDto(appearanceDataList);
    }

    @Override
    public boolean hasAppearances(String jurorNumber) {
        return appearanceRepository.countByJurorNumber(jurorNumber) > 0;
    }

    @Override
    public boolean hasAttendances(String jurorNumber) {
        return appearanceRepository.countNoneAbsentAttendances(jurorNumber) > 0;
    }

    @Override
    public Optional<Appearance> getFirstAppearanceWithAuditNumber(String auditNumber, Collection<String> locCodes) {
        return appearanceRepository.findFirstByAttendanceAuditNumberEqualsAndLocCodeIn(auditNumber, locCodes);
    }

    @Override
    public AttendanceDetailsResponse retrieveAttendanceDetails(BureauJwtPayload payload,
                                                               RetrieveAttendanceDetailsDto request) {
        final RetrieveAttendanceDetailsDto.CommonData commonData = request.getCommonData();

        CourtLocationUtils.validateAccessToCourtLocation(commonData.getLocationCode(), payload.getOwner(),
            courtLocationRepository);

        // retrieve attendance details
        List<Tuple> attendanceTuples = appearanceRepository.retrieveAttendanceDetails(request);

        if (request.getCommonData().getTag().equals(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE)) {
            // the response for confirm_attendance is different
            return retrieveConfirmAttendance(attendanceTuples.size(), commonData);
        }

        return buildAttendanceDetailsResponse(attendanceTuples);
    }

    @Override
    @Transactional
    public AttendanceDetailsResponse updateAttendance(BureauJwtPayload payload, UpdateAttendanceDto request) {
        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();
        final UpdateAttendanceStatus status = commonData.getStatus();

        // confirm-attendance
        if (status.equals(UpdateAttendanceStatus.CONFIRM_ATTENDANCE)) {
            return updateConfirmAttendance(commonData, request.getJuror());
        }

        // check-in jurors
        if (status.equals(UpdateAttendanceStatus.CHECK_IN)) {
            return updateCheckIn(request, payload.getOwner());
        }

        // check-out jurors
        if (status.equals(UpdateAttendanceStatus.CHECK_OUT)) {
            return updateCheckOut(request, payload.getOwner());
        }

        // check-in-and-out jurors
        if (status.equals(UpdateAttendanceStatus.CHECK_IN_AND_OUT)) {
            return updateCheckInAndOut(request, payload.getOwner());
        }

        // check-out panelled jurors
        if (status.equals(UpdateAttendanceStatus.CHECK_OUT_PANELLED)) {
            return updateCheckOutPanelled(request, payload.getOwner());
        }

        // shouldn't have got to this point.
        throw new MojException.BadRequest("Failed to update attendance. Ensure the request is valid.",
            null);
    }

    @Override
    @Transactional
    public String updateAttendanceDate(UpdateAttendanceDateDto request) {
        log.trace(String.format("Entered method: updateAttendanceDate(). There are %s jurors to update",
            request.getJurorNumbers().size()));

        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        List<JurorPool> updatedJurorPools = new ArrayList<>();

        for (String jurorNumber : request.getJurorNumbers()) {
            JurorPool jurorPool = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                payload.getOwner(), jurorNumber, request.getPoolNumber(), Boolean.TRUE);

            if (jurorPool == null) {
                log.trace(String.format("In method: updateAttendanceDate().  No juror pool found matching criteria "
                    + "for juror %s.  Attendance date not updated", jurorNumber));
            } else if (List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL, IJurorStatus.JUROR)
                .contains(jurorPool.getStatus().getStatus())) {

                // update the juror next (attendance) date and clear on call flag in case it is set
                jurorPool.setNextDate(request.getAttendanceDate());
                jurorPool.setOnCall(Boolean.FALSE);

                updatedJurorPools.add(jurorPool);
            } else {
                log.trace(String.format("In method: updateAttendanceDate(). Status is %s for juror %s. "
                    + "Attendance date not updated", jurorPool.getStatus().getStatus(), jurorNumber));
            }
        }

        if (!updatedJurorPools.isEmpty()) {
            jurorPoolRepository.saveAllAndFlush(updatedJurorPools);
        }

        String message = String.format("Attendance date updated for %s juror(s)", updatedJurorPools.size());
        log.trace("Exiting method: updateAttendanceDate(). " + message);
        return message;
    }

    @Override
    @Transactional
    public void modifyConfirmedAttendance(ModifyConfirmedAttendanceDto request) {
        final LocalDate attendanceDate = request.getAttendanceDate();
        final String jurorNumber = request.getJurorNumber();

        log.debug(
            String.format("User %s is modifying attendance for juror %s", SecurityUtil.getActiveLogin(), jurorNumber));

        // get the appearance record if it exists
        Appearance appearance =
            appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                    SecurityUtil.getLocCode(),
                    jurorNumber,
                    attendanceDate)
                .orElseThrow(() -> new MojException.NotFound("No valid appearance record found", null));

        modifyConfirmedAttendance(appearance,
            request.getModifyAttendanceType(),
            request.getCheckInTime(),
            request.getCheckOutTime());
    }

    private void modifyConfirmedAttendance(Appearance appearance,
                                           ModifyConfirmedAttendanceDto.ModifyAttendanceType modifyAttendanceType,
                                           LocalTime checkInTime,
                                           LocalTime checkOutTime
    ) {
        SecurityUtil.validateIsLocCode(appearance.getLocCode());
        if (appearance.getAppearanceStage() != null
            && !Set.of(AppearanceStage.EXPENSE_ENTERED, AppearanceStage.CHECKED_IN, AppearanceStage.CHECKED_OUT)
            .contains(appearance.getAppearanceStage())) {
            throw new MojException.BusinessRuleViolation(
                "Can only modify confirmed attendances that have no approved expenses",
                MojException.BusinessRuleViolation.ErrorCode.APPEARANCE_MUST_HAVE_NO_APPROVED_EXPENSES);
        }

        appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
        appearance.setNoShow(false);
        appearance.setAttendanceType(null);
        if (ModifyConfirmedAttendanceDto.ModifyAttendanceType.ATTENDANCE.equals(modifyAttendanceType)) {
            appearance.setNonAttendanceDay(Boolean.FALSE);
            //default value will get realigned apart of realignAttendanceType(...)
            appearance.setAttendanceType(AttendanceType.FULL_DAY);

            // update the check-in time
            if (checkInTime != null) {
                appearance.setTimeIn(checkInTime);
            }

            // update the check-out time
            if (checkOutTime != null) {
                appearance.setTimeOut(checkOutTime);
            }
            realignAttendanceType(appearance);

        } else if (modifyAttendanceType.equals(ModifyConfirmedAttendanceDto.ModifyAttendanceType.NON_ATTENDANCE)) {
            appearance.setTimeIn(null);
            appearance.setTimeOut(null);
            appearance.setNonAttendanceDay(Boolean.TRUE);
            appearance.setAttendanceType(AttendanceType.NON_ATTENDANCE);
            realignAttendanceType(appearance);

        } else if (modifyAttendanceType.equals(ModifyConfirmedAttendanceDto.ModifyAttendanceType.ABSENCE)) {
            appearance.setTimeIn(null);
            appearance.setTimeOut(null);
            appearance.setAppearanceStage(null);
            appearance.setNonAttendanceDay(Boolean.FALSE);
            appearance.setAttendanceType(AttendanceType.ABSENT);
            appearance.setNoShow(Boolean.TRUE);
        }

        if (ModifyConfirmedAttendanceDto.ModifyAttendanceType.DELETE.equals(modifyAttendanceType)) {
            appearanceRepository.delete(appearance);
            realignAttendanceType(appearance.getJurorNumber());
        } else {
            appearanceRepository.saveAndFlush(appearance);
            jurorExpenseService.realignExpenseDetails(appearance);
        }
    }

    @Override
    public void markJurorAsAbsent(BureauJwtPayload payload,
                                  UpdateAttendanceDto.CommonData updateCommonData) {
        // 1. retrieve details of jurors who attended court for the given attendance date (checked-in)
        RetrieveAttendanceDetailsDto.CommonData retrieveCommonData = new RetrieveAttendanceDetailsDto.CommonData();
        retrieveCommonData.setAttendanceDate(updateCommonData.getAttendanceDate());
        retrieveCommonData.setLocationCode(updateCommonData.getLocationCode());
        retrieveCommonData.setTag(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE);

        // 2. retrieve details of jurors who failed to show up on the day (no show)
        List<Tuple> absentTuples = appearanceRepository.retrieveNonAttendanceDetails(retrieveCommonData);

        CourtLocation courtLocation = courtLocationRepository.findByLocCode(updateCommonData.getLocationCode())
            .orElseThrow(() -> new MojException.NotFound("Court location not found", null));

        // 3. absent jurors - build new appearance record with minimal data
        List<Appearance> absentJurors = new ArrayList<>();
        absentTuples.forEach(tuple -> {
            Appearance appearance = appearanceCreationService.createAbsentAppearance(
                tuple.get(0, String.class),
                updateCommonData.getAttendanceDate(),
                courtLocation,
                tuple.get(4, String.class),
                false
            );
            absentJurors.add(appearance);
        });

        appearanceRepository.saveAllAndFlush(absentJurors);

    }

    @Override
    @Transactional(readOnly = true)
    public JurorsToDismissResponseDto retrieveJurorsToDismiss(JurorsToDismissRequestDto request) {

        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        String locationCode = request.getLocationCode();

        log.debug(String.format("User %s is retrieving jurors to dismiss for court location %s", payload.getLogin(),
            locationCode));

        CourtLocationUtils.validateAccessToCourtLocation(locationCode, payload.getOwner(), courtLocationRepository);

        // retrieve the list of jurors to dismiss based on request
        List<JurorPool> jurorsToDismiss = retrieveJurorPoolRecords(request);

        if (jurorsToDismiss.isEmpty()) {
            // return an empty response
            return new JurorsToDismissResponseDto(new ArrayList<>());
        }

        // shuffle the list of jurors to dismiss for random selection
        Collections.shuffle(jurorsToDismiss);

        // retrieve the juror numbers
        List<String> jurorNumbers = jurorsToDismiss.stream().map(JurorPool::getJuror).map(Juror::getJurorNumber)
            .toList();

        // limit the number of jurors to dismiss
        jurorNumbers = jurorNumbers.stream().limit(request.getNumberOfJurorsToDismiss()).toList();

        List<Tuple> jurorsToDismissTuples = jurorPoolRepository.getJurorsToDismiss(request.getPoolNumbers(),
            jurorNumbers, locationCode);

        List<JurorsToDismissResponseDto.JurorsToDismissData> jurorsToDismissResponseData =
            buildJurorsToDismissResponse(jurorsToDismissTuples);

        return new JurorsToDismissResponseDto(jurorsToDismissResponseData);
    }

    @Override
    @Transactional
    public void addNonAttendance(JurorNonAttendanceDto request) {

        // validate the court user has access to the juror and pool
        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        final String locationCode = request.getLocationCode();
        final LocalDate nonAttendanceDate = request.getNonAttendanceDate();
        SecurityUtil.validateIsLocCode(locationCode);

        log.debug(String.format("User %s is adding a non attendance day for juror %s", payload.getLogin(),
            request.getJurorNumber()));

        CourtLocation courtLocation = courtLocationRepository.findByLocCode(locationCode).orElseThrow(
            () -> new MojException.NotFound("Court location " + locationCode + " not found", null)
        );
        JurorPool jurorPool = validateJurorPoolAndStartDate(request, nonAttendanceDate);
        checkExistingAttendance(request, nonAttendanceDate);

        // create a new Appearance record for juror
        Appearance appearance =
            appearanceCreationService.createNoneAttendanceAppearance(
                request.getJurorNumber(), nonAttendanceDate, courtLocation,
                jurorPool.getPool().getPoolNumber(), false);
        realignAttendanceType(appearance);
        jurorExpenseService.applyDefaultExpenses(appearance, jurorPool.getJuror());
        appearanceRepository.saveAndFlush(appearance);
        log.debug("Completed adding a non attendance day for juror {}", request.getJurorNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public JurorsOnTrialResponseDto retrieveJurorsOnTrials(String locationCode, LocalDate attendanceDate) {

        String owner = SecurityUtil.getActiveUsersBureauPayload().getOwner();

        // check user has access to the location
        CourtLocationUtils.validateAccessToCourtLocation(locationCode, owner, courtLocationRepository);

        JurorsOnTrialResponseDto jurorsOnTrialResponseDto = new JurorsOnTrialResponseDto();
        jurorsOnTrialResponseDto.setTrialsList(new ArrayList<>());

        // run query to retrieve list of trials with juror count
        List<Tuple> jurorsOnTrialsTuples = trialRepository.getActiveTrialsWithJurorCount(locationCode, attendanceDate);

        // build the response
        for (Tuple tuple : jurorsOnTrialsTuples) {
            JurorsOnTrialResponseDto.JurorsOnTrialResponseData jurorsOnTrialData =
                JurorsOnTrialResponseDto.JurorsOnTrialResponseData.builder()
                    .trialNumber(tuple.get(QTrial.trial.trialNumber))
                    .parties(tuple.get(QTrial.trial.description))
                    .trialType(tuple.get(QTrial.trial.trialType).getDescription())
                    .courtroom(tuple.get(QTrial.trial.courtroom.description))
                    .judge(tuple.get(QTrial.trial.judge.name))
                    .totalJurors(tuple.get(QPanel.panel.count()))
                    .build();

            jurorsOnTrialResponseDto.getTrialsList().add(jurorsOnTrialData);
        }

        // run query to retrieve list of trials and number attended
        List<Tuple> jurorsAttendanceCounts = appearanceRepository.getTrialsWithAttendanceCount(locationCode,
            attendanceDate);

        // update the response with the number of jurors attended
        jurorsOnTrialResponseDto.getTrialsList().forEach(jurorsOnTrialData -> {
            jurorsAttendanceCounts.forEach(tuple -> {
                if (jurorsOnTrialData.getTrialNumber().equals(tuple.get(QPanel.panel.trial.trialNumber))) {
                    jurorsOnTrialData.setNumberAttended(tuple.get(QAppearance.appearance.jurorNumber.count()));
                    jurorsOnTrialData.setAttendanceAudit(tuple.get(QAppearance.appearance.attendanceAuditNumber));
                }
            });
        });

        // return the response
        return jurorsOnTrialResponseDto;
    }

    @Override
    @SuppressWarnings("PMD.CognitiveComplexity")
    @Transactional
    public void confirmJuryAttendance(UpdateAttendanceDto request) {
        log.info("Confirming jury attendance for jurors on trial");

        final String owner = SecurityUtil.getActiveOwner();
        final String locCode = request.getCommonData().getLocationCode();

        // see if there is an attendance record for the date to get the audit number
        String trialNumber = request.getTrialNumber();
        List<Appearance> appearances = appearanceRepository.findByLocCodeAndAttendanceDateAndTrialNumber(locCode,
                                                                         request.getCommonData().getAttendanceDate(),
                                                                         trialNumber
        );

        final String juryAttendancePrefix = "J";
        String juryAttendanceNumber;

        if (!appearances.isEmpty()) {
            // check if there is an attendance_audit_number already set and retrieve it
            juryAttendanceNumber = appearances.stream()
                .map(Appearance::getAttendanceAuditNumber)
                .filter(ObjectUtils::isNotEmpty)
                .findFirst().orElse(getAttendanceAuditNumber(juryAttendancePrefix));
        } else {
            juryAttendanceNumber = getAttendanceAuditNumber(juryAttendancePrefix);
        }

        CourtLocation courtLocation =
            courtLocationRepository.findByLocCode(locCode)
                .orElseThrow(() -> new MojException.NotFound("Court location not found", null));

        request.getJuror().forEach(jurorNumber -> {
            // validate the juror record exists and user has ownership of the record
            validateJuror(owner, jurorNumber);

            JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, jurorNumber,
                courtLocation);

            // get the juror appearance record if it exists
            Appearance appearance = getAppearance(jurorNumber, request.getCommonData().getAttendanceDate(), locCode)
                .orElseGet(() -> appearanceRepository.save(
                    appearanceCreationService.createAppearance(
                        jurorNumber,
                        request.getCommonData().getAttendanceDate(),
                        courtLocation,
                        jurorPool.getPool().getPoolNumber(),
                        false
                    )));

            if (appearance.getAppearanceStage() != null
                && AppearanceStage.getConfirmedAppearanceStages().contains(appearance.getAppearanceStage())) {
                throw new MojException.BusinessRuleViolation(
                    "Juror " + jurorNumber + " has already confirmed their attendance for this day",
                    MojException.BusinessRuleViolation.ErrorCode.DAY_ALREADY_CONFIRMED);
            }

            // update the check-in time if there is none
            if (appearance.getTimeIn() == null) {
                appearance.setTimeIn(request.getCommonData().getCheckInTime());
            }

            // update the check-out time if there is none
            if (appearance.getTimeOut() == null) {
                appearance.setTimeOut(request.getCommonData().getCheckOutTime());
            }

            appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
            realignAttendanceType(appearance);
            appearance.setAppearanceConfirmed(Boolean.TRUE);

            appearance.setAttendanceAuditNumber(juryAttendanceNumber);
            appearance.setSatOnJury(true);

            // update appearance by adding the trial number
            Panel panel;
            if (trialNumber == null) {
                panel = panelRepository.findActivePanel(locCode, jurorNumber);
            } else {
                panel = panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumber(
                    trialNumber, locCode, jurorNumber);
            }

            if (panel == null) {
                log.info("Panel not found for juror {} on trial {}", jurorNumber, trialNumber);
                throw new MojException.NotFound("Panel not found for juror " + jurorNumber, null);
            }

            appearance.setTrialNumber(panel.getTrial().getTrialNumber());
            jurorHistoryService.createJuryAttendanceHistory(jurorPool, appearance, panel);

            appearanceRepository.saveAndFlush(appearance);
            jurorExpenseService.applyDefaultExpenses(appearance, jurorPool.getJuror());

            // update the juror next date and clear on call flag in case it is set
            jurorPool.setNextDate(request.getCommonData().getAttendanceDate());
            jurorPool.setOnCall(false);
            jurorPoolRepository.saveAndFlush(jurorPool);
        });
    }

    private void checkExistingAttendance(JurorNonAttendanceDto request, LocalDate nonAttendanceDate) {
        final String locCode = request.getLocationCode();
        final String jurorNumber = request.getJurorNumber();
        // check if there is already an appearance record for the juror for the non-attendance date
        appearanceRepository.findByLocCodeAndJurorNumberAndAttendanceDate(locCode, jurorNumber, nonAttendanceDate)
            .ifPresent(appearance -> {
                if (appearance.getAppearanceStage() != null) {
                    throw new MojException.BusinessRuleViolation("Juror " + jurorNumber + " already has an "
                        + "attendance record for the date " + nonAttendanceDate, ATTENDANCE_RECORD_ALREADY_EXISTS);
                }
                if (appearance.getNoShow() != null && appearance.getNoShow()) {
                    // this record will get replaced with the new non-attendance record
                    appearanceRepository.delete(appearance);
                    log.info("Deleted existing no-show record for juror {} on date {}", jurorNumber, nonAttendanceDate);
                }
            });
    }

    private JurorPool validateJurorPoolAndStartDate(JurorNonAttendanceDto request, LocalDate nonAttendanceDate) {
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(request.getJurorNumber(),
            request.getPoolNumber());

        if (jurorPool == null) {
            throw new MojException.NotFound("Juror not found in Pool " + request.getPoolNumber(), null);
        }

        if (jurorPool.getPool().getReturnDate().isAfter(nonAttendanceDate)) {
            throw new MojException.BusinessRuleViolation(
                "Non-attendance date is before the service start date of the pool",
                APPEARANCE_RECORD_BEFORE_SERVICE_START_DATE);
        }
        return jurorPool;
    }

    private static void validateAppearanceStage(String jurorNumber, AppearanceStage appearanceStage,
                                                Appearance appearance) {
        if (appearance.getAppearanceStage().equals(AppearanceStage.CHECKED_IN) && appearanceStage.equals(
            AppearanceStage.CHECKED_IN)) {
            throw new MojException.BadRequest("Juror " + jurorNumber + " has already checked in", null);
        } else if (appearance.getAppearanceStage().equals(AppearanceStage.CHECKED_OUT)) {
            throw new MojException.BadRequest("Juror " + jurorNumber + " has already checked out", null);
        } else if (Set.of(AppearanceStage.EXPENSE_ENTERED, AppearanceStage.EXPENSE_EDITED,
                AppearanceStage.EXPENSE_AUTHORISED)
            .contains(appearance.getAppearanceStage())) {
            throw new MojException.BadRequest("Juror " + jurorNumber + " has already confirmed their attendance", null);
        }
    }

    void validateTimeAndAppearanceStage(LocalTime checkInTime, LocalTime checkOutTime,
                                        AppearanceStage appearanceStage, boolean allowCheckInAndOut) {
        if (allowCheckInAndOut) {
            validateCheckInNotNull(checkInTime);
            validateCheckOutNotNull(checkOutTime);
        } else if (appearanceStage == AppearanceStage.CHECKED_IN) {
            validateCheckInNotNull(checkInTime);
        } else if (appearanceStage == AppearanceStage.CHECKED_OUT) {
            validateCheckOutNotNull(checkOutTime);
        } else {
            throw new MojException.BadRequest("Invalid appearance stage and time combination supplied", null);
        }
        validateBothCheckInAndOutTimeNotNull(checkInTime, checkOutTime);

        if (!allowCheckInAndOut) {
            validateBothCheckInAndOutTimeNotSet(checkInTime, checkOutTime);
        }

    }

    private static UpdateAttendanceDto.CommonData validateDeleteRequest(UpdateAttendanceDto request) {
        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();

        if (request.getJuror().size() > 1 ^ commonData.getSingleJuror().equals(Boolean.FALSE)) {
            throw new MojException.BadRequest("Cannot delete multiple juror attendance records",
                null);
        }

        if (!commonData.getStatus().equals(UpdateAttendanceStatus.DELETE)) {
            throw new MojException.BadRequest("Cannot delete attendance records for status "
                + commonData.getStatus(), null);
        }
        return commonData;
    }

    private List<JurorsToDismissResponseDto.JurorsToDismissData> buildJurorsToDismissResponse(
        List<Tuple> jurorsToDismissTuples) {
        List<JurorsToDismissResponseDto.JurorsToDismissData> jurorsToDismissResponseData = new ArrayList<>();

        jurorsToDismissTuples.forEach(tuple -> {
            JurorsToDismissResponseDto.JurorsToDismissData jurorsToDismissData =
                JurorsToDismissResponseDto.JurorsToDismissData.builder()
                    .jurorNumber(tuple.get(0, String.class))
                    .firstName(tuple.get(1, String.class))
                    .lastName(tuple.get(2, String.class))
                    .attending(tuple.get(3, String.class))
                    .checkInTime(tuple.get(4, LocalTime.class))
                    .nextDueAtCourt(tuple.get(5, String.class))
                    .serviceStartDate(tuple.get(6, LocalDate.class))
                    .build();

            jurorsToDismissResponseData.add(jurorsToDismissData);
        });

        return jurorsToDismissResponseData;
    }

    private List<JurorPool> retrieveJurorPoolRecords(JurorsToDismissRequestDto request) {

        // Always need the jurors in attendance
        List<JurorPool> jurorPools =
            jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(request.getLocationCode(),
                request.getPoolNumbers());

        if (ObjectUtils.defaultIfNull(request.getIncludeOnCall(), false)) {
            List<JurorPool> jurorPoolOnCall =
                jurorPoolRepository.findJurorsOnCallAtCourtLocation(request.getLocationCode(),
                    request.getPoolNumbers());
            jurorPools.addAll(jurorPoolOnCall);
        }

        if (ObjectUtils.defaultIfNull(request.getIncludeNotInAttendance(), false)) {
            List<JurorPool> jurorPoolNotInAttendance =
                jurorPoolRepository.findJurorsNotInAttendanceAtCourtLocation(request.getLocationCode(),
                    request.getPoolNumbers());
            jurorPools.addAll(jurorPoolNotInAttendance);
        }

        return jurorPools;
    }

    private List<String> retrieveJurorsToUpdate(UpdateAttendanceDto.CommonData updateCommonData,
                                                RetrieveAttendanceDetailsTag tag) {
        // build the request
        RetrieveAttendanceDetailsDto.CommonData retrieveCommonData = new RetrieveAttendanceDetailsDto.CommonData();
        retrieveCommonData.setAttendanceDate(updateCommonData.getAttendanceDate());
        retrieveCommonData.setLocationCode(updateCommonData.getLocationCode());
        retrieveCommonData.setTag(tag);

        RetrieveAttendanceDetailsDto request = RetrieveAttendanceDetailsDto.builder()
            .commonData(retrieveCommonData)
            .build();

        // retrieve the details
        List<Tuple> resultTuples = appearanceRepository.retrieveAttendanceDetails(request);

        // build the juror list
        ArrayList<String> jurorNumbers = new ArrayList<>();
        resultTuples.forEach(tuple -> jurorNumbers.add(tuple.get(0, String.class)));

        return jurorNumbers;
    }

    private Appearance retrieveExistingAppearanceDetails(String locCode,
                                                         String jurorNumber, LocalDate attendanceDate) {
        CourtLocation courtLocation = courtLocationRepository.findByLocCode(locCode)
            .orElseThrow(() -> new MojException.NotFound("Court location not found", null));

        return unboxOptionalRecord(appearanceRepository
            .findById(new AppearanceId(jurorNumber, attendanceDate, courtLocation)), jurorNumber);
    }

    private AttendanceDetailsResponse retrieveConfirmAttendance(int noCheckedIn,
                                                                RetrieveAttendanceDetailsDto.CommonData commonData) {
        // retrieve non attendance details
        List<Tuple> absentTuples = appearanceRepository.retrieveNonAttendanceDetails(commonData);

        // build summary section
        AttendanceDetailsResponse.Summary summary = new AttendanceDetailsResponse.Summary();
        summary.setAbsent(absentTuples.size());
        summary.setCheckedIn(noCheckedIn);

        // build details section
        List<AttendanceDetailsResponse.Details> details = buildAbsentResponse(absentTuples);

        // build and return the response object
        return AttendanceDetailsResponse.builder().details(details).summary(summary).build();
    }

    AttendanceDetailsResponse updateConfirmAttendance(UpdateAttendanceDto.CommonData updateCommonData,
                                                      List<String> jurors) {
        // 1. retrieve details of jurors who attended court for the given attendance date (checked-in)
        RetrieveAttendanceDetailsDto.CommonData retrieveCommonData = new RetrieveAttendanceDetailsDto.CommonData();
        retrieveCommonData.setAttendanceDate(updateCommonData.getAttendanceDate());
        retrieveCommonData.setLocationCode(updateCommonData.getLocationCode());
        retrieveCommonData.setTag(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE);

        RetrieveAttendanceDetailsDto request = RetrieveAttendanceDetailsDto.builder()
            .commonData(retrieveCommonData)
            .jurorInWaiting(true)
            .build();

        List<Tuple> checkedInJurors = appearanceRepository.retrieveAttendanceDetails(request);

        if (!jurors.isEmpty()) {
            checkedInJurors = checkedInJurors.stream()
                .filter(tuple -> jurors.contains(tuple.get(0, String.class)))
                .toList();
        }

        // 2. checked-in jurors - update and save records
        List<AppearanceId> appearanceIds = new ArrayList<>();

        CourtLocation courtLocation = courtLocationRepository.findByLocCode(updateCommonData.getLocationCode())
            .orElseThrow(() -> new MojException.NotFound("Court location not found", null));

        checkedInJurors.forEach(tuple -> {
            // build array of appearanceIds
            AppearanceId appearanceId = new AppearanceId(tuple.get(0, String.class),
                updateCommonData.getAttendanceDate(),
                courtLocation);
            appearanceIds.add(appearanceId);
        });

        // one attendance audit number applies to ALL jurors in this batch of attendances being confirmed
        final String poolAttendancePrefix = "P";
        final String poolAttendanceNumber = getAttendanceAuditNumber(poolAttendancePrefix);

        List<Appearance> checkedInAttendances = appearanceRepository.findAllById(appearanceIds);
        checkedInAttendances.forEach(appearance -> {
            appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
            appearance.setAppearanceConfirmed(Boolean.TRUE);
            realignAttendanceType(appearance);
            appearance.setAttendanceAuditNumber(poolAttendanceNumber);

            JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, appearance.getJurorNumber(),
                appearance.getCourtLocation());
            jurorHistoryService.createPoolAttendanceHistory(jurorPool, appearance);
        });

        jurorExpenseService.applyDefaultExpenses(checkedInAttendances);

        appearanceRepository.saveAllAndFlush(checkedInAttendances);

        // 3. retrieve details of jurors who failed to show up on the day (no show)
        List<Tuple> absentTuples = appearanceRepository.retrieveNonAttendanceDetails(retrieveCommonData);

        // 4. absent jurors - build new appearance record with minimal data
        List<Appearance> absentJurors = new ArrayList<>();
        absentTuples.forEach(tuple -> {
            Appearance appearance = appearanceCreationService.createAbsentAppearance(
                tuple.get(0, String.class),
                updateCommonData.getAttendanceDate(),
                courtLocation,
                tuple.get(4, String.class),
                true
            );
            absentJurors.add(appearance);
        });

        appearanceRepository.saveAllAndFlush(absentJurors);

        // 5. build and return a summary
        AttendanceDetailsResponse response = new AttendanceDetailsResponse();
        AttendanceDetailsResponse.Summary summary = AttendanceDetailsResponse.Summary.builder()
            .checkedIn(checkedInAttendances.size())
            .absent(absentJurors.size())
            .build();
        response.setSummary(summary);

        return response;
    }

    private String getAttendanceAuditNumber(String prefix) {

        // get the next available attendance number from the database sequence
        final long attendanceAuditNumber = appearanceRepository.getNextAttendanceAuditNumber();

        //  add a single character prefix to the audit number, "J" for jury attendance, "P" for pool attendance
        return prefix + attendanceAuditNumber;
    }

    private AttendanceDetailsResponse updateCheckIn(UpdateAttendanceDto request, String owner) {
        validateCheckInNotNull(request.getCommonData().getCheckInTime());

        validateTheNumberOfJurorsToUpdate(request);

        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();

        List<Appearance> checkedInJurors = new ArrayList<>();
        request.getJuror().forEach(jurorNumber -> {
            validateJuror(owner, jurorNumber);

            // retrieve the existing Appearance record
            Appearance appearance = retrieveExistingAppearanceDetails(commonData.getLocationCode(), jurorNumber,
                commonData.getAttendanceDate());

            // validate and update the relevant values in the Appearance entity based on the attendance status
            appearance.setTimeIn(commonData.getCheckInTime());
            appearance.setAppearanceStage(AppearanceStage.CHECKED_IN);
            realignAttendanceType(appearance);
            checkedInJurors.add(appearance);
        });

        if (!checkedInJurors.isEmpty()) {
            appearanceRepository.saveAllAndFlush(checkedInJurors);
        }

        // build and return a summary
        AttendanceDetailsResponse response = new AttendanceDetailsResponse();
        AttendanceDetailsResponse.Summary summary = AttendanceDetailsResponse.Summary.builder()
            .checkedIn(request.getJuror().size())
            .build();
        response.setSummary(summary);

        return response;
    }

    private AttendanceDetailsResponse updateCheckOut(UpdateAttendanceDto request, String owner) {
        validateTheNumberOfJurorsToUpdate(request);

        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();
        final String locCode = commonData.getLocationCode();
        final LocalDate appearanceDate = commonData.getAttendanceDate();

        List<AttendanceDetailsResponse.Details> panelledJurors = new ArrayList<>();
        List<Appearance> checkedOutJurors = new ArrayList<>();

        request.getJuror().forEach(jurorNumber -> {
            validateJuror(owner, jurorNumber);

            // retrieve the current attendance details
            List<JurorAppearanceResponseDto.JurorAppearanceResponseData> currentAttendanceDetails =
                appearanceRepository.getAppearanceRecords(locCode, appearanceDate, jurorNumber,
                    JurorStatusGroup.ALL);

            // if the status of the juror is panelled, the record is not updated.
            if (currentAttendanceDetails.get(0).getJurorStatus().equals(IJurorStatus.PANEL)) {
                AttendanceDetailsResponse.Details details = AttendanceDetailsResponse.Details.builder()
                    .jurorNumber(jurorNumber)
                    .firstName(currentAttendanceDetails.get(0).getFirstName())
                    .lastName(currentAttendanceDetails.get(0).getLastName())
                    .jurorStatus(currentAttendanceDetails.get(0).getJurorStatus())
                    .build();
                panelledJurors.add(details);
            } else {
                // retrieve the existing Appearance record
                Appearance appearance = retrieveExistingAppearanceDetails(locCode, jurorNumber, appearanceDate);

                // validate and update the relevant values in the Appearance entity based on the attendance status
                validateCheckInNotNull(appearance.getTimeIn());
                validateCheckOutNotBeforeCheckIn(appearance.getTimeIn(), commonData.getCheckOutTime());
                appearance.setTimeOut(commonData.getCheckOutTime());
                appearance.setAppearanceStage(AppearanceStage.CHECKED_OUT);
                realignAttendanceType(appearance);
                checkedOutJurors.add(appearance);
            }
        });

        if (!checkedOutJurors.isEmpty()) {
            appearanceRepository.saveAllAndFlush(checkedOutJurors);
        }

        // build and return a summary
        AttendanceDetailsResponse response = new AttendanceDetailsResponse();
        AttendanceDetailsResponse.Summary summary = AttendanceDetailsResponse.Summary.builder()
            .checkedOut(checkedOutJurors.size())
            .panelled(panelledJurors.size())
            .build();
        response.setSummary(summary);
        response.setDetails(panelledJurors); // add details of panelled jurors to allow the officer to view and confirm

        return response;
    }

    private AttendanceDetailsResponse updateCheckInAndOut(UpdateAttendanceDto request, String owner) {
        validateTheNumberOfJurorsToUpdate(request);

        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();

        List<Appearance> checkInAndOutJurors = new ArrayList<>();
        request.getJuror().forEach(jurorNumber -> {
            validateJuror(owner, jurorNumber);

            // retrieve the existing Appearance record
            Appearance appearance = retrieveExistingAppearanceDetails(commonData.getLocationCode(),
                jurorNumber, commonData.getAttendanceDate());

            // validate and update the relevant values in the Appearance entity based on the attendance status
            validateBothCheckInAndOutTimeNotNull(commonData.getCheckInTime(), commonData.getCheckOutTime());
            validateCheckOutNotBeforeCheckIn(commonData.getCheckInTime(), commonData.getCheckOutTime());
            appearance.setTimeIn(commonData.getCheckInTime());
            appearance.setTimeOut(commonData.getCheckOutTime());
            appearance.setAppearanceStage(AppearanceStage.CHECKED_OUT);
            realignAttendanceType(appearance);
            checkInAndOutJurors.add(appearance);
        });

        if (!checkInAndOutJurors.isEmpty()) {
            appearanceRepository.saveAllAndFlush(checkInAndOutJurors);
        }

        // build and return a summary
        AttendanceDetailsResponse response = new AttendanceDetailsResponse();
        AttendanceDetailsResponse.Summary summary = AttendanceDetailsResponse.Summary.builder()
            .checkedInAndOut(request.getJuror().size())
            .build();
        response.setSummary(summary);

        return response;
    }

    private AttendanceDetailsResponse updateCheckOutPanelled(UpdateAttendanceDto request, String owner) {
        validateTheNumberOfJurorsToUpdate(request);

        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();

        List<Appearance> checkedOutPanelledJurors = new ArrayList<>();
        request.getJuror().forEach(jurorNumber -> {
            validateJuror(owner, jurorNumber);

            Appearance appearance = retrieveExistingAppearanceDetails(commonData.getLocationCode(),
                jurorNumber, commonData.getAttendanceDate());

            // validate and update the relevant values in the Appearance entity based on the attendance status
            validateCheckInNotNull(appearance.getTimeIn());
            validateCheckOutNotBeforeCheckIn(appearance.getTimeIn(), commonData.getCheckOutTime());
            appearance.setTimeOut(commonData.getCheckOutTime());
            appearance.setAppearanceStage(AppearanceStage.CHECKED_OUT);
            realignAttendanceType(appearance);
            checkedOutPanelledJurors.add(appearance);
        });

        if (!checkedOutPanelledJurors.isEmpty()) {
            appearanceRepository.saveAllAndFlush(checkedOutPanelledJurors);
        }

        // build and return a summary
        AttendanceDetailsResponse response = new AttendanceDetailsResponse();
        AttendanceDetailsResponse.Summary summary = AttendanceDetailsResponse.Summary.builder()
            .checkedOut(checkedOutPanelledJurors.size())
            .build();
        response.setSummary(summary);

        return response;
    }

    private void validateJuror(String owner, String jurorNumber) {
        // validate the juror record exists, and user has ownership of the record
        Juror activeJurorRecord = validateOwnership(owner, jurorNumber);

        // check juror status to make sure they can be checked in or out
        validateJurorStatus(activeJurorRecord, true);
    }

    private Juror validateOwnership(String owner, String jurorNumber) {
        // validate the juror record exists, and user has ownership of the record
        Juror activeJurorRecord = getActiveJurorRecord(jurorRepository, jurorNumber);
        checkOwnershipForCurrentUser(activeJurorRecord, owner);

        return activeJurorRecord;
    }

    void validateBothCheckInAndOutTimeNotNull(LocalTime checkInTime, LocalTime checkOutTime) {
        if (checkInTime == null && checkOutTime == null) {
            throw new MojException.BadRequest("Must provide a Check-in or Check-out time",
                null);
        }
    }

    void validateBothCheckInAndOutTimeNotSet(LocalTime checkInTime, LocalTime checkOutTime) {
        if (checkInTime != null && checkOutTime != null) {
            throw new MojException.BadRequest("Cannot have both Check-in and Check-out time",
                null);
        }
    }

    void validateCheckInNotNull(LocalTime checkInTime) {
        if (checkInTime == null) {
            throw new MojException.BadRequest("Check-in time cannot be null",
                null);
        }
    }

    void validateCheckOutNotNull(LocalTime checkOutTime) {
        if (checkOutTime == null) {
            throw new MojException.BadRequest("Check-out time cannot be null",
                null);
        }
    }

    private void validateCheckOutNotBeforeCheckIn(LocalTime checkInTime, LocalTime checkOutTime) {
        if (checkOutTime.isBefore(checkInTime)) {
            throw new MojException.BadRequest("Check-out time cannot be before check-in",
                null);
        }
    }

    private JurorPool validateJurorStatus(Juror juror, boolean isCompleted) {
        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(juror.getJurorNumber());
        validateJurorStatus(jurorPool.getStatus().getStatus(), isCompleted);
        return jurorPool;
    }

    private void validateJurorStatus(int status, boolean isCompleted) {
        if (status != IJurorStatus.RESPONDED && status != IJurorStatus.PANEL && status != IJurorStatus.JUROR
            && !(status == IJurorStatus.COMPLETED && isCompleted)) {
            throw new MojException.BadRequest("Cannot check in or out a juror with an invalid status",
                null);
        }
    }

    private void validateTheNumberOfJurorsToUpdate(UpdateAttendanceDto request) {
        UpdateAttendanceDto.CommonData commonData = request.getCommonData();

        if (commonData.getSingleJuror().equals(Boolean.TRUE) && request.getJuror().size() > 1) {
            throw new MojException.BadRequest("Multiple jurors not allowed for single record "
                + "update", null);
        }

        if (isEmptyOrNull(request.getJuror())) {
            // retrieve all jurors to update based on query
            request.setJuror(retrieveJurorsToUpdate(commonData, mapUpdateStatusToRetrieveTag(commonData.getStatus())));
        }

        // if the above conditions are not met, the invoking method will update/delete all the jurors in the list
    }

    private AttendanceDetailsResponse buildAttendanceDetailsResponse(List<Tuple> tuples) {
        List<AttendanceDetailsResponse.Details> attendanceDetails = new ArrayList<>();

        tuples.forEach(tuple -> {
            AttendanceDetailsResponse.Details details = AttendanceDetailsResponse.Details.builder()
                .jurorNumber(tuple.get(0, String.class))
                .firstName(tuple.get(1, String.class))
                .lastName(tuple.get(2, String.class))
                .jurorStatus(tuple.get(3, Integer.class))
                .checkInTime(tuple.get(4, LocalTime.class))
                .checkOutTime(tuple.get(5, LocalTime.class))
                .isNoShow(tuple.get(6, Boolean.class))
                .appearanceStage(tuple.get(7, AppearanceStage.class))
                .build();
            attendanceDetails.add(details);
        });

        return AttendanceDetailsResponse.builder().details(attendanceDetails).build();
    }

    private List<AttendanceDetailsResponse.Details> buildAbsentResponse(List<Tuple> tuples) {
        List<AttendanceDetailsResponse.Details> attendanceDetails = new ArrayList<>();

        tuples.forEach(tuple -> {
            AttendanceDetailsResponse.Details details = AttendanceDetailsResponse.Details.builder()
                .jurorNumber(tuple.get(0, String.class))
                .firstName(tuple.get(1, String.class))
                .lastName(tuple.get(2, String.class))
                .jurorStatus(tuple.get(3, Integer.class))
                .build();
            attendanceDetails.add(details);
        });

        return attendanceDetails;
    }

    private RetrieveAttendanceDetailsTag mapUpdateStatusToRetrieveTag(UpdateAttendanceStatus status) {
        return switch (status) {
            case CHECK_OUT -> RetrieveAttendanceDetailsTag.NOT_CHECKED_OUT;
            case CHECK_OUT_PANELLED -> RetrieveAttendanceDetailsTag.PANELLED;
            case CHECK_IN -> RetrieveAttendanceDetailsTag.NOT_CHECKED_IN;
            default -> null;
        };
    }

    @Override
    public Optional<Appearance> getAppearance(String jurorNumber,
                                              LocalDate appearanceDate,
                                              String locCode) {
        return appearanceRepository.findByLocCodeAndJurorNumberAndAttendanceDate(
            locCode, jurorNumber, appearanceDate);
    }

    @Override
    public void realignAttendanceType(Appearance appearance) {
        appearanceRepository.save(appearance);
        realignAttendanceType(appearance.getJurorNumber());
    }

    @Override
    public void realignAttendanceType(String jurorNumber) {
        List<Appearance> appearances = getAllAppearances(jurorNumber);
        List<LocalDate> localDates = appearances
            .stream()
            .map(Appearance::getAttendanceDate)
            .distinct()
            .sorted(Comparator.naturalOrder())
            .toList();

        appearances
            .forEach(appearance1 -> realignAttendanceTypeInternal(appearance1,
                isLongTrialDay(localDates, appearance1.getAttendanceDate())));

        appearanceRepository.saveAll(appearances);
    }

    @Override
    public UnconfirmedJurorResponseDto retrieveUnconfirmedJurors(String locationCode, LocalDate attendanceDate) {
        log.info("Retrieving unconfirmed jurors for location {} on date {}", locationCode, attendanceDate);

        //confirm user has access to location
        SecurityUtil.validateIsLocCode(locationCode);

        LocalDate today = LocalDate.now();
        // check if the day is not confirmed, e.g. less than 7 days before today
        if (today.minusDays(7).isBefore(attendanceDate)) {
            log.error("Attendance for location {} on date {} is not confirmed", locationCode, attendanceDate);
            // throw bad request exception
            throw new MojException.BadRequest("Attendance for location " + locationCode + " on date "
                + attendanceDate + " is not confirmed", null);
        }

        List<Tuple> unconfirmedJurors = appearanceRepository.getUnconfirmedJurors(locationCode, attendanceDate);

        if (unconfirmedJurors.isEmpty()) {
            log.info("No unconfirmed jurors found for location {} on date {}", locationCode, attendanceDate);
            return new UnconfirmedJurorResponseDto(new ArrayList<>());
        }

        log.info("Unconfirmed jurors found for location {} on date {}", locationCode, attendanceDate);

        List<UnconfirmedJurorDataDto> unconfirmedJurorDataList = new ArrayList<>();

        unconfirmedJurors.forEach(tuple -> {
            UnconfirmedJurorDataDto unconfirmedJurorData =
                UnconfirmedJurorDataDto.builder()
                    .jurorNumber(tuple.get(0, String.class))
                    .firstName(tuple.get(1, String.class))
                    .lastName(tuple.get(2, String.class))
                    .status(JurorStatusEnum.fromStatus(tuple.get(3, Integer.class)))
                    .checkInTime(tuple.get(4, LocalTime.class))
                    .checkOutTime(tuple.get(5, LocalTime.class))
                    .build();
            unconfirmedJurorDataList.add(unconfirmedJurorData);
        });

        return new UnconfirmedJurorResponseDto(unconfirmedJurorDataList);
    }

    @Override
    @Transactional
    public void confirmAttendance(ConfirmAttendanceDto request) {
        log.info("Confirming attendance for juror {} on date {}", request.getJurorNumber(),
            request.getAttendanceDate());

        final String owner = SecurityUtil.getActiveOwner();
        final String locCode = request.getLocationCode();
        final String jurorNumber = request.getJurorNumber();

        CourtLocation courtLocation =
            courtLocationRepository.findByLocCode(locCode)
                .orElseThrow(() -> new MojException.NotFound("Court location not found", null));

        // validate the juror record exists and user has ownership of the record
        validateJuror(owner, jurorNumber);

        // check if juror has been empanelled
        if (panelService.isEmpanelledJuror(jurorNumber, locCode, request.getAttendanceDate())) {
            throw new MojException.BusinessRuleViolation("Juror has been empanelled: " + jurorNumber,
                                                         CANNOT_PROCESS_EMPANELLED_JUROR);
        }

        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPool(jurorPoolRepository, jurorNumber,
            courtLocation);

        // get the juror appearance record if it exists
        Appearance appearance = getAppearance(jurorNumber, request.getAttendanceDate(), locCode)
            .orElseGet(() -> appearanceRepository.save(
                appearanceCreationService.createAppearance(
                    jurorNumber,
                    request.getAttendanceDate(),
                    courtLocation,
                    jurorPool.getPool().getPoolNumber(),
                    false
                )));

        if (appearance.getAppearanceStage() != null
            && AppearanceStage.getConfirmedAppearanceStages().contains(appearance.getAppearanceStage())) {
            throw new MojException.BusinessRuleViolation(
                "Juror " + jurorNumber + " has already confirmed their attendance for date"
                    + " " + request.getAttendanceDate(),
                MojException.BusinessRuleViolation.ErrorCode.DAY_ALREADY_CONFIRMED);
        }

        String juryAttendancePrefix;
        if (appearance.getTrialNumber() != null) {
            juryAttendancePrefix = "J";
            appearance.setSatOnJury(true);
        } else {
            juryAttendancePrefix = "P";
        }

        appearance.setAttendanceAuditNumber(getAttendanceAuditNumber(juryAttendancePrefix));

        appearance.setTimeIn(request.getCheckInTime());
        appearance.setTimeOut(request.getCheckOutTime());

        appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
        realignAttendanceType(appearance);
        appearance.setAppearanceConfirmed(Boolean.TRUE);

        appearanceRepository.saveAndFlush(appearance);
        jurorExpenseService.applyDefaultExpenses(appearance, jurorPool.getJuror());

        // update the juror next date and clear on call flag in case it is set
        jurorPool.setNextDate(request.getAttendanceDate());
        jurorPool.setOnCall(false);
        jurorPoolRepository.saveAndFlush(jurorPool);

        log.info("Attendance confirmed for juror {} on date {}", jurorNumber, request.getAttendanceDate());
    }


    private void realignAttendanceTypeInternal(Appearance appearance, boolean isLongTrialDay) {
        if ((!Boolean.TRUE.equals(appearance.getNonAttendanceDay())
            && (appearance.getTimeIn() == null || appearance.getTimeOut() == null))
            || Boolean.TRUE.equals(appearance.getNoShow())
            || AttendanceType.ABSENT.equals(appearance.getAttendanceType())) {
            return;
        }

        if (appearance.getAttendanceType() != null
            && Set.of(AttendanceType.NON_ATTENDANCE, AttendanceType.NON_ATTENDANCE_LONG_TRIAL)
            .contains(appearance.getAttendanceType())) {
            appearance.setAttendanceType(isLongTrialDay
                ? AttendanceType.NON_ATTENDANCE_LONG_TRIAL : AttendanceType.NON_ATTENDANCE
            );
        } else if (appearance.isFullDay()) {
            appearance.setAttendanceType(isLongTrialDay
                ? AttendanceType.FULL_DAY_LONG_TRIAL : AttendanceType.FULL_DAY
            );
        } else {
            appearance.setAttendanceType(isLongTrialDay
                ? AttendanceType.HALF_DAY_LONG_TRIAL : AttendanceType.HALF_DAY);
        }
    }

    List<Appearance> getAllAppearances(String jurorNumber) {
        return appearanceRepository.findAllByJurorNumber(jurorNumber);
    }

    boolean isLongTrialDay(List<LocalDate> appearanceDates, LocalDate dateToCheck) {
        return appearanceDates.indexOf(dateToCheck) >= 10;
    }
}
