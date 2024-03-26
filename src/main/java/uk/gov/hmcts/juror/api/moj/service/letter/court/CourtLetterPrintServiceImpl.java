package uk.gov.hmcts.juror.api.moj.service.letter.court;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.querydsl.core.Tuple;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QWelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CertificateOfExemptionRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.SystemParameterRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.CourtPrintLetterRepository;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.CERTIFICATE_OF_EXEMPTION;
import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.DEFERRAL_GRANTED;

@SuppressWarnings("PMD.LawOfDemeter")
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtLetterPrintServiceImpl implements CourtLetterPrintService {

    @NonNull
    private final SystemParameterRepositoryMod systemParameterRepository;

    @NonNull
    private final JurorRepository jurorRepository;

    @NonNull
    private final WelshCourtLocationRepository welshCourtLocationRepository;

    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;

    @NonNull
    private final CourtPrintLetterRepository courtPrintLetterRepository;


    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QWelshCourtLocation WELSH_COURT_LOCATION = QWelshCourtLocation.welshCourtLocation;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QJuror JUROR = QJuror.juror;
    private static final QAppearance APPEARANCE = QAppearance.appearance;

    private static final QTrial TRIAL = QTrial.trial;

    //System parameter setting for english and welsh url's
    private static final int ENGLISH_URL_PARAM = 102;
    private static final int WELSH_URL_PARAM = 103;

    private static final String ROYAL_COURTS_OF_JUSTICE = "626";
    private static final String NEW_LINE = "\n";

    @Override
    public List<PrintLetterDataResponseDto> getPrintLettersData(PrintLettersRequestDto printLettersRequestDto,
                                                                String login) {
        Multimap<String, LocalDate> lettersDataMap = transposeListToMap(printLettersRequestDto);

        List<PrintLetterDataResponseDto> letters = new ArrayList<>();

        String owner = SecurityUtil.getActiveOwner();

        for (Map.Entry<String, LocalDate> entry : lettersDataMap.entries()) {
            String jurorNumber = entry.getKey();

            Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
            if (juror == null) {
                log.error("Cannot find juror number " + jurorNumber);
                throw new MojException.NotFound("Cannot find juror number: " + jurorNumber, null);
            }

            JurorUtils.checkOwnershipForCurrentUser(juror, owner);

            // retrieve print information
            boolean welsh = BooleanUtils.toBoolean(juror.getWelsh());
            Tuple data;
            if (printLettersRequestDto.getLetterType().equals(CERTIFICATE_OF_EXEMPTION)) {
                CertificateOfExemptionRequestDto exemptionRequestDto =
                    (CertificateOfExemptionRequestDto) printLettersRequestDto;
                data = courtPrintLetterRepository.retrievePrintInformation(jurorNumber,
                    printLettersRequestDto.getLetterType(), welsh, owner,
                    exemptionRequestDto.getTrialNumber());
            } else if (CourtLetterType.SHOW_CAUSE.equals(printLettersRequestDto.getLetterType())
                || CourtLetterType.FAILED_TO_ATTEND.equals(printLettersRequestDto.getLetterType())) {
                data = courtPrintLetterRepository.retrievePrintInformationBasedOnLetterSpecificDate(jurorNumber,
                    printLettersRequestDto.getLetterType(), welsh, owner, entry.getValue());
            } else {
                data = courtPrintLetterRepository.retrievePrintInformation(jurorNumber,
                    printLettersRequestDto.getLetterType(), welsh, owner);
            }

            if (data == null) {
                continue;
            }

            // add history item
            addHistoryItem(printLettersRequestDto, login, jurorNumber, data);

            // create the print letter response
            boolean welshInformation = CourtLocationUtils
                .isWelshCourtLocation(welshCourtLocationRepository,
                    Objects.requireNonNull(data).get(COURT_LOCATION.locCode));

            PrintLetterDataResponseDto dto;
            if (!printLettersRequestDto.getLetterType().equals(CERTIFICATE_OF_EXEMPTION)) {
                dto = createPrintLetterDataResponseDto(data, welshInformation, printLettersRequestDto);
            } else {
                CertificateOfExemptionRequestDto exemptionRequestDto =
                    (CertificateOfExemptionRequestDto) printLettersRequestDto;
                dto = createPrintLetterDataResponseDto(data, welshInformation, exemptionRequestDto);
            }
            letters.add(dto);
        }
        return letters;
    }

    private Multimap<String, LocalDate> transposeListToMap(PrintLettersRequestDto printLettersRequestDto) {
        Multimap<String, LocalDate> map = LinkedListMultimap.create();

        if (CourtLetterType.SHOW_CAUSE.equals(printLettersRequestDto.getLetterType())
            || CourtLetterType.FAILED_TO_ATTEND.equals(printLettersRequestDto.getLetterType())) {
            // can print multiple letters for each juror if absent from jury service on more than one day - need to
            // filter based on the given date, e.g. attendanceDate (absentDate)
            printLettersRequestDto.getDetailsPerLetter()
                .forEach(key -> map.put(key.getJurorNumber(), key.getLetterDate()));
        } else {
            // de-duplicate list of juror numbers, so we only print one letter per juror
            List<String> jurorNumbersList = printLettersRequestDto.getJurorNumbers().stream().distinct().toList();
            jurorNumbersList.forEach(jurorNumber -> map.put(jurorNumber, null));
        }
        return map;
    }

    private void addHistoryItem(PrintLettersRequestDto printLettersRequestDto, String login, String jurorNumber,
                                Tuple data) {
        JurorHistory.JurorHistoryBuilder jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .dateCreated(LocalDateTime.now())
            .createdBy(login)
            .poolNumber(data.get(POOL_REQUEST.poolNumber));

        switch (printLettersRequestDto.getLetterType()) {
            case DEFERRAL_GRANTED, POSTPONED -> {
                jurorHistory.otherInformationRef(data.get(JUROR_POOL.deferralCode));
                jurorHistory.otherInformationDate(data.get(JUROR_POOL.deferralDate));

                if (DEFERRAL_GRANTED.equals(printLettersRequestDto.getLetterType())) {
                    jurorHistory.historyCode(HistoryCodeMod.DEFERRED_LETTER);
                    jurorHistory.otherInformation("Reissued deferral letter");
                } else {
                    jurorHistory.historyCode(HistoryCodeMod.POSTPONED_LETTER);
                    jurorHistory.otherInformation("Reissued postponed letter");
                }
            }
            case EXCUSAL_GRANTED -> {
                jurorHistory.historyCode(HistoryCodeMod.EXCUSED_LETTER);
                jurorHistory.otherInformation("Reissued excusal letter");
                jurorHistory.otherInformationRef(data.get(JUROR.excusalCode));
                jurorHistory.otherInformationDate(data.get(JUROR.excusalDate));
            }
            case DEFERRAL_REFUSED -> {
                jurorHistory.historyCode(HistoryCodeMod.NON_DEFERRED_LETTER);
                jurorHistory.otherInformation("Print Deferral Denied Letter");
                jurorHistory.otherInformationRef(data.get(JUROR_POOL.deferralCode));
            }
            case WITHDRAWAL -> {
                jurorHistory.historyCode(HistoryCodeMod.WITHDRAWAL_LETTER);
                jurorHistory.otherInformation("Reissued withdrawal letter");
                jurorHistory.otherInformationRef(data.get(JUROR.disqualifyCode));
                jurorHistory.otherInformationDate(data.get(JUROR.disqualifyDate));
            }
            case EXCUSAL_REFUSED -> {
                jurorHistory.historyCode(HistoryCodeMod.NON_EXCUSED_LETTER);
                jurorHistory.otherInformation("Reissued excusal denied letter");
                jurorHistory.otherInformationRef(data.get(JUROR_POOL.juror.excusalCode));
                jurorHistory.otherInformationDate(data.get(JUROR_POOL.juror.excusalDate));
            }
            case SHOW_CAUSE -> {
                jurorHistory.historyCode(HistoryCodeMod.SHOW_CAUSE_LETTER);
                jurorHistory.otherInformation(HistoryCodeMod.SHOW_CAUSE_LETTER.getDescription());
            }
            case FAILED_TO_ATTEND -> {
                jurorHistory.historyCode(HistoryCodeMod.FAILED_TO_ATTEND_LETTER);
                jurorHistory.otherInformation(HistoryCodeMod.FAILED_TO_ATTEND_LETTER.getDescription());
            }
            case CERTIFICATE_OF_EXEMPTION -> {
                CertificateOfExemptionRequestDto exemptionRequestDto =
                    (CertificateOfExemptionRequestDto) printLettersRequestDto;
                jurorHistory.historyCode(HistoryCodeMod.CERTIFICATE_OF_EXEMPTION);
                jurorHistory.otherInformation("Print Certificate of Exemption");
                jurorHistory.otherInformationRef(exemptionRequestDto.getExemptionPeriod());
                jurorHistory.otherInformationDate(LocalDate.now());
            }
            case CERTIFICATE_OF_ATTENDANCE -> {
                jurorHistory.historyCode(HistoryCodeMod.CERTIFICATE_OF_RECOGNITION);
                jurorHistory.otherInformation("Certificate of Attendance");
            }
            default -> throw new MojException.NotImplemented("letter type not implemented", null);
        }

        JurorHistory history = jurorHistory.build();
        jurorHistoryRepository.save(history);
    }

    @SuppressWarnings("checkstyle:WhitespaceAround")
    private PrintLetterDataResponseDto createPrintLetterDataResponseDto(Tuple data, boolean welsh,
                                                                        PrintLettersRequestDto dto) {
        PrintLetterDataResponseDto.PrintLetterDataResponseDtoBuilder builder = PrintLetterDataResponseDto.builder();

        if (welsh) {
            String welshUrl = RepositoryUtils.unboxOptionalRecord(systemParameterRepository.findById(WELSH_URL_PARAM),
                Integer.toString(WELSH_URL_PARAM)).getValue();
            builder.courtName(
                formatWelshCourtName(Objects.requireNonNull(data.get(WELSH_COURT_LOCATION.locCourtName))));
            builder.courtAddressLine1(data.get(WELSH_COURT_LOCATION.address1));
            builder.courtAddressLine2(data.get(WELSH_COURT_LOCATION.address2));
            builder.courtAddressLine3(data.get(WELSH_COURT_LOCATION.address3));
            builder.courtAddressLine4(data.get(WELSH_COURT_LOCATION.address4));
            builder.courtAddressLine5(data.get(WELSH_COURT_LOCATION.address5));
            builder.courtAddressLine6(data.get(WELSH_COURT_LOCATION.address6));
            builder.url(welshUrl);
            builder.welsh(Boolean.TRUE);
        } else {
            String englishUrl =
                RepositoryUtils.unboxOptionalRecord(systemParameterRepository.findById(ENGLISH_URL_PARAM),
                    Integer.toString(ENGLISH_URL_PARAM)).getValue();
            builder.courtName(formatEnglishCourt(data.get(COURT_LOCATION.name),
                Objects.requireNonNull(data.get(COURT_LOCATION.locCode))));
            builder.courtAddressLine1(data.get(COURT_LOCATION.address1));
            builder.courtAddressLine2(data.get(COURT_LOCATION.address2));
            builder.courtAddressLine3(data.get(COURT_LOCATION.address3));
            builder.courtAddressLine4(data.get(COURT_LOCATION.address4));
            builder.courtAddressLine5(data.get(COURT_LOCATION.address5));
            builder.courtAddressLine6(data.get(COURT_LOCATION.address6));
            builder.url(englishUrl);
        }

        builder.courtPhoneNumber(data.get(COURT_LOCATION.locPhone));
        builder.signature(formatSignature(data.get(COURT_LOCATION.signatory), data.get(COURT_LOCATION.locCode), welsh));
        builder.courtPostCode(data.get(COURT_LOCATION.postcode));

        builder.jurorFirstName(data.get(JUROR_POOL.juror.firstName));
        builder.jurorLastName(data.get(JUROR_POOL.juror.lastName));
        builder.jurorAddressLine1(data.get(JUROR_POOL.juror.addressLine1));
        builder.jurorAddressLine2(data.get(JUROR_POOL.juror.addressLine2));
        builder.jurorAddressLine3(data.get(JUROR_POOL.juror.addressLine3));
        builder.jurorAddressLine4(data.get(JUROR_POOL.juror.addressLine4));
        builder.jurorAddressLine5(data.get(JUROR_POOL.juror.addressLine5));
        builder.jurorPostcode(data.get(JUROR_POOL.juror.postcode));
        builder.jurorNumber(data.get(JUROR_POOL.juror.jurorNumber));
        LocalDateTime attendanceTime = data.get(POOL_REQUEST.attendTime);
        builder.attendTime(attendanceTime != null
            ? attendanceTime.toLocalTime()
            : null);
        builder.date(formatDate(LocalDate.now(), welsh));

        switch (dto.getLetterType()) {
            case DEFERRAL_GRANTED -> builder
                .deferredToDate(formatDate(Objects.requireNonNull(data.get(JUROR_POOL.deferralDate)), welsh));
            case POSTPONED -> builder
                .postponedToDate(formatDate(Objects.requireNonNull(data.get(JUROR_POOL.deferralDate)), welsh));
            case DEFERRAL_REFUSED, EXCUSAL_REFUSED -> builder.courtManager(formatCourtManager(welsh));
            case EXCUSAL_GRANTED, WITHDRAWAL -> {
            } // do nothing as no additional fields are required }
            case CERTIFICATE_OF_ATTENDANCE -> {
                builder.lossOfEarnings(data.get(APPEARANCE.lossOfEarningsDue))
                    .childCare(data.get(APPEARANCE.childcareDue))
                    .misc(data.get(APPEARANCE.miscAmountDue));
                Boolean nonAttendance = data.get(APPEARANCE.nonAttendanceDay);
                if (nonAttendance == null || nonAttendance.equals(false)) {
                    builder.nonAttendance("Non Attendance");
                }
            }
            case CERTIFICATE_OF_EXEMPTION -> {
                CertificateOfExemptionRequestDto exemptionRequestDto = (CertificateOfExemptionRequestDto) dto;
                if (exemptionRequestDto.getJudge() != null) {
                    builder.judgeName(exemptionRequestDto.getJudge());
                } else {
                    builder.judgeName(data.get(TRIAL.judge.name));
                }
                builder.periodOfExemption(exemptionRequestDto.getExemptionPeriod());
                builder.defendant(data.get(TRIAL.description));
            }
            case SHOW_CAUSE -> builder
                .attendanceDate(formatDate(Objects.requireNonNull(data.get(APPEARANCE.attendanceDate)), welsh))
                .noShowDate(formatDate(Objects.requireNonNull(dto.getShowCauseDate()), welsh))
                .noShowTime(Objects.requireNonNull(dto.getShowCauseTime()));
            case FAILED_TO_ATTEND -> builder
                .attendanceDate(formatDate(Objects.requireNonNull(data.get(APPEARANCE.attendanceDate)), welsh))
                .replyByDate(formatDate(LocalDate.now().plusDays(7), welsh));
            default -> throw new MojException.NotImplemented("letter type not implemented",
                null);
        }

        return builder.build();
    }

    private String formatEnglishCourt(String courtName, String locationCode) {
        if (ROYAL_COURTS_OF_JUSTICE.equalsIgnoreCase(locationCode)) {
            return "The Royal Court\nof Justice";
        }
        return "The Crown Court\nat " + courtName;
    }

    private String formatWelshCourtName(String courtName) {
        String formattedCourtName = "Llys y Goron\n";

        switch (courtName.substring(0, 1).toLowerCase()) {
            case "b", "m" -> formattedCourtName += "ym M" + courtName.substring(1).toLowerCase();
            case "c" -> formattedCourtName += "yng Ngh" + courtName.substring(1).toLowerCase();
            case "d" -> formattedCourtName += "yn N" + courtName.substring(1).toLowerCase();
            case "g" -> formattedCourtName += "yng Ng" + courtName.substring(1).toLowerCase();
            case "p" -> formattedCourtName += "ym Mh" + courtName.substring(1).toLowerCase();
            case "t" -> formattedCourtName += "yn Nh" + courtName.substring(1).toLowerCase();
            default -> formattedCourtName += "yn" + courtName.charAt(0) + courtName.substring(1).toLowerCase();
        }

        return formattedCourtName;
    }

    // todo - refactor this out to reusable utility class for welsh translations and reduce cyclomatic complexity
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private String formatDate(LocalDate date, boolean welsh) {

        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        if (!welsh) {
            return formattedDate;
        }

        switch (date.getMonth()) {
            case JANUARY -> formattedDate = formattedDate.replace("January", "Ionawr");
            case FEBRUARY -> formattedDate = formattedDate.replace("February", "Chwefror");
            case MARCH -> formattedDate = formattedDate.replace("March", "Mawrth");
            case APRIL -> formattedDate = formattedDate.replace("April", "Ebrill");
            case MAY -> formattedDate = formattedDate.replace("May", "Mai");
            case JUNE -> formattedDate = formattedDate.replace("June", "Mehefin");
            case JULY -> formattedDate = formattedDate.replace("July", "Gorffenaf");
            case AUGUST -> formattedDate = formattedDate.replace("August", "Awst");
            case SEPTEMBER -> formattedDate = formattedDate.replace("September", "Medi");
            case OCTOBER -> formattedDate = formattedDate.replace("October", "Hydref");
            case NOVEMBER -> formattedDate = formattedDate.replace("November", "Tachwedd");
            case DECEMBER -> formattedDate = formattedDate.replace("December", "Rhagfyr");
            default -> throw new MojException.InternalServerError(
                "Cannot replace month, something is wrong with the provided date", null);
        }

        return formattedDate;
    }

    private String formatCourtManager(boolean welsh) {
        if (!welsh) {
            return "The Court Manager";
        }
        return "Y Rheolwr Llys";
    }

    private String formatSignature(String signature, String locCode, boolean welsh) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(signature);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);

        if (!welsh) {
            stringBuilder.append("An Officer of the ");
            if (!ROYAL_COURTS_OF_JUSTICE.equalsIgnoreCase(locCode)) {
                stringBuilder.append("Crown ");
            }
            stringBuilder.append("Court");

        } else {
            stringBuilder.append("Swyddog Llys");
        }

        return stringBuilder.toString();
    }
}
