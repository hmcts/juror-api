package uk.gov.hmcts.juror.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * POJO to map json response of default Spring Boot.
 * {@link org.springframework.boot.web.servlet.error.DefaultErrorAttributes}
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpringBootErrorResponse {
    private Date timestamp;
    private Integer status;
    private List<Error> errors;
    private String error;
    private String exception;
    private String message;
    private String path;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Error {
        private List<String> codes;
        private String defaultMessage;
        private String objectName;
        private String field;
        //        private String rejectedValue;
        private String bindingFailure;
        private String code;
        private List<Argument> arguments;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Argument {
        private List<String> codes;
        private String arguments;
        private String defaultMessage;
        private String code;
    }
}
