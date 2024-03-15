package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BureauServiceImpl}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private BureauService bureauService;

    private BureauJurorDetail bureauJurorDetail;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bureauJurorDetail = new BureauJurorDetail();
        bureauJurorDetail.setJurorNumber("209092530");
        bureauJurorDetail.setTitle("Dr");
        bureauJurorDetail.setFirstName("Jane");
        bureauJurorDetail.setLastName("CASTILLO");
        bureauJurorDetail.setProcessingStatus("TODO");
        bureauJurorDetail.setCourtName("PRESTON");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauRepository_findByJurorNumber.sql")
    public void getDetailsByJurorNumber_WithValidJurorNumber_ReturnsJustJurorDetailsNotEnriched() {
        BureauJurorDetailDto actualDetails = bureauService.getDetailsByJurorNumber(bureauJurorDetail.getJurorNumber());
        assertThat(actualDetails).extracting("jurorNumber", "title", "firstName", "lastName", "processingStatus",
                "courtName")
            .contains(bureauJurorDetail.getJurorNumber(), bureauJurorDetail.getTitle(),
                bureauJurorDetail.getFirstName(), bureauJurorDetail.getLastName(),
                bureauJurorDetail.getProcessingStatus(), bureauJurorDetail.getCourtName());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauRepository_findByJurorNumber.sql")
    @Sql("/db/BureauLogRepository_findByLogKey.sql")
    @Sql("/db/BureauJurorSpecialNeedsRepository_findByJurorNumber.sql")
    @Sql("/db/BureauJurorCJSRepository_findByCjsKey.sql")
    public void getDetailsByJurorNumber_WithValidJurorNumber_ReturnsEnrichedDetails() {
        BureauJurorDetailDto actualDetails = bureauService.getDetailsByJurorNumber(bureauJurorDetail.getJurorNumber());
        assertThat(actualDetails.getPhoneLogs()).hasSize(2);
        assertThat(actualDetails.getPhoneLogs().get(0)).extracting("jurorNumber")
            .isEqualTo(bureauJurorDetail.getJurorNumber());
        assertThat(actualDetails.getCjsEmployments()).hasSize(1);
        assertThat(actualDetails.getCjsEmployments().get(0)).extracting("jurorNumber")
            .isEqualTo(bureauJurorDetail.getJurorNumber());
        assertThat(actualDetails.getSpecialNeeds()).hasSize(1);
        assertThat(actualDetails.getSpecialNeeds().get(0)).extracting("jurorNumber")
            .isEqualTo(bureauJurorDetail.getJurorNumber());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauRepository_findByJurorNumber.sql")
    public void getDetailsByProcessingStatus_WithValidCategory_ReturnsDetailsWithStatusPlusCount() {

        BureauResponseSummaryWrapper wrapper = bureauService.getDetailsByProcessingStatus("todo");
        assertThat(wrapper.getResponses()).hasSize(3);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);
        assertCorrectSortOrder(wrapper);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauRepository_findByJurorNumber.sql")
    public void getDetailsByProcessingStatus_WithInValidCategory_ReturnsAllResults() {

        BureauResponseSummaryWrapper wrapper = bureauService.getDetailsByProcessingStatus("bogus");
        assertThat(wrapper.getResponses()).hasSize(8);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);

    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImpl_getByProcessingStatus.sql"
    })
    public void getTodo_returnsCorrectResults() {
        BureauResponseSummaryWrapper wrapper = bureauService.getTodo("ncrawford");
        assertThat(wrapper.getResponses()).isNotNull().hasSize(3);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);
        assertThat(wrapper.getResponses()).extracting("jurorNumber").contains("209092530", "586856851", "487498307");
        assertThat(wrapper.getResponses()).extracting("processingStatus").containsOnly("TODO");
        assertThat(wrapper.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly(
            "ncrawford");
        assertCorrectSortOrder(wrapper);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImpl_getByProcessingStatus.sql"
    })
    public void getTodo_returnsEmptyListWhenNoResults() {
        final BureauResponseSummaryWrapper wrapper = bureauService.getTodo("kfry");
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getResponses()).isNotNull().isEmpty();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImpl_getByProcessingStatus.sql"
    })
    public void getRepliesPending_returnsCorrectResults() {
        BureauResponseSummaryWrapper wrapper = bureauService.getPending("ncrawford");
        assertThat(wrapper.getResponses()).isNotNull().hasSize(4);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);
        assertThat(wrapper.getResponses()).extracting("jurorNumber").contains("472008411", "845814425", "275852838",
            "811923115");
        assertThat(wrapper.getResponses()).extracting("processingStatus").containsOnly("AWAITING_CONTACT",
            "AWAITING_TRANSLATION", "AWAITING_COURT_REPLY");
        assertThat(wrapper.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly(
            "ncrawford");
        assertCorrectSortOrder(wrapper);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImpl_getByProcessingStatus.sql"
    })
    public void getRepliesPending_returnsEmptyListWhenNoResults() {
        final BureauResponseSummaryWrapper wrapper = bureauService.getPending("kfry");
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getResponses()).isNotNull().isEmpty();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauServiceImplTest_completedToday.sql")
    public void getCompletedToday_returnsCorrectResults() {
        BureauResponseSummaryWrapper wrapper = bureauService.getCompletedToday("ncrawford");
        assertThat(wrapper.getResponses()).isNotNull().hasSize(1);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);
        assertThat(wrapper.getResponses()).extracting("jurorNumber").contains("827761086");
        assertThat(wrapper.getResponses()).extracting("processingStatus").containsOnly("CLOSED");
        assertThat(wrapper.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly(
            "ncrawford");
        assertCorrectSortOrder(wrapper);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauServiceImplTest_completedToday.sql")
    public void getCompletedToday_returnsEmptyListWhenNoResults() {
        final BureauResponseSummaryWrapper wrapper = bureauService.getCompletedToday("kfry");
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getResponses()).isNotNull().isEmpty();
    }

    /**
     * Asserts that the response is sorted in the order required by the AC
     *
     * @param wrapper response to assert against
     * @since JDB-2142
     */
    private void assertCorrectSortOrder(BureauResponseSummaryWrapper wrapper) {
        assertThat(wrapper.getResponses()).isSortedAccordingTo(
            Comparator.comparing(BureauResponseSummaryDto::getDateReceived));
    }

}
