package xyz.sunziyue.elastic.job.annotation;

import org.springframework.context.annotation.Import;
import xyz.sunziyue.elastic.job.autoconfigure.JobAutoConfiguration;

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
