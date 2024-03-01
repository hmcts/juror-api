package uk.gov.hmcts.juror.api.moj.domain.system;

public interface HasCodeAndDescription<T extends Comparable<T>> {

    T getCode();

    String getDescription();
}
