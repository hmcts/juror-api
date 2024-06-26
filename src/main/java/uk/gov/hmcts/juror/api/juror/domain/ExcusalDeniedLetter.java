package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * Excusal denied letter entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ExcusalDeniedLetterKey.class)
@Entity
@Table(name = "EXC_DENIED_LETT", schema = "JUROR_DIGITAL_USER")
@Deprecated(forRemoval = true)
public class ExcusalDeniedLetter implements Serializable {
    @Id
    @Length(min = 3, max = 3)
    @Column(name = "OWNER")
    private String owner = "400";

    @Id
    @Length(min = 9, max = 9)
    @Column(name = "JUROR_NUMBER")
    private String jurorNumber;

    @NotNull
    @Length(min = 1, max = 1)
    @Column(name = "EXC_CODE")
    private String excusalCode;

    @NotNull
    @Column(name = "DATE_EXCUSED")
    private Date dateExcused;

    @Column(name = "DATE_PRINTED")
    private Date datePrinted;

    @Column(name = "PRINTED")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean printed;
}
