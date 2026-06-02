package uk.gov.hmcts.juror.api.moj.service;

@FunctionalInterface
public interface ValidationService {
    void validate(Object request, Class<?> clazz);
}
