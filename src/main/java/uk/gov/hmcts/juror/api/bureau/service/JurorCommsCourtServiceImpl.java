package uk.gov.hmcts.juror.api.bureau.service;


import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Implementation of {@link BureauProcessService}.
 */
@Slf4j
@Service
public class JurorCommsCourtServiceImpl implements BureauProcessService {


    @Autowired
    public JurorCommsCourtServiceImpl(

        final AppSettingService appSetting,
        final PoolRepository poolRepository) {
        Assert.notNull(appSetting, "AppSettingService cannot be null.");
        Assert.notNull(poolRepository, "PoolRepository cannot be null.");


    }


    /**
     * Implements a specific job execution.
     * Processes entries in the Juror.messages table and sends the appropriate email notifications to
     * the juror
     */
    @Override
    @Transactional
    public void process() {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Court Comms Processing : Started - {}", dateFormat.format(new Date()));

        // Process court comms
        // adding test comments for new git
        // adding more comments git test
        //more testing feature and repo


        log.info("Court Comms Processing : Finished - {}", dateFormat.format(new Date()));
    }

}
