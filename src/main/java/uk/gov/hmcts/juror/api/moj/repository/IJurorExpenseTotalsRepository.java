package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IJurorExpenseTotalsRepository {

    List<JurorExpenseTotals> findUnpaidByCourtLocationCode(String locCode, Pageable pageable);

    List<JurorExpenseTotals> findUnpaidByCourtLocationCodeAndAppearanceDate(String locCode, LocalDate minDate,
                                                                            LocalDate maxDate, Pageable pageable);

    long countUnpaidByCourtLocationCodeAndAppearanceDate(String locCode, LocalDate minDate, LocalDate maxDate);

}
