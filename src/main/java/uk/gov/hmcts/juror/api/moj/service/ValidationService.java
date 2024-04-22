package uk.gov.hmcts.juror.api.moj.service;

public interface ValidationService {
    void validate(Object request, Class<?> _class);
}
