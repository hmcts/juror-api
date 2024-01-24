package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsWelshOnlineResponse is a Querydsl query type for StatsWelshOnlineResponse
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsWelshOnlineResponse extends EntityPathBase<StatsWelshOnlineResponse> {

    private static final long serialVersionUID = -1196401653L;

    public static final QStatsWelshOnlineResponse statsWelshOnlineResponse = new QStatsWelshOnlineResponse("statsWelshOnlineResponse");

    public final DateTimePath<java.util.Date> summonsMonth = createDateTime("summonsMonth", java.util.Date.class);

    public final NumberPath<Integer> welshResponseCount = createNumber("welshResponseCount", Integer.class);

    public QStatsWelshOnlineResponse(String variable) {
        super(StatsWelshOnlineResponse.class, forVariable(variable));
    }

    public QStatsWelshOnlineResponse(Path<? extends StatsWelshOnlineResponse> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsWelshOnlineResponse(PathMetadata metadata) {
        super(StatsWelshOnlineResponse.class, metadata);
    }

}

