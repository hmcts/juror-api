package uk.gov.hmcts.juror.api.bureau.domain;

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
 * Print files notify comms entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(JurorCommsPrintFilesKey.class)
@Table(name = "PRINT_FILES_NOTIFY_COMMS", schema = "JUROR_DIGITAL_USER")
public class JurorCommsPrintFiles implements Serializable {
    @Id
    @Length(max = 12)
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
    @Column(name = "JUROR_NUMBER")
    private String jurorNumber;

    @Length(max = 1)
    @Column(name = "DIGITAL_COMMS")
    private String digitalComms;

    @NotNull
    @Length(max = 50)
    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Length(max = 40)
    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @NotNull
    @Length(max = 60)
    @Column(name = "NOTIFY_NAME")
    private String notifyName;

}
