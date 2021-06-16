package xyz.szy.elastic.job.service;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import xyz.szy.elastic.job.bean.Job;
import xyz.szy.elastic.job.util.JsonUtils;
import xyz.szy.elastic.job.util.ListenerBeanDefinitionUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Sunziyue
 */
@Slf4j
public class JobService {
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;
    @Autowired
    private ApplicationContext applicationContext;
    private static final Map<String, AtomicInteger> JOB_ADD_COUNT = Maps.newConcurrentMap();
    public void addJob(Job job) {
        JobCoreConfiguration coreConfiguration = JobCoreConfiguration.newBuilder(job.getJobName(), job.getCron(), job.getShardingTotalCount()).shardingItemParameters(job.getShardingItemParameters()).description(job.getDescription()).jobParameter(job.getJobParameter()).failover(job.isFailover()).jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), job.getJobProperties().getJobExceptionHandler()).jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), job.getJobProperties().getJobExceptionHandler()).build();
        LiteJobConfiguration jobConfig = null;
        JobTypeConfiguration typeConfig = null;
        String jobType = job.getJobType();
        if (Objects.equals("SimpleJob", jobType)) {
            typeConfig = new SimpleJobConfiguration(coreConfiguration, job.getJobClass());
        }

        if (Objects.equals("DataflowJob", jobType)) {
            typeConfig = new DataflowJobConfiguration(coreConfiguration, job.getJobClass(), job.isStreamingProcess());
        }

        if (Objects.equals("ScriptJob", jobType)) {
            typeConfig = new ScriptJobConfiguration(coreConfiguration, job.getScriptCommandLine());
        }

        LiteJobConfiguration.newBuilder(typeConfig).overwrite(job.isOverwrite()).disabled(job.isDisabled()).monitorPort(job.getMonitorPort()).monitorExecution(job.isMonitorExecution()).maxTimeDiffSeconds(job.getMaxTimeDiffSeconds()).jobShardingStrategyClass(job.getJobShardingStrategyClass()).reconcileIntervalMinutes(job.getReconcileIntervalMinutes()).build();
        List<BeanDefinition> elasticJobListeners = this.getElasticJobListeners(job);
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
        factory.setScope("prototype");
        BeanDefinitionBuilder jobEventRdbFactory;
        if (Objects.equals("ScriptJob", jobType)) {
            factory.addConstructorArgValue(null);
        } else {
            jobEventRdbFactory = BeanDefinitionBuilder.rootBeanDefinition(job.getJobClass());
            factory.addConstructorArgValue(jobEventRdbFactory.getBeanDefinition());
        }

        factory.addConstructorArgValue(this.zookeeperRegistryCenter);
        factory.addConstructorArgValue(jobConfig);
        if (StringUtils.hasText(job.getEventTraceRdbDataSource())) {
            jobEventRdbFactory = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
            jobEventRdbFactory.addConstructorArgReference(job.getEventTraceRdbDataSource());
            factory.addConstructorArgValue(jobEventRdbFactory.getBeanDefinition());
        }

        factory.addConstructorArgValue(elasticJobListeners);
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)this.applicationContext.getAutowireCapableBeanFactory();
        defaultListableBeanFactory.registerBeanDefinition("SpringJobScheduler", factory.getBeanDefinition());
        SpringJobScheduler springJobScheduler = (SpringJobScheduler)this.applicationContext.getBean("SpringJobScheduler");
        springJobScheduler.init();
        log.info("【" + "{}" + "】\t" + "{}" + "\tinit success", job.getJobName(), job.getJobClass());
    }

    private List<BeanDefinition> getElasticJobListeners(Job job) {
        List<BeanDefinition> result = new ManagedList<>(2);
        String listeners = job.getListener();
        if (StringUtils.hasText(listeners)) {
            result.add(ListenerBeanDefinitionUtil.getJobListenerBeanDefinition(listeners));
        }

        String distributedListeners = job.getDistributedListener();
        long startedTimeoutMilliseconds = job.getStartedTimeoutMilliseconds();
        long completedTimeoutMilliseconds = job.getCompletedTimeoutMilliseconds();
        if (StringUtils.hasText(distributedListeners)) {
            result.add(ListenerBeanDefinitionUtil.getJobDistributedListenersBeanDefinition(distributedListeners, startedTimeoutMilliseconds, completedTimeoutMilliseconds));
        }

        return result;
    }

    public void removeJob(String jobName) throws Exception {
        CuratorFramework client = this.zookeeperRegistryCenter.getClient();
        client.delete().deletingChildrenIfNeeded().forPath("/" + jobName);
    }

    public void monitorJobRegister() {
        CuratorFramework client = this.zookeeperRegistryCenter.getClient();
        PathChildrenCache childrenCache = new PathChildrenCache(client, "/", true);
        PathChildrenCacheListener childrenCacheListener = (childClient, event) -> {
            ChildData data = event.getData();
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                String config = new String(childClient.getData().forPath(data.getPath() + "/config"));
                Job job = JsonUtils.toBean(Job.class, config);
                if (!JOB_ADD_COUNT.containsKey(job.getJobName())) {
                    JOB_ADD_COUNT.put(job.getJobName(), new AtomicInteger());
                }

                int count = JOB_ADD_COUNT.get(job.getJobName()).incrementAndGet();
                if (count > 1) {
                    this.addJob(job);
                }
            }
        };
        childrenCache.getListenable().addListener(childrenCacheListener);

        try {
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
