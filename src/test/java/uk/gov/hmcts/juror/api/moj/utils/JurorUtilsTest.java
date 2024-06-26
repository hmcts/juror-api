package uk.gov.hmcts.juror.api.moj.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.exception.JurorRecordException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
public class JurorUtilsTest {

    @Mock
    JurorRepository jurorRepository;
    @Mock
    JurorPoolRepository jurorPoolRepository;

    @Test
    public void test_checkReadAccessForCurrentUser_bureauUser_bureauOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "400";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorUtils.checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, owner));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_bureauUser_courtOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "415";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorUtils.checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, "400"));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_courtUser_bureauOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "400";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorUtils.checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, "415"));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_courtUser_sameCourtOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "415";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorUtils.checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, owner));
    }

    @Test
    public void test_checkReadAccessForCurrentUser_courtUser_differentCourtOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "416";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorUtils.checkReadAccessForCurrentUser(jurorPoolRepository, jurorNumber, "415"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_bureauUser_bureauOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "400";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorUtils.checkOwnershipForCurrentUser(juror, owner));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_bureauUser_courtOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "415";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorUtils.checkOwnershipForCurrentUser(juror, "400"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_courtUser_bureauOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "400";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorUtils.checkOwnershipForCurrentUser(juror, "415"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_courtUser_sameCourtOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "415";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorUtils.checkOwnershipForCurrentUser(juror, owner));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_courtUser_differentCourtOwnedRecord() {
        String jurorNumber = "111111111";
        String owner = "416";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorUtils.checkOwnershipForCurrentUser(juror, "415"));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_bureauUser_courtOwned_alwaysAllowBureauFalse() {
        String jurorNumber = "111111111";
        String owner = "416";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            JurorUtils.checkOwnershipForCurrentUser(juror, "400",false));
    }

    @Test
    public void test_checkOwnershipForCurrentUser_bureauUser_courtOwned_alwaysAllowBureauTrue() {
        String jurorNumber = "111111111";
        String owner = "416";
        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        Juror juror = jurorPool.getJuror();

        Assertions.assertThatNoException().isThrownBy(() ->
            JurorUtils.checkOwnershipForCurrentUser(juror, "400",true));
    }

    @Test
    public void test_getActiveJurorRecord_recordExists() {
        Juror jurorOne = new Juror();
        jurorOne.setJurorNumber("111111111");

        Mockito.doReturn(Optional.of(jurorOne)).when(jurorRepository).findById("111111111");

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, "111111111");
        Assertions.assertThat(juror).isEqualTo(jurorOne);
    }

    @Test
    public void test_getActiveJurorRecord_noRecords() {
        Juror jurorOne = new Juror();
        jurorOne.setJurorNumber("111111111");

        Mockito.doReturn(Optional.empty()).when(jurorRepository).findById("333333333");

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            JurorUtils.getActiveJurorRecord(jurorRepository, "333333333"));
    }

    @Test
    public void test_checkValidJurorNumbersList_validList_Happy() {
        List<String> jurorNumbers = Arrays.asList("123456789", "987654321");
        Assertions.assertThatNoException().isThrownBy(() -> JurorUtils.validateJurorNumbers(jurorNumbers));
    }

    @Test
    public void test_checkValidJurorNumbersList_emtpyList() {
        List<String> jurorNumbers = new ArrayList<>();

        Assertions.assertThatExceptionOfType(JurorRecordException.InvalidJurorNumber.class).isThrownBy(() ->
            JurorUtils.validateJurorNumbers(jurorNumbers));
    }

    @Test
    public void test_checkValidJurorNumbersList_nullList() {
        Assertions.assertThatExceptionOfType(JurorRecordException.InvalidJurorNumber.class).isThrownBy(() ->
            JurorUtils.validateJurorNumbers(null));
    }

    @Test
    public void test_checkValidJurorNumbersList_invalidList() {
        // second number has an alphabet character 0
        List<String> jurorNumbers = Arrays.asList("123456789", "987O54321");

        Assertions.assertThatExceptionOfType(JurorRecordException.InvalidJurorNumber.class).isThrownBy(() ->
            JurorUtils.validateJurorNumbers(jurorNumbers));
    }

    @Test
    public void test_getJurorAgeAtHearingDate_lowerBoundaryCheck() {
        LocalDate dateOfBirth = LocalDate.of(1990, 6, 14);
        LocalDate serviceStartDate = LocalDate.of(2023, 6, 13);

        int age = JurorUtils.getJurorAgeAtHearingDate(dateOfBirth, serviceStartDate);

        Assertions.assertThat(age).as("Service Start Date is one day prior to juror's 33rd birthday")
            .isEqualTo(32);
    }

    @Test
    public void test_getJurorAgeAtHearingDate_upperBoundaryCheck() {
        LocalDate dateOfBirth = LocalDate.of(1990, 6, 14);
        LocalDate serviceStartDate = LocalDate.of(2023, 6, 15);

        int age = JurorUtils.getJurorAgeAtHearingDate(dateOfBirth, serviceStartDate);

        Assertions.assertThat(age).as("Service Start Date is one day after juror's 33rd birthday")
            .isEqualTo(33);
    }

    @Test
    public void test_getJurorAgeAtHearingDate_boundaryCheck() {
        LocalDate dateOfBirth = LocalDate.of(1990, 6, 14);
        LocalDate serviceStartDate = LocalDate.of(2023, 6, 14);

        int age = JurorUtils.getJurorAgeAtHearingDate(dateOfBirth, serviceStartDate);

        Assertions.assertThat(age).as("Service Start Date is on juror's 33rd birthday")
            .isEqualTo(33);
    }

    @Test
    public void test_getJurorAgeAtHearingDate_invalidDob() {
        LocalDate serviceStartDate = LocalDate.of(2023, 6, 14);

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            JurorUtils.getJurorAgeAtHearingDate(null, serviceStartDate));
    }

    @Test
    public void test_getJurorAgeAtHearingDate_invalidServiceStartDate() {
        LocalDate dateOfBirth = LocalDate.of(2023, 6, 14);

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            JurorUtils.getJurorAgeAtHearingDate(dateOfBirth, null));
    }

    @Test
    public void test_getJurorAgeAtHearingDate_bothArgumentsInvalid() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            JurorUtils.getJurorAgeAtHearingDate(null, null));
    }

    private JurorPool createJurorPool(String jurorNumber, String owner) {
        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setAssociatedPools(Set.of(jurorPool));

        jurorPool.setJuror(juror);

        return jurorPool;
    }


}
