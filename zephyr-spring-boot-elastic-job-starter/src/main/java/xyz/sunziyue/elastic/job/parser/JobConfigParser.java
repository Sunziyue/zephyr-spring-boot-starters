package xyz.sunziyue.elastic.job.parser;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import xyz.sunziyue.elastic.job.annotation.ElasticJobConfig;
import xyz.sunziyue.elastic.job.bean.Job;
import xyz.sunziyue.elastic.job.bean.JobProperties;
import xyz.sunziyue.elastic.job.service.JobService;
import xyz.sunziyue.elastic.job.util.ListenerBeanDefinitionUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sunziyue
 */
@Slf4j
public class JobConfigParser implements ApplicationContextAware {
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;
    private String prefix = "elastic.job.";
    private Environment environment;
    @Autowired(required = false)
    private JobService jobService;
    private Class<?> jobTypeClass;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.environment = ctx.getEnvironment();
        Map<String, Object> beanMap = ctx.getBeansWithAnnotation(ElasticJobConfig.class);
        Iterator var3 = beanMap.values().iterator();

        while (var3.hasNext()) {
            Object confBean = var3.next();
            Class<?> clz = confBean.getClass();
            this.getJobTypeClass(clz);
            String jobTypeName = this.jobTypeClass.getSimpleName();
            ElasticJobConfig conf = (ElasticJobConfig) clz.getAnnotation(ElasticJobConfig.class);
            String jobClass = clz.getName();
            String jobName = conf.name();
            Job job = this.initJob(jobName, jobClass, conf);
            job.setJobType(jobTypeName);
            LiteJobConfiguration jobConfig = this.getJobConfiguration(job);
            List<BeanDefinition> elasticJobListeners = this.getElasticJobListeners(conf);
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
            factory.setScope("prototype");
            if (Objects.equals("ScriptJob", jobTypeName)) {
                factory.addConstructorArgValue((Object) null);
            } else {
                factory.addConstructorArgValue(confBean);
            }

            factory.addConstructorArgValue(this.zookeeperRegistryCenter);
            factory.addConstructorArgValue(jobConfig);
            if (StringUtils.hasText(job.getEventTraceRdbDataSource())) {
                BeanDefinitionBuilder rdbFactory = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
                rdbFactory.addConstructorArgReference(job.getEventTraceRdbDataSource());
                factory.addConstructorArgValue(rdbFactory.getBeanDefinition());
            }

            factory.addConstructorArgValue(elasticJobListeners);
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
            defaultListableBeanFactory.registerBeanDefinition("SpringJobScheduler", factory.getBeanDefinition());
            SpringJobScheduler springJobScheduler = (SpringJobScheduler) ctx.getBean("SpringJobScheduler");
            springJobScheduler.init();
            log.info("【" + jobName + "】\t" + jobClass + "\tinit success");
        }

