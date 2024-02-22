package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.PaymentData;

import java.util.List;

@Repository
public interface PaymentDataRepository extends CrudRepository<PaymentData, String> {

    List<PaymentData> findByJurorNumber(String jurorNumber);

}
