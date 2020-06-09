# Elastic-job 的 starter 工程
### application.yml
```yaml
elastic:
  job:
    zk:
      # 连接Zookeeper服务器的列表.
      # 包括IP地址和端口号.
      # 多个地址用逗号分隔.
      # 如: host1:2181,host2:2181
      serverLists: "192.168.99.100:2181"
      # 命名空间
      namespace: "elastic-job-demo"
      # 等待重试的间隔时间的初始值（毫秒）
      baseSleepTimeMilliseconds: 5000
      # 等待重试的间隔时间的最大值（毫秒）
      maxSleepTimeMilliseconds: 5000
      # 最大重试次数
      maxretries: 3
      # 会话超时时间毫秒（毫秒）
      sessionTimeoutMilliseconds: 6000
      # 连接超时毫秒（毫秒）
      connectionTimeoutMilliseconds: 6000
      # 连接Zookeeper的权限令牌.
      digest:

    simpleJob:
      cron: 0/5 * * * * ?
      shardingTotalCount: 3
      shardingItemParameters: 0=A,1=B,2=C
      overwrite: true #配置覆盖，去中心
      failover: true #失效转移
      misfire: true #错过机制
      description: init

    simpleJob2:
      cron: 0/5 * * * * ?
      shardingTotalCount: 3
      shardingItemParameters: 0=A,1=B,2=C
      overwrite: true #配置覆盖，去中心
      failover: true #失效转移
      misfire: true #错过机制
      description: init
```
---
### java code
```java
package xyz.sunziyue.service.schedule;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.sunziyue.elastic.job.annotation.ElasticJobConfig;

/**
 * @author Sunziyue
 */
@Slf4j
@Component
@ElasticJobConfig(name = "simpleJob")
public class JobAll implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        log.info(shardingContext.getShardingParameter());
    }
}
```
---
### elastic-job 配置参数说明
> 更多详情请阅读官网elastic-job-lite文档 https://github.com/elasticjob/elastic-job-doc/blob/master/elastic-job-lite-doc/content/00-overview/index.md

#### ZookeeperConfiguration属性详细说明

| 属性名                         | 类型    | 构造器注入 | 缺省值 | 描述 |
| ------------------------------|:--------|:---------|:-------|:----|
| serverLists                   | String  | 是     |        | 连接Zookeeper服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                     | String  | 是     |        | Zookeeper的命名空间 |
| baseSleepTimeMilliseconds     | int     | 否       | 1000   | 等待重试的间隔时间的初始值<br />单位：毫秒 |
| maxSleepTimeMilliseconds      | String  | 否       | 3000   | 等待重试的间隔时间的最大值<br />单位：毫秒 |
| maxRetries                    | String  | 否       | 3      | 最大重试次数 |
| sessionTimeoutMilliseconds    | boolean | 否       | 60000  | 会话超时时间<br />单位：毫秒 |
| connectionTimeoutMilliseconds | boolean | 否       | 15000  | 连接超时时间<br />单位：毫秒 |
| digest                        | String  | 否       |        | 连接Zookeeper的权限令牌<br />缺省为不需要权限验证 |


#### JobCoreConfiguration属性详细说明

| 属性名                  | 类型    | 构造器注入 | 缺省值 | 描述     |
| -----------------------|:--------|:---------|:-------|:--------|
| jobName                | String  | 是       |        | 作业名称 |
| cron                   | String  | 是       |        | cron表达式，用于控制作业触发时间 |
| shardingTotalCount     | int     | 是       |        | 作业分片总数 |
| shardingItemParameters | String  | 否       |        | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从0开始，不可大于或等于作业分片总数<br />如：<br/>0=a,1=b,2=c |
| jobParameter           | String  | 否       |        | 作业自定义参数<br />作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业<br />例：每次获取的数据量、作业实例从数据库读取的主键等 |
| failover               | boolean | 否       | false  | 是否开启任务执行失效转移，开启表示如果作业在一次任务执行中途宕机，允许将该次未完成的任务在另一作业节点上补偿执行 |
| misfire                | boolean | 否       | true   | 是否开启错过任务重新执行 |
| description            | String  | 否       |        | 作业描述信息 |
| jobProperties          | Enum    | 否       |        | 配置jobProperties定义的枚举控制Elastic-Job的实现细节<br />JOB_EXCEPTION_HANDLER用于扩展异常处理类<br />EXECUTOR_SERVICE_HANDLER用于扩展作业处理线程池类|