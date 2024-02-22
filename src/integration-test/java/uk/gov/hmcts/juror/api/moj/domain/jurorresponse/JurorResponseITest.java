package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.time.LocalDate;
import java.util.Collections;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql({"/db/mod/truncate.sql", "/db/jurorresponse/JurorResponseITest.sql"})
public class JurorResponseITest extends ContainerTest {
    @Autowired
    JurorPaperResponseRepositoryMod paperResponseRepository;

    @Autowired
    JurorDigitalResponseRepositoryMod digitalResponseRepository;

    @Autowired
    JurorResponseCjsEmploymentRepositoryMod cjsEmploymentRepository;

    @Autowired
    JurorReasonableAdjustmentRepository reasonableAdjustmentRepository;

    @Test
    public void insertPaperResponse() {
        PaperResponse response = createTestPaperResponse();
        paperResponseRepository.save(response);
        cjsEmploymentRepository.saveAll(response.getCjsEmployments());
        reasonableAdjustmentRepository.saveAll(response.getReasonableAdjustments());

        Assertions.assertThat(paperResponseRepository.count()).isEqualTo(1);
        Assertions.assertThat(cjsEmploymentRepository.count()).isEqualTo(1);
        Assertions.assertThat(reasonableAdjustmentRepository.count()).isEqualTo(1);
    }

    @Test
    public void insertDigitalResponse() {
        DigitalResponse response = createDigitalResponse();
        digitalResponseRepository.save(response);
        cjsEmploymentRepository.saveAll(response.getCjsEmployments());
        reasonableAdjustmentRepository.saveAll(response.getReasonableAdjustments());
        Assertions.assertThat(digitalResponseRepository.count()).isEqualTo(1);
        Assertions.assertThat(cjsEmploymentRepository.count()).isEqualTo(1);
        Assertions.assertThat(reasonableAdjustmentRepository.count()).isEqualTo(1);
    }

    private AbstractJurorResponse createGenericResponse() {
        AbstractJurorResponse response = new PaperResponse();
        response.setJurorNumber("111111111");
        response.setDateReceived(LocalDate.now());
        response.setDateOfBirth(LocalDate.of(1970, 1, 1));
        response.setEmail("test@test.com");
        response.setAddressLine1("test road");
        response.setAddressLine2("somewhere");
        response.setAddressLine3("some town");
        response.setAddressLine4("some county");
        response.setAddressLine5("some country");
        response.setPostcode("TS12 9TT");
        response.setPhoneNumber("01234567894");
        response.setAltPhoneNumber("09876543221");
        response.setReasonableAdjustmentsArrangements("Reasonable adjustment arrangements");

        return response;
    }

    private JurorReasonableAdjustment createTestReasonableAdjustment(String jurorNumber) {
        JurorReasonableAdjustment reasonableAdjustments = new JurorReasonableAdjustment();
        reasonableAdjustments.setJurorNumber(jurorNumber);
        reasonableAdjustments.setReasonableAdjustment(
            new ReasonableAdjustments(ReasonableAdjustmentsEnum.READING.getCode(),
                ReasonableAdjustmentsEnum.READING.getDescription()));
        reasonableAdjustments.setReasonableAdjustmentDetail("Cannot read");
        return reasonableAdjustments;
    }

    private JurorResponseCjsEmployment createTestCjsEmployment(String jurorNumber) {
        JurorResponseCjsEmployment cjsEmployment = new JurorResponseCjsEmployment();
        cjsEmployment.setCjsEmployer("CJS Employer");
        cjsEmployment.setJurorNumber(jurorNumber);
        cjsEmployment.setCjsEmployerDetails("Some details");
        return cjsEmployment;
    }

    private DigitalResponse createDigitalResponse() {
        DigitalResponse digitalResponse = new DigitalResponse();
        String[] ignoreProperties = {"residency", "bail", "mentalHealthAct", "convictions"};
        BeanUtils.copyProperties(createGenericResponse(), digitalResponse, ignoreProperties);

        digitalResponse.setJurorNumber("111111112");
        digitalResponse.setResidencyDetail("Residency detail");
        digitalResponse.setMentalHealthActDetails("Mental health details");
        digitalResponse.setBailDetails("Bail details");
        digitalResponse.setConvictions(Boolean.TRUE);
        digitalResponse.setMentalHealthAct(Boolean.TRUE);
        digitalResponse.setResidency(Boolean.TRUE);
        digitalResponse.setBail(Boolean.TRUE);
        digitalResponse.setConvictionsDetails("conviction details");
        digitalResponse.setDeferralReason("Some Reason");
        digitalResponse.setDeferralDate(LocalDate.now().toString());
        digitalResponse.setExcusalReason("Some reason");
        digitalResponse.setVersion(1);
        digitalResponse.setThirdPartyFName("Third party First name");
        digitalResponse.setThirdPartyLName("Third party Last name");
        digitalResponse.setMainPhone("0123456789");
        digitalResponse.setOtherPhone("0987654321");
        digitalResponse.setEmailAddress("test@test.com");
        digitalResponse.setThirdPartyOtherReason("Some other reason");
        digitalResponse.setStaffAssignmentDate(LocalDate.now());
        digitalResponse.setReasonableAdjustments(Collections.singletonList(
            createTestReasonableAdjustment(digitalResponse.getJurorNumber())));
        digitalResponse.setCjsEmployments(Collections.singletonList(
            createTestCjsEmployment(digitalResponse.getJurorNumber())));
        return digitalResponse;
    }

    public PaperResponse createTestPaperResponse() {
        PaperResponse response = (PaperResponse) createGenericResponse();
        response.setSigned(true);
        response.setBail(false);
        response.setExcusal(false);
        response.setDeferral(false);
        response.setMentalHealthCapacity(false);
        response.setReplyType(new ReplyType("Paper", null));
        response.setReasonableAdjustments(Collections.singletonList(
            createTestReasonableAdjustment(response.getJurorNumber())));
        response.setCjsEmployments(Collections.singletonList(
            createTestCjsEmployment(response.getJurorNumber())));
        return response;
    }
}
