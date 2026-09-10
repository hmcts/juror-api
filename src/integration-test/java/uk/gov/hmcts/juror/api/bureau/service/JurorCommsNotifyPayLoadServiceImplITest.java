package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Juror comms notify payload service")
class JurorCommsNotifyPayLoadServiceImplITest extends AbstractIntegrationTest {

    private static final String DBD_CONFIRM_ENGLISH_TEMPLATE_ID = "8a71632e-34a2-421e-9f17-100a419be7ba";
    private static final String DBD_CONFIRM_WELSH_TEMPLATE_ID = "7093b2b4-2f71-478b-a179-92321a7f77a7";

    @Autowired
    private JurorCommsNotifyPayLoadService jurorCommsNotifyPayLoadService;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Test
    @Sql({
        "/db/mod/truncate.sql",
        "/db/letter/LetterController_initPoolReissueDeferralLetter.sql",
        "/db/letter/JurorCommsNotifyPayLoadService_dbdConfirmationAttachments.sql"
    })
    void generatePayLoadDataDbdConfirmationEnglishContainsAttachmentUrls() {
        Map<String, String> payload = executeInTransaction(() -> {
            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                "555555565", true, "400");
            return jurorCommsNotifyPayLoadService.generatePayLoadData(DBD_CONFIRM_ENGLISH_TEMPLATE_ID, jurorPool);
        });

        assertThat(payload)
            .containsEntry("COURT_MAP_URL",
                "http://localhost:3000/assets/documents/court-information/EX104_chester.pdf")
            .containsEntry("EMAIL_ATTACHMENT_ALLOWANCES_URL",
                "http://localhost:3000/assets/documents/5223A_juror_allowances.pdf")
            .containsEntry("EMAIL_ATTACHMENT_LOSS_OF_EARNINGS_URL",
                "http://localhost:3000/assets/documents/5223D_certificate_of_loss_of_earnings.pdf")
            .containsEntry("EMAIL_ATTACHMENT_GUIDANCE_EMPLOYERS_URL",
                "http://localhost:3000/assets/documents/5223E_guidance_for_employers.pdf")
            .containsEntry("EMAIL_ATTACHMENT_JURY_GUIDE_URL",
                "http://localhost:3000/assets/documents/5222_your_guide_to_jury_service.pdf");
    }

    @Test
    @Sql({
        "/db/mod/truncate.sql",
        "/db/letter/LetterController_initPoolReissueDeferralLetter.sql",
        "/db/letter/JurorCommsNotifyPayLoadService_dbdConfirmationAttachments.sql"
    })
    void generatePayLoadDataDbdConfirmationWelshContainsWelshAttachmentUrls() {
        Map<String, String> payload = executeInTransaction(() -> {
            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                "555555567", true, "400");
            return jurorCommsNotifyPayLoadService.generatePayLoadData(DBD_CONFIRM_WELSH_TEMPLATE_ID, jurorPool);
        });

        assertThat(payload)
            .containsEntry("COURT_MAP_URL",
                "http://localhost:3000/assets/documents/court-information/EX104_cardiff_CY.pdf")
            .containsEntry("EMAIL_ATTACHMENT_ALLOWANCES_URL",
                "http://localhost:3000/assets/documents/5223A_juror_allowances_CY.pdf")
            .containsEntry("EMAIL_ATTACHMENT_LOSS_OF_EARNINGS_URL",
                "http://localhost:3000/assets/documents/5223D_certificate_of_loss_of_earnings_CY.pdf")
            .containsEntry("EMAIL_ATTACHMENT_GUIDANCE_EMPLOYERS_URL",
                "http://localhost:3000/assets/documents/5223E_guidance_for_employers_CY.pdf")
            .containsEntry("EMAIL_ATTACHMENT_JURY_GUIDE_URL",
                "http://localhost:3000/assets/documents/5222_your_guide_to_jury_service_CY.pdf");
    }
}
