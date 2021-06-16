package xyz.szy.elastic.job.annotation;

import org.springframework.context.annotation.Import;
import xyz.szy.elastic.job.autoconfigure.JobAutoConfiguration;

import java.lang.annotation.*;

/**
 * @author Sunziyue
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({JobAutoConfiguration.class})
public @interface EnableElasticJob {
}
