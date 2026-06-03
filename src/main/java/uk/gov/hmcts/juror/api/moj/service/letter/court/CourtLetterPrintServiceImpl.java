package uk.gov.hmcts.juror.api.moj.service.letter.court;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QWelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CertificateOfExemptionRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
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
import uk.gov.hmcts.juror.api.moj.repository.letter.court.CertificateOfAttendanceListRepository;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.CERTIFICATE_OF_EXEMPTION;
import static uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType.DEFERRAL_GRANTED;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings({"PMD.GodClass", "PMD.ExcessiveImports"})
public class CourtLetterPrintServiceImpl implements CourtLetterPrintService {

    private final SystemParameterRepositoryMod systemParameterRepository;
    private final JurorRepository jurorRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
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
    private final CertificateOfAttendanceListRepository certificateOfAttendanceListRepository;

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public List<PrintLetterDataResponseDto> getPrintLettersData(PrintLettersRequestDto printLettersRequestDto,
                                                                String login) {
        Multimap<String, LocalDate> lettersDataMap = transposeListToMap(printLettersRequestDto);

        List<PrintLetterDataResponseDto> letters = new ArrayList<>();

        String owner = SecurityUtil.getActiveOwner();

        for (Map.Entry<String, LocalDate> entry : lettersDataMap.entries()) {
            String jurorNumber = entry.getKey();

            Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
            if (juror == null) {
                log.error("Cannot find juror number {}", jurorNumber);
                throw new MojException.NotFound("Cannot find juror number: " + jurorNumber, null);
            }

            JurorUtils.checkOwnershipForCurrentUser(juror, owner);

            // retrieve print information
            boolean welsh = BooleanUtils.toBoolean(juror.getWelsh())
                && CourtLocationUtils.isWelshCourtLocation(welshCourtLocationRepository, SecurityUtil.getLocCode());
            Tuple data;
            if (printLettersRequestDto.getLetterType() == CERTIFICATE_OF_EXEMPTION) {
                CertificateOfExemptionRequestDto exemptionRequestDto =
                    (CertificateOfExemptionRequestDto) printLettersRequestDto;
                data = courtPrintLetterRepository.retrievePrintInformation(jurorNumber,
                    printLettersRequestDto.getLetterType(), welsh, owner,
                    exemptionRequestDto.getTrialNumber());
            } else if (printLettersRequestDto.getLetterType() == CourtLetterType.SHOW_CAUSE
                || printLettersRequestDto.getLetterType() == CourtLetterType.FAILED_TO_ATTEND) {
                data = courtPrintLetterRepository.retrievePrintInformationBasedOnLetterSpecificDate(jurorNumber,
                    printLettersRequestDto.getLetterType(), welsh, owner, entry.getValue());
            } else {
                data = courtPrintLetterRepository.retrievePrintInformation(jurorNumber,
                    printLettersRequestDto.getLetterType(), welsh, owner);
            }

            if (data == null) {
                continue;
            }

            // create the print letter response
            PrintLetterDataResponseDto dto;
            if (printLettersRequestDto.getLetterType() == CERTIFICATE_OF_EXEMPTION) {
                CertificateOfExemptionRequestDto exemptionRequestDto =
                    (CertificateOfExemptionRequestDto) printLettersRequestDto;
                dto = createPrintLetterDataResponseDto(data, welsh, exemptionRequestDto);
            } else {
                dto = createPrintLetterDataResponseDto(data, welsh, printLettersRequestDto);
            }
            letters.add(dto);
            // add history item
            addHistoryItem(printLettersRequestDto, login, jurorNumber, data);

        }

        if (letters.isEmpty()) {
            throw new MojException.BadRequest("Cannot generate letter data for juror(s):\n"
                + StringUtils.collectionToDelimitedString(printLettersRequestDto.getJurorNumbers(), "\n"), null);
        }

        return letters;
    }

