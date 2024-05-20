package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("PMD.TooManyMethods")
class CsvBuilderTest {

    private CsvBuilder csvBuilder;

    @BeforeEach
    void setUp() {
        csvBuilder = new CsvBuilder(List.of(
            "title1",
            "title2",
            "title3"
        ));
    }

    @Test
    void positiveConstructor() {
        assertThat(csvBuilder.rows.size()).isEqualTo(1);
        assertThat(csvBuilder.expectedLength).isEqualTo(3);
        assertThat(csvBuilder.rows.get(0))
            .containsExactly("title1", "title2", "title3");
    }

    @Test
    void positiveBuildEmpty() {
        assertThat(csvBuilder.build()).isEqualTo("title1,title2,title3");
    }

    @Test
    void positiveAddRow() {
        csvBuilder.addRow(List.of(
            "data1",
            "data2",
            "data3"
        ));
        assertThat(csvBuilder.rows.size()).isEqualTo(2);
        assertThat(csvBuilder.rows.get(1))
            .containsExactly("data1", "data2", "data3");

        csvBuilder.addRow(List.of(
            "data4",
            "data5",
            "data6"
        ));

        assertThat(csvBuilder.rows.size()).isEqualTo(3);
        assertThat(csvBuilder.rows.get(2))
            .containsExactly("data4", "data5", "data6");
    }

    @Test
    void negativeAddRow() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> csvBuilder.addRow(List.of(
                "data1",
                "data2"
            )),
            "Row length does not match expected length"
        );
        assertThat(exception.getMessage()).isEqualTo("Row length does not match expected length");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void positiveBuild() {
        csvBuilder.addRow(List.of(
            "data1",
            "data2",
            "data3"
        ));
        csvBuilder.addRow(List.of(
            "data4",
            "data5",
            "data6"
        ));
        assertThat(csvBuilder.build()).isEqualTo("title1,title2,title3\ndata1,data2,data3\ndata4,data5,data6");
    }


    @Test
    void positiveEscapeSpecialCharacters() {
        assertThat(csvBuilder.escapeSpecialCharacters("data")).isEqualTo("data");
        assertThat(csvBuilder.escapeSpecialCharacters("data,data")).isEqualTo("\"data,data\"");
        assertThat(csvBuilder.escapeSpecialCharacters("data\"data")).isEqualTo("\"data\"\"data\"");
        assertThat(csvBuilder.escapeSpecialCharacters("data\ndata")).isEqualTo("data data");
    }

    @Test
    void positiveEscapeSpecialCharactersNull() {
        assertThat(csvBuilder.escapeSpecialCharacters(null)).isEqualTo("null");
    }

    @Test
    void positiveEscapeSpecialCharactersEmpty() {
        assertThat(csvBuilder.escapeSpecialCharacters("")).isEqualTo("");
    }

    @Test
    void positiveEscapeSpecialCharactersEmptySpace() {
        assertThat(csvBuilder.escapeSpecialCharacters(" ")).isEqualTo(" ");
    }

    @Test
    void positiveEscapeSpecialCharactersEmptyComma() {
        assertThat(csvBuilder.escapeSpecialCharacters(",")).isEqualTo("\",\"");
    }

    @Test
    void positiveEscapeSpecialCharactersEmptyQuote() {
        assertThat(csvBuilder.escapeSpecialCharacters("\"")).isEqualTo("\"\"\"\"");
    }

    @Test
    void positiveEscapeSpecialCharactersEmptyApostrophe() {
        assertThat(csvBuilder.escapeSpecialCharacters("'")).isEqualTo("\"'\"");
    }
}
