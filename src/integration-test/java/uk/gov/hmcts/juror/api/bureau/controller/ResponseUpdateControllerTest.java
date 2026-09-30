package uk.gov.hmcts.juror.api.bureau.controller;

import jakarta.validation.ValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ResponseUpdateController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResponseUpdateControllerTest extends AbstractIntegrationTest {
    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private TestRestTemplate template;

    @Test
    public void assertJurorNumberPathVariable_happy_validationPass() throws Exception {
        final String validJurorNumber = "123456789";
        ResponseUpdateController.assertJurorNumberPathVariable(validJurorNumber);
        // no exception = pass
    }

    @Test
    public void assertJurorNumberPathVariable_unhappy_validationFail() throws Exception {
        final String invalidJurorNumberLong = "1234567890";// too long
        final String invalidJurorNumberShort = "12345678";// too short
        final String invalidJurorNumberAlpha = "1234S6789";// alpha char
        final String invalidJurorNumberSpecial = "!23456789";// special char

        try {
            ResponseUpdateController.assertJurorNumberPathVariable(invalidJurorNumberLong);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.assertJurorNumberPathVariable(invalidJurorNumberShort);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.assertJurorNumberPathVariable(invalidJurorNumberAlpha);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.assertJurorNumberPathVariable(invalidJurorNumberSpecial);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }
    }
}
