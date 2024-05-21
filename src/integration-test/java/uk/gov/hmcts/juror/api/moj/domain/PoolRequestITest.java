package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql({"/db/mod/truncate.sql","/db/PoolRequest_initPoolTypes.sql"})
public class PoolRequestITest extends ContainerTest {

    @Autowired
    CourtLocationRepository courtLocationRepository;
    @Autowired
    PoolRequestRepository poolRequestRepository;
    @Autowired
    PoolTypeRepository poolTypeRepository;


    @Test
    @Sql(statements = "DELETE FROM JUROR_MOD.POOL_HISTORY")
    @Sql(statements = "DELETE FROM JUROR_MOD.POOL_COMMENTS")
    @Sql(statements = "DELETE FROM JUROR_MOD.JUROR_POOL")
    @Sql(statements = "DELETE FROM JUROR_MOD.POOL")
    public void test_poolRequest_crudLifeCycle() {
        // CREATE
        Optional<CourtLocation> courtLocation = courtLocationRepository.findByLocCode("401");
        Optional<PoolType> poolType = poolTypeRepository.findById("CRO");
        PoolRequest expectedPoolRequest = new PoolRequest();
        expectedPoolRequest.setPoolNumber("401220801");
        courtLocation.ifPresent(expectedPoolRequest::setCourtLocation);
        poolType.ifPresent(expectedPoolRequest::setPoolType);
        expectedPoolRequest.setOwner("401");
        expectedPoolRequest.setReturnDate(LocalDate.of(2022, 10, 31));
        poolRequestRepository.saveAndFlush(expectedPoolRequest);

        // READ
        PoolRequest actualPoolRequest = RepositoryUtils.retrieveFromDatabase("401220801", poolRequestRepository);
        assertThat(actualPoolRequest.getPoolNumber())
            .as("Expect Pool Number values to match for Actual and Expected Pool Requests")
            .isEqualTo(expectedPoolRequest.getPoolNumber());
        assertThat(actualPoolRequest.getOwner())
            .as("Expect Owner values to match for Actual and Expected Pool Requests")
            .isEqualTo(expectedPoolRequest.getOwner());
        assertThat(actualPoolRequest.getPoolType().getPoolType())
            .as("Expect Pool Type values to match for Actual and Expected Pool Requests")
            .isEqualTo(expectedPoolRequest.getPoolType().getPoolType());
        assertThat(actualPoolRequest.getCourtLocation().getLocCode())
            .as("Expect Court Location values to match for Actual and Expected Pool Requests")
            .isEqualTo(expectedPoolRequest.getCourtLocation().getLocCode());
        assertThat(actualPoolRequest.getReturnDate())
            .as("Expect Return Date values to match for Actual and Expected Pool Requests")
            .isEqualTo(expectedPoolRequest.getReturnDate());
        assertThat(actualPoolRequest.getLastUpdate())
            .as("Expect Last Update value to be set by lifecycle event callback")
            .isNotNull();

        // UPDATE
        assertThat(actualPoolRequest.getNumberRequested())
            .as("Expect Number Requested value to allow nulls")
            .isNull();
        assertThat(actualPoolRequest.getNewRequest())
            .as("Expect New Request value to be defaulted to 'Y''")
            .isEqualTo('Y');
        assertThat(actualPoolRequest.getAdditionalSummons())
            .as("Expect Additional Summons value to be null if not set")
            .isNull();

        actualPoolRequest.setNumberRequested(100);
        actualPoolRequest.setNewRequest('T');
        actualPoolRequest.setAdditionalSummons(50);
        poolRequestRepository.saveAndFlush(actualPoolRequest);

        actualPoolRequest = RepositoryUtils.retrieveFromDatabase("401220801", poolRequestRepository);
        assertThat(actualPoolRequest.getNumberRequested())
            .as("Expect Number Requested value to be updated to 100")
            .isEqualTo(100);
        assertThat(actualPoolRequest.getNewRequest())
            .as("Expect New Request value to be updated to 'T'")
            .isEqualTo('T');
        assertThat(actualPoolRequest.getAdditionalSummons())
            .as("Expect Additional Summons value to be updated to 50")
            .isEqualTo(50);

        // DELETE
        poolRequestRepository.delete(actualPoolRequest);
        assertThat(poolRequestRepository.findById("401220801").isPresent())
            .as("Expect Pool Request to be removed, and no longer present in the database")
            .isFalse();
    }

}
