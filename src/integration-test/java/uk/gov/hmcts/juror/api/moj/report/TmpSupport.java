package uk.gov.hmcts.juror.api.moj.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TmpSupport {

    private static String asStringLinked(List<LinkedHashMap<String, Object>> data) {
        return "List.of(" + data.stream()
            .map(TmpSupport::asStringLinked)
            .collect(Collectors.joining(",")) + ")";
    }

    private static String asStringLinked(LinkedHashMap<String, Object> data) {
        return "\nnew ReportLinkedMap<String, Object>()"
            + data.entrySet()
            .stream()
            .map(TmpSupport::asStringLinked)
            .reduce("", (s, s2) -> s + s2);
    }

    private static String asStringLinked(Map.Entry<String, Object> data) {
        if (data.getValue() instanceof LinkedHashMap) {
            return "\n.add(\"" + data.getKey() + "\", " + asStringLinked(
                (LinkedHashMap<String, Object>) data.getValue()) + ")";
        }
        return "\n.add(\"" + data.getKey() + "\", " + asStringObj(data.getValue()) + ")";
    }

    public static String asString(StandardReportResponse standardReportResponse) {
        return "StandardReportResponse.builder()"
            + "\n.headings(new ReportHashMap<String, StandardReportResponse.DataTypeValue>()"
            +
            standardReportResponse.getHeadings().entrySet()
                .stream()
                .map(TmpSupport::asHeading)
                .reduce("", (s, s2) -> s + s2)
            + ")"
            + "\n.tableData(" + asString(standardReportResponse.getTableData()) + ")"
            + "\n.build()";
    }

    public static String asHeading(Map.Entry<String, StandardReportResponse.DataTypeValue> stringDataTypeValueEntry) {
        return "\n.add(\"" + stringDataTypeValueEntry.getKey() + "\", StandardReportResponse.DataTypeValue.builder()"
            + "\n.displayName(" + asString(stringDataTypeValueEntry.getValue().getDisplayName()) + ")"
            + "\n.dataType(" + asString(stringDataTypeValueEntry.getValue().getDataType()) + ")"
            + "\n.value(" + asStringObj(stringDataTypeValueEntry.getValue().getValue()) + ")"
            + "\n.build())";
    }

    public static String asString(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value + "\"";
    }

    public static String asString(StandardReportResponse.DataTypeValue value) {
        if (value == null) {
            return "null";
        }
        if (value.getDataType().equals(String.class.getSimpleName())) {
            return "\"" + value.getValue() + "\"";
        }
        return String.valueOf(value.getValue());
    }

    public static String asStringObj(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }

    public static String asString(StandardReportResponse.TableData.Heading heading) {
        return "\nStandardReportResponse.TableData.Heading.builder()"
            + "\n.id(" + asString(heading.getId()) + ")"
            + "\n.name(" + asString(heading.getName()) + ")"
            + "\n.dataType(" + asString(heading.getDataType()) + ")"
            + "\n.headings(" + asString(heading.getHeadings()) + ")"
            + "\n.build()";
    }

    public static String asString(List<StandardReportResponse.TableData.Heading> headings) {
        if (headings == null) {
            return "null";
        }
        return "List.of(" + headings.stream()
            .map(TmpSupport::asString)
            .collect(Collectors.joining(",")) + ")";
    }

    public static String asString(StandardReportResponse.TableData tableData) {
        return "\nStandardReportResponse.TableData.builder()"
            + "\n.headings(" + asString(tableData.getHeadings()) + ")"
            + "\n.data(" + asStringLinked(tableData.getData()) + ")"
            + "\n.build()";
    }
}
