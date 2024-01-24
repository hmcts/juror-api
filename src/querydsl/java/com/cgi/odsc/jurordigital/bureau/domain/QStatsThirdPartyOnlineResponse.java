package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatsThirdPartyOnlineResponse is a Querydsl query type for StatsThirdPartyOnlineResponse
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatsThirdPartyOnlineResponse extends EntityPathBase<StatsThirdPartyOnlineResponse> {

    private static final long serialVersionUID = 1907955343L;

    public static final QStatsThirdPartyOnlineResponse statsThirdPartyOnlineResponse = new QStatsThirdPartyOnlineResponse("statsThirdPartyOnlineResponse");

    public final DateTimePath<java.util.Date> summonsMonth = createDateTime("summonsMonth", java.util.Date.class);

    public final NumberPath<Integer> thirdPartyResponseCount = createNumber("thirdPartyResponseCount", Integer.class);

    public QStatsThirdPartyOnlineResponse(String variable) {
        super(StatsThirdPartyOnlineResponse.class, forVariable(variable));
    }

    public QStatsThirdPartyOnlineResponse(Path<? extends StatsThirdPartyOnlineResponse> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatsThirdPartyOnlineResponse(PathMetadata metadata) {
        super(StatsThirdPartyOnlineResponse.class, metadata);
    }

}

