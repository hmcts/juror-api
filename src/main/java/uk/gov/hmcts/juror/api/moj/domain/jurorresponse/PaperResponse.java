package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Table(name = "juror_response", schema = "juror_mod")
@EqualsAndHashCode(callSuper = true)
public class PaperResponse extends AbstractJurorResponse {

    @Column(name = "mental_health_capacity")
    private Boolean mentalHealthCapacity;

    @Column(name = "deferral")
    private Boolean deferral;

    @Column(name = "excusal")
    private Boolean excusal;

    @Column(name = "signed")
    private Boolean signed;

    public PaperResponse() {
        super();
        super.setReplyType(new ReplyType("Paper", "Paper response"));
    }
}
