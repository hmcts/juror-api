package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLoginAttempt is a Querydsl query type for LoginAttempt
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QLoginAttempt extends EntityPathBase<LoginAttempt> {

    private static final long serialVersionUID = 1603411453L;

    public static final QLoginAttempt loginAttempt = new QLoginAttempt("loginAttempt");

    public final NumberPath<Integer> loginattempts = createNumber("loginattempts", Integer.class);

    public final StringPath username = createString("username");

    public QLoginAttempt(String variable) {
        super(LoginAttempt.class, forVariable(variable));
    }

    public QLoginAttempt(Path<? extends LoginAttempt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLoginAttempt(PathMetadata metadata) {
        super(LoginAttempt.class, metadata);
    }

}

