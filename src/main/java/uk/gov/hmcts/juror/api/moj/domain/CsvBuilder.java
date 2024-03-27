package uk.gov.hmcts.juror.api.moj.domain;

import java.util.ArrayList;
import java.util.List;


public class CsvBuilder {
    final List<List<String>> rows;
    final int expectedLength;


    public CsvBuilder(List<String> titles) {
        this.rows = new ArrayList<>();
        expectedLength = titles.size();
        addRow(titles);
    }

    public String build() {
        return this.rows.stream()
            .map(row -> String.join(",", row))
            .reduce((a, b) -> a + "\n" + b)
            .orElse("");
    }

    public void addRow(List<String> list) {
        if (list.size() != expectedLength) {
            throw new IllegalArgumentException("Row length does not match expected length");
        }
        this.rows.add(list.stream().map(this::escapeSpecialCharacters).toList());
    }

    public String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "null";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
