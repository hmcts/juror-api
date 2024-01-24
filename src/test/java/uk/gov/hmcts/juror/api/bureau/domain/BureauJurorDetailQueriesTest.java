package uk.gov.hmcts.juror.api.bureau.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetailQueries.buildPostcodeMatchers;

/**
 * Tests for {@link BureauJurorDetailQueries}.
 */
public class BureauJurorDetailQueriesTest {

    @Test
    public void buildPostcodeMatchers_threeCharacters() {
        assertThat(buildPostcodeMatchers("G11")).isNotNull().hasSize(1).containsOnly("G1 1");
    }

    @Test
    public void buildPostcodeMatchers_fourCharacters() {
        // "Contains the user's actual input" is covered by the main query, so shouldn't be
        // duplicated by this helper
        assertThat(buildPostcodeMatchers("G1 2")).isNotNull().hasSize(1).containsOnly("G12");

        //Second part of a postcode can't start with two digits
        assertThat(buildPostcodeMatchers("211")).isNotNull().hasSize(1).containsOnly("21 1").doesNotContain("2 11");
    }

    @Test
    public void buildPostcodeMatchers_fiveCharacters() {
        // doesNotContain is redundant given the containsOnly, but is included for explanation
        assertThat(buildPostcodeMatchers("G466U")).isNotNull().hasSize(1).containsOnly("G46 6U").doesNotContain("G466" +
            " U");
    }

    @Test
    public void buildPostcodeMatchers_sixCharacters() {
        assertThat(buildPostcodeMatchers("L233AB")).hasSize(1).containsOnly("L23 3AB");
    }

    @Test
    public void buildPostcodeMatchers_sevenCharacters() {
        assertThat(buildPostcodeMatchers("LL233AB")).hasSize(1).containsOnly("LL23 3AB");
    }
}
