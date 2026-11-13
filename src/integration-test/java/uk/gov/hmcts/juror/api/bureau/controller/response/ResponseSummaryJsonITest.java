package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.juror.api.bureau.service.BureauTransformsServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.CombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Checks the production transformations and Spring Jackson configuration against the frontend contract.
 */
@JsonTest
@Import(BureauTransformsServiceImpl.class)
@SuppressWarnings("PMD.TooManyMethods") // Fixture helpers keep each contract scenario focused.
class ResponseSummaryJsonITest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private BureauTransformsServiceImpl transforms;
    @MockitoBean
    private UrgencyService urgencyService;

    @Test
    void viewMappingsPreserveBothWrapperContracts() throws Exception {
        ModJurorDetail detail = detail();
        assertWrappers(transforms.detailToDto(detail), transforms.detailCourtToDto(detail), expected("populated"));
    }

    @Test
    void viewMappingsPreserveNullableFields() throws Exception {
        ModJurorDetail detail = new ModJurorDetail();
        ObjectNode expected = (ObjectNode) expected("empty");
        expected.put("sla_overdue", false);
        assertWrappers(transforms.detailToDto(detail), transforms.detailCourtToDto(detail), expected);
    }

    @Test
    void combinedResponseMappingsPreserveBothWrapperContracts() throws Exception {
        CombinedJurorResponse response = response(true);
        Juror juror = juror();
        JurorPool jurorPool = new JurorPool();
        jurorPool.setNextDate(LocalDate.of(2026, 11, 1));
        PoolRequest pool = pool();
        when(urgencyService.slaBreached(ProcessingStatus.CLOSED, jurorPool.getNextDate())).thenReturn(false);

        assertWrappers(transforms.detailToDto(response, juror, jurorPool, pool),
            transforms.detailCourtToDto(response, juror, jurorPool, pool, "415"), expected("populated"));
    }

    @Test
    void courtMappingPreservesLocationOverrideAndUnassignedStaff() throws Exception {
        final CombinedJurorResponse response = response(false);
        JurorPool jurorPool = new JurorPool();
        jurorPool.setNextDate(LocalDate.of(2026, 11, 1));
        when(urgencyService.slaBreached(ProcessingStatus.CLOSED, jurorPool.getNextDate())).thenReturn(true);

        ObjectNode expectedObject = (ObjectNode) expected("populated");
        expectedObject.putNull("assigned_staff_member");
        expectedObject.putNull("completed_at");
        expectedObject.put("sla_overdue", true);
        BureauResponseSummaryDto bureau = transforms.detailToDto(response, juror(), jurorPool, pool());
        assertThat(mapper.<JsonNode>valueToTree(bureau)).isEqualTo(expectedObject);
        expectedObject.put("court_code", "416");
        CourtResponseSummaryDto court = transforms.detailCourtToDto(response, juror(), jurorPool, pool(), "416");
        assertThat(mapper.<JsonNode>valueToTree(court)).isEqualTo(expectedObject);
    }

    private void assertWrappers(BureauResponseSummaryDto bureau, CourtResponseSummaryDto court, JsonNode expected)
        throws IOException {
        BureauResponseSummaryWrapper bureauWrapper = BureauResponseSummaryWrapper.builder()
            .todoCount(2L).repliesPendingCount(3L).completedCount(4L).responses(List.of(bureau)).build();
        CourtResponseSummaryWrapper courtWrapper = CourtResponseSummaryWrapper.builder()
            .todoCount(2L).repliesPendingCount(3L).completedCount(4L).responses(List.of(court)).build();
        JsonNode bureauJson = mapper.<JsonNode>valueToTree(bureauWrapper);
        JsonNode courtJson = mapper.<JsonNode>valueToTree(courtWrapper);
        assertThat(bureauJson).isEqualTo(courtJson);
        assertThat(bureauJson.properties()).extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("todo_count", "replies_pending_count", "completed_count", "data");
        assertThat(bureauJson.path("todo_count").longValue()).isEqualTo(2L);
        assertThat(bureauJson.path("replies_pending_count").longValue()).isEqualTo(3L);
        assertThat(bureauJson.path("completed_count").longValue()).isEqualTo(4L);
        assertThat(bureauJson.path("data")).hasSize(1);
        assertThat(bureauJson.path("data").get(0)).isEqualTo(expected);
        assertThat(mapper.treeToValue(bureauJson, BureauResponseSummaryWrapper.class)).isEqualTo(bureauWrapper);
        assertThat(mapper.treeToValue(courtJson, CourtResponseSummaryWrapper.class)).isEqualTo(courtWrapper);
    }

    private ModJurorDetail detail() {
        ModJurorDetail detail = new ModJurorDetail();
        detail.setJurorNumber("123456789");
        detail.setTitle("Old title");
        detail.setFirstName("Old first name");
        detail.setLastName("Old last name");
        detail.setNewTitle("Dr");
        detail.setNewFirstName("Test");
        detail.setNewLastName("Juror");
        detail.setNewJurorPostcode("AA1 1AA");
        detail.setCourtName("Test Court");
        detail.setCourtCode("415");
        detail.setReplyType("Digital");
        detail.setProcessingStatus("CLOSED");
        detail.setResidency(true);
        detail.setMentalHealthAct(false);
        detail.setBail(false);
        detail.setConvictions(true);
        detail.setDeferralDate("2026-11-02");
        detail.setDeferral(true);
        detail.setExcusalReason("Synthetic reason");
        detail.setExcusal(false);
        detail.setPoolNumber("415260101");
        detail.setUrgent(true);
        detail.setSlaOverdue(false);
        detail.setDateReceived(LocalDate.of(2026, 10, 1));
        detail.setAssignedStaffMember(staff());
        detail.setVersion(7);
        detail.setCompletedAt(LocalDateTime.of(2026, 10, 2, 13, 14, 15));
        return detail;
    }

    private CombinedJurorResponse response(boolean assigned) {
        return CombinedJurorResponse.builder()
            .replyType(new ReplyType("Digital", "Digital response"))
            .processingStatus(ProcessingStatus.CLOSED)
            .residency(true)
            .mentalHealthAct(false)
            .bail(false)
            .convictions(true)
            .deferralDate("2026-11-02")
            .deferral(true)
            .excusalReason("Synthetic reason")
            .excusal(false)
            .urgent(true)
            .dateReceived(LocalDateTime.of(2026, 10, 1, 9, 10, 11))
            .staff(assigned ? staff() : null)
            .version(7)
            .completedAt(assigned ? LocalDateTime.of(2026, 10, 2, 13, 14, 15) : null)
            .build();
    }

    private Juror juror() {
        Juror juror = new Juror();
        juror.setJurorNumber("123456789");
        juror.setTitle("Dr");
        juror.setFirstName("Test");
        juror.setLastName("Juror");
        juror.setPostcode("AA1 1AA");
        return juror;
    }

    private PoolRequest pool() {
        CourtLocation court = new CourtLocation();
        court.setLocCode("415");
        court.setName("Test Court");
        PoolRequest pool = new PoolRequest();
        pool.setCourtLocation(court);
        pool.setPoolNumber("415260101");
        return pool;
    }

    private User staff() {
        User user = new User();
        user.setUsername("teststaff");
        user.setName("Test Staff");
        user.setActive(true);
        user.setUserType(UserType.BUREAU);
        user.setRoles(Set.of(Role.MANAGER));
        return user;
    }

    private JsonNode expected(String fixture) throws IOException {
        return mapper.readTree(getClass().getResource("/response-summary/" + fixture + ".json"));
    }
}
