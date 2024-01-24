package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Entity
@Table(name = "juror_response_cjs_employment", schema = "juror_mod")
@Data
public class JurorResponseCjsEmployment implements Serializable {
    @Id
    @GeneratedValue(generator = "juror_response_cjs_employment_gen")
    @Column(name = "id")
    @SequenceGenerator(name = "juror_response_cjs_employment_gen",
        sequenceName = "juror_mod.juror_response_cjs_employment_id_seq")
    private Long id;

    @Column(name = "juror_number")
    @Length(max = 9)
    private String jurorNumber;

    @Column(name = "cjs_employer")
    @Length(max = 100)
    private String cjsEmployer;

    @Column(name = "cjs_employer_details")
    @Length(max = 1000)
    private String cjsEmployerDetails;


}
