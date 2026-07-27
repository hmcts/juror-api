package uk.gov.hmcts.juror.api.moj.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.enumeration.CommunicationChannel;
import uk.gov.hmcts.juror.api.moj.enumeration.EmailStatus;

import java.time.LocalDate;

//Bulk Print Data entity based on the juror Print Files entity.
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "bulk_print_data", schema = "juror_mod")
public class BulkPrintData {

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_type")
    private FormAttribute formAttribute;

    @NotNull
    @Length(max = 1285)
    @Column(name = "detail_rec")
    private String detailRec;

    @Column(name = "extracted_flag")
    @Builder.Default
    private boolean extractedFlag = false;

    @Column(name = "digital_comms")
    private boolean digitalComms;

    @NotNull
    @Column(name = "communication_channel", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommunicationChannel communicationChannel = CommunicationChannel.LETTER;

    @Length(max = 40)
    @Column(name = "notify_template_name")
    private String notifyTemplateName;

    @Column(name = "email_status", length = 20)
    @Enumerated(EnumType.STRING)
    private EmailStatus emailStatus;

}
