package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "t_police", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PoliceCode implements Serializable {

    @Id
    @NotNull
    @Column(name = "code")
    private String policeCode;

    @Column(name = "description")
    private String description;
}
