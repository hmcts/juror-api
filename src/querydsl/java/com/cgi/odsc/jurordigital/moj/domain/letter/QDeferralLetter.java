package com.cgi.odsc.jurordigital.moj.domain.letter;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QDeferralLetter is a Querydsl query type for DeferralLetter
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QDeferralLetter extends EntityPathBase<DeferralLetter> {

    private static final long serialVersionUID = -216435358L;

    public static final QDeferralLetter deferralLetter = new QDeferralLetter("deferralLetter");

    public final QLetter _super = new QLetter(this);

    public final DatePath<java.time.LocalDate> dateDef = createDate("dateDef", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> datePrinted = _super.datePrinted;

    public final StringPath excusalCode = createString("excusalCode");

    //inherited
    public final StringPath jurorNumber = _super.jurorNumber;

    //inherited
    public final StringPath owner = _super.owner;

    //inherited
    public final BooleanPath printed = _super.printed;

    public QDeferralLetter(String variable) {
        super(DeferralLetter.class, forVariable(variable));
    }

    public QDeferralLetter(Path<? extends DeferralLetter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDeferralLetter(PathMetadata metadata) {
        super(DeferralLetter.class, metadata);
    }

}

