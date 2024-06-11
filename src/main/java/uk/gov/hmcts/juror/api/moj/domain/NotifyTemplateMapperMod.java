package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;

import java.util.function.Function;

@Getter
public enum NotifyTemplateMapperMod {
    JUROR_EMAIL(Type.JUROR, (context -> context.getJuror().getEmail())),
    JUROR_FIRST_NAME(Type.JUROR, (context -> context.getJuror().getFirstName())),
    JUROR_NUMBER(Type.JUROR, (context -> context.getJuror().getJurorNumber())),
    JUROR_ALT_PHONE_NUMBER(Type.JUROR, (context -> context.getJuror().getAltPhoneNumber())),
    JUROR_LAST_NAME(Type.JUROR, (context -> context.getJuror().getLastName())),

    RESPONSE_FIRST_NAME(Type.RESPONSE, (context -> context.getAbstractResponse().getFirstName())),
    RESPONSE_LAST_NAME(Type.RESPONSE, (context -> context.getAbstractResponse().getLastName())),
    RESPONSE_JUROR_NUMBER(Type.RESPONSE, (context -> context.getAbstractResponse().getJurorNumber())),
    RESPONSE_EMAIL(Type.RESPONSE, (context -> context.getAbstractResponse().getEmail())),
    RESPONSE_PHONE_NUMBER(Type.RESPONSE, (context -> context.getAbstractResponse().getPhoneNumber())),

    JUROR_POOL_NEXT_DATE(Type.JUROR, (context -> context.getJurorPool().getNextDate())),
    JUROR_POOL_LOC_CODE(Type.JUROR, (context -> context.getJurorPool().getCourt().getLocCode())),

    POOL_ATTEND_TIME(Type.JUROR, (context -> context.getPoolRequest().getAttendTime())),
    POOL_RETURN_DATE(Type.JUROR, (context -> context.getPoolRequest().getReturnDate())),

    COURT_ADDRESS_5(Type.COURT, (context -> context.getActualCourtLocation().getAddress5())),
    COURT_ADDRESS_4(Type.COURT, (context -> context.getActualCourtLocation().getAddress4())),
    COURT_ADDRESS_3(Type.COURT, (context -> context.getActualCourtLocation().getAddress3())),
    COURT_ADDRESS_2(Type.COURT, (context -> context.getActualCourtLocation().getAddress2())),
    COURT_ADDRESS_1(Type.COURT, (context -> context.getActualCourtLocation().getAddress1())),
    COURT_LOC_COURT_NAME(Type.COURT, (context) -> context.getActualCourtLocation().getLocCourtName()),
    COURT_LOC_POSTCODE(Type.COURT, (context -> context.getCourtLocation().getPostcode())),
    COURT_LOC_ADDRESS(Type.COURT, (context -> context.getActualCourtLocation().getLocationAddress())),
    COURT_JURY_OFFICER_PHONE(Type.COURT, (context -> context.getCourtLocation().getJuryOfficerPhone())),

    BULK_PRINT_DATA(Type.BULK_PRINT,context -> context.getDetailData()
                .substring(context.getPositionFrom() - 1, context.getPositionTo()).trim());

    private final Function<Context, Object> mapper;
    private Type type;

    NotifyTemplateMapperMod(Type type, Function<Context, Object> mapper) {
        this.type = type;
        this.mapper = mapper;
    }

    @Getter
    @Setter
    @Builder
    public static class Context {
        Juror juror;
        JurorPool jurorPool;
        PoolRequest poolRequest;
        ICourtLocation actualCourtLocation;

        CourtLocation courtLocation;
        WelshCourtLocation welshCourtLocation;
        JurorCommonResponseRepositoryMod.AbstractResponse abstractResponse;

        String detailData;
        Integer positionFrom;
        Integer positionTo;

        public static Context from(JurorPool jurorPool) {
            return Context.builder()
                .jurorPool(jurorPool)
                .juror(jurorPool.getJuror())
                .poolRequest(jurorPool.getPool())
                .courtLocation(jurorPool.getPool().getCourtLocation())
                .actualCourtLocation(jurorPool.getPool().getCourtLocation())
                .build();
        }
    }

    @Getter
    public enum Type {
        JUROR, COURT, RESPONSE, BULK_PRINT
    }
}
