package uk.gov.hmcts.juror.api.bureau.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * Tests for {@link BureauJurorSpecialNeedsRepository}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JurorReasonableAdjustmentRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;

    private JurorReasonableAdjustment reasonableAdjustment;

    @Before
    public void setUp() throws Exception {
        ReasonableAdjustments reasonableAdjustment1 = new ReasonableAdjustments("D", "ALLERGIES");
        reasonableAdjustment = new JurorReasonableAdjustment();
        reasonableAdjustment.setJurorNumber("209092530");
        reasonableAdjustment.setReasonableAdjustment(reasonableAdjustment1);
        reasonableAdjustment.setReasonableAdjustmentDetail("Some Details");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/BureauJurorSpecialNeedsRepository_findByJurorNumber.sql")
    public void findByJurorNumberWithValidJurorNumberReturnsJurorSpecialNeeds() {
        List<JurorReasonableAdjustment> actualReasonableAdjustment = jurorReasonableAdjustmentRepository
            .findByJurorNumber("209092530");
        assertThat(actualReasonableAdjustment).hasSize(1);
        assertThat(actualReasonableAdjustment).extracting("jurorNumber", "reasonableAdjustment.code",
                "reasonableAdjustment.description", "reasonableAdjustmentDetail")
            .contains(tuple(reasonableAdjustment.getJurorNumber(),
                reasonableAdjustment.getReasonableAdjustment().getCode(),
                reasonableAdjustment.getReasonableAdjustment().getDescription(),
                reasonableAdjustment.getReasonableAdjustmentDetail()));
    }
}
