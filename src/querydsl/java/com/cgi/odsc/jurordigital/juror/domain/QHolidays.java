package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QHolidays is a Querydsl query type for Holidays
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QHolidays extends EntityPathBase<Holidays> {

    private static final long serialVersionUID = -166243724L;

    public static final QHolidays holidays = new QHolidays("holidays");

    public final StringPath description = createString("description");

    public final DateTimePath<java.util.Date> holiday = createDateTime("holiday", java.util.Date.class);

    public final StringPath owner = createString("owner");

    public QHolidays(String variable) {
        super(Holidays.class, forVariable(variable));
    }

    public QHolidays(Path<? extends Holidays> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHolidays(PathMetadata metadata) {
        super(Holidays.class, metadata);
    }

}

