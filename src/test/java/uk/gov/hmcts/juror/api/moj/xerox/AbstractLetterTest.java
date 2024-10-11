package uk.gov.hmcts.juror.api.moj.xerox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.String.format;

public abstract class AbstractLetterTest {
    protected final StringBuilder expectedEnglish = new StringBuilder();
    protected final StringBuilder expectedWelsh = new StringBuilder();

    protected abstract void setupEnglishExpectedResult();

    protected abstract void setupWelshExpectedResult();


    protected void addEnglishField(String data, Integer width) {
        expectedEnglish.append(LetterTestUtils.pad(data, width));
    }

    protected void addWelshField(String data, Integer width) {
        expectedWelsh.append(LetterTestUtils.pad(data, width));
    }

    protected void addEnglishLetterDate() {
        addEnglishField(generateDate().toUpperCase(), 18);
    }

    protected void addWelshLetterDate() {
        addWelshField(generateWelshDate().toUpperCase(), 18);
    }

    private String generateDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
                cal.add(Calendar.DAY_OF_MONTH, 2);
                break;
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            case Calendar.SATURDAY:
            case Calendar.SUNDAY:
                cal.add(Calendar.DAY_OF_MONTH, 4);
                break;
            default:
                break;
        }

        return formatter.format(cal.getTime());
    }

    private String generateWelshDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
                cal.add(Calendar.DAY_OF_MONTH, 2);
                break;
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            case Calendar.SATURDAY:
            case Calendar.SUNDAY:
                cal.add(Calendar.DAY_OF_MONTH, 4);
                break;
            default:
                break;
        }


        String dateString = formatter.format(cal.getTime()).toUpperCase();
        String[] dateParts = dateString.split("\\s");
        String welshMonth = XeroxConstants.WELSH_DATE_TRANSLATION_MAP.get(dateParts[1]);
        return format("%s %s %s", dateParts[0], welshMonth, dateParts[2]);
    }

    protected String getExpectedEnglishResult() {
        return expectedEnglish.toString();
    }

    protected String getExpectedWelshResult() {
        return expectedWelsh.toString();
    }


}
