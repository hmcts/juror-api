package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/* Note 1: This entity class is common to paper and digital responses.  Any properties, specific to paper/digital
responses, can be included in this common class.

Note 2: This class should only be used for reading paper and digital responses (not for creating/updating). To
create/update paper or digital responses, use the entities specific to the reply method.
*/

@Entity
@AllArgsConstructor
@SuperBuilder
@Getter
@Table(name = "juror_response", schema = "juror_mod")
@EqualsAndHashCode(callSuper = true)
@Immutable
public class JurorResponseCommon extends AbstractJurorResponse implements Serializable {
}
