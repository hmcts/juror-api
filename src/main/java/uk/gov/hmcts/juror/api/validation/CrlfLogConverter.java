package uk.gov.hmcts.juror.api.validation;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

/**
 * This class is attached to logback and used to sanitize messages where desired.
 * <p> Currently this converter is used to encode any carriage returns and line feeds to
 * prevent log injection attacks
 * <p> It is not possible to replace the actual formatted message, instead this
 * converter returns a masked version of the message that can be accessed using
 * the conversionWord specified in the conversionRule definition in logback.xml.
 */
public class CrlfLogConverter extends CompositeConverter<ILoggingEvent> {

    @Override
    protected String transform(ILoggingEvent event, String in) {
        return in.replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Override start method because the superclass ReplacingCompositeConverter
     * requires at least two options and this class has none.
     */
    @Override
    public void start() {
        started = true;
    }
}
