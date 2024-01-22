package uk.gov.hmcts.juror.api.moj.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.util.Optional;

@RunWith(SpringRunner.class)
public class RepositoryUtilsTest {

    @Test
    public void test_unboxOptionalRecord_containsRecord() {
        PoolRequest poolRequest = new PoolRequest();
        Optional<PoolRequest> poolRequestOpt = Optional.of(poolRequest);

        Assertions.assertThat(RepositoryUtils.unboxOptionalRecord(poolRequestOpt, "new pool request"))
            .isEqualTo(poolRequest);
    }

    @Test
    public void test_unboxOptionalRecord_empty() {
        Optional<PoolRequest> poolRequestOpt = Optional.empty();

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            RepositoryUtils.unboxOptionalRecord(poolRequestOpt, "new pool request"));
    }

}
