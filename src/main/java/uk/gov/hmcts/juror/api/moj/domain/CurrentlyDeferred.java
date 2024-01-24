package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "currently_deferred", schema = "juror_mod")
public class CurrentlyDeferred {

    @Id
    @Column(name = "juror_number")
    private String jurorNumber;

    @NotNull
    @Column(name = "owner")
    private String owner;

    @NotNull
    @Column(name = "deferred_to")
    private LocalDate deferredTo;

    @NotEmpty
    @Column(name = "loc_code")
    private String locCode;

}
