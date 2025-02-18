package uk.gov.hmcts.juror.api.moj.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.THistoryCode;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.THistoryCodeRepository;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class JurorHistoryCodesITest extends ContainerTest {
    @Autowired
    THistoryCodeRepository historyCodeRepository;
    
    @Test
    public void testJurorHistoryCodesHappy() {

        Iterable<THistoryCode> jurorHistoryCodesIter = historyCodeRepository.findAll();
        ArrayList<THistoryCode> historyCodesRepo = new ArrayList<>();
        jurorHistoryCodesIter.forEach(historyCodesRepo::add);

        List<String> historyCodesDB = historyCodesRepo.stream().map(h -> h.getHistoryCode()).sorted().toList();
        List<String> historyCodes = Arrays.stream(HistoryCodeMod.values()).map(h -> h.getCode()).sorted().toList();

        // Expect the history codes in DB to match those in the enum
        assertThat(historyCodesDB).as("Expected history codes to match").isEqualTo(historyCodes);
    }

}
