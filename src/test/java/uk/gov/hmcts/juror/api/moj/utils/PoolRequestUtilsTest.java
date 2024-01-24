package uk.gov.hmcts.juror.api.moj.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Optional;

@RunWith(SpringRunner.class)
public class PoolRequestUtilsTest {

    @Mock
    PoolRequestRepository poolRequestRepository;


    @Test
    public void test_getActivePoolRecord_poolRecordExists() {
        String poolNumber = "415230101";
        String courtOwner = "415";

        PoolRequest poolRequest = createPoolRequest(poolNumber, courtOwner);

        Mockito.doReturn(Optional.of(poolRequest)).when(poolRequestRepository)
            .findByPoolNumber(poolNumber);

        Assertions.assertThat(PoolRequestUtils.getActivePoolRecord(poolRequestRepository, poolNumber))
            .isEqualTo(poolRequest);
    }

    @Test
    public void test_getActivePoolRecord_poolRecordDoesNotExist() {
        String poolNumber = "415230101";
        String courtOwner = "415";

        Mockito.doReturn(Optional.empty()).when(poolRequestRepository)
            .findByPoolNumber(poolNumber);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            PoolRequestUtils.getActivePoolRecord(poolRequestRepository, poolNumber));
    }

    private PoolRequest createPoolRequest(String poolNumber, String owner) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setOwner(owner);
        poolRequest.setPoolNumber(poolNumber);
        return poolRequest;
    }


}
