package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactEnquiryTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
public class JurorAuditChangeServiceTest {

    @Mock
    JurorHistoryRepository jurorHistoryRepository;
    @Mock
    ContactCodeRepository contactCodeRepository;
    @Mock
    ContactLogRepository contactLogRepository;
    @Mock
    ContactEnquiryTypeRepository contactEnquiryTypeRepository;
    @Mock
    JurorPoolRepository jurorPoolRepository;

    @InjectMocks
    JurorAuditChangeServiceImpl jurorAuditChangeService;

    @Before
    public void setUpMocks() {
        Mockito.doReturn(null).when(jurorHistoryRepository).save(Mockito.any());
    }

    public void test_initChangedPropertyMap_nameDetails_noChanges() {
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        JurorNameDetailsDto dto = new JurorNameDetailsDto(null, "Test", "Person");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, dto);

        Assertions.assertThat(changedProperties.size()).isEqualTo(3);

        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("No changes expected for property: %s", key)).isFalse();
        }
    }

    public void test_initChangedPropertyMap_nameDetails_titleChanged() {
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "Test", "Person");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, dto);

        Assertions.assertThat(changedProperties.size()).isEqualTo(3);
        Assertions.assertThat(changedProperties.get("title")).isTrue();
        Assertions.assertThat(changedProperties.get("first Name")).isFalse();
        Assertions.assertThat(changedProperties.get("last Name")).isFalse();
    }

    public void test_initChangedPropertyMap_nameDetails_firstNameChanged() {
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        JurorNameDetailsDto dto = new JurorNameDetailsDto(null, "First", "Person");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, dto);

        Assertions.assertThat(changedProperties.size()).isEqualTo(3);
        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("first Name")).isTrue();
        Assertions.assertThat(changedProperties.get("last Name")).isFalse();
    }

    public void test_initChangedPropertyMap_nameDetails_lastNameChanged() {
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        JurorNameDetailsDto dto = new JurorNameDetailsDto(null, "Test", "Last");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, dto);

        Assertions.assertThat(changedProperties.size()).isEqualTo(3);
        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("first Name")).isFalse();
        Assertions.assertThat(changedProperties.get("last Name")).isTrue();
    }

    public void test_initChangedPropertyMap_nameDetails_allChanged() {
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mrs", "First", "Last");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, dto);

        Assertions.assertThat(changedProperties.size()).isEqualTo(3);

        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("Changes expected for property: %s", key)).isTrue();
        }
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_noChanges() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("No changes expected for property: %s", key)).isFalse();
        }
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_titleChanged_noNameChange() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());
        pojo.setTitle("Dr");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isTrue();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isFalse();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_titleChanged_firstNameChanged() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());
        pojo.setTitle("Dr");
        pojo.setFirstName("First");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("No changes expected for property: %s", key)).isFalse();
        }
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_titleChanged_lastNameChanged() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());
        pojo.setTitle("Dr");
        pojo.setLastName("Last");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("No changes expected for property: %s", key)).isFalse();
        }
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_titleChanged_bothNamesChanged() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());
        pojo.setTitle("Dr");
        pojo.setFirstName("First");
        pojo.setLastName("Last");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("No changes expected for property: %s", key)).isFalse();
        }
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_noTitleChange_bothNamesChanged() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());
        pojo.setFirstName("First");
        pojo.setLastName("Last");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("No changes expected for property: %s", key)).isFalse();
        }
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_dobChanged() {
        final LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());
        pojo.setDateOfBirth(dob);

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isTrue();
        Assertions.assertThat(changedProperties.get("address")).isFalse();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_addressChanged_addressLine1() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1("Some new value");
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isTrue();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_addressChanged_addressLine2() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2("Some new value");
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isTrue();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_addressChanged_addressLine3() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3("Some new value");
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isTrue();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_addressChanged_addressLine4() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4("Some new value");
        pojo.setAddressLine5(juror.getAddressLine5());

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isTrue();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_addressChanged_addressLine5() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5("Some new value");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isTrue();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_addressChanged_allChanged() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1("Some");
        pojo.setAddressLine2("New");
        pojo.setAddressLine3("Address");
        pojo.setAddressLine4("Line");
        pojo.setAddressLine5("Values");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isTrue();
        Assertions.assertThat(changedProperties.get("postcode")).isFalse();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_postcodeChanged() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        juror.setDateOfBirth(dob);

        AbstractJurorResponse pojo = new PaperResponse();
        BeanUtils.copyProperties(juror, pojo);
        pojo.setAddressLine1(juror.getAddressLine1());
        pojo.setAddressLine2(juror.getAddressLine2());
        pojo.setAddressLine3(juror.getAddressLine3());
        pojo.setAddressLine4(juror.getAddressLine4());
        pojo.setAddressLine5(juror.getAddressLine5());
        pojo.setPostcode("W12 5HQ");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);

        Assertions.assertThat(changedProperties.get("title")).isFalse();
        Assertions.assertThat(changedProperties.get("date Of Birth")).isFalse();
        Assertions.assertThat(changedProperties.get("address")).isFalse();
        Assertions.assertThat(changedProperties.get("postcode")).isTrue();
    }

    @Test
    public void test_initChangedPropertyMap_jurorResponse_allChanged() {
        LocalDate dob = LocalDate.of(1990, 7, 1);
        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();

        AbstractJurorResponse pojo = new PaperResponse();
        pojo.setTitle("Mr");
        pojo.setFirstName("Test");
        pojo.setLastName("Person");
        pojo.setDateOfBirth(dob);
        pojo.setAddressLine1("Some");
        pojo.setAddressLine2("New");
        pojo.setAddressLine3("Address");
        pojo.setAddressLine4("Line");
        pojo.setAddressLine5("Values");
        pojo.setPostcode("W12 5HQ");

        Map<String, Boolean> changedProperties = jurorAuditChangeService.initChangedPropertyMap(juror, pojo);

        Assertions.assertThat(changedProperties.size()).isEqualTo(4);
        for (String key : changedProperties.keySet()) {
            Assertions.assertThat(changedProperties.get(key))
                .as(String.format("Changes expected for property: %s", key)).isTrue();
        }
    }

    @Test
    public void test_hasTitleChanged_noChange_nonNull() {
        String originalTitle = "Mr";
        String newTitle = "Mr";

        Assertions.assertThat(jurorAuditChangeService.hasTitleChanged(newTitle, originalTitle)).isFalse();
    }

    @Test
    public void test_hasTitleChanged_noChange_null() {
        Assertions.assertThat(jurorAuditChangeService.hasTitleChanged(null, null)).isFalse();
    }

    @Test
    public void test_hasTitleChanged_changed_fromNull() {
        String newTitle = "Mr";

        Assertions.assertThat(jurorAuditChangeService.hasTitleChanged(newTitle, null)).isTrue();
    }

    @Test
    public void test_hasTitleChanged_changed_toNull() {
        String originalTitle = "Mr";

        Assertions.assertThat(jurorAuditChangeService.hasTitleChanged(null, originalTitle)).isTrue();
    }

    @Test
    public void test_hasTitleChanged_changed() {
        String originalTitle = "Mrs";
        String newTitle = "Dr";

        Assertions.assertThat(jurorAuditChangeService.hasTitleChanged(newTitle, originalTitle)).isTrue();
    }

    @Test
    public void test_hasNameChanged_noChange() {
        String originalFirstName = "Test";
        String originalLastName = "Person";

        String newFirstName = originalFirstName;
        String newLastName = originalLastName;

        Assertions.assertThat(jurorAuditChangeService.hasNameChanged(newFirstName, originalFirstName,
            newLastName, originalLastName)).isFalse();
    }

    @Test
    public void test_hasNameChanged_firstNameChanged() {
        String originalFirstName = "Test";
        String originalLastName = "Person";

        String newFirstName = "First";
        String newLastName = originalLastName;

        Assertions.assertThat(jurorAuditChangeService.hasNameChanged(newFirstName, originalFirstName,
            newLastName, originalLastName)).isTrue();
    }

    @Test
    public void test_hasNameChanged_lastNameChanged() {
        String originalFirstName = "Test";
        String originalLastName = "Person";

        String newFirstName = originalFirstName;
        String newLastName = "Last";

        Assertions.assertThat(jurorAuditChangeService.hasNameChanged(newFirstName, originalFirstName,
            newLastName, originalLastName)).isTrue();
    }

    @Test
    public void test_recordApprovalHistoryEvent_approved() {
        ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        String username = "Court_User";

        Mockito.doReturn(null).when(jurorHistoryRepository).save(Mockito.any());

        jurorAuditChangeService.recordApprovalHistoryEvent(juror.getJurorNumber(), ApprovalDecision.APPROVE, username,
            jurorPool.getPoolNumber());

        Mockito.verify(jurorHistoryRepository, Mockito.times(1))
            .save(jurorHistoryArgumentCaptor.capture());
        JurorHistory jurorHistory = jurorHistoryArgumentCaptor.getValue();

        Assertions.assertThat(jurorHistory.getJurorNumber()).isEqualTo(juror.getJurorNumber());
        Assertions.assertThat(jurorHistory.getPoolNumber()).isEqualTo(jurorPool.getPoolNumber());
        Assertions.assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
        Assertions.assertThat(jurorHistory.getOtherInformation()).isEqualTo("Name change "
            + ApprovalDecision.APPROVE.getDescription());
        Assertions.assertThat(jurorHistory.getCreatedBy()).isEqualTo(username);
    }

    @Test
    public void test_recordApprovalHistoryEvent_rejected() {
        ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

        JurorPool jurorPool = createJurorPool("415");
        Juror juror = jurorPool.getJuror();
        String username = "Court_User";

        Mockito.doReturn(null).when(jurorHistoryRepository).save(Mockito.any());

        jurorAuditChangeService.recordApprovalHistoryEvent(juror.getJurorNumber(), ApprovalDecision.REJECT, username,
            jurorPool.getPoolNumber());

        Mockito.verify(jurorHistoryRepository, Mockito.times(1))
            .save(jurorHistoryArgumentCaptor.capture());
        JurorHistory jurorHistory = jurorHistoryArgumentCaptor.getValue();

        Assertions.assertThat(jurorHistory.getJurorNumber()).isEqualTo(juror.getJurorNumber());
        Assertions.assertThat(jurorHistory.getPoolNumber()).isEqualTo(jurorPool.getPoolNumber());
        Assertions.assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
        Assertions.assertThat(jurorHistory.getOtherInformation()).isEqualTo("Name change "
            + ApprovalDecision.REJECT.getDescription());
        Assertions.assertThat(jurorHistory.getCreatedBy()).isEqualTo(username);
    }

    @Test
    public void test_recordContactLog() {
        final ArgumentCaptor<ContactLog> contactLogArgumentCaptor = ArgumentCaptor.forClass(ContactLog.class);

        JurorPool jurorPool = createJurorPool("415");
        final Juror juror = jurorPool.getJuror();
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(jurorPool);
        final String username = "Court_User";
        final String notes = "Approved juror's change of name. Some additional information";
        final String enquiryCode = "CN";

        ContactEnquiryType contactEnquiryType = new ContactEnquiryType(ContactEnquiryCode.CN, "Change of name");

        Mockito.doReturn(Optional.of(contactEnquiryType)).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.CN);
        Mockito.doReturn(null).when(contactLogRepository).saveAndFlush(Mockito.any());
        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(juror.getJurorNumber(), true);

        ContactCode contactCode = new ContactCode(
            IContactCode.CHANGE_OF_NAME.getCode(),
            IContactCode.CHANGE_OF_NAME.getDescription());
        Mockito.when(contactCodeRepository.findById(
            IContactCode.CHANGE_OF_NAME.getCode())).thenReturn(Optional.of(contactCode));

        jurorAuditChangeService.recordContactLog(juror, username, enquiryCode, notes);

        Mockito.verify(contactLogRepository, Mockito.times(1))
            .saveAndFlush(contactLogArgumentCaptor.capture());
        ContactLog contactLog = contactLogArgumentCaptor.getValue();

        Assertions.assertThat(contactLog.getJurorNumber()).isEqualTo(jurorPool.getJurorNumber());
        Assertions.assertThat(contactLog.getEnquiryType()).isEqualTo(contactCode);
        Assertions.assertThat(contactLog.getNotes()).isEqualTo(notes);
        Assertions.assertThat(contactLog.getUsername()).isEqualTo(username);
    }

    @Test
    public void test_hasNameChanged_bothNameChanged() {
        String originalFirstName = "Test";
        String originalLastName = "Person";

        String newFirstName = "First";
        String newLastName = "Last";

        Assertions.assertThat(jurorAuditChangeService.hasNameChanged(newFirstName, originalFirstName,
            newLastName, originalLastName)).isTrue();
    }

    @Test
    public void test_recordPersonalDetailsHistory_argumentValidation() {
        ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

        String propertyName = "first Name";
        JurorPool jurorPool = createJurorPool("400");
        String username = "Test_User";

        jurorAuditChangeService.recordPersonalDetailsHistory(propertyName, jurorPool.getJuror(),
            jurorPool.getPoolNumber(), username);

        Mockito.verify(jurorHistoryRepository, Mockito.times(1))
            .save(jurorHistoryArgumentCaptor.capture());

        JurorHistory jurorHistory = jurorHistoryArgumentCaptor.getValue();
        Assertions.assertThat(jurorHistory.getJurorNumber()).isEqualToIgnoringCase(jurorPool.getJurorNumber());
        Assertions.assertThat(jurorHistory.getPoolNumber()).isEqualToIgnoringCase(jurorPool.getPoolNumber());
        Assertions.assertThat(jurorHistory.getDateCreated()).isEqualToIgnoringHours(LocalDateTime.now());
        Assertions.assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
        Assertions.assertThat(jurorHistory.getOtherInformation()).isEqualTo("First Name Changed");
        Assertions.assertThat(jurorHistory.getCreatedBy()).isEqualTo(username);
    }

    private JurorPool createJurorPool(String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("CHESTER");
        courtLocation.setLocCode("415");
        courtLocation.setOwner("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setOwner(owner);
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber("123456789");
        juror.setFirstName("Test");
        juror.setLastName("Person");
        juror.setAddressLine1("Address Line 1");
        juror.setAddressLine4("Some Town");
        juror.setPostcode("CH1 2AN");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

}
