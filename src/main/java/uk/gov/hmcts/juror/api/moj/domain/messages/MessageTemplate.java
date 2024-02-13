package uk.gov.hmcts.juror.api.moj.domain.messages;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Entity
@Table(name = "t_message_template", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MessageTemplate {

    @Id
    @Column(name = "id")
    @Length(max = 38)
    @NotNull
    private int id;

    @ManyToMany
    @JoinTable(
        schema = "juror_mod",
        name = "message_to_placeholders",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "placeholder_name"))
    private Set<MessagePlaceholders> placeholders;

    @Column(name = "scope")
    @Length(max = 6)
    @NotNull
    private String scope;

    @Column(name = "title")
    @Length(max = 27)
    @NotNull
    private String title;

    @Column(name = "subject")
    @Length(max = 100)
    @NotNull
    private String subject;

    @Column(name = "text")
    @Length(max = 2000)
    @NotNull
    private String text;

    @Column(name = "display_order")
    private Integer displayOrder;
}
