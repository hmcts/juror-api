package uk.gov.hmcts.juror.api.moj.report;

public final class AvailableListReportUtil {

    public static final ReportLinkedMap<String, Object> JUROR_00 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500020")
            .add("first_name", "John0")
            .add("last_name", "Smith0")
            .add("status", "Responded");

    public static final ReportLinkedMap<String, Object> JUROR_02 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500022")
            .add("first_name", "John2")
            .add("last_name", "Smith2")
            .add("status", "Responded");

    public static final ReportLinkedMap<String, Object> JUROR_03 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500023")
            .add("first_name", "John3")
            .add("last_name", "Smith3")
            .add("status", "Summoned");

    public static final ReportLinkedMap<String, Object> JUROR_04 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500024")
            .add("first_name", "John4")
            .add("last_name", "Smith4")
            .add("status", "Responded");

    public static final ReportLinkedMap<String, Object> JUROR_05 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500025")
            .add("first_name", "John5")
            .add("last_name", "Smith5")
            .add("status", "Juror");

    public static final ReportLinkedMap<String, Object> JUROR_06 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500026")
            .add("first_name", "John6")
            .add("last_name", "Smith6")
            .add("status", "Panel")
            .add("juror_reasonable_adjustment_with_message",
                new ReportLinkedMap<String, Object>()
                    .add("reasonable_adjustment_code_with_description", "U - MEDICATION")
                    .add("juror_reasonable_adjustment_message", "Test Message 2"));

    public static final ReportLinkedMap<String, Object> JUROR_07 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500027")
            .add("first_name", "John7")
            .add("last_name", "Smith7")
            .add("status", "Responded");

    public static final ReportLinkedMap<String, Object> JUROR_08 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500028")
            .add("first_name", "John8")
            .add("last_name", "Smith8")
            .add("status", "Responded");

    public static final ReportLinkedMap<String, Object> JUROR_09 =
        new ReportLinkedMap<String, Object>()
            .add("juror_number", "641500029")
            .add("first_name", "John9")
            .add("last_name", "Smith9")
            .add("status", "Summoned")
            .add("juror_reasonable_adjustment_with_message",
                new ReportLinkedMap<String, Object>()
                    .add("reasonable_adjustment_code_with_description", "W - WHEEL CHAIR ACCESS")
                    .add("juror_reasonable_adjustment_message", "Test Message 3"));

    private AvailableListReportUtil() {

    }
}
