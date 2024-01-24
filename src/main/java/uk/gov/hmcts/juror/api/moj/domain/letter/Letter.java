package uk.gov.hmcts.juror.api.moj.domain.letter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@MappedSuperclass
@IdClass(LetterId.class)
@Getter
@Setter
public abstract class Letter {

    @Id
    @NotNull
    @Column(name = "OWNER")
    @Length(min = 3, max = 3)
    private String owner;

    @Id
    @NotNull
    @Column(name = "PART_NO")
    @Length(max = 9)
    private String jurorNumber;

    @Column(name = "PRINTED")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean printed;

    @Column(name = "DATE_PRINTED")
    private LocalDateTime datePrinted;

}
