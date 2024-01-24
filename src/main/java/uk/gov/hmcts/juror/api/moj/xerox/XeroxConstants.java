package uk.gov.hmcts.juror.api.moj.xerox;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class XeroxConstants {

    public static final Map<String, String> WELSH_DATE_TRANSLATION_MAP = Stream.of(new String[][]{
        {"MONDAY", "Dydd Llun"},
        {"TUESDAY", "Dydd Mawrth"},
        {"WEDNESDAY", "Dydd Mercher"},
        {"THURSDAY", "Dydd Iau"},
        {"FRIDAY", "Dydd Gwener"},
        {"SATURDAY", "Dydd Sadwrn"},
        {"SUNDAY", "Dydd Sul"},
        {"JANUARY", "Ionawr"},
        {"FEBRUARY", "Chwefror"},
        {"MARCH", "Mawrth"},
        {"APRIL", "Ebrill"},
        {"MAY", "Mai"},
        {"JUNE", "Mehefin"},
        {"JULY", "Gorffenaf"},
        {"AUGUST", "Awst"},
        {"SEPTEMBER", "Medi"},
        {"OCTOBER", "Hydref"},
        {"NOVEMBER", "Tachwedd"},
        {"DECEMBER", "Rhagfyr"}
    }).collect(Collectors.collectingAndThen(
        Collectors.toMap(data -> data[0], data -> data[1]),
        Collections::<String, String>unmodifiableMap
    ));

    private XeroxConstants() {
    }

}
