package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoronerPoolDetail is a Querydsl query type for CoronerPoolDetail
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCoronerPoolDetail extends EntityPathBase<CoronerPoolDetail> {

    private static final long serialVersionUID = -2140653080L;

    public static final QCoronerPoolDetail coronerPoolDetail = new QCoronerPoolDetail("coronerPoolDetail");

    public final StringPath addressLine1 = createString("addressLine1");

    public final StringPath addressLine2 = createString("addressLine2");

    public final StringPath addressLine3 = createString("addressLine3");

    public final StringPath addressLine4 = createString("addressLine4");

    public final StringPath addressLine5 = createString("addressLine5");

    public final StringPath addressLine6 = createString("addressLine6");

    public final StringPath firstName = createString("firstName");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath lastName = createString("lastName");

    public final StringPath poolNumber = createString("poolNumber");

    public final StringPath postcode = createString("postcode");

    public final StringPath title = createString("title");

    public QCoronerPoolDetail(String variable) {
        super(CoronerPoolDetail.class, forVariable(variable));
    }

    public QCoronerPoolDetail(Path<? extends CoronerPoolDetail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoronerPoolDetail(PathMetadata metadata) {
        super(CoronerPoolDetail.class, metadata);
    }

}

