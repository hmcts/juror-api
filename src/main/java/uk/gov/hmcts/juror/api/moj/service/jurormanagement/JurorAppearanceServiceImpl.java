package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.isEmptyOrNull;
import static uk.gov.hmcts.juror.api.moj.utils.JurorUtils.checkOwnershipForCurrentUser;
import static uk.gov.hmcts.juror.api.moj.utils.JurorUtils.getActiveJurorRecord;
import static uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils.unboxOptionalRecord;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.GodClass"})
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class JurorAppearanceServiceImpl implements JurorAppearanceService {
    private final JurorPoolRepository jurorPoolRepository;
    private final AppearanceRepository appearanceRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;

    @Override
    @Transactional
    public JurorAppearanceResponseDto.JurorAppearanceResponseData processAppearance(
        BureauJWTPayload payload, JurorAppearanceDto jurorAppearanceDto) {

        final String jurorNumber = jurorAppearanceDto.getJurorNumber();
        final String locCode = jurorAppearanceDto.getLocationCode();
        final LocalDate appearanceDate = jurorAppearanceDto.getAttendanceDate();
        final AppearanceStage appearanceStage = jurorAppearanceDto.getAppearanceStage();

        // validate the request to ensure the time and appearance stage combination are valid
        validateTimeAndAppearanceStage(jurorAppearanceDto.getCheckInTime(), jurorAppearanceDto.getCheckOutTime(),
            appearanceStage);

        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode, payload.getOwner(),
            courtLocationRepository);

        // validate the juror record exists and user has ownership of the record
        Juror juror = getActiveJurorRecord(jurorRepository, jurorNumber);
        checkOwnershipForCurrentUser(juror, payload.getOwner());

        // check juror status to make sure they can be checked in
        final JurorPool jurorPool = validateJurorStatus(juror);

        Appearance appearance = appearanceRepository.findByJurorNumberAndAttendanceDate(jurorNumber,
            appearanceDate);

        if (appearance == null) {
            appearance = Appearance.builder()
                .jurorNumber(jurorNumber)
                .attendanceDate(appearanceDate)
                .courtLocation(courtLocation)
                .build();
        } else {
            // validate the current record and the new appearance stage
            validateAppearanceStage(jurorNumber, appearanceStage, appearance);
        }

        if (appearanceStage == AppearanceStage.CHECKED_IN) {
            appearance.setTimeIn(jurorAppearanceDto.getCheckInTime());
        }

        if (appearanceStage == AppearanceStage.CHECKED_OUT) {
            appearance.setTimeOut(jurorAppearanceDto.getCheckOutTime());
        }

        appearance.setAppearanceStage(appearanceStage);
        appearanceRepository.saveAndFlush(appearance);

        // update the juror next date and clear on call flag in case it is set
        jurorPool.setNextDate(appearanceDate);
        jurorPool.setOnCall(false);
        jurorPoolRepository.saveAndFlush(jurorPool);

        // now read the data back and send to front end
        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList =
            appearanceRepository.getAppearanceRecords(locCode, appearanceDate, jurorNumber);

        if (appearanceDataList.size() != 1) {
            throw new MojException.InternalServerError("Error checking in juror " + jurorNumber, null);
        }

        return appearanceDataList.get(0);
    }

    private static void validateAppearanceStage(String jurorNumber, AppearanceStage appearanceStage,
                                                Appearance appearance) {
        if (appearance.getAppearanceStage().equals(AppearanceStage.CHECKED_IN) && appearanceStage.equals(
            AppearanceStage.CHECKED_IN)) {
            throw new MojException.BadRequest("Juror " + jurorNumber + " has already checked in", null);
        } else if (appearance.getAppearanceStage().equals(AppearanceStage.CHECKED_OUT)) {
            throw new MojException.BadRequest("Juror " + jurorNumber + " has already checked out", null);
        } else if (appearance.getAppearanceStage().equals(AppearanceStage.APPEARANCE_CONFIRMED)) {
            throw new MojException.BadRequest("Juror " + jurorNumber + " has already confirmed their attendance", null);
        }
    }

    private void validateTimeAndAppearanceStage(LocalTime checkInTime, LocalTime checkOutTime,
                                                AppearanceStage appearanceStage) {
        if (appearanceStage == AppearanceStage.CHECKED_IN) {
            validateCheckInNotNull(checkInTime);
        } else if (appearanceStage == AppearanceStage.CHECKED_OUT) {
            validateCheckOutNotNull(checkOutTime);
        } else {
            throw new MojException.BadRequest("Invalid appearance stage and time combination supplied", null);
        }

        validateBothCheckInAndOutTimeNotNull(checkInTime, checkOutTime);
        validateBothCheckInAndOutTimeNotSet(checkInTime, checkOutTime);
    }

    @Override
    public JurorAppearanceResponseDto getAppearanceRecords(String locCode, LocalDate date,
                                                           BureauJWTPayload payload) {

        CourtLocationUtils.validateAccessToCourtLocation(locCode, payload.getOwner(), courtLocationRepository);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList =
            appearanceRepository.getAppearanceRecords(locCode, date, null);

        return new JurorAppearanceResponseDto(appearanceDataList);
    }

    @Override
    public boolean hasAppearances(String jurorNumber) {
        return appearanceRepository.countByJurorNumber(jurorNumber) > 0;
    }

    @Override
    public AttendanceDetailsResponse retrieveAttendanceDetails(BureauJWTPayload payload,
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
    public AttendanceDetailsResponse updateAttendance(BureauJWTPayload payload, UpdateAttendanceDto request) {
        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();
        final UpdateAttendanceStatus status = commonData.getStatus();

        // confirm-attendance
        if (status.equals(UpdateAttendanceStatus.CONFIRM_ATTENDANCE)) {
            return updateConfirmAttendance(commonData);
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
    public AttendanceDetailsResponse deleteAttendance(BureauJWTPayload payload, UpdateAttendanceDto request) {
        final UpdateAttendanceDto.CommonData commonData = request.getCommonData();

        if (request.getJuror().size() > 1 ^ commonData.getSingleJuror().equals(Boolean.FALSE)) {
            throw new MojException.BadRequest("Cannot delete multiple juror attendance records",
                null);
        }

        if (!commonData.getStatus().equals(UpdateAttendanceStatus.DELETE)) {
            throw new MojException.BadRequest("Cannot delete attendance records for status "
                + commonData.getStatus(), null);
        }

        // ensure only a single attendance record is deleted per api call
        validateTheNumberOfJurorsToUpdate(request);

        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(commonData.getLocationCode(),
            payload.getOwner(), courtLocationRepository);

        // validate the juror record exists and user has ownership of the record
        String jurorNumber = request.getJuror().get(0);
        validateJuror(payload.getOwner(), jurorNumber);

        // build the juror id
        AppearanceId appearanceId = new AppearanceId(jurorNumber, commonData.getAttendanceDate(), courtLocation);

        AttendanceDetailsResponse.Summary summary;
        Optional<Appearance> appearance = appearanceRepository.findById(appearanceId);
        if (appearance.isPresent()) {
            appearanceRepository.deleteById(appearanceId);
            summary = AttendanceDetailsResponse.Summary.builder().deleted(1).build();
        } else {
            summary = AttendanceDetailsResponse.Summary.builder()
                .deleted(0)
                .additionalInformation("No attendance record found for juror number " + jurorNumber)
                .build();
        }

        // build and return a summary
        AttendanceDetailsResponse response = new AttendanceDetailsResponse();
        response.setSummary(summary);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public JurorsToDismissResponseDto retrieveJurorsToDismiss(JurorsToDismissRequestDto request) {

        BureauJWTPayload payload = SecurityUtil.getActiveUsersBureauPayload();
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

        if (request.getIncludeOnCall()) {
            List<JurorPool> jurorPoolOnCall =
                jurorPoolRepository.findJurorsOnCallAtCourtLocation(request.getLocationCode(),
                    request.getPoolNumbers());
            jurorPools.addAll(jurorPoolOnCall);
        }

        if (request.getIncludeNotInAttendance()) {
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
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);

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

    private AttendanceDetailsResponse updateConfirmAttendance(UpdateAttendanceDto.CommonData updateCommonData) {
        // 1. retrieve details of jurors who attended court for the given attendance date (checked-in)
        RetrieveAttendanceDetailsDto.CommonData retrieveCommonData = new RetrieveAttendanceDetailsDto.CommonData();
        retrieveCommonData.setAttendanceDate(updateCommonData.getAttendanceDate());
        retrieveCommonData.setLocationCode(updateCommonData.getLocationCode());
        retrieveCommonData.setTag(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE);

        RetrieveAttendanceDetailsDto request = RetrieveAttendanceDetailsDto.builder()
            .commonData(retrieveCommonData)
            .build();

        List<Tuple> checkedInJurors = appearanceRepository.retrieveAttendanceDetails(request);

        // 2. checked-in jurors - update and save records
        List<AppearanceId> appearanceIds = new ArrayList<>();

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(updateCommonData.getLocationCode());

        checkedInJurors.forEach(tuple -> {
            // build array of appearanceIds
            AppearanceId appearanceId = new AppearanceId(tuple.get(0, String.class),
                updateCommonData.getAttendanceDate(),
                courtLocation);
            appearanceIds.add(appearanceId);
        });

        List<Appearance> checkedInAttendances = appearanceRepository.findAllById(appearanceIds);
        checkedInAttendances.forEach(appearance -> appearance.setAppearanceStage(AppearanceStage.APPEARANCE_CONFIRMED));

        appearanceRepository.saveAllAndFlush(checkedInAttendances);

        // 3. retrieve details of jurors who failed to attend/non-attendance (no show)
        List<Tuple> absentTuples = appearanceRepository.retrieveNonAttendanceDetails(retrieveCommonData);

        // 4. absent jurors - build new appearance record with minimal data
        List<Appearance> absentJurors = new ArrayList<>();
        absentTuples.forEach(tuple -> {
            Appearance appearance = Appearance.builder()
                .jurorNumber(tuple.get(0, String.class))
                .attendanceDate(updateCommonData.getAttendanceDate())
                .courtLocation(courtLocation)
                .noShow(Boolean.TRUE)
                .build();
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
                appearanceRepository.getAppearanceRecords(locCode, appearanceDate, jurorNumber);

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
        Juror activeJurorRecord = getActiveJurorRecord(jurorRepository, jurorNumber);
        checkOwnershipForCurrentUser(activeJurorRecord, owner);

        // check juror status to make sure they can be checked in or out
        validateJurorStatus(activeJurorRecord);
    }

    private void validateBothCheckInAndOutTimeNotNull(LocalTime checkInTime, LocalTime checkOutTime) {
        if (checkInTime == null && checkOutTime == null) {
            throw new MojException.BadRequest("Must provide a Check-in or Check-out time",
                null);
        }
    }

    private void validateBothCheckInAndOutTimeNotSet(LocalTime checkInTime, LocalTime checkOutTime) {
        if (checkInTime != null && checkOutTime != null) {
            throw new MojException.BadRequest("Cannot have both Check-in and Check-out time",
                null);
        }
    }

    private void validateCheckInNotNull(LocalTime checkInTime) {
        if (checkInTime == null) {
            throw new MojException.BadRequest("Check-in time cannot be null",
                null);
        }
    }

    private void validateCheckOutNotNull(LocalTime checkOutTime) {
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

    private JurorPool validateJurorStatus(Juror juror) {
        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository,
            juror.getJurorNumber());

        final int status = jurorPool.getStatus().getStatus();

        if (status != IJurorStatus.RESPONDED && status != IJurorStatus.PANEL && status != IJurorStatus.JUROR) {
            throw new MojException.BadRequest("Cannot check in or out a juror with an invalid status",
                null);
        }
        return jurorPool;
    }

    private void validateTheNumberOfJurorsToUpdate(UpdateAttendanceDto request) {
        UpdateAttendanceDto.CommonData commonData = request.getCommonData();

        if (commonData.getSingleJuror().equals(Boolean.TRUE) && (request.getJuror().size() > 1)) {
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
}
