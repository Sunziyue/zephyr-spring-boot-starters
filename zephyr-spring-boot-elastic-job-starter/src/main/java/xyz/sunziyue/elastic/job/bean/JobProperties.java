package xyz.sunziyue.elastic.job.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author Sunziyue
 */
@Data
@Builder
public class JobProperties {
    @JsonProperty("job_exception_handler")
    private String jobExceptionHandler = "com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler";
    @JsonProperty("executor_service_handler")
    private String executorServiceHandler = "com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler";

    public JobProperties(String jobExceptionHandler, String executorServiceHandler) {
        this.jobExceptionHandler = jobExceptionHandler;
        this.executorServiceHandler = executorServiceHandler;
    }
}
