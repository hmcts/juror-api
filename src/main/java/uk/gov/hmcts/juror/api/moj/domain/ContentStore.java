package uk.gov.hmcts.juror.api.moj.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "content_store", schema = "juror_mod")
public class ContentStore {

    @Id
    @Column(name = "id")
    private Long id;

    @Length(max = 50)
    @Column(name = "document_id")
    private String documentId;

    @NotNull
    @Column(name = "date_on_q_for_send")
    private LocalDate dateEnqueued;

    @NotNull
    @Length(max = 10)
    @Column(name = "file_type")
    private String fileType;

    @NotNull
    @Column(name = "date_sent")
    private LocalDateTime dateSent;

    @NotNull
    @Column(name = "data")
    private String data;

    @Column(name = "failed_file_transfer")
    private boolean failedFileTransfer;

}
