package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class BulkServiceImplTest {

    private BulkServiceImpl bulkService;

    @BeforeEach
    void beforeEach() {
        this.bulkService = new BulkServiceImpl();
    }

    @Test
    void positiveProcess() {
        List<String> inputList = List.of("A", "B", "C", "ABC");
        Map<String, String> outputList = Map.of(
            "A", "A1",
            "B", "B12",
            "C", "C3",
            "ABC", "Some Value",
            "ABC4", "Unused value"
        );
        Function<String, String> function = outputList::get;

        assertEquals(
            List.of("A1", "B12", "C3", "Some Value"),
            this.bulkService.process(inputList, function),
            "Function should be called to map outputs"
        );
    }

    @Test
    void positiveProcessZeroInputSize() {
        Function<String, String> function = s -> fail("Should not be called");
        assertEquals(List.of(), this.bulkService.process(List.of(), function),
            "If no inputs there should be no outputs");
    }
}
