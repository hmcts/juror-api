package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.SettingDefinition;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Entity
@Table(name = "t_form_attr", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Immutable
public class FormAttribute implements Serializable {

    @Id
    @Column(name = "form_type")
    @Length(max = 6)
    @NotNull
    private String formType;

    @NotNull
    @Column(name = "dir_name")
    @Length(max = 20)
    private String directoryName;

    @Column(name = "max_rec_len")
    private int maxRecLen;
}
