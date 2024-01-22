package uk.gov.hmcts.juror.api.validation;

import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.AnnotationFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class AbstractValidatorTest<T extends Annotation> {

    protected T createAnnotation(final Map<String, Object> values, Class<T> annotation) {
        final AnnotationDescriptor.Builder<T> builder = new AnnotationDescriptor.Builder<>(annotation);
        values.forEach(builder::setAttribute);
        return AnnotationFactory.create(builder.build());
    }
}
