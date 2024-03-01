package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Juror detail information and bank details for adding or editing juror bank details")
public class JurorBankDetailsDto {

    @JsonProperty("sort_code")
    @Length(max = 6)
    private String sortCode;

    @JsonProperty("bank_account_number")
    @Length(max = 8)
    private String bankAccountNumber;

    @JsonProperty("address_line_1")
    @Length(max = 35)
    private String addressLineOne;

    @JsonProperty("address_line_2")
    @Length(max = 35)
    private String addressLineTwo;

    @JsonProperty("address_line_3")
    @Length(max = 35)
    private String addressLineThree;

    @JsonProperty("address_line_4")
    @Length(max = 35)
    private String addressLineFour;

    @JsonProperty("address_line_5")
    @Length(max = 35)
    private String addressLineFive;

    @JsonProperty("postcode")
    @Length(max = 10)
    private String postCode;

    @JsonProperty("notes")
    @Length(max = 2000)
    private String notes;


    /**
     * Initialise instance of this DTO using Juror object to populate properties
     * @param juror
     */

    public JurorBankDetailsDto (Juror juror)
    {
        this.addressLineOne = juror.getAddressLine1();
        this.addressLineTwo = juror.getAddressLine2();
        this.addressLineThree = juror.getAddressLine3();
        this.addressLineFour = juror.getAddressLine4();
        this.addressLineFive = juror.getAddressLine5();
        this.postCode = juror.getPostcode();
        this.bankAccountNumber = juror.getBankAccountNumber();
        this.sortCode = juror.getSortCode();
        this.notes = juror.getNotes();
    }

}
