package uk.gov.hmcts.juror.api.moj.xerox;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
public class LetterBase {

    protected final LetterContext letterContext;
    @Getter
    final List<ILetterData> data;
    @Setter
    private FormCode formCode;
    @Getter
    private final String jurorNumber;

    public LetterBase(LetterContext context) {
        this.letterContext = context;
        this.data = new ArrayList<>();
        this.jurorNumber = context.jurorPool.getJuror().getJurorNumber();
        this.setup(context.jurorPool.getJuror());
    }

    protected void setup(Juror juror) {
        if (juror.isWelsh() && ContextType.WELSH_COURT_LOCATION.validate(letterContext)) {
            setupWelsh();
        } else {
            setupEnglish();
        }
    }


    protected void setupWelsh() {
    } // Override me

    protected void setupEnglish() {
    } // Override me

    public String getFormCode() {
        return formCode.getCode();
    }

    public void addData(LetterDataType letterDataType, int length) {
        letterDataType.validateContext(letterContext);
        this.data.add(new LetterData(length, letterDataType));
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void addDataShuffle(DataShuffle... dataShuffle) {
        LetterDataShuffle letterDataShuffle = new LetterDataShuffle();
        for (DataShuffle shuffle : dataShuffle) {
            shuffle.letterDataType.validateContext(letterContext);
            letterDataShuffle.add(new LetterData(shuffle.length, shuffle.letterDataType));
        }
        this.data.add(letterDataShuffle);
    }


    public record DataShuffle(LetterDataType letterDataType, int length) {

    }


    @SuppressWarnings("PMD.AvoidRethrowingException")
    public String getLetterString() {
        try {
            return this.data.stream()
                .map(ILetterData::getFormattedString)
                .collect(Collectors.joining())
                .toUpperCase();
        } catch (MojException mojException) {
            throw mojException;
        } catch (Exception exception) {
            log.error("Failed to generate letter string", exception);
            throw new MojException.InternalServerError("Failed to generate letter string",
                exception);
        }
    }

    private static String dateToWelsh(LocalDate date) {
        SimpleDateFormat hearingDateFormat = new SimpleDateFormat("EEEEEE d MMMM, yyyy");
        String dateString = hearingDateFormat.format(Date.valueOf(date)).toUpperCase();

        String[] dateParts = dateString.split("\\s");

        String welshDay = XeroxConstants.WELSH_DATE_TRANSLATION_MAP.get(dateParts[0]);
        String welshMonth = XeroxConstants.WELSH_DATE_TRANSLATION_MAP
            .get(dateParts[2].substring(0, dateParts[2].length() - 1));

        return format("%s %s %s, %s", welshDay, dateParts[1], welshMonth, dateParts[3]);
    }

    public static String getDateOfLetter() {
        final int processDays = 2;
        final int processDaysOverWeekend = 4;

        Calendar cal = Calendar.getInstance();

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY:
                cal.add(Calendar.DAY_OF_MONTH, processDays);
                break;
            case Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY:
                cal.add(Calendar.DAY_OF_MONTH, processDaysOverWeekend);
                break;
            default:
                throw new MojException.InternalServerError("Unknown day of week", null);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");

        return formatter.format(cal.getTime()).toUpperCase();
    }

    public static String getWelshDateOfLetter() {
        final int processDays = 2;
        final int processDaysOverWeekend = 4;

        Calendar cal = Calendar.getInstance();

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY:
                cal.add(Calendar.DAY_OF_MONTH, processDays);
                break;
            case Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY:
                cal.add(Calendar.DAY_OF_MONTH, processDaysOverWeekend);
                break;
            default:
                throw new MojException.InternalServerError("Unknown day of week", null);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");

        String dateString = formatter.format(cal.getTime()).toUpperCase();
        String[] dateParts = dateString.split("\\s");
        String welshMonth = XeroxConstants.WELSH_DATE_TRANSLATION_MAP
            .get(dateParts[1].substring(0, dateParts[1].length() - 1));

       // return formatter.format(cal.getTime()).toUpperCase();
        return format("%s %s %s", dateParts[0], welshMonth, dateParts[2]);
    }

    @RequiredArgsConstructor
    protected class LetterData implements ILetterData {
        private final int length;
        private final LetterDataType type;


        @Override
        public boolean hasData() {
            return !StringUtils.isBlank(getValue());
        }

        @Override
        public String getValue() {
            String text = Optional.ofNullable(type.getValue(letterContext)).orElse("");
            if (text.length() > length) {
                return text.substring(0, length);
            }
            return text;
        }

        @Override
        public int getLength() {
            return length;
        }
    }


    @EqualsAndHashCode
    protected static class LetterDataShuffle implements ILetterData {
        private final List<ILetterData> letterDataShuffleList = new ArrayList<>();

        @Override
        public boolean hasData() {
            return letterDataShuffleList.stream().anyMatch(ILetterData::hasData);
        }

        @Override
        public String getValue() {
            return letterDataShuffleList
                .stream()
                .filter(ILetterData::hasData)
                .map(ILetterData::getFormattedString)
                .collect(Collectors.joining());
        }

        @Override
        public int getLength() {
            return letterDataShuffleList.stream()
                .mapToInt(data -> data.getFormattedString().length())
                .sum();
        }

        public void add(ILetterData letterData) {
            this.letterDataShuffleList.add(letterData);
        }
    }

    protected interface ILetterData {
        default String getFormattedString() {
            return StringUtils.rightPad(getValue(), getLength(), " ");
        }

        boolean hasData();

        String getValue();

        int getLength();
    }


    public enum LetterDataType {
        DATE_OF_LETTER(context -> getDateOfLetter()),

        WELSH_DATE_OF_LETTER(context -> getWelshDateOfLetter()),
        COURT_LOCATION_CODE(context -> context.getCourtLocation().getLocCode(), ContextType.COURT_LOCATION),
        COURT_NAME(context -> {
            String locCode = context.getCourtLocation().getLocCode();
            if (locCode != null && !List.of("400", "626").contains(locCode)) {
                return "THE CROWN COURT AT " + context.getCourtLocation().getName();
            }
            return context.getCourtLocation().getName();
        }, ContextType.COURT_LOCATION),
        COURT_ADDRESS1(context -> context.getCourtLocation().getAddress1(), ContextType.COURT_LOCATION),
        COURT_ADDRESS2(context -> context.getCourtLocation().getAddress2(), ContextType.COURT_LOCATION),
        COURT_ADDRESS3(context -> context.getCourtLocation().getAddress3(), ContextType.COURT_LOCATION),
        COURT_ADDRESS4(context -> context.getCourtLocation().getAddress4(), ContextType.COURT_LOCATION),
        COURT_ADDRESS5(context -> context.getCourtLocation().getAddress5(), ContextType.COURT_LOCATION),
        COURT_ADDRESS6(context -> context.getCourtLocation().getAddress6(), ContextType.COURT_LOCATION),
        COURT_POSTCODE(context -> context.getCourtLocation().getPostcode(), ContextType.COURT_LOCATION),
        COURT_PHONE(context -> context.getCourtLocation().getLocPhone(), ContextType.COURT_LOCATION),
        COURT_FAX(context ->
            // Fax appears to be deprecated
            // context.getCourtLocation().getCourtFaxNo()
            ""
        ),
        INSERT_INDICATORS(context -> context.getCourtLocation().getInsertIndicators(), ContextType.COURT_LOCATION),
        COURT_SIGNATORY(context -> context.getCourtLocation().getSignatory(), ContextType.COURT_LOCATION),
        BUREAU_NAME(context -> context.getBureauLocation().getName(), ContextType.BUREAU_LOCATION),
        BUREAU_ADDRESS1(context -> context.getBureauLocation().getAddress1(), ContextType.BUREAU_LOCATION),
        BUREAU_ADDRESS2(context -> context.getBureauLocation().getAddress2(), ContextType.BUREAU_LOCATION),
        BUREAU_ADDRESS3(context -> context.getBureauLocation().getAddress3(), ContextType.BUREAU_LOCATION),
        BUREAU_ADDRESS4(context -> context.getBureauLocation().getAddress4(), ContextType.BUREAU_LOCATION),
        BUREAU_ADDRESS5(context -> context.getBureauLocation().getAddress5(), ContextType.BUREAU_LOCATION),
        BUREAU_ADDRESS6(context -> context.getBureauLocation().getAddress6(), ContextType.BUREAU_LOCATION),
        BUREAU_POSTCODE(context -> context.getBureauLocation().getPostcode(), ContextType.BUREAU_LOCATION),
        BUREAU_PHONE(context -> context.getBureauLocation().getLocPhone(), ContextType.BUREAU_LOCATION),
        BUREAU_FAX(context ->
            // Fax appears to be deprecated
            // context.getBureauLocation().getCourtFaxNo()
            ""
        ),
        BUREAU_SIGNATORY(context -> context.getBureauLocation().getSignatory(), ContextType.BUREAU_LOCATION),
        DATE_OF_ATTENDANCE(context ->
            new SimpleDateFormat("EEEEEE d MMMM, yyyy").format(Date.valueOf(context.getJurorPool().getNextDate())),
            ContextType.JUROR_POOL),
        WELSH_DATE_OF_ATTENDANCE(context -> dateToWelsh(context.getJurorPool().getNextDate()), ContextType.JUROR_POOL),
        DEFERRAL_DATE(context ->
            new SimpleDateFormat("EEEEEE d MMMM, yyyy").format(Date.valueOf(context.getJurorPool().getDeferralDate())),
            ContextType.JUROR_POOL),
        WELSH_DEFERRAL_DATE(context -> dateToWelsh(context.getJurorPool().getDeferralDate()), ContextType.JUROR_POOL),
        TIME_OF_ATTENDANCE(context -> DataUtils.asStringHHmmAmPm(context.getCourtLocation().getCourtAttendTime()),
            ContextType.COURT_LOCATION),
        DEFERRAL_TIME(context -> DataUtils.asStringHHmmAmPm(context.getCourtLocation().getCourtAttendTime()),
            ContextType.COURT_LOCATION),
        JUROR_TITLE(context -> context.getJurorPool().getJuror().getTitle(), ContextType.JUROR_POOL),
        JUROR_FIRST_NAME(context -> context.getJurorPool().getJuror().getFirstName(), ContextType.JUROR_POOL),
        JUROR_LAST_NAME(context -> context.getJurorPool().getJuror().getLastName(), ContextType.JUROR_POOL),
        JUROR_ADDRESS1(context -> context.getJurorPool().getJuror().getAddressLine1(), ContextType.JUROR_POOL),
        JUROR_ADDRESS2(context -> context.getJurorPool().getJuror().getAddressLine2(), ContextType.JUROR_POOL),
        JUROR_ADDRESS3(context -> context.getJurorPool().getJuror().getAddressLine3(), ContextType.JUROR_POOL),
        JUROR_ADDRESS4(context -> context.getJurorPool().getJuror().getAddressLine4(), ContextType.JUROR_POOL),
        JUROR_ADDRESS5(context -> context.getJurorPool().getJuror().getAddressLine5(), ContextType.JUROR_POOL),
        JUROR_ADDRESS6(context -> ""),
        JUROR_POSTCODE(context -> context.getJurorPool().getJuror().getPostcode(), ContextType.JUROR_POOL),
        JUROR_NUMBER(context -> context.getJurorPool().getJuror().getJurorNumber(), ContextType.JUROR_POOL),
        POOL_NUMBER(context -> context.getJurorPool().getPoolNumber(), ContextType.JUROR_POOL),
        ADDITIONAL_INFORMATION(LetterContext::getAdditionalInformation, ContextType.ADDITIONAL_INFORMATION),
        WELSH_CORRESPONDENCE_NAME(context -> context.getWelshCourtLocation().getCorrespondenceName(),
            ContextType.WELSH_COURT_LOCATION),
        WELSH_COURT_NAME(context -> context.getWelshCourtLocation().getLocCourtName(),
            ContextType.WELSH_COURT_LOCATION),
        WELSH_COURT_ADDRESS1(context -> context.getWelshCourtLocation().getAddress1(),
            ContextType.WELSH_COURT_LOCATION),
        WELSH_COURT_ADDRESS2(context -> context.getWelshCourtLocation().getAddress2(),
            ContextType.WELSH_COURT_LOCATION),
        WELSH_COURT_ADDRESS3(context -> context.getWelshCourtLocation().getAddress3(),
            ContextType.WELSH_COURT_LOCATION),
        WELSH_COURT_ADDRESS4(context -> context.getWelshCourtLocation().getAddress4(),
            ContextType.WELSH_COURT_LOCATION),
        WELSH_COURT_ADDRESS5(context -> context.getWelshCourtLocation().getAddress5(),
            ContextType.WELSH_COURT_LOCATION),
        WELSH_COURT_ADDRESS6(context -> context.getWelshCourtLocation().getAddress6(),
            ContextType.WELSH_COURT_LOCATION);


        private final Function<LetterContext, String> valueFunction;
        private final ContextType[] contextTypes;

        LetterDataType(Function<LetterContext, String> valueFunction, ContextType... contextTypes) {
            this.valueFunction = valueFunction;
            this.contextTypes = contextTypes;
        }

        public String getValue(LetterContext context) {
            return valueFunction.apply(context);
        }

        public void validateContext(LetterContext context) {
            Arrays.stream(contextTypes).forEach(contextType -> {
                if (!contextType.validate(context)) {
                    throw new MojException.InternalServerError("Letter context validation failed", null);
                }
            });
        }
    }


    protected enum ContextType {
        JUROR_POOL(context -> context.getJurorPool() != null),
        COURT_LOCATION(context -> context.getCourtLocation() != null),
        BUREAU_LOCATION(context -> context.getBureauLocation() != null),
        WELSH_COURT_LOCATION(context -> context.getWelshCourtLocation() != null),
        ADDITIONAL_INFORMATION(context -> context.getAdditionalInformation() != null);

        private final Function<LetterContext, Boolean> validateFunction;

        ContextType(Function<LetterContext, Boolean> validateFunction) {
            this.validateFunction = validateFunction;
        }

        public Boolean validate(LetterContext context) {
            return validateFunction.apply(context);
        }
    }

    @Builder
    @Getter
    public static class LetterContext {
        private final JurorPool jurorPool;
        private final CourtLocation courtLocation;
        private final CourtLocation bureauLocation;
        private final WelshCourtLocation welshCourtLocation;
        private final String additionalInformation;
    }

    protected void addBureauAddress() {
        addDataShuffle(
            new DataShuffle(LetterDataType.BUREAU_ADDRESS1, 35),
            new DataShuffle(LetterDataType.BUREAU_ADDRESS2, 35),
            new DataShuffle(LetterDataType.BUREAU_ADDRESS3, 35),
            new DataShuffle(LetterDataType.BUREAU_ADDRESS4, 35),
            new DataShuffle(LetterDataType.BUREAU_ADDRESS5, 35),
            new DataShuffle(LetterDataType.BUREAU_ADDRESS6, 35),
            new DataShuffle(LetterDataType.BUREAU_POSTCODE, 10)
        );
    }

    protected void addJurorAddress() {
        addData(LetterDataType.JUROR_ADDRESS1, 35);
        addDataShuffle(
            new DataShuffle(LetterDataType.JUROR_ADDRESS2, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS3, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS4, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS5, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS6, 35),
            new DataShuffle(LetterDataType.JUROR_POSTCODE, 10)
        );
    }

    protected void addWelshCourtAddress() {
        addDataShuffle(
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS1, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS2, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS3, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS4, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS5, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS6, 35),
            new DataShuffle(LetterDataType.COURT_POSTCODE, 10)
        );
    }

    protected void addEnglishCourtAddress() {
        addDataShuffle(
            new DataShuffle(LetterDataType.COURT_ADDRESS1, 35),
            new DataShuffle(LetterDataType.COURT_ADDRESS2, 35),
            new DataShuffle(LetterDataType.COURT_ADDRESS3, 35),
            new DataShuffle(LetterDataType.COURT_ADDRESS4, 35),
            new DataShuffle(LetterDataType.COURT_ADDRESS5, 35),
            new DataShuffle(LetterDataType.COURT_ADDRESS6, 35),
            new DataShuffle(LetterDataType.COURT_POSTCODE, 10)
        );
    }

}
