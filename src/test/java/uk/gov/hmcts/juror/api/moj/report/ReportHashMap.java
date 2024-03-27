package uk.gov.hmcts.juror.api.moj.report;

import java.util.HashMap;

public class ReportHashMap<K, V> extends HashMap<K, V> {

    public ReportHashMap<K, V> add(K key, V value) {
        put(key, value);
        return this;
    }
}
