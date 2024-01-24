package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

/**
 * Print Files entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(PrintFileKey.class)
@Table(name = "PRINT_FILES", schema = "JUROR_DIGITAL_USER")
public class PrintFile {
    @Id
    @NotEmpty
    @Length(max = 15)
    @Column(name = "PRINTFILE_NAME")
    private String printFileName;

    @Id
    @NotNull
    @Column(name = "CREATION_DATE")
    private Date creationDate;

    @NotNull
    @Length(max = 6)
    @Column(name = "FORM_TYPE")
    private String formType;

    @NotNull
    @Length(max = 1260)
    @Column(name = "DETAIL_REC")
    private String detailRec;

    @Column(name = "EXTRACTED_FLAG")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean extractedFlag;

    @Id
    @Length(max = 9)
    @Column(name = "PART_NO")
    private String partNo;

    @Column(name = "DIGITAL_COMMS")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean digitalComms;

}
