package uk.gov.hmcts.juror.api.moj.report;

import java.util.LinkedHashMap;

public class ReportLinkedMap<K, V> extends LinkedHashMap<K, V> {

    public ReportLinkedMap<K, V> add(K key, V value) {
        put(key, value);
        return this;
    }
}
