package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QVotersLocPostcodeTotals is a Querydsl query type for VotersLocPostcodeTotals
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QVotersLocPostcodeTotals extends EntityPathBase<VotersLocPostcodeTotals> {

    private static final long serialVersionUID = 2224954L;

    public static final QVotersLocPostcodeTotals votersLocPostcodeTotals = new QVotersLocPostcodeTotals("votersLocPostcodeTotals");

    public final StringPath locCode = createString("locCode");

    public final StringPath postcode = createString("postcode");

    public final NumberPath<Integer> total = createNumber("total", Integer.class);

    public final NumberPath<Integer> totalCor = createNumber("totalCor", Integer.class);

    public QVotersLocPostcodeTotals(String variable) {
        super(VotersLocPostcodeTotals.class, forVariable(variable));
    }

    public QVotersLocPostcodeTotals(Path<? extends VotersLocPostcodeTotals> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVotersLocPostcodeTotals(PathMetadata metadata) {
        super(VotersLocPostcodeTotals.class, metadata);
    }

}

