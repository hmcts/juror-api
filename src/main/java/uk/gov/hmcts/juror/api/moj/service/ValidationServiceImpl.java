package uk.gov.hmcts.juror.api.moj.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ValidationServiceImpl implements ValidationService {

    private final Validator validator;

    @Override
    public void validate(Object request, Class<?> _class) {
        Set<ConstraintViolation<Object>> violations =
            validator.validate(request, _class);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
