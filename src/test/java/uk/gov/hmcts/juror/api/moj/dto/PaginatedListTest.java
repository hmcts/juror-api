package uk.gov.hmcts.juror.api.moj.dto;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginatedListTest extends AbstractValidatorTest<PaginatedList<String>> {
    @Override
    protected PaginatedList<String> createValidObject() {
        PaginatedList<String> paginatedList = new PaginatedList<>();
        paginatedList.setCurrentPage(1L);
        paginatedList.setTotalPages(1L);
        paginatedList.setTotalItems(1L);
        return paginatedList;
    }

    @Nested
    class CurrentPageTest extends AbstractValidationFieldTestLong {
        protected CurrentPageTest() {
            super("currentPage", PaginatedList::setCurrentPage);
            addMin(1L, null);
            addRequiredTest(null);
        }
    }

    @Nested
    class TotalPagesTest extends AbstractValidationFieldTestLong {
        protected TotalPagesTest() {
            super("totalPages", PaginatedList::setTotalPages);
            addMin(1L, null);
            addRequiredTest(null);
        }
    }

    @Nested
    class TotalItemsTest extends AbstractValidationFieldTestLong {
        protected TotalItemsTest() {
            super("totalItems", PaginatedList::setTotalItems);
            addMin(0L, null);
            addRequiredTest(null);
        }
    }


    @Nested
    class DataTest extends AbstractValidationFieldTestList<String> {
        protected DataTest() {
            super("data", PaginatedList::setData);
            addNotRequiredTest(null);
            addNullValueInListTest(null);
        }
    }

    @Test
    void positiveSetTotalItemsTypicalMultiplePagesOnLimit() {
        PaginatedList<String> list = createValidObject();
        list.setTotalItems(100, 25);
        assertThat(list.getTotalItems()).isEqualTo(100L);
        assertThat(list.getTotalPages()).isEqualTo(4L);
    }

    @Test
    void positiveSetTotalItemsTypicalMultiplePagesBelowLimit() {
        PaginatedList<String> list = createValidObject();
        list.setTotalItems(100, 24);
        assertThat(list.getTotalItems()).isEqualTo(100L);
        assertThat(list.getTotalPages()).isEqualTo(5L);
    }

    @Test
    void positiveSetTotalItemsTypicalSinglePageOnLimit() {
        PaginatedList<String> list = createValidObject();
        list.setTotalItems(25, 25);
        assertThat(list.getTotalItems()).isEqualTo(25L);
        assertThat(list.getTotalPages()).isEqualTo(1L);
    }

    @Test
    void positiveSetTotalItemsTypicalSingleBelowLimit() {
        PaginatedList<String> list = createValidObject();
        list.setTotalItems(20, 25);
        assertThat(list.getTotalItems()).isEqualTo(20L);
        assertThat(list.getTotalPages()).isEqualTo(1L);
    }

    @Test
    void positiveSetTotalItemsZeroTotalItems() {
        PaginatedList<String> list = createValidObject();
        list.setTotalItems(0, 25);
        assertThat(list.getTotalItems()).isEqualTo(0L);
        assertThat(list.getTotalPages()).isEqualTo(0L);

    }

    @Test
    void positiveIsEmptyTrueEmptyList() {
        PaginatedList<String> list = createValidObject();
        list.setData(List.of());
        assertThat(list.isEmpty()).isTrue();
    }

    @Test
    void positiveIsEmptyTrueNullList() {
        PaginatedList<String> list = createValidObject();
        list.setData(null);
        assertThat(list.isEmpty()).isTrue();
    }

    @Test
    void positiveIsEmptyFalse() {
        PaginatedList<String> list = createValidObject();
        list.setData(List.of("Any"));
        assertThat(list.isEmpty()).isFalse();
    }
}
