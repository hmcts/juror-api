package uk.gov.hmcts.juror.api.bureau.service;

import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorResponseSearchRequest;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorResponseSearchResults;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JurorResponseSearchServiceImpl}.
 *
 * @implNote Reduced logging levels for this test to increase speed.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "logging.level.uk.gov.hmcts.juror.api=INFO",
    "logging.level.org.hibernate.type=INFO",
    "spring.jpa.show-sql=false"
})
@SuppressWarnings("PMD.TooManyMethods")
public class JurorResponseSearchServiceImplTest extends AbstractIntegrationTest {


    @Autowired
    private JurorResponseSearchService searchService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_jurorNumberOnly() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().jurorNumber("111111009").build(),
                false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(1).extracting("lastName").containsOnly("Cabrera");

        assertResponsesSortedCorrectly(dto);
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_lastNameOnly() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().lastName("Charleston").build(),
                false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(1);
        // Only surnames _beginning_ with the search string should be found
        assertThat(dto.getResponses()).extracting("lastName").contains("Charleston");
        assertThat(dto.getResponses()).extracting("lastName").doesNotContainNull();

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer_detailsChangedInResponse.sql"
    })
    public void searchForResponses_lastNameOnly_lastNameChangedInJurorResponse() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().lastName("McChangedname").build(),
                false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(1);
        assertThat(dto.getResponses()).extracting("lastName").contains("McChangedname");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_lastNameOnly_bureauOfficer_maximum100Results() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().lastName("a").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(0);
        assertThat(dto.getMeta().getMax()).isEqualTo(1000);
        assertThat(dto.getMeta().getTotal()).isEqualTo(0);

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql")
    public void searchForResponses_lastNameOnly_teamLeader_maximum250Results() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().lastName("e").build(), true);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(0);
        assertThat(dto.getMeta().getMax()).isEqualTo(250);
        assertThat(dto.getMeta().getTotal()).isEqualTo(0);

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_postcodeOnly() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().postCode("IV180AL").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(87);
        assertThat(dto.getResponses()).allMatch(result -> "IV180AL".equals(result.getPostcode()));

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer_detailsChangedInResponse.sql")
    public void searchForResponses_postcodeOnly_postcodeChangedInJurorResponse() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().postCode("R").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(0);
        assertThat(dto.getResponses()).allMatch(result -> "RG9 2AG".equals(result.getPostcode()));

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_postcodeOnly_spaceHandling() {
        final JurorResponseSearchResults withoutSpaceResults =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().postCode("IV180AL").build(), false);
        final JurorResponseSearchResults withSpaceResults =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().postCode("IV18 0AL").build(), false);
        assertThat(withSpaceResults.getResponses()).hasSameElementsAs(withoutSpaceResults.getResponses());

        assertResponsesSortedCorrectly(withoutSpaceResults);
    }

    /**
     * Tests search matching against five-character postcodes in the database.
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_fiveCharacterPostcode.sql")
    public void searchForResponses_postcodeOnly_fiveCharacterPostcode() {
        final List<String> works = Arrays.asList("G1 1RD", "G11RD");
        //"G1 1R", "G11R", "G1 1", "G11", "G1", "1 1RD", "11RD", "1RD"
        final List<String> doesNotWork = Arrays.asList("G1 1R", "G11R", "G1 1", "G11", "G1", "1 1RD", "11RD", "1RD");
        assertPostcodeCheck(works, doesNotWork);
    }

    /**
     * Tests search matching against five-character postcodes in the database.
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_sixCharacterPostcode.sql")
    public void searchForResponses_postcodeOnly_sixCharacterPostcode() {
        final List<String> works = Arrays.asList("G46 6JF", "G466JF");
        final List<String> doesNotWork = Arrays.asList("G4", "G46", "G466", "G46 6", "G466J", "G46 6J", "466JF",
            "46 6JF", "6 6JF", "66JF", "6JF");
        assertPostcodeCheck(works, doesNotWork);
    }

    /**
     * Tests search matching against seven-character postcodes in the database.
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_sevenCharacterPostcode.sql")
    public void searchForResponses_postcodeOnly_sevenCharacterPostcode() {
        final List<String> works = Arrays.asList("LL12 7BQ", "LL127BQ");
        final List<String> doesNotWork = Arrays.asList("LL12 7B", "LL127B", "LL12 7", "LL127", "LL12", "L12 7BQ",
            "L127BQ", "12 7BQ", "127BQ", "2 7BQ", "27BQ", "7BQ");
        assertPostcodeCheck(works, doesNotWork);
    }

    private void assertPostcodeCheck(Iterable<String> working, Iterable<String> notWorking) {
        for (String search : working) {
            assertThat(searchService.searchForResponses(JurorResponseSearchRequest.builder().postCode(search).build(),
                false).getResponses()).describedAs("Search using " + search + " should return 50 matches")
                .hasSize(50);
            assertThat(searchService.searchForResponses(
                    JurorResponseSearchRequest.builder().postCode(search.toLowerCase()).build(), false)
                .getResponses()).describedAs("Search using " + search.toLowerCase() + " should return 50 matches")
                .hasSize(50);
        }
        for (String search : notWorking) {
            assertThat(searchService.searchForResponses(JurorResponseSearchRequest.builder().postCode(search).build(),
                false).getResponses()).describedAs("Search using " + search + " should return 0 matches").size()
                .isLessThan(50);
            assertThat(searchService.searchForResponses(
                    JurorResponseSearchRequest.builder().postCode(search.toLowerCase()).build(), false)
                .getResponses()).describedAs("Search using " + search.toLowerCase() + " should return 0 matches").size()
                .isLessThan(50);
        }
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_jurorNumberAndLastName() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().jurorNumber("111111270").lastName(
                "Parkhurst").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(1);
        assertThat(dto.getResponses()).extracting("firstName").containsOnly("Lauren");

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_jurorNumberAndPostcode() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().jurorNumber("111111236").postCode(
                "IV180AL").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(1);

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")//False positive
    public void searchForResponses_lastNameAndPostcode() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().lastName("Larson").postCode(
                "LA233HQ").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(1).extracting("firstName", "lastName").containsExactly(
            Tuple.tuple("Richard", "Larson"));

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_jurorNumberAndLastNameAndPostcode() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().jurorNumber("111111229").lastName(
                "Ward").postCode("LA233HQ").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(1);

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql")
    public void searchForResponses_poolNumber() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().poolNumber("102").build(), false);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(3);

        assertResponsesSortedCorrectly(dto);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql")
    public void searchForResponses_status() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().status(Collections.singletonList(
                "CLOSED")).build(), true);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(44);
    }

    /**
     * An empty list should be treated the same as null - i.e. 'do not filter on this field'
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql")
    public void searchForResponses__status_alternatePath_emptyList() {
        assertThat(searchService.searchForResponses(
                JurorResponseSearchRequest.builder().urgentsOnly(true).status(Collections.emptyList()).build(), true)
            .getResponses()).hasSameElementsAs(
            searchService.searchForResponses(JurorResponseSearchRequest.builder().urgentsOnly(true).build(), true)
                .getResponses());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql")
    public void searchForResponses_urgentsOnly() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().urgentsOnly(true).build(), true);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(179);
        assertThat(dto.getResponses()).allMatch(
            summary -> Boolean.TRUE.equals(summary.getUrgent()) || Boolean.TRUE.equals(summary.getSuperUrgent()));
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql")
    public void searchForResponses_assignedTo() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().staffAssigned("sgomez").build(),
                true);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(179);
        assertThat(dto.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly("sgomez");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql")
    public void searchForResponses_urgentsOnly_assignedTo() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().urgentsOnly(true).staffAssigned(
                "sgomez").build(), true);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(60);
        assertThat(dto.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly("sgomez");
        assertThat(dto.getResponses()).allMatch(
            summary -> Boolean.TRUE.equals(summary.getUrgent()) || Boolean.TRUE.equals(summary.getSuperUrgent()));

    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql")
    public void searchForResponses_courtCodeOnly() {
        final JurorResponseSearchResults dto =
            searchService.searchForResponses(JurorResponseSearchRequest.builder().courtCode("448").build(), true);
        assertThat(dto).isNotNull();
        assertThat(dto.getResponses()).isNotNull().hasSize(250);

    }


    /**
     * Asserts the sort order required by the JDB-1971 AC
     *
     * <p>The list returned must be displayed on the screen in date summons response received order
     * with the oldest first.
     *
     * @param dto dto to assert against
     */
    public static void assertResponsesSortedCorrectly(JurorResponseSearchResults dto) {
        assertThat(dto.getResponses()).isSortedAccordingTo(
            Comparator.comparing(BureauResponseSummaryDto::getDateReceived));
    }
}
