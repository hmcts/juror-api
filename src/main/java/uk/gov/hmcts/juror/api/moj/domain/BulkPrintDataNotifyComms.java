package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
//@IdClass(BulkPrintDataNotifyCommsKey.class)
@Table(name = "bulk_print_data_notify_comms", schema = "juror_mod")
public class BulkPrintDataNotifyComms {


    private static final String GENERATOR_NAME = "bulk_print_data_seq_generator";

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = GENERATOR_NAME, schema = "juror_mod", sequenceName = "bulk_print_data_seq",
        allocationSize = 1)
    @GeneratedValue(generator = GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    private Long id;

    @Length(max = 9)
    @Column(name = "juror_no")
    private String jurorNo;

    @NotNull
    @Column(name = "creation_date")
    private LocalDate creationDate;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "form_type")
    private FormAttribute formAttribute;

    @NotNull
    @Length(max = 1260)
    @Column(name = "detail_rec")
    private String detailRec;

    @Column(name = "extracted_flag")
    private Boolean extractedFlag;

    @Column(name = "digital_comms")
    private Boolean digitalComms;

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
