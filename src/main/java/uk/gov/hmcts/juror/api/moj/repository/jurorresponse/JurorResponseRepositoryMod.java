package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;

@NoRepositoryBean
public interface JurorResponseRepositoryMod<T extends AbstractJurorResponse> extends CrudRepository<T,
    String> {
}
