package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "juror_response_aud", schema = "juror_mod")
@Data
@Qualifier("NewJurorResponseAudit")
@IdClass(JurorResponseAuditMod.JurorResponseAuditModId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JurorResponseAuditMod {

    @Id
    @Column(name = "juror_number")
    private String jurorNumber;

    @Id
    @Column(name = "changed")
    private LocalDateTime changed;

    @Column(name = "login")
    private String login;

    @Column(name = "old_processing_status")
    @Enumerated(EnumType.STRING)
    private ProcessingStatus oldProcessingStatus;

    @Column(name = "new_processing_status")
    @Enumerated(EnumType.STRING)
    private ProcessingStatus newProcessingStatus;

    public static class JurorResponseAuditModId {
        private String jurorNumber;
        private LocalDateTime changed;
    }
}
