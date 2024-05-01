package uk.gov.hmcts.juror.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ApplicationSettingsController;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JurorDigitalApplicationTests extends AbstractIntegrationTest {

    @Autowired
    ApplicationSettingsController controller;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @Test
    public void contextLoads() {
        assertThat(controller)
            .as("Application Settings Controller should exist when the application context starts")
            .isNotNull();
    }

    /**
     * Reset the sequences multiple times to assert they are resetting correctly using the @Before hook.
     */
    @Test
    @Sql("/db/truncate.sql")
    @Repeat(3)
    public void sequenceResetWorksCorrectly() throws Exception {
        assertThat(jdbcTemplate.queryForObject("SELECT currval('JUROR_DIGITAL.SPEC_NEED_SEQ')", Integer.class))
                .as("The sequence initial value is 999 (for testing) in it's default state. "
                    + "Is the stored procedure for sequence rest accessible?")
                .isEqualTo(999);
        assertThat(jdbcTemplate.queryForObject("SELECT nextval('JUROR_DIGITAL.SPEC_NEED_SEQ')", Integer.class))
                .as("The sequence next value is 1000 (during testing)")
                .isEqualTo(1000);
    }
}
