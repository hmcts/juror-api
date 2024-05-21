package uk.gov.hmcts.juror.api.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executor;

@Configuration
public class ApplicationBeans {

    @Bean
    @ConditionalOnProperty(
        prefix = "testing",
        name = {"fix-clock"},
        havingValue = "false",
        matchIfMissing = true
    )
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "testing",
        name = {"fix-clock"},
        havingValue = "true"
    )
    public Clock clockTest() {
        return Clock.fixed(
            Instant.now().truncatedTo(ChronoUnit.SECONDS),
            ZoneId.of("UCT")
        );
    }

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }



    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.simpleDateFormat("yyyy-MM-dd");
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
            builder.serializers(new LocalTimeSerializer(DateTimeFormatter.ISO_TIME));
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));

            //builder.deserializers(new LocalDateDeserializer(DateTimeFormatter.ISO_DATE));
            //builder.deserializers(new LocalTimeDeserializer(DateTimeFormatter.ISO_TIME));
            //builder.deserializers(new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        };
    }
}
