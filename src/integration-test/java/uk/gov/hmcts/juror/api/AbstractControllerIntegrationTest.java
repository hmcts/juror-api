package uk.gov.hmcts.juror.api;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractControllerIntegrationTest<P, R> extends AbstractIntegrationTest {


    private final TestRestTemplate template;
    private final HttpMethod method;
    private final HttpStatus validStatus;
    protected HttpHeaders httpHeaders;

    private final Type returnType;

    protected abstract String getValidUrl();

    protected abstract String getValidJwt();

    protected AbstractControllerIntegrationTest(HttpMethod method, TestRestTemplate template,
                                                HttpStatus validStatus) {
        super();
        this.method = method;
        this.template = template;
        this.validStatus = validStatus;
        this.returnType = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    protected AbstractControllerIntegrationTest(HttpMethod method, TestRestTemplate template,
                                                HttpStatus validStatus, Type returnType) {
        super();
        this.method = method;
        this.template = template;
        this.validStatus = validStatus;
        this.returnType = returnType;
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    public ControllerTest<P, R> testBuilder() {
        return new ControllerTest<>();
    }

    protected abstract P getValidPayload();

    @Getter
    @Setter
    @Accessors(chain = true, fluent = true)
    public class ControllerTest<P1 extends P, R1 extends R> {
        private String jwt = getValidJwt();
        private String url = getValidUrl();
        private P payload = getValidPayload();
        Map<String, String[]> queryParams = new HashMap<>();

        public ControllerTest<P1, R1> addQueryParam(String name, String... value) {
            queryParams.put(name, value);
            return this;
        }

        public ControllerTestResponseString triggerInvalid() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
            return new ControllerTestResponseString(template.exchange(
                new RequestEntity<>(payload, httpHeaders, method,
                    URI.create(getUrlWithQueryParams())),
                String.class));
        }

        public ControllerTestResponse<R> triggerValid() {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
            return new ControllerTestResponse<>(template.exchange(
                new RequestEntity<>(payload, httpHeaders, method,
                    URI.create(getUrlWithQueryParams())),
                ParameterizedTypeReference.forType(returnType)));
        }


        private String getUrlWithQueryParams() {
            if (queryParams.isEmpty()) {
                return url;
            }
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append('?');
            queryParams.forEach((key, value) -> {
                for (String v : value) {
                    urlBuilder.append(key).append('=').append(v).append('&');
                }
            });

            return urlBuilder.substring(0, urlBuilder.length() - 1);
        }



        @Getter
        @Setter
        @Accessors(chain = true, fluent = true)
        public class ControllerTestResponse<T> extends AbstractIntegrationTest {
            protected final ResponseEntity<T> responseEntity;
            protected final T body;

            public ControllerTestResponse(ResponseEntity<T> responseEntity) {
                super();
                this.responseEntity = responseEntity;
                this.body = responseEntity.getBody();
            }

            void assertValidStatusCode() {
                assertThat(responseEntity.getStatusCode())
                    .isEqualTo(validStatus);
            }

            public void assertValidNoBody() {
                assertValidStatusCode();
                assertThat(body).isNull();
            }

            public void assertValid(BiConsumer<ControllerTest<P1, R1>, T> responseValidator) {
                assertValidStatusCode();
                responseValidator.accept(ControllerTest.this, body);
            }

            @SuppressWarnings("unchecked")
            public void assertValidCollectionExactOrder(Object... responseItems) {
                assertValidStatusCode();
                assertThat(body).isInstanceOf(Collection.class);
                assertThat((Collection<Object>) body).containsExactly(responseItems);
            }

            public void assertEquals(Object object) {
                assertValidStatusCode();
                assertThat(body).isEqualTo(object);
            }

            public ControllerTestResponse<T> printResponse() {
                System.out.println(responseEntity);
                return this;
            }

            public ControllerTestResponse<T> responseConsumer(Consumer<T> consumer) {
                consumer.accept(body);
                return this;
            }
        }

        @Getter
        @Setter
        @Accessors(chain = true, fluent = true)
        public class ControllerTestResponseString extends ControllerTestResponse<String> {

            public ControllerTestResponseString(ResponseEntity<String> responseEntity) {
                super(responseEntity);
            }

            public void assertInvalidPathParam(String message) {
                assertInvalidPathParam(responseEntity, message);
            }


            public void assertInvalidPayload(RestResponseEntityExceptionHandler.FieldError... fieldErrors) {
                assertInvalidPayload(responseEntity, fieldErrors);
            }

            public void assertBusinessRuleViolation(MojException.BusinessRuleViolation.ErrorCode errorCode,
                                                    String message) {
                assertBusinessRuleViolation(responseEntity, message, errorCode);
            }

            public void assertInternalServerErrorViolation(
                Class<? extends Exception> expectedException, String message) {
                assertInternalServerErrorViolation(responseEntity, url,
                    expectedException, message);
            }

            public void assertInvalidJwtAuthenticationException() {
                this.assertInternalServerErrorViolation(
                    InvalidJwtAuthenticationException.class,
                    "Failed to parse JWT");
            }

            public void assertNotFound(String message) {
                assertNotFound(responseEntity, url, message);
            }

            public void assertMojForbiddenResponse(String message) {
                assertMojForbiddenResponse(responseEntity, url, message);
            }

            public void assertForbiddenResponse() {
                assertForbiddenResponse(responseEntity, url);
            }

            @Override
            public ControllerTestResponseString printResponse() {
                super.printResponse();
                return this;
            }
        }
    }
}
