package uk.gov.hmcts.juror.api.moj.domain.lisener;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PostLoad;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;

import java.util.List;

@Component
@NoArgsConstructor
public class UserListener {
    private static CourtLocationRepository courtLocationRepository;
    private static EntityManager entityManager;


    @Autowired
    public UserListener(CourtLocationRepository courtLocationRepository,
                        EntityManager entityManager) {
        UserListener.courtLocationRepository = courtLocationRepository;
        UserListener.entityManager = entityManager;
    }

    @PostLoad
    void postLoad(User user) {
        List<String> courts = courtLocationRepository.findLocCodeByOwner(entityManager, user.getOwner());
        user.setCourtLocation(courts);
    }
}
