package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application settings service tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppSettingServiceImplTest extends AbstractIntegrationTest {
    @Autowired
    private AppSettingService appSettingService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    public void findAllSettings() {
        List<AppSetting> allSettings = appSettingService.findAllSettings();
        assertThat(allSettings.size()).isGreaterThanOrEqualTo(17);
        assertThat(allSettings).doesNotHaveDuplicates();
    }
}