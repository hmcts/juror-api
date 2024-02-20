package uk.gov.hmcts.juror.api.moj.domain.messages;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "message_placeholders", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MessagePlaceholders {

    @Id
    @Column(name = "placeholder_name")
    @Length(max = 48)
    @NotNull
    private String placeholderName;

    @Column(name = "source_table_name")
    @Length(max = 48)
    @NotNull
    private String sourceTableName;

    @Column(name = "source_column_name")
    @Length(max = 48)
    @NotNull
    private String sourceColumnName;

    @Column(name = "display_name")
    @Length(max = 32)
    @NotNull
    private String displayName;

    @Column(name = "data_type")
    @Length(max = 12)
    @NotNull
    @Enumerated(EnumType.STRING)
    private DataType type;

    @Column(name = "description")
    @Length(max = 100)
    private String description;

    @Column(name = "editable")
    private boolean editable;

    @Column(name = "validation_regex")
    @Length(max = 600)
    @NotNull
    private String validationRegex;

    @Column(name = "validation_message")
    @Length(max = 200)
    @NotNull
    private String validationMessage;

}