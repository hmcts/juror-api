package uk.gov.hmcts.juror.api.moj.domain.letter.court;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@IdClass(LetterListId.class)
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class LetterListBase implements Serializable {

    @Id
    @NotNull
    @Column(name = "owner")
    @Length(min = 3, max = 3)
    private String owner;

    @Id
    @NotNull
    @PoolNumber
    @Column(name = "pool_number")
    private String poolNumber;

    @Id
    @NotNull
    @JurorNumber
    @Column(name = "juror_number")
    private String jurorNumber;

    @Id
    @Column(name = "row_no")
    private int rowNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "status_desc")
    private String status;

    @Column(name = "date_printed")
    private LocalDateTime datePrinted;

    @Column(name = "is_active")
    private boolean isActive;

}
