package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseSummaryDtoTest {

    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @ParameterizedTest
    @ValueSource(classes = {BureauResponseSummaryDto.class, CourtResponseSummaryDto.class})
    void populatedBuilderPreservesJsonContract(Class<?> type) throws Exception {
        assertThat(mapper.<JsonNode>valueToTree(populated(type))).isEqualTo(expected("populated"));
    }

    @ParameterizedTest
    @ValueSource(classes = {BureauResponseSummaryDto.class, CourtResponseSummaryDto.class})
    void noArgsConstructorPreservesNullFields(Class<?> type) throws Exception {
        assertThat(mapper.<JsonNode>valueToTree(type.getConstructor().newInstance())).isEqualTo(expected("empty"));
    }

    @ParameterizedTest
    @ValueSource(classes = {BureauResponseSummaryDto.class, CourtResponseSummaryDto.class})
    void deserializationPreservesAllValuesAndEquality(Class<?> type) throws Exception {
        Object dto = mapper.treeToValue(expected("populated"), type);
        assertThat(mapper.<JsonNode>valueToTree(dto)).isEqualTo(expected("populated"));
        assertThat(dto).isEqualTo(populated(type)).hasSameHashCodeAs(populated(type));
        assertThat(dto).isNotEqualTo(type.getConstructor().newInstance());
        assertThat(mapper.treeToValue(expected("empty"), type))
            .isEqualTo(type.getConstructor().newInstance());
    }

    @ParameterizedTest
    @ValueSource(classes = {BureauResponseSummaryDto.class, CourtResponseSummaryDto.class})
    void openApiPreservesPropertiesAndRequiredVersion(Class<?> type) throws Exception {
        Schema<?> schema = ModelConverters.getInstance().read(type).get(type.getSimpleName());
        List<String> properties = new ArrayList<>();
        expected("populated").fieldNames().forEachRemaining(properties::add);
        assertThat(schema.getProperties().keySet()).containsExactlyInAnyOrderElementsOf(properties);
        assertThat(schema.getRequired()).containsExactly("version");
    }

    @Test
    void allArgsConstructorsPreserveFieldOrder() throws Exception {
        BureauResponseSummaryDto bureau = new BureauResponseSummaryDto(
            "123456789", "Dr", "Test", "Juror", "AA1 1AA", "Test Court", "415", "Digital", "CLOSED",
            true, false, false, true, "2026-11-02", true, "Synthetic reason", false, "415260101", true, false,
            LocalDate.of(2026, 10, 1), staff(), 7, LocalDateTime.of(2026, 10, 2, 13, 14, 15));
        CourtResponseSummaryDto court = new CourtResponseSummaryDto(
            "123456789", "Dr", "Test", "Juror", "AA1 1AA", "Test Court", "415", "Digital", "CLOSED",
            true, false, false, true, "2026-11-02", true, "Synthetic reason", false, "415260101", true, false,
            LocalDate.of(2026, 10, 1), staff(), 7, LocalDateTime.of(2026, 10, 2, 13, 14, 15));
        assertThat(mapper.<JsonNode>valueToTree(bureau)).isEqualTo(expected("populated"));
        assertThat(mapper.<JsonNode>valueToTree(court)).isEqualTo(expected("populated"));
        assertThat(bureau).isNotEqualTo(court);
    }

    private Object populated(Class<?> type) {
        if (type == BureauResponseSummaryDto.class) {
            return BureauResponseSummaryDto.builder()
                .jurorNumber("123456789")
                .title("Dr")
                .firstName("Test")
                .lastName("Juror")
                .postcode("AA1 1AA")
                .courtName("Test Court")
                .courtCode("415")
                .replyMethod("Digital")
                .processingStatus("CLOSED")
                .residency(true)
                .mentalHealthAct(false)
                .bail(false)
                .convictions(true)
                .deferralDate("2026-11-02")
                .deferral(true)
                .excusalReason("Synthetic reason")
                .excusal(false)
                .poolNumber("415260101")
                .urgent(true)
                .slaOverdue(false)
                .dateReceived(LocalDate.of(2026, 10, 1))
                .assignedStaffMember(staff())
                .version(7)
                .completedAt(LocalDateTime.of(2026, 10, 2, 13, 14, 15))
                .build();
        }
        return CourtResponseSummaryDto.builder()
            .jurorNumber("123456789")
            .title("Dr")
            .firstName("Test")
            .lastName("Juror")
            .postcode("AA1 1AA")
            .courtName("Test Court")
            .courtCode("415")
            .replyMethod("Digital")
            .processingStatus("CLOSED")
            .residency(true)
            .mentalHealthAct(false)
            .bail(false)
            .convictions(true)
            .deferralDate("2026-11-02")
            .deferral(true)
            .excusalReason("Synthetic reason")
            .excusal(false)
            .poolNumber("415260101")
            .urgent(true)
            .slaOverdue(false)
            .dateReceived(LocalDate.of(2026, 10, 1))
            .assignedStaffMember(staff())
            .version(7)
            .completedAt(LocalDateTime.of(2026, 10, 2, 13, 14, 15))
            .build();
    }

    private StaffDto staff() {
        return StaffDto.builder().login("teststaff").name("Test Staff").isTeamLeader(true).isActive(true).build();
    }

    private JsonNode expected(String fixture) throws IOException {
        return mapper.readTree(getClass().getResource("/response-summary/" + fixture + ".json"));
    }
}
