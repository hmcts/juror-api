package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

/**
 * juror coroner_pool_detail table data.
 */
@Entity
@Table(name = "coroner_pool_detail", schema = "juror_mod")
@IdClass(CoronerPoolDetailId.class)
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CoronerPoolDetail extends Address implements Serializable {

    @Id
    @NotNull
    @Column(name = "COR_POOL_NO")
    @Length(min = 9, max = 9)
    private String poolNumber;

    @Id
    @NotNull
    @Column(name = "JUROR_NUMBER")
    @Length(min = 9, max = 9)
    private String jurorNumber;

    @Column(name = "TITLE")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "FIRST_NAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String firstName;

    @Column(name = "LAST_NAME")
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String lastName;

}