        if (this.jobService != null) {
            this.jobService.monitorJobRegister();
        }
    }

    private LiteJobConfiguration getJobConfiguration(Job job) {
        JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder(job.getJobName(), job.getCron(), job.getShardingTotalCount()).shardingItemParameters(job.getShardingItemParameters()).description(job.getDescription()).failover(job.isFailover()).jobParameter(job.getJobParameter()).misfire(job.isMisfire()).jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), job.getJobProperties().getJobExceptionHandler()).jobProperties(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), job.getJobProperties().getExecutorServiceHandler()).build();
        JobTypeConfiguration typeConfig = null;
        if (Objects.equals("SimpleJob", job.getJobType())) {
            typeConfig = new SimpleJobConfiguration(coreConfig, job.getJobClass());
        }

        if (Objects.equals("DataflowJob", job.getJobType())) {
            typeConfig = new DataflowJobConfiguration(coreConfig, job.getJobClass(), job.isStreamingProcess());
        }

        if (Objects.equals("ScriptJob", job.getJobType())) {
            typeConfig = new ScriptJobConfiguration(coreConfig, job.getScriptCommandLine());
        }

        LiteJobConfiguration jobConfig = LiteJobConfiguration.newBuilder((JobTypeConfiguration)typeConfig).overwrite(job.isOverwrite()).disabled(job.isDisabled()).monitorPort(job.getMonitorPort()).monitorExecution(job.isMonitorExecution()).maxTimeDiffSeconds(job.getMaxTimeDiffSeconds()).jobShardingStrategyClass(job.getJobShardingStrategyClass()).reconcileIntervalMinutes(job.getReconcileIntervalMinutes()).build();
        return jobConfig;
    }

    private List<BeanDefinition> getElasticJobListeners(ElasticJobConfig conf) {
        List<BeanDefinition> result = new ManagedList(2);
        String listeners = this.getEnvironmentStringValue(conf.name(), "listener", conf.listener());
        if (StringUtils.hasText(listeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(listeners);
            factory.setScope("prototype");
            result.add(factory.getBeanDefinition());
        }

        String distributedListeners = this.getEnvironmentStringValue(conf.name(), "distributedListener", conf.distributedListener());
        long startedTimeoutMilliseconds = this.getEnvironmentLongValue(conf.name(), "startedTimeoutMilliseconds", conf.startedTimeoutMilliseconds());
        long completedTimeoutMilliseconds = this.getEnvironmentLongValue(conf.name(), "completedTimeoutMilliseconds", conf.completedTimeoutMilliseconds());
        if (StringUtils.hasText(distributedListeners)) {
            result.add(ListenerBeanDefinitionUtil.getJobDistributedListenersBeanDefinition(distributedListeners, startedTimeoutMilliseconds, completedTimeoutMilliseconds));
        }

        return result;
    }



    private void getJobTypeClass(Class<?> clz) {
        if (Objects.isNull(clz)) {
            throw new RuntimeException("can not find job xyz.sunziyue.elastic.job.annotation ,job init fail");
        } else {
            Class<?>[] classes = clz.getInterfaces();
            boolean isJob = false;
            if (Objects.nonNull(classes) && classes.length > 0) {
                Class[] var4 = classes;
                int var5 = classes.length;

                for (int var6 = 0; var6 < var5; ++var6) {
                    Class<?> c = var4[var6];
                    String name = c.getSimpleName();
                    if (Objects.equals(name, "SimpleJob") || Objects.equals(name, "DataflowJob") || Objects.equals(name, "ScriptJob")) {
                        isJob = true;
                        this.jobTypeClass = c;
                        break;
                    }
                }
            }

            if (!isJob) {
                this.getJobTypeClass(clz.getSuperclass());
            }

        }
    }

    private String getEnvironmentStringValue(String jobName, String fieldName, String defaultValue) {
        String key = this.prefix + jobName + "." + fieldName;
        String value = this.environment.getProperty(key);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private int getEnvironmentIntValue(String jobName, String fieldName, int defaultValue) {
        String key = this.prefix + jobName + "." + fieldName;
        String value = this.environment.getProperty(key);
        return StringUtils.hasText(value) ? Integer.valueOf(value) : defaultValue;
    }

    private long getEnvironmentLongValue(String jobName, String fieldName, long defaultValue) {
        String key = this.prefix + jobName + "." + fieldName;
        String value = this.environment.getProperty(key);
        return StringUtils.hasText(value) ? Long.valueOf(value) : defaultValue;
    }

    private boolean getEnvironmentBooleanValue(String jobName, String fieldName, boolean defaultValue) {
        String key = this.prefix + jobName + "." + fieldName;
        String value = this.environment.getProperty(key);
        return StringUtils.hasText(value) ? Boolean.valueOf(value) : defaultValue;
    }

    private Job initJob(String jobName, String jobClass, ElasticJobConfig conf) {
        String cron = this.getEnvironmentStringValue(jobName, "cron", conf.cron());
        String shardingItemParameters = this.getEnvironmentStringValue(jobName, "shardingItemParameters", conf.shardingItemParameters());
        String description = this.getEnvironmentStringValue(jobName, "description", conf.description());
        String jobParameter = this.getEnvironmentStringValue(jobName, "jobParameter", conf.jobParameter());
        String jobExceptionHandler = this.getEnvironmentStringValue(jobName, "jobExceptionHandler", conf.jobExceptionHandler());
        String executorServiceHandler = this.getEnvironmentStringValue(jobName, "executorServiceHandler", conf.executorServiceHandler());
        String jobShardingStrategyClass = this.getEnvironmentStringValue(jobName, "jobShardingStrategyClass", conf.jobShardingStrategyClass());
        String eventTraceRdbDataSource = this.getEnvironmentStringValue(jobName, "eventTraceRdbDataSource", conf.eventTraceRdbDataSource());
        String scriptCommandLine = this.getEnvironmentStringValue(jobName, "scriptCommandLine", conf.scriptCommandLine());
        boolean failover = this.getEnvironmentBooleanValue(jobName, "failover", conf.failover());
        boolean misfire = this.getEnvironmentBooleanValue(jobName, "misfire", conf.misfire());
        boolean overwrite = this.getEnvironmentBooleanValue(jobName, "overwrite", conf.overwrite());
        boolean disabled = this.getEnvironmentBooleanValue(jobName, "disabled", conf.disabled());
        boolean monitorExecution = this.getEnvironmentBooleanValue(jobName, "monitorExecution", conf.monitorExecution());
        boolean streamingProcess = this.getEnvironmentBooleanValue(jobName, "streamingProcess", conf.streamingProcess());
        int shardingTotalCount = this.getEnvironmentIntValue(jobName, "shardingTotalCount", conf.shardingTotalCount());
        int monitorPort = this.getEnvironmentIntValue(jobName, "monitorPort", conf.monitorPort());
        int maxTimeDiffSeconds = this.getEnvironmentIntValue(jobName, "maxTimeDiffSeconds", conf.maxTimeDiffSeconds());
        int reconcileIntervalMinutes = this.getEnvironmentIntValue(jobName, "reconcileIntervalMinutes", conf.reconcileIntervalMinutes());
        return Job.builder()
                .cron(cron)
                .jobName(jobName)
                .description(description)
                .jobClass(jobClass)
                .jobParameter(jobParameter)
                .jobProperties(
                        JobProperties.builder()
                        .jobExceptionHandler(jobExceptionHandler)
                        .executorServiceHandler(executorServiceHandler)
                        .build()
                )
                .jobShardingStrategyClass(jobShardingStrategyClass)
                .failover(failover)
                .misfire(misfire)
                .overwrite(overwrite)
                .disabled(disabled)
                .monitorExecution(monitorExecution)
                .streamingProcess(streamingProcess)
                .scriptCommandLine(scriptCommandLine)
                .shardingTotalCount(shardingTotalCount)
                .monitorPort(monitorPort)
                .maxTimeDiffSeconds(maxTimeDiffSeconds)
                .reconcileIntervalMinutes(reconcileIntervalMinutes)
                .shardingItemParameters(shardingItemParameters)
                .eventTraceRdbDataSource(eventTraceRdbDataSource)
                .build();
    }
}
