package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface IReissueLetterRepository {

    List<Tuple> findLetters(ReissueLetterListRequestDto request, Consumer<JPAQuery<Tuple>> queryConsumer);

    Optional<BulkPrintData> findByJurorNumberFormCodeDatePrinted(String jurorNumber, String formCode,
                                                                 LocalDate datePrinted);

    Optional<BulkPrintData> findByJurorNumberFormCodeAndPending(String jurorNumber, String formCode);
}
