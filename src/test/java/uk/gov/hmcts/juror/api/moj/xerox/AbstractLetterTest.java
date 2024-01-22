package uk.gov.hmcts.juror.api.moj.xerox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        addWelshField(generateDate().toUpperCase(), 18);
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
                cal.add(Calendar.DAY_OF_MONTH, 4);
                break;
            default:
                break;
        }

        return formatter.format(cal.getTime());
    }

    protected String getExpectedEnglishResult() {
        return expectedEnglish.toString();
    }

    protected String getExpectedWelshResult() {
        return expectedWelsh.toString();
    }


}
