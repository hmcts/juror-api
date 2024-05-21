package uk.gov.hmcts.juror.api.bureau.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.scheduler.BureauBatchScheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Integration test for {@link JurorCommsCronBatchController}.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JurorCommsCronBatchControllerTest extends AbstractIntegrationTest {

    private static final String[] TYPES = {"letterComms"};

    @MockBean
    BureauBatchScheduler bureauBatchScheduler;

    @Autowired
    JurorCommsCronBatchController jurorCommsCronBatchController;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void jurorCommsCronBatch_happy() {
        ResponseEntity<Object> response = jurorCommsCronBatchController.callBureauBatchScheduler(TYPES, null, null);
        verify(bureauBatchScheduler, times(1)).processBatchJobServices(TYPES, null, null);
        assertThat(response).isNotNull();
    }
}
