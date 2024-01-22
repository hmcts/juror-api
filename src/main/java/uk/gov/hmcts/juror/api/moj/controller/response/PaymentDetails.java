package uk.gov.hmcts.juror.api.moj.controller.response;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

@Builder
@Data
public class PaymentDetails {

    @Column(name = "sort_code")
    @Length(max = 6)
    private String sortCode;

    @Column(name = "bank_account_name")
    @Length(max = 18)
    private String bankAccountName;

    @Column(name = "bank_account_number")
    @Length(max = 8)
    private String bankAccountNumber;

    @Column(name = "building_society_roll_number")
    @Length(max = 18)
    private String buildingSocietyRollNumber;

    public static PaymentDetails from(Juror juror) {
        return PaymentDetails.builder()
            .sortCode(juror.getSortCode())
            .bankAccountName(juror.getBankAccountName())
            .bankAccountNumber(juror.getBankAccountNumber())
            .buildingSocietyRollNumber(juror.getBuildingSocietyRollNumber())
            .build();
    }
}