    private Multimap<String, LocalDate> transposeListToMap(PrintLettersRequestDto printLettersRequestDto) {
        Multimap<String, LocalDate> map = LinkedListMultimap.create();

        if (printLettersRequestDto.getLetterType() == CourtLetterType.SHOW_CAUSE
            || printLettersRequestDto.getLetterType() == CourtLetterType.FAILED_TO_ATTEND) {
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

    @SuppressWarnings("PMD.CyclomaticComplexity")
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

                if (printLettersRequestDto.getLetterType() == DEFERRAL_GRANTED) {
                    jurorHistory.historyCode(HistoryCodeMod.DEFERRED_LETTER);
                    jurorHistory.otherInformation("Print deferral letter");
                } else {
                    jurorHistory.historyCode(HistoryCodeMod.POSTPONED_LETTER);
                    jurorHistory.otherInformation("Print postponed letter");
                }
            }
            case EXCUSAL_GRANTED -> {
                jurorHistory.historyCode(HistoryCodeMod.EXCUSED_LETTER);
                jurorHistory.otherInformation("Print excusal letter");
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
                jurorHistory.otherInformation("Print withdrawal letter");
                jurorHistory.otherInformationRef(data.get(JUROR.disqualifyCode));
                jurorHistory.otherInformationDate(data.get(JUROR.disqualifyDate));
            }
            case EXCUSAL_REFUSED -> {
                jurorHistory.historyCode(HistoryCodeMod.NON_EXCUSED_LETTER);
                jurorHistory.otherInformation("Print excusal denied letter");
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
        }

        JurorHistory history = jurorHistory.build();
        jurorHistoryRepository.save(history);
    }

    @SuppressWarnings({"checkstyle:WhitespaceAround", "PMD.CyclomaticComplexity", "PMD.NcssCount"})
    private PrintLetterDataResponseDto createPrintLetterDataResponseDto(Tuple data, boolean welsh,
                                                                        PrintLettersRequestDto dto) {
        PrintLetterDataResponseDto.PrintLetterDataResponseDtoBuilder builder = PrintLetterDataResponseDto.builder();

        if (welsh) {
            String welshUrl = RepositoryUtils.unboxOptionalRecord(systemParameterRepository.findById(WELSH_URL_PARAM),
                Integer.toString(WELSH_URL_PARAM)).getValue();
            builder.courtName(
                formatWelshCourtName(data.get(WELSH_COURT_LOCATION.locCourtName)));
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
            : data.get(COURT_LOCATION.courtAttendTime));
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
                List<PrintLetterDataResponseDto.AttendanceData> attendanceDataList = new ArrayList<>();

                //run query to return appearance list
                List<Appearance> appearanceList = certificateOfAttendanceListRepository.getAttendances(
                    SecurityUtil.getLocCode(), data.get(JUROR_POOL.juror.jurorNumber));

                appearanceList.sort(Comparator.comparing(Appearance::getAttendanceDate));

                //from appearance list create attendance data
                for (Appearance appearance : appearanceList) {
                    attendanceDataList.add(PrintLetterDataResponseDto.AttendanceData.builder()
                        .nonAttendance(appearance.getNonAttendanceDay())
                        .misc(appearance.getMiscAmountPaid())
                        .lossOfEarnings(appearance.getLossOfEarningsPaid())
                        .childCare(appearance.getChildcarePaid())
                        .attendanceDate(appearance.getAttendanceDate())
                        .build());
                }

                //add attendance data list to response dto
                builder.attendanceDataList(attendanceDataList);

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

        String rest = courtName.substring(1).toLowerCase();

        return "Llys y Goron\n" +
            switch (courtName.substring(0, 1).toLowerCase()) {
                case "b", "m" -> "ym M" + rest;
                case "c" -> "yng Ngh" + rest;
                case "d" -> "yn N" + rest;
                case "g" -> "yng Ng" + rest;
                case "p" -> "ym Mh" + rest;
                case "t" -> "yn Nh" + rest;
                default -> "yn" + courtName.charAt(0) + rest;
            };
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
            case JULY -> formattedDate = formattedDate.replace("July", "Gorffennaf");
            case AUGUST -> formattedDate = formattedDate.replace("August", "Awst");
            case SEPTEMBER -> formattedDate = formattedDate.replace("September", "Medi");
            case OCTOBER -> formattedDate = formattedDate.replace("October", "Hydref");
            case NOVEMBER -> formattedDate = formattedDate.replace("November", "Tachwedd");
            case DECEMBER -> formattedDate = formattedDate.replace("December", "Rhagfyr");
        }

        return formattedDate;
    }

    private String formatCourtManager(boolean welsh) {
        if (!welsh) {
            return "The Court Manager";
        }
        return "Y Rheolwr Llys";
    }

    @SuppressWarnings({"PMD.ConfusingTernary", "PMD.InsufficientStringBufferDeclaration"})
    private String formatSignature(String signature, String locCode, boolean welsh) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(signature)
        .append(NEW_LINE)
        .append(NEW_LINE);

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
