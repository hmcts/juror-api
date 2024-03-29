package uk.gov.hmcts.juror.api.utils;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

import static uk.gov.hmcts.juror.api.TestUtils.createJwt;

public class CustomArgumentResolverBureau implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(BureauJwtPayload.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return createJwt("400", "BUREAU_USER");
    }
}
