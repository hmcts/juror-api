package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QSurveyResponse is a Querydsl query type for SurveyResponse
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSurveyResponse extends EntityPathBase<SurveyResponse> {

    private static final long serialVersionUID = 1118817246L;

    public static final QSurveyResponse surveyResponse = new QSurveyResponse("surveyResponse");

    public final StringPath id = createString("id");

    public final StringPath satisfactionDesc = createString("satisfactionDesc");

    public final StringPath surveyId = createString("surveyId");

    public final DateTimePath<java.util.Date> surveyResponseDate = createDateTime("surveyResponseDate", java.util.Date.class);

    public final NumberPath<Integer> userNo = createNumber("userNo", Integer.class);

    public QSurveyResponse(String variable) {
        super(SurveyResponse.class, forVariable(variable));
    }

    public QSurveyResponse(Path<? extends SurveyResponse> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSurveyResponse(PathMetadata metadata) {
        super(SurveyResponse.class, metadata);
    }

}

