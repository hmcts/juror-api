package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsUnprocessedResponse is a Querydsl query type for StatsUnprocessedResponse
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsUnprocessedResponse extends EntityPathBase<StatsUnprocessedResponse> {

    private static final long serialVersionUID = -79975558L;

    public static final QStatsUnprocessedResponse statsUnprocessedResponse = new QStatsUnprocessedResponse("statsUnprocessedResponse");

    public final StringPath locCode = createString("locCode");

    public final NumberPath<Integer> unprocessedCount = createNumber("unprocessedCount", Integer.class);

    public QStatsUnprocessedResponse(String variable) {
        super(StatsUnprocessedResponse.class, forVariable(variable));
    }

    public QStatsUnprocessedResponse(Path<? extends StatsUnprocessedResponse> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsUnprocessedResponse(PathMetadata metadata) {
        super(StatsUnprocessedResponse.class, metadata);
    }

}

