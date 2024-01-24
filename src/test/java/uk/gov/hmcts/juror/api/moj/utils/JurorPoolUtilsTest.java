package uk.gov.hmcts.juror.api.moj.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.JurorRecordException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
public class JurorPoolUtilsTest {

    @Mock
    JurorPoolRepository jurorPoolPoolRepository;

    @Test
    public void test_checkReadAccessForCurrentUser_bureauUser_bureauOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "400");

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, "400"));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_bureauUser_courtOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "415");

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, "400"));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_courtUser_bureauOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "400");

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, "415"));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_courtUser_sameCourtOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "415");

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, "415"));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_courtUser_differentCourtOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "416");

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.checkReadAccessForCurrentUser(jurorPool, "415"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_bureauUser_bureauOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "400");

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, "400"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_bureauUser_courtOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "415");

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, "400"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_courtUser_bureauOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "400");

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, "415"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_courtUser_sameCourtOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "415");

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, "415"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_courtUser_differentCourtOwnedRecord() {
        JurorPool jurorPool = createJurorPool("111111111", "416");

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, "415"));
    }

    @Test
    public void test_getActiveJurorPoolRecords_multipleRecords() {
        JurorPool jurorPoolOne = createJurorPool("111111111", "457");
        JurorPool jurorPoolTwo = createJurorPool("111111111", "415");
        JurorPool jurorPoolThree = createJurorPool("222222222", "415");

        Mockito.doReturn(Arrays.asList(jurorPoolOne, jurorPoolTwo)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive("111111111", true);

        List<JurorPool> jurorPools = JurorPoolUtils.getActiveJurorPoolRecords(jurorPoolPoolRepository, "111111111");
        Assertions.assertThat(jurorPools.size()).isEqualTo(2);
        Assertions.assertThat(jurorPools.contains(jurorPoolOne)).isTrue();
        Assertions.assertThat(jurorPools.contains(jurorPoolTwo)).isTrue();
        Assertions.assertThat(jurorPools.contains(jurorPoolThree)).isFalse();
    }

    @Test
    public void test_getActiveJurorPoolRecords_singleRecord() {
        JurorPool jurorPoolOne = createJurorPool("111111111", "457");
        JurorPool jurorPoolTwo = createJurorPool("222222222", "457");

        Mockito.doReturn(Collections.singletonList(jurorPoolOne)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive("111111111", true);

        List<JurorPool> jurorPools = JurorPoolUtils.getActiveJurorPoolRecords(jurorPoolPoolRepository, "111111111");
        Assertions.assertThat(jurorPools.size()).isEqualTo(1);
        Assertions.assertThat(jurorPools.contains(jurorPoolOne)).isTrue();
        Assertions.assertThat(jurorPools.contains(jurorPoolTwo)).isFalse();
    }

    @Test
    public void test_getActiveJurorPoolRecords_noRecords() {
        JurorPool jurorPoolOne = createJurorPool("111111111", "457");
        JurorPool jurorPoolTwo = createJurorPool("222222222", "457");

        Mockito.doReturn(new ArrayList<JurorPool>()).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive("333333333", true);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            JurorPoolUtils.getActiveJurorPoolRecords(jurorPoolPoolRepository, "333333333"));
    }

    @Test
    public void test_getActiveJurorRecord_singleRecord() {
        JurorPool jurorPoolOne = createJurorPool("111111111", "457");
        JurorPool jurorPoolTwo = createJurorPool("222222222", "457");

        Mockito.doReturn(Collections.singletonList(jurorPoolOne)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive("111111111", true);

        Assertions.assertThat(JurorPoolUtils.getActiveJurorRecord(jurorPoolPoolRepository, "111111111"))
            .isEqualTo(jurorPoolOne.getJuror());
    }

    @Test
    public void test_getActiveJurorRecord_noRecords() {
        JurorPool jurorPoolOne = createJurorPool("111111111", "457");
        JurorPool jurorPoolTwo = createJurorPool("222222222", "457");

        Mockito.doReturn(new ArrayList<JurorPool>()).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive("333333333", true);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            JurorPoolUtils.getActiveJurorRecord(jurorPoolPoolRepository, "333333333"));
    }

    @Test
    public void test_getActiveJurorRecord_multipleRecords() {
        JurorPool jurorPoolOne = createJurorPool("111111111", "457");
        JurorPool jurorPoolTwo = createJurorPool("111111111", "415");

        Mockito.doReturn(Arrays.asList(jurorPoolOne, jurorPoolTwo)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive("111111111", true);

        Assertions.assertThatExceptionOfType(JurorRecordException.MultipleJurorRecordsFound.class).isThrownBy(() ->
            JurorPoolUtils.getActiveJurorRecord(jurorPoolPoolRepository, "111111111"));
    }

    @Test
    public void test_checkMultipleRecordReadAccess_bureauUser_bureauOwnedRecord() {
        String jurorNumber = "111111111";
        String bureauOwnerCode = "400";
        JurorPool jurorPool = createJurorPool(jurorNumber, bureauOwnerCode);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolPoolRepository, jurorNumber, bureauOwnerCode));
    }

    @Test
    public void test_checkMultipleRecordReadAccess_bureauUser_multipleCourtOwnedRecords() {
        String jurorNumber = "111111111";
        String bureauOwnerCode = "400";
        JurorPool jurorPoolOne = createJurorPool(jurorNumber, "415");
        JurorPool jurorPoolTwo = createJurorPool(jurorNumber, "411");

        Mockito.doReturn(Arrays.asList(jurorPoolOne, jurorPoolTwo)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolPoolRepository, jurorNumber, bureauOwnerCode));
    }

    @Test
    public void test_checkMultipleRecordReadAccess_courtUser_bureauOwnedRecord() {
        String jurorNumber = "111111111";
        String bureauOwnerCode = "400";
        JurorPool jurorPool = createJurorPool(jurorNumber, bureauOwnerCode);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolPoolRepository, jurorNumber, "415"));
    }

    @Test
    public void test_checkMultipleRecordReadAccess_courtUser_multipleCourtOwnedRecords_ownsOne() {
        String jurorNumber = "111111111";
        JurorPool jurorPoolOne = createJurorPool(jurorNumber, "415");
        JurorPool jurorPoolTwo = createJurorPool(jurorNumber, "411");

        Mockito.doReturn(Arrays.asList(jurorPoolOne, jurorPoolTwo)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolPoolRepository, jurorNumber, "415"));
    }

    @Test
    public void test_checkMultipleRecordReadAccess_courtUser_multipleCourtOwnedRecords_ownsNeither() {
        String jurorNumber = "111111111";
        JurorPool jurorPoolOne = createJurorPool(jurorNumber, "415");
        JurorPool jurorPoolTwo = createJurorPool(jurorNumber, "411");

        Mockito.doReturn(Arrays.asList(jurorPoolOne, jurorPoolTwo)).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.checkMultipleRecordReadAccess(jurorPoolPoolRepository, jurorNumber, "435"));
    }

    @Test
    public void test_getActiveJurorPoolForUser_bureauUser_bureauOwned() {
        String jurorNumber = "111111111";
        JurorPool jurorPool = createJurorPool(jurorNumber, "400");
        List<JurorPool> jurorPools = Collections.singletonList(jurorPool);
        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        JurorPool jurorForUser = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "400");
        Assertions.assertThat(jurorForUser)
            .as("Expect the bureau owned juror pool record to be returned")
            .isEqualTo(jurorPool);
    }

    @Test
    public void test_getActiveJurorPoolForUser_bureauUser_singleCourtOwned() {
        String jurorNumber = "111111111";
        JurorPool jurorPool = createJurorPool(jurorNumber, "415");
        List<JurorPool> jurorPools = Collections.singletonList(jurorPool);
        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        JurorPool jurorForUser = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "400");
        Assertions.assertThat(jurorForUser)
            .as("Expect the bureau owned juror pool record to be returned")
            .isEqualTo(jurorPool);
    }

    @Test
    public void test_getActiveJurorPoolForUser_bureauUser_multipleCourtOwned() {
        String jurorNumber = "111111111";
        JurorPool jurorPool1 = createJurorPool(jurorNumber, "415");
        JurorPool jurorPool2 = createJurorPool(jurorNumber, "416");
        List<JurorPool> jurorPools = Arrays.asList(jurorPool1, jurorPool2);

        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        JurorPool jurorForUser = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "400");

        Assertions.assertThat(jurorForUser)
            .as("Expect a single active juror pool record to be returned")
            .isNotNull();
        Assertions.assertThat(jurorPools)
            .as("Expect one of the juror pool records to be returned from the source list")
            .contains(jurorForUser);
    }

    @Test
    public void test_getActiveJurorPoolForUser_bureauUser_noJurorPool() {
        String jurorNumber = "111111111";

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "400"));
    }


    @Test
    public void test_getActiveJurorPoolForUser_courtUser_bureauOwned() {
        String jurorNumber = "111111111";
        JurorPool jurorPool = createJurorPool(jurorNumber, "400");
        List<JurorPool> jurorPools = Collections.singletonList(jurorPool);
        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "415"));
    }

    @Test
    public void test_getActiveJurorPoolForUser_courtUser_singleCourtOwned_withAccess() {
        String jurorNumber = "111111111";
        JurorPool jurorPool = createJurorPool(jurorNumber, "415");
        List<JurorPool> jurorPools = Collections.singletonList(jurorPool);

        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        JurorPool jurorForUser = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "415");
        Assertions.assertThat(jurorForUser)
            .as("Expect the bureau owned juror pool record to be returned")
            .isEqualTo(jurorPool);
    }

    @Test
    public void test_getActiveJurorPoolForUser_courtUser_singleCourtOwned_noAccess() {
        String jurorNumber = "111111111";
        JurorPool jurorPool = createJurorPool(jurorNumber, "416");
        List<JurorPool> jurorPools = Collections.singletonList(jurorPool);
        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "415"));
    }

    @Test
    public void test_getActiveJurorPoolForUser_courtUser_multipleCourtOwned() {
        String jurorNumber = "111111111";
        JurorPool jurorPool1 = createJurorPool(jurorNumber, "415");
        JurorPool jurorPool2 = createJurorPool(jurorNumber, "416");
        List<JurorPool> jurorPools = Arrays.asList(jurorPool1, jurorPool2);

        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        JurorPool jurorForUser = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "415");

        Assertions.assertThat(jurorForUser)
            .as("Expect the juror pool record with the matching owner value to be returned")
            .isEqualTo(jurorPool1);
    }

    @Test
    public void test_getActiveJurorPoolForUser_courtUser_noJurorPool() {
        String jurorNumber = "111111111";

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolPoolRepository, jurorNumber, "415"));
    }

    @Test
    public void test_getSingleActiveJurorPool_singleJurorPool() {
        String jurorNumber = "111111111";
        String owner = "400";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        List<JurorPool> jurorPools = Collections.singletonList(jurorPool);
        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        JurorPool jurorForUser = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolPoolRepository, jurorNumber);
        Assertions.assertThat(jurorForUser)
            .as("Expect the juror pool record to be returned successfully")
            .isEqualTo(jurorPool);
    }

    @Test
    public void test_getSingleActiveJurorPool_noJurorPools() {
        String jurorNumber = "111111111";

        Mockito.doReturn(new ArrayList<>()).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            JurorPoolUtils.getSingleActiveJurorPool(jurorPoolPoolRepository, jurorNumber));
    }

    @Test
    public void test_getSingleActiveJurorPool_multipleJurorPools() {
        String jurorNumber = "111111111";
        String owner = "400";

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createJurorPool(jurorNumber, owner));
        jurorPools.add(createJurorPool(jurorNumber, owner));

        Mockito.doReturn(jurorPools).when(jurorPoolPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(JurorRecordException.MultipleJurorRecordsFound.class).isThrownBy(() ->
            JurorPoolUtils.getSingleActiveJurorPool(jurorPoolPoolRepository, jurorNumber));
    }

    private JurorPool createJurorPool(String jurorNumber, String owner) {
        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        PoolRequest pool = new PoolRequest();
        pool.setPoolNumber("123456789");

        jurorPool.setJuror(juror);
        jurorPool.setPool(pool);

        return jurorPool;
    }


}
