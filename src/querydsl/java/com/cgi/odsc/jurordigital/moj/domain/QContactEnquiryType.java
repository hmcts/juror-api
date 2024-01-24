package com.cgi.odsc.jurordigital.moj.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QContactEnquiryType is a Querydsl query type for ContactEnquiryType
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QContactEnquiryType extends EntityPathBase<ContactEnquiryType> {

    private static final long serialVersionUID = -1295561708L;

    public static final QContactEnquiryType contactEnquiryType = new QContactEnquiryType("contactEnquiryType");

    public final StringPath description = createString("description");

    public final EnumPath<ContactEnquiryCode> enquiryCode = createEnum("enquiryCode", ContactEnquiryCode.class);

    public QContactEnquiryType(String variable) {
        super(ContactEnquiryType.class, forVariable(variable));
    }

    public QContactEnquiryType(Path<? extends ContactEnquiryType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContactEnquiryType(PathMetadata metadata) {
        super(ContactEnquiryType.class, metadata);
    }

}

