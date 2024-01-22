package com.cgi.odsc.jurordigital.moj.domain.letter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCertLetter is a Querydsl query type for CertLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCertLetter extends EntityPathBase<CertLetter> {

    private static final long serialVersionUID = -407508517L;

    public static final QCertLetter certLetter = new QCertLetter("certLetter");

    public final QLetter _super = new QLetter(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> datePrinted = _super.datePrinted;

    //inherited
    public final StringPath jurorNumber = _super.jurorNumber;

    //inherited
    public final StringPath owner = _super.owner;

    //inherited
    public final BooleanPath printed = _super.printed;

    public QCertLetter(String variable) {
        super(CertLetter.class, forVariable(variable));
    }

    public QCertLetter(Path<? extends CertLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCertLetter(PathMetadata metadata) {
        super(CertLetter.class, metadata);
    }

}

