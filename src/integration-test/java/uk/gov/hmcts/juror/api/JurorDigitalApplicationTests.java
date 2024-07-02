package uk.gov.hmcts.juror.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ApplicationSettingsController;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JurorDigitalApplicationTests extends AbstractIntegrationTest {

    @Autowired
    ApplicationSettingsController controller;

    @Test
    public void contextLoads() {
        assertThat(controller)
            .as("Application Settings Controller should exist when the application context starts")
            .isNotNull();
    }
}
