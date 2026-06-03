package uk.gov.hmcts.juror.api.moj.service;

@FunctionalInterface
public interface DeletePoolService {

    void deletePool(String poolNumber);

}
